package com.jakubadamek.robotemil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkUnitsManager {
	/** max trials for one WorkUnit */
	private static final int MAX_TRIALS = 5;
	
	private final App app;
	
	private final List<WorkUnit> workUnits = new ArrayList<WorkUnit>();
	private Thread checker;
	private CountDownLatch workUnitsLatch;

	private ExecutorService threadPool;

	public WorkUnitsManager(App app) {
		super();
		this.app = app;
	}

	public synchronized void add(WorkUnit workUnit) {
		workUnits.add(workUnit);
	}

	public synchronized int unfinishedCount() {
		return (int) workUnitsLatch.getCount();
	}

	public synchronized void checkPeriodically() {
		checker = new Thread("WorkUnitsChecker") {
			@Override
			public void run() {
				try {
					while(! isInterrupted()) {
						check();
						Thread.sleep(1000);
					}
				} catch(InterruptedException e) {
					// interrupted - probably the application is shutting down
				}
			}
		};
		checker.start();
	}
	
	private long secondsSince(Date date) {
		return (new Date().getTime() - date.getTime()) / 1000;
	}

	public synchronized void check() {
		for(WorkUnit workUnit : workUnits) {
			if(! workUnit.finished) {
				Date response = workUnit.lastResponseTime;
				if(response != null && secondsSince(response) > App.RESTART_AFTER_SECONDS) {
					submit(workUnit);
				}
			}
		}
	}

	public synchronized void interrupt() {
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
        System.out.println("latch entered");
        threadPool.shutdown();
		interrupt();		
	}

	public void submit(WorkUnit workUnit) {
		if(workUnit.trials.addAndGet(1) < MAX_TRIALS) {
			DownloadTask task = new DownloadTask(workUnit, app); 
			threadPool.submit(task);
		} else {
			workUnit.finished = true;
			getLatch().countDown();
		}
	}
}

