package com.jakubadamek.robotemil.services.util;

import java.util.Date;

public class DateUtil {
	public static final long MILLIS_PER_DAY = 24 * 3600 * 1000;
	
	public static Date trunc(Date date) { 
		return new Date((date.getTime() / MILLIS_PER_DAY) * MILLIS_PER_DAY);
	}
	
	public static int daysBefore(Date date1, Date date2) {
		return (int) ((trunc(date1).getTime() - trunc(date2).getTime()) / MILLIS_PER_DAY);
	}	
}
