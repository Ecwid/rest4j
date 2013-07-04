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

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public abstract class APIRequest {
	public abstract String method();
	public abstract String path();
	public abstract String param(String name);
	public abstract String header(String name);
	public abstract JSONObject objectInput() throws IOException, ApiException;
	public abstract JSONArray arrayInput() throws IOException, ApiException;
	public abstract InputStream binaryInput() throws IOException, ApiException;
	public abstract Reader textInput() throws IOException, ApiException;
	public abstract boolean https();

	public static APIRequest from(HttpServletRequest request) {
		return new ApiRequestServletImpl(request);
	}

}
