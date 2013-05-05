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
import com.rest4j.ConfigurationException;
import org.json.JSONObject;

import java.lang.reflect.Type;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
class StringApiType extends SimpleApiType {
	String[] enumValues;

	StringApiType(String[] enumValues) {
		this.enumValues = enumValues;
	}

	@Override
	boolean equals(Object val1, Object val2) {
		return cast(val1, String.class).equals(cast(val2, String.class));
	}

	@Override
	boolean check(Type javaType) {
		if (!(javaType instanceof Class)) return false;
		Class clz = (Class)javaType;
		if (clz == null) return false;
		if (clz.isEnum()) {
			try {
				Marshaller.checkEnum(clz, enumValues);
			} catch(ConfigurationException ex) {
				ex.printStackTrace();
				return false;
			}
			return true;
		} else {
			return clz == String.class;
		}
	}

	@Override
	Object cast(Object value, Type javaClass) {
		if (value == null) return null;
		if (value instanceof String) {
			Class clz = (Class)javaClass;
			if (clz.isEnum()) {
				return Util.getEnumConstant(clz, (String)value);
			}
		} else if (value instanceof Enum) {
			if (javaClass == String.class) return ((Enum) value).name();
		}
		return value;
	}

	@Override
	String getJavaName() {
		if (enumValues != null) {
			return "String or enum";
		} else {
			return "String";
		}
	}

	@Override
	Object unmarshal(Object val) throws APIException {
		if (JSONObject.NULL == val) val = null;
		if (!(val instanceof String)) {
			throw new APIException(400, "{value} is expected to be a string");
		}
		if (enumValues != null) {
			boolean found = false;
			for (String enumVal: enumValues) {
				if (enumVal.equals(val)) {
					found = true;
					break;
				}
			}
			if (!found) {
				StringBuilder valuesString = new StringBuilder();
				for (String enumVal: enumValues) {
					if (valuesString.length()>0) valuesString.append(", ");
					valuesString.append(enumVal);
				}
				throw new APIException(400, "{value} is expected to be one of "+valuesString);
			}
		}
		return val;
	}

	@Override
	Object marshal(Object val) throws APIException {
		if (val == null) return JSONObject.NULL;
		if (val instanceof String) {
			return val;
		} else if (val instanceof Enum) {
			return ((Enum)val).name();
		} else {
			throw new APIException(500, "Expected String or Enum, "+val.getClass()+" given");
		}
	}

}
