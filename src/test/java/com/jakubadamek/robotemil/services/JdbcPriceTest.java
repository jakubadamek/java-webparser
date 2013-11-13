package com.jakubadamek.robotemil.services;

import java.util.Collections;
import java.util.Date;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Configurable;

import com.jakubadamek.robotemil.DateLosWeb;
import com.jakubadamek.robotemil.Prices;
import com.jakubadamek.robotemil.services.util.DateUtil;

@Configurable(autowire = Autowire.BY_NAME)
public class JdbcPriceTest extends SpringTransactionalTest {
    private final Logger logger = Logger.getLogger(getClass());
	
	private PriceService jdbcPriceService;

	@Before
	public void init() {
		jdbcPriceService = (PriceService) applicationContext.getBean("jdbcPriceService");		
	}
	
	@Test
	public void testLookupSql() {
		Date date = new DateTime(2010, 4, 4, 12, 0, 0, 0).toDate();
		String sql = JdbcPriceService.lookupSql(Collections.singletonList(date), Collections.singletonList(1), Collections.singletonList("hrs"));
		Assert.assertEquals(sql, 
				"SELECT * FROM " + JdbcPriceService.TABLE_PRICES + " WHERE Web IN ('hrs') AND LengthOfStay IN (1) "
				+ "AND Date IN (" + date.getTime() + ") AND DaysBefore=(Date - " + new Date().getTime() + ") / " + DateUtil.MILLIS_PER_DAY);
		logger.info(sql);		
	}
	
	@Test
	public void testLookup() {
		
	}
	
	@Test
	public void testReadStorePrices() {
		init();
		Date date = new DateTime(2010, 4, 4, 12, 0, 0, 0).toDate();
		DateLosWeb key = new DateLosWeb(date, 1, "hrs");
		Prices prices = new Prices();
		prices.addPrice("hotel1", key, 1400, 2, true);
		prices.addPrice("hotel2", key, 1300, 3, true);
		jdbcPriceService.persistPrices(prices, key);
		
		Prices prices2 = new Prices();
		int rows = jdbcPriceService.readPrices(prices2, key);
		Assert.assertEquals(2, rows);
		Assert.assertEquals(2, prices2.size());
		Assert.assertEquals(2, prices2.findHotel("hotel1", key).order);
		Assert.assertEquals(3, prices2.findHotel("hotel2", key).order);		
		Assert.assertEquals(1300, prices2.findHotel("hotel2", key).price);

		jdbcPriceService.persistPrices(prices, key);
	}
}
