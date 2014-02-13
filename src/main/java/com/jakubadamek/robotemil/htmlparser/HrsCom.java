package com.jakubadamek.robotemil.htmlparser;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

/**
 * Parses hrs.com
 *
 * clientId=Y3NfX0hSUw--&cid=7-4&page=listOfHotels
 * clientId=Y3NfX0hSUw--&cid=3-4&activity=setRegion&regionKey=49370&regionType=city
 *
 * http://www.hrs.com/search.do;jsessionid=4E8CA1799BDCCC476E46A08BC6463687?clientId=Y3NfX0hSUw--&cid=12-1&location=Praha (Hlavní město Praha)&perimeter=20&startDateDay=20&startDateMonth=11&startDateYear=2008&endDateDay=21&endDateMonth=11&endDateYear=2008
 *
 * @author Jakub Adamek
 */	
public class HrsCom extends HtmlParser
{
    private final Logger logger = LoggerFactory.getLogger(getClass());
	@Override
	public boolean run() throws FailingHttpStatusCodeException, IOException {
		WebClient webClient = new WebClient(BrowserVersion.FIREFOX_17);
		try {
    		webClient.getOptions().setJavaScriptEnabled(false);
    	    URL url = new URL("http://www.hrs.com");
    	    HtmlPage page = (HtmlPage)webClient.getPage(url);
    	    if(isStop()) return false;
    
    	    fillTextField(page, "location", "Praha (Praha)");
    	    fillTextField(page, "singleRooms", "1");
    	    fillTextField(page, "doubleRooms", "0");
    	    fillTextField(page, "adults", "1");
    	    Calendar calendar = Calendar.getInstance();
    	    calendar.setTime(this.dateFrom);
    	    DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.ENGLISH);
    	    fillTextField(page, "stayPeriod.start.date", dateFormat.format(calendar.getTime()));
    	    logger.info(dateFormat.format(calendar.getTime()));
    	    calendar.setTime(this.dateTo.toDate());
    	    fillTextField(page, "stayPeriod.end.date", dateFormat.format(calendar.getTime()));
    	    String sumbitXPath = "//input[@name='submitBasicSearch']";
    	    HtmlSubmitInput inputSubmit = (HtmlSubmitInput) page.getByXPath(sumbitXPath).get(0);
    	    logger.info("Clicking on " + inputSubmit);
    	    page = (HtmlPage) inputSubmit.click();
    	    if(isStop()) return false;
    	    /*savePage(page);
  			Thread.sleep(100000);*/
    
    	    boolean prahaFound = false;
    	    String prahaXPath = "//a";
    	    @SuppressWarnings("unused")
			String anchorTexts = "";
    	    for(Object o : page.getByXPath(prahaXPath)) {
    		    if(isStop()) return	false;
    	    	HtmlAnchor a = (HtmlAnchor) o;
    	    	//logger.info(a.getTextContent());
    	    	if(a.getTextContent().startsWith("Praha (Hl") || a.getTextContent().startsWith("Praha (Pr")) {
    	    	    logger.info("Clicking on " + a);
        	    	page = (HtmlPage) a.click();
    	    		prahaFound = true;
    	    	}
    	    	anchorTexts += a.getTextContent() + " ";
    	    }
    	    logger.info("link for Praha found? {}", prahaFound);
    	    if(! prahaFound) {
    	    	//throw new RuntimeException("Praha not found in " + anchorTexts);
    	    }
    
    	    logger.info("Downloading hotellist. Frames on this page: " + page.getFrames());
    	    if(isStop()) return false;
    
    		logger.info("Downloading hotellist 2. Frames on this page: " + page.getFrames());
    	    // 16.8.2010
    		if(page.getFrames().toString().contains("hotellist")) {
    			page = (HtmlPage) page.getFrameByName("hotellist").getEnclosedPage();
    		}
    	    if(isStop()) return false;
    	    String hotelXPath = "//td[@class='hn']/a[@class='pu']";
    	    //savePage(page);
    	    int ihotel = 0;
    	    for(Object o : page.getByXPath(hotelXPath)) {
    		    if(isStop()) return false;
	    	    if(maxPages != 0) {
		    	    ihotel ++;
	    	    	if(maxPages * 50 < ihotel) {
	    	    		return true;
	    	    	}
	    	    }
    	    	HtmlAnchor a = (HtmlAnchor) o;
    	    	if(! a.getTextContent().contains("Podrobnosti")) {
    		    	List<?> anchors = a.getByXPath("../../td/div/div[@class='hp']/a");
    		    	if(anchors.size() > 0) {
    			    	Object anchor = anchors.get(0);
    			    	String price1 = ((DomNode) anchor).getTextContent();
    			    	if(price1.contains("Sgl.")) {
    				    	price1 = price1.substring(price1.indexOf("Sgl.") + 4);
    				    	//logger.info(price1);
    				    	String price = "";
    				    	for(int i=0; i < price1.length(); i ++) {
    				    		if(Character.isDigit(price1.charAt(i)))
    				    			price += price1.charAt(i);
    				    		if(price1.charAt(i) == '.')
    				    			price += ".";
    				    	}
    			    	    addPrice(a.getTextContent(), key, price, false, Currency.USD, true);
    			    	}
    		    	}
    	    	}
    	    }
		} finally {
		    webClient.closeAllWindows();
		}
	    return true;
	}

}