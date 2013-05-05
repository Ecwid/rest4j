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

import com.rest4j.APIException;
import org.json.JSONObject;

import java.lang.reflect.Type;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
class NumberApiType extends SimpleApiType {
	@Override
	boolean check(Type javaType) {
		if (!(javaType instanceof Class)) return false;
		Class clz = (Class)javaType;
		if (clz == null) return false;
		return clz == Number.class || clz == Double.class || clz == double.class || clz == Integer.class || clz == int.class || clz == Long.class || clz == long.class;
	}

	@Override
	boolean equals(Object val1, Object val2) {
		return cast(val1, Double.class).equals(cast(val2, Double.class));
	}

	@Override
	Object cast(Object value, Type javaClass) {
		if (value == null) return null;
		if (value instanceof Number) {
			Number numValue = (Number) value;
			if (javaClass == Integer.class || javaClass == int.class) return numValue.intValue();
			if (javaClass == Double.class || javaClass == double.class) return numValue.doubleValue();
			if (javaClass == Long.class || javaClass == long.class) return numValue.longValue();
			return value;
		}
		return value;
	}

	@Override
	String getJavaName() {
		return "Number, Double, double, Integer, int, Long, or long";
	}

	@Override
	Object unmarshal(Object val) throws APIException {
		if (JSONObject.NULL == val) val = null;
		if (!(val instanceof Number)) {
			throw new APIException(400, "{value} is expected to be a number");
		}
		return val;
	}

	@Override
	Object marshal(Object val) throws APIException {
		if (val == null) return JSONObject.NULL;
		if (!(val instanceof Number)) {
			throw new APIException(500, "Expected Number, "+val.getClass()+" given");
		}
		return val;
	}
}
