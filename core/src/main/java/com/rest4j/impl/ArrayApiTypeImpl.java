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
import com.rest4j.type.ArrayApiType;
import com.rest4j.json.JSONArray;
import com.rest4j.json.JSONException;
import com.rest4j.json.JSONObject;

import java.lang.reflect.*;
import java.util.*;

/**
* @author Joseph Kapizza <joseph@rest4j.com>
*/
public class ArrayApiTypeImpl extends ApiTypeImpl implements ArrayApiType {
	ApiType elementType;

	ArrayApiTypeImpl(Marshaller marshaller, ApiType elementType) {
		super(marshaller);
		this.elementType = elementType;
	}

	@Override
	public boolean check(Type javaClass) {
		if (javaClass instanceof GenericArrayType) {
			return elementType.check(((GenericArrayType)javaClass).getGenericComponentType());
		}
		Class clz = Util.getClass(javaClass);
		if (clz == null) return false;
		if (clz != List.class && clz != Set.class && !clz.isArray()) return false;
		if (javaClass instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) javaClass;
			// a parameter is the element type
			return elementType.check(pType.getActualTypeArguments()[0]);
		} else if (clz.isArray()) {
			return elementType.check(clz.getComponentType());
		} else {
			return false;
		}
	}

	@Override
	public Object cast(Object value, Type javaClass) {
		if (value == null) return null;
		Class clz = Util.getClass(javaClass);
		if (clz != null && clz.isArray() || javaClass instanceof GenericArrayType) {
			Type componentType;
			if (clz != null && clz.isArray()) componentType = clz.getComponentType();
			else componentType = ((GenericArrayType)javaClass).getGenericComponentType();
			Class componentClass = Util.getClass(componentType);
			Object array = Array.newInstance(componentClass, size(value));
			int i=0;
			for (Object element: iterable(value)) {
				Array.set(array, i++, elementType.cast(element, componentType));
			}
			return array;
		} else {
			if (!(javaClass instanceof ParameterizedType)) return value;
			ParameterizedType pType = (ParameterizedType) javaClass;
			Type elementJavaType = pType.getActualTypeArguments()[0];
			Collection newCollection;
			if (Util.getClass(javaClass) == List.class) {
				newCollection = new ArrayList(size(value));
			} else if (Util.getClass(javaClass) == Set.class) {
				newCollection = new LinkedHashSet(size(value));
			} else {
				return value;
			}
			for (Object element: iterable(value)) {
				newCollection.add(elementType.cast(element, elementJavaType));
			}
			return newCollection;
		}
	}

	static int size(final Object value) {
		if (value instanceof Collection) return ((Collection)value).size();
		return Array.getLength(value);
	}

	static Iterable iterable(final Object value) {
		if (value instanceof Iterable) return (Iterable) value;
		if (!value.getClass().isArray()) throw new AssertionError("Expected array or Iterable");
		final int length = Array.getLength(value);
		return new Iterable() {
			@Override
			public Iterator iterator() {
				return new Iterator() {
					int i=0;

					@Override
					public boolean hasNext() {
						return i < length;
					}

					@Override
					public Object next() {
						return Array.get(value, i++);
					}

					@Override
					public void remove() {
					}
				};
			}
		};
	}

	@Override
	public String getJavaName() {
		return "List, Set or an array of <"+elementType.getJavaName()+">";
	}

	@Override
	Object unmarshal(Object val) throws ApiException {
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
					list.add(marshaller.unmarshal(elementType, element));
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
	Object marshal(Object val) throws ApiException {
		if (val == null) return JSONObject.NULL;
		JSONArray array = new JSONArray();
		int i=0;
		for (Object element: iterable(val)) {
			try {
				array.put(i++, marshaller.marshal(elementType, element));
			} catch (JSONException e) {
				throw new ApiException("Cannot create JSON array from "+val).setHttpStatus(500);
			}
		}
		return array;
	}

	@Override
	public ApiType getElementType() {
		return elementType;
	}
}
