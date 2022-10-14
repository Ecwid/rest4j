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

import com.rest4j.*;
import org.apache.commons.lang.StringUtils;

import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class ApiResponseImpl implements ApiResponse {
	boolean prettify;
	String callbackFunctionName = null;
	int status = 200;
	String statusMessage;
	Headers headers = new Headers();
	Resource response;
	boolean compress;
	boolean addEtag;

	public ApiResponseImpl(APIImpl api, ApiRequest request, Resource response) {
		this.prettify = api.getParams().isPrettifyByDefault() == null ? false : api.getParams().isPrettifyByDefault();
		String param = api.getParams().getPrettifyParam();
		if (param != null) {
			int pos = param.indexOf('=');
			if (pos == -1 && request.param(param) != null) {
				prettify = !prettify;
			} else {
				String prettifyParamName = param.substring(0, pos);
				String val = request.param(prettifyParamName);
				if (val != null && val.matches(param.substring(pos+1))) {
					prettify = !prettify;
				}
			}
		}
		this.response = response;
		if (api.getParams().getJsonpParamName() != null) {
			this.callbackFunctionName = request.param(api.getParams().getJsonpParamName());
		}
		if (response != null && response.headers() != null) {
			for (Header header: response.headers())
				headers.addHeader(header.getName(), header.getValue());
		}
		compress = StringUtils.containsIgnoreCase(request.header("Accept-Encoding"), "gzip");
		addEtag = request.method().equals("GET");
	}

	@Override
	public int getStatus() {
		return status;
	}

	public ApiResponseImpl setStatus(int status, String statusMessage) {
		this.status = status;
		this.statusMessage = statusMessage;
		return this;
	}

	public ApiResponseImpl setStatus(int status) {
		this.status = status;
		this.statusMessage = null;
		return this;
	}

	public ApiResponseImpl addHeader(String name, String value) {
		headers.addHeader(name, value);
		return this;
	}

	@Override
	public Resource getResource() {
		return response;
	}

	@Override
	public void outputBody(HttpServletResponse response) throws IOException {
		response.setStatus(status);
		headers.outputHeaders(response);
		if (this.response == null) return;
		response.addHeader("Content-type", this.response.getContentType());
		if (addEtag) {
			String etag = this.response.getETag();
			if (etag != null) response.addHeader("ETag", etag);
		}

		OutputStream outputStream;
		byte[] resourceBytes = ((JSONResource) this.response).getJSONObject().toString().getBytes();
		int contentLength = resourceBytes.length;
		if (compress) {
			response.addHeader("Content-encoding", "gzip");
			ByteArrayOutputStream outputByteStream = new ByteArrayOutputStream();
			GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputByteStream);
			gzipOutputStream.write(resourceBytes);
			gzipOutputStream.finish(); // финиш нужен чтобы результат закрепился!
			contentLength = outputByteStream.toByteArray().length;
			gzipOutputStream.close();
			outputByteStream.close();

			outputStream = new GZIPOutputStream(response.getOutputStream());
		} else {
			outputStream = response.getOutputStream();
		}
		response.addHeader("Content-Length", String.valueOf(contentLength));

		if (this.response instanceof JSONResource) {
			((JSONResource)this.response).setPrettify(prettify);
		}
		if (callbackFunctionName == null) {
			this.response.write(outputStream);
		} else {
			this.response.writeJSONP(outputStream, callbackFunctionName);
		}
		outputStream.close();
	}

}
