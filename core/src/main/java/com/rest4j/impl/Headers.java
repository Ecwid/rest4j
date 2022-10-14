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

import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class Headers {
	List<Header> headers = new ArrayList<Header>();

	public void addHeader(String name, String value) {
		Header h = new Header();
		h.name = name;
		h.value = value;
		headers.add(h);
	}

	public void outputHeaders(HttpServletResponse response) {
		for (Header h: headers) {
			response.setHeader(h.name, h.value);
		}
	}

	public String getHeader(String name) {
		for (Header hdr: headers) {
			if (hdr.name.equals(name)) return hdr.value;
		}
		return null;
	}

	public Iterable<com.rest4j.Header> headers() {
		return (Iterable)headers;
	}

	static class Header implements com.rest4j.Header {
		String name;
		String value;

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getValue() {
			return value;
		}
	}
}
