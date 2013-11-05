package com.jakubadamek.robotemil.htmlparser;

import java.io.IOException;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Test;

public class ExchangeRateTest {
    private static final Logger logger = Logger.getLogger(ExchangeRateTest.class);

	@Test
	public void test() throws IOException {
		double value = new ExchangeRate().currentUsdEur();
		Assert.assertFalse(value == 0);
		logger.info("EUR to USD is " + value);
	}
}
