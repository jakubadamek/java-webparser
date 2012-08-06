package com.jakubadamek.robotemil.htmlparser;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

public class ExchangeRate {
	private static final String URL_GOOGLE = "http://www.google.com/ig/calculator?hl=en&q=1EUR%3D%3FUSD";
	
	public double currentUsdEur() throws IOException {
		URL url = new URL(URL_GOOGLE);
		InputStream inputStream = url.openStream();
		StringWriter stringWriter = new StringWriter();
		try {
			IOUtils.copy(inputStream, stringWriter);		
		} finally {
			inputStream.close();
		}
		String jsonString = stringWriter.toString();
		jsonString = jsonString.substring(jsonString.indexOf("rhs: ") + 6);
		jsonString = jsonString.substring(0, jsonString.indexOf(" "));
		return Double.valueOf(jsonString);
	}
}
