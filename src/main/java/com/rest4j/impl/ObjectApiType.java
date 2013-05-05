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
import com.rest4j.ObjectFactory;
import com.rest4j.Patch;
import com.rest4j.impl.model.FieldAccessType;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

/**
* @author Joseph Kapizza <joseph@rest4j.com>
*/
class ObjectApiType extends ApiType { // ComplexField
	String name;
	Class clz;
	FieldImpl[] fields;
	ObjectFactory factory;

	ObjectApiType(String name, Class clz, FieldImpl[] fields, ObjectFactory factory) {
		this.name = name;
		this.clz = clz;
		this.fields = fields;
		this.factory = factory;
	}

	@Override
	boolean check(Type javaClass) {
		return javaClass == clz;
	}

	@Override
	Object defaultValue() {
		return null;
	}

	@Override
	Object cast(Object value, Type javaClass) {
		return value;
	}

	@Override
	String getJavaName() {
		return clz.getName();
	}

	@Override
	Object unmarshal(Object val) throws APIException {
		if (val == null) return null;

		if (!(val instanceof JSONObject)) {
			throw new APIException(400, "{value} should be an object");
		}
		JSONObject object = (JSONObject) val;
		Object inst = factory.createInstance(name, clz, object);

		if (inst == null) return null;

		// first unmarshal non-custom-mapping properties, so that we could use them in a custom mapping logic
		for (FieldImpl field : fields) {
			if (field.mapping != null || field.access == FieldAccessType.READONLY) continue;
			Object fieldVal = object.opt(field.name);
			fieldVal = field.unmarshal(fieldVal, name);
			field.set(inst, fieldVal);
		}
		for (FieldImpl field : fields) {
			if (field.mapping == null || field.access == FieldAccessType.READONLY) continue;
			Object fieldVal = object.opt(field.name);
			fieldVal = field.unmarshal(fieldVal, name);
			field.set(inst, fieldVal);
		}
		return inst;
	}

	@Override
	Object marshal(Object val) throws APIException {
		if (val == null) return null;
		if (!clz.isAssignableFrom(val.getClass())) {
			throw new APIException(500, "Unexpected value "+val+" where "+clz+" was expected");
		}
		JSONObject json = new JSONObject();
		for (FieldImpl field : fields) {
			if (field.access == FieldAccessType.WRITEONLY) continue;
			Object fieldValue = field.value == null ? field.get(val) : field.value;
			fieldValue = field.marshal(fieldValue);
			if (fieldValue != null) {
				try {
					json.put(field.name, fieldValue);
				} catch (JSONException e) {
					throw new APIException(500, "Wrong value of field "+name+"."+field.name+": "+e.getMessage());
				}
			}
		}
		return json;
	}

	public Patch unmarshalPatch(Object original, JSONObject object) throws APIException {
		HashMap<String, Object> result = new HashMap<String, Object>();

		Object patched = Util.deepClone(original);

		// first unmarshal non-custom-mapping properties, so that we could use them in a custom mapping logic
		ArrayList<FieldImpl> ordered = new ArrayList<FieldImpl>();
		for (FieldImpl field : fields) {
			if (field.mapping != null || field.access == FieldAccessType.READONLY) continue;
			ordered.add(field);
		}
		for (FieldImpl field : fields) {
			if (field.mapping == null || field.access == FieldAccessType.READONLY) continue;
			ordered.add(field);
		}
		for (FieldImpl field : ordered) {
			if (object.has(field.name)) {
				Object fieldVal = object.opt(field.name);
				fieldVal = field.unmarshal(fieldVal, name);
				field.set(patched, fieldVal);
				result.put(field.name, fieldVal);
			}
		}
		return new Patch(original, patched, result);
	}
}
