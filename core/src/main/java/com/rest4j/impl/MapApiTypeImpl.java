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
import com.rest4j.Cloner;
import com.rest4j.Marshaller;
import com.rest4j.type.ApiType;
import com.rest4j.type.MapApiType;
import com.rest4j.type.StringApiType;
import org.apache.commons.lang3.StringEscapeUtils;
import com.rest4j.json.JSONException;
import com.rest4j.json.JSONObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
* @author Joseph Kapizza <joseph@rest4j.com>
*/
public class MapApiTypeImpl extends ApiTypeImpl implements MapApiType, PatchableType {
	ApiType elementType;
	final StringApiType stringApiType;
	private final Cloner cloner;

	MapApiTypeImpl(Marshaller marshaller, ApiType elementType, Cloner cloner) {
		super(marshaller);
		this.elementType = elementType;
		this.cloner = cloner;
		stringApiType = marshaller.getStringType(null);
	}

	@Override
	public ApiType getElementType() {
		return elementType;
	}

	@Override
	public boolean check(Type javaType) {
		Class clz = Util.getClass(javaType);
		if (clz == null) return false;
		if (clz != Map.class && clz != HashMap.class && clz != LinkedHashMap.class) return false;
		if (javaType instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) javaType;
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
	public Object cast(Object value, Type javaType) {
		if (value == null) return null;
		if (!(javaType instanceof ParameterizedType) || !(value instanceof Map)) return value;
		ParameterizedType pType = (ParameterizedType) javaType;
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
			Map map = (Map)createInstance(object);

			Iterator<String> keys = object.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				Object element = object.opt(key);
				if (JSONObject.NULL.equals(element)) {
					map.put(key, null);
					continue;
//					throw new ApiException("{value}[\""+ StringEscapeUtils.escapeJavaScript(key)+"\"] should not be null");
				}
				try {
					map.put(key, marshaller.unmarshal(elementType, element));
				} catch (ApiException apiex) {
					throw Util.replaceValue(apiex, "{value}[\"" + StringEscapeUtils.escapeEcmaScript(key) + "\"]");
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

	@Override
	public Object unmarshalPatch(Object original, JSONObject object) throws ApiException {
		if (original == null) return null;

		Object patched = cloner.clone(original);

		Map map = (Map)patched;

		Iterator<String> keys = object.keys();
		Map<String, Object> result = new HashMap<String, Object>(object.length());
		while (keys.hasNext()) {
			String key = keys.next();
			Object element = object.opt(key);
			if (JSONObject.NULL.equals(element)) {
				map.remove(key);
			} else {
				if (elementType instanceof PatchableType) {
					if (!(element instanceof JSONObject)) {
						throw new ApiException("Expected JSON object in key '"+key+"'");
					}
					PatchableType ptype = (PatchableType)elementType;
					Object originalVal = map.get(key);
					if (originalVal == null) {
						originalVal = ptype.createInstance(null);
					}
					try {
						map.put(key, marshaller.unmarshalPatch(ptype, originalVal, (JSONObject)element));
					} catch (ApiException apiex) {
						throw Util.replaceValue(apiex, "{value}[\"" + StringEscapeUtils.escapeEcmaScript(key) + "\"]");
					}
				} else {
					try {
						map.put(key, marshaller.unmarshal(elementType, element));
					} catch (ApiException apiex) {
						throw Util.replaceValue(apiex, "{value}[\"" + StringEscapeUtils.escapeEcmaScript(key) + "\"]");
					}
				}
			}
		}
		return patched;
	}

	@Override
	public Object createInstance(JSONObject object) throws ApiException {
		return new LinkedHashMap(object.length());
	}
}
