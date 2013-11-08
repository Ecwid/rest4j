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
import com.rest4j.json.JSONArray;
import com.rest4j.json.JSONException;
import com.rest4j.json.JSONObject;
import com.rest4j.type.ApiType;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * A {@link Resource} that represents a JSON object or an array. Has Content-Type: application/json; charset=utf-8
 * and an Etag based on hashCode. You don't usually have to create this object.
 *
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class JSONResource extends ResourceBase {
	Object object;
	ApiType apiType;
	private boolean prettify;

	/**
	 * Constructs the JSON resource with the given JSON data and API type. You don't usually have to
	 * create this object.
	 *
	 * @param object JSONObject or JSONArray.
	 * @param apiType One of ObjectApiType, ArrayApiType, or MapApiType, which will be available via {@link #getApiType()}.
	 * This is for information only and can be set to null in most cases.
	 */
	public JSONResource(Object object, ApiType apiType) {
		super("application/json; charset=utf-8");
		this.object = object;
		this.apiType = apiType;
	}

	/**
	 * One of ObjectApiType, ArrayApiType, or MapApiType that was passed to the constructor.
	 * @return
	 */
	public ApiType getApiType() {
		return apiType;
	}

	@Override
	public String getETag() {
		return "W/\""+hashCode(object)+"\"";
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(outputStream);
		try {
			if (object instanceof JSONObject) {
				((JSONObject) object).write(writer, isPrettify() ? 2 : 0);
			} else {
				((JSONArray) object).write(writer, isPrettify() ? 2 : 0);
			}
			writer.flush();
		} catch (JSONException ex) {
			if (ex.getCause() instanceof IOException) {
				throw (IOException)ex.getCause();
			}
		}
	}

	@Override
	public void writeJSONP(OutputStream os, String callbackFunctionName) throws IOException {
		os.write((callbackFunctionName+"(").getBytes("UTF-8"));
		write(os);
		os.write(')');
	}

	public Object getJSONObject() {
		return object;
	}

	private static int hashCode(Object obj) {
		if (obj == null) return 0;
		return obj.hashCode();
	}


	public void setPrettify(boolean prettify) {
		this.prettify = prettify;
	}

	public boolean isPrettify() {
		return prettify;
	}
}
