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
 * The main facade class for the Rest4j framework. The instance should be created with the {@link com.rest4j.ApiFactory#createAPI()} call
 * or, when using Spring, by adding the {@link com.rest4j.spring.APIFactoryBean} to your app context.
 *
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public interface API {
	/**
	 * Performs RESTful request on a given ApiRequest. This should be called from your servlet or Spring controller.
	 * Note that the built-in {@link com.rest4j.spring.Controller} and {@link com.rest4j.servlet.APIServlet}
	 * utility classes already do that job for you. But you are free to implement you own controller/servlet
	 * code that calls serve().
	 *
	 * @param request The request, usually obtained with {@link ApiRequest#from(javax.servlet.http.HttpServletRequest)}
	 * @return An encapsulated {@link Resource}. Null when the request should not have a body.
	 * @throws IOException On error reading the request
	 * @throws ApiException Any business-related error (including those propagated from Service). This kind
	 * of exception may contain JSON body, describing the error. To obtain this body, call {@link ApiException#createResponse()}.
	 */
	ApiResponse serve(ApiRequest request) throws IOException, ApiException;

	/**
	 * Use this method to create an {@link ApiResponse} in your custom API wrappers.
	 *
	 * @param request The original request.
	 * @param resource The response body; can be null.
	 * @return The ApiResponse, which can be further modified by calling methods like addHeader().
	 */
	ApiResponse createApiResponse(ApiRequest request, Resource resource);

	/**
	 * Determine the allowed HTTP methods (e.g. 'GET', 'POST', etc) for the given web resource.
	 * @param request The request encapsulating the resource in question.
	 * @return A list of HTTP method names, never null. If the resource is not found,
	 * returns an empty list.
	 */
	List<String> getAllowedMethods(ApiRequest request) throws IOException, ApiException;

	/**
	 * @return The marshaller object that can be used to marshal/unmarshal data.
	 */
	Marshaller getMarshaller();

	/**
	 * @return The prefix that should be added to every endpoint/route in the XML description.
	 */
	String getPathPrefix();
}
