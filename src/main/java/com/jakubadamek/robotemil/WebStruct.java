package com.jakubadamek.robotemil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Text;

/** web struct */
public class WebStruct implements Serializable {
    private static final long serialVersionUID = 7941780928540322765L;
    /** hotel text fields */
    private transient List<Text> hotelTexts = new ArrayList<Text>();
    /** hotel names, used to initialize hotelTexts */
    private List<String> hotelList = new ArrayList<String>();
    /** prices */
    private Prices prices = new Prices();
    
	private WebParams params = new WebParams();

    /**
     * Is hotel with this index in the list?
     * @param index
     * @return true or false
     */
    public boolean hasHotel(int index) {
	return this.getHotelTexts().size() > index && this.getHotelTexts().get(index).getText().trim().length() > 0;
	// return this.hotelList.size() > index && this.hotelList.get(index).trim().length() > 0;
    }

    /**
     * @param hotelTexts the hotelTexts to set
     */
    public void setHotelTexts(List<Text> hotelTexts) {
	this.hotelTexts = hotelTexts;
    }

    /**
     * @return the hotelTexts
     */
    public List<Text> getHotelTexts() {
	return hotelTexts;
    }

    /**
     * @param hotelList the hotelList to set
     */
    public void setHotelList(List<String> hotelList) {
	this.hotelList = hotelList;
    }

    /**
     * @return the hotelList
     */
    public List<String> getHotelList() {
	return hotelList;
    }

    /**
     * @param prices the prices to set
     */
    public void setPrices(Prices prices) {
	this.prices = prices;
    }

    /**
     * @return the prices
     */
    public Prices getPrices() {
	return prices;
    }

    public WebParams getParams() {
		return params;
	}

	public void setParams(WebParams params) {
		this.params = params;
	}
}