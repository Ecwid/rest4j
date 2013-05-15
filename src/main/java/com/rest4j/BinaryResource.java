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

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class BinaryResource implements Resource {

	private String contentType;
	private String etag;
	private InputStream is;

	public BinaryResource(String contentType, String etag, InputStream is) {
		this.contentType = contentType;
		this.etag = etag;
		this.is = is;
	}

	public BinaryResource(byte[] content) {
		this.contentType = "application/octet-stream";
		this.is = new ByteArrayInputStream(content);
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public String getETag() {
		return etag;
	}

	@Override
	public void write(OutputStream os) throws IOException {
		IOUtils.copy(is, os);
	}

	@Override
	public void writeJSONP(OutputStream os, String callbackFunctionName) throws IOException {
		write(os);
	}
}
