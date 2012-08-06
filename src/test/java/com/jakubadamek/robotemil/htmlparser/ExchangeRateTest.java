package com.jakubadamek.robotemil.htmlparser;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

public class ExchangeRateTest {

	@Test
	public void test() throws IOException {
		Assert.assertFalse(new ExchangeRate().currentUsdEur() == 0);
	}
}
