package com.jakubadamek.robotemil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SettingsModel implements Serializable {
    private static final long serialVersionUID = -3949508903097322764L;

    private List<OurHotel> ourHotels = new ArrayList<OurHotel>();
    private List<String> ourHotelNames;
    private List<WebParams> webStructs;
    private String customer;
    private String appTitle;
    private String excelFile;

    /**
     * @param ourHotels the ourHotels to set
     */
    public void setOurHotels(List<OurHotel> ourHotels) {
	this.ourHotels = ourHotels;
    }

    /**
     * @return the ourHotels
     */
    public List<OurHotel> getOurHotels() {
	return ourHotels;
    }

    /**
     * @param ourHotelNames the ourHotelNames to set
     */
    public void setOurHotelNames(List<String> ourHotelNames) {
	this.ourHotelNames = ourHotelNames;
    }

    /**
     * @return the ourHotelNames
     */
    public List<String> getOurHotelNames() {
	return ourHotelNames;
    }

    /**
     * @param webStructs the webStructs to set
     */
    public void setWebStructs(List<WebParams> webStructs) {
	this.webStructs = webStructs;
    }

    /**
     * @return the webStructs
     */
    public List<WebParams> getWebParams() {
	return webStructs;
    }

    /**
     * @param customer the customer to set
     */
    public void setCustomer(String customer) {
	this.customer = customer;
    }

    /**
     * @return the customer
     */
    public String getCustomer() {
	return customer;
    }

    /**
     * @param appTitle the appTitle to set
     */
    public void setAppTitle(String appTitle) {
	this.appTitle = appTitle;
    }

    /**
     * @return the appTitle
     */
    public String getAppTitle() {
	return appTitle;
    }

    /**
     * @param excelFile the excelFile to set
     */
    public void setExcelFile(String excelFile) {
	this.excelFile = excelFile;
    }

    /**
     * @return the excelFile
     */
    public String getExcelFile() {
	return excelFile;
    }
}
