package com.jakubadamek.robotemil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Text;

import com.jakubadamek.robotemil.htmlparser.HtmlParser;


/** web struct */
class WebStruct implements Serializable {
	private static final long serialVersionUID = 7941780928540322765L;
	/** label */
	String label;
	/** file name */
	String fileName;
	/** Excel name */
	String excelName;
	/** Icon file name */
	String iconName;
	/** hotel text fields */
	transient List<Text> hotelTexts = new ArrayList<Text>();
	/** hotel names, used to initialize hotelTexts */
	List<String> hotelList = new ArrayList<String>();
	/** prices */ 
	Prices prices = new Prices();
	/** class used to download the data */
	Class<? extends HtmlParser> parserClass;
	
	/**
	 * Is hotel with this index in the list?
	 * @param index
	 * @return true or false
	 */
	boolean hasHotel(int index) {
		return this.hotelTexts.size() > index && this.hotelTexts.get(index).getText().trim().length() > 0;		
		//return this.hotelList.size() > index && this.hotelList.get(index).trim().length() > 0;
	}
}