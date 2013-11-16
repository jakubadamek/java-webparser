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
		
	private void test(HtmlParser parser) throws Exception {
		WebStruct web = new WebStruct();
		WorkUnit workUnit = new WorkUnit(new DateLosWeb(new DateTime().plusDays(2).toDate(), 1, web));
		workUnit.maxPages = 1;
		parser.init(workUnit, null);
		parser.run();
		Assert.assertTrue(parser.getPrices().size() > 3);
	}
}
