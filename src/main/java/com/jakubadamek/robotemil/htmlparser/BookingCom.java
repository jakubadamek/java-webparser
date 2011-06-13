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
 * Parses booking.com
 *
 * @author Jakub
 */
public class BookingCom extends HtmlParser {
    private final Logger logger = Logger.getLogger(getClass());
	private static final String HTML_EURO = "&#x20AC;";
	private static final String BOOKING_COM =
		"http://www.booking.com/searchresults.html?city=-553173&ssne=Prague" +
		"&order=&addressAddress=&addressCity=&addressZIP=&addressCountry=cz" +
		"&si=ai%2Cco%2Cci%2Cre&ss=Prague&radius=15&do_availability_check=on";

	@Override
	public boolean run() throws ParserException, IOException {
		String url = BOOKING_COM;
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(this.dateFrom);
	    //checkin_monthday=22;checkin_year_month=2008-8;" +
		//"checkout_monthday=23;checkout_year_month=2008-8;" +
		url += ";checkin_monthday=" + calendar.get(Calendar.DAY_OF_MONTH);
		url += ";checkin_year_month=" + calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1);
		calendar.setTime(this.dateTo.toDate());
		url += ";checkout_monthday=" + calendar.get(Calendar.DAY_OF_MONTH);
		url += ";checkout_year_month=" + calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1);
		logger.info(url);
		NodeFilter hotelNameFilter =
			new AndFilter(
					new TagNameFilter("a"),
					new HasParentFilter(new TagNameFilter("h3")));
		NodeFilter priceFilter =
			new AndFilter(
					new TagNameFilter("div"),
					new HasParentFilter(
							new AndFilter(
									new TagNameFilter("td"),
									new HasAttributeFilter("class", "roomPrice"))));
/*		NodeFilter roomTypeFilter = 
			new AndFilter(
					new TagNameFilter("a"),
					new HasParentFilter( // div
							new HasParentFilter( // td
									new HasParentFilter( // tr class="roomrow"
											new AndFilter(
													new TagNameFilter("tr"),
													new HasAttributeFilter("class", "roomrow"))))));
*/		
		NodeFilter roomTypeFilter = 
			new AndFilter(
					new TagNameFilter("a"),
					new HasAttributeFilter("class", "room_link "));

		
		NodeFilter takeAllFilter = new OrFilter(new OrFilter(hotelNameFilter, priceFilter), roomTypeFilter);
		int ipage = 0;
		int pageHotels = 1;
		int offset = 0;
		while(pageHotels > 0) {
		    if(isStop()) return false;
			String hotel = "";
			String price = "";
			String pagedUrl = url + "&offset=" + offset;
			ipage ++;
			pageHotels = 0;
			Parser parser = new Parser(pagedUrl);
		    if(isStop()) return false;
			for(Node node : parser.extractAllNodesThatMatch(takeAllFilter).toNodeArray()) {
			    if(isStop()) return false;
				if(hotelNameFilter.accept(node)) {
					hotel = node.getChildren().toNodeArray()[0].getText().trim();
				}
				if(roomTypeFilter.accept(node)) {
					//logger.info(node.getChildren().toNodeArray()[0].getText().trim());
				}
				if(priceFilter.accept(node)) {
					for(Node child : node.getChildren().toNodeArray()) {
						if(child.getText().contains(HTML_EURO)) {
							price = child.getText().trim();
						}
					}
					price = price.replace(HTML_EURO, "").replace("&nbsp;", "").trim();
					if(price.length() > 0 && hotel != "") {
						addPrice(hotel, key, price, true);
						pageHotels ++;
						hotel = "";
					}
				}
			}
			offset += pageHotels;
			logger.info("*** " + pageHotels + " hotels");
		}
		return true;
	}
}
