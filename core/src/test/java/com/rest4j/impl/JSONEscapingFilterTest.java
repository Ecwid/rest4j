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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class JSONEscapingFilterTest {
	StringReader reader = new StringReader("\n\r\t \b\f</script>\u0080\\üöäÄÜÖß");
	JSONEscapingFilter filter = new JSONEscapingFilter(reader);
	char[] cbuf;

	@BeforeEach public void init() {
		cbuf = new char[100];
		Arrays.fill(cbuf, '=');
	}

	@Test public void testAllInOneRead_underflow() throws Exception {
		filter.read(cbuf, 1, 99);
		assertEquals("=\\n\\r\\t \\b\\f<\\/script>\\u0080\\\\üöäÄÜÖß===============================================================", new String(cbuf));
	}

	@Test public void testAllInOneRead_overflow() throws Exception {
		filter.read(cbuf, 1, 10);
		assertEquals("=\\n\\r\\t \\b\\=========================================================================================", new String(cbuf));
	}

	@Test public void testOneByOne() throws Exception {
		StringBuilder read = new StringBuilder();
		int c;
		while ((c = filter.read()) != -1) {
			read.append((char)c);
		}
		assertEquals("\\n\\r\\t \\b\\f<\\/script>\\u0080\\\\üöäÄÜÖß", read.toString());
		assertEquals(-1, filter.read());
	}

	@Test public void testMixed() throws Exception {
		StringBuilder read = new StringBuilder();
		int c;
		while ((c = filter.read()) != -1) {
			read.append((char)c);
			char[] buf = new char[2];
			int readCount = filter.read(buf, 0, 2);
			read.append(buf, 0, readCount);
		}
		assertEquals("\\n\\r\\t \\b\\f<\\/script>\\u0080\\\\üöäÄÜÖß", read.toString());
	}
	
	@Test public void testExpand() throws Exception {
		reader = new StringReader("\u0080\u0080\u0080\u0080\u0080\u0080\u0080\u0080\u0080\u0080\u0080\u0080\u0080\u0080\u0080\u0080");
		filter = new JSONEscapingFilter(reader);
		filter.ring = new char[1];
		cbuf = new char[16];
		StringBuilder read = new StringBuilder();
		for (int i=0; i<6; i++) {
			assertEquals(16, filter.read(cbuf));
			read.append(cbuf);
		}
		assertEquals(-1, filter.read(cbuf));
		assertEquals("\\u0080\\u0080\\u0080\\u0080\\u0080\\u0080\\u0080\\u0080\\u0080\\u0080\\u0080\\u0080\\u0080\\u0080\\u0080\\u0080", read.toString());
	}
}
