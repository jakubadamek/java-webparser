package com.jakubadamek.robotemil;

import java.io.Serializable;

import com.jakubadamek.robotemil.htmlparser.HtmlParser;

/** web params initialized by Spring */
public class WebParams implements Serializable {
    private static final long serialVersionUID = 7941780928540322765L;
    /** label */
    private String label;
    /** file name */
    private String fileName;
    /** Excel name */
    private String excelName;
    /** Icon file name */
    private String iconName;
    /** class used to download the data */
    private Class<? extends HtmlParser> parserClass;
    /** is user-enabled? */ 
    private boolean enabled = true;

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
    
    public boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}