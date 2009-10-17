package com.jakubadamek.robotemil;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Prices for hotel rooms
 */
public class Prices implements Serializable {
	private static final long serialVersionUID = 4924511073557468113L;
	/** Data */
	public Map<String, Map<Date, PriceAndOrder>> data = new HashMap<String, Map<Date, PriceAndOrder>>();

	/**
	 * Adds a price
	 *
	 * @param hotel
	 * @param date
	 * @param price
	 * @param order
	 */
	public void addPrice(String hotel, Date date, String price, int order) {
		addPrice(hotel, date, Double.valueOf(price), order);
	}

	public synchronized void addPrice(String hotel, Date date, Double price, int order) {
		if(! this.data.containsKey(hotel)) {
			this.data.put(hotel, new HashMap<Date, PriceAndOrder>());
		}
		PriceAndOrder priceAndOrder = new PriceAndOrder();
		priceAndOrder.price = price;
		priceAndOrder.order = order;
		this.data.get(hotel).put(date, priceAndOrder);
	}

	/**
	 * Adds all prices
	 *
	 * @param prices
	 */
	public void addAll(Prices prices) {
		for(String hotel : prices.data.keySet()) {
			Map<Date, PriceAndOrder> map = prices.data.get(hotel);
			for(Date date : map.keySet()) {
				PriceAndOrder priceAndOrder = map.get(date);
				addPrice(hotel, date, priceAndOrder.price, priceAndOrder.order);
			}
		}
	}

	public long size() {
		long retval = 0;
		for(String hotel : data.keySet()) {
			retval += data.get(hotel).size();
		}
		return retval;
	}
}