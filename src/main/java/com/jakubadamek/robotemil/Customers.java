package com.jakubadamek.robotemil;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

public class Customers implements InitializingBean {
	private Map<String, SettingsModel> customers = new HashMap<String, SettingsModel>();
	private SettingsModel customer;
	private App app;
	
	public SettingsModel getSettingsModel() {
		return customer;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		customer = customers.get(app.getCustomer().toString());
	}	

	@Required
	public void setApp(App app) {
		this.app = app;
	}

	@Required
	public void setCustomers(Map<String, SettingsModel> customers) {
		this.customers = customers;
	}	
}
