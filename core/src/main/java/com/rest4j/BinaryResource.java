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

import com.rest4j.impl.ResourceBase;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A {@link Resource} that represents a binary entity. Can have a custom Etag and Content-Type.
 *
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class BinaryResource extends ResourceBase {

	private String etag;
	private InputStream is;

	public BinaryResource(String contentType, String etag, InputStream is) {
		super(contentType);
		this.etag = etag;
		if (is == null) throw new NullPointerException();
		this.is = is;
	}

	/**
	 * Create an entity containing the given binary data with Content-Type: application/octet-stream.
	 */
	public BinaryResource(byte[] content) {
		super("application/octet-stream");
		this.is = new ByteArrayInputStream(content);
	}

	@Override
	public String getETag() {
		return etag;
	}

	@Override
	public void write(OutputStream os) throws IOException {
		IOUtils.copy(is, os);
		is.close();
	}

	@Override
	public void writeJSONP(OutputStream os, String callbackFunctionName) throws IOException {
		write(os);
	}
}
