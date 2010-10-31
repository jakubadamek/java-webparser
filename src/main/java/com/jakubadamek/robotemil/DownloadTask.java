package com.jakubadamek.robotemil;

import java.text.DateFormat;
import java.util.Date;

import com.jakubadamek.robotemil.htmlparser.HtmlParser;

public class DownloadTask implements Runnable {
	
	private WorkUnit workUnit;
	private final App app;

	public DownloadTask(WorkUnit workUnit, App app) {
		this.workUnit = workUnit;
		this.app = app;
	}

	public void run() {
		System.out.println("Starting thread for " + this.workUnit.web.getParserClass().getSimpleName() + " " +
				DateFormat.getDateInstance().format(this.workUnit.date));
		if(workUnit.finished) {
			return;
		}
		int readFromCache = 0;
		DateFormat dateFormat = DateFormat.getDateInstance();
		String workUnitDesc = this.workUnit.web.getLabel() + " " + dateFormat.format(this.workUnit.date);
		if(this.app.isUseCache()) {
			readFromCache = app.priceService.readPrices(this.workUnit.web.getExcelName(), this.workUnit.web.getPrices(), this.workUnit.date);
			if(readFromCache > 0) {
				this.app.showLog("Cache " + workUnitDesc + ": " + readFromCache + " " + this.app.getBundleString("data nalezena v cache"));
				workUnit.finished = true;
				this.app.workUnitsManager.getLatch().countDown();
				return;
			}
		}
		try {
			Date start = new Date();
			this.app.showLog("Start " + workUnitDesc);
			HtmlParser htmlParser = this.workUnit.web.getParserClass().newInstance();
			htmlParser.init(this.workUnit, this.app);
			if(htmlParser.run()) {
				if(htmlParser.getPrices().size() > 0) {
					this.workUnit.web.getPrices().addAll(htmlParser.getPrices());
					app.priceService.persistPrices(this.workUnit.web.getExcelName(), htmlParser.getPrices(), this.workUnit.date);
					this.app.showLog("Hotovo " + workUnitDesc + ": " + htmlParser.getPrices().size()
							+ " hotelu za " + (new Date().getTime() - start.getTime()) / 1000 + " s");
					workUnit.finished = true;
					this.app.workUnitsManager.getLatch().countDown();
				} else {
					this.app.showLog("Chyba, nic nenacteno " + workUnitDesc);
				}
			} else {
				this.app.showLog("Stop " + workUnitDesc);				
			}
		} catch (Exception e) {
			e.printStackTrace();
			this.app.showLog("Chyba " + workUnitDesc);
			this.app.workUnitsManager.submit(this.workUnit);
		}
	}
}
