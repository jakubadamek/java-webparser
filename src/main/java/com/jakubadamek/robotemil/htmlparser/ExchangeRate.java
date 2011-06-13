package com.jakubadamek.robotemil.htmlparser;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.util.ParserException;

public class ExchangeRate {
	private static final String URL = "http://www.x-rates.com/";
	private static final String HREF = "/d/USD/EUR/graph120.html";
	
	public double currentUsdEur() {
		NodeFilter usdEurFilter = new HasAttributeFilter("href", HREF);
		try {
			Parser parser = new Parser(URL);
			for(Node node : parser.extractAllNodesThatMatch(usdEurFilter).toNodeArray()) {
				String rate = node.getChildren().toNodeArray()[0].getText().trim();
				return Double.valueOf(rate);
			}
		} catch (ParserException e) {
			throw new RuntimeException(e);
		}
		return 0;
	}
}
