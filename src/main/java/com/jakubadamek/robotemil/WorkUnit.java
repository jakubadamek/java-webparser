package com.jakubadamek.robotemil;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;


/** thread work unit */
public class WorkUnit {
	public int maxPages;
	public DateLosWeb key;
	/** last response time */
	public volatile Date lastResponseTime;
	/** restart immediately */
	public volatile boolean restartNow = false;
	/** work finished */
	public volatile boolean finished = false;
	/** trial count */
	public AtomicInteger trials = new AtomicInteger(0);
	
	public WorkUnit(DateLosWeb key) {
		super();
		this.key = key;
	}

	/**
	 * Compares two work units
	 * @param other
	 * @return true if equal
	 */
	public boolean miniEquals(WorkUnit other) {
		return this.key.equals(other.key);
	}

    @Override
    public String toString() {
        return "WorkUnit [date=" + key.getDate() + ", lengthOfStay=" + key.getLengthOfStay() + ", lastResponseTime=" + lastResponseTime + ", finished=" + finished
                + ", trials=" + trials + "]";
    }	
}