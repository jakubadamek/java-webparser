package com.jakubadamek.robotemil.htmlparser;

import java.text.DateFormat;
import java.util.Date;

import com.jakubadamek.robotemil.App;
import com.jakubadamek.robotemil.DiacriticsRemover;
import com.jakubadamek.robotemil.Prices;
import com.jakubadamek.robotemil.WorkUnit;


/**
 * Ancestor to the parsing classes
 *
 * @author Jakub
 */
public abstract class HtmlParser {
	/** date for which prices are searched */
	protected Date date;
	/** prices - the result of parsing */
	private Prices prices;
	private int order;
	private App app;
	private WorkUnit workUnit;

	/**
	 * Constructor
	 *
	 * @param aWorkUnit
	 * @param aApp
	 */
	public void init(WorkUnit aWorkUnit, App aApp) {
		this.date = aWorkUnit.date;
		this.workUnit = aWorkUnit;
		this.prices = new Prices();
		this.app = aApp;
		this.order = 0;
	}

	/**
	 * Runs the real job
	 *
	 * @throws Exception
	 */
	public abstract void run() throws Exception;

	private DateFormat dateFormat = DateFormat.getDateInstance();

	/**
	 * Adds price to the inner structure
	 *
	 * @param aHotel
	 * @param aDate
	 * @param price
	 */
	protected void addPrice(String aHotel, Date aDate, String price) {
		this.order ++;
		String hotel = DiacriticsRemover.removeDiacritics(aHotel.replace("&amp;", "&"));
		final String logRow = getClass().getSimpleName() + " " + this.order + " " + hotel + " " + price + " "
			+ this.dateFormat.format(aDate);
		System.out.println(logRow);
		if(this.app != null) {
			this.app.showLog(logRow);
		}
		this.prices.addPrice(hotel, aDate, price, this.order);
	}

	/**
	 * @return the stop
	 */
	public boolean isStop() {
		this.workUnit.lastResponseTime = new Date();
		return this.workUnit.finished || App.stop;
	}

	/**
	 * @return the prices
	 */
	public Prices getPrices() {
		return this.prices;
	}
}
