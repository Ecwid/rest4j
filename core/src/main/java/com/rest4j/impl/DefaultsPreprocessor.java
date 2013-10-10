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

package com.rest4j.impl;

import com.rest4j.ApiFactory;
import com.rest4j.ConfigurationException;
import com.rest4j.Preprocessor;
import org.w3c.dom.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

import static com.rest4j.impl.Util.find;
import static com.rest4j.impl.Util.it;

/**
 * A preprocessor that fills default values:
 * <ul>
 * <li> &lt;values> tags with enum constants according to the @enum and @doc-method attributes.</li>
 * <li> Inserts missing @method in the endpoint tags equal to lowercase @http.</li>
 * <li> Inserts missing @collection (sets it to 'singleton')</li>
 * <li> Inserts missing @type (sets it to 'string')</li>
 * <li> Sets up default @nullable and @default attributes.</li>
 * </ul>
 *
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class DefaultsPreprocessor implements Preprocessor {

	@Override
	public void process(ApiFactory apiFactory, Document xml) throws ConfigurationException {
		cleanup(xml.getDocumentElement());
		for (Node element : it(xml.getDocumentElement().getChildNodes())) {
			if ("model".equals(element.getNodeName())) {
				Node fields = find(element, "fields");
				for (Node child : it(fields.getChildNodes())) {
					if (child.getNodeType() == Node.ELEMENT_NODE) {
						processField(xml, child);
					}
				}
			} else if ("endpoint".equals(element.getNodeName())) {
				processEndpoint(xml, element);
			}
		}
	}

	// for some reason, some parsers insert xml:base attribute into an external-entity xml elements, which
	// will fail during JAXB validation
	private void cleanup(Element element) {
		NamedNodeMap attributes = element.getAttributes();
		if (attributes.getNamedItemNS("http://www.w3.org/XML/1998/namespace", "base") != null) {
			attributes.removeNamedItemNS("http://www.w3.org/XML/1998/namespace", "base");
		}
		for (Node child: it(element.getChildNodes())) {
			if (child instanceof Element) {
				cleanup((Element)child);
			}
		}
	}

	private void processEndpoint(Document xml, Node element) throws ConfigurationException {
		Node service = find(element, "service");
		if (service.getAttributes().getNamedItem("method") == null) {
			String httpMethod = element.getAttributes().getNamedItem("http").getTextContent();
			Attr method = xml.createAttribute("method");
			method.setNodeValue(httpMethod.toLowerCase(Locale.ENGLISH));
			service.getAttributes().setNamedItem(method);
		}
		defaultValue(xml, element, "httpsonly", "false");
		Node parameters = find(element, "parameters");
		for (Node param: it(parameters.getChildNodes())) {
			if ("parameter".equals(param.getNodeName())) {
				processParameter(xml, param);
			}
		}
		Node response = find(element, "response");
		if (response != null) {
			Node json = find(response, "json");
			if (json != null) {
				defaultValue(xml, json, "collection", "singleton");
				defaultValue(xml, json, "optional", "false");
			}
		}
		Node body = find(element, "body");
		if (body != null) {
			Node json = find(body, "json");
			if (json != null) {
				defaultValue(xml, json, "collection", "singleton");
				defaultValue(xml, json, "optional", "false");
			}
		}
	}

	private void defaultValue(Document xml, Node element, String attrName, String value) {
		Attr collection = attr(element, attrName);
		if (collection == null) {
			collection = xml.createAttribute(attrName);
			collection.setValue(value);
			element.getAttributes().setNamedItem(collection);
		}
	}

	private void processParameter(Document xml, Node param) throws ConfigurationException {
		defaultValue(xml, param, "type", "string");
		if (isPathParam(param)) {
			Attr optionalAttr = attr(param, "optional");
			if (optionalAttr != null && "true".equals(optionalAttr.getValue())) {
				throw new ConfigurationException("The path parameter '"+param.getAttributes().getNamedItem("name").getTextContent()+"' is declared as optional='true'. Path parameters cannot be optional.");
			}
			defaultValue(xml, param, "optional", "false");
		} else {
			defaultValue(xml, param, "optional", "true");
		}
		defaultValue(xml, param, "httpsonly", "false");
		processEnums(xml, param);
	}

	private boolean isPathParam(Node param) {
		String name = attr(param, "name").getValue();
		Node routeNode = find(param.getParentNode().getParentNode(), "route");
		for (Node child: Util.it(routeNode.getChildNodes())) {
			if ("param".equals(child.getNodeName())) {
				if (name.equals(child.getTextContent())) {
					return true;
				}
			}
		}
		return false;
	}

	private void processField(Document xml, Node child) throws ConfigurationException {
		Attr collection = attr(child, "collection");
		Attr nullable = attr(child, "nullable");
		Attr deflt = attr(child, "default");
		Attr valueAttr = attr(child, "value");
		Attr type = attr(child, "type");
		if (collection == null) {
			collection = xml.createAttribute("collection");
			collection.setValue("singleton");
			child.getAttributes().setNamedItem(collection);
		}
		if (nullable == null) {
			nullable = xml.createAttribute("nullable");
			if ((valueAttr != null && "null".equals(valueAttr.getValue())) || (deflt != null && "null".equals(deflt.getValue()))) {
				nullable.setValue("true");
			} else {
				nullable.setValue("false");
			}
			child.getAttributes().setNamedItem(nullable);
		}
		if (deflt == null && !"singleton".equals(collection.getValue()) && "false".equals(nullable.getValue())) {
			deflt = xml.createAttribute("default");
			deflt.setValue("empty");
			child.getAttributes().setNamedItem(deflt);
		}
		if (type == null) {
			type = xml.createAttribute("type");
			type.setValue("string");
			child.getAttributes().setNamedItem(type);
		}

		processEnums(xml, child);
	}

	private void processEnums(Document xml, Node child) throws ConfigurationException {
		Node values = find(child, "values");
		if (values == null) return;
		Attr enumAttr = (Attr) values.getAttributes().getNamedItem("enum");
		if (enumAttr == null) return;
		try {
			Class enumClass = Class.forName(enumAttr.getValue());
			if (!enumClass.isEnum()) {
				throw new ConfigurationException("Class " + enumAttr.getValue() + " is not a enum");
			}
			Attr docMethodAttr = (Attr) values.getAttributes().getNamedItem("doc-method");
			String docMethodName = "toString";
			if (docMethodAttr != null) {
				docMethodName = docMethodAttr.getValue();
			}
			if (find(values, "value") != null) {
				throw new ConfigurationException("<values> should not have both @enum attribute and value subtags");
			}
			try {
				Method docMethod = enumClass.getMethod(docMethodName);
				for (Object value : enumClass.getEnumConstants()) {
					String name = ((Enum) value).name();
					Object returned = docMethod.invoke(value);
					String doc = null;
					if (returned != null) doc = returned.toString();
					Element valueElement = xml.createElementNS("http://rest4j.com/api-description", "value");
					valueElement.appendChild(xml.createTextNode(name));
					if (doc != null && !doc.equals(name)) {
						valueElement.setAttribute("description", doc);
					}
					values.appendChild(valueElement);
				}
			} catch (NoSuchMethodException e) {
				throw new ConfigurationException("doc-method '" + docMethodName + "' not found in " + enumClass);
			} catch (InvocationTargetException e) {
				throw new ConfigurationException("doc-method '" + docMethodName + "' threw exception " + e.getCause());
			} catch (IllegalAccessException e) {
				throw new ConfigurationException("doc-method '" + docMethodName + "' cannot be called: " + e.getMessage());
			}
		} catch (ClassNotFoundException e) {
			throw new ConfigurationException("Enum class " + enumAttr.getValue() + " not found");
		}
	}

	Attr attr(Node node, String name) {
		if (node.getAttributes() == null) return null;
		return (Attr) node.getAttributes().getNamedItem(name);
	}

}
