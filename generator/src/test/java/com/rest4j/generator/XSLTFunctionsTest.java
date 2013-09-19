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

import net.sf.saxon.s9api.*;
import org.junit.Test;

import javax.xml.transform.stream.StreamSource;

import static org.junit.Assert.assertEquals;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class XSLTFunctionsTest {
	@Test
	public void testJavadoc() throws Exception {
		Processor proc = new Processor(false);
		proc.registerExtensionFunction(new XSLTFunctions.JavadocEscape(4, "javadocEscape"));
		XdmNode doc = proc.newDocumentBuilder().build(new StreamSource(getClass().getResourceAsStream("javadoc.xml")));
		XPathCompiler xPathCompiler = proc.newXPathCompiler();
		xPathCompiler.declareNamespace("rest4j", XSLTFunctions.NAMESPACE);
		XPathSelector selector = xPathCompiler.compile("rest4j:javadocEscape(javadoc/(*|text()))").load();
		selector.setContextItem(doc);
		String result = selector.iterator().next().getStringValue();
		assertEquals("Patch an A And return\n" +
				"     * <a href=\"test\">C</a>. &lt;Abrakadabra&gt;&.\n" +
				"     * What else?", result);
	}
}

