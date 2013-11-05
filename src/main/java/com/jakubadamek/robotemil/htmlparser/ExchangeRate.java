package com.jakubadamek.robotemil.htmlparser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ExchangeRate {
    private static final Logger logger = Logger.getLogger(ExchangeRate.class);
    private static final String URL = "http://openexchangerates.org/api/latest.json?app_id=dfdc5b2857654ec0834124b56c7523b0";

	public double currentUsdEur() throws IOException {
		URL url = new URL(URL);
		InputStream inputStream = url.openStream();
		String jsonString;
		try {
			jsonString = IOUtils.toString(inputStream, "UTF-8");
		} finally {
			inputStream.close();
		}
		logger.debug(jsonString);
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode tree = objectMapper.readTree(jsonString);
		return 1.0 / tree.get("rates").get("EUR").doubleValue();
	}
}
