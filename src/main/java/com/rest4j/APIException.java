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
import org.json.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class APIException extends Exception {
	private final int status;
	Headers headers = new Headers();
	JSONObject jsonResponse;

	public APIException(int status, String message) {
		super(message);
		this.status = status;
	}

	public APIException(int status, String message, JSONObject jsonResponse) {
		this(status, message);
		this.jsonResponse = jsonResponse;
	}

	public JSONObject getJSONResponse() {
		return jsonResponse;
	}

	public int getStatus() {
		return status;
	}

	public APIException replaceMessage(String newMessage) {
		APIException ex = new APIException(status, newMessage);
		ex.headers = headers;
		return ex;
	}

	public APIException addHeader(String name, String value) {
		headers.addHeader(name, value);
		return this;
	}

	public void outputHeaders(HttpServletResponse response) throws IOException {
		response.sendError(status, getMessage());
		headers.outputHeaders(response);
	}

	public String getHeader(String name) {
		return headers.getHeader(name);
	}

}
