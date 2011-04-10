package com.jakubadamek.robotemil.services;

import javax.sql.DataSource;

import jxl.common.Logger;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.jakubadamek.robotemil.OurHotel;
import com.jakubadamek.robotemil.WebStruct;

@Repository
public class JdbcSettingsService implements SettingsService {

    private static final Logger logger = Logger.getLogger(JdbcSettingsService.class);
	private static final String WEB_ENABLED = ".enabled";
    private SimpleJdbcTemplate jdbcTemplate;

	@Required
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	@Transactional
	@Override
	public void createTables() {
		jdbcTemplate.update(
				"CREATE TABLE IF NOT EXISTS Settings" +
				"	(Key VARCHAR(255), " +
				"	Value VARCHAR(255), " +
				"	PRIMARY KEY (Key))");
	}

	@Transactional
	@Override
	public void storeSetting(String key, String value) {
		jdbcTemplate.update("MERGE INTO Settings(Key, Value) KEY(Key) VALUES(?, ?)",
				key, value);
	}

	/* Read-only because of the method name starting with "read" */
	@Transactional
	@Override
	public String readSetting(String key, String defaultValue) {
		try {
			return jdbcTemplate.queryForObject(
					"SELECT Value FROM Settings WHERE Key = ?", String.class,
					key);
		} catch (EmptyResultDataAccessException empty) {
			//storeSetting(key, defaultValue);
			return defaultValue;
		}
	}

	/* Read-only because of the method name starting with "read" */
	@Transactional
    @Override
    public void readEnabledWebs(OurHotel ourHotel) {
        for(WebStruct webStruct : ourHotel.getWebStructs()) {
            String enabled = readSetting(webStruct.getParams().getExcelName() + WEB_ENABLED, "true");
            webStruct.getParams().setEnabled(Boolean.parseBoolean(enabled));
        }
    }

	@Transactional
    @Override
    public void storeEnabledWebs(OurHotel ourHotel) {
        for(WebStruct webStruct : ourHotel.getWebStructs()) {
            String enabled = "" + webStruct.getParams().getEnabled(); 
            storeSetting(webStruct.getParams().getExcelName() + WEB_ENABLED, enabled);
            logger.info("storeEnabledWebs " + webStruct.getParams().getExcelName() + WEB_ENABLED + " " + enabled);
        }        
        logger.info("storeEnabledWebs finished");
    }
}
