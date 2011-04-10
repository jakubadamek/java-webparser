package com.jakubadamek.robotemil.services;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.jakubadamek.robotemil.Prices;
import com.jakubadamek.robotemil.entities.PriceAndOrder;

@Repository
public class JdbcPriceService implements PriceService {
    private final Logger logger = Logger.getLogger(getClass());

	private static final String PRICES_COLUMNS = "Web, Hotel, QueryDate, DaysBefore, Date, Price, HotelOrder";

	private SimpleJdbcTemplate jdbcTemplate;

	@Required
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	/* Read-only because of the method name starting with "read" */
	@Transactional
	@Override
	public int readPrices(final String web, final Prices prices, final Date date) {
		ParameterizedRowMapper<Object> rowMapper = new ParameterizedRowMapper<Object>() {
			@Override
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				String hotel = rs.getString("Hotel");
				Double price = rs.getDouble("Price");
				int order = rs.getInt("HotelOrder");
				prices.addPrice(hotel, date, price, order);
				return null;
			}
		};
		int rows = jdbcTemplate.query(
				"SELECT * FROM Prices WHERE Date=? AND Web=? AND DaysBefore=?",
				rowMapper, new java.sql.Date(date.getTime()), web, daysBefore(date, new Date())).size();
		return rows;
	}

	@Transactional
	@Override
	public void createTables() {
		jdbcTemplate
				.update("CREATE TABLE IF NOT EXISTS Prices(" +
						"	Web VARCHAR(255), " +
						"	Hotel VARCHAR(255), " +
						"	QueryDate DATE, " +
						"	DaysBefore INTEGER, " +
						"	Date DATE, " +
						"	Price DECIMAL(10), " +
						"	HotelOrder INTEGER)");
	}

	@Transactional
	@Override
	public void persistPrices(String web, Prices prices, Date date) {
		deleteRefreshedData(web, date);
		for (String hotel : prices.getData().keySet()) {
			PriceAndOrder priceAndOrder = prices.getData().get(hotel).get(date);
			if(priceAndOrder != null) {
    			jdbcTemplate.update("INSERT INTO Prices(" + PRICES_COLUMNS
    					+ ") VALUES(?, ?, ?, ?, ?, ?, ?)", 
    					web, hotel, new Date(),
    					daysBefore(date, new Date()), 
    					new java.sql.Date(date.getTime()), 
    					priceAndOrder.price,
    					priceAndOrder.order);
			}
		}
	}

	private void deleteRefreshedData(String web, Date date) {
		int deleted = jdbcTemplate.update(
				"DELETE FROM Prices WHERE Date=? AND Web=? AND DaysBefore=?",
				date, web, daysBefore(date, new Date()));
		logger.info("Deleted " + deleted + " rows");
	}

	private int daysBefore(Date date1, Date date2) {
		return (int) Math.round((date1.getTime() - date2.getTime())
				/ (24.0 * 60 * 60 * 1000));
	}

}
