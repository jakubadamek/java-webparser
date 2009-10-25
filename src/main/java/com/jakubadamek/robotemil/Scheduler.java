package com.jakubadamek.robotemil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Schedules task periodically once per day. Allows to run missed tasks.
 * This scheduler may be used together with the built-in Windows scheduler
 * to ensure automatic starting.
 *
 * @author Jakub Adamek
 */
public class Scheduler {
	private int executeUpdate(PreparedStatement stat) throws SQLException {
		try {
			return stat.executeUpdate();
		} finally {
			stat.close();
		}
	}

	public static void main(String... args) {
		System.out.println(new Scheduler().isScheduled("test1"));
		System.out.println(new Scheduler().isScheduled("test2"));
		System.out.println(new Scheduler().isScheduled("test1"));
	}

	/**
	 * @param task
	 * @return true if the task is scheduled now (should be executed)
	 */
	public boolean isScheduled(String task) {
		try {
			return isScheduledBody(task);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean isScheduledBody(String task) throws SQLException {
		Connection connection = Database.getConnection();
		try {
			String columns = "Task, LastRun, NextRun";
	        PreparedStatement stat = connection.prepareStatement(
		        "CREATE TABLE IF NOT EXISTS Scheduler(" + columns + ")");
	        executeUpdate(stat);
	        stat = connection.prepareStatement(
	        	"SELECT NextRun FROM Scheduler WHERE Task=?");
	        stat.setString(1, task);
	        ResultSet resultSet = stat.executeQuery();
	        try {
	        	if(resultSet.next()) {
	        		Timestamp nextRun = resultSet.getTimestamp("NextRun");
	        		if(nextRun.after(new java.util.Date())) {
	        			return false;
	        		}
        			stat = connection.prepareStatement(
        				"DELETE FROM Scheduler WHERE Task=?");
        			stat.setString(1, task);
        	        executeUpdate(stat);
	        	}
	        } finally {
	        	resultSet.close();
	        }
	        stat = connection.prepareStatement(
	        	"INSERT INTO Scheduler (" + columns + ") VALUES (?,?,?)");
	        int icol = 1;
	        stat.setString(icol ++, task);
	        Calendar cal = Calendar.getInstance();
	        cal.setTime(new java.util.Date());
	        stat.setTimestamp(icol ++, new Timestamp(cal.getTime().getTime()));
	        cal.add(Calendar.DATE, 1);
	        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 0, 0, 0);
	        cal.set(Calendar.MILLISECOND, 0);
	        stat.setTimestamp(icol ++, new Timestamp(cal.getTime().getTime()));
	        executeUpdate(stat);
		} finally {
			connection.commit();
			connection.close();
		}
        return true;
	}
}
