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

import com.rest4j.APIException;
import com.rest4j.APIRequest;
import com.rest4j.APIResponse;
import com.rest4j.JSONResource;
import org.json.JSONObject;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
class APIExceptionWrapper extends APIException {
	APIImpl api;
	APIRequest request;
	APIException ex;

	APIExceptionWrapper(APIImpl api, APIRequest request, APIException ex) {
		super(ex.getStatus(), ex.getMessage());
		this.api = api;
		this.request = request;
	}

	@Override
	public int getStatus() {
		return ex.getStatus();
	}

	@Override
	public JSONObject getJSONResponse() {
		return ex.getJSONResponse();
	}

	@Override
	public APIException replaceMessage(String newMessage) {
		throw new IllegalStateException();
	}

	@Override
	public APIResponse createResponse() {
		JSONObject jsonResponse = getJSONResponse();
		return new APIResponseImpl(api, request, jsonResponse == null ? null : new JSONResource(jsonResponse))
				.setStatus(getStatus(), getMessage())
				.addHeader("Cache-control", "must-revalidate,no-cache,no-store");
	}
}
