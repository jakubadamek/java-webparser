package com.jakubadamek.robotemil;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

public class WorkUnitsManager {
	/** max trials for one WorkUnit */
	private static final int MAX_TRIALS = 5;
    private final Logger logger = Logger.getLogger(getClass());
	
	private final App app;
	
	private final List<WorkUnit> workUnits = new CopyOnWriteArrayList<WorkUnit>();
    // @GuardedBy this
	private Thread checker;
	private CountDownLatch workUnitsLatch;

	private ExecutorService threadPool;

	public WorkUnitsManager(App app) {
		super();
		this.app = app;
	}

	public void add(WorkUnit workUnit) {
		workUnits.add(workUnit);
	}

	public int unfinishedCount() {
		return (int) workUnitsLatch.getCount();
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
				Date response = workUnit.lastResponseTime;
				if(response != null && secondsSince(response) > App.RESTART_AFTER_SECONDS) {
					submit(workUnit);
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
		workUnitsLatch = new CountDownLatch(app.getDates().size() * app.getOurHotel().getWebStructs().size());		
	}
	
	public void downloadAll(int threadCount) throws InterruptedException {
        threadPool = Executors.newFixedThreadPool(threadCount);

		OurHotel ourHotel = app.getOurHotel();
        for(Date date : app.getDates()) {
        	for(WebStruct webHotels : ourHotel.getWebStructs()) {
        		WorkUnit workUnit = new WorkUnit(date, webHotels);
        		add(workUnit);
        		submit(workUnit);
        	}
        }
        checkPeriodically();
        getLatch().await();
        logger.info("latch entered");
        threadPool.shutdown();
		interruptChecker();		
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
}

