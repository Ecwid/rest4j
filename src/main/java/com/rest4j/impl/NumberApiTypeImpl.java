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
import com.rest4j.type.NumberApiType;
import org.json.JSONObject;

import java.lang.reflect.Type;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class NumberApiTypeImpl extends SimpleApiTypeImpl implements NumberApiType {
	@Override
	public boolean check(Type javaType) {
		if (!(javaType instanceof Class)) return false;
		Class clz = (Class)javaType;
		if (clz == null) return false;
		return clz == Number.class || clz == Double.class || clz == double.class || clz == Integer.class ||
				clz == int.class || clz == Long.class || clz == long.class || clz == Character.class || clz == char.class;
	}

	@Override
	public boolean equals(Object val1, Object val2) {
		return cast(val1, Double.class).equals(cast(val2, Double.class));
	}

	@Override
	public Object cast(Object value, Type javaClass) {
		if (value == null) {
			if (javaClass == int.class || javaClass == double.class || javaClass == long.class) {
				throw new NullPointerException();
			}
			return null;
		}
		if (value instanceof Number) {
			Number numValue = (Number) value;
			if (javaClass == Integer.class || javaClass == int.class) return numValue.intValue();
			if (javaClass == Double.class || javaClass == double.class) return numValue.doubleValue();
			if (javaClass == Long.class || javaClass == long.class) return numValue.longValue();
			if (javaClass == Character.class || javaClass == char.class) return Character.valueOf((char)numValue.intValue());
			return value;
		} else if (value instanceof Character) {
			char charValue = ((Character)value).charValue();
			if (javaClass == Integer.class || javaClass == int.class) return Integer.valueOf(charValue);
			if (javaClass == Double.class || javaClass == double.class) return Double.valueOf(charValue);
			if (javaClass == Long.class || javaClass == long.class) return Long.valueOf(charValue);
			return value;
		}
		return value;
	}

	@Override
	public String getJavaName() {
		return "Number, Double, double, Integer, int, Long, long, Character, or char";
	}

	@Override
	public Object unmarshal(Object val) throws ApiException {
		if (JSONObject.NULL == val) val = null;
		if (!(val instanceof Number)) {
			throw new ApiException("{value} is expected to be a number");
		}
		return val;
	}

	@Override
	public Object marshal(Object val) throws ApiException {
		if (val == null) return JSONObject.NULL;
		if (val instanceof Character) return Integer.valueOf(((Character)val).charValue());
		if (!(val instanceof Number)) {
			throw new ApiException("Expected Number, "+val.getClass()+" given").setHttpStatus(500);
		}
		return val;
	}
}
