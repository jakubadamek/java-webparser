package com.jakubadamek.robotemil.htmlparser;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlImageInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
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
public class HrsComOld extends HtmlParser 
{
	/**
	 * @param args
	 * @throws FailingHttpStatusCodeException
	 * @throws IOException
	 */
	public static void main(String[] args) throws FailingHttpStatusCodeException, IOException {
		HrsComOld hrsCom = new HrsComOld();
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
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(this.date);
	    URL url = new URL("http://www.hrs.com/web3/?client=cs__HRS&lid=ToWeb3");
	    //final URL url = new URL("file:///D:/jakub/Kravinky/robotemil/search.do.htm");
	    HtmlPage page = (HtmlPage)webClient.getPage(url);
	    if(isStop()) return;
	    
	    String locationXPath = "//input[@name='location']"; 
	    HtmlTextInput inputLocation = (HtmlTextInput) page.getByXPath(locationXPath).get(0);
	    inputLocation.setValueAttribute("Praha");	    

	    selectOption(page, "//select[@name='perimeter']", "20");
	    selectOption(page, "//select[@name='currency']", "EUR");
	    selectOption(page, "//select[@name='localeString']", "cs");
	    selectOption(page, "//select[@name='startDateDay']", String.valueOf(calendar.get(Calendar.DATE)));
	    selectOption(page, "//select[@name='startDateMonth']", String.valueOf(calendar.get(Calendar.MONTH) + 1));
	    selectOption(page, "//select[@name='startDateYear']", String.valueOf(calendar.get(Calendar.YEAR)));
	    calendar.add(Calendar.DATE, 1);
	    selectOption(page, "//select[@name='endDateDay']", String.valueOf(calendar.get(Calendar.DATE)));
	    selectOption(page, "//select[@name='endDateMonth']", String.valueOf(calendar.get(Calendar.MONTH) + 1));
	    selectOption(page, "//select[@name='endDateYear']", String.valueOf(calendar.get(Calendar.YEAR)));
	    
	    String sumbitXPath = "//input[@name='submitBasicSearch']";
	    HtmlImageInput inputSubmit = (HtmlImageInput) page.getByXPath(sumbitXPath).get(0);
	    System.out.println("Clicking on " + inputSubmit);
	    page = (HtmlPage) inputSubmit.click();
	    if(isStop()) return;
	    
	    boolean prahaFound = false;
	    String prahaXPath = "//a";
	    for(Object o : page.getByXPath(prahaXPath)) {
		    if(isStop()) return;
	    	HtmlAnchor a = (HtmlAnchor) o;
	    	if(a.getTextContent().startsWith("Praha (Hlavn")) {
	    	    System.out.println("Clicking on " + a);
    	    	page = (HtmlPage) a.click();
	    		prahaFound = true;
	    	}
	    }
	    if(! prahaFound) {
	    	throw new RuntimeException("Praha not found");
	    }
	    
	    System.out.println("Downloading mainFrame. Frames on this page: " + page.getFrames());
	    if(isStop())
	    	return;
	    //HtmlPage page = (HtmlPage)webClient.getPage("file:///D:/temp/search.do%3bjsessionid=7B1084050944D9BA183E226EB60CD6D7_soubory/showPage_003.htm");
	    HtmlPage head = (HtmlPage) page.getFrameByName("head").getEnclosedPage();
	    if(isStop()) return;
	    for(Object o : head.getByXPath("//div")) {
	    	System.out.println(((HtmlDivision) o).getTextContent());	    	
	    }
	    page = (HtmlPage) page.getFrameByName("mainFrame").getEnclosedPage();
	    if(isStop()) return;
	    String hotelXPath = "//td[@class='hn ']/div/a[@onclick!='']";
	    for(Object o : page.getByXPath(hotelXPath)) {
		    if(isStop()) return;
	    	HtmlAnchor a = (HtmlAnchor) o;
	    	List<?> divs = a.getByXPath("../../../td[@class='hp ']/div");
	    	if(divs.size() > 0) {
		    	Object div = divs.get(0);
		    	String price1 = ((HtmlDivision) div).getTextContent();
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