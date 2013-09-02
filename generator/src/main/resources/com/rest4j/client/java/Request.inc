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
package com.rest4j.client;

import org.apache.http.HttpEntity;
import org.json.JSONException;

import java.io.IOException;
import java.net.URI;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public abstract class Request<T> {
	final protected URI uri;
	final protected HttpEntity body;

	public Request(URI uri) {
		this.uri = uri;
		this.body = null;
	}

	public Request(URI uri, HttpEntity body) {
		this.uri = uri;
		this.body = body;
	}

	public URI getUri() {
		return uri;
	}

	public HttpEntity getBody() {
		return body;
	}

	public abstract T execute() throws IOException, JSONException;
}
