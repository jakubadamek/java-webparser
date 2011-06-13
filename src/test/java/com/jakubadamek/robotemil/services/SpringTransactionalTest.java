package com.jakubadamek.robotemil.services;

import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

public abstract class SpringTransactionalTest extends AbstractTransactionalDataSourceSpringContextTests {
	@Override
	protected String[] getConfigLocations() {
		return new String[] { "/testhotel/test-config.xml" };
	}
}
