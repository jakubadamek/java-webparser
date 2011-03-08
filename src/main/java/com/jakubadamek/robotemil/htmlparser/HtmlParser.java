package com.jakubadamek.robotemil.htmlparser;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.jakubadamek.robotemil.App;
import com.jakubadamek.robotemil.DiacriticsRemover;
import com.jakubadamek.robotemil.Prices;
import com.jakubadamek.robotemil.WorkUnit;


/**
 * Ancestor to the parsing classes
 *
 * @author Jakub
 */
public abstract class HtmlParser {
    private static final Logger logger = Logger.getLogger(HtmlParser.class);
	/** date for which prices are searched */
	protected Date dateFrom;
	protected DateTime dateTo;
	/** prices - the result of parsing */
	private Prices prices;
	private int order;
	private App app;
	private WorkUnit workUnit;

	/**
	 * Constructor
	 *
	 * @param aWorkUnit
	 * @param aApp
	 */
	public void init(WorkUnit aWorkUnit, App aApp) {
		this.dateFrom = aWorkUnit.date;
		this.dateTo = new DateTime(aWorkUnit.date).plusDays(1);
		this.workUnit = aWorkUnit;
		this.prices = new Prices();
		this.app = aApp;
		this.order = 0;
	}

	/**
	 * Runs the real job. Returns true if it runs to the end,
	 * false if stopped in the middle.
	 *
	 * @throws Exception
	 */
	public abstract boolean run() throws Exception;

	private DateFormat dateFormat = DateFormat.getDateInstance();

	/**
	 * Adds price to the inner structure
	 *
	 * @param aHotel
	 * @param aDate
	 * @param price
	 */
	protected void addPrice(String aHotel, Date aDate, String price) {
		this.order ++;
		String hotel = DiacriticsRemover.removeDiacritics(aHotel.replace("&amp;", "&"));
		final String logRow = getClass().getSimpleName() + " " + this.order + " " + hotel + " " + price + " "
			+ this.dateFormat.format(aDate);
		logger.info(logRow);
		if(this.app != null) {
			//this.app.showLog(logRow);
		}
		this.prices.addPrice(hotel, aDate, price, this.order);
	}

	/**
	 * @return the stop
	 */
	public boolean isStop() {
		this.workUnit.lastResponseTime = new Date();
		return this.workUnit.finished || App.stop;
	}

	/**
	 * @return the prices
	 */
	public Prices getPrices() {
		return this.prices;
	}
	
    protected void selectOption(HtmlPage page, String selectXPath, String optionValue) throws IOException {
        logger.info("Setting " + selectXPath + " to " + optionValue);
        HtmlSelect select = null;
        try {
            select = (HtmlSelect) page.getByXPath(selectXPath).get(0);
            select.setSelectedAttribute(select.getOptionByValue(optionValue), true);
        } catch(Exception e) {
            if(select == null) {
                throw new IOException("select " + selectXPath + " could not be found");
            }
            throw new IOException("Page " + page + " selectXPath " + selectXPath + " options " +
                    select.getOptions() + " optionValue " + optionValue + " " + e.toString());
        }
        logger.info("Finished setting " + selectXPath + " to " + optionValue);
    }

    protected void fillTextField(HtmlPage page, String fieldName, String value) throws IOException {
        logger.info("Setting " + fieldName + " to " + value);
        String xPath = "//input[@name='" + fieldName + "']";
        HtmlInput input = null;
        try {
            input = (HtmlInput) page.getByXPath(xPath).get(0);
            input.setValueAttribute(value);
        } catch(Exception e) {
            if(input == null) {
                throw new IOException("Page " + page + " input " + fieldName + " " + e.toString());
            }
        }
        logger.info("Finished setting " + fieldName + " to " + value);
    }

    @SuppressWarnings("unused")
    protected void savePage(HtmlPage page) {
        try {
            File htmlFile = new File("backup.html");
            logger.info("Saving html page to " + htmlFile.getCanonicalPath());
            if(htmlFile.exists()) {
                htmlFile.delete();
            }
            page.save(htmlFile);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }	
    
    protected void selectRadio(HtmlPage page, String radioXPath) throws IOException {
        logger.info("Selecting radio " + radioXPath);
        HtmlRadioButtonInput radio = null;
        try {
            radio = (HtmlRadioButtonInput) page.getByXPath(radioXPath).get(0);
            radio.setChecked(true);
        } catch(Exception e) {
            if(radio == null) {
                throw new IOException("radio " + radioXPath + " could not be found");
            }
            throw new IOException("Page " + page + " radioXPath " + radioXPath + " " + e.toString());
        }
        logger.info("Finished selecting " + radioXPath);        
    }
    
    protected HtmlPage clickAnchor(HtmlPage page, String anchorXPath) throws IOException {
        logger.info("Clicking on anchor " + anchorXPath);
        HtmlAnchor anchor = null;
        try {
            anchor = (HtmlAnchor) page.getByXPath(anchorXPath).get(0);
            HtmlPage retval = anchor.click();
            logger.info("Finished clicking on " + anchorXPath);
            return retval;
        } catch(Throwable e) {
            e.printStackTrace();
            if(anchor == null) {
                throw new IOException("anchor " + anchorXPath + " could not be found");                
            }
            throw new IOException("Page " + page + " anchor " + anchorXPath + " " + e.toString());
        }
    }
}
