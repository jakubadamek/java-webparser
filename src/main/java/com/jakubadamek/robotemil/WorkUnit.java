package com.jakubadamek.robotemil;

import java.util.Date;


/** thread work unit */
public class WorkUnit {
	/** date */
	public Date date;
	/** web */
	public WebStruct web;
	/** last response time */
	public Date lastResponseTime;
	/** work finished */
	public boolean finished = false;
	/** trial count */
	public int trials = 0;
	
	/**
	 * Compares two work units
	 * @param other
	 * @return true if equal
	 */
	public boolean miniEquals(WorkUnit other) {
		return this.date.equals(other.date) && this.web == other.web;
	}
}