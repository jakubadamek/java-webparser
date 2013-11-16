package com.jakubadamek.robotemil.htmlparser;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Parses expedia.com
 *
 *
 * @author Jakub Adamek
 */
public class ExpediaIt extends HtmlParser
{
    private static final String PAGE_NUMBER_REGEXP = "([0-9]+)\\W+di\\W+([0-9]+)";
    private static final int AJAX_WAIT_MILLIS = 3000;
    private static final int AJAX_TRIALS = 3;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private static final String EXPEDIA_URL = 
        "http://www.expedia.it/Hotels" +
        "?action=hotelSearchWizard%40searchHotelOnly" +
        "&hotelSearchWizard_inpItid=" +
        "&hotelSearchWizard_inpItty=&hotelSearchWizard_inpItdx=&hotelSearchWizard_inpSearchNearType=CITY" +
        "&hotelSearchWizard_inpSearchNear={0}&hotelSearchWizard_inpSearchNearStreetAddr=" +
        "&hotelSearchWizard_inpSearchNearCity=&hotelSearchWizard_inpSearchNearState=" +
        "&hotelSearchWizard_inpSearchNearZipCode=" +
        "&hotelSearchWizard_inpCheckIn={1,date,short}&hotelSearchWizard_inpCheckOut={2,date,short}" +
        "&hotelSearchWizard_inpNumRooms=1&hotelSearchWizard_inpNumAdultsInRoom=1" +
        "&hotelSearchWizard_inpNumChildrenInRoom=0&hotelSearchWizard_inpAddOptionFlag=" +
        "&hotelSearchWizard_inpHotelName=&hotelSearchWizard_inpHotelClass=0" +
        "&searchWizard_wizardType=hotelOnly";

    @SuppressWarnings("unused")
	private static final String AJAX_URL =
        "action=hotelResultsDisplay%40search" +
        "&hotelResultsDisplay_inpAjax=1" +
        "&hotelResultsDisplay_inpRfrrId=" +
        "&hotelResultsDisplay_inpSelectedHotelId=" +
        "&hotelResultsDisplay_inpRoomAvailsOpenedState=" +
        "&hotelResultsDisplay_topResultPageControl_inpPageIndex=25" +
        "&hotelResultsDisplay_bottomResultPageControl_inpPageIndex=25" +
        "&hotelResultsDisplay_hotelAreaMapControl_inpNearArea=" +
        "&hotelResultsDisplay_hotelResultsViewTypeControl_inpViewType=0" +
        "&hotelResultsDisplay_hotelMapControl_inpMapofx=" +
        "&hotelResultsDisplay_hotelMapControl_inpMapofy=" +
        "&hotelResultsDisplay_hotelMapControl_inpMapSize=0" +
        "&hotelResultsDisplay_hotelMapControl_inpCenLatitude=" +
        "&hotelResultsDisplay_hotelMapControl_inpCenLongitude=" +
        "&hotelResultsDisplay_hotelMapControl_inpZoomLevel=" +
        "&_=";    
    
