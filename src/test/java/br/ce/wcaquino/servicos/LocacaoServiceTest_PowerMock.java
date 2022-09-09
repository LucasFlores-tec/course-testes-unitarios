package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.builders.FilmeBuilder.umFilme;
import static br.ce.wcaquino.builders.UsuarioBuilder.umUsuario;
import static br.ce.wcaquino.matchers.MatchersProprios.caiNumaSegunda;
import static br.ce.wcaquino.matchers.MatchersProprios.ehHoje;
import static br.ce.wcaquino.matchers.MatchersProprios.ehHojeComDiferencaDias;
import static br.ce.wcaquino.utils.DataUtils.obterData;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LocacaoService.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class LocacaoServiceTest_PowerMock {
	
	@InjectMocks
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
		service = PowerMockito.spy(service);
	}
	
	@Test
	public void deveAlugarFilme() throws Exception {
		// scenario
		Usuario usuario = umUsuario().agora();
		List<Filme> filmes = asList(umFilme().agora());
		whenNew(Date.class).withNoArguments().thenReturn(obterData(9, 9, 2022));
		
		// action
		Locacao locacao = service.alugarFilme(usuario, filmes);
		
		// validation
		error.checkThat(locacao.getValor(), is(4.0));
		error.checkThat(locacao.getDataLocacao(), ehHoje());
		error.checkThat(locacao.getDataRetorno(), ehHojeComDiferencaDias(1));
	}
	
	@Test
	public void deveRetornarFilmeNaSegundaAoAlugarNoSabado() throws Exception {
		// scenario
		Usuario usuario = umUsuario().agora();
		List<Filme> filmes = asList(umFilme().agora());
		whenNew(Date.class).withNoArguments().thenReturn(obterData(10, 9, 2022));
		
		// action
		Locacao retorno = service.alugarFilme(usuario, filmes);
		
		// validation
		assertThat(retorno.getDataRetorno(), caiNumaSegunda());
	}
	
	@Test
	public void deveAlugarFilme_semCalcularValor() throws Exception {
		// scenario
		Usuario usuario = umUsuario().agora();
		List<Filme> filmes = asList(umFilme().agora());
		doReturn(1.0).when(service, "calcularValorLocacao", filmes); // usando PowerMockito para mockar um método privado
		
		// action
		Locacao locacao = service.alugarFilme(usuario, filmes);
		
		// validation
		assertThat(locacao.getValor(), is(1.0));
		verifyPrivate(service).invoke("calcularValorLocacao", filmes);
	}
	
	@Test
	public void deveCalcularValorLocacao() throws Exception {
		// scenario
		List<Filme> filmes = asList(umFilme().agora());
		
		// action
		Double valor = (Double) Whitebox.invokeMethod(service, "calcularValorLocacao", filmes); // usando Whitebox para invocar um método privado
		
		// validation
		assertThat(valor, is(4.0));
	}
	
}
