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

package com.rest4j;

import com.rest4j.impl.APIImpl;
import org.springframework.beans.factory.FactoryBean;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.net.URL;
import java.util.List;

/**
 * Receives the API description in XML and creates the corresponding JSON marshaller and unmarshaller.
 *
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class APIFactory implements FactoryBean<API> {
	URL apiDescriptionXml;
	String pathPrefix;
	CustomMapping customMapping;
	ServiceProvider daoProvider;
	private JAXBContext context;
	APIImpl api;
	List<ObjectFactoryChain> factories;

	public APIFactory(URL apiDescriptionXml, String pathPrefix, CustomMapping customMapping, ServiceProvider daoProvider) {
		this(apiDescriptionXml, pathPrefix, customMapping, daoProvider, null);
	}

	public APIFactory(URL apiDescriptionXml, String pathPrefix, CustomMapping customMapping, ServiceProvider daoProvider,
					  List<ObjectFactoryChain> factories) {
		this.apiDescriptionXml = apiDescriptionXml;
		this.pathPrefix = pathPrefix;
		this.customMapping = customMapping;
		this.daoProvider = daoProvider;
		try {
			this.context = JAXBContext.newInstance("com.rest4j.impl.model");
		} catch (JAXBException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public synchronized API getObject() throws ConfigurationException {
		if (api == null) {
			try {
				Schema schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(getClass().getResource("api.xsd"));

				Unmarshaller unmarshaller = context.createUnmarshaller();
				unmarshaller.setSchema(schema);
				JAXBElement<com.rest4j.impl.model.API> element = (JAXBElement<com.rest4j.impl.model.API>) unmarshaller.unmarshal(apiDescriptionXml);
				com.rest4j.impl.model.API root = element.getValue();
				api = new APIImpl(root, pathPrefix, customMapping, daoProvider);
				if (factories != null) {
					for (ObjectFactoryChain of: factories) api.addObjectFactory(of);
				}
			} catch (javax.xml.bind.UnmarshalException e) {
				if (e.getLinkedException() instanceof SAXParseException) {
					throw new ConfigurationException("Cannot parse "+apiDescriptionXml+": "+e.getLinkedException().getMessage());
				}
				throw new AssertionError(e);
			} catch (ConfigurationException e) {
				throw (ConfigurationException)e;
			} catch (RuntimeException e) {
				throw (RuntimeException)e;
			} catch (Exception e) {
				throw new AssertionError(e);
			}
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
}
