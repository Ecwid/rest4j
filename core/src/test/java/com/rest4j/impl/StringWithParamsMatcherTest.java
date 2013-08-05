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

import com.rest4j.impl.model.StringWithParams;
import org.junit.Test;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class StringWithParamsMatcherTest {

	@Test
	public void testMatches_noparams() {
		StringWithParams pattern = new StringWithParams();
		pattern.getContent().add("/path");
		StringWithParamsMatcher matcher = new StringWithParamsMatcher(pattern);
		assertTrue(matcher.matches("/path"));
		assertFalse(matcher.matches("/path1"));
		assertEquals(Collections.emptyMap(), matcher.match("/path"));
	}

	@Test
	public void testMatches_two_params() {
		StringWithParams pattern = new StringWithParams();
		pattern.getContent().add("/");
		pattern.getContent().add(new JAXBElement<String>(new QName("param"), String.class, "string"));
		pattern.getContent().add("/");
		pattern.getContent().add(new JAXBElement<String>(new QName("param"), String.class, "number"));

		StringWithParamsMatcher matcher = new StringWithParamsMatcher(pattern);
		assertTrue(matcher.matches("/product/123"));
		Map<String,String> params = new HashMap<String, String>();
		params.put("string", "product");
		params.put("number", "123");
		assertEquals(params, matcher.match("/product/123"));
	}
}
