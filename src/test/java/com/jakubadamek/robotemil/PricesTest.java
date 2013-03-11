package com.jakubadamek.robotemil;

import junit.framework.Assert;

import org.junit.Test;

public class PricesTest {
	@Test
	public void testFindHotel() {
		Prices prices = new Prices();
		prices.getData().put("Ahoj", null);
		prices.getData().put("AhojAhoj", null);
		
		Assert.assertEquals("Ahoj", prices.findHotelName("Ahoj"));
		Assert.assertEquals(null, prices.findHotelName("Aho"));
		Assert.assertEquals("AhojAhoj", prices.findHotelName("AhojA"));

		Assert.assertEquals("Ahoj", prices.findHotelName("ahOJ"));
		Assert.assertEquals(null, prices.findHotelName("aho"));
		Assert.assertEquals("AhojAhoj", prices.findHotelName("ahoja"));

		Assert.assertEquals(null, prices.findHotelName("ahojahoja"));
	}
}
