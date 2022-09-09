package br.ce.wcaquino.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import br.ce.wcaquino.servicos.CalculoValorLocacaoTest;
import br.ce.wcaquino.servicos.LocacaoServiceTest;
import br.ce.wcaquino.servicos.LocacaoServiceTest_PowerMock;

@RunWith(Suite.class)
@SuiteClasses({
	CalculoValorLocacaoTest.class,
	LocacaoServiceTest.class,
	LocacaoServiceTest_PowerMock.class
})
public class SuiteExecucao {

}
