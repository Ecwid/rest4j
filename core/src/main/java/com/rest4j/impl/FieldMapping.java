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

import com.rest4j.*;
import com.rest4j.impl.model.*;
import com.rest4j.type.ApiType;
import com.rest4j.type.SimpleApiType;
import com.rest4j.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

/**
* @author Joseph Kapizza <joseph@rest4j.com>
*/
abstract class FieldMapping implements com.rest4j.type.Field {
	String name;
	String parent;
	boolean nullable;
	boolean hasDefault;
	FieldAccessType access;

	ApiType type;
	Object value; // constant
	Field field;
	Type propType;
	Marshaller marshaller;

	Converter<Object, Object> converter = IdConverter.getInstance();

	FieldMapping(MarshallerImpl marshaller, Field fld, String parent) throws ConfigurationException {
		name = fld.getName();
		this.parent = parent;
		nullable = fld.isNullable();
		hasDefault = fld.getDefault() != null;
		access = fld.getAccess();
		field = fld;
		this.marshaller = marshaller;

		if (fld instanceof SimpleField) {
			SimpleField simple = (SimpleField) field;
			if (isConstant()) {
				// this field should have constant value
				if (simple.getCollection() != CollectionType.SINGLETON) {
					throw new ConfigurationException("Field " + name + " cannot be a collection and have 'value' attribute at the same time");
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

	abstract boolean isReadonly();

	boolean isWriteOnly() {
		return field.getAccess() == FieldAccessType.WRITEONLY;
	}

	public Object unmarshal(Object val) throws ApiException {
		if (JSONObject.NULL == val) {
			if (nullable) return null;
			throw new ApiException("Field " + parent + "." + name + " cannot be null");
		}
		if (val == null) {
			throw new ApiException("Field " + parent + "." + name + " is absent");
		}
		try {
			Object result = marshaller.unmarshal(type, val);
			if (value != null && !((SimpleApiType)type).equals(value, result)) {
				throw new ApiException("Field " + parent + "." + name + " should have value "+value);
			}
			return result;
		} catch (ApiException apiex) {
			throw Util.replaceValue(apiex, "Field " + parent + "." + name);
		}
	}

	public Object unmarshalPatch(Object val, Object patched) throws ApiException {
		if (type instanceof PatchableType) {
			if (JSONObject.NULL == val) {
				if (nullable) return null;
				throw new ApiException("Field " + parent + "." + name + " cannot be null");
			}
			if (!(val instanceof JSONObject)) {
				throw new ApiException("JSON object expected for " + parent + "." + name);
			}
			PatchableType ptype = (PatchableType)type;
			Object originalVal = isWriteOnly() ? null : get(patched);
			if (originalVal == null) {
				originalVal = ptype.createInstance((JSONObject)val);
			}
			return ptype.unmarshalPatch(originalVal, (JSONObject)val);
		} else {
			return unmarshal(val);
		}
	}

	public abstract void set(Object inst, Object fieldVal) throws ApiException;

	public Object marshal(Object val) throws ApiException {
		val = converter.marshal(val, type);
		if (val == null) return JSONObject.NULL;
		return marshaller.marshal(type, val);
	}

	public abstract Object get(Object inst) throws ApiException;

	boolean isConstant() {
		return field instanceof SimpleField && ((SimpleField)field).getValue() != null;
	}

	void link(MarshallerImpl marshaller) throws ConfigurationException {
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
					values[j] = simple.getValues().getValue().get(j).getContent();
				}
				if (isConstant()) {
					if (simple.getType() == FieldType.STRING) {
						values = new String[]{simple.getValue()};
					}
				}
			}
			elementType = marshaller.createSimpleType(simple.getType(), values);
		}

		type = marshaller.createType(field.getCollection(), elementType);

		if (!converter.checkOuterType(type)) {
			throw new ConfigurationException("The property "+parent+"."+name+" type does not correspond to converter "+converter+"; expected "+converter.getRequiredOuterType());
		}

		checkType();
	}

	protected abstract void checkType() throws ConfigurationException;

	public String getEffectivePropName() {
		if (field.getProp() == null) return field.getName();
		else return field.getProp();
	}

	protected Object cast(Object fieldVal) throws ApiException {
		try {
			fieldVal = converter.unmarshal(fieldVal, propType, type);
			fieldVal = type.cast(fieldVal, propType);
		} catch (NullPointerException npe) {
			throw new ApiException("Field " + parent + "." + name + " value is absent");
		} catch (IllegalArgumentException iae) {
			throw new ApiException("Field " + parent + "." + name + " has wrong value: "+iae.getMessage());
		}
		return fieldVal;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ApiType getType() {
		return type;
	}

	@Override
	public List getExtra() {
		ExtraInfo extra;
		if (field instanceof SimpleField) {
			extra = ((SimpleField) field).getExtra();
		} else if (field instanceof ComplexField) {
			extra = ((ComplexField) field).getExtra();
		} else {
			throw new AssertionError();
		}
		if (extra == null) return null;
		return extra.getAny();
	}

	public boolean isOptional() {
		return nullable || hasDefault;
	}

	public void setServiceProvider(ServiceProvider serviceProvider) throws ConfigurationException {
		if (field.getConverter() != null && serviceProvider != null) {
			converter = serviceProvider.lookupConverter(field.getConverter());
			if (converter == null) {
				throw new ConfigurationException("Converter not found: "+field.getConverter()+" in "+parent+"."+name);
			}
		}
	}

}
