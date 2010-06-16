package com.jakubadamek.robotemil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jakubadamek.robotemil.htmlparser.HtmlParser;


/** downloads data from one web for one date */
class DownloadThread extends Thread {
	/**
	 *
	 */
	private final App app;
	/**
	 * @param app
	 */
	DownloadThread(App app) {
		this.app = app;
	}
	/** Work unit */
	WorkUnit workUnit;
	/** html parser */
	HtmlParser htmlParser;
	@Override
	public void run() {
		while(! this.app.workQueue.isEmpty() && ! App.stop) {
			getWorkUnit();
			if(this.workUnit != null) {
    			System.out.println("Starting thread for " + this.workUnit.web.parserClass.getSimpleName() + " " +
    					DateFormat.getDateInstance().format(this.workUnit.date));
    			int readFromCache = 0;
				if(this.app.isUseCache()) {
					try {
						readFromCache = readPrices(this.workUnit.web.excelName, this.workUnit.web.prices, this.workUnit.date);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				DateFormat dateFormat = DateFormat.getDateInstance();
				String workUnitDesc = this.workUnit.web.label + " " + dateFormat.format(this.workUnit.date);
				if(readFromCache > 0) {
					this.app.progress ++;
					if(isInWorkQueue()) {
						this.app.showLog("Cache " + workUnitDesc + ": " + readFromCache + " " + this.app.getBundleString("data nalezena v cache"));
					}
    				cleanWorkQueue();
				} else {
					try {
						Date start = new Date();
						this.app.showLog("Start " + workUnitDesc);
						this.htmlParser = this.workUnit.web.parserClass.newInstance();
		    			this.htmlParser.init(this.workUnit, this.app);
		    			this.htmlParser.run();
		    			if(! this.htmlParser.isStop()) {
		    				this.app.progress ++;
		    				this.workUnit.web.prices.addAll(this.htmlParser.getPrices());
		    				persistPrices(this.workUnit.web.excelName, this.htmlParser.getPrices(), this.workUnit.date);
		    				if(isInWorkQueue()) {
								this.app.showLog("Hotovo " + workUnitDesc + ": " + this.htmlParser.getPrices().size()
										+ " hotelu za " + (new Date().getTime() - start.getTime()) / 1000 + " s");
		    				}
		    				cleanWorkQueue();
		    			} else {
							this.app.showLog("Vyprsel casovy limit " + workUnitDesc + ": " + (new Date().getTime() - start.getTime()) / 1000 + " s bez odezvy");
		    				this.workUnit.lastResponseTime = null;
		    			}
					} catch (Exception e) {
						e.printStackTrace();
	    				this.workUnit.lastResponseTime = null;
	    				this.app.showLog("Chyba " + workUnitDesc);
						//throw new RuntimeException(e);
					}
				}
			}
		}
	}

	private static final Object databaseLock = new Object();

	private int readPrices(String web, Prices prices, Date date) throws SQLException {
		synchronized(databaseLock) {
			Connection connection = Database.getConnection();
			createTablePrices(connection);
			int rows = 0;
	        try {
		        PreparedStatement stat = connection.prepareStatement(
		        	"SELECT * FROM Prices WHERE Date=? AND Web=? AND DaysBefore=?");
		        int icol = 1;
		        stat.setDate(icol ++, new java.sql.Date(date.getTime()));
		        stat.setString(icol ++, web);
		        stat.setInt(icol ++, daysBefore(date, new Date()));
		        ResultSet resultSet = stat.executeQuery();
		        while(resultSet.next()) {
		        	String hotel = resultSet.getString("Hotel");
		        	Double price = resultSet.getDouble("Price");
		        	int order = resultSet.getInt("HotelOrder");
		        	prices.addPrice(hotel, date, price, order);
		        	rows ++;
		        }
	        } finally {
	        	connection.close();
	        }
	        return rows;
		}
	}

   	private static final String PRICES_COLUMNS = "Web, Hotel, Today, DaysBefore, Date, Price, HotelOrder";

   	private void createTablePrices(Connection connection) throws SQLException {
        PreparedStatement stat = connection.prepareStatement(
        	"CREATE TABLE IF NOT EXISTS Prices(" + PRICES_COLUMNS + ")");
        try {
        	stat.executeUpdate();
        } finally {
        	stat.close();
        }
   	}

	private void persistPrices(String web, Prices prices, Date date) throws SQLException {
		synchronized(databaseLock) {
			Connection connection = Database.getConnection();
	        try {
	        	createTablePrices(connection);
				deleteRefreshedData(web, date, connection);
				PreparedStatement stat = connection.prepareStatement(
		        	"INSERT INTO Prices(" + PRICES_COLUMNS + ") VALUES(?, ?, ?, ?, ?, ?, ?)");
		        try {
			        for(String hotel : prices.data.keySet()) {
		        		int icol = 1;
		        		stat.setString(icol ++, web);
		        		stat.setString(icol ++, hotel);
		        		stat.setTimestamp(icol ++, new java.sql.Timestamp(new Date().getTime()));
		        		stat.setInt(icol ++, daysBefore(date, new Date()));
		        		stat.setDate(icol ++, new java.sql.Date(date.getTime()));
		        		PriceAndOrder priceAndOrder = prices.data.get(hotel).get(date);
		        		stat.setDouble(icol ++, priceAndOrder.price);
		        		stat.setInt(icol ++, priceAndOrder.order);
		        		stat.executeUpdate();
			        }
		        } finally {
		        	stat.close();
		        }
		        connection.commit();
	        } catch (SQLException e) {
	        	connection.close();
	        	Database.deleteDatabaseFile();
	        } finally {
	        	connection.close();
	        }
		}
	}

	private void deleteRefreshedData(String web, Date date, Connection connection) throws SQLException {
		PreparedStatement stat;
		stat = connection.prepareStatement(
			"DELETE FROM Prices WHERE Date=? AND Web=? AND DaysBefore=?");
		int icol = 1;
		stat.setDate(icol ++, new java.sql.Date(date.getTime()));
		stat.setString(icol ++, web);
		stat.setInt(icol ++, daysBefore(date, new Date()));
		int deleted = stat.executeUpdate();
		System.out.println("Deleted " + deleted + " rows");
	}

	private int daysBefore(Date date1, Date date2) {
		return (int) Math.round((date1.getTime() - date2.getTime()) / (24.0*60*60*1000));
	}

	private boolean isInWorkQueue() {
		synchronized(this.app.workQueue) {
			return this.app.workQueue.contains(this.workUnit);
		}
	}

	private void cleanWorkQueue() {
		synchronized(this.app.workQueue) {
			List<WorkUnit> delete = new ArrayList<WorkUnit>();
			this.workUnit.finished = true;
			for(WorkUnit iWorkUnit : this.app.workQueue) {
				if(iWorkUnit.miniEquals(this.workUnit)) {
					delete.add(iWorkUnit);
					this.app.progress ++;
				}
			}
			this.app.workQueue.removeAll(delete);
		}
	}

	private long secondsSince(Date date) {
		return (new Date().getTime() - date.getTime()) / 1000;
	}

	private void getWorkUnit() {
		while(! this.app.workQueue.isEmpty()) {
			synchronized(this.app.workQueue) {
				if(! this.app.workQueue.isEmpty()) {
					for(WorkUnit iWorkUnit : this.app.workQueue) {
						if(iWorkUnit.lastResponseTime == null ||
								secondsSince(iWorkUnit.lastResponseTime) > App.RESTART_AFTER_SECONDS) {
							this.workUnit = this.app.workQueue.get(0);
							this.app.workQueue.remove(0);
							this.workUnit.lastResponseTime = new Date();
							this.workUnit.trials ++;
							if(this.workUnit.trials < 10) {
								this.app.workQueue.add(this.workUnit);
							}
							return;
						}
					}
				}
			}
			App.sleep(1000);
		}
	}
}