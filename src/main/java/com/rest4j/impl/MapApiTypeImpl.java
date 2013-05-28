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

import com.rest4j.ApiException;
import com.rest4j.type.ApiType;
import com.rest4j.type.ArrayApiType;
import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
* @author Joseph Kapizza <joseph@rest4j.com>
*/
public class MapApiTypeImpl extends ApiTypeImpl implements ArrayApiType {
	ApiType elementType;

	MapApiTypeImpl(ApiType elementType) {
		this.elementType = elementType;
	}

	@Override
	public boolean check(Type javaClass) {
		Class clz = Util.getClass(javaClass);
		if (clz == null) return false;
		if (clz != Map.class && clz != HashMap.class && clz != LinkedHashMap.class) return false;
		if (javaClass instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) javaClass;
			if (pType.getActualTypeArguments().length != 2) return false;
			// the first parameter is String
			if (pType.getActualTypeArguments()[0] != String.class) return false;
			// the second parameter is the element type
			return elementType.check(pType.getActualTypeArguments()[1]);
		} else {
			return false;
		}
	}

	@Override
	public Object defaultValue() {
		return new LinkedHashMap();
	}

	@Override
	public Object cast(Object value, Type javaClass) {
		if (value == null) return null;
		Class clz = Util.getClass(javaClass);
		ParameterizedType pType = (ParameterizedType) javaClass;
		Type elementJavaType = pType.getActualTypeArguments()[1];
		Map map = (Map)value;
		Map newMap = new LinkedHashMap();
		for (Object key: map.keySet()) {
			newMap.put(key, elementType.cast(map.get(key), elementJavaType));
		}
		return newMap;
	}

	@Override
	public String getJavaName() {
		return "Map<String,"+elementType.getJavaName()+">";
	}

	@Override
	public Object unmarshal(Object val) throws ApiException {
		if (val instanceof JSONObject) {
			JSONObject object = (JSONObject) val;
			Map map = new LinkedHashMap(object.length());

			Iterator<String> keys = object.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				Object element = object.opt(key);
				if (JSONObject.NULL.equals(element)) {
					throw new ApiException("{value}[\""+ StringEscapeUtils.escapeJavaScript(key)+"\"] should not be null");
				}
				try {
					map.put(key, elementType.unmarshal(element));
				} catch (ApiException apiex) {
					throw Util.replaceValue(apiex, "{value}[\"" + StringEscapeUtils.escapeJavaScript(key) + "\"]");
				}
			}
			return map;
		} else {
			throw new ApiException("{value} should be an object");
		}
	}

	@Override
	public Object marshal(Object val) throws ApiException {
		if (val == null) return JSONObject.NULL;
		JSONObject object = new JSONObject();
		if (!(val instanceof Map)) {
			throw new ApiException("Expected Map, "+val.getClass()+" given").setHttpStatus(500);
		}
		for (Map.Entry<String, Object> entry: ((Map<String, Object>)val).entrySet()) {
			try {
				object.put(entry.getKey().toString(), elementType.marshal(entry.getValue()));
			} catch (JSONException e) {
				throw new ApiException("Cannot create JSON object from "+val).setHttpStatus(500);
			}
		}
		return object;
	}

}
