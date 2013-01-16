package com.jakubadamek.robotemil.services;

import java.util.Date;

import junit.framework.Assert;

import org.joda.time.DateTime;

import com.jakubadamek.robotemil.Prices;
import com.jakubadamek.robotemil.WorkUnitKey;

public class JdbcPriceTest extends SpringTransactionalTest {
	
	private PriceService priceService;
	
	public void setPriceService(PriceService priceService) {
		this.priceService = priceService;
	}

	public void testReadStorePrices() {
		Date date = new DateTime(2010, 4, 4, 12, 0, 0, 0).toDate();
		WorkUnitKey key = new WorkUnitKey(date, 1);
		Prices prices = new Prices();
		prices.addPrice("hotel1", key, Double.valueOf(14), 2, true);
		prices.addPrice("hotel2", key, Double.valueOf(13), 3, true);
		priceService.persistPrices("web", prices, key);
		priceService.persistPrices("web", prices, key);
		
		Prices prices2 = new Prices();
		priceService.readPrices("web", prices2, key);
		Assert.assertEquals(2, prices2.size());
		Assert.assertEquals(2, prices2.findHotel("hotel1", key).order);
		Assert.assertEquals(3, prices2.findHotel("hotel2", key).order);		
		Assert.assertEquals(13, prices2.findHotel("hotel2", key).price, 0.001);		
	}
}
