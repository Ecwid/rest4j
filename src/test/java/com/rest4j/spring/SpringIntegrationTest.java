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

package com.rest4j.spring;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.mvc.Controller;

import static org.junit.Assert.assertEquals;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class SpringIntegrationTest {

	private ClassPathXmlApplicationContext context;
	private Controller controller;

	@Before
	public void init() {
		context = new ClassPathXmlApplicationContext("com/rest4j/spring/test-context.xml");
		controller = (org.springframework.web.servlet.mvc.Controller) context.getBean("controller");
	}

	@Test
	public void testCreateContext() throws Exception {
	}

	@Test
	public void testHandleRequest_success() throws Exception {
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		mockRequest.setMethod("GET");
		mockRequest.setRequestURI("/api/v2/pets");
		mockRequest.setParameter("type", "cat");
		MockHttpServletResponse mockResponse = new MockHttpServletResponse();
		controller.handleRequest(mockRequest, mockResponse);

		assertEquals(200, mockResponse.getStatus());
		String response = new String(mockResponse.getContentAsByteArray(), "UTF-8");
		JSONArray pets = new JSONArray(response);
		assertEquals(1, pets.length());
		assertEquals("cat", pets.getJSONObject(0).getString("type"));
	}

	@Test
	public void testHandleRequest_exception() throws Exception {
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		mockRequest.setMethod("GET");
		mockRequest.setRequestURI("/api/v2/pets");
		// no type parameter
		MockHttpServletResponse mockResponse = new MockHttpServletResponse();
		controller.handleRequest(mockRequest, mockResponse);

		assertEquals(400, mockResponse.getStatus());
		String response = new String(mockResponse.getContentAsByteArray(), "UTF-8");
		JSONObject err = new JSONObject(response);
		assertEquals("type", err.getString("field"));
	}
}
