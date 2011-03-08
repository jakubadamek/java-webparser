package com.jakubadamek.robotemil.services;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.jakubadamek.robotemil.Prices;
import com.jakubadamek.robotemil.entities.PriceAndOrder;

@Repository
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
public class JdbcPriceService implements PriceService {
    private final Logger logger = Logger.getLogger(getClass());

	private static final String PRICES_COLUMNS = "Web, Hotel, Today, DaysBefore, Date, Price, HotelOrder";

	private SimpleJdbcTemplate jdbcTemplate;

	@Required
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	@Override
	public int readPrices(final String web, final Prices prices, final Date date) {
		createTablePrices();
		RowMapper<Object> rowMapper = new RowMapper<Object>() {
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
				rowMapper, date, web, daysBefore(date, new Date())).size();
		return rows;
	}

	private void createTablePrices() {
		try {
			jdbcTemplate
					.update("CREATE TABLE Prices(Web VARCHAR(255), Hotel VARCHAR(255), Today DATE, DaysBefore INTEGER, "
							+ "Date DATE, Price DECIMAL(10), HotelOrder INTEGER)");
		} catch (BadSqlGrammarException e) {
			if (e.getCause() == null
					|| !e.getCause().getMessage()
							.contains("Table already exists")) {
				throw e;
			}
		}
	}

	@Override
	public void persistPrices(String web, Prices prices, Date date) {
		createTablePrices();
		deleteRefreshedData(web, date);
		List<Object[]> rows = new ArrayList<Object[]>();
		for (String hotel : prices.getData().keySet()) {
			PriceAndOrder priceAndOrder = prices.getData().get(hotel).get(date);
			if(priceAndOrder != null) {
    			rows.add(new Object[] { web, hotel, new Date(),
    					daysBefore(date, new Date()), date, priceAndOrder.price,
    					priceAndOrder.order });
			}
		}
		jdbcTemplate.batchUpdate("INSERT INTO Prices(" + PRICES_COLUMNS
				+ ") VALUES(?, ?, ?, ?, ?, ?, ?)", rows);
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
