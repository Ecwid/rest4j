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

package com.rest4j.generator;

import com.rest4j.impl.Util;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class DocGeneratorTest {
	Generator gen = new Generator();

	@Test public void testComputeModelGraph() throws Exception {
		Document xml = getDocument("doc-generator-graph.xml");
		List<Generator.ModelNode> graph = gen.computeModelGraph(xml);
		assertEquals(3, graph.size());
		Generator.ModelNode a = find(graph, "A");
		Generator.ModelNode b = find(graph, "B");
		Generator.ModelNode c = find(graph, "C");

		assertEquals(set(ref(b, false), ref(b, true)), a.references);
		assertEquals(set(ref(c, false)), b.references);
		assertEquals(set(), c.references);
	}

	@Test public void testComputeModels() throws Exception {
		Document xml = getDocument("doc-generator-graph.xml");
		List<Generator.ModelNode> graph = gen.computeModelGraph(xml);
		assertEquals("A,B,C", modelsToString(gen.computeModels(graph, "A", false)));
		assertEquals("patch A,patch B,B,patch C,C", modelsToString(gen.computeModels(graph, "A", true)));
	}

	@Test public void testPreprocess() throws Exception {
		Document xml = getDocument("doc-generator-graph.xml");
		gen.preprocess(xml);
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		xpath.setNamespaceContext(new Generator.APINamespaceContext());

		assertEquals("patch A,patch B,B,patch C,C", modelsToString(xpath.compile("//api:endpoint[api:service/@method='patch']/api:body/api:model").evaluate(xml, XPathConstants.NODESET)));
		assertEquals("C", modelsToString(xpath.compile("//api:endpoint[api:service/@method='patch']/api:response/api:model").evaluate(xml, XPathConstants.NODESET)));
		assertEquals("B,C", modelsToString(xpath.compile("//api:endpoint[api:route='get']/api:response/api:model").evaluate(xml, XPathConstants.NODESET)));
		assertEquals("B,C", modelsToString(xpath.compile("//api:endpoint[api:route='get']/api:response/api:model").evaluate(xml, XPathConstants.NODESET)));
		assertEquals("", modelsToString(xpath.compile("//api:endpoint[api:route='binary']/api:response/api:model").evaluate(xml, XPathConstants.NODESET)));
	}

	@Test public void testGenerate() throws Exception {
		new File("target/doc").mkdir();
		gen.setApiXmlUrl(getClass().getResource("doc-generator-graph.xml"));
		gen.setOutputDir("target/doc");
		gen.addParam(new TemplateParam("url", "http(s)://api.rest4j.com/"));
		gen.addParam(new TemplateParam("https-url", "https://api.rest4j.com/"));
		gen.setCustomXSLTUrl(getClass().getResource("custom.xslt"));
		gen.generate();
	}

	@Test public void testGenerate_client_lang() throws Exception {
		testGenerate();
		String patchA = IOUtils.toString(new File("target/doc/a.patch.html").toURI());
		assertFalse(patchA, patchA.contains("Some additional client info")); // Some additional python client info
		assertFalse(patchA, patchA.contains("Some additional python client info"));
	}

	String modelsToString(Object input) {
		Iterable<Node> models;
		if (input instanceof Iterable) models = (Iterable<Node>) input;
		else if (input instanceof NodeList) models = Util.it((NodeList) input);
		else throw new AssertionError();
		StringBuilder sb = new StringBuilder();
		for (Node model: models) {
			if (sb.length() > 0) sb.append(',');
			if (model.getAttributes().getNamedItem("patch") != null) {
				sb.append("patch ");
			}
			sb.append(model.getAttributes().getNamedItem("name").getTextContent());
		}
		return sb.toString();
	}

	Generator.Ref ref(Generator.ModelNode node, boolean array) {
		Generator.Ref ref = new Generator.Ref();
		ref.referencedModel = node;
		ref.array = array;
		return ref;
	}

	<T> Set<T> set(T ... ts) {
		Set<T> result = new HashSet<T>();
		for (T t: ts) result.add(t);
		return result;
	}

	private Generator.ModelNode find(List<Generator.ModelNode> graph, String name) {
		for (Generator.ModelNode node: graph) {
			if (node.name().equals(name)) return node;
		}
		return null;
	}


	private Document getDocument(String name) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		return documentBuilder.parse(getClass().getResourceAsStream(name));
	}
}
