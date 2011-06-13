package com.jakubadamek.robotemil.htmlparser;

import junit.framework.Assert;

import org.htmlparser.util.ParserException;
import org.junit.Test;

public class ExchangeRateTest {

	@Test
	public void test() throws ParserException {
		Assert.assertFalse(new ExchangeRate().currentUsdEur() == 0);
	}
}
