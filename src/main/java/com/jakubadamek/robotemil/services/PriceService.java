package com.jakubadamek.robotemil.services;

import java.util.Set;

import com.jakubadamek.robotemil.DateLosWeb;
import com.jakubadamek.robotemil.Prices;
import com.jakubadamek.robotemil.services.util.IWebToPrices;

public interface PriceService {
    void persistPrices(Prices prices, DateLosWeb key);

    int readPrices(final Prices prices, DateLosWeb key);
    
    void createTables();

    void deleteRefreshedData(DateLosWeb key);
    
    /**
     * Looks up dateLosWebs - and what is found, removes from the set.
     * @param minRows TODO
     */
	int lookup(Set<DateLosWeb> dateLosWebs, IWebToPrices webToPrices, int minRows);    
}
