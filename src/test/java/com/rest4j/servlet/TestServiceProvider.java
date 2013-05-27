package com.rest4j.servlet;

import com.rest4j.Converter;
import com.rest4j.ServiceProvider;
import com.rest4j.spring.TestService;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class TestServiceProvider implements ServiceProvider {
	@Override
	public Object lookupService(String name) {
		if ("pets".equals(name)) return new TestService();
		return null;
	}

	@Override
	public Object lookupFieldMapper(String model, String name) {
		if ("petMapping".equals(name)) return new com.rest4j.impl.petapi.PetMapping();
		return null;
	}

	@Override
	public Converter lookupConverter(String name) {
		return null;
	}
}
