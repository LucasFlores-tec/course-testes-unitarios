package br.ce.wcaquino.servicos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import br.ce.wcaquino.exceptions.NaoPodeDividirPorZeroException;

public class CalculadoraTest {
	
	private Calculadora calc;
	
	@Before
	public void setup() {
		calc = new Calculadora();
	}
	
	@Test
	public void deveSomarDoisValores() {
		// scenario
		int a = 5;
		int b = 3;
		
		// action
		int resultado = calc.somar(a, b);
		
		// validation
		Assert.assertEquals(8, resultado);
	}
	
	@Test
	public void deveSubtrairDoisValores() {
		// scenario
		int a = 8;
		int b = 5;
		
		// action
		int resultado = calc.subtrair(a, b);
		
		// validation
		assertEquals(3, resultado);
	}
	
	@Test
	public void deveDividirDoisValores() throws NaoPodeDividirPorZeroException {
		// scenario
		int a = 10;
		int b = 5;
		
		// action
		int resultado = calc.divide(a, b);
		
		// validation
		assertEquals(2, resultado);
	}
	
	@Test
	public void deveLancarExcecaoAoDividirPorZero() {
		// scenario
		int a = 10;
		int b = 0;
		
		// action
		assertThrows(NaoPodeDividirPorZeroException.class, () -> calc.divide(a, b));
	}

}
