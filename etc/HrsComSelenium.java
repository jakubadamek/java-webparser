package com.jakubadamek.robotemil.htmlparser;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.thoughtworks.selenium.SeleneseTestBase;
import com.thoughtworks.selenium.Selenium;

/**
 * Parses hrs.com
 *
 * clientId=Y3NfX0hSUw--&cid=7-4&page=listOfHotels
 * clientId=Y3NfX0hSUw--&cid=3-4&activity=setRegion&regionKey=49370&regionType=city
 *
 * http://www.hrs.com/search.do;jsessionid=4E8CA1799BDCCC476E46A08BC6463687?clientId=Y3NfX0hSUw--&cid=12-1&location=Praha (Hlavní město Praha)&perimeter=20&startDateDay=20&startDateMonth=11&startDateYear=2008&endDateDay=21&endDateMonth=11&endDateYear=2008
 *
 * @author Jakub Adamek
 */	
public class HrsComSelenium extends HtmlParser
{
    private final Logger logger = Logger.getLogger(getClass());
	@Override
	public boolean run() throws FailingHttpStatusCodeException, IOException, InterruptedException {		 
		WebDriver driver = new FirefoxDriver();
        driver.get("http://www.hrs.com/web3");
        try {
			driver.findElement(By.id("destiny")).sendKeys("Prague (Praha)");
			driver.findElement(By.id("singleRooms")).sendKeys("1");
			WebElement adults = driver.findElement(By.id("adults"));
			adults.clear();
			adults.sendKeys("1");
			DateTimeFormatter dateFormat = DateTimeFormat.forPattern("dd.MM.yyyy");
			WebElement start = driver.findElement(By.id("start_stayPeriod"));
			start.clear();
			start.sendKeys(dateFormat.print(dateFrom.getTime()));
			WebElement end = driver.findElement(By.id("end_stayPeriod"));
			end.clear();
			end.sendKeys(dateFormat.print(dateTo.getMillis()));
			driver.findElement(By.id("adults")).submit();			
			while(true) {
				if(driver.findElements(By.className("hn")).size() > 0) {
					break;
				}
				Thread.sleep(1000);
			}
			savePage(driver.getPageSource());			
        } finally {
        	driver.close();
        }
	    return true;
	}

}