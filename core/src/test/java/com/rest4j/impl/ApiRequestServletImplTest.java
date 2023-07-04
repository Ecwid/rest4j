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

import com.rest4j.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class ApiRequestServletImplTest {

	ApiRequestServletImpl impl;

	@Test public void testCheckJSON() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		impl  = new ApiRequestServletImpl(request);
		request.setContentType("application/json");
		impl.checkJSON();
		request.setContentType("text/json");
		impl.checkJSON();
		request.setContentType("text/json; charset=utf-8");
		impl.checkJSON();
		request.setContentType("text/plain; charset=utf-8");
		try {
			impl.checkJSON();
			fail();
		} catch (ApiException ex) {
			assertEquals(415, ex.getHttpStatus());
		}
	}

	@Test
	public void testObjectInput_givenEmptyObjectInput_whenReaderReturnsEmptyString_thenReturnsNull() throws IOException, ApiException {
		MockHttpServletRequest request = mock();
		impl  = new ApiRequestServletImpl(request);
		impl.objectInput = null;
		when(request.getReader()).thenReturn(new BufferedReader(new StringReader("")));

		assertNull(impl.objectInput());
	}

	@Test
	public void testArrayInput_givenEmptyArrayInput_whenReaderReturnsEmptyString_thenReturnsNull() throws IOException, ApiException {
		MockHttpServletRequest request = mock();
		impl  = new ApiRequestServletImpl(request);
		impl.arrayInput = null;
		when(request.getReader()).thenReturn(new BufferedReader(new StringReader("")));

		assertNull(impl.arrayInput());
	}
}
