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
import com.rest4j.ObjectFactoryChain;
import com.rest4j.Patch;
import com.rest4j.impl.model.Field;
import com.rest4j.impl.model.Model;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
* @author Joseph Kapizza <joseph@rest4j.com>
*/
class ObjectApiType extends ApiType {
	static final Logger log = Logger.getLogger(ObjectApiType.class.getName());
	final Model model;
	final Marshaller marshaller;
	final String name;
	final Class clz;
	// there could be several concrete subclasses corresponding to an abstract class
	final ArrayList<ConcreteClassMapping> mappings = new ArrayList<ConcreteClassMapping>();
	final ObjectFactoryChain factory;
	final Object fieldMapper;

	ObjectApiType(Marshaller marshaller, String name, Class clz, Model model, Object fieldMapper, ObjectFactoryChain factory) throws ConfigurationException {
		this.marshaller = marshaller;
		this.name = name;
		this.clz = clz;
		this.model = model;
		this.fieldMapper = fieldMapper;
		mappings.add(new ConcreteClassMapping(marshaller, clz, model, fieldMapper));
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

		Object inst = null;
		try {
			inst = factory.createInstance(name, clz, object);
		} catch (JSONException e) {
			throw new APIException(500, "Cannot create instance of "+name+": "+e.getMessage());
		}

		if (inst == null) return null;

		getMapping(inst.getClass()).unmarshal(object, inst);

		return inst;
	}

	@Override
	Object marshal(Object val) throws APIException {
		if (val == null) return null;
		if (!clz.isAssignableFrom(val.getClass())) {
			throw new APIException(500, "Unexpected value "+val+" where "+clz+" was expected");
		}
		JSONObject json = new JSONObject();

		getMapping(val.getClass()).marshal(json, val);
		return json;
	}

	public Patch unmarshalPatch(Object original, JSONObject object) throws APIException {
		if (original == null) return new Patch(null, null, new HashMap<String, Object>());

		Object patched = Util.deepClone(original);

		HashMap<String, Object> result = getMapping(original.getClass()).unmarshalPatch(object, patched);

		return new Patch(original, patched, result);
	}

	ConcreteClassMapping getMapping(Class clz) throws APIException {
		for (ConcreteClassMapping ccm: mappings) {
			if (ccm.clz == clz) return ccm;
		}
		synchronized(this) {
			for (ConcreteClassMapping ccm: mappings) {
				if (ccm.clz == clz) return ccm;
			}

			try {
				ConcreteClassMapping ccm = new ConcreteClassMapping(marshaller, clz, model, fieldMapper);
				ccm.link();
				mappings.add(ccm);
				return ccm;
			} catch (ConfigurationException e) {
				log.log(Level.SEVERE, "Cannot map class "+clz, e);
				throw new APIException(500, "Internal error: "+e.getMessage());
			}

		}
	}

	void link() throws ConfigurationException {
		for (ConcreteClassMapping ccm: mappings) {
			ccm.link();
		}
	}

	public FieldMapping checkFieldAsArgument(String name, Type paramType) throws ConfigurationException {
		for (Field field: model.getFields().getSimpleAndComplex()) {
			if (field.getName().equals(name)) {
				FieldMapping fieldMapping = new FieldMapping(marshaller, field, null, this.name);
				fieldMapping.mapping = null;
				fieldMapping.link(marshaller);
				return fieldMapping;
			}
		}
		return null;
	}
}
