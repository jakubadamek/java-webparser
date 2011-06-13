package com.jakubadamek.robotemil;

import java.io.Serializable;
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
	/** Data: hotel name -> work unit key -> price and order */
	private Map<String, Map<WorkUnitKey, PriceAndOrder>> data = new HashMap<String, Map<WorkUnitKey,PriceAndOrder>>();

	/**
	 * Adds a price
	 *
	 * @param hotel
	 * @param key
	 * @param price
	 * @param order
	 */
	public synchronized void addPrice(String hotel, WorkUnitKey key, Double price, int order) {
		if(! this.data.containsKey(hotel)) {
			this.data.put(hotel, new HashMap<WorkUnitKey, PriceAndOrder>());
		}
		if(price != null) {
    		PriceAndOrder priceAndOrder = new PriceAndOrder();
    		priceAndOrder.price = price;
    		priceAndOrder.order = order;
    		this.data.get(hotel).put(key, priceAndOrder);
		}
	}

	/**
	 * Adds all prices
	 *
	 * @param prices
	 */
	public synchronized void addAll(Prices prices) {
		for(String hotel : prices.data.keySet()) {
			Map<WorkUnitKey, PriceAndOrder> map = prices.data.get(hotel);
			for(WorkUnitKey key : map.keySet()) {
				PriceAndOrder priceAndOrder = map.get(key);
				addPrice(hotel, key, priceAndOrder.price, priceAndOrder.order);
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
	public Map<String, Map<WorkUnitKey, PriceAndOrder>> getData() {
		return this.data;
	}
	
	public String findHotelName(String hotelNamePart) {
        String hotelName = hotelNamePart; 
	    if(this.data.containsKey(hotelNamePart)) {
	    	return hotelNamePart;
	    }
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
	    if(this.data.containsKey(hotelName)) {
	    	return hotelName;
	    }
	    return null;
	}
	
	public PriceAndOrder findHotel(String hotelNamePart, WorkUnitKey key) {
		String hotelName = findHotelName(hotelNamePart);
	    if(hotelName != null) {
	        return data.get(hotelName).get(key);
	    }
	    return null;
	}
}