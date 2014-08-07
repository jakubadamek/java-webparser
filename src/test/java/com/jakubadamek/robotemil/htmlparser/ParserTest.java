package com.jakubadamek.robotemil.htmlparser;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import com.jakubadamek.robotemil.DateLosWeb;
import com.jakubadamek.robotemil.WebStruct;
import com.jakubadamek.robotemil.WorkUnit;

public class ParserTest {

	@Test
	public void testLastminuteEs() throws Exception {
		test(new LastminuteEs());
	}
	
	@Test
	public void testBookingCom() throws Exception {
		test(new BookingCom());
	}
		
	@Test
	public void testHrsCom() throws Exception {
		test(new HrsCom());
	}
	
	private static boolean checkNoDiacritics(String withDiacritics) {
		String allowed = "¡";
		for(int i=0; i < withDiacritics.length(); i ++) {
			char c = withDiacritics.charAt(i);
			if(c >= 128 && allowed.indexOf(c) == -1) {
				return false;
			} 
		}
		return true;
	}
	
	private void test(HtmlParser parser) throws Exception {
		WebStruct web = new WebStruct();
		WorkUnit workUnit = new WorkUnit(new DateLosWeb(new DateTime().plusDays(1).toDate(), 1, web));
		workUnit.maxPages = 1;
		parser.init(workUnit, null);
		parser.run();
		Assert.assertTrue(parser.getPrices().size() > 3);
		for(String hotel : parser.getPrices().getData().keySet()) {
			Assert.assertTrue(hotel, checkNoDiacritics(hotel));
		}
	}
}
