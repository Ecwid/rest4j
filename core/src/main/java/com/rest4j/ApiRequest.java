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

package com.rest4j;

import com.rest4j.impl.ApiRequestServletImpl;
import com.rest4j.json.JSONArray;
import com.rest4j.json.JSONObject;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Encapsulates the API request. You usually create instances with {@link ApiRequest#from(jakarta.servlet.http.HttpServletRequest)}.
 * It is OK to wrap ApiRequests with your custom logic - {@link com.rest4j.impl.DelegatingApiRequest}.
 * This object is passed to {@link API#serve(ApiRequest)} by a servlet or a Spring controller.
 *
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public abstract class ApiRequest {
	/**
	 * Return HTTP method, capital letters (e.g. 'GET', 'POST').
	 */
	public abstract String method();

	/**
	 * Return the query path (query before the '?' character)
	 */
	public abstract String path();

	/**
	 * Return the querystring parameter value. The querystring (the part of the query after the '?') should be
	 * urlform-encoded using UTF-8 encoding. Return null if the parameter name was not found in the querystring.
	 *
	 * @param name Query string parameter name
	 */
	public abstract String param(String name);

	/**
	 * Return a set of querystring parameter names. Empty set if there is no querystring.
	 */
	public abstract Iterable<String> paramNames();

	/**
	 * Return the HTTP request header with the given name, case insensitive.
	 * @param name The HTTP request header name, case insensitive.
	 */
	public abstract String header(String name);

	/**
	 * Return the JSON object that was sent as the request body (e.g. using 'PUT' or 'POST' methods).
	 * This method is called only once, when Rest4j expects JSON object in the input.
	 * The default implementations calls getReader on the HttpServletRequest.
	 *
	 * @throws ApiException If the body is not in the JSON object format.
	 */
	public abstract JSONObject objectInput() throws IOException, ApiException;

	/**
	 * Return the JSON array that was sent as the request body (e.g. using 'PUT' or 'POST' methods).
	 * This method is called only once, when Rest4j expects JSON array in the input.
	 * The default implementations calls getReader on the HttpServletRequest.
	 *
	 * @throws ApiException If the body is not in the JSON array format.
	 */
	public abstract JSONArray arrayInput() throws IOException, ApiException;

	/**
	 * Return the request body as binary stream. This method is called only once, when Rest4j expects
	 * the request body in the binary form (&lt;binary> tag in the API XML description). null if there is no
	 * request body. The default implementations calls getInputStream on the HttpServletRequest.
	 *
	 */
	public abstract InputStream binaryInput() throws IOException, ApiException;

	/**
	 * Return the request body as test. This method is called only once, when Rest4j expects
	 * the request body in the text form (&lt;text> tag in the API XML description). null if there is no
	 * request body. The default implementations calls getReader on the HttpServletRequest.
	 *
	 */
	public abstract Reader textInput() throws IOException, ApiException;

	/**
	 * Return true if the request was made by a secure connection. The default implementations calls
	 * HttpServletRequest.isSecure().
	 */
	public abstract boolean https();

	/**
	 * Create the default ApiRequest from a given HttpServletRequest.
	 */
	public static ApiRequest from(HttpServletRequest request) {
		return new ApiRequestServletImpl(request);
	}

}
