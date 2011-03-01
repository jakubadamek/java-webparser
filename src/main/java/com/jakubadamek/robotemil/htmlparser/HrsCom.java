package com.jakubadamek.robotemil.htmlparser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
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
		System.out.println("Finished setting " + selectXPath + " to " + optionValue);
	}

	private void fillTextField(HtmlPage page, String fieldName, String value) {
		System.out.println("Setting " + fieldName + " to " + value);
	    String xPath = "//input[@name='" + fieldName + "']";
	    HtmlInput input = (HtmlInput) page.getByXPath(xPath).get(0);
	    input.setValueAttribute(value);
		System.out.println("Finished setting " + fieldName + " to " + value);
	}

	@SuppressWarnings("unused")
	private void savePage(HtmlPage page) throws IOException {
	    File htmlFile = new File("backup.html");
		System.out.println("Saving html page to " + htmlFile.getCanonicalPath());
	    if(htmlFile.exists()) {
	    	htmlFile.delete();
	    }
	    page.save(htmlFile);
	}

	@Override
	public boolean run() throws FailingHttpStatusCodeException, IOException {
		WebClient webClient = new WebClient(BrowserVersion.FIREFOX_3);
		webClient.setJavaScriptEnabled(false);
	    URL url = new URL("http://www.hrs.com");
	    //final URL url = new URL("file:///D:/jakub/Kravinky/robotemil/search.do.htm");
	    HtmlPage page = (HtmlPage)webClient.getPage(url);
	    if(isStop()) return false;
	    //selectOption(page, "//select[@name='localeString']", "cs");

	    fillTextField(page, "location", "Prague (Praha)");
	    fillTextField(page, "singleRooms", "1");
	    fillTextField(page, "doubleRooms", "0");
	    fillTextField(page, "adults", "1");
	    // Toto se stane pri vyberu "Praha (Hlavni mesto)" z kontextove napovedy:
	    //fillTextField(page, "suggestedID", "%49370");
	    //selectOption(page, "//select[@name='perimeter']", "16");
	    //selectOption(page, "//select[@name='localeString']", "de");
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(this.date);
	    DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.ENGLISH);
	    fillTextField(page, "stayPeriod.start.date", dateFormat.format(calendar.getTime()));
	    System.out.println(dateFormat.format(calendar.getTime()));
	    calendar.add(Calendar.DATE, 1);
	    fillTextField(page, "stayPeriod.end.date", dateFormat.format(calendar.getTime()));
	    String sumbitXPath = "//input[@name='submitBasicSearch']";
	    HtmlSubmitInput inputSubmit = (HtmlSubmitInput) page.getByXPath(sumbitXPath).get(0);
	    System.out.println("Clicking on " + inputSubmit);
	    page = (HtmlPage) inputSubmit.click();
	    if(isStop()) return false;
	    //savePage(page);
	    //if(true) return;
	    /*savePage(page);
	    try {
			Thread.sleep(100000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

	    boolean prahaFound = false;
	    String prahaXPath = "//a";
	    String anchorTexts = "";
	    for(Object o : page.getByXPath(prahaXPath)) {
		    if(isStop()) return	false;
	    	HtmlAnchor a = (HtmlAnchor) o;
	    	//System.out.println(a.getTextContent());
	    	if(a.getTextContent().startsWith("Prague (Praha)")) {
	    	    System.out.println("Clicking on " + a);
    	    	page = (HtmlPage) a.click();
	    		prahaFound = true;
	    	}
	    	anchorTexts += a.getTextContent() + " ";
	    }
	    if(! prahaFound) {
	    	//throw new RuntimeException("Praha not found in " + anchorTexts);
	    }

	    System.out.println("Downloading hotellist. Frames on this page: " + page.getFrames());
	    if(isStop()) return false;
	    //HtmlPage page = (HtmlPage)webClient.getPage("file:///D:/temp/search.do%3bjsessionid=7B1084050944D9BA183E226EB60CD6D7_soubory/showPage_003.htm");
	    /*HtmlPage head = (HtmlPage) page.getFrameByName("head").getEnclosedPage();
	    if(isStop()) return false;
	    for(Object o : head.getByXPath("//div")) {
	    	System.out.println(((HtmlDivision) o).getTextContent());
	    }*/
	    //savePage(page);
	    selectOption(page, "//select[@name='currency']", "EUR");
	    //sumbitXPath = "//form[@name='currencyForm']/noscript/input";
	    //HtmlImageInput inputImage = (HtmlImageInput) page.getByXPath(sumbitXPath).get(0);
	    //System.out.println("Clicking on " + inputImage);
	    //page = (HtmlPage) inputImage.click();
	    sumbitXPath = "//form[@name='currencyForm']";
	    HtmlForm currencyForm = (HtmlForm) page.getByXPath(sumbitXPath).get(0);
	    System.out.println("Submitting " + currencyForm);
	    page = (HtmlPage) currencyForm.submit(null);

		System.out.println("Downloading hotellist 2. Frames on this page: " + page.getFrames());
	    // 16.8.2010
		if(page.getFrames().toString().contains("hotellist")) {
			page = (HtmlPage) page.getFrameByName("hotellist").getEnclosedPage();
		}
	    if(isStop()) return false;
	    String hotelXPath = "//td[@class='hn']/a[@class='pu']";
	    //savePage(page);
	    for(Object o : page.getByXPath(hotelXPath)) {
		    if(isStop()) return false;
	    	HtmlAnchor a = (HtmlAnchor) o;
	    	if(! a.getTextContent().contains("Podrobnosti")) {
		    	List<?> anchors = a.getByXPath("../../td/div/div[@class='hp']/a");
		    	if(anchors.size() > 0) {
			    	Object anchor = anchors.get(0);
			    	String price1 = ((DomNode) anchor).getTextContent();
			    	if(price1.contains("Sgl.")) {
				    	price1 = price1.substring(price1.indexOf("Sgl.") + 4);
				    	//System.out.println(price1);
				    	String price = "";
				    	for(int i=0; i < price1.length(); i ++) {
				    		if(Character.isDigit(price1.charAt(i)))
				    			price += price1.charAt(i);
				    		if(price1.charAt(i) == '.')
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
	    return true;
	}

}