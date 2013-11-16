package com.jakubadamek.robotemil.htmlparser;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExchangeRateTest {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateTest.class);

	@Test
	public void test() throws IOException {
		double value = new ExchangeRate().currentUsdEur();
		Assert.assertFalse(value == 0);
		logger.info("EUR to USD is " + value);
	}
}
