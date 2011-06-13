package com.jakubadamek.robotemil;

import org.junit.Assert;
import org.junit.Test;

public class LevenstheinTest {

	@Test
	public void test() {
		Assert.assertEquals(0, Levensthein.wordDistance("ahoj", "ahoj"));
		Assert.assertEquals(1, Levensthein.wordDistance("bhoj", "ahoj"));
		Assert.assertEquals(1, Levensthein.wordDistance("ahoj", "Bhoj"));
		Assert.assertEquals(17, Levensthein.wordDistance("Radisson SAS Alcron", "Hotel Raddison Blu"));
	}
}
