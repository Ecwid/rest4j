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
import com.rest4j.type.BooleanApiType;
import org.json.JSONObject;

import java.lang.reflect.Type;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
class BooleanApiTypeImpl extends SimpleApiTypeImpl implements BooleanApiType {
	@Override
	public boolean check(Type javaType) {
		if (!(javaType instanceof Class)) return false;
		Class clz = (Class)javaType;
		if (clz == null) return false;
		return clz == Boolean.class || clz == boolean.class;
	}

	@Override
	public boolean equals(Object val1, Object val2) {
		return val1.equals(val2);
	}

	@Override
	public Object cast(Object value, Type javaClass) {
		if (value == null) {
			if (javaClass == boolean.class) throw new NullPointerException();
			return null;
		}
		return value;
	}

	@Override
	public String getJavaName() {
		return "Boolean or boolean";
	}

	@Override
	public Object unmarshal(Object val) throws ApiException {
		if (JSONObject.NULL == val) val = null;
		if (!(val instanceof Boolean)) {
			throw new ApiException("{value} is expected to be boolean");
		}
		return val;
	}

	@Override
	public Object marshal(Object val) throws ApiException {
		if (val == null) return JSONObject.NULL;
		if (!(val instanceof Boolean)) {
			throw new ApiException("Expected Boolean, "+val.getClass()+" given").setHttpStatus(500);
		}
		return val;
	}

}
