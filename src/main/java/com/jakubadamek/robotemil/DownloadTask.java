package com.jakubadamek.robotemil;

import java.util.Date;

import org.apache.log4j.Logger;

import com.jakubadamek.robotemil.htmlparser.HtmlParser;

public class DownloadTask implements Runnable {
	
    private final Logger logger = Logger.getLogger(getClass());
    
	private WorkUnit workUnit;
	private final App app;

	public DownloadTask(WorkUnit workUnit, App app) {
		this.workUnit = workUnit;
		this.app = app;
	}

	@Override
	public void run() {
		String workUnitDesc = this.workUnit.web.getParams().getLabel() + " " + this.workUnit.key;
		logger.info("Starting thread for " + workUnitDesc);
		workUnit.restartNow = false;
		if(workUnit.finished) {
			return;
		}
		int readFromCache = 0;
		try {
			if(this.app.isUseCache()) {
				readFromCache = app.priceService.readPrices(this.workUnit.web.getParams().getExcelName(), this.workUnit.web.getPrices(), this.workUnit.key);
				if(readFromCache > 0) {
					this.app.showLog("Cache " + workUnitDesc + ": " + readFromCache + " " + this.app.getBundleString("data nalezena v cache"));
					workUnit.finished = true;
					logger.info("Latch count down - read from CACHE " + readFromCache + " records");
				} else {
					readFromCache = app.httpPriceService.readPrices(this.workUnit.web.getParams().getExcelName(), this.workUnit.web.getPrices(), this.workUnit.key);
					if(readFromCache > 0) {
						this.app.showLog("Cache " + workUnitDesc + ": " + readFromCache + " " + this.app.getBundleString("data nalezena na serveru"));
						workUnit.finished = true;
						logger.info("Latch count down - read from SERVER " + readFromCache + " records");
					}
				}
				if(readFromCache > 0) {
					this.app.workUnitsManager.getLatch().countDown();
					return;
				}
			}
			Date start = new Date();
			this.app.showLog("Start " + workUnitDesc);
			HtmlParser htmlParser = this.workUnit.web.getParams().getParserClass().newInstance();
			htmlParser.init(this.workUnit, this.app);
			if(htmlParser.run()) {
				if(htmlParser.getPrices().size() > 25) {
					this.workUnit.web.getPrices().addAll(htmlParser.getPrices());
					String web = this.workUnit.web.getParams().getExcelName();
					//app.priceService.persistPrices(web, htmlParser.getPrices(), this.workUnit.key);
					app.httpPriceService.persistPrices(web, htmlParser.getPrices(), this.workUnit.key);
					this.app.showLog("Hotovo " + workUnitDesc + ": " + htmlParser.getPrices().size()
							+ " hotelu za " + (new Date().getTime() - start.getTime()) / 1000 + " s");
					workUnit.finished = true;
	                logger.info("Latch count down - read from WEB " + htmlParser.getPrices().size() + " records");
					this.app.workUnitsManager.getLatch().countDown();					
				} else {
					this.app.showLog("Chyba, nenacteno pouze " + workUnitDesc + " zaznamu");
					workUnit.restartNow = true;
				}
			} else {
				this.app.showLog("Stop " + workUnitDesc);				
			}
		} catch (Throwable e) {
			e.printStackTrace();
			this.app.showLog("Chyba " + workUnitDesc);
			this.app.workUnitsManager.submit(this.workUnit);
		}
	}

}
