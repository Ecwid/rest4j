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

package com.rest4j.doc;

import com.rest4j.ApiFactory;
import com.rest4j.Preprocessor;
import com.rest4j.impl.Util;
import com.sun.org.apache.xml.internal.resolver.helpers.FileURL;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class DocGenerator implements URIResolver {
	URL apiXml;
	URL customXSLT;
	String outputDir = "target/doc";
	List<TemplateParam> params = new ArrayList<TemplateParam>();
	List<String> preprocessors = new ArrayList<String>();
	TransformerFactory tFactory = new net.sf.saxon.TransformerFactoryImpl();
	static final List<TemplateParam> defaultParams = new ArrayList<TemplateParam>();
	static {
		defaultParams.add(new TemplateParam("style", "style.css"));
	}

	public static void main(String[] args) throws Exception {
		if (args.length == 0 || args[0].equals("-h") || args[0].equals("--help")) {
			help();
			return;
		}
		DocGenerator gen = new DocGenerator();
		for (int i=0; i<args.length-1; i++) {
			if (args[i].equals("--output-dir") || args[i].equals("-o")) {
				gen.setOutputDir(args[++i]);
			} else if (args[i].equals("--api-xml") || args[i].equals("-x")) {
				gen.setApiXml(new File(args[++i]));
			} else if (args[i].equals("--xslt") || args[i].equals("-s")) {
				gen.setCustomXSLT(new File(args[++i]));
			} else if (args[i].equals("--preprocessor") || args[i].equals("-p")) {
				gen.preprocessors.add(args[++i]);
			} else if (args[i].equals("--param") || args[i].equals("-v")) {
				String[] pair = args[++i].split("=");
				TemplateParam param = new TemplateParam(pair[0], pair.length > 1 ? pair[1] : null);
				gen.addParam(param);
			} else {
				System.err.println("Unrecognized option "+args[i]);
				help();
				System.exit(-1);
			}
		}
		gen.generate();
	}

	private static void help() {
		l("Generates REST API documentation. Options are:");
		l("");
		l("   --api-xml or -x        XML API description file.");
		l("   --output-dir or -o     Output directory.");
		l("   --xslt or -s           Custom post-processing XSLT file (optional).");
		l("   --preprocessor or -p   A preprocessor class name (implementing Preprocessor interface). There can be several --preprocessor options.");
		l("   --param or -v          A name=value pair that is passed to the custom post-processing XSLT template.  There can be several --param options.");
	}

	private static void l(String s) {
		System.out.println(s);
	}

	public DocGenerator() {
		tFactory.setURIResolver(this);
	}

	public void setApiXml(File xml) throws MalformedURLException {
		this.apiXml = xml.toURI().toURL();
	}

	public void setApiXmlUrl(URL xml) {
		this.apiXml = xml;
	}

	public void setCustomXSLT(File customXSLT) throws MalformedURLException {
		this.customXSLT = customXSLT.toURI().toURL();
	}

	public void setCustomXSLTUrl(URL customXSLTUrl) {
		this.customXSLT = customXSLTUrl;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	public void addParam(TemplateParam param) {
		params.add(param);
	}

	public void generate() throws Exception {
		ApiFactory fac = new ApiFactory(apiXml, null, null);
		for (String className: preprocessors) {
			Preprocessor p = (Preprocessor) Class.forName(className).newInstance();
			fac.addPreprocessor(p);
		}
		Document xml = fac.getDocument();
		preprocess(xml);
		URL url = getDefaultStylesheet();

		String filename = "index.html";
		for (TemplateParam param: params) {
			if (param.getName().equals("filename")) {
				filename = param.getValue();
			}
		}

		Document doc = transform(xml, url);
		cleanup(doc.getDocumentElement());

		if (customXSLT != null) {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document composed =  documentBuilder.newDocument();
			org.w3c.dom.Element top = composed.createElementNS("http://rest4j.com/api-description", "top");

			composed.appendChild(top);
			top.appendChild(composed.adoptNode(xml.getDocumentElement()));
			top.appendChild(composed.adoptNode(doc.getDocumentElement()));

			xml = null; doc = null; // free some mem

			doc = transform(composed, customXSLT);
		}

		if ("files".equals(doc.getDocumentElement().getLocalName())) {
			// break the result into files
			for (Node child : Util.it(doc.getDocumentElement().getChildNodes())) {
				if ("file".equals(child.getLocalName())) {
					if (child.getAttributes().getNamedItem("name") == null) {
						throw new IllegalArgumentException("Attribute name not found in <file>");
					}
					String name = child.getAttributes().getNamedItem("name").getTextContent();
					File file = new File(outputDir, name);
					file.getParentFile().mkdirs();
					System.out.println("Write " + file.getAbsolutePath());
					Attr copyFromAttr = (Attr)child.getAttributes().getNamedItem("copy-from");
					if (copyFromAttr == null) {
						output(child, file);
					} else {
						String copyFrom = copyFromAttr.getValue();
						URL asset = getClass().getClassLoader().getResource(copyFrom);
						if (asset == null) {
							asset = getClass().getResource(copyFrom);
						}
						if (asset == null) {
							File assetFile = new File(copyFrom);
							if (!assetFile.canRead()) {
								if (customXSLT != null) {
									asset = new URL(customXSLT, copyFrom);
									try {
										asset.openStream().close();
									} catch (FileNotFoundException fnfe) {
										asset = null;
									}
								}
								if (asset == null) throw new IllegalArgumentException("File '"+ copyFrom +"' specified by @copy-from not found in the classpath or filesystem");
							} else {
								asset = assetFile.toURI().toURL();
							}
						}
						InputStream is = asset.openStream();
						OutputStream fos = new FileOutputStream(file);
						try {
							IOUtils.copy(is, fos);
						} finally {
							IOUtils.closeQuietly(is);
							IOUtils.closeQuietly(fos);
						}
					}
				} else if (child.getNodeType() == Node.ELEMENT_NODE) {
					throw new IllegalArgumentException("Something but <file> found inside <files>");
				}
			}
		} else {
			File file = new File(outputDir, filename);
			System.out.println("Write "+file.getAbsolutePath());
			DOMSource source = new DOMSource(doc);
			FileOutputStream fos = new FileOutputStream(file);
			try {
				StreamResult result = new StreamResult(fos);
				Transformer trans = tFactory.newTransformer();
				trans.transform(source, result);
			} finally {
				IOUtils.closeQuietly(fos);
			}
		}
	}

	private void cleanup(Element element) {
		if ("http://www.w3.org/1999/xhtml".equals(element.getNamespaceURI())) {
			element.getOwnerDocument().renameNode(element, null, element.getLocalName());
		}
		for (Node child: Util.it(element.getChildNodes())) {
			if (child instanceof Element) {
				cleanup((Element)child);
			}
		}
	}

	private Document transform(Document xml, URL url) throws IOException, ParserConfigurationException, TransformerException {
		Templates templates = tFactory.newTemplates(new StreamSource(url.openStream(), url.toString()));
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		Transformer transformer = templates.newTransformer();
		Set<String> paramNames = new HashSet<String>();
		if (params != null) for (TemplateParam param: params) {
			transformer.setParameter(param.getName(), param.getValue());
			paramNames.add(param.getName());
		}
		for (TemplateParam deflt: defaultParams) {
			if (!paramNames.contains(deflt.getName())) {
				transformer.setParameter(deflt.getName(), deflt.getValue());
			}
		}
		Document doc = factory.newDocumentBuilder().newDocument();
		//transformer.setOutputProperty("indent", "no");
		//transformer.transform(new DOMSource(xml), new StreamResult(System.out)/*new DOMResult(doc)*/);
		transformer.transform(new DOMSource(xml), new DOMResult(doc));
		return doc;
	}

	private void output(Node node, File file) throws TransformerException, IOException {
		FileOutputStream fos = new FileOutputStream(file);
		try {
			for (Node child: Util.it(node.getChildNodes())) {
				DOMSource source = new DOMSource(child);
				StreamResult result = new StreamResult(fos);
				Transformer trans = tFactory.newTransformer();
				trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				trans.setOutputProperty(OutputKeys.INDENT, "no");
				trans.transform(source, result);
			}
		} finally {
			IOUtils.closeQuietly(fos);
		}
	}

	void preprocess(Document xml) throws Exception {
		List<ModelNode> graph = computeModelGraph(xml);
		for (Node endpoint: Util.it(xml.getElementsByTagName("endpoint"))) {
			Node body = findChild(endpoint, "body");
			if (body != null) {
				preprocess(graph, body);
			}
			Node response = findChild(endpoint, "response");
			if (response != null) {
				preprocess(graph, response);
			}
		}
	}

	void preprocess(List<ModelNode> graph, Node node) throws Exception {
		Node json = findChild(node, "json");
		List<Node> models;
		if (json != null) {
			String type = json.getAttributes().getNamedItem("type").getTextContent();
			models = computeModels(graph, type, false);
		} else {
			Node patch = findChild(node, "patch");
			if (patch != null) {
				String type = patch.getAttributes().getNamedItem("type").getTextContent();
				models = computeModels(graph, type, true);
			} else {
				return;
			}
		}
		for (Node model: models) {
			node.appendChild(model);
		}
	}

	Node findChild(Node el, String name) {
		for (Node child: Util.it(el.getChildNodes())) {
			if (name.equals(child.getNodeName())) {
				return child;
			}
		}
		return null;
	}

	public URL getDefaultStylesheet() {
		return getClass().getResource("doc.xslt");
	}

	@Override
	public Source resolve(String href, String base) throws TransformerException {
		String uri = href;
		String result = null;
		if (href.equals("doc.xslt")) {
			result = getDefaultStylesheet().toString();
		}
		if (result == null) {
			try {
				URL url;

				if (base==null) {
					url = new URL(uri);
					result = url.toString();
				} else {
					URL baseURL = new URL(base);
					url = (href.length()==0 ? baseURL : new URL(baseURL, uri));
					result = url.toString();
				}
			} catch (java.net.MalformedURLException mue) {
				// try to make an absolute URI from the current base
				String absBase = makeAbsolute(base);
				if (!absBase.equals(base)) {
					// don't bother if the absBase isn't different!
					return resolve(href, absBase);
				} else {
					throw new TransformerException("Malformed URL "
							+ href + "(base " + base + ")",
							mue);
				}
			}
		}

		SAXSource source = new SAXSource();
		source.setInputSource(new InputSource(result));
		return source;
	}

	/** Attempt to construct an absolute URI */
	private String makeAbsolute(String uri) {
		if (uri == null) {
			uri = "";
		}

		try {
			URL url = new URL(uri);
			return url.toString();
		} catch (MalformedURLException mue) {
			try {
				URL fileURL = FileURL.makeURL(uri);
				return fileURL.toString();
			} catch (MalformedURLException mue2) {
				// bail
				return uri;
			}
		}
	}


	static class Ref {
		ModelNode referencedModel;
		boolean array;

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Ref)) return false;
			Ref ref = (Ref)obj;
			if (!ref.referencedModel.name().equals(referencedModel.name())) return false;
			return ref.array == array;
		}

		@Override
		public int hashCode() {
			return referencedModel.name().hashCode()*2+(array? 1: 0);
		}

		@Override
		public String toString() {
			return (array ? "array ":"")+referencedModel.name();
		}
	}

	static class ModelNode {
		Node model;
		Set<Ref> references = new LinkedHashSet<Ref>();
		boolean issuePatch;
		boolean issueNormal;

		public ModelNode(Node model) {
			this.model = model;
		}

		String name() {
			return model.getAttributes().getNamedItem("name").getTextContent();
		}

	}

	List<Node> computeModels(List<ModelNode> graph, String rootType, boolean patch) throws Exception {
		List<Node> result = new ArrayList<Node>();
		ModelNode root = null;
		for (ModelNode node: graph) {
			node.issueNormal = false;
			node.issuePatch = false;
			if (node.name().equals(rootType)) {
				if (patch) {
					node.issuePatch = true;
					issueNode(result, node, true);
				} else {
					node.issueNormal = true;
					issueNode(result, node, false);
				}
				root = node;
			}
		}

		if (root == null) throw new IllegalArgumentException("No such type: "+rootType);

		// closure
		boolean changed;
		do {
			changed = false;
			for (ModelNode node: graph) {
				for (Ref ref: node.references) {
					if (!ref.array) {
						if (node.issuePatch && !ref.referencedModel.issuePatch) {
							ref.referencedModel.issuePatch = true;
							changed = true;
							issueNode(result, ref.referencedModel, true);
						}
					}
					if ((node.issueNormal || ref.array && node.issuePatch) && !ref.referencedModel.issueNormal) {
						ref.referencedModel.issueNormal = true;
						changed = true;
						issueNode(result, ref.referencedModel, false);
					}
				}
			}
		} while (changed);
		return result;
	}

	private void issueNode(List<Node> result, ModelNode node, boolean patch) {
		Node node1 = node.model.cloneNode(true);
		result.add(node1);
		if (patch) node1.getAttributes().setNamedItem(node1.getOwnerDocument().createAttribute("patch"));
	}

	List<ModelNode> computeModelGraph(Document xml) throws Exception {
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		xpath.setNamespaceContext(new APINamespaceContext());
		XPathExpression refsExpr = xpath.compile(".//api:complex");
		HashMap<String, ModelNode> graph = new HashMap<String, ModelNode>();
		for (Node model: Util.it(xml.getDocumentElement().getElementsByTagName("model"))) {
			String name = ((Attr)model.getAttributes().getNamedItem("name")).getValue();
			graph.put(name, new ModelNode(model));
		}
		for (ModelNode node: graph.values()) {
			for (Node complex: Util.it((NodeList) refsExpr.evaluate(node.model, XPathConstants.NODESET))) {
				Ref ref = new Ref();
				String type = complex.getAttributes().getNamedItem("type").getTextContent();
				ModelNode referenced = graph.get(type);
				if (referenced == null) throw new IllegalArgumentException("Wrong reference from "+
						node.model.getAttributes().getNamedItem("name").getTextContent()+"."+complex.getAttributes().getNamedItem("name").getTextContent()+" to "+type);
				ref.referencedModel = referenced;
				if (complex.getAttributes().getNamedItem("collection").getTextContent().equals("array")) {
					ref.array = true;
				}
				node.references.add(ref);
			}
		}
		return new ArrayList<ModelNode>(graph.values());
	}

	static class APINamespaceContext implements NamespaceContext {

		public String getNamespaceURI(String prefix) {
			if("api".equals(prefix)) {
				return "http://rest4j.com/api-description";
			}
			return null;
		}

		public String getPrefix(String namespaceURI) {
			return null;
		}

		public Iterator getPrefixes(String namespaceURI) {
			return null;
		}

	}

//	private void computeModelGraph(Document xml) {
//		for (Node endpoint: it(xml.getDocumentElement().getElementsByTagName("endpoint"))) {
//			computeModelGraph(xml, endpoint);
//		}
//	}
}
