package com.jakubadamek.robotemil;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class WorkUnitKey {
	/** date */
	private final Date date;
	/** length of stay */
	private int lengthOfStay;
	
	private static DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("dd.MM.yyyy");
	
	public WorkUnitKey(Date date, int lengthOfStay) {
		this.date = date;
		this.lengthOfStay = lengthOfStay;
	}
	
	public Date getDate() {
		return date;
	}
	
	public int getLengthOfStay() {
		return lengthOfStay;
	}

	@Override
	public String toString() {
		return dateTimeFormatter.print(new DateTime(date)) + " LOS " + lengthOfStay;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + lengthOfStay;
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
		WorkUnitKey other = (WorkUnitKey) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (lengthOfStay != other.lengthOfStay)
			return false;
		return true;
	}
}
