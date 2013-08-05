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

import java.io.IOException;
import java.util.List;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public interface API {
	/**
	 * Performs RESTful request on a given ApiRequest.
	 *
	 * @param request
	 * @return Either JSONArray or JSONObject. Null when the request should not have a body.
	 * @throws IOException On error reading the request
	 * @throws ApiException Any business-related error (including those propagated from Service)
	 */
	ApiResponse serve(ApiRequest request) throws IOException, ApiException;

	ApiResponse createApiResponse(ApiRequest request, Resource resource);

	List<String> getAllowedMethods(ApiRequest request) throws IOException, ApiException;

	Marshaller getMarshaller();

	String getPathPrefix();
}
