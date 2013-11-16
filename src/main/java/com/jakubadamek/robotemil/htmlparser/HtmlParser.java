package com.jakubadamek.robotemil.htmlparser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.jakubadamek.robotemil.App;
import com.jakubadamek.robotemil.DateLosWeb;
import com.jakubadamek.robotemil.DiacriticsRemover;
import com.jakubadamek.robotemil.Prices;
import com.jakubadamek.robotemil.WorkUnit;

/**
 * Ancestor to the parsing classes
 * 
 * @author Jakub
 */
public abstract class HtmlParser {
	private static final Logger logger = LoggerFactory.getLogger(HtmlParser.class);
	private static final int CONNECTION_TIMEOUT = 120000;
	/** date for which prices are searched */
	protected Date dateFrom;
	protected DateTime dateTo;
	protected DateLosWeb key;
	protected int maxPages;
	/** prices - the result of parsing */
	private Prices prices;
	private int order;
	private App app;
	private WorkUnit workUnit;

	/**
	 * Constructor
	 * 
	 * @param aWorkUnit
	 * @param aApp
	 */
	public void init(WorkUnit aWorkUnit, App aApp) {
		this.dateFrom = aWorkUnit.key.getDate();
		this.dateTo = new DateTime(aWorkUnit.key.getDate())
				.plusDays(aWorkUnit.key.getLengthOfStay());
		this.key = aWorkUnit.key;
		this.workUnit = aWorkUnit;
		this.prices = new Prices();
		this.app = aApp;
		this.order = 0;
		this.maxPages = aWorkUnit.maxPages;
	}

	/**
	 * Runs the real job. Returns true if it runs to the end, false if stopped
	 * in the middle.
	 * 
	 * @throws Exception
	 */
	public abstract boolean run() throws Exception;

	/**
	 * Adds price to the inner structure
	 * 
	 * @param aHotel
	 * @param aDate
	 * @param price
	 * @param divideByLOS
	 */
	protected void addPrice(String aHotel, DateLosWeb aKey, String price,
			boolean divideByLOS) {
		addPrice(aHotel, aKey, price, divideByLOS, Currency.EUR, true);
	}

	private double usdEurExchangeRate;

