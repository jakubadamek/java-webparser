package com.jakubadamek.robotemil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.junit.Test;

public class RegExpTest {
    @Test
    public void testPage1of20() {
        String text = new String(new byte[] { 80, 97, 103, 101, -96, 49, -96, 111, 102, -96, 50, 48 });
        System.out.println(text);
        Matcher matcher = Pattern.compile("of\\W([0-9]+)").matcher(text);
        Assert.assertTrue(matcher.find());
        System.out.println(matcher.group(1));
    }
}
