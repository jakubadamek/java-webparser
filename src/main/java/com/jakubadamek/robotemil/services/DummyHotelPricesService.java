package com.jakubadamek.robotemil.services;

import java.util.Date;

import com.jakubadamek.robotemil.entities.HotelPrices;

public class DummyHotelPricesService implements HotelPricesService {

    public HotelPrices findPrices(String web, Date bookingDate, short dayCount, boolean useCache) {
	return new HotelPrices();
    }

}
