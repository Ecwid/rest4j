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
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class ApiException extends Exception {
	int status = 400;
	Headers headers = new Headers();
	JSONResource jsonResponse;

	public ApiException(String message) {
		super(message);
		this.status = status;
	}

	public ApiException(String message, JSONResource jsonResponse) {
		this(message);
		this.jsonResponse = jsonResponse;
	}

	public JSONResource getJSONResponse() {
		return jsonResponse;
	}

	public int getHttpStatus() {
		return status;
	}

	public ApiException replaceMessage(String newMessage) {
		ApiException ex = new ApiException(newMessage);
		ex.status = status;
		ex.headers = headers;
		return ex;
	}

	public ApiException setHttpStatus(int status) {
		this.status = status;
		return this;
	}

	public ApiException addHeader(String name, String value) {
		headers.addHeader(name, value);
		return this;
	}

	public APIResponse createResponse() {
		throw new IllegalArgumentException("This exception was not thrown from API.serve(); cannot createResponse()");
	}

	public String getHeader(String name) {
		return headers.getHeader(name);
	}
}
