package com.jakubadamek.robotemil.htmlparser;

import java.io.IOException;
import java.util.Date;

import org.htmlparser.util.ParserException;
import org.junit.Assert;
import org.junit.Test;

import com.jakubadamek.robotemil.WebStruct;
import com.jakubadamek.robotemil.WorkUnit;
import com.jakubadamek.robotemil.WorkUnitKey;

public class LastminuteEsTest {

	@Test
	public void test() throws ParserException, IOException {
		LastminuteEs parser = new LastminuteEs();
		WebStruct web = new WebStruct();
		WorkUnit workUnit = new WorkUnit(new WorkUnitKey(new Date(), 1), web);
		parser.init(workUnit, null);
		parser.run();
		Assert.assertTrue(parser.getPrices().size() > 3);
	}
}
