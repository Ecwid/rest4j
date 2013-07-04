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

import com.rest4j.json.JSONArray;
import com.rest4j.json.JSONException;
import com.rest4j.json.JSONObject;
import com.rest4j.type.ApiType;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class JSONResource implements Resource {
	Object object;
	ApiType apiType;

	public JSONResource(Object object, ApiType apiType) {
		this.object = object;
		this.apiType = apiType;
	}

	public ApiType getApiType() {
		return apiType;
	}

	@Override
	public String getContentType() {
		return "application/json; charset=utf-8";
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
				((JSONObject) object).write(writer);
			} else {
				((JSONArray) object).write(writer);
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


}
