package com.jakubadamek.robotemil.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jakubadamek.robotemil.Prices;
import com.jakubadamek.robotemil.WorkUnitKey;
import com.jakubadamek.robotemil.entities.PriceAndOrder;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

public class HttpPriceService implements PriceService {
    private final Logger logger = Logger.getLogger(getClass());
    //private static final String SERVER = "http://localhost:8088/trick/store.php";
    private static final String SERVER = "http://jakubadamek.me.cz/trickbenchmark/store.php";
    private static final DateTimeFormatter DATE_TIME_FORMAT = new DateTimeFormatterBuilder()
    	.appendYear(4, 4).appendMonthOfYear(2).appendDayOfMonth(2).toFormatter();
    private static DateTimeZone ZONE = DateTimeZone.forTimeZone(TimeZone.getTimeZone("CES"));

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
	
	private List<HttpPriceDTO> pricesToDtos(Prices prices) {
		List<HttpPriceDTO> dtos = new ArrayList<HttpPriceDTO>();
		for(String hotel : prices.getData().keySet()) {
			Map<WorkUnitKey, PriceAndOrder> map2 = prices.getData().get(hotel); 
			for(WorkUnitKey workUnitKey : map2.keySet()) {
				HttpPriceDTO dto = new HttpPriceDTO();
				dto.hotel = hotel;
				PriceAndOrder priceAndOrder = map2.get(workUnitKey); 
				dto.order = priceAndOrder.order;
				if(priceAndOrder.price != null) {
					dto.price = (int) (priceAndOrder.price * 100 + 0.5);
				}
				dtos.add(dto);
			}
		}	
		return dtos;
	}
	
	private String marshal(Prices prices) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		String json = objectMapper.writeValueAsString(pricesToDtos(prices));
		
		ByteOutputStream bos = new ByteOutputStream();
        GZIPOutputStream gzip = 
        		new GZIPOutputStream(
    						new Base64OutputStream(
    								bos));
        gzip.write(json.getBytes("UTF-8"));
        gzip.close();
        
        return bos.toString(); //new String(bos.getBytes());
	}
	
	private HttpPriceDTO[] unmarshal(InputStream inputStream) throws IOException {
		GZIPInputStream gzipInputStream = 
				new GZIPInputStream(
						new Base64InputStream(
								inputStream));
		StringWriter stringWriter = new StringWriter();
		try {
			IOUtils.copy(gzipInputStream, stringWriter);		
		} finally {
			gzipInputStream.close();
		}
		String json = stringWriter.toString();
		if(json.length() > 10) {
			ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.readValue(json, HttpPriceDTO[].class);
		}
		return new HttpPriceDTO[0];
	}
	
	@Override
	public void persistPrices(String web, Prices prices, WorkUnitKey key) {
		try {			
		    HttpClient client = new DefaultHttpClient();
		    HttpPost post = new HttpPost(SERVER);
		    String pricesString = marshal(prices);
		    
		    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("los", String.valueOf(key.getLengthOfStay())));
			nameValuePairs.add(new BasicNameValuePair("web", web));
			nameValuePairs.add(new BasicNameValuePair("date", formatDate(key.getDate())));
			nameValuePairs.add(new BasicNameValuePair("today", formatDate(new Date())));
			nameValuePairs.add(new BasicNameValuePair("prices", pricesString));
			nameValuePairs.add(new BasicNameValuePair("crc", md5(pricesString)));
			     
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			 
			HttpResponse response = client.execute(post);
			StringWriter writer = new StringWriter();
			IOUtils.copy(response.getEntity().getContent(), writer, "UTF-8");
			logger.info("Send JSON response: " + writer.toString());
		} catch(Exception e) {
			logger.error("", e);
		}
	}

	@Override
	public int readPrices(String web, Prices prices, WorkUnitKey key) {
		try {
			String urlText = SERVER + 
					"?web=" + web + 
					"&los=" + key.getLengthOfStay() + 
					"&date=" + formatDate(key.getDate()) +
					"&today=" + formatDate(new Date());
			URL url = new URL(urlText);
			InputStream inputStream = url.openStream();
			HttpPriceDTO[] dtos = unmarshal(inputStream);
			for(HttpPriceDTO dto : dtos) {
				prices.addPrice(dto.hotel, key, dto.price / 100.0, dto.order, true);
			}
			return dtos.length;
		} catch(Exception e) {
			logger.error("", e);
		}
		return 0;
	}

	@Override
	public void createTables() {
		// nothing to do
	}

}
