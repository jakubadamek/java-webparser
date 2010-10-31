package com.jakubadamek.robotemil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Text;

import com.jakubadamek.robotemil.Prices;
import com.jakubadamek.robotemil.htmlparser.HtmlParser;

/** web struct */
public class WebStruct implements Serializable {
    private static final long serialVersionUID = 7941780928540322765L;
    /** label */
    private String label;
    /** file name */
    private String fileName;
    /** Excel name */
    private String excelName;
    /** Icon file name */
    private String iconName;
    /** hotel text fields */
    private transient List<Text> hotelTexts = new ArrayList<Text>();
    /** hotel names, used to initialize hotelTexts */
    private List<String> hotelList = new ArrayList<String>();
    /** prices */
    private Prices prices = new Prices();
    /** class used to download the data */
    private Class<? extends HtmlParser> parserClass;

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
     * @param label the label to set
     */
    public void setLabel(String label) {
	this.label = label;
    }

    /**
     * @return the label
     */
    public String getLabel() {
	return label;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
	this.fileName = fileName;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
	return fileName;
    }

    /**
     * @param excelName the excelName to set
     */
    public void setExcelName(String excelName) {
	this.excelName = excelName;
    }

    /**
     * @return the excelName
     */
    public String getExcelName() {
	return excelName;
    }

    /**
     * @param iconName the iconName to set
     */
    public void setIconName(String iconName) {
	this.iconName = iconName;
    }

    /**
     * @return the iconName
     */
    public String getIconName() {
	return iconName;
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

    /**
     * @param parserClass the parserClass to set
     */
    public void setParserClass(Class<? extends HtmlParser> parserClass) {
	this.parserClass = parserClass;
    }

    /**
     * @return the parserClass
     */
    public Class<? extends HtmlParser> getParserClass() {
	return parserClass;
    }
}