package com.jakubadamek.robotemil.services;

import java.util.Date;

import com.jakubadamek.robotemil.Prices;

public interface PriceService {
    void persistPrices(String web, Prices prices, Date date);

    int readPrices(final String web, final Prices prices, final Date date);
}
