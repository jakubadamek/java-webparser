package com.jakubadamek.robotemil.htmlparser;

import java.util.Calendar;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.ParserException;

import com.jakubadamek.robotemil.App;

/**
 * Parses booking.com
 * 
 * @author Jakub
 */
class HrsCom2 extends HtmlParser {
	private static final String BOOKING_COM =
		"http://www.booking.com/searchresults.html?city=-553173&ssne=Prague" +
		"&order=&addressAddress=&addressCity=&addressZIP=&addressCountry=cz" +
		"&si=ai%2Cco%2Cci%2Cre&ss=Prague&radius=15&do_availability_check=on" +
		"&rows=50";
	
	@Override
	public void run() throws ParserException {
		String url = BOOKING_COM;
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(this.date);
	    //checkin_monthday=22;checkin_year_month=2008-8;" +
		//"checkout_monthday=23;checkout_year_month=2008-8;" +
		url += ";checkin_monthday=" + calendar.get(Calendar.DAY_OF_MONTH);
		url += ";checkin_year_month=" + calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1);
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		url += ";checkout_monthday=" + calendar.get(Calendar.DAY_OF_MONTH);
		url += ";checkout_year_month=" + calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1);
		System.out.println(url);
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
		NodeFilter takeAllFilter = new OrFilter(hotelNameFilter, priceFilter); 
		int ipage = 0;
		int pageHotels = 100;
		while(pageHotels > 0) {
		    if(App.stop)
		    	return;
			String hotel = "";
			String price = "";
			String pagedUrl = url + "&offset=" + (ipage * 50);
			ipage ++;
			pageHotels = 0;
			Parser parser = new Parser(pagedUrl);
			for(Node node : parser.extractAllNodesThatMatch(takeAllFilter).toNodeArray()) {
			    if(App.stop)
			    	return;
				if(hotelNameFilter.accept(node)) {
					hotel = node.getChildren().toNodeArray()[0].getText().trim();
				}
				if(priceFilter.accept(node)) {
					if(node.getChildren().size() > 2) {
						price = node.getChildren().toNodeArray()[2].getText().trim();
					} else {
						price = node.getChildren().toNodeArray()[0].getText().trim();
					}
					price = price.replace("&#x20AC;", "").replace("&nbsp;", "").trim();
					if(price.length() > 0 && hotel != "") {
						addPrice(hotel, this.date, price);
						pageHotels ++;
						hotel = "";
					}
				}
			}
			System.out.println("*** " + pageHotels + " hotels");
		}
	}	
}
