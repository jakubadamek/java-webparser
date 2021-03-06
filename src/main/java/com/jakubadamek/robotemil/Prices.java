package com.jakubadamek.robotemil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jxl.common.Logger;

import org.springframework.util.Assert;

import com.jakubadamek.robotemil.entities.PriceAndOrder;


/**
 * Prices for hotel rooms
 */
public class Prices implements Serializable {
    private static final Logger logger = Logger.getLogger(Prices.class);
	private static final long serialVersionUID = 4924511073557468113L;
	/** Data: hotel name -> work unit key -> price and order */
	private Map<String, Map<DateLosWeb, PriceAndOrder>> data = new HashMap<String, Map<DateLosWeb, PriceAndOrder>>();

	/**
	 * Adds a price
	 *
	 * @param hotel
	 * @param key
	 * @param price
	 * @param order
	 */
	public synchronized void addPrice(String hotel, DateLosWeb key, int price, int order, boolean breakfastIncluded) {
		if(! this.data.containsKey(hotel)) {
			this.data.put(hotel, new HashMap<DateLosWeb, PriceAndOrder>());
		}
		PriceAndOrder priceAndOrder = new PriceAndOrder();
		priceAndOrder.price = price;
		priceAndOrder.order = order;
		priceAndOrder.breakfastIncluded = breakfastIncluded;
		 
		PriceAndOrder previousPrice = this.data.get(hotel).get(key);
		if(previousPrice != null) {
			if(! previousPrice.breakfastIncluded && breakfastIncluded) {
				logger.info("Replacing price without breakfast " + previousPrice.price + " by " + price);
				this.data.get(hotel).put(key, priceAndOrder);
			}
		} else {
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
			Map<DateLosWeb, PriceAndOrder> map = prices.data.get(hotel);
			for(DateLosWeb key : map.keySet()) {
				PriceAndOrder priceAndOrder = map.get(key);
				addPrice(hotel, key, priceAndOrder.price, priceAndOrder.order, priceAndOrder.breakfastIncluded);
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
	public Map<String, Map<DateLosWeb, PriceAndOrder>> getData() {
		return this.data;
	}

	private interface HotelNameMatcher {
		boolean matches(String hotelName);
	}
	
	public String findHotelName(final String hotelNamePart) {
		Assert.notNull(hotelNamePart);
		
		HotelNameMatcher[] matchers = new HotelNameMatcher[] {
			// first look for exact match
			new HotelNameMatcher() {
				@Override
				public boolean matches(String hotel) {
					return hotel.toLowerCase().equals(hotelNamePart.toLowerCase());
				}
			},
		    // next look for partial match from start
			new HotelNameMatcher() {
				@Override
				public boolean matches(String hotel) {
					return hotel.toLowerCase().startsWith(hotelNamePart.toLowerCase());
				}
			},
		    // next look for partial match anywhere
			new HotelNameMatcher() {
				@Override
				public boolean matches(String hotel) {
					return hotel.toLowerCase().contains(hotelNamePart.toLowerCase());
				}
			}						
		};
		
		for(HotelNameMatcher matcher : matchers) {
	        String hotelName = null;
	        int matchCount = 0;
		    for(String hotel : this.data.keySet()) {
		        if(matcher.matches(hotel)) {
		        	if (hotelName != null) {
		        		logger.info("Hotel name conflict: " + hotelName + " vs " + hotel);
		        	}
	                hotelName = hotel;
		        	matchCount ++;
		        }
		    }
		    if(matchCount == 1) {
		    	return hotelName;
		    }
		}
    	return null;
	}
	
	public PriceAndOrder findHotel(String hotelNamePart, DateLosWeb key) {
		String hotelName = findHotelName(hotelNamePart);
	    if(hotelName != null) {
	        return data.get(hotelName).get(key);
	    }
	    return null;
	}
	
	/**
	 * Retains only those hotels which are required for the hotel list in the webStruct.
	 * @param webStruct
	 */
	public void filterHotelNames(WebStruct webStruct) {
		Map<String, Map<DateLosWeb, PriceAndOrder>> filtered = new HashMap<String, Map<DateLosWeb,PriceAndOrder>>();
		Set<String> foundHotelNames = new HashSet<String>();
		for(String hotel : webStruct.getHotelList()) {
			foundHotelNames.add(findHotelName(hotel));
		}
		for(String foundHotelName : foundHotelNames) {
			if(data.containsKey(foundHotelName)) {
				filtered.put(foundHotelName, data.get(foundHotelName));
			}
		}
		data = filtered;
	}
}