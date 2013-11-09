package com.jakubadamek.robotemil.services;

import java.io.IOException;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.jakubadamek.robotemil.Prices;
import com.jakubadamek.robotemil.WorkUnitKey;

@Repository
public class JdbcPriceService implements PriceService {
    private final Logger logger = Logger.getLogger(getClass());
    private String TABLE_PRICES = "Prices9";

	private static final String PRICES_COLUMNS = "Web, DaysBefore, Date, Prices, LengthOfStay";

	private SimpleJdbcTemplate jdbcTemplate;

	@Required
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	/* Read-only because of the method name starting with "read" */
	@Transactional
	@Override
	public int readPrices(final String web, final Prices prices, final WorkUnitKey key) {
		String pricesString;
		try {
			pricesString = jdbcTemplate.queryForObject(
					"SELECT Prices FROM " + TABLE_PRICES + " " +
					"WHERE Date=? AND Web=? AND DaysBefore=? AND LengthOfStay=? ",
					String.class,
					new java.sql.Date(key.getDate().getTime()), 
					web, 
					daysBefore(key.getDate(), new Date()), 
					key.getLengthOfStay());
		} catch(EmptyResultDataAccessException e) {
			return 0;
		}
		try {
			return PricesMarshaller.unmarshal(IOUtils.toInputStream(pricesString), prices, key);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Transactional
	@Override
	public void createTables() {
		jdbcTemplate
				.update("CREATE TABLE IF NOT EXISTS " + TABLE_PRICES + "(" +
						"	Web VARCHAR(255), " +
						"	DaysBefore INTEGER, " +
						"	Date DATE, " +
						"	Prices CLOB, " +
						"   LengthOfStay INTEGER)");
	}

	@Transactional
	@Override
	public void persistPrices(String web, Prices prices, WorkUnitKey key) {
		deleteRefreshedData(web, key);		
		try {
			jdbcTemplate.update("INSERT INTO " + TABLE_PRICES + "(" + PRICES_COLUMNS
					+ ") VALUES(?, ?, ?, ?, ?)", 
					web, 
					daysBefore(key.getDate(), new Date()), 
					new java.sql.Date(key.getDate().getTime()),
					PricesMarshaller.marshal(prices, key),
					key.getLengthOfStay());
		} catch (DataAccessException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Transactional
	@Override
	public void deleteRefreshedData(String web, WorkUnitKey key) {
		int deleted = jdbcTemplate.update(
				"DELETE FROM " + TABLE_PRICES + " WHERE Date=? AND Web=? AND DaysBefore=? AND LengthOfStay=?",
				key.getDate(), web, daysBefore(key.getDate(), new Date()), key.getLengthOfStay());
		logger.info("Deleted " + deleted + " rows");
	}

	private int daysBefore(Date date1, Date date2) {
		return (int) Math.round((date1.getTime() - date2.getTime())
				/ (24.0 * 60 * 60 * 1000));
	}

}
