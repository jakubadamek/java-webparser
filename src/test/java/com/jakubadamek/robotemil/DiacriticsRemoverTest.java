package com.jakubadamek.robotemil;

import junit.framework.Assert;

import org.junit.Test;

public class DiacriticsRemoverTest {
	@Test
	public void test() {
		Assert.assertEquals(DiacriticsRemover.removeDiacritics("ěščřžýáíé"), "escrzyaie");
	}
}
