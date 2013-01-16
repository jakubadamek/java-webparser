package com.jakubadamek.robotemil.htmlparser;

import java.io.IOException;

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
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Parses lastminute.es
 * 
 * @author Jakub
 */
public class LastminuteEs extends HtmlParser {
    private final Logger logger = Logger.getLogger(getClass());
	private static final String LASTMINUTE_COM =
		"http://www.es.lastminute.com/trips/hotellist/listInternal" +
		"?hotelMaxReturnPerPage=25" +
		"&guestCounts=1&guestCodes=ADULT&city=PRG" +
		"&numRooms=1&path=hotels";

	@Override
	public boolean run() throws ParserException, IOException {
		String url = LASTMINUTE_COM;
	    //&checkInDate=2013-01-12&checkOutDate=2013-01-13
	    //&startIndex=26
	    DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd");
	    url += "&checkInDate=" + dateFormat.print(new DateTime(this.dateFrom));
	    url += "&checkOutDate=" + dateFormat.print(this.dateTo);
		logger.debug(url);
		NodeFilter hotelNameFilter = 
			new AndFilter(
					new TagNameFilter("a"),
					new HasParentFilter(new TagNameFilter("h3")));
		NodeFilter priceFilter = 
			new AndFilter(
					new TagNameFilter("strong"),
					new OrFilter(
							new HasAttributeFilter("class", "number highlight has-tooltip cursor-pointer"),
							new HasAttributeFilter("class", "number has-tooltip cursor-pointer")));
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
					logger.debug("Found hotel: " + hotel);
				}
				if(priceFilter.accept(node)) {
					//logger.debug("Found price: " + node.getNextSibling());
					price = node.getNextSibling().getText().trim();
					price = price.replace("&#8364;", "").replace("&nbsp;", "").trim();
					price = price.replace(",", ".");
					if(price.length() > 0 && hotel != "") {
						addPrice(hotel, this.key, price, false);
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
