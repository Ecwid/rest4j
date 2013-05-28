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
import com.rest4j.impl.model.*;
import com.rest4j.type.ApiType;
import com.rest4j.type.SimpleApiType;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
* @author Joseph Kapizza <joseph@rest4j.com>
*/
abstract class FieldMapping {
	String name;
	String parent;
	boolean optional;
	Method propGetter;
	Method propSetter;
	FieldAccessType access;

	ApiType type;
	Object value; // constant
	Field field;
	Type propType;

	FieldMapping(Marshaller marshaller, Field fld, String parent) throws ConfigurationException {
		name = fld.getName();
		this.parent = parent;
		optional = fld.isOptional();
		access = fld.getAccess();
		field = fld;

		if (fld instanceof SimpleField) {
			SimpleField simple = (SimpleField) field;
			if (isConstant()) {
				// this field should have constant value
				if (simple.isArray()) {
					throw new ConfigurationException("Field " + name + " cannot be an array and have 'value' attribute at the same time");
				}
				if (simple.getValues() != null) {
					throw new ConfigurationException("Field " + name + " cannot have 'values' tag and a 'value' attribute at the same time");
				}
				if (simple.getDefault() != null) {
					throw new ConfigurationException("Field " + name + " cannot have 'default' and 'value' attributes at the same time");
				}
				value = marshaller.parse(simple.getValue(), simple.getType());
			}
		}

	}

	abstract boolean initAccessors(Class clz) throws ConfigurationException;

	boolean isReadonly() {
		return propGetter == null;
	}

	public Object unmarshal(Object val) throws ApiException {
		if (JSONObject.NULL == val) {
			if (optional) return null;
			throw new ApiException("Field " + parent + "." + name + " cannot be null");
		}
		if (val == null) {
			if (optional || type.defaultValue() != null) return type.defaultValue();
			throw new ApiException("Field " + parent + "." + name + " is absent");
		}
		try {
			Object result = type.unmarshal(val);
			if (value != null && !((SimpleApiType)type).equals(value, result)) {
				throw new ApiException("Field " + parent + "." + name + " should have value "+value);
			}
			return result;
		} catch (ApiException apiex) {
			throw Util.replaceValue(apiex, "Field " + parent + "." + name);
		}
	}

	public void set(Object inst, Object fieldVal) throws ApiException {
		if (propSetter == null) return; // the field is probably mapped to a Service method argument
		try {
			if (propType == null) {
				propType = propSetter.getGenericParameterTypes()[0];
			}
			try {
				fieldVal = type.cast(fieldVal, propType);
			} catch (NullPointerException npe) {
				throw new ApiException("Field " + parent + "." + name + " value is absent");
			}
			propSetter.invoke(inst, fieldVal);

		} catch (IllegalAccessException e) {
			throw new ApiException("Cannot invoke "+propSetter+" "+e.getMessage()).setHttpStatus(500);
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof ApiException) {
				throw (ApiException)e.getTargetException();
			}
			if (e.getTargetException() instanceof RuntimeException) {
				throw (RuntimeException)e.getTargetException();
			}
			throw new RuntimeException("Cannot set "+name, e.getTargetException());
		}
	}

	public Object marshal(Object val) throws ApiException {
		if (val == null) return JSONObject.NULL;
		if (optional && type instanceof SimpleApiType && type.defaultValue() != null && val != null) {
			if (((SimpleApiType) type).equals(type.defaultValue(), val)) {
				// Don't serialize optional fields having default values
				return null;
			}
		}
		return type.marshal(val);
	}

	public Object get(Object inst) throws ApiException {
		try {
			return propGetter.invoke(inst);
		} catch (IllegalAccessException e) {
			throw new ApiException("Cannot invoke "+propGetter+" "+e.getMessage()).setHttpStatus(500);
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof ApiException) {
				throw (ApiException)e.getTargetException();
			}
			if (e.getTargetException() instanceof RuntimeException) {
				throw (RuntimeException)e.getTargetException();
			}
			throw new RuntimeException("Cannot get "+name, e.getTargetException());
		}
	}

	boolean isConstant() {
		return field instanceof SimpleField && ((SimpleField)field).getValue() != null;
	}

	void link(Marshaller marshaller) throws ConfigurationException {
		String inField = " in field " + parent + "." + name;

		ApiType elementType;
		if (field instanceof ComplexField) {
			ComplexField complex = (ComplexField) field;
			ObjectApiTypeImpl reference = (ObjectApiTypeImpl) marshaller.getObjectType(complex.getType());
			if (reference == null)
				throw new ConfigurationException("Field " + field.getName() + " type not found: " + complex.getType());
			elementType = reference;
		} else {
			SimpleField simple = (SimpleField) field;
			String values[] = null;
			if (simple.getValues() != null && simple.getType() == FieldType.STRING) {
				values = new String[simple.getValues().getValue().size()];
				for (int j = 0; j < values.length; j++) {
					values[j] = simple.getValues().getValue().get(j);
				}
				if (isConstant()) {
					if (simple.getType() == FieldType.STRING) {
						values = new String[]{simple.getValue()};
					}
				}
			}
			elementType = SimpleApiTypeImpl.create(simple.getType(), marshaller.parse(simple.getDefault(), simple.getType()), values);
		}

		if (field.isArray()) {
			type = new ArrayApiTypeImpl(elementType);
		} else {
			type = elementType;
		}

	}

	public String getEffectivePropName() {
		if (field.getProp() == null) return field.getName();
		else return field.getProp();
	}
}
