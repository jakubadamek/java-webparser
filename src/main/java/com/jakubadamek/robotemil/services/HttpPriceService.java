package com.jakubadamek.robotemil.services;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Repository;

import com.jakubadamek.robotemil.DateLosWeb;
import com.jakubadamek.robotemil.Prices;
import com.jakubadamek.robotemil.services.util.IWebToPrices;

@Repository
public class HttpPriceService implements PriceService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    //private static final String SERVER = "http://localhost/store/";
    private static final String SERVER = "http://jakubadamek.me.cz/trickbenchmark/store/";
    private static final DateTimeFormatter DATE_TIME_FORMAT = new DateTimeFormatterBuilder()
    	.appendYear(4, 4).appendMonthOfYear(2).appendDayOfMonth(2).toFormatter();
    private static DateTimeZone ZONE = DateTimeZone.forTimeZone(TimeZone.getTimeZone("CES"));
    
    private PriceService jdbcPriceService;

	public static String md5(String input) throws NoSuchAlgorithmException {
	    String result = input;
	    if(input != null) {
	        MessageDigest md = MessageDigest.getInstance("MD5"); //or "SHA-1"
	        md.update(input.getBytes());
	        BigInteger hash = new BigInteger(1, md.digest());
	        result = hash.toString(16);
	        while(result.length() < 32) { //40 for SHA-1
	            result = "0" + result;
	        }
	    }
	    return result;
	}
	

	private String formatDate(Date date) {
		return DATE_TIME_FORMAT.print(new DateTime(date).withZone(ZONE)); 
	}
	
	@Override
	public void persistPrices(Prices prices, DateLosWeb key) {
		try {			
			
			HttpClient client = HttpClientBuilder.create()
				.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(120000).build())
				.build();
			
		    HttpPost post = new HttpPost(SERVER + "store.php");
		    String pricesString = PricesMarshaller.marshal(prices, key);
		    
		    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("los", String.valueOf(key.getLengthOfStay())));
			nameValuePairs.add(new BasicNameValuePair("web", key.getWeb()));
			nameValuePairs.add(new BasicNameValuePair("date", formatDate(key.getDate())));
			nameValuePairs.add(new BasicNameValuePair("today", formatDate(new Date())));
			nameValuePairs.add(new BasicNameValuePair("prices", pricesString));
			nameValuePairs.add(new BasicNameValuePair("crc", md5(pricesString)));
			     
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			 
			HttpResponse response = client.execute(post);
			StringWriter writer = new StringWriter();
			IOUtils.copy(response.getEntity().getContent(), writer, "UTF-8");
			logger.info("Received JSON response in persistPrices: " + writer.toString().trim());
		} catch(Exception e) {
			logger.error("", e);
		}
	}

	@Override
	public int readPrices(Prices prices, DateLosWeb key) {
		try {
			String urlText = SERVER + 
					"?web=" + key.getWeb() + 
					"&los=" + key.getLengthOfStay() + 
					"&date=" + formatDate(key.getDate()) +
					"&today=" + formatDate(new Date());
			URL url = new URL(urlText);
			InputStream inputStream = url.openStream();
			return PricesMarshaller.unmarshal(inputStream, prices, key);
		} catch(Exception e) {
			logger.error("", e);
		}
		return 0;
	}

	@Override
	public void createTables() {
		// nothing to do
	}

	@Override
	public void deleteRefreshedData(DateLosWeb key) {
		// do nothing
	}

	@Override
	public 	int lookup(Set<DateLosWeb> dateLosWebs, IWebToPrices webToPrices, int minRows) {
		try {
			URL url = new URL(lookupUrl(dateLosWebs));
			BufferedInputStream inputStream = new BufferedInputStream(url.openStream());
			//ZipInputStream zipIs = new ZipInputStream(inputStream);
			File temp = File.createTempFile("temp",".zip");
			FileOutputStream fos = new FileOutputStream(temp);
			IOUtils.copy(inputStream, fos);
			inputStream.close();
			fos.close();

			ZipFile zipFile = new ZipFile(temp);
		    Enumeration<? extends ZipEntry> entries = zipFile.entries();
		    int found = 0;
		    while(entries.hasMoreElements()){
		        ZipEntry entry = entries.nextElement();
		        InputStream stream = zipFile.getInputStream(entry);
		        DateLosWeb key = fromFileName(entry.getName());
		        Prices prices = webToPrices.get(key.getWeb());
		        int rows = PricesMarshaller.unmarshal(stream, prices, key);
		        if(rows >= minRows) {
		        	found ++;
		        	dateLosWebs.remove(key);
			        jdbcPriceService.persistPrices(prices, key);
		        }
		    }
		    zipFile.close();
		    return found;
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String lookupUrl(Set<DateLosWeb> dateLosWebs) {
		StringBuilder urlText = new StringBuilder();
		urlText.append(SERVER).append("load.php?today=").append(formatDate(new Date()));
		urlText.append("&dateLosWebs=");
		boolean first = true;
		for(DateLosWeb dateLosWeb : dateLosWebs) {
			if(! first) {
				urlText.append(";");
			} 
			first = false;
			urlText.append(formatDate(dateLosWeb.getDate()))
				.append(",")
				.append(dateLosWeb.getLengthOfStay())
				.append(",")
				.append(dateLosWeb.getWeb());
		}
		logger.info("lookup URL " + urlText);
		return urlText.toString();
	}
	
	private DateLosWeb fromFileName(String fileName) {
		// 20900404-1-hrs.json
		int pos1 = fileName.indexOf("-");
		int pos2 = fileName.indexOf("-", pos1+1);
		int pos3 = fileName.indexOf(".", pos2+1);
		
		String date = fileName.substring(0, pos1);
		String los = fileName.substring(pos1+1, pos2);
		String web = fileName.substring(pos2+1, pos3);
		
		logger.info("Parsed " + fileName + " to date=" + date + " los=" + los + " web=" + web);
		
		return new DateLosWeb(DATE_TIME_FORMAT.parseDateTime(date).toDate(), Integer.valueOf(los), web);
	}

	@Required
	public void setJdbcPriceService(PriceService jdbcPriceService) {
		this.jdbcPriceService = jdbcPriceService;
	}	
}

