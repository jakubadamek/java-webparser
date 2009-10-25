package com.jakubadamek.robotemil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Database {

	/** Test */
	public static void main(String ... args) {
		String testKey = "testKey";
		String testValue = "testValue";
		storeSetting(testKey, testValue);
		if(! readSetting(testKey, "aha").equals(testValue)) {
			throw new AssertionError();
		}
		if(! readSetting("chi", "aha").equals("aha")) {
			throw new AssertionError();
		}
	}

	public static Connection getConnection() {
		Connection connection;
        try {
            Class.forName("SQLite.JDBCDriver");
            String filename = App.netxDir().getPath().replace("\\","/") + "/prices.sqlite";
            System.out.println("Opening database " + filename);
            connection = DriverManager.getConnection("jdbc:sqlite:/" + filename, "", "");
            connection.setAutoCommit(false);
            return connection;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}

	private static void createSettingsTable(Connection connection) throws SQLException {
        PreparedStatement stat = connection.prepareStatement(
            	"CREATE TABLE IF NOT EXISTS Settings(Key, Value, PRIMARY KEY (Key))");
    	stat.executeUpdate();
    	stat.close();
	}

	public static void storeSetting(String key, String value) {
		Connection connection = Database.getConnection();
		try {
	        try {
		        PreparedStatement stat = connection.prepareStatement(
		        	"INSERT OR REPLACE INTO Settings(Key, Value) VALUES(?, ?)");
		        try {
		        	createSettingsTable(connection);
	        		int icol = 1;
	        		stat.setString(icol ++, key);
	        		stat.setString(icol ++, value);
			        stat.executeUpdate();
		        } finally {
		        	stat.close();
		        }
		        connection.commit();
	        } finally {
	        	connection.close();
	        }
	    } catch(SQLException e) {
	    	throw new RuntimeException(e);
	    }
	}

	public static String readSetting(String key, String defaultValue) {
		Connection connection = Database.getConnection();
		try {
	        try {
	        	createSettingsTable(connection);
		        PreparedStatement stat = connection.prepareStatement(
	            	"SELECT Value FROM Settings WHERE Key = ?");
		        try {
	        		int icol = 1;
	        		stat.setString(icol ++, key);
			        ResultSet resultSet = stat.executeQuery();
			        while(resultSet.next()) {
			        	return resultSet.getString("Value");
			        }
		        } finally {
		        	stat.close();
		        }
	        } finally {
	        	connection.close();
	        }
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
		storeSetting(key, defaultValue);
        return defaultValue;
	}
}
