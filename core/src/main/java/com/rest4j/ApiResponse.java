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

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Encapsulates the response of the RESTful request. This object is returned by the {@link API#serve(ApiRequest)}
 * on success and {@link com.rest4j.ApiException#createResponse()} on error.
 *
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public interface ApiResponse {

	/**
	 * HTTP status code.
	 */
	int getStatus();

	/**
	 * An optional resource that will be returned to the client.
	 */
	Resource getResource();

	/**
	 * Outputs the response to the given HttpServletResponse. This method handles
	 * format=pretty, JSONP, gzip compression, Etags, custom headers.
	 */
	void outputBody(HttpServletResponse response) throws IOException;
}
