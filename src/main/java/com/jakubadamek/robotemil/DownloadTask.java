package com.jakubadamek.robotemil;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jakubadamek.robotemil.htmlparser.HtmlParser;

public class DownloadTask implements Runnable {
	
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
	private WorkUnit workUnit;
	private final App app;

	public DownloadTask(WorkUnit workUnit, App app) {
		this.workUnit = workUnit;
		this.app = app;
	}

	@Override
	public void run() {
		WebStruct webStruct = app.webStruct(workUnit.key.getWeb());
		String workUnitDesc = webStruct.getParams().getLabel() + " " + this.workUnit.key;
		logger.info("Starting thread for " + workUnitDesc);
		workUnit.restartNow = false;
		if(workUnit.finished) {
			return;
		}
		try {
			Date start = new Date();
			this.app.showLog("Start " + workUnitDesc);
			HtmlParser htmlParser = webStruct.getParams().getParserClass().newInstance();
			htmlParser.init(this.workUnit, this.app);
			if(htmlParser.run()) {
				if(htmlParser.getPrices().size() > 5) {
					webStruct.getPrices().addAll(htmlParser.getPrices());
					app.jdbcPriceService.persistPrices(htmlParser.getPrices(), this.workUnit.key);
					app.httpPriceService.persistPrices(htmlParser.getPrices(), this.workUnit.key);
					this.app.showLog("Hotovo " + workUnitDesc + ": " + htmlParser.getPrices().size()
							+ " hotelu za " + (new Date().getTime() - start.getTime()) / 1000 + " s");
					workUnit.finished = true;
	                logger.info("Latch count down - read from WEB " + htmlParser.getPrices().size() + " records");
					this.app.workUnitsManager.getLatch().countDown();					
				} else {
					this.app.showLog("Chyba " + workUnitDesc + ", nacteno pouze "
									+ htmlParser.getPrices().size() + " zaznamu");
					workUnit.restartNow = true;
				}
			} else {
				this.app.showLog("Stop " + workUnitDesc);				
			}
		} catch (Throwable e) {
			logger.error("Chyba pri zpracovani " + workUnitDesc, e);
			this.app.showLog("Chyba " + workUnitDesc);
			this.app.workUnitsManager.submit(this.workUnit);
		}
	}

}
