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
import com.jakubadamek.robotemil.WorkUnitKey;
import com.jakubadamek.robotemil.entities.PriceAndOrder;

@Repository
public class JdbcPriceService implements PriceService {
    private final Logger logger = Logger.getLogger(getClass());

	private static final String PRICES_COLUMNS = "Web, Hotel, QueryDate, DaysBefore, Date, Price, HotelOrder, LengthOfStay";

	private SimpleJdbcTemplate jdbcTemplate;

	@Required
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	/* Read-only because of the method name starting with "read" */
	@Transactional
	@Override
	public int readPrices(final String web, final Prices prices, final WorkUnitKey key) {
		ParameterizedRowMapper<Object> rowMapper = new ParameterizedRowMapper<Object>() {
			@Override
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				String hotel = rs.getString("Hotel");
				Double price = rs.getDouble("Price");
				int order = rs.getInt("HotelOrder");
				prices.addPrice(hotel, key, price, order);
				return null;
			}
		};
		int rows = jdbcTemplate.query(
				"SELECT * FROM Prices WHERE Date=? AND Web=? AND DaysBefore=? AND LengthOfStay=?",
				rowMapper, new java.sql.Date(key.getDate().getTime()), web, daysBefore(key.getDate(), new Date()), key.getLengthOfStay()).size();
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
						"	HotelOrder INTEGER, " +
						"   LengthOfStay INTEGER)");
	}

	@Transactional
	@Override
	public void persistPrices(String web, Prices prices, WorkUnitKey key) {
		deleteRefreshedData(web, key);
		for (String hotel : prices.getData().keySet()) {
			PriceAndOrder priceAndOrder = prices.getData().get(hotel).get(key);
			if(priceAndOrder != null) {
    			jdbcTemplate.update("INSERT INTO Prices(" + PRICES_COLUMNS
    					+ ") VALUES(?, ?, ?, ?, ?, ?, ?, ?)", 
    					web, hotel, new Date(),
    					daysBefore(key.getDate(), new Date()), 
    					new java.sql.Date(key.getDate().getTime()), 
    					priceAndOrder.price,
    					priceAndOrder.order,
    					key.getLengthOfStay());
			}
		}
	}

	private void deleteRefreshedData(String web, WorkUnitKey key) {
		int deleted = jdbcTemplate.update(
				"DELETE FROM Prices WHERE Date=? AND Web=? AND DaysBefore=? AND LengthOfStay=?",
				key.getDate(), web, daysBefore(key.getDate(), new Date()), key.getLengthOfStay());
		logger.info("Deleted " + deleted + " rows");
	}

	private int daysBefore(Date date1, Date date2) {
		return (int) Math.round((date1.getTime() - date2.getTime())
				/ (24.0 * 60 * 60 * 1000));
	}

}
