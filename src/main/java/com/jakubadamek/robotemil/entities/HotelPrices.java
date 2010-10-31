package com.jakubadamek.robotemil.entities;

import java.util.ArrayList;
import java.util.List;

public class HotelPrices {
    private List<HotelPrice> hotelPrices = new ArrayList<HotelPrice>();

    /**
     * @return the hotelPrices
     */
    public List<HotelPrice> getHotelPrices() {
	return hotelPrices;
    }

    /**
     * @param hotelPrices the hotelPrices to set
     */
    public void setHotelPrices(List<HotelPrice> hotelPrices) {
	this.hotelPrices = hotelPrices;
    }
}
