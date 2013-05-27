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
import com.rest4j.impl.ApiTypeImpl;
import com.rest4j.impl.Util;
import com.rest4j.type.ApiType;
import com.rest4j.type.ArrayApiType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
* @author Joseph Kapizza <joseph@rest4j.com>
*/
public class ArrayApiTypeImpl extends ApiTypeImpl implements ArrayApiType {
	ApiType elementType;

	ArrayApiTypeImpl(ApiType elementType) {
		this.elementType = elementType;
	}

	@Override
	public boolean check(Type javaClass) {
		Class clz = Util.getClass(javaClass);
		if (clz == null) return false;
		if (clz != List.class) return false;
		if (javaClass instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) javaClass;
			// a parameter is the element type
			return  elementType.check(pType.getActualTypeArguments()[0]);
		} else {
			return false;
		}
	}

	@Override
	public Object defaultValue() {
		return new ArrayList();
	}

	@Override
	public Object cast(Object value, Type javaClass) {
		if (value == null) return null;
		ParameterizedType pType = (ParameterizedType) javaClass;
		Type elementJavaType = pType.getActualTypeArguments()[0];
		List list = (List)value;
		ArrayList newList = new ArrayList(list.size());
		for (Object element: list) {
			newList.add(elementType.cast(element, elementJavaType));
		}
		return newList;
	}

	@Override
	public String getJavaName() {
		return "List<"+elementType.getJavaName()+">";
	}

	@Override
	public Object unmarshal(Object val) throws ApiException {
		if (val instanceof JSONArray) {
			JSONArray array = (JSONArray) val;
			int l = array.length();
			List list = new ArrayList(l);

			for (int i = 0; i < l; i++) {
				Object element = array.opt(i);
				if (JSONObject.NULL.equals(element)) {
					throw new ApiException("{value}["+i+"] should not be null");
				}
				try {
					list.add(elementType.unmarshal(element));
				} catch (ApiException apiex) {
					throw Util.replaceValue(apiex, "{value}[" + i + "]");
				}
			}
			return list;
		} else {
			throw new ApiException("{value} should be an array");
		}
	}

	@Override
	public Object marshal(Object val) throws ApiException {
		if (val == null) return JSONObject.NULL;
		JSONArray array = new JSONArray();
		if (!(val instanceof List)) {
			throw new ApiException("Expected List, "+val.getClass()+" given").setHttpStatus(500);
		}
		int i=0;
		for (Object element: (List)val) {
			try {
				array.put(i++, elementType.marshal(element));
			} catch (JSONException e) {
				throw new ApiException("Cannot create JSON array from "+val).setHttpStatus(500);
			}
		}
		return array;
	}

}
