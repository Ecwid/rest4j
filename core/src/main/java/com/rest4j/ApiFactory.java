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
 * Creates the instance of {@link API} from the given XML API description in XML.
 *
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class ApiFactory {
	URL apiDescriptionXml;
	String pathPrefix;
	ServiceProvider serviceProvider;
	List<ObjectFactory> factories = new ArrayList<ObjectFactory>();
	List<FieldFilter> fieldFilters = new ArrayList<FieldFilter>();
	List<Preprocessor> preprocessors = new ArrayList<Preprocessor>();
	String extSchema;
	Class extObjectFactory;
	private PermissionChecker permissionChecker;
	private final Cloner cloner;

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
	public ApiFactory(URL apiDescriptionXml, String pathPrefix, ServiceProvider serviceProvider, Cloner cloner) {
		this.apiDescriptionXml = apiDescriptionXml;
		this.pathPrefix = pathPrefix;
		this.serviceProvider = serviceProvider;
		this.cloner = cloner;
		preprocessors.add(new DefaultsPreprocessor());
	}

	/**
	 * Add an ObjectFactory to the end of the chain. Object factories are used to create Java object during unmarshalling.
	 * ObjectFactories are organized into a chain.
	 *
	 * @param of The object factory to be added.
	 * @see ObjectFactory
	 */
	public void addObjectFactory(ObjectFactory of) {
		factories.add(of);
	}

	/**
	 * Add a FieldFilter to the end of the chain. Field filters can be used to change or remove JSON fields
	 * during marshalling and unmarshalling.
	 * @param ff The field filter to be added.
	 * @see FieldFilter
	 */
	public void addFieldFilter(FieldFilter ff) {
		fieldFilters.add(ff);
	}

	/**
	 * Adds an API description preprocessor to the end of the list. Preprocessors can be used to change the
	 * API description XML.
	 *
	 * @param proc The preprocessor to be added.
	 * @see Preprocessor
	 */
	public void addPreprocessor(Preprocessor proc) {
		preprocessors.add(proc);
	}

	/**
	 * Sets path inside a classpath to the resource containing XML Schema for extra info (&lt;extra> tags).
	 * &lt;extra> tags can contain information for the your custom generators and field filters. This
	 * information is an arbitrary XML of the given schema.
	 *
	 * @param extObjectFactory the JAXB ObjectFactory to use when reading extra info with JAXB.
	 */
	public void setExtSchema(String extSchema, Class extObjectFactory) {
		this.extSchema = extSchema;
		this.extObjectFactory = extObjectFactory;
	}

	public URL getApiDescriptionXml() {
		return apiDescriptionXml;
	}

	public String getPathPrefix() {
		return pathPrefix;
	}

	public ServiceProvider getServiceProvider() {
		return serviceProvider;
	}

	public List<ObjectFactory> getFactories() {
		return factories;
	}

	public List<FieldFilter> getFieldFilters() {
		return fieldFilters;
	}

	public List<Preprocessor> getPreprocessors() {
		return preprocessors;
	}

	public String getExtSchema() {
		return extSchema;
	}

	public Class getExtObjectFactory() {
		return extObjectFactory;
	}

	/**
	 * Create the API instance. The XML describing the API is read, preprocessed, analyzed for errors,
	 * and the internal structures for Java object marshalling and unmarshalling are created, as
	 * well as endpoint mappings.
	 *
	 * @return The API instance
	 * @throws ConfigurationException When there is a problem with your XML description.
	 */
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
					fieldFilters.toArray(new FieldFilter[fieldFilters.size()]),
					permissionChecker,
					cloner
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

	/**
	 * This method can be used to just read the API description XML into a DOM. The XML is schema-validated
	 * and preprocessed with the preprocessors that were added with {@link #addPreprocessor(Preprocessor)}.
	 *
	 * @return The validated and preprocessed DOM.
	 */
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
			pre.process(this, xml);
		}
		return xml;
	}

	/**
	 * Check permission before call api method
	 * @param permissionChecker
	 */
	public void setPermissionChecker(PermissionChecker permissionChecker) {
		this.permissionChecker = permissionChecker;
	}
}
