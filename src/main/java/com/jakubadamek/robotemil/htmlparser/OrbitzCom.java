package com.jakubadamek.robotemil.htmlparser;

import java.io.IOException;
import java.text.SimpleDateFormat;
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
 * Parses booking.com
 *
 * @author Jakub
 */
public class OrbitzCom extends HtmlParser {
    private final Logger logger = Logger.getLogger(getClass());
	private static final String ORBITZ_COM =
	    "http://www.orbitz.com/shop/hotelsearch" +
	    "?type=hotel&hotel.typeOfSearch=keyword" +
	    "&hotel.locationKeywordInput.key=Praha" +
	    "&hotel.locId=" +
	    "&hotel.hotelSearchDetails.numberOfRooms=1" +
	    "&hotel.hotelSearchDetails.rooms[0].numberOfAdults=1" +
	    "&hotel.hotelSearchDetails.rooms[1].numberOfAdults=1" +
	    "&hotel.hotelSearchDetails.rooms[2].numberOfAdults=1" +
	    "&hotel.hotelSearchDetails.rooms[3].numberOfAdults=1" +
	    "&hotel.hotelRating=&hotel.hotelChain=&hotel.hotelName=&hotel.couponCode=" +
	    "&search=Search";
	
	@Override
	public boolean run() throws ParserException, IOException {
		double usdEurRate = new ExchangeRate().currentUsdEur();
		String url = ORBITZ_COM;
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(this.dateFrom);
        url += "&hotel.hotelSearchDetails.checkinDate=" + dateFormat.format(this.dateFrom);
        url += "&hotel.hotelSearchDetails.checkoutDate=" + dateFormat.format(this.dateTo.toDate());
		logger.info(url);
		NodeFilter hotelNameFilter =
			new AndFilter(
					new TagNameFilter("a"),
					new HasAttributeFilter("class", "hotelNameLink link"));
		NodeFilter priceFilter =
			new AndFilter(
					new TagNameFilter("strong"),
					new AndFilter(
					        new HasAttributeFilter("class", "rate"),
					        new HasParentFilter(
					                new AndFilter(
					                        new TagNameFilter("div"),
					                        new HasAttributeFilter("class", "totalPrice")))));
		
		NodeFilter takeAllFilter = new OrFilter(hotelNameFilter, priceFilter);
		int ipage = 1;
		int pageHotels = 1;
		while(pageHotels > 0) {
		    if(isStop()) return false;
			String hotel = "";
			String price = null;
			String pagedUrl = url;
			if(ipage > 1) {
			    pagedUrl += "&models['hotelSearchView'].page=" + ipage;
			}
			ipage ++;
			pageHotels = 0;
			Parser parser = new Parser(pagedUrl);
		    if(isStop()) return false;
			for(Node node : parser.extractAllNodesThatMatch(takeAllFilter).toNodeArray()) {
			    if(isStop()) return false;
				if(priceFilter.accept(node)) {
					price = node.getNextSibling().getText();
    				price = price.replace("$", "").replace(",","").trim();
    				//logger.info("Price " + price + " position " + node.getNextSibling().getStartPosition());
				}
                if(hotelNameFilter.accept(node)) {
                    hotel = node.getFirstChild().getText().trim();
                    //logger.info("Hotel " + hotel + " position " + node.getFirstChild().getStartPosition());
                    addPrice(hotel, this.key, price, false, Currency.USD);
                    pageHotels ++;
                    price = null;
                }
			}
			logger.info("*** " + pageHotels + " hotels");
		}
		return true;
	}
}
