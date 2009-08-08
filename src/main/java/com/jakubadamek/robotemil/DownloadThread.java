package com.jakubadamek.robotemil;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jakubadamek.robotemil.htmlparser.HtmlParser;


/** downloads data from one web for one date */
class DownloadThread extends Thread {
	/**
	 * 
	 */
	private final App app;
	/**
	 * @param app
	 */
	DownloadThread(App app) {
		this.app = app;
	}
	/** Work unit */
	WorkUnit workUnit;
	/** html parser */
	HtmlParser htmlParser;
	@Override
	public void run() {
		while(! this.app.workQueue.isEmpty() && ! App.stop) {
			getWorkUnit();
			if(this.workUnit != null) {
    			System.out.println("Starting thread for " + this.workUnit.web.parserClass.getSimpleName() + " " + 
    					DateFormat.getDateInstance().format(this.workUnit.date));
				try {
					this.htmlParser = this.workUnit.web.parserClass.newInstance();
	    			this.htmlParser.init(this.workUnit, this.app);
	    			this.htmlParser.run();
	    			if(! this.htmlParser.isStop()) {
	    				this.app.progress ++;
	    				this.workUnit.web.prices.addAll(this.htmlParser.getPrices());
	    				cleanWorkQueue();
	    			} else {
	    				this.workUnit.lastResponseTime = null;
	    			}
				} catch (Exception e) {
					e.printStackTrace();
    				this.workUnit.lastResponseTime = null;
					//throw new RuntimeException(e);
				} 
			}
		}
	}
	
	private void cleanWorkQueue() {
		synchronized(this.app.workQueue) {
			List<WorkUnit> delete = new ArrayList<WorkUnit>();
			this.workUnit.finished = true;
			for(WorkUnit iWorkUnit : this.app.workQueue) {
				if(iWorkUnit.miniEquals(this.workUnit)) {
					delete.add(iWorkUnit);
				}
			}
			this.app.workQueue.removeAll(delete);
		}
	}
	
	private long secondsSince(Date date) {
		return (new Date().getTime() - date.getTime()) / 1000;
	}
	
	private void getWorkUnit() {
		while(! this.app.workQueue.isEmpty()) {
			synchronized(this.app.workQueue) {
				if(! this.app.workQueue.isEmpty()) {
					for(WorkUnit iWorkUnit : this.app.workQueue) {
						if(iWorkUnit.lastResponseTime == null || 
								secondsSince(iWorkUnit.lastResponseTime) > App.RESTART_AFTER_SECONDS) {
							this.workUnit = this.app.workQueue.get(0);
							this.app.workQueue.remove(0);
							this.workUnit.lastResponseTime = new Date();
							this.workUnit.trials ++;
							if(this.workUnit.trials < 10) {
								this.app.workQueue.add(this.workUnit);
							}
							return;
						}
					}
				}
			}
			App.sleep(1000);
		}
	}
}