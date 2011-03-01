package com.jakubadamek.robotemil.services;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
public class JdbcSettingsService implements SettingsService {

	private SimpleJdbcTemplate jdbcTemplate;

	@Required
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	private void createSettingsTable() {
		try {
			jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Settings",
					Integer.class);
		} catch (Exception e) {
			jdbcTemplate
					.update("CREATE TABLE Settings(Key VARCHAR(255), Value VARCHAR(255), PRIMARY KEY (Key))");
		}
	}

	@Override
	public void storeSetting(String key, String value) {
		createSettingsTable();
		jdbcTemplate.update("DELETE FROM Settings WHERE Key=?", key);
		jdbcTemplate.update("INSERT INTO Settings(Key, Value) VALUES(?, ?)",
				key, value);
	}

	@Override
	public String readSetting(String key, String defaultValue) {
		createSettingsTable();
		try {
			return jdbcTemplate.queryForObject(
					"SELECT Value FROM Settings WHERE Key = ?", String.class,
					key);
		} catch (EmptyResultDataAccessException empty) {
			storeSetting(key, defaultValue);
			return defaultValue;
		}
	}
}
