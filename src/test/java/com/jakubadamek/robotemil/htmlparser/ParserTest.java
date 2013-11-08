package com.jakubadamek.robotemil.htmlparser;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import com.jakubadamek.robotemil.WebStruct;
import com.jakubadamek.robotemil.WorkUnit;
import com.jakubadamek.robotemil.WorkUnitKey;

public class ParserTest {

	@Test
	public void testLastminuteEs() throws Exception {
		test(new LastminuteEs());
	}
	
	@Test
	public void testBookingCom() throws Exception {
		test(new BookingCom());
	}
		
	private void test(HtmlParser parser) throws Exception {
		WebStruct web = new WebStruct();
		WorkUnit workUnit = new WorkUnit(new WorkUnitKey(new DateTime().plusDays(2).toDate(), 1), web);
		workUnit.maxPages = 1;
		parser.init(workUnit, null);
		parser.run();
		Assert.assertTrue(parser.getPrices().size() > 3);
	}
}
