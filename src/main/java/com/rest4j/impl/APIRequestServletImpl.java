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
import com.rest4j.ApiRequest;
import com.rest4j.json.JSONArray;
import com.rest4j.json.JSONException;
import com.rest4j.json.JSONObject;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class ApiRequestServletImpl extends ApiRequest {
	private final HttpServletRequest request;

	public ApiRequestServletImpl(HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public String method() {
		return request.getMethod();
	}

	@Override
	public String path() {
		return request.getRequestURI();
	}

	@Override
	public String param(String name) {
		return request.getParameter(name);
	}

	@Override
	public String header(String name) {
		return request.getHeader(name);
	}

	@Override
	public JSONObject objectInput() throws IOException, ApiException {
		if (request.getContentType() != null &&
				!"application/json".equals(request.getContentType()) &&
				!"text/json".equals(request.getContentType())) {
			throw new ApiException("Unsupported content-type: expected either application/json or text/json").setHttpStatus(415);
		}
		String json = IOUtils.toString(request.getReader());
		try {
			return new JSONObject(json);
		} catch (JSONException e) {
			throw new ApiException("Wrong JSON format: "+e.getMessage());
		}
	}

	@Override
	public JSONArray arrayInput() throws IOException, ApiException {
		if (request.getContentType() != null &&
				!"application/json".equals(request.getContentType()) &&
				!"text/json".equals(request.getContentType())) {
			throw new ApiException("Unsupported content-type: expected either application/json or text/json").setHttpStatus(415);
		}
		String json = IOUtils.toString(request.getReader());
		try {
			return new JSONArray(json);
		} catch (JSONException e) {
			throw new ApiException("Wrong JSON format: "+e.getMessage());
		}
	}

	@Override
	public InputStream binaryInput() throws IOException, ApiException {
		return request.getInputStream();
	}

	@Override
	public Reader textInput() throws IOException, ApiException {
		return request.getReader();
	}

	@Override
	public boolean https() {
		return request.isSecure();
	}
}
