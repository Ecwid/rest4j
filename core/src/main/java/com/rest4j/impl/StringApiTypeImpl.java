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
import com.rest4j.ConfigurationException;
import com.rest4j.Marshaller;
import com.rest4j.type.StringApiType;
import com.rest4j.json.JSONObject;

import java.lang.reflect.Type;
import java.util.logging.Logger;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
class StringApiTypeImpl extends SimpleApiTypeImpl implements StringApiType {
	String[] enumValues;
	static final Logger log = Logger.getLogger(StringApiTypeImpl.class.getName());

	StringApiTypeImpl(Marshaller marshaller, String[] enumValues) {
		super(marshaller);
		this.enumValues = enumValues;
	}

	@Override
	public boolean equals(Object val1, Object val2) {
		return cast(val1, String.class).equals(cast(val2, String.class));
	}

	@Override
	public boolean check(Type javaType) {
		if (!(javaType instanceof Class)) return false;
		Class clz = (Class)javaType;
		if (clz == null) return false;
		if (clz.isEnum()) {
			if (enumValues != null) {
				try {
					MarshallerImpl.checkEnum(clz, enumValues);
				} catch(ConfigurationException ex) {
					log.warning(ex.getMessage());
					return false;
				}
			}
			return true;
		} else {
			return clz == String.class || clz == Character.class || clz == char.class;
		}
	}

	@Override
	public Object cast(Object value, Type javaClass) {
		if (value == null) return null;
		if (value instanceof String) {
			Class clz = (Class)javaClass;
			String str = (String) value;
			if (clz.isEnum()) {
				return Util.getEnumConstant(clz, str);
			} else if (javaClass == char.class) {
				return Character.valueOf(str.length() == 0 ? '\0' : asChar(str));
			} else if (javaClass == Character.class) {
				return str.length() == 0 ? null : Character.valueOf(asChar(str));
			}
		} else if (value instanceof Character) {
			if (javaClass != Character.class && javaClass != char.class) return cast(value.toString(), javaClass);
		} else if (value instanceof Enum) {
			Class clz = (Class)javaClass;
			if (!clz.isEnum()) return cast(((Enum) value).name(), javaClass);
		}
		return value;
	}

	private char asChar(String str) {
		if (str.length() > 1) throw new IllegalArgumentException("Expected single character");
		return str.charAt(0);
	}

	@Override
	public String getJavaName() {
		return "String, char, or enum";
	}

	@Override
	public Object unmarshal(Object val) throws ApiException {
		if (JSONObject.NULL == val) val = null;
		if (!(val instanceof String)) {
			throw new ApiException("{value} is expected to be a string");
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
				throw new ApiException("{value} is expected to be one of "+valuesString);
			}
		}
		return val;
	}

	@Override
	public Object marshal(Object val) throws ApiException {
		if (val == null) return JSONObject.NULL;
		if (val instanceof String) {
			return val;
		} else if (val instanceof Enum) {
			return ((Enum)val).name();
		} else if (val instanceof Character) {
			return val.toString();
		} else {
			throw new ApiException("Expected "+getJavaName()+", "+val.getClass()+" given").setHttpStatus(500);
		}
	}

}
