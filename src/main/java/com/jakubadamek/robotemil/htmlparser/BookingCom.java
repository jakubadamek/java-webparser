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
	private static final int MAX_TRIALS = 3;
	private static final String HTML_EURO = "&#x20AC;";
	private static final String BOOKING_COM =
		"http://www.booking.com/searchresults.en-us.html?" +
		"class_interval=1;" +
		"group_adults=1;group_children=0;" +
		"inac=0;" +
		"redirected_from_city=0;" +
		"redirected_from_landmark=0;" +
		"review_score_group=empty;score_min=0;" +
		"si=ai%2Cco%2Cci%2Cre%2Cdi;src=index;ss_all=0;;" +
		"city=-553173;origin=disamb;srhash=2002341665;srpos=1;rows=50;selected_currency=EUR";
	
	/** Should the class care about breakfast? */
	protected boolean careAboutBreakfast;
	
	public BookingCom() {
		careAboutBreakfast = false;
	}
	
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
		
		NodeFilter breakfastFilter = new TagNameFilter("sup");
		
		NodeFilter roomRowFilter = 
			new AndFilter(
					new TagNameFilter("tr"),
					new HasAttributeFilter("class", "roomrow "));
		
		NodeFilter takeAllFilter = new OrFilter(new OrFilter(hotelNameFilter, priceFilter), roomTypeFilter);
		@SuppressWarnings("unused")
		int ipage = 0;
		int pageHotels = 1;
		int offset = 0;
		int trials = 0;
		// 20120603 booking.com nikdy nedobehl, protoze porad dokola nacital posledni stranku
		String firstHotelOnPage = "";
		// 20120603 
		boolean lastPageRepeats = false;
		while(! lastPageRepeats && (pageHotels > 0 || trials < MAX_TRIALS)) {
		    if(isStop()) return false;
			String hotel = "";
			String price = "";
			boolean breakfastIncluded = false;			
			String pagedUrl = url + ";offset=" + offset;
			ipage ++;
			pageHotels = 0;
			Parser parser = new Parser(pagedUrl);
		    if(isStop()) return false;
			// 20120603 
		    boolean firstHotel = true;
			for(Node node : parser.extractAllNodesThatMatch(takeAllFilter).toNodeArray()) {
			    if(isStop()) return false;
				if(hotelNameFilter.accept(node)) {
					hotel = node.getChildren().toNodeArray()[0].getText().trim();
				}
				if(roomTypeFilter.accept(node)) {
					//logger.info(node.getChildren().toNodeArray()[0].getText().trim());
				}
				if(roomRowFilter.accept(node)) {
					breakfastIncluded = false;
				}
				if(careAboutBreakfast && breakfastFilter.accept(node)) {
					String text = node.getChildren().toNodeArray()[0].getText().trim();
					if(text.contains("Breakfast included")) {
						breakfastIncluded = true;
					}
				}
				if(priceFilter.accept(node)) {
					for(Node child : node.getChildren().toNodeArray()) {
						if(child.getText().contains(HTML_EURO)) {
							price = child.getText().trim();
						}
					}
					price = price.replace(HTML_EURO, "").replace("&nbsp;", "").trim();
					if(price.length() > 0 && hotel != "") {
						if(! careAboutBreakfast) {
							breakfastIncluded = true;
						}
						addPrice(hotel, key, price, true, Currency.EUR, breakfastIncluded);
						// 20120603 
						if(firstHotel) {
							if(firstHotelOnPage != "" && firstHotelOnPage.equals(hotel)) {
								lastPageRepeats = true;
							}
							firstHotelOnPage = hotel;
							firstHotel = false;
						}
						pageHotels ++;
						hotel = "";
					}
				}
			}
			offset += pageHotels;
			logger.info("*** " + pageHotels + " hotels " + key + " " + pagedUrl);
			if(pageHotels == 0) {
				trials ++;
			} else {
				trials = 0;
			}
		}
		return true;
	}
}
