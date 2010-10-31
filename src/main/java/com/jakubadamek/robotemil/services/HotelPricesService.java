package com.jakubadamek.robotemil.services;

import java.util.Date;

import com.jakubadamek.robotemil.entities.HotelPrices;

public interface HotelPricesService {
    HotelPrices findPrices(String web, Date bookingDate, short dayCount, boolean useCache);
}
