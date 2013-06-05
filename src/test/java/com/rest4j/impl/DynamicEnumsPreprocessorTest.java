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
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class DynamicEnumsPreprocessorTest {

	DynamicEnumsPreprocessor preprocessor = new DynamicEnumsPreprocessor();

	@Test public void testProcess_doc_method_not_found() throws Exception {
		Document xml = parse("doc-method-not-found.xml");
		try {
			preprocessor.process(xml);
			fail();
		} catch (ConfigurationException ce) {
			assertEquals("doc-method 'nosuchmethod' not found in class com.rest4j.impl.TestEnum", ce.getMessage());
		}
	}

	@Test public void testProcess_enum_class_with_values() throws Exception {
		Document xml = parse("enum-class-with-values.xml");
		try {
			preprocessor.process(xml);
			fail();
		} catch (ConfigurationException ce) {
			assertEquals("<values> should not have both @enum attribute and value subtags", ce.getMessage());
		}
	}

	@Test public void testProcess_dynamic_enums() throws Exception {
		Document xml = parse("dynamic-enums.xml");
		preprocessor.process(xml);

		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		NodeList nodes = (NodeList) xpath.compile("//model[@name='Test1']/fields/simple/values/value/text()").evaluate(xml, XPathConstants.NODESET);
		assertEquals("TEST,TEST1,S", join(nodes));
		nodes = (NodeList) xpath.compile("//model[@name='Test2']/fields/simple/values/value/text()").evaluate(xml, XPathConstants.NODESET);
		assertEquals("TEST,TEST1,S", join(nodes));
		nodes = (NodeList) xpath.compile("//model[@name='Test1']/fields/simple/values/value/@description").evaluate(xml, XPathConstants.NODESET);
		assertEquals("Just TEST", join(nodes));
		nodes = (NodeList) xpath.compile("//model[@name='Test2']/fields/simple/values/value/@description").evaluate(xml, XPathConstants.NODESET);
		assertEquals("TEST doc,TEST1 doc,S doc", join(nodes));
	}

	String join(NodeList nodes) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i<nodes.getLength(); i++) {
			if (i > 0) result.append(',');
			result.append(nodes.item(i).getTextContent());
		}
		return result.toString();
	}

	private Document parse(String name) throws SAXException, IOException, ParserConfigurationException {
		return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getClass().getResourceAsStream(name));
	}

}
