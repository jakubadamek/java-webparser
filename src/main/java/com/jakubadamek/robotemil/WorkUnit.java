package com.jakubadamek.robotemil;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;


/** thread work unit */
public class WorkUnit {
	public int maxPages;
	public WorkUnitKey key;
	/** web */
	public final WebStruct web;
	/** last response time */
	public volatile Date lastResponseTime;
	/** restart immediately */
	public volatile boolean restartNow = false;
	/** work finished */
	public volatile boolean finished = false;
	/** trial count */
	public AtomicInteger trials = new AtomicInteger(0);
	
	public WorkUnit(WorkUnitKey key, WebStruct web) {
		super();
		this.key = key;
		this.web = web;
	}

	/**
	 * Compares two work units
	 * @param other
	 * @return true if equal
	 */
	public boolean miniEquals(WorkUnit other) {
		return this.key.equals(other.key) && this.web == other.web;
	}

    @Override
    public String toString() {
        return "WorkUnit [date=" + key.getDate() + ", lengthOfStay=" + key.getLengthOfStay() + ", web=" + web + ", lastResponseTime=" + lastResponseTime + ", finished=" + finished
                + ", trials=" + trials + "]";
    }	
}