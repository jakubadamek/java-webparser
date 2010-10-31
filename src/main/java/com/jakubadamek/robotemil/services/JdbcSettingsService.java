package com.jakubadamek.robotemil.services;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
public class JdbcSettingsService implements SettingsService {

    private SimpleJdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
	this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
    }

    private void createSettingsTable() {
	jdbcTemplate.update("CREATE TABLE IF NOT EXISTS Settings(Key, Value, PRIMARY KEY (Key))");
    }

    public void storeSetting(String key, String value) {
	createSettingsTable();
	jdbcTemplate.update("INSERT OR REPLACE INTO Settings(Key, Value) VALUES(?, ?)", key, value);
    }

    public String readSetting(String key, String defaultValue) {
	createSettingsTable();
	String retval = jdbcTemplate.queryForObject("SELECT Value FROM Settings WHERE Key = ?", String.class, key);
	if (retval == null) {
	    storeSetting(key, defaultValue);
	    return defaultValue;
	}
	return retval;
    }
}
