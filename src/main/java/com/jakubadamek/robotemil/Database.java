package com.jakubadamek.robotemil;

import java.sql.Connection;
import java.sql.DriverManager;

public class Database {

	public static Connection getConnection() {
		Connection connection;
        try {
            Class.forName("SQLite.JDBCDriver");
            connection = DriverManager.getConnection("jdbc:sqlite:/prices.sqlite", "", "");
            connection.setAutoCommit(false);
            return connection;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}
}
