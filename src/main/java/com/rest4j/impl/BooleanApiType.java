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
class BooleanApiType extends SimpleApiType {
	@Override
	boolean check(Type javaType) {
		if (!(javaType instanceof Class)) return false;
		Class clz = (Class)javaType;
		if (clz == null) return false;
		return clz == Boolean.class || clz == boolean.class;
	}

	@Override
	boolean equals(Object val1, Object val2) {
		return val1.equals(val2);
	}

	@Override
	Object cast(Object value, Type javaClass) {
		if (value == null) {
			if (javaClass == boolean.class) throw new NullPointerException();
			return null;
		}
		return value;
	}

	@Override
	String getJavaName() {
		return "Boolean or boolean";
	}

	@Override
	Object unmarshal(Object val) throws APIException {
		if (JSONObject.NULL == val) val = null;
		if (!(val instanceof Boolean)) {
			throw new APIException(400, "{value} is expected to be boolean");
		}
		return val;
	}

	@Override
	Object marshal(Object val) throws APIException {
		if (val == null) return JSONObject.NULL;
		if (!(val instanceof Boolean)) {
			throw new APIException(500, "Expected Boolean, "+val.getClass()+" given");
		}
		return val;
	}

}
