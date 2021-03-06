package com.jakubadamek.robotemil.htmlparser;

import java.io.IOException;
import java.util.Calendar;

import org.htmlparser.util.ParserException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses booking.com
 *
 * @author Jakub
 */  
public class BookingCom extends HtmlParser {
    private final Logger logger = LoggerFactory.getLogger(getClass());
	private static final int MAX_TRIALS = 3;
	private static final String HTML_EURO = "&#x20AC;";
	private static final String CHAR_EURO = "\u20ac";
	private static final String[] CITIES = new String[] {
		"-553173", // Praha
	};
	private static final String BOOKING_COM =
		"http://www.booking.com/searchresults.en-us.html?" +
		"class_interval=1;" +
		"group_adults=1;group_children=0;" +
		"inac=0;" +
		"redirected_from_city=0;" +
		"redirected_from_landmark=0;" +
		"review_score_group=empty;score_min=0;" +
		"si=ai%2Cco%2Cci%2Cre%2Cdi;src=index;ss_all=0;;" +
		"origin=disamb;srhash=2002341665;srpos=1;rows=50;selected_currency=EUR;city=";
	
	/** Should the class care about breakfast? */
	protected boolean careAboutBreakfast;
	
	public BookingCom() {
		careAboutBreakfast = false;
	}

	@Override
	public boolean run() throws ParserException, IOException {
		for (String city : CITIES) {
			if (! runCity(city)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean runCity(String city) {		
		String url = BOOKING_COM + city;
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
		
		//fetchHtml1(url);
		//fetchHtml2(url);
		
		int ipage = 0;
		int pageHotels = 1;
		int offset = 0;
		int trials = 0;
		// 20120603 booking.com nikdy nedobehl, protoze porad dokola nacital posledni stranku
		String firstHotelOnPage = "";
		boolean lastPageRepeats = false;
		while(! lastPageRepeats 
				&& (pageHotels > 0 || trials < MAX_TRIALS)
				&& (maxPages == 0 || ipage < maxPages)) {
		    if(isStop()) return false;
			String pagedUrl = url + ";offset=" + offset;
			ipage ++;
			pageHotels = 0;
			Document doc = Jsoup.parse(fetchHtml(pagedUrl));
		    if(isStop()) return false;
		    boolean firstHotel = true;
			for(Element div : doc.select("div.sr_item")) {
				String price = null;
				String hotel = "";
				boolean breakfastIncluded = false;
				
				Element aHotelName = div.select("a.hotel_name_link").first();
				if(aHotelName != null) {
					hotel = aHotelName.text().trim();
					// A B means B is any descendant of A
					Element elPrice = div.select("strong.price").first();
					if(elPrice != null) {
						price = elPrice.text();
					}
				}
			    if(isStop()) return false;
				if(price == null) {
					logger.error("Error parsing hotel " + hotel + " url " + url + " price is null");
					//FileUtils.writeStringToFile(new File("error.html"), doc.toString());
				} else {
					price = price.replace(HTML_EURO, "").replace(CHAR_EURO, "").replace("&nbsp;", "").trim();
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
			logger.info("*** " + pageHotels + " of " + offset + " hotels " + key + " " + pagedUrl);
			if(pageHotels == 0) {
				trials ++;
			} else {
				trials = 0;
			}
		}
		return true;
	}
}
