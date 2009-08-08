package com.jakubadamek.robotemil.htmlparser;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.jakubadamek.robotemil.WorkUnit;
	
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
	/**
	 * @param args
	 * @throws FailingHttpStatusCodeException
	 * @throws IOException
	 */
	public static void main(String[] args) throws FailingHttpStatusCodeException, IOException {
		HrsCom hrsCom = new HrsCom();
		hrsCom.init(new WorkUnit(), null);
		hrsCom.run();
	}
	
	private void selectOption(HtmlPage page, String selectXPath, String optionValue) throws IOException {
		System.out.println("Setting " + selectXPath + " to " + optionValue);
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
	}
	
	@Override
	public void run() throws FailingHttpStatusCodeException, IOException {		
		WebClient webClient = new WebClient(BrowserVersion.FIREFOX_2);
	    URL url = new URL("http://www.hrs.com");
	    //final URL url = new URL("file:///D:/jakub/Kravinky/robotemil/search.do.htm");
	    HtmlPage page = (HtmlPage)webClient.getPage(url);
	    if(isStop()) return;
	    
	    String locationXPath = "//input[@name='location']"; 
	    HtmlTextInput inputLocation = (HtmlTextInput) page.getByXPath(locationXPath).get(0);
	    inputLocation.setValueAttribute("Praha");	    

	    selectOption(page, "//select[@name='perimeter']", "32");
	    selectOption(page, "//select[@name='localeString']", "cs");
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(this.date);
	    DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
	    HtmlTextInput startDate = (HtmlTextInput) page.getByXPath("//input[@name='stayPeriod.start.date']").get(0);
	    startDate.setValueAttribute(dateFormat.format(calendar.getTime()));
	    System.out.println(dateFormat.format(calendar.getTime()));
	    calendar.add(Calendar.DATE, 1);
	    HtmlTextInput endDate = (HtmlTextInput) page.getByXPath("//input[@name='stayPeriod.end.date']").get(0);
	    endDate.setValueAttribute(dateFormat.format(calendar.getTime()));
	    String sumbitXPath = "//input[@name='submitBasicSearch']";
	    HtmlSubmitInput inputSubmit = (HtmlSubmitInput) page.getByXPath(sumbitXPath).get(0);
	    System.out.println("Clicking on " + inputSubmit);
	    page = (HtmlPage) inputSubmit.click();
	    if(isStop()) return;
	    
	    boolean prahaFound = false;
	    String prahaXPath = "//a";
	    for(Object o : page.getByXPath(prahaXPath)) {
		    if(isStop()) return	;
	    	HtmlAnchor a = (HtmlAnchor) o;
	    	//System.out.println(a.getTextContent());
	    	if(a.getTextContent().startsWith("Praha (Hlavn")) {
	    	    System.out.println("Clicking on " + a);
    	    	page = (HtmlPage) a.click();
	    		prahaFound = true;
	    	}
	    }
	    if(! prahaFound) {
	    	throw new RuntimeException("Praha not found");
	    }
	    
	    System.out.println("Downloading hotellist. Frames on this page: " + page.getFrames());
	    if(isStop())
	    	return;
	    //HtmlPage page = (HtmlPage)webClient.getPage("file:///D:/temp/search.do%3bjsessionid=7B1084050944D9BA183E226EB60CD6D7_soubory/showPage_003.htm");
	    /*HtmlPage head = (HtmlPage) page.getFrameByName("head").getEnclosedPage();
	    if(isStop()) return;
	    for(Object o : head.getByXPath("//div")) {
	    	System.out.println(((HtmlDivision) o).getTextContent());	    	
	    }*/
	    selectOption(page, "//select[@name='currency']", "EUR");
	    page = (HtmlPage) page.getFrameByName("hotellist").getEnclosedPage();
	    if(isStop()) return;
	    String hotelXPath = "//td[@class='hn ']/a[@onclick!='']";
	    for(Object o : page.getByXPath(hotelXPath)) {
		    if(isStop()) return;
	    	HtmlAnchor a = (HtmlAnchor) o;
	    	if(! a.getTextContent().contains("Podrobnosti")) {
		    	List<?> anchors = a.getByXPath("../../td/div/span[@class='hp']/a");
		    	if(anchors.size() > 0) {
			    	Object anchor = anchors.get(0);
			    	String price1 = ((DomNode) anchor).getTextContent();
			    	System.out.println(price1);
			    	String price = "";
			    	for(int i=0; i < price1.length(); i ++) {
			    		if(Character.isDigit(price1.charAt(i)))
			    			price += price1.charAt(i);
			    		if(price1.charAt(i) == ',')
			    			price += ".";
			    	}
			    	//HtmlTable htmlTable = (HtmlTable) a.getParentNode().getParentNode().getParentNode().getParentNode().getParentNode();
			    	//System.out.println(htmlTable.getAttribute("id") + " " + a.getCanonicalXPath());
		    	    //System.out.println(a.getTextContent() + "; " + price);
		    	    addPrice(a.getTextContent(), this.date, price);
		    	}
	    	}
	    }
	}

}