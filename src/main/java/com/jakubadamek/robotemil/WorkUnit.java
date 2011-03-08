package com.jakubadamek.robotemil;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;


/** thread work unit */
public class WorkUnit {
	/** date */
	public final Date date;
	/** web */
	public final WebStruct web;
	/** last response time */
	public volatile Date lastResponseTime;
	/** work finished */
	public volatile boolean finished = false;
	/** trial count */
	public AtomicInteger trials = new AtomicInteger(0);
	
	public WorkUnit(Date date, WebStruct web) {
		super();
		this.date = date;
		this.web = web;
	}

	/**
	 * Compares two work units
	 * @param other
	 * @return true if equal
	 */
	public boolean miniEquals(WorkUnit other) {
		return this.date.equals(other.date) && this.web == other.web;
	}

    @Override
    public String toString() {
        return "WorkUnit [date=" + date + ", web=" + web + ", lastResponseTime=" + lastResponseTime + ", finished=" + finished
                + ", trials=" + trials + "]";
    }	
}