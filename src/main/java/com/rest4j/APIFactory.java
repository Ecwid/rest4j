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
import com.rest4j.impl.DefaultsPreprocessor;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Receives the API description in XML and creates the corresponding JSON marshaller and unmarshaller.
 *
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class APIFactory {
	URL apiDescriptionXml;
	String pathPrefix;
	ServiceProvider serviceProvider;
	List<ObjectFactory> factories = new ArrayList<ObjectFactory>();
	List<FieldFilter> fieldFilters = new ArrayList<FieldFilter>();
	List<Preprocessor> preprocessors = new ArrayList<Preprocessor>();
	String extSchema;
	Class extObjectFactory;

	/**
	 * Create a factory that can be used to create API objects. This constructor does not accept ObjectFactories.
	 * If you want customize object creation, pass ObjectFactory to the {@link #addObjectFactory(ObjectFactory)}
	 * prior to the createAPI() call.
	 *
	 * @param apiDescriptionXml An URL to the API description conforming to the api.xsd schema.
	 * @param pathPrefix An prefix that all requests should have. The prefix is removed prior matching against
	 *                   endpoint route specified in the API description XML.
	 *
	 * @param serviceProvider Used to lookup services and custom field mappers during initialization step.
	 *                        Services are looked up once during the call to createAPI().
	 */
	public APIFactory(URL apiDescriptionXml, String pathPrefix, ServiceProvider serviceProvider) {
		this.apiDescriptionXml = apiDescriptionXml;
		this.pathPrefix = pathPrefix;
		this.serviceProvider = serviceProvider;
		preprocessors.add(new DefaultsPreprocessor());
	}

	public void addObjectFactory(ObjectFactory of) {
		factories.add(of);
	}

	public void addFieldFilter(FieldFilter ff) {
		fieldFilters.add(ff);
	}

	public void addPreprocessor(Preprocessor proc) {
		preprocessors.add(proc);
	}

	/**
	 * Sets path inside a classpath to the resource containing XML Schema for extra info (&lt;extra> tags).
	 * @param extObjectFactory ObjectFactory to use when reading extra info.
	 */
	public void setExtSchema(String extSchema, Class extObjectFactory) {
		this.extSchema = extSchema;
		this.extObjectFactory = extObjectFactory;
	}

	public API createAPI() throws ConfigurationException {
		try {
			JAXBContext context;
			if (extObjectFactory == null)
				context = JAXBContext.newInstance(com.rest4j.impl.model.ObjectFactory.class);
			else
				context = JAXBContext.newInstance(com.rest4j.impl.model.ObjectFactory.class, extObjectFactory);
			Document xml = getDocument();

			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Source apiXsdSource = new StreamSource(getClass().getResourceAsStream("api.xsd"));
			List<Source> xsds = new ArrayList<Source>();
			xsds.add(apiXsdSource);
			Schema schema;
			if (!StringUtils.isEmpty(extSchema)) {
				xsds.add(new StreamSource(getClass().getClassLoader().getResourceAsStream(extSchema)));
			}
			xsds.add(new StreamSource(getClass().getResourceAsStream("html.xsd")));
			schema = schemaFactory.newSchema(xsds.toArray(new Source[xsds.size()]));

			Unmarshaller unmarshaller = context.createUnmarshaller();
			unmarshaller.setSchema(schema);
			JAXBElement<com.rest4j.impl.model.API> element = (JAXBElement<com.rest4j.impl.model.API>) unmarshaller.unmarshal(xml);
			com.rest4j.impl.model.API root = element.getValue();
			APIImpl api;
			api = new APIImpl(root, pathPrefix, serviceProvider,
					factories.toArray(new ObjectFactory[factories.size()]),
					fieldFilters.toArray(new FieldFilter[fieldFilters.size()])
					);
			return api;
		} catch (javax.xml.bind.UnmarshalException e) {
			if (e.getLinkedException() instanceof SAXParseException) {
				SAXParseException spe = (SAXParseException)e.getLinkedException();
				throw new ConfigurationException("Cannot parse "+apiDescriptionXml, spe);
			}
			throw new AssertionError(e);
		} catch (ConfigurationException e) {
			throw e;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	public Document getDocument() throws ParserConfigurationException, SAXException, IOException, ConfigurationException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document xml = null;
		try {
			xml = documentBuilder.parse(apiDescriptionXml.toURI().toString());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		for (Preprocessor pre: preprocessors) {
			pre.process(xml);
		}
		return xml;
	}

}
