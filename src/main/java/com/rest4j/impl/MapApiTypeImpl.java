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
import com.rest4j.Marshaller;
import com.rest4j.type.ApiType;
import com.rest4j.type.MapApiType;
import com.rest4j.type.StringApiType;
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
public class MapApiTypeImpl extends ApiTypeImpl implements MapApiType {
	ApiType elementType;
	final StringApiType stringApiType;

	MapApiTypeImpl(Marshaller marshaller, ApiType elementType) {
		super(marshaller);
		this.elementType = elementType;
		stringApiType = marshaller.getStringType(null);
	}

	@Override
	public ApiType getElementType() {
		return elementType;
	}

	@Override
	public boolean check(Type javaClass) {
		Class clz = Util.getClass(javaClass);
		if (clz == null) return false;
		if (clz != Map.class && clz != HashMap.class && clz != LinkedHashMap.class) return false;
		if (javaClass instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) javaClass;
			if (pType.getActualTypeArguments().length != 2) return false;
			// the first parameter is a string
			if (!stringApiType.check(pType.getActualTypeArguments()[0])) return false;
			// the second parameter is the element type
			return elementType.check(pType.getActualTypeArguments()[1]);
		} else {
			return false;
		}
	}

	@Override
	public Object cast(Object value, Type javaClass) {
		if (value == null) return null;
		ParameterizedType pType = (ParameterizedType) javaClass;
		Type keyJavaType = pType.getActualTypeArguments()[0];
		Type elementJavaType = pType.getActualTypeArguments()[1];
		Map map = (Map)value;
		Map newMap = new LinkedHashMap();
		for (Object key: map.keySet()) {
			newMap.put(stringApiType.cast(key, keyJavaType), elementType.cast(map.get(key), elementJavaType));
		}
		return newMap;
	}

	@Override
	public String getJavaName() {
		return "Map<"+ stringApiType.getJavaName()+","+elementType.getJavaName()+">";
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
					map.put(key, marshaller.unmarshal(elementType, element));
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
		for (Map.Entry entry: ((Map<String, Object>)val).entrySet()) {
			try {
				object.put((String)marshaller.marshal(stringApiType, entry.getKey()), marshaller.marshal(elementType, entry.getValue()));
			} catch (JSONException e) {
				throw new ApiException("Cannot create JSON object from "+val).setHttpStatus(500);
			}
		}
		return object;
	}

}
