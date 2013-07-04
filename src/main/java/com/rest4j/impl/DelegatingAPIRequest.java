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
import com.rest4j.APIRequest;
import com.rest4j.json.JSONArray;
import com.rest4j.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class DelegatingAPIRequest extends APIRequest {
	APIRequest peer;

	public DelegatingAPIRequest(APIRequest peer) {
		this.peer = peer;
	}

	@Override
	public String method() {
		return peer.method();
	}

	@Override
	public String path() {
		return peer.path();
	}

	@Override
	public String param(String name) {
		return peer.param(name);
	}

	@Override
	public String header(String name) {
		return peer.header(name);
	}

	@Override
	public JSONObject objectInput() throws IOException, ApiException {
		return peer.objectInput();
	}

	@Override
	public JSONArray arrayInput() throws IOException, ApiException {
		return peer.arrayInput();
	}

	@Override
	public InputStream binaryInput() throws IOException, ApiException {
		return peer.binaryInput();
	}

	@Override
	public Reader textInput() throws IOException, ApiException {
		return peer.textInput();
	}

	@Override
	public boolean https() {
		return peer.https();
	}
}