	@Override
	public boolean run() throws FailingHttpStatusCodeException, IOException, InterruptedException {
	    WebClient webClient = new WebClient(BrowserVersion.FIREFOX_17);
	    try {
    		webClient.getOptions().setJavaScriptEnabled(true);
    	    URL url = new URL(MessageFormat.format(EXPEDIA_URL, 
    	            new Object[] { "Praha", this.dateFrom, this.dateTo.toDate() }));
    	    //final URL url = new URL("file:///D:/jakub/Kravinky/robotemil/search.do.htm");
    	    logger.info("", url);
    	    HtmlPage page = (HtmlPage)webClient.getPage(url);
    	    if(isStop()) return false;
    	    //fillTextField(page, "hotelSearchWizard_inpSearchNear", "Praha");
    /*        fillTextField(page, "hotelSearchWizard_inpSearchNear", "Prague (and vicinity), Czech Republic");
    	    Calendar calendar = Calendar.getInstance();
    	    calendar.setTime(this.date);
    	    DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.ENGLISH);
    	    fillTextField(page, "hotelSearchWizard_inpCheckIn", dateFormat.format(calendar.getTime()));
    	    logger.info(dateFormat.format(calendar.getTime()));
    	    calendar.add(Calendar.DATE, 1);
    	    fillTextField(page, "hotelSearchWizard_inpCheckOut", dateFormat.format(calendar.getTime()));
    	    selectOption(page, "//select[@id='hot-room[1]-alloc-adult-sel']", "1"); // 1 Adult
    	    HtmlForm htmlForm = (HtmlForm) page.getByXPath("//form[@id='hot-wiz-form']").get(0);
    	    page = (HtmlPage) htmlForm.submit(null);
            page = clickAnchor(page, "//a[@id='hot-sub-anc']");
    */	    
    	    /*WebRequest webRequest = new WebRequest(new URL(AJAX_URL), HttpMethod.POST);
    	    XMLHttpRequest xmlHttpRequest = new XMLHttpRequest(); 
    	        // webClient.getAjaxController().processSynchron(page, webRequest, false);
    	    
    	    xmlHttpRequest.jsxFunction_open("POST", AJAX_URL, false, null, null);
    	    xmlHttpRequest.jsxFunction_send(content)*/
    	    
    	    int iPage = 1;
    	    int pageCount = 999;
    	    boolean finished = false;
    	    do {
        	    String hotelXPath = "//div[@class='hotelTitle hotelTitle_8_it_IT']/a[@class='fl fl_8_it_IT']";
        	    //savePage(page);
        	    for(Object hotelTag : page.getByXPath(hotelXPath)) {
        	        if(logger.isDebugEnabled()) {
        	            logger.debug("Div found " + hotelTag);
        	        }
        		    if(isStop()) return false;
        	    	HtmlAnchor hotelAnchor = (HtmlAnchor) hotelTag;
        	    	String hotelName = hotelAnchor.getTextContent().trim();
        	    	List<?> prices = hotelAnchor.getByXPath("../../../..//span[@class='avgRatePrice']/nobr");
        	    	if(prices.size() > 0) {
        		    	Object price = prices.get(0);
        		    	String price1 = ((DomNode) price).getTextContent().replace("€", "");
        		    	addPrice(hotelName, key, price1, false);
        	    	} else {
        	    	    addPrice(hotelName, key, null, false);
        	    	}
        	    }
        	    if(iPage >= pageCount) {
        	        finished = true;
        	    } else {
            	    DateTime clickTime = new DateTime();
            	    clickAnchor(page, "//a[@id='toNextHrefhotelResultsDisplay_bottomResultPageControl']");
                    if(isStop()) return false;
    
            	    // prvni ochrana: pockej, dokud nedobehne JavaScript, viz http://htmlunit.sourceforge.net/faq.html
                    webClient.waitForBackgroundJavaScriptStartingBefore(new DateTime().getMillis() - clickTime.getMillis());
                    logger.info("Finished waitForBackgroundJavaScriptStartingBefore");
    
            	    // druha ochrana: pockej, dokud se nenacte Ajaxem nova stranka - pozna se podle toho, ze se zmeni text "Page 1 of 20" 
                    boolean pageLoaded = false;
            	    for(int itrial = 0; itrial < AJAX_TRIALS && ! pageLoaded; itrial ++) {
                        if(isStop()) return false;
            	        String pageCountXPath = "//td[@class='pageNameNo']";
            	        try {
                	        if(page.getByXPath(pageCountXPath).size() > 0) {
                	            String pageCountText = ((DomNode) page.getByXPath(pageCountXPath).get(0)).getTextContent().trim();
                	            Matcher matcher = Pattern.compile(PAGE_NUMBER_REGEXP).matcher(pageCountText);
                	            if(matcher.find()) {
                	                int pageNo = Integer.valueOf(matcher.group(1));
                	                pageCount = Integer.valueOf(matcher.group(2));
                                    logger.info("page no " + pageNo + " page count " + pageCount + " iPage " + iPage);
                	                if(pageNo != iPage) {
                	                    pageLoaded = true;
                	                    iPage = pageNo;
                	                }
                	            }            
                	        }
            	        } catch(Exception e) {
            	            logger.info("", e);
            	        }
            	        if(! pageLoaded) {
                            Thread.sleep(AJAX_WAIT_MILLIS);        	            
            	        }
            	    }
            	    if(! pageLoaded) {
            	        throw new IOException("Nepodarilo se nacist AJAX, strana " + iPage);
            	    }
            	    logger.info("Finished waiting for hotels");
        	    } 
    	    } while(! finished);
	    } finally {
	        webClient.closeAllWindows();
	    }
	    return true;
	}
}