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
 * Spring bean factory that creates an API instance. Usage:
 * <pre>
 * &lt;bean id="api" class="com.rest4j.spring.APIFactoryBean">
 *   &lt;!-- Classpath or filename of the API XML description -->
 *   &lt;property name="apiDescriptionXml" value="com/pet/api/petapi.xml"/>
 *   &lt;!-- All queries will be prefixed with this string; prefix is removed before matching
 *   a path with endpoint roots described in the API XML -->
 *   &lt;property name="pathPrefix" value="/apiv2"/>
 *   &lt;!-- Optional Object Factories for JSON unmarshalling -->
 *   &lt;property name="objectFactories">
 *     &lt;list>
 *       &lt;bean class="com.pet.api.PetObjectFactory"/>
 *     &lt;/list>
 *   &lt;/property>
 *   &lt;!-- This suffix is added to the service names described in the API XML
 *   before lookup in the Spring context. E.g. service="pets" becomes "petsService" -->
 *   &lt;property name="serviceSuffix" value="Service">&lt;/property>
 *   &lt;!-- This suffix will be added to field-mapper names descibed in the API XML
 *   before lookup in the Spring context -->
 *   &lt;property name="mapperSuffix" value="Mapper">&lt;/property>
 * &lt;/bean>
 * </pre>
 *
 * The created bean can then be assigned to a Controller.api property or injected in your code.
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

	/**
	 * Gets classpath to API XML description.
	 */
	public String getApiDescriptionXml() {
		return apiDescriptionXml;
	}

	/**
	 * Sets classpath to API XML description.
	 */
	public void setApiDescriptionXml(String apiDescriptionXml) {
		this.apiDescriptionXml = apiDescriptionXml;
	}

	/**
	 * Gets request prefix added to endpoint/route. Default is no prefix.
	 */
	public String getPathPrefix() {
		return pathPrefix;
	}

	/**
	 * Sets request prefix added to endpoint/route. Default is no prefix.
	 */
	public void setPathPrefix(String pathPrefix) {
		this.pathPrefix = pathPrefix;
	}

	/**
	 * Gets service provider. Default is {@link ContextServiceProvider}.
	 */
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

	/**
	 * Sets service provider. Default is {@link ContextServiceProvider}.
	 */
	public void setServiceProvider(ServiceProvider serviceProvider) {
		this.serviceProvider = serviceProvider;
	}

	/**
	 * Gets custom Object Factories.
	 * @see ObjectFactory
	 */
	public List<ObjectFactory> getObjectFactories() {
		return objectFactories;
	}

	/**
	 * Sets custom Object Factories.
	 * @see ObjectFactory
	 */
	public void setObjectFactories(List<ObjectFactory> objectFactories) {
		this.objectFactories = objectFactories;
	}

	/**
	 * Gets custom Field Filters.
	 * @see FieldFilter
	 */
	public List<FieldFilter> getFieldFilters() {
		return fieldFilters;
	}

	/**
	 * Sets custom Field Filters.
	 * @see FieldFilter
	 */
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
			ApiFactory fac = new ApiFactory(url, getPathPrefix(), getServiceProvider());
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

	/**
	 * Suffix that is added to the service/@name to obtain the Spring bean id. Default is no suffix.
	 */
	public String getServiceSuffix() {
		return serviceSuffix;
	}

	/**
	 * Suffix that is added to the service/@name to obtain the Spring bean id. Default is no suffix.
	 */
	public void setServiceSuffix(String serviceSuffix) {
		this.serviceSuffix = serviceSuffix;
	}

	/**
	 * Suffix that is added to the model/@field-mapper to obtain the Spring bean id. Default is no suffix.
	 */
	public String getMapperSuffix() {
		return mapperSuffix;
	}

	/**
	 * Suffix that is added to the model/@field-mapper to obtain the Spring bean id. Default is no suffix.
	 */
	public void setMapperSuffix(String mapperSuffix) {
		this.mapperSuffix = mapperSuffix;
	}

	/**
	 * Suffix that is added to the (simple|complex)/@converter to obtain the Spring bean id. Default is no suffix.
	 */
	public String getConverterSuffix() {
		return converterSuffix;
	}

	/**
	 * Suffix that is added to the (simple|complex)/@converter to obtain the Spring bean id. Default is no suffix.
	 */
	public void setConverterSuffix(String converterSuffix) {
		this.converterSuffix = converterSuffix;
	}

	/**
	 * Classpath of an XML schema for custom tags in the &lt;extra> tags in API definition XML.
	 */
	public String getExtSchema() {
		return extSchema;
	}

	/**
	 * Classpath of an XML schema for custom tags in the &lt;extra> tags in API definition XML.
	 */
	public void setExtSchema(String extSchema) {
		this.extSchema = extSchema;
	}

	/**
	 * JAXB object factory for custom tags in the &lt;extra> tags in API definition XML.
	 */
	public String getExtObjectFactory() {
		return extObjectFactory;
	}

	/**
	 * JAXB object factory for custom tags in the &lt;extra> tags in API definition XML.
	 */
	public void setExtObjectFactory(String extObjectFactory) {
		this.extObjectFactory = extObjectFactory;
	}
}
