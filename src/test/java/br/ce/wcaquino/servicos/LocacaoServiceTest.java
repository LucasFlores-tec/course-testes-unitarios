package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.builders.FilmeBuilder.umFilme;
import static br.ce.wcaquino.builders.LocacaoBuilder.umaLocacao;
import static br.ce.wcaquino.builders.UsuarioBuilder.umUsuario;
import static br.ce.wcaquino.matchers.MatchersProprios.caiNumaSegunda;
import static br.ce.wcaquino.matchers.MatchersProprios.ehHoje;
import static br.ce.wcaquino.matchers.MatchersProprios.ehHojeComDiferencaDias;
import static br.ce.wcaquino.utils.DataUtils.isMesmaData;
import static br.ce.wcaquino.utils.DataUtils.obterData;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;

public class LocacaoServiceTest {
	
	@InjectMocks @Spy
	private LocacaoService service;
	@Mock
	private SPCService spc;
	@Mock
	private LocacaoDAO dao;
	@Mock
	private EmailService emailService;
	@Rule
	public ErrorCollector error = new ErrorCollector();
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void deveAlugarFilme() throws Exception {
		// scenario
		Usuario usuario = umUsuario().agora();
		List<Filme> filmes = asList(umFilme().agora());
		
		Mockito.doReturn(obterData(9, 9, 2022)).when(service).obterData();
		
		// action
		Locacao locacao = service.alugarFilme(usuario, filmes);
		
		// validation
		error.checkThat(locacao.getValor(), is(4.0));
		error.checkThat(isMesmaData(locacao.getDataLocacao(), obterData(9, 9, 2022)), is(true));
		error.checkThat(isMesmaData(locacao.getDataRetorno(), obterData(10, 9, 2022)), is(true));
	}
	
	@Test(expected = FilmeSemEstoqueException.class)
	public void deveLancarExcecaoAoAlugarFilmeSemEstoque() throws Exception {
		// scenario
		Usuario usuario = umUsuario().agora();
		List<Filme> filmes = asList(umFilme().semEstoque().agora());
		
		// action
		service.alugarFilme(usuario, filmes);
	}

	@Test
	public void deveLancarExcecaoAoAlugarFilmeSemUsuario() throws FilmeSemEstoqueException {
		// scenario
		List<Filme> filmes = Arrays.asList(umFilme().agora());
		
		// action // validation
		try {
			service.alugarFilme(null, filmes);
			fail();
		} catch (LocadoraException e) {
			assertThat(e.getMessage(), is("Usuario vazio"));
		}
	}
	
	@Test 
	public void deveLancarExcecaoAoAlugarFilmeSemFilme() throws FilmeSemEstoqueException { 
		// scenario
		Usuario usuario = umUsuario().agora(); 
		
		// action // validation
		try {
			service.alugarFilme(usuario, null);
			fail("Deveria retornar uma LocadoraExceptio");
		} catch (LocadoraException e) {
			assertThat(e.getMessage(), is("Filme vazio"));
		} 
	}
	
	@Test
	public void deveRetornarFilmeNaSegundaAoAlugarNoSabado() throws Exception {
		// scenario
		Usuario usuario = umUsuario().agora();
		List<Filme> filmes = asList(umFilme().agora());
		Mockito.doReturn(obterData(10, 9, 2022)).when(service).obterData();
		
		// action
		Locacao retorno = service.alugarFilme(usuario, filmes);
		
		// validation
		assertThat(retorno.getDataRetorno(), caiNumaSegunda());
	}
 
	@Test
	public void naoDeveAlugarFilmeParaNegativadoSpc() throws Exception {
		// scenario
		Usuario usuario = umUsuario().agora();
		List<Filme> filmes = Arrays.asList(umFilme().agora());
		when(spc.possuiNegativacao(usuario)).thenReturn(true);
		
		// action // validation
		assertThrows(LocadoraException.class, () -> service.alugarFilme(usuario, filmes));
	}
	
	@Test
	public void deveEnviarEmailParaLocacoesAtrasadas() {
		// scenario
		Usuario usuario = umUsuario().agora();
		Usuario usuario2 = umUsuario().comNome("Usuario em dia").agora();
		Usuario usuario3 = umUsuario().comNome("Usuario com atraso").agora();
		List<Locacao> locacoes = asList(
				umaLocacao().atrasada().comUsuario(usuario).agora(),
				umaLocacao().atrasada().comUsuario(usuario3).agora(),
				umaLocacao().comUsuario(usuario2).agora());
		when(dao.obterLocacoesPendentes()).thenReturn(locacoes);
		
		// action
		service.notificarAtrasos();
		
		// validation
		Mockito.verify(emailService, Mockito.times(2)).notificarAtraso(Mockito.any(Usuario.class));
		verify(emailService).notificarAtraso(usuario);
		verify(emailService, never()).notificarAtraso(usuario2);
		verify(emailService).notificarAtraso(usuario3);
		verifyNoMoreInteractions(emailService);
	}
	
	@Test
	public void deveTratarErroNoSpc() throws Exception {
		// scenario
		Usuario usuario = umUsuario().agora();
		List<Filme> filmes = Arrays.asList(umFilme().agora());
		
		when(spc.possuiNegativacao(usuario)).thenThrow(new Exception("Falha catastrófica"));
		
		// action // validation
	    assertThrows(Exception.class, () -> service.alugarFilme(usuario, filmes));
	}
	
	@Test
	public void deveProrrogarUmaLocacao() {
		// scenario
		Locacao locacao = umaLocacao().agora();
		
		// action
		service.prorrogarLocacao(locacao, 3);
		
		// validation
		ArgumentCaptor<Locacao> argCapt = forClass(Locacao.class);
		verify(dao).salvar(argCapt.capture());
		Locacao locacaoRetornada = argCapt.getValue();
		
		error.checkThat(locacaoRetornada.getValor(), is(12.0));
		error.checkThat(locacaoRetornada.getDataLocacao(), ehHoje());
		error.checkThat(locacaoRetornada.getDataRetorno(), ehHojeComDiferencaDias(3));
	}
	
	@Test
	public void deveCalcularValorLocacao() throws Exception {
		// scenario
		List<Filme> filmes = asList(umFilme().agora());
		
		// action
		Class<LocacaoService> clazz = LocacaoService.class;
		Method metodo = clazz.getDeclaredMethod("calcularValorLocacao", List.class);
		metodo.setAccessible(true);
		Double valor = (Double) metodo.invoke(service, filmes);
		
		// validation
		assertThat(valor, is(4.0));
	}
	
}
