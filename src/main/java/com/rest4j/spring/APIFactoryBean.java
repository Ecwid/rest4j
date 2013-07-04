/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rest4j.spring;

import com.rest4j.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a class for using Rest4j in Spring context. Usage:
 *
 * &ltbean>
 *
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class APIFactoryBean implements FactoryBean<API>, ApplicationContextAware {
	String apiDescriptionXml;
	String pathPrefix;
	ServiceProvider serviceProvider;
	List<ObjectFactory> objectFactories = new ArrayList<ObjectFactory>();
	List<FieldFilter> fieldFilters = new ArrayList<FieldFilter>();
	String serviceSuffix;
	String mapperSuffix;
	String converterSuffix;
	String extSchema;
	String extObjectFactory;

	API api;
	private ApplicationContext context;

	public String getApiDescriptionXml() {
		return apiDescriptionXml;
	}

	public void setApiDescriptionXml(String apiDescriptionXml) {
		this.apiDescriptionXml = apiDescriptionXml;
	}

	public String getPathPrefix() {
		return pathPrefix;
	}

	public void setPathPrefix(String pathPrefix) {
		this.pathPrefix = pathPrefix;
	}

	public ServiceProvider getServiceProvider() {
		if (serviceProvider == null) {
			ContextServiceProvider ssp = new ContextServiceProvider();
			ssp.setApplicationContext(context);
			ssp.setServiceSuffix(getServiceSuffix());
			ssp.setMapperSuffix(getMapperSuffix());
			ssp.setConverterSuffix(getConverterSuffix());
			return ssp;
		}
		return serviceProvider;
	}

	public void setServiceProvider(ServiceProvider serviceProvider) {
		this.serviceProvider = serviceProvider;
	}

	public List<ObjectFactory> getObjectFactories() {
		return objectFactories;
	}

	public void setObjectFactories(List<ObjectFactory> objectFactories) {
		this.objectFactories = objectFactories;
	}

	public List<FieldFilter> getFieldFilters() {
		return fieldFilters;
	}

	public void setFieldFilters(List<FieldFilter> fieldFilters) {
		this.fieldFilters = fieldFilters;
	}

	@Override
	public synchronized API getObject() throws ConfigurationException, ClassNotFoundException {
		if (api == null) {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			URL url = classLoader.getResource(apiDescriptionXml);
			if (url == null) {
				url = getClass().getClassLoader().getResource(apiDescriptionXml);
				if (url == null) {
					throw new ConfigurationException("Cannot find "+apiDescriptionXml+" in the classpath");
				}
			}
			APIFactory fac = new APIFactory(url, getPathPrefix(), getServiceProvider());
			Class extObjectFactoryClass = null;
			if (extObjectFactory != null) {
				extObjectFactoryClass = classLoader.loadClass(extObjectFactory);
			}
			if (extSchema != null) {
				fac.setExtSchema(extSchema, extObjectFactoryClass);
			}
			for (ObjectFactory of: getObjectFactories()) {
				fac.addObjectFactory(of);
			}
			for (FieldFilter ff: getFieldFilters()) {
				fac.addFieldFilter(ff);
			}
			api = fac.createAPI();
		}
		return api;
	}

	@Override
	public Class<?> getObjectType() {
		return API.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}

	public String getServiceSuffix() {
		return serviceSuffix;
	}

	public void setServiceSuffix(String serviceSuffix) {
		this.serviceSuffix = serviceSuffix;
	}

	public String getMapperSuffix() {
		return mapperSuffix;
	}

	public void setMapperSuffix(String mapperSuffix) {
		this.mapperSuffix = mapperSuffix;
	}

	public String getConverterSuffix() {
		return converterSuffix;
	}

	public void setConverterSuffix(String converterSuffix) {
		this.converterSuffix = converterSuffix;
	}

	public String getExtSchema() {
		return extSchema;
	}

	public void setExtSchema(String extSchema) {
		this.extSchema = extSchema;
	}

	public String getExtObjectFactory() {
		return extObjectFactory;
	}

	public void setExtObjectFactory(String extObjectFactory) {
		this.extObjectFactory = extObjectFactory;
	}
}
