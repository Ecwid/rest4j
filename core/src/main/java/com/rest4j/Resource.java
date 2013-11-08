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
import java.io.OutputStream;

/**
 * HTTP Response Entity. Part of {@link ApiResponse}. May have Content-Type, Etag and headers.
 *
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public interface Resource {

	/**
	 * The Content-Type header value.
	 */
	String getContentType();

	/**
	 * The Etag header value. Can be null.
	 */
	String getETag();

	/**
	 * Writes this resource to the output stream.
	 */
	void write(OutputStream os) throws IOException;

	/**
	 * Writes this resource to the output stream as a JSONP.
	 */
	void writeJSONP(OutputStream os, String callbackFunctionName) throws IOException;

	/**
	 * Returns HTTP additional response headers.
	 */
	Iterable<Header> headers();
}
