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

import java.util.UUID;

import static org.junit.Assert.*;

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
				"     * <ol>\n" +
				"     * <li>What else?</li>\n" +
				"     * </ol>\n" +
				"     * \n" +
				"     * <p>\n" +
				"     * Paragraph\n" +
				"     * </p>\n" +
				"     * lines<br></br>separated<br></br>\n" +
				"     * by<br></br>\n" +
				"     * brs<br></br>\n" +
				"     * <table>\n" +
				"     * <tr><th>col1</th><th>col2</th></tr>\n" +
				"     * <tr><td>val1</td><td>val2</td></tr>\n" +
				"     * </table>\n" +
				"     * \n" +
				"     * \n" +
				"     * \n" +
				"     * ä", result);
	}

	@Test
	public void testHashComment() throws Exception {
		String result = xpath("rest4j:hashComment('line 1\r\n line 2\r\n')", new XSLTFunctions.HashComment());
		assertEquals(
				"# line 1\n" +
				"#  line 2\n" +
				"# ", result);
	}

	@Test
	public void testHtmlToPlain() throws Exception {
		Processor proc = new Processor(false);
		proc.registerExtensionFunction(new XSLTFunctions.HtmlToPlain(2, "htmlToPlain"));
		XdmNode doc = proc.newDocumentBuilder().build(new StreamSource(getClass().getResourceAsStream("javadoc.xml")));
		XPathCompiler xPathCompiler = proc.newXPathCompiler();
		xPathCompiler.declareNamespace("rest4j", XSLTFunctions.NAMESPACE);
		XPathSelector selector = xPathCompiler.compile("rest4j:htmlToPlain(javadoc/(*|text()))").load();
		selector.setContextItem(doc);
		String result = selector.iterator().next().getStringValue();
		assertEquals("Patch an A And return\n" +
				"\t\tC(test). <Abrakadabra>&.\n" +
				"\t\t\n" +
				"\t\t- What else?\n" +
				"\n" +
				"\t\tParagraph\n" +
				"\t\t\n" +
				"\t\tlines\n" +
				"\t\tseparated\n" +
				"\t\tby\n" +
				"\t\tbrs\n" +
				"\t\t|col1||col2|\n" +
				"\t\t|val1||val2|\n" +
				"\t\tä", result);
	}

	@Test
	public void testXmlComments() throws Exception {
		Processor proc = new Processor(false);
		proc.registerExtensionFunction(new XSLTFunctions.XmlComments(1, "xmlComments"));
		XdmNode doc = proc.newDocumentBuilder().build(new StreamSource(getClass().getResourceAsStream("javadoc.xml")));
		XPathCompiler xPathCompiler = proc.newXPathCompiler();
		xPathCompiler.declareNamespace("rest4j", XSLTFunctions.NAMESPACE);
		XPathSelector selector = xPathCompiler.compile("rest4j:xmlComments(javadoc/(*|text()))").load();
		selector.setContextItem(doc);
		String result = selector.iterator().next().getStringValue();
		assertEquals("Patch an A And return\n" +
				"    /// C(test). &lt;Abrakadabra>&amp;.\n" +
				"    /// <para>- What else?</para>\n" +
				"    /// <para>\n"+
				"    /// Paragraph\n" +
				"    /// </para>\n"+
				"    /// lines<para/>\n" +
				"    /// separated<para/>\n" +
				"    /// by<para/>\n" +
				"    /// brs<para/>\n" +
				"    /// <para>|col1||col2|</para>\n" +
				"    /// <para>|val1||val2|</para>\n" +
				"    /// ä", result);
	}

	@Test
	public void testRandomUUID() throws Exception {
		Processor proc = new Processor(false);
		proc.registerExtensionFunction(new XSLTFunctions.RandomUUID());
		XPathCompiler xPathCompiler = proc.newXPathCompiler();
		xPathCompiler.declareNamespace("rest4j", XSLTFunctions.NAMESPACE);
		XPathSelector selector = xPathCompiler.compile("rest4j:randomUUID()").load();
		String result = selector.iterator().next().getStringValue();
		System.out.println(result);
		System.out.println(UUID.randomUUID().getMostSignificantBits());
		System.out.println(UUID.randomUUID().getLeastSignificantBits());
	}

	@Test
	public void testAssemblyUUID() throws Exception {
		Processor proc = new Processor(false);
		proc.registerExtensionFunction(new XSLTFunctions.AssemblyUUID());
		XPathCompiler xPathCompiler = proc.newXPathCompiler();
		xPathCompiler.declareNamespace("rest4j", XSLTFunctions.NAMESPACE);

		XPathSelector selector = xPathCompiler.compile("rest4j:assemblyUUID('Module')").load();
		String result = selector.iterator().next().getStringValue();
		System.out.println(result);

		selector = xPathCompiler.compile("rest4j:assemblyUUID('Module')").load();
		String result1 = selector.iterator().next().getStringValue();
		assertEquals(result, result1);

		selector = xPathCompiler.compile("rest4j:assemblyUUID('Module1')").load();
		String result2 = selector.iterator().next().getStringValue();
		assertFalse(result.equals(result2));
	}

	@Test
	public void testIdentifier_keyword() throws Exception {
		XSLTFunctions.Identifier function = new XSLTFunctions.Identifier();
		assertEquals("For", xpath("rest4j:identifier('for','for while id then else')", function));
		assertEquals("for", xpath("rest4j:identifier('for','forward')", function));
	}

	@Test
	public void testIdentifier_bad_chars() throws Exception {
		XSLTFunctions.Identifier function = new XSLTFunctions.Identifier();
		assertEquals("for_some_reason", xpath("rest4j:identifier('for.some@reason','for while id then else')", function));
		assertEquals("_5for", xpath("rest4j:identifier('5for','')", function));
	}

	@Test
	public void testPackageCamelCase_empty() throws Exception {
		XSLTFunctions.PackageCamelCase function = new XSLTFunctions.PackageCamelCase();
		assertEquals("", xpath("rest4j:packageCamelCase('')", function));
	}

	@Test
	public void testPackageCamelCase_dots() throws Exception {
		XSLTFunctions.PackageCamelCase function = new XSLTFunctions.PackageCamelCase();
		assertEquals("My.Package.Name", xpath("rest4j:packageCamelCase('my.package.Name')", function));
	}

	@Test
	public void testNormalizeVersion() throws Exception {
		assertEquals("1.0", xpath("replace('1.0-SNAPSHOT','(([0-9]+\\.)*[0-9]+).*', '$1')", null));
		assertEquals("5.0.11", xpath("replace('5.0.11-20131025','(([0-9]+\\.)*[0-9]+).*', '$1')", null));
	}

	private String xpath(String xpath, ExtensionFunction function) throws SaxonApiException {
		Processor proc = new Processor(false);
		if (function != null) proc.registerExtensionFunction(function);
		XPathCompiler xPathCompiler = proc.newXPathCompiler();
		xPathCompiler.declareNamespace("rest4j", XSLTFunctions.NAMESPACE);
		XPathSelector selector = xPathCompiler.compile(xpath).load();
		return selector.iterator().next().getStringValue();
	}
}

