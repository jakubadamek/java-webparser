package com.jakubadamek.robotemil;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jxl.common.Logger;

import com.jakubadamek.robotemil.entities.PriceAndOrder;


/**
 * Prices for hotel rooms
 */
public class Prices implements Serializable {
    private static final Logger logger = Logger.getLogger(Prices.class);
	private static final long serialVersionUID = 4924511073557468113L;
	/** Data */
	private Map<String, Map<Date, PriceAndOrder>> data = new HashMap<String, Map<Date, PriceAndOrder>>();

	/**
	 * Adds a price
	 *
	 * @param hotel
	 * @param date
	 * @param price
	 * @param order
	 */
	public synchronized void addPrice(String hotel, Date date, String price, int order) {
		addPrice(hotel, date, price == null ? null : Double.valueOf(price), order);
	}

	public synchronized void addPrice(String hotel, Date date, Double price, int order) {
		if(! this.data.containsKey(hotel)) {
			this.data.put(hotel, new HashMap<Date, PriceAndOrder>());
		}
		if(price != null) {
    		PriceAndOrder priceAndOrder = new PriceAndOrder();
    		priceAndOrder.price = price;
    		priceAndOrder.order = order;
    		this.data.get(hotel).put(date, priceAndOrder);
		}
	}

	/**
	 * Adds all prices
	 *
	 * @param prices
	 */
	public synchronized void addAll(Prices prices) {
		for(String hotel : prices.data.keySet()) {
			Map<Date, PriceAndOrder> map = prices.data.get(hotel);
			for(Date date : map.keySet()) {
				PriceAndOrder priceAndOrder = map.get(date);
				addPrice(hotel, date, priceAndOrder.price, priceAndOrder.order);
			}
		}
	}

	public synchronized long size() {
		long retval = 0;
		for(String hotel : data.keySet()) {
			retval += data.get(hotel).size();
		}
		return retval;
	}
	
	/**
	 * Concurrency: Always lock the object before using getData.
	 * @return the inner data structure
	 */
	public Map<String, Map<Date, PriceAndOrder>> getData() {
		return this.data;
	}
	
	public PriceAndOrder findHotel(String hotelNamePart, Date date) {
        String hotelName = hotelNamePart; 
	    if(! this.data.containsKey(hotelNamePart)) {
    	    for(String hotel : this.data.keySet()) {
    	        if(hotel.contains(hotelNamePart)) {
    	            if(hotelNamePart.equals(hotelName)) {
    	                hotelName = hotel;
    	            } else {
                        logger.info("Duplicit hotelNamePart: " + hotelNamePart + " = " + hotelName + " x " + hotel);
    	                hotelName = null;
    	            }
    	        }
    	    }
	    }
	    if(this.data.containsKey(hotelName)) {
	        return data.get(hotelName).get(date);
	    }
	    return null;
	}
}