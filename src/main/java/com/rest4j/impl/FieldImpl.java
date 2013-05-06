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
import com.rest4j.CustomMapping;
import com.rest4j.impl.model.FieldAccessType;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
* @author Joseph Kapizza <joseph@rest4j.com>
*/
class FieldImpl {
	String name;
	boolean optional;
	String mapping; // call getter/setter on a CustomMapping object, not the bean itself
	Method propGetter;
	Method propSetter;
	FieldAccessType access;

	ApiType type;
	CustomMapping customMapping;
	Object value; // constant

	public Object unmarshal(Object val, String parent) throws APIException {
		if (JSONObject.NULL == val) {
			if (optional) return null;
			throw new APIException(400, "Field " + parent + "." + name + " cannot be null");
		}
		if (val == null) {
			if (optional || type.defaultValue() != null) return type.defaultValue();
			throw new APIException(400, "Field " + parent + "." + name + " is absent");
		}
		try {
			Object result = type.unmarshal(val);
			if (value != null && !((SimpleApiType)type).equals(value, result)) {
				throw new APIException(400, "Field " + parent + "." + name + " should have value "+value);
			}
			return result;
		} catch (APIException apiex) {
			throw Util.replaceValue(apiex, "Field " + parent + "." + name);
		}
	}

	public void set(Object inst, Object fieldVal) throws APIException {
		if (propSetter == null) return; // the field is probably mapped to a Service method argument
		try {
			if (mapping == null) {
				propSetter.invoke(inst, type.cast(fieldVal, propSetter.getGenericParameterTypes()[0]));
			} else {
				propSetter.invoke(customMapping, inst, type.cast(fieldVal, propSetter.getGenericParameterTypes()[1]));
			}
		} catch (IllegalAccessException e) {
			throw new APIException(500, "Cannot invoke "+propSetter+" "+e.getMessage());
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof APIException) {
				throw (APIException)e.getTargetException();
			}
			if (e.getTargetException() instanceof RuntimeException) {
				throw (RuntimeException)e.getTargetException();
			}
			throw new RuntimeException("Cannot set "+name, e.getTargetException());
		}
	}

	public Object marshal(Object val) throws APIException {
		if (val == null) return JSONObject.NULL;
		if (optional && type instanceof SimpleApiType && type.defaultValue() != null && val != null) {
			if (((SimpleApiType) type).equals(type.defaultValue(), val)) {
				// Don't serialize optional fields having default values
				return null;
			}
		}
		return type.marshal(val);
	}

	public Object get(Object inst) throws APIException {
		try {
			if (mapping == null) {
				return propGetter.invoke(inst);
			} else {
				return propGetter.invoke(customMapping, inst);
			}
		} catch (IllegalAccessException e) {
			throw new APIException(500, "Cannot invoke "+propGetter+" "+e.getMessage());
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof APIException) {
				throw (APIException)e.getTargetException();
			}
			if (e.getTargetException() instanceof RuntimeException) {
				throw (RuntimeException)e.getTargetException();
			}
			throw new RuntimeException("Cannot get "+name, e.getTargetException());
		}
	}
}
