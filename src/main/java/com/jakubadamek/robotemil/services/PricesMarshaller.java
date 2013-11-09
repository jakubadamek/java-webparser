package com.jakubadamek.robotemil.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jakubadamek.robotemil.Prices;
import com.jakubadamek.robotemil.WorkUnitKey;
import com.jakubadamek.robotemil.entities.PriceAndOrder;

public class PricesMarshaller {
    private static final Logger logger = Logger.getLogger(PricesMarshaller.class);
    
	private static List<HttpPriceDTO> pricesToDtos(Prices prices, WorkUnitKey key) {
		List<HttpPriceDTO> dtos = new ArrayList<HttpPriceDTO>();
		for(String hotel : prices.getData().keySet()) {
			Map<WorkUnitKey, PriceAndOrder> map2 = prices.getData().get(hotel); 
			HttpPriceDTO dto = new HttpPriceDTO();
			dto.hotel = hotel;
			PriceAndOrder priceAndOrder = map2.get(key);
			if(priceAndOrder != null) {
				dto.order = priceAndOrder.order;
				dto.price = priceAndOrder.price;
				dtos.add(dto);
			}
		}	
		return dtos;
	}
	
	public static String marshal(Prices prices, WorkUnitKey key) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		String json = objectMapper.writeValueAsString(pricesToDtos(prices, key));
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream gzip = 
        		new GZIPOutputStream(
    						new Base64OutputStream(
    								bos));
        gzip.write(json.getBytes("UTF-8"));
        gzip.close();
        
        String retval = bos.toString(); 
        
        String retval2 = PricesMarshaller2.marshal(prices, key);
        logger.info("old length " + retval.length() + " new length " + retval2.length());
        
        return retval;
	}
	
	private static HttpPriceDTO[] unmarshal(InputStream inputStream) throws IOException {
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
	
	public static int unmarshal(InputStream inputStream, Prices prices, WorkUnitKey key) throws IOException {
		HttpPriceDTO[] dtos = unmarshal(inputStream);
		for(HttpPriceDTO dto : dtos) {
			prices.addPrice(dto.hotel, key, dto.price, dto.order, true);
		}
		return dtos.length;		
	}
}
