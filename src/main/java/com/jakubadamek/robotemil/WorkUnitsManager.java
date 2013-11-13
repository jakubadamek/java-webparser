package com.jakubadamek.robotemil;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.jakubadamek.robotemil.services.util.IWebToPrices;

public class WorkUnitsManager {
	/** max trials for one WorkUnit */
	private static final int MAX_TRIALS = 5;
	private static final int MIN_ROWS_CACHE = 25;
    private final Logger logger = Logger.getLogger(getClass());
	
	final App app;
	
	private final List<WorkUnit> workUnits = new CopyOnWriteArrayList<WorkUnit>();
    // @GuardedBy this
	private Thread checker;
	private CountDownLatch workUnitsLatch;
	private volatile Thread latchThread;

	private ExecutorService threadPool;
	private Set<DateLosWeb> dateLosWebs;

	public WorkUnitsManager(App app) {
		super();
		this.app = app;
	}

	public void add(WorkUnit workUnit) {
		workUnits.add(workUnit);
	}

	public int unfinishedCount() {		
		return workUnitsLatch == null ? 0 : (int) workUnitsLatch.getCount();
	}

	public synchronized void checkPeriodically() {
		checker = new Thread("WorkUnitsChecker") {
			@SuppressWarnings("synthetic-access")
            @Override
			public void run() {
				try {
					while(! isInterrupted()) {
						check();
						Thread.sleep(1000);
					}
				} catch(InterruptedException e) {
					logger.info("Checker was interrupted - probably the app is shutting down.");
				}
			}
		};
		checker.start();
	}
	
	private long secondsSince(Date date) {
		return (new Date().getTime() - date.getTime()) / 1000;
	}

	public void check() {
		for(WorkUnit workUnit : workUnits) {
			if(! workUnit.finished) {
				if(workUnit.restartNow) {
					submit(workUnit);
				} else {
					Date response = workUnit.lastResponseTime;
					if(response != null && secondsSince(response) > App.RESTART_AFTER_SECONDS) {
						submit(workUnit);
					}
				}
			}
		}
	}

	public synchronized void interruptChecker() {
		if(checker != null) {
			checker.interrupt();
		}		
	}

	public CountDownLatch getLatch() {
		return this.workUnitsLatch;
	}
	
	public void prepare() {
        dateLosWebs = new HashSet<DateLosWeb>();

		OurHotel ourHotel = app.getOurHotel();
		for(Integer lengthOfStay : app.getLengthsOfStay()) {
	        for(Date date : app.getDates()) {
	        	for(WebStruct webHotels : ourHotel.getEnabledWebStructs()) {
	        		dateLosWebs.add(new DateLosWeb(date, lengthOfStay, webHotels.getParams().getExcelName()));
	        	}
	        }
		}
		
		lookup();
		workUnitsLatch = new CountDownLatch(dateLosWebs.size());		
	}
	
	public void downloadAll(int threadCount) throws InterruptedException {
        threadPool = Executors.newFixedThreadPool(threadCount);
		
		for(DateLosWeb dateLosWeb : dateLosWebs) {
			WorkUnit workUnit = new WorkUnit(dateLosWeb);
			add(workUnit);
			submit(workUnit);
		}
		
        checkPeriodically();
        latchThread = Thread.currentThread();
        getLatch().await();
        logger.info("latch entered");
        threadPool.shutdown();
		interruptChecker();		
	}

	private void lookup() {
		if(this.app.isUseCache()) {
			IWebToPrices iWebToPrices = new IWebToPrices() {				
				@Override
				public Prices get(String webExcelName) {
					return app.webStruct(webExcelName).getPrices();
				}
			};

			app.showLog("Start, hleda se v cache");

			int count = dateLosWebs.size();
			int found = 0;
			try {
				found = app.jdbcPriceService.lookup(dateLosWebs, iWebToPrices, MIN_ROWS_CACHE);
			} catch(Exception e) {
				logger.error("Cache", e);
			}
			app.showLog("Nalezeno v cache " + found + " z " + count);
			
			count = dateLosWebs.size();
			found = 0;
			try {
				found = app.httpPriceService.lookup(dateLosWebs, iWebToPrices, MIN_ROWS_CACHE);
			} catch(Exception e) {
				logger.error("Http", e);
			}
			app.showLog("Nalezeno na serveru " + found + " z " + count);
		}
	}

	public void submit(WorkUnit workUnit) {
		if(workUnit.trials.addAndGet(1) < MAX_TRIALS) {
			DownloadTask task = new DownloadTask(workUnit, app);
			workUnit.lastResponseTime = new Date();
			threadPool.submit(task);
			logger.info("Submitting task " + workUnit);
		} else {
			workUnit.finished = true;
            logger.info("Latch count down - max trials reached " + MAX_TRIALS);			
			getLatch().countDown();
		}
	}
	
	public void shutdown() {
	    if(threadPool != null) {
	        threadPool.shutdownNow();
	    }
	    if(latchThread != null) {
	    	latchThread.interrupt();
	    }
	    interruptChecker();
	}
}

