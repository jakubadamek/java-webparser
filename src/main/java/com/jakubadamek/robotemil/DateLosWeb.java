package com.jakubadamek.robotemil;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateLosWeb {
	private static final long DATE_PRECISION = 24 * 3600 * 1000;
	
	private final String webExcelName;
	/** date */
	private final Date date;
	/** length of stay */
	private int lengthOfStay;
	
	private static DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("dd.MM.yyyy");
	
	public DateLosWeb(Date date, int lengthOfStay, String web) {
		this.date = date;
		this.lengthOfStay = lengthOfStay;
		this.webExcelName = web;
	}
	
	public DateLosWeb(Date date, int lengthOfStay, WebStruct web) {
		this(date, lengthOfStay, web.getParams().getExcelName());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : (int) (date.getTime() / DATE_PRECISION));
		result = prime * result + lengthOfStay;
		result = prime * result
				+ ((webExcelName == null) ? 0 : webExcelName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DateLosWeb other = (DateLosWeb) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (other.date == null) {
			return false;
		} else if(date.getTime() / DATE_PRECISION != other.date.getTime() / DATE_PRECISION)
			return false;
		if (lengthOfStay != other.lengthOfStay)
			return false;
		if (webExcelName == null) {
			if (other.webExcelName != null)
				return false;
		} else if (!webExcelName.equals(other.webExcelName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return dateTimeFormatter.print(new DateTime(date)) + " LOS " + lengthOfStay + " web " + getWeb();
	}

	public String getWeb() {
		return webExcelName;
	}	

	public Date getDate() {
		return date;
	}
	
	public int getLengthOfStay() {
		return lengthOfStay;
	}

}
