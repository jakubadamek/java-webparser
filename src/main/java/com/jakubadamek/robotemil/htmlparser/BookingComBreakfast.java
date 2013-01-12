package com.jakubadamek.robotemil.htmlparser;


/**
 * Parses booking.com, cares about breakfasts
 *
 * @author Jakub
 */
public class BookingComBreakfast extends BookingCom {
	public BookingComBreakfast() {
		super();
		careAboutBreakfast = true;
	}	
}
