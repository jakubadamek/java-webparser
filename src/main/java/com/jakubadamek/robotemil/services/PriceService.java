package com.jakubadamek.robotemil.services;

import com.jakubadamek.robotemil.Prices;
import com.jakubadamek.robotemil.WorkUnitKey;

public interface PriceService {
    void persistPrices(String web, Prices prices, WorkUnitKey key);

    int readPrices(final String web, final Prices prices, WorkUnitKey key);
    
    void createTables();
}
