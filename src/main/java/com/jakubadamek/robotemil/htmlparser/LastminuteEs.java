package com.jakubadamek.robotemil.htmlparser;

import java.io.IOException;
import java.util.Calendar;

import org.apache.log4j.Logger;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.ParserException;

/**
 * Parses lastminute.es
 * 
 * @author Jakub
 */
public class LastminuteEs extends HtmlParser {
    private final Logger logger = Logger.getLogger(getClass());
	private static final String LASTMINUTE_COM =
		"http://www.es.lastminute.com/site/viajes/hoteles/hotels-results.html" +
		"?skin=eses.lastminute.com&lmnRooms=1" +
		"&returnURL=http%3A%2F%2Fwww.es.lastminute.com%2Fsite%2Fmain%2Fhomepage_es_ES.html" +
		"&preserveName-CATEGORY=hotels" +
		"&preserveName-requestURL=%2Fsite%2Fmain%2Fhomepage_es_ES.html" +
		"&lmnShowRestaurants=False&lmnLengthOfStay=1" +
		"&lmnRoom2ChildAge2=-1&lmnRoom2ChildAge1=-1&lmnRoom3ChildAge1=-1" +
		"&lmnChildrenRoom3=0&lmnChildrenRoom2=0&lmnChildrenRoom1=0&preserveName-CATID=4" +
		"&lmnRoom3ChildAge3=-1&lmnRoom2ChildAge3=-1&lmnHotelName=&method=hotelsFullSearch" +
		"&lmnRoom2ChildAge4=-1&CATID=4&preserveName-SEARCH=basic&lmnRoom3ChildAge2=-1" +
		"&errorURL=http%3A%2F%2Fwww.es.lastminute.com%2Fsite%2Fviajes%2Fhoteles%2Ferror.html" +
		"&filterResultsBy=all&searchTypeName=cityName" +
		"&lmnCountry=CZ|CZECH+REPUBLIC|CZECH+REPUBLIC&lmnAdultsRoom3=2&lmnAdultsRoom2=2&lmnAdultsRoom1=1" +
		"&lmnRoom1ChildAge3=-1&lmnRoom1ChildAge2=-1&lmnRoom1ChildAge1=-1&lmnLocation=Prague" +
		"&lmnRoom3ChildAge4=-1&lmnRoom1ChildAge4=-1&preserveName-skin=eses.lastminute.com";

	@Override
	public boolean run() throws ParserException, IOException {
		String url = LASTMINUTE_COM;
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(this.dateFrom);
	    //&lmnCheckOutMonth=8&lmnCheckInDay=15&lmnCheckOutDay=16&lmnCheckInMonth=8
	    //&startIndex=26
	    url += "&lmnCheckInDay=" + calendar.get(Calendar.DAY_OF_MONTH);
	    url += "&lmnCheckInMonth=" + (calendar.get(Calendar.MONTH) + 1);
	    calendar.add(Calendar.DAY_OF_MONTH, 1);
	    url += "&lmnCheckOutDay=" + calendar.get(Calendar.DAY_OF_MONTH);
	    url += "&lmnCheckOutMonth=" + (calendar.get(Calendar.MONTH) + 1);
		logger.debug(url);
		NodeFilter hotelNameFilter = 
			new AndFilter(
					new TagNameFilter("a"),
					new HasParentFilter(new TagNameFilter("h3")));
		NodeFilter priceFilter = 
			new AndFilter(
					new TagNameFilter("span"),
					new HasAttributeFilter("class", "text_inverse"));
		NodeFilter takeAllFilter = new OrFilter(hotelNameFilter, priceFilter); 
		int ipage = 0;
		int pageHotels = 1;
		while(pageHotels > 0) {
		    if(isStop()) return false;
			String hotel = "";
			String price = "";
			String pagedUrl = url;
			if(ipage > 0)
				pagedUrl += "&startIndex=" + (ipage * 25 + 1);
			ipage ++;
			pageHotels = 0;
			Parser parser = new Parser(pagedUrl);
		    if(isStop()) return false;
			for(Node node : parser.extractAllNodesThatMatch(takeAllFilter).toNodeArray()) {
			    if(isStop()) return false;
				if(hotelNameFilter.accept(node)) {
					hotel = node.getChildren().toNodeArray()[0].getText().replace("- Praga", "").trim();
				}
				if(priceFilter.accept(node)) {
					if(node.getChildren().size() > 2) {
						price = node.getChildren().toNodeArray()[2].getText().trim();
					} else {
						price = node.getChildren().toNodeArray()[0].getText().trim();
					}
					price = price.replace("&euro;", "").replace("&nbsp;", "").trim();
					if(price.length() > 0 && hotel != "") {
						addPrice(hotel, this.dateFrom, price);
						pageHotels ++;
						hotel = "";
					}
				}
			}
			logger.info("*** " + pageHotels + " hotels");
		}
		return true;
	}	
}
