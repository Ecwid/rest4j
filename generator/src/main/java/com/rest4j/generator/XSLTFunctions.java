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

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class XSLTFunctions {

	public static final String NAMESPACE = "http://rest4j.com/func";

	public static ExtensionFunction[] functions() {
		return new ExtensionFunction[] {
				new CamelCase(),
				new Quote(),
				new JavadocEscape(),
				new ParamNameAsIdentifier(),
				new Singular()
		};
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
			String first = ((XdmAtomicValue)arguments[0].itemAt(0)).getStringValue();
			String second = ((XdmAtomicValue)arguments[1].itemAt(0)).getStringValue();
			second = second.replaceAll("[^a-zA-Z0-9_]", "_");
			return new XdmAtomicValue(first + StringUtils.capitalize(second));
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
			String arg = ((XdmAtomicValue)arguments[0].itemAt(0)).getStringValue();
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
			String arg = ((XdmAtomicValue)arguments[0].itemAt(0)).getStringValue();
			if (arg.endsWith("ies")) arg = arg.substring(0, arg.length()-3)+"y";
			else if (arg.endsWith("s")) arg = arg.substring(0, arg.length()-1);
			return new XdmAtomicValue(arg);
		}
	}

	static class JavadocEscape implements ExtensionFunction {
		@Override
		public QName getName() {
			return new QName(NAMESPACE, "javadocEscape");
		}

		@Override
		public SequenceType getResultType() {
			return SequenceType.makeSequenceType(ItemType.STRING, OccurrenceIndicator.ONE);
		}

		@Override
		public SequenceType[] getArgumentTypes() {
			return new SequenceType[] {SequenceType.makeSequenceType(ItemType.ANY_ITEM, OccurrenceIndicator.ZERO_OR_MORE)};
		}

		@Override
		public XdmValue call(XdmValue[] arguments) throws SaxonApiException {
			JavadocBuilder sb = new JavadocBuilder();
			for (XdmItem item:arguments[0]) {
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
					sb.appendText(node.getStringValue());
					break;
			}
		}
	}

	static class JavadocBuilder {
		StringBuilder buf = new StringBuilder();
		boolean leadingSpaces = true;
		boolean startOfLineSpaces = true;

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
				buf.append("     * ");
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
	}

	static class ParamNameAsIdentifier implements ExtensionFunction {
		@Override
		public QName getName() {
			return new QName(NAMESPACE, "paramNameAsIdentifier");
		}

		@Override
		public SequenceType getResultType() {
			return SequenceType.makeSequenceType(ItemType.STRING, OccurrenceIndicator.ONE);
		}

		@Override
		public SequenceType[] getArgumentTypes() {
			return new SequenceType[]{SequenceType.makeSequenceType(ItemType.STRING, OccurrenceIndicator.ONE)};
		}

		@Override
		public XdmValue call(XdmValue[] xdmValues) throws SaxonApiException {
			String stringValue = ((XdmAtomicValue) xdmValues[0]).itemAt(0).getStringValue();
			stringValue = stringValue.replace('-', '_').replace(' ', '_');
			return new XdmAtomicValue(stringValue);
		}
	}
}