	protected void addPrice(String aHotel, DateLosWeb aKey, String price,
			boolean divideByLOS, Currency currency, boolean breakfastIncluded) {
		this.order++;
		String hotel = DiacriticsRemover.removeDiacritics(aHotel.replace(
				"&amp;", "&"));
		final String logRow = getClass().getSimpleName() + " " + this.order
				+ " " + hotel + " " + price + " " + key;
		logger.info(logRow);
		if (this.app != null) {
			// this.app.showLog(logRow);
		}
		Double priceDouble = null;
		if (price != null) {
			price = price.replaceAll(",", "");
			if (divideByLOS) {
				priceDouble = Double.valueOf(price) / aKey.getLengthOfStay();
			} else {
				priceDouble = Double.valueOf(price);
			}
		}
		if (priceDouble != null && currency != Currency.EUR) {
			switch (currency) {
			case USD:
				if (usdEurExchangeRate == 0) {
					try {
						usdEurExchangeRate = new ExchangeRate().currentUsdEur();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				priceDouble /= usdEurExchangeRate;
				break;
			default:
				throw new IllegalArgumentException(currency.name());
			}
		}
		int priceInt = priceDouble == null ? 0 : (int) (priceDouble * 100);
		this.prices.addPrice(hotel, aKey, priceInt, this.order, breakfastIncluded);
	}

	/**
	 * @return the stop
	 */
	public boolean isStop() {
		this.workUnit.lastResponseTime = new Date();
		return this.workUnit.finished || App.stop;
	}

	/**
	 * @return the prices
	 */
	public Prices getPrices() {
		return this.prices;
	}

	protected void selectOption(HtmlPage page, String selectXPath,
			String optionValue) throws IOException {
		logger.info("Setting " + selectXPath + " to " + optionValue);
		HtmlSelect select = null;
		try {
			select = (HtmlSelect) page.getByXPath(selectXPath).get(0);
			select.setSelectedAttribute(select.getOptionByValue(optionValue),
					true);
		} catch (Exception e) {
			if (select == null) {
				throw new IOException("select " + selectXPath
						+ " could not be found");
			}
			throw new IOException("Page " + page + " selectXPath "
					+ selectXPath + " options " + select.getOptions()
					+ " optionValue " + optionValue + " " + e.toString());
		}
		logger.info("Finished setting " + selectXPath + " to " + optionValue);
	}

	protected void fillTextField(HtmlPage page, String fieldName, String value)
			throws IOException {
		logger.info("Setting " + fieldName + " to " + value);
		String xPath = "//input[@name='" + fieldName + "']";
		HtmlInput input = null;
		try {
			input = (HtmlInput) page.getByXPath(xPath).get(0);
			input.setValueAttribute(value);
		} catch (Exception e) {
			if (input == null) {
				throw new IOException("Page " + page + " input " + fieldName
						+ " " + e.toString());
			}
		}
		logger.info("Finished setting " + fieldName + " to " + value);
	}

	protected void savePage(HtmlPage page) {
		try {
			File htmlFile = new File("backup.html");
			logger.info("Saving html page to " + htmlFile.getCanonicalPath());
			if (htmlFile.exists()) {
				htmlFile.delete();
			}
			page.save(htmlFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void savePage(String pageSource) {
		try {
			File htmlFile = new File("backup.html");
			logger.info("Saving html page to " + htmlFile.getCanonicalPath());
			if (htmlFile.exists()) {
				htmlFile.delete();
			}
			FileWriter fileWriter = new FileWriter(htmlFile);
			fileWriter.write(pageSource);
			fileWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void selectRadio(HtmlPage page, String radioXPath)
			throws IOException {
		logger.info("Selecting radio " + radioXPath);
		HtmlRadioButtonInput radio = null;
		try {
			radio = (HtmlRadioButtonInput) page.getByXPath(radioXPath).get(0);
			radio.setChecked(true);
		} catch (Exception e) {
			if (radio == null) {
				throw new IOException("radio " + radioXPath
						+ " could not be found");
			}
			throw new IOException("Page " + page + " radioXPath " + radioXPath
					+ " " + e.toString());
		}
		logger.info("Finished selecting " + radioXPath);
	}

	protected HtmlPage clickAnchor(HtmlPage page, String anchorXPath)
			throws IOException {
		logger.info("Clicking on anchor " + anchorXPath);
		HtmlAnchor anchor = null;
		try {
			anchor = (HtmlAnchor) page.getByXPath(anchorXPath).get(0);
			HtmlPage retval = anchor.click();
			logger.info("Finished clicking on " + anchorXPath);
			return retval;
		} catch (Throwable e) {
			e.printStackTrace();
			if (anchor == null) {
				throw new IOException("anchor " + anchorXPath
						+ " could not be found");
			}
			throw new IOException("Page " + page + " anchor " + anchorXPath
					+ " " + e.toString());
		}
	}

	private String fetchHtml(InputStream is, String via, long start) {	
		StringWriter os = null;		
		String retval = null;
		try {
			os = new StringWriter();
			/*FileOutputStream fos = new FileOutputStream(new File("temp.html")); 
			IOUtils.copy(is, fos);
			fos.close();*/
			IOUtils.copy(is, os);
			retval = os.toString();
			return retval;
		} catch (IOException e) {
			logger.info("Fetching html via {} timed out after {} ms", via, System.currentTimeMillis() - start, e);
			return null;
		} finally {
			logger.info("Fetching html via {} took {} ms, length {} bytes", 
					via, System.currentTimeMillis() - start, (retval == null ? 0 : retval.length()));
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(os);
		}
	}
	
	protected String fetchHtml1(String url) {
		long start = System.currentTimeMillis();
		try {
			URLConnection urlConnection = new URL(url).openConnection();
			urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
			urlConnection.setReadTimeout(CONNECTION_TIMEOUT);
			return fetchHtml(urlConnection.getInputStream(), "URLConnection", start);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected String fetchHtml2(String url) {
		long start = System.currentTimeMillis();
		try {
			HttpClient client = HttpClientBuilder.create()
					.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(CONNECTION_TIMEOUT).build())
					.build();
			HttpGet httpGet = new HttpGet(url);
			httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.2; rv:25.0) Gecko/20100101 Firefox/25.0");
			return fetchHtml(client.execute(httpGet).getEntity().getContent(), "HttpClient", start);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
