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

import com.rest4j.impl.JSONEscapingFilter;
import com.rest4j.impl.ResourceBase;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;

/**
 * A {@link Resource} that represents plain text. Has Content-Type: text/plain; charset=utf-8
 * and an Etag based on hashCode. You don't usually have to create this object.
 *
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class TextResource extends ResourceBase {
	String etag;
	Reader reader;

	public TextResource(String etag, Reader reader) {
		super("text/plain; charset=utf-8");
		this.etag = etag;
		this.reader = reader;
	}

	public TextResource(String text) {
		super("text/plain; charset=utf-8");
		this.etag = "\""+text.hashCode()+"\"";
		this.reader = new StringReader(text);
	}

	@Override
	public String getETag() {
		return etag;
	}

	@Override
	public void write(OutputStream os) throws IOException {
		IOUtils.copy(reader, os, "UTF-8");
	}

	@Override
	/**
	 * A JSONP representation of a text resource is a JavaScript function call
	 * with string parameter containing the text data.
	 */
	public void writeJSONP(OutputStream os, String callbackFunctionName) throws IOException {
		os.write((callbackFunctionName+"(\"").getBytes("UTF-8"));
		JSONEscapingFilter filter = new JSONEscapingFilter(reader);
		IOUtils.copy(filter, os, "UTF-8");
		os.write('\"');
		os.write(')');
	}
}
