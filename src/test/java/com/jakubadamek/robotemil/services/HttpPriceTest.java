package com.jakubadamek.robotemil.services;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.Test;

import com.jakubadamek.robotemil.DateLosWeb;
import com.jakubadamek.robotemil.Prices;
import com.jakubadamek.robotemil.services.util.IWebToPrices;

public class HttpPriceTest extends SpringTransactionalTest {
	
	protected PriceService httpPriceService;
	protected PriceService jdbcPriceService;
	
	@Test
	public void testReadStorePrices() {
		httpPriceService = (PriceService) applicationContext.getBean("httpPriceService");
		jdbcPriceService = (PriceService) applicationContext.getBean("jdbcPriceService");

		Date date = new DateTime(2090, 4, 4, 12, 0, 0, 0).toDate();
		DateLosWeb key = new DateLosWeb(date, 1, "hrs");
		
		Prices prices = new Prices();
		prices.addPrice("hotel1", key, 1400, 2, true);
		prices.addPrice("hotel2", key, 1300, 3, true);
		httpPriceService.persistPrices(prices, key);
		httpPriceService.persistPrices(prices, key);
		
		final Prices prices2 = new Prices();
		IWebToPrices webToPrices = new IWebToPrices() {			
			@Override
			public Prices get(String webExcelName) {
				return prices2;
			}
		};
		Set<DateLosWeb> dateLosWebs = new HashSet<DateLosWeb>();
		dateLosWebs.add(key);
		Assert.assertEquals(1, httpPriceService.lookup(dateLosWebs, webToPrices, 0));
		//priceService.readPrices(prices2, key);
		Assert.assertEquals(2, prices2.size());
		Assert.assertEquals(2, prices2.findHotel("hotel1", key).order);
		Assert.assertEquals(3, prices2.findHotel("hotel2", key).order);		
		Assert.assertEquals(1300, prices2.findHotel("hotel2", key).price);		
		
		dateLosWebs.add(key);
		Assert.assertEquals(1, jdbcPriceService.lookup(dateLosWebs, webToPrices, 0));
	}
}
