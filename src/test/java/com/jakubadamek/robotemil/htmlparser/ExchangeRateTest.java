package com.jakubadamek.robotemil.htmlparser;

import junit.framework.Assert;

import org.junit.Test;

public class ExchangeRateTest {

	@Test
	public void test() {
		Assert.assertFalse(new ExchangeRate().currentUsdEur() == 0);
	}
}
