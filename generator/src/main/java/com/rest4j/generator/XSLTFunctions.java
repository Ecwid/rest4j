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
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class XSLTFunctions {

	public static final String NAMESPACE = "http://rest4j.com/func";
	public static final String WRONG_CHAR_PTRN = "[^a-zA-Z0-9_]";

	public static ExtensionFunction[] functions() {
		return new ExtensionFunction[] {
				new CamelCase(),
				new PackageCamelCase(),
				new Quote(),
				new JavadocEscape(4, "javadocEscape"),
				new JavadocEscape(0, "javadocEscape0"),
				new XmlComments(2, "xmlComments"),
				new Identifier(),
				new Singular(),
				new HashComment(),
				new HtmlToPlain(1, "htmlToPlain1"),
				new HtmlToPlain(2, "htmlToPlain2"),
				new RandomUUID(),
				new AssemblyUUID()
		};
	}

	static class AssemblyUUID implements ExtensionFunction {
		@Override
		public QName getName() {
			return new QName(NAMESPACE, "assemblyUUID");
		}

		@Override
		public SequenceType getResultType() {
			return SequenceType.makeSequenceType(
					ItemType.STRING, OccurrenceIndicator.ONE
			);
		}

		@Override
		public SequenceType[] getArgumentTypes() {
			return new SequenceType[] {SequenceType.makeSequenceType(ItemType.STRING, OccurrenceIndicator.ONE)};
		}

		@Override
		public XdmValue call(XdmValue[] arguments) throws SaxonApiException {
			String packageName = arguments[0].itemAt(0).getStringValue();
			UUID uuid = new UUID(4421721295643364989l, -5717901630047851516l ^ packageName.hashCode());
			return new XdmAtomicValue(uuid.toString());
		}
	}

	static class RandomUUID implements ExtensionFunction {
		@Override
		public QName getName() {
			return new QName(NAMESPACE, "randomUUID");
		}

		@Override
		public SequenceType getResultType() {
			return SequenceType.makeSequenceType(
					ItemType.STRING, OccurrenceIndicator.ONE
			);
		}

		@Override
		public SequenceType[] getArgumentTypes() {
			return new SequenceType[] {
			};
		}

		@Override
		public XdmValue call(XdmValue[] arguments) throws SaxonApiException {
			return new XdmAtomicValue(java.util.UUID.randomUUID().toString());
		}
	}

	static class CamelCase implements ExtensionFunction {
		@Override
		public QName getName() {
			return new QName(NAMESPACE, "camelCase");
		}

		@Override
		public SequenceType getResultType() {
			return SequenceType.makeSequenceType(
					ItemType.STRING, OccurrenceIndicator.ONE
			);
		}

		@Override
		public SequenceType[] getArgumentTypes() {
			return new SequenceType[] {
				SequenceType.makeSequenceType(ItemType.STRING, OccurrenceIndicator.ONE),
				SequenceType.makeSequenceType(ItemType.STRING, OccurrenceIndicator.ONE)
			};
		}

		@Override
		public XdmValue call(XdmValue[] arguments) throws SaxonApiException {
			String first = arguments[0].itemAt(0).getStringValue();
			String second = arguments[1].itemAt(0).getStringValue();
			second = second.replaceAll(WRONG_CHAR_PTRN, "_");
			return new XdmAtomicValue(first + StringUtils.capitalize(second));
		}
	}

	static class PackageCamelCase implements ExtensionFunction {
		@Override
		public QName getName() {
			return new QName(NAMESPACE, "packageCamelCase");
		}

		@Override
		public SequenceType getResultType() {
			return SequenceType.makeSequenceType(
					ItemType.STRING, OccurrenceIndicator.ONE
			);
		}

		@Override
		public SequenceType[] getArgumentTypes() {
			return new SequenceType[] {
					SequenceType.makeSequenceType(ItemType.STRING, OccurrenceIndicator.ONE)
			};
		}

		@Override
		public XdmValue call(XdmValue[] arguments) throws SaxonApiException {
			String packageName = arguments[0].itemAt(0).getStringValue();
			StringBuilder sb = new StringBuilder(packageName.length());
			char c = 0;
			for (int i=0; i<packageName.length(); i++) {
				if (c == '.')
					c = Character.toUpperCase(packageName.charAt(i));
				else
					c = packageName.charAt(i);
				if (i == 0) c = Character.toUpperCase(c);
				sb.append(c);
			}
			return new XdmAtomicValue(sb.toString());
		}
	}

	static class Quote implements ExtensionFunction {
		@Override
		public QName getName() {
			return new QName(NAMESPACE, "quote");
		}

		@Override
		public SequenceType getResultType() {
			return SequenceType.makeSequenceType(ItemType.STRING, OccurrenceIndicator.ONE);
		}

		@Override
		public SequenceType[] getArgumentTypes() {
			return new SequenceType[] {SequenceType.makeSequenceType(ItemType.STRING, OccurrenceIndicator.ONE)};
		}

		@Override
		public XdmValue call(XdmValue[] arguments) throws SaxonApiException {
			String arg = arguments[0].itemAt(0).getStringValue();
			return new XdmAtomicValue("\""+StringEscapeUtils.escapeJava(arg)+"\"");
		}
	}

	static class Singular implements ExtensionFunction {
		@Override
		public QName getName() {
			return new QName(NAMESPACE, "singular");
		}

		@Override
		public SequenceType getResultType() {
			return SequenceType.makeSequenceType(ItemType.STRING, OccurrenceIndicator.ONE);
		}

		@Override
		public SequenceType[] getArgumentTypes() {
			return new SequenceType[] {SequenceType.makeSequenceType(ItemType.STRING, OccurrenceIndicator.ONE)};
		}

		@Override
		public XdmValue call(XdmValue[] arguments) throws SaxonApiException {
			String arg = arguments[0].itemAt(0).getStringValue();
			if (arg.endsWith("ies")) arg = arg.substring(0, arg.length()-3)+"y";
			else if (arg.endsWith("ses")) arg = arg.substring(0, arg.length()-2);
			else if (arg.endsWith("hes")) arg = arg.substring(0, arg.length()-2);
			else if (arg.endsWith("s")) arg = arg.substring(0, arg.length()-1);
			return new XdmAtomicValue(arg);
		}
	}

	static class JavadocEscape implements ExtensionFunction {
		final int indent;
		final String name;

		JavadocEscape(int indent, String name) {
			this.indent = indent;
			this.name = name;
		}

		@Override
		public QName getName() {
			return new QName(NAMESPACE, name);
		}

		@Override
		public SequenceType getResultType() {
			return SequenceType.makeSequenceType(ItemType.STRING, OccurrenceIndicator.ONE);
		}

		@Override
		public SequenceType[] getArgumentTypes() {
			return new SequenceType[] {
					SequenceType.makeSequenceType(ItemType.ANY_ITEM, OccurrenceIndicator.ZERO_OR_MORE) // comment text, HTML
			};
		}

		@Override
		public XdmValue call(XdmValue[] arguments) throws SaxonApiException {
			JavadocBuilder sb = new JavadocBuilder();
			sb.setIndent(indent);
			for (XdmItem item : arguments[0]) {
				if (item instanceof XdmAtomicValue) sb.append(item.getStringValue());
				else if (item instanceof XdmNode) {
					XdmNode node = (XdmNode) item;
					if (node.getNodeName() != null && "title".equals(node.getNodeName().getLocalName())) {
						// remove the 'title' tag
						XdmSequenceIterator it = node.axisIterator(Axis.CHILD);
						while (it.hasNext()) {
							XdmItem next = it.next();
							if (next instanceof XdmNode) toJavadoc((XdmNode) next, sb);
						}
					} else toJavadoc(node, sb);
				}
			}
			return new XdmAtomicValue(sb.toString());
		}

		private void toJavadoc(XdmNode node, JavadocBuilder sb) {
			switch (node.getNodeKind()) {
				case ATTRIBUTE:
					break;
				case ELEMENT:
					sb.append('<').append(node.getNodeName().getLocalName());
					XdmSequenceIterator it = node.axisIterator(Axis.ATTRIBUTE);
					while (it.hasNext()) {
						XdmNode attr = (XdmNode)it.next();
						sb.append(' ').append(attr.getNodeName().getLocalName()).append("=\"").append(StringEscapeUtils.escapeHtml(attr.getStringValue())).append("\"");
					}
					sb.append('>');
					it = node.axisIterator(Axis.CHILD);
					while (it.hasNext()) {
						toJavadoc((XdmNode) it.next(), sb);
					}
					sb.append("</").append(node.getNodeName().getLocalName()).append('>');
					break;
				case TEXT:
				case DOCUMENT: // for external entities
					sb.appendText(node.getStringValue());
					break;
			}
		}
	}

	static class JavadocBuilder {
		StringBuilder buf = new StringBuilder();
		boolean leadingSpaces = true;
		boolean startOfLineSpaces = true;
		private int indent;
		private String asterisk = "     * ";

		JavadocBuilder append(char c) {
			if (leadingSpaces && (c == ' ' || c == '\r' || c == '\n' || c == '\t')) {
				return this;
			}
			leadingSpaces = false;
			if (startOfLineSpaces && (c == ' ' || c == '\t')) {
				return this;
			}
			startOfLineSpaces = false;
			buf.append(c);
			if (c == '\n') {
				startOfLineSpaces = true;
				buf.append(asterisk);
			}
			return this;
		}

		JavadocBuilder append(String s) {
			for (int i=0; i<s.length(); i++) {
				append(s.charAt(i));
			}
			return this;
		}

		JavadocBuilder appendText(char c) {
			if (c == '<') return append("&lt;");
			if (c == '>') return append("&gt;");
			return append(c);
		}

		JavadocBuilder appendText(String s) {
			for (int i=0; i<s.length(); i++) {
				appendText(s.charAt(i));
			}
			return this;
		}

		@Override
		public String toString() {
			// remove trailing empty lines and spaces
			int i = buf.length()-1;
			while (i >= 0) {
				char c = buf.charAt(i);
				if (c != ' ' && c != '\t' && c != '\n' && c != '\r' && c != '*') break;
				i--;
			}
			buf.setLength(i+1);
			return buf.toString();
		}

		public void setIndent(int indent) {
			this.indent = indent;
			asterisk = StringUtils.repeat(" ", indent) + " * ";
		}
	}
	static class HtmlToPlain implements ExtensionFunction {
		final int indent;
		final String name;

		HtmlToPlain(int indent, String name) {
			this.indent = indent;
			this.name = name;
		}

		@Override
		public QName getName() {
			return new QName(NAMESPACE, name);
		}

		@Override
		public SequenceType getResultType() {
			return SequenceType.makeSequenceType(ItemType.STRING, OccurrenceIndicator.ONE);
		}

		@Override
		public SequenceType[] getArgumentTypes() {
			return new SequenceType[] {
					SequenceType.makeSequenceType(ItemType.ANY_ITEM, OccurrenceIndicator.ZERO_OR_MORE)
			};
		}

		@Override
		public XdmValue call(XdmValue[] arguments) throws SaxonApiException {
			PlainTextBuilder sb = new PlainTextBuilder();
			sb.setIndent(indent);
			for (XdmItem item : arguments[0]) {
				if (item instanceof XdmAtomicValue) sb.append(item.getStringValue());
				else if (item instanceof XdmNode) {
					XdmNode node = (XdmNode) item;
					toPlainText(node, sb);
				}
			}
			String s = sb.toString();
			// make sure the string can be used inside triple-quoted strings in Python
			s = s.replaceAll("^\"", " \"");
			s = s.replaceAll("\"$", "\" ");
			s = s.replaceAll("\"\"\"", "'''");
			return new XdmAtomicValue(s);
		}

		private void toPlainText(XdmNode node, PlainTextBuilder sb) {
			switch (node.getNodeKind()) {
				case ATTRIBUTE:
					break;
				case ELEMENT:
					XdmSequenceIterator it;
					String elementName = node.getNodeName().getLocalName();
					if ("p".equalsIgnoreCase(elementName) || "tr".equalsIgnoreCase(elementName) || "li".equalsIgnoreCase(elementName) || "dt".equalsIgnoreCase(elementName)) {
						if ("tr".equalsIgnoreCase(elementName)) sb.append("\n");
						else sb.append("\013");
						if ("li".equalsIgnoreCase(elementName) || "dt".equalsIgnoreCase(elementName)) sb.append("- ");
						it = node.axisIterator(Axis.CHILD);
						while (it.hasNext()) {
							toPlainText((XdmNode) it.next(), sb);
						}
						if ("tr".equalsIgnoreCase(elementName)) sb.append("\n");
						else sb.append("\013");
					} else if ("br".equalsIgnoreCase(elementName) || "hr".equalsIgnoreCase(elementName)) {
						sb.append("\n");
					} else if ("td".equalsIgnoreCase(elementName) || "th".equalsIgnoreCase(elementName)) {
						// TODO: use https://github.com/iNamik/Java-Text-Table-Formatter
						sb.append("|");
						it = node.axisIterator(Axis.CHILD);
						while (it.hasNext()) {
							toPlainText((XdmNode) it.next(), sb);
						}
						sb.append("|");
					} else {
						it = node.axisIterator(Axis.CHILD);
						while (it.hasNext()) {
							toPlainText((XdmNode) it.next(), sb);
						}
					}
					if ("a".equalsIgnoreCase(elementName)) {
						String href = node.getAttributeValue(new QName("href"));
						if (href != null) {
							sb.append('(').append(href).append(')');
						}
					}
					break;
				case TEXT:
				case DOCUMENT: // for external entities
					sb.appendText(node.getStringValue());
					break;
			}
		}
	}

	static class PlainTextBuilder {
		StringBuilder buf = new StringBuilder();
		boolean leadingSpaces = true;
		boolean startOfLineSpaces = true;
		boolean doubleNL= false;
		StringBuilder entity = null;
		private int indent;

		PlainTextBuilder append(char c) {
			if (entity == null && c == '&') {
				entity = new StringBuilder();
				return this;
			}
			if (entity != null) {
				if (c == ';' || Character.isSpaceChar(c)) {
					buf.append(entityToText(entity.toString()));
					entity = null;
					if (c != ';') return append(c);
					return this;
				}
				entity.append(c);
				return this;
			}
			if (leadingSpaces && (c == ' ' || c == '\r' || c == '\n' || c == '\t')) {
				return this;
			}
			leadingSpaces = false;
			if (startOfLineSpaces) {
				if (c == ' ' || c == '\t') {
					return this;
				}
				if (c == '\n') return this;
				if (c == '\013') {
					if (doubleNL) return this;
				}
			}
			if (c == '\013') {
				doubleNL = true;
				if (!startOfLineSpaces) buf.append('\n');
				c = '\n';
			}
			startOfLineSpaces = false;
			buf.append(c);
			if (c == '\n') {
				startOfLineSpaces = true;
				buf.append(StringUtils.repeat("\t", indent));
			} else {
				doubleNL = false;
			}
			return this;
		}

		static Map<String, String> entities = new HashMap<String, String>();
		static {
			entities.put("amp", "&");
			entities.put("nbsp", " ");
			entities.put("lt", "<");
			entities.put("gt", ">");
			entities.put("cent", "¢");
			entities.put("pound", "£");
			entities.put("yen", "¥");
			entities.put("euro", "€");
			entities.put("copy", "©");
			entities.put("reg", "®");
			entities.put("trade", "™");
			entities.put("quot", "\"");
			entities.put("apos", "'");
		}

		static String entityToText(String entity) {
			if (entity.startsWith("#")) {
				try {
					int tryParse = Integer.parseInt(entity.substring(1));
					return new String(new char[]{(char)tryParse});
				} catch (NumberFormatException nfe) {
					return "";
				}
			}
			String result = entities.get(entity);
			return result == null ? "" : result;
		}

		PlainTextBuilder append(String s) {
			for (int i=0; i<s.length(); i++) {
				append(s.charAt(i));
			}
			return this;
		}

		PlainTextBuilder appendText(char c) {
			if (c == '&') return append("&amp;");
			return append(c);
		}

		PlainTextBuilder appendText(String s) {
			for (int i=0; i<s.length(); i++) {
				appendText(s.charAt(i));
			}
			return this;
		}

		@Override
		public String toString() {
			// remove trailing empty lines and spaces
			int i = buf.length()-1;
			while (i >= 0) {
				char c = buf.charAt(i);
				if (c != ' ' && c != '\t' && c != '\n' && c != '\r' && c != '*') break;
				i--;
			}
			buf.setLength(i+1);
			return buf.toString();
		}

		public void setIndent(int indent) {
			this.indent = indent;
		}
	}

	static class XmlComments implements ExtensionFunction {
		final int indent;
		final String name;

		XmlComments(int indent, String name) {
			this.indent = indent;
			this.name = name;
		}

		@Override
		public QName getName() {
			return new QName(NAMESPACE, name);
		}

		@Override
		public SequenceType getResultType() {
			return SequenceType.makeSequenceType(ItemType.STRING, OccurrenceIndicator.ONE);
		}

		@Override
		public SequenceType[] getArgumentTypes() {
			return new SequenceType[] {
					SequenceType.makeSequenceType(ItemType.ANY_ITEM, OccurrenceIndicator.ZERO_OR_MORE)
			};
		}

		@Override
		public XdmValue call(XdmValue[] arguments) throws SaxonApiException {
			XmlCommentsBuilder sb = new XmlCommentsBuilder();
			sb.setIndent(indent);
			for (XdmItem item : arguments[0]) {
				if (item instanceof XdmAtomicValue) sb.append(item.getStringValue());
				else if (item instanceof XdmNode) {
					XdmNode node = (XdmNode) item;
					toXmlComments(node, sb);
				}
			}
			String s = sb.toString();
			return new XdmAtomicValue(s);
		}

		private void toXmlComments(XdmNode node, XmlCommentsBuilder sb) {
			switch (node.getNodeKind()) {
				case ATTRIBUTE:
					break;
				case ELEMENT:
					XdmSequenceIterator it;
					String elementName = node.getNodeName().getLocalName();
					if ("p".equalsIgnoreCase(elementName) || "tr".equalsIgnoreCase(elementName) || "li".equalsIgnoreCase(elementName) || "dt".equalsIgnoreCase(elementName)) {
						sb.append("<para>");
						if ("li".equalsIgnoreCase(elementName) || "dt".equalsIgnoreCase(elementName)) sb.append("- ");
						it = node.axisIterator(Axis.CHILD);
						while (it.hasNext()) {
							toXmlComments((XdmNode) it.next(), sb);
						}
						sb.append("</para>\n");
					} else if ("br".equalsIgnoreCase(elementName) || "hr".equalsIgnoreCase(elementName)) {
						sb.append("<para/>\n");
					} else if ("td".equalsIgnoreCase(elementName) || "th".equalsIgnoreCase(elementName)) {
						// TODO: use https://github.com/iNamik/Java-Text-Table-Formatter
						sb.append("|");
						it = node.axisIterator(Axis.CHILD);
						while (it.hasNext()) {
							toXmlComments((XdmNode) it.next(), sb);
						}
						sb.append("|");
					} else {
						it = node.axisIterator(Axis.CHILD);
						while (it.hasNext()) {
							toXmlComments((XdmNode) it.next(), sb);
						}
					}
					if ("a".equalsIgnoreCase(elementName)) {
						String href = node.getAttributeValue(new QName("href"));
						if (href != null) {
							sb.append('(').append(href).append(')');
						}
					}
					break;
				case TEXT:
				case DOCUMENT: // for external entities
					sb.appendText(node.getStringValue());
					break;
			}
		}
	}

	static class XmlCommentsBuilder {
		StringBuilder buf = new StringBuilder();
		boolean leadingSpaces = true;
		boolean startOfLineSpaces = true;
		boolean doubleNL= false;
		StringBuilder entity = null;
		private int indent;

		XmlCommentsBuilder append(char c) {
			if (entity == null && c == '&') {
				entity = new StringBuilder();
				return this;
			}
			if (entity != null) {
				if (c == ';' || Character.isSpaceChar(c)) {
					buf.append(entityToText(entity.toString()));
					entity = null;
					if (c != ';') return append(c);
					return this;
				}
				entity.append(c);
				return this;
			}
			if (leadingSpaces && (c == ' ' || c == '\r' || c == '\n' || c == '\t')) {
				return this;
			}
			leadingSpaces = false;
			if (startOfLineSpaces) {
				if (c == ' ' || c == '\t') {
					return this;
				}
				if (c == '\n') return this;
				if (c == '\013') {
					if (doubleNL) return this;
				}
			}
			if (c == '\013') {
				doubleNL = true;
				if (!startOfLineSpaces) buf.append('\n');
				c = '\n';
			}
			startOfLineSpaces = false;
			buf.append(c);
			if (c == '\n') {
				startOfLineSpaces = true;
				buf.append(StringUtils.repeat("    ", indent)+"/// ");
			} else {
				doubleNL = false;
			}
			return this;
		}

		static Map<String, String> entities = new HashMap<String, String>();
		static {
			entities.put("nbsp", " ");
			entities.put("gt", ">");
			entities.put("cent", "¢");
			entities.put("pound", "£");
			entities.put("yen", "¥");
			entities.put("euro", "€");
			entities.put("copy", "©");
			entities.put("reg", "®");
			entities.put("trade", "™");
			entities.put("quot", "\"");
			entities.put("apos", "'");
		}

		static String entityToText(String entity) {
			String result = entities.get(entity);
			return result == null ? "&"+entity+";" : result;
		}

		XmlCommentsBuilder append(String s) {
			for (int i=0; i<s.length(); i++) {
				append(s.charAt(i));
			}
			return this;
		}

		XmlCommentsBuilder appendText(char c) {
			if (c == '&') return append("&amp;");
			if (c == '<') return append("&lt;");
			return append(c);
		}

		XmlCommentsBuilder appendText(String s) {
			for (int i=0; i<s.length(); i++) {
				appendText(s.charAt(i));
			}
			return this;
		}

		@Override
		public String toString() {
			// remove trailing empty lines and spaces
			int i = buf.length()-1;
			while (i >= 0) {
				char c = buf.charAt(i);
				if (c != ' ' && c != '\t' && c != '\n' && c != '\r' && c != '/') break;
				i--;
			}
			buf.setLength(i+1);
			return buf.toString();
		}

		public void setIndent(int indent) {
			this.indent = indent;
		}
	}

	static class Identifier implements ExtensionFunction {
		@Override
		public QName getName() {
			return new QName(NAMESPACE, "identifier");
		}

		@Override
		public SequenceType getResultType() {
			return SequenceType.makeSequenceType(ItemType.STRING, OccurrenceIndicator.ONE);
		}

		@Override
		public SequenceType[] getArgumentTypes() {
			return new SequenceType[]{
					SequenceType.makeSequenceType(ItemType.STRING, OccurrenceIndicator.ONE),
					SequenceType.makeSequenceType(ItemType.STRING, OccurrenceIndicator.ONE),
			};
		}

		@Override
		public XdmValue call(XdmValue[] xdmValues) throws SaxonApiException {
			String stringValue = xdmValues[0].itemAt(0).getStringValue();
			stringValue = stringValue.replaceAll(WRONG_CHAR_PTRN, "_");
			if (stringValue.length() > 0 && stringValue.charAt(0) >= '0' && stringValue.charAt(0) <= '9') {
				stringValue = "_"+stringValue;
			}
			String keywords = xdmValues[1].itemAt(0).getStringValue();
			String[] split = keywords.split(" ");
			for (String kw: split) {
				if (kwequal(kw, stringValue)) {
					stringValue = kwcorrect(stringValue, split);
				}
			}
			return new XdmAtomicValue(stringValue);
		}

		protected String kwcorrect(String stringValue, String[] split) {
			// capitalize the first letter
			return stringValue.substring(0, 1).toUpperCase() + stringValue.substring(1);
		}

		protected boolean kwequal(String kw, String stringValue) {
			return kw.equals(stringValue);
		}
	}

	static class CIIdentifier extends Identifier {
		@Override
		public QName getName() {
			return new QName(NAMESPACE, "ciidentifier");
		}

		@Override
		protected String kwcorrect(String stringValue, String[] split) {
			return "_"+stringValue;
		}

		@Override
		protected boolean kwequal(String kw, String stringValue) {
			return kw.equalsIgnoreCase(stringValue);
		}
	}

	static class HashComment implements ExtensionFunction {
		@Override
		public QName getName() {
			return new QName(NAMESPACE, "hashComment");
		}

		@Override
		public SequenceType getResultType() {
			return SequenceType.makeSequenceType(ItemType.STRING, OccurrenceIndicator.ONE);
		}

		@Override
		public SequenceType[] getArgumentTypes() {
			return new SequenceType[]{
					SequenceType.makeSequenceType(ItemType.STRING, OccurrenceIndicator.ONE)
			};
		}

		@Override
		public XdmValue call(XdmValue[] xdmValues) throws SaxonApiException {
			String stringValue = xdmValues[0].itemAt(0).getStringValue();
			if (stringValue.length() == 0) return new XdmAtomicValue("");
			StringBuilder result = new StringBuilder("# ");
			for (int i=0; i<stringValue.length(); i++) {
				char c = stringValue.charAt(i);
				if (c == '\r') continue;
				result.append(c);
				if (c == '\n') {
					result.append("# ");
				}
			}
			return new XdmAtomicValue(result.toString());
		}
	}
}
