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

import com.rest4j.ConfigurationException;
import com.rest4j.Preprocessor;
import org.w3c.dom.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

/**
 * A preprocessor that fills &lt;values> tags with enum constants according to the @enum and @doc-method attributes.
 *
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class DynamicEnumsPreprocessor implements Preprocessor {

	@Override
	public void process(Document xml) throws ConfigurationException {
		for (Node element: it(xml.getDocumentElement().getChildNodes())) {
			if (!"model".equals(element.getNodeName())) continue;
			Node fields = find(element, "fields");
			for (Node child: it(fields.getChildNodes())) {
				Node values = find(child, "values");
				if (values == null) continue;
				Attr enumAttr = (Attr) values.getAttributes().getNamedItem("enum");
				if (enumAttr == null) continue;
				try {
					Class enumClass = Class.forName(enumAttr.getValue());
					if (!enumClass.isEnum()) {
						throw new ConfigurationException("Class "+enumAttr.getValue()+" is not a enum");
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
						for (Object value: enumClass.getEnumConstants()) {
							String name = ((Enum)value).name();
							Object returned = docMethod.invoke(value);
							String doc = null;
							if (returned != null) doc = returned.toString();
							Element valueElement = xml.createElement("value");
							valueElement.appendChild(xml.createTextNode(name));
							if (doc != null && !doc.equals(name)) {
								valueElement.setAttribute("description", doc);
							}
							values.appendChild(valueElement);
						}
					} catch (NoSuchMethodException e) {
						throw new ConfigurationException("doc-method '"+docMethodName+"' not found in "+enumClass);
					} catch (InvocationTargetException e) {
						throw new ConfigurationException("doc-method '"+docMethodName+"' threw exception "+e.getCause());
					} catch (IllegalAccessException e) {
						throw new ConfigurationException("doc-method '"+docMethodName+"' cannot be called: "+e.getMessage());
					}
				} catch (ClassNotFoundException e) {
					throw new ConfigurationException("Enum class "+enumAttr.getValue()+" not found");
				}
			}
		}
	}

	static Node find(Node element, String name) {
		for (Node child: it(element.getChildNodes())) {
			if (name.equals(child.getNodeName())) {
				return child;
			}
		}
		return null;
	}

	static Iterable<Node> it(final NodeList nodeList) {
		return new Iterable<Node>() {
			@Override
			public Iterator<Node> iterator() {
				return new Iterator<Node>() {
					int i = 0;
					@Override
					public boolean hasNext() {
						return i < nodeList.getLength();
					}

					@Override
					public Node next() {
						return nodeList.item(i++);
					}

					@Override
					public void remove() {
					}
				};
			}
		};
	}
}
