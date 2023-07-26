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

import com.rest4j.impl.Headers;

/**
 * This is thrown from {@link API#serve(ApiRequest)} to indicate that there was an error.
 * This class contains the HTTP status code and message that should be sent to the client.
 * It can also optionally contain the JSON body that should be returned and HTTP headers.
 * <p/>
 * Throw this exception from your service code, field mappers or converters.
 *
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class ApiException extends Exception {
	int status = 400;
	Headers headers = new Headers();
	JSONResource jsonResponse;

	public ApiException(String message) {
		super(message);
	}

	public ApiException(String message, Throwable cause) {
		super(message, cause);
	}

	public ApiException(String message, JSONResource jsonResponse) {
		this(message);
		this.jsonResponse = jsonResponse;
	}

	public ApiException(String message, JSONResource jsonResponse, Throwable cause) {
		this(message, cause);
		this.jsonResponse = jsonResponse;
	}

	/**
	 * @return An optional JSON that should be returned to the client.
	 */
	public JSONResource getJSONResponse() {
		return jsonResponse;
	}

	/**
	 * @return HTTP status code. The default is 400.
	 */
	public int getHttpStatus() {
		return status;
	}

	/**
	 * Create the copy of this instance with different error message.
	 * The exception's message cannot be changed, because there is no such setter.
	 * But you can create another instance of the exception with different message text.
	 * @param newMessage The message text that should be used in the new exception instance.
	 * @return The new exception instance with everything like in this instance but the error message.
	 */
	public ApiException replaceMessage(String newMessage) {
		ApiException ex = new ApiException(newMessage);
		ex.status = status;
		ex.headers = headers;
		return ex;
	}

	/**
	 * Sets the status code that should be returned to the customer. The default code is 400.
	 * @param status New HTTP status
	 * @return this instance, so that the calls can be chained
	 */
	public ApiException setHttpStatus(int status) {
		this.status = status;
		return this;
	}

	/**
	 * Adds a custom HTTP response header. You can add several headers with the same name.
	 * @param name HTTP header name
	 * @param value HTTP header value
	 * @return this instance, so that the calls can be chained
	 */
	public ApiException addHeader(String name, String value) {
		headers.addHeader(name, value);
		return this;
	}

	/**
	 * Creates the ApiResponse from this exception. This is usually done in your custom servlet/controller code.
	 * Example:
	 * <pre>
	 * ApiResponse response;
	 * try {
	 *   response = api.serve(ApiRequest.from(httpServletRequest));
	 * } catch (ApiException e) {
	 *   <b>response = e.createResponse();</b>
	 * }
	 * response.outputBody(httpServletResponse);
	 * </pre>
	 * @return
	 */
	public ApiResponse createResponse() {
		throw new IllegalArgumentException("This exception was not thrown from API.serve(); cannot createResponse()");
	}

	/**
	 * Returns HTTP header value that was previously set with {@link #addHeader}.
	 *
	 * @param name HTTP response header name
	 * @return The HTTP header value that was previously set with {@link #addHeader} or null if not found. If several
	 * headers were added under the given name, the valu eof the first header is returned.
	 */
	public String getHeader(String name) {
		return headers.getHeader(name);
	}
}
