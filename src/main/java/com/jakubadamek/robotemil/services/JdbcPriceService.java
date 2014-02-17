package com.jakubadamek.robotemil.services;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.jakubadamek.robotemil.DateLosWeb;
import com.jakubadamek.robotemil.Prices;
import com.jakubadamek.robotemil.services.util.DateUtil;
import com.jakubadamek.robotemil.services.util.IWebToPrices;

@Repository
public class JdbcPriceService implements PriceService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    public static final String TABLE_PRICES = "Prices16";

	private static final String PRICES_COLUMNS = "Web, DaysBefore, Date, Prices, LengthOfStay";

	private SimpleJdbcTemplate jdbcTemplate;

	@Required
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	static class ReadPricesMapper implements ParameterizedRowMapper<Object> {
		String pricesField;
		
		@Override
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			pricesField = rs.getString("Prices");
			return null;
		}
	}

	/* Read-only because of the method name starting with "read" */
	@Transactional
	@Override
	public int readPrices(final Prices prices, final DateLosWeb key) {
		ReadPricesMapper rowMapper = new ReadPricesMapper(); 
		logger.info("readPrices params " + key + " date " + DateUtil.trunc(key.getDate()).getTime());
		jdbcTemplate.query(
				"SELECT Prices FROM " + TABLE_PRICES + " " +
						"WHERE Web=? AND DaysBefore=? AND Date=? AND LengthOfStay=?",
				rowMapper,
				key.getWeb(),
				DateUtil.daysBefore(key.getDate(), new Date()),
				DateUtil.trunc(key.getDate()).getTime(),
				key.getLengthOfStay()).size();
		if(rowMapper.pricesField != null) {
			try {
				return PricesMarshaller.unmarshal(IOUtils.toInputStream(rowMapper.pricesField), prices, key);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
		}
		return 0;
	}

	@Transactional
	@Override
	public void persistPrices(Prices prices, DateLosWeb key) {
		deleteRefreshedData(key);		
		logger.info("persistPrices params " + key + " date " + DateUtil.trunc(key.getDate()).getTime());
		try {
			jdbcTemplate.update("INSERT INTO " + TABLE_PRICES + "(" + PRICES_COLUMNS
					+ ") VALUES(?, ?, ?, ?, ?)", 
					key.getWeb(), 
					DateUtil.daysBefore(key.getDate(), new Date()), 
					DateUtil.trunc(key.getDate()).getTime(),
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
	public void createTables() {
		jdbcTemplate
				.update("CREATE TABLE IF NOT EXISTS " + TABLE_PRICES + "(" +
						"	Web VARCHAR(255), " +
						"	DaysBefore INTEGER, " +
						"	Date BIGINT, " +
						"	Prices CLOB, " +
						"   LengthOfStay INTEGER)");
	}

	@Transactional
	@Override
	public void deleteRefreshedData(DateLosWeb key) {
		int deleted = jdbcTemplate.update(
				"DELETE FROM " + TABLE_PRICES + " WHERE Date=? AND Web=? AND DaysBefore=? AND LengthOfStay=?",
				key.getDate().getTime(), 
				key.getWeb(),
				DateUtil.daysBefore(key.getDate(), new Date()), 
				key.getLengthOfStay());
		if(deleted > 0) {
			logger.info("Deleted " + deleted + " rows");
		}
	}

	public static String lookupSql(List<Date> dates, List<Integer> loses, List<String> webs, Date now) {
		StringBuilder sql = new StringBuilder("SELECT * FROM " + TABLE_PRICES + " WHERE Web IN (");
		for(String web : webs) {
			sql.append("'").append(web).append("',");
		}
		sql.deleteCharAt(sql.length() - 1);
		sql.append(") AND LengthOfStay IN (");
		for(Integer los : loses) {
			sql.append(los).append(",");
		}
		sql.deleteCharAt(sql.length() - 1);
		sql.append(") AND Date IN (");
		for(Date date : dates) {
			sql.append(date.getTime()).append(",");
		}
		sql.deleteCharAt(sql.length() - 1);
		sql.append(") AND DaysBefore=(Date - ").append(now.getTime()).append(") / ").append(DateUtil.MILLIS_PER_DAY);
		return sql.toString();
	}
	
	public int lookup(final Set<DateLosWeb> dateLosWebs, List<Date> dates, List<Integer> loses, List<String> webs, final IWebToPrices webToPrices, final int minRows) {				
		ParameterizedRowMapper<Object> rowMapper = new ParameterizedRowMapper<Object>() {
			@Override
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				String web = rs.getString("Web");
				DateLosWeb key = new DateLosWeb(new Date(rs.getInt("Date")), rs.getInt("LengthOfStay"), web);
				int rows;
				try {
					rows = PricesMarshaller.unmarshal(IOUtils.toInputStream(rs.getString("Prices")), webToPrices.get(web), key);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				if(rows >= minRows) {
					dateLosWebs.remove(key);
				}
				return null;
			}
		};
		
		return jdbcTemplate.query(
				lookupSql(dates, loses, webs, new Date()),
				rowMapper).size(); 
	}
	
	@Override
	public int lookup(Set<DateLosWeb> dateLosWebs, IWebToPrices webToPrices, int minRows) {
		int found = 0;
		for(Iterator<DateLosWeb> i = dateLosWebs.iterator(); i.hasNext(); ) {
			DateLosWeb dateLosWeb = i.next();
			int readFromCache = readPrices(webToPrices.get(dateLosWeb.getWeb()), dateLosWeb);
			if(readFromCache > minRows) {
				found ++;
				i.remove();
			}
		}
		return found;
	}
}
