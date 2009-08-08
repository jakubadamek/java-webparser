package com.jakubadamek.robotemil;

import java.util.ArrayList;
import java.util.List;


/**
 * Info about our hotel (if the app is used in several hotels with different settings)
 * @author Jakub
 */
class OurHotel {
	/**
	 * 
	 */
	private final App app;
	/**
	 * @param app
	 */
	OurHotel(App app) {
		this.app = app;
	}
	/** Web structs */
	List<WebStruct> webStructs = new ArrayList<WebStruct>();
	/** Our hotel name */
	String ourHotelName;
	/** 
	 * @param web 
	 * @return File name for the web 
	 */		
	String fileName(WebStruct web) {
		int index = this.app.ourHotels.indexOf(this);
		if(index == 0) {
			return web.fileName;
		}
		return index + "_" + web.fileName;
	}
}