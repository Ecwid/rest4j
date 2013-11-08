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
import com.rest4j.impl.model.Field;
import com.rest4j.impl.model.Model;
import com.rest4j.type.ObjectApiType;
import com.rest4j.json.JSONException;
import com.rest4j.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
* @author Joseph Kapizza <joseph@rest4j.com>
*/
public class ObjectApiTypeImpl extends ApiTypeImpl implements ObjectApiType, PatchableType {
	static final Logger log = Logger.getLogger(ObjectApiType.class.getName());
	final Model model;
	final MarshallerImpl marshaller;
	final String name;
	final Class clz;
	// there could be several concrete subclasses corresponding to an abstract class
	final ArrayList<ConcreteClassMapping> mappings = new ArrayList<ConcreteClassMapping>();
	final ObjectFactoryChain factory;
	final Object fieldMapper;
	final ServiceProvider serviceProvider;
	private Class instantiate;
	final FieldFilterChain fieldFilter;

	ObjectApiTypeImpl(MarshallerImpl marshaller, String name, Class clz, Model model, Object fieldMapper, ObjectFactoryChain factory, FieldFilterChain fieldFilter, ServiceProvider serviceProvider) throws ConfigurationException {
		super(marshaller);
		this.serviceProvider = serviceProvider;
		this.marshaller = marshaller;
		this.name = name;
		this.clz = clz;
		this.model = model;
		this.fieldMapper = fieldMapper;
		mappings.add(new ConcreteClassMapping(marshaller, clz, model, fieldMapper, serviceProvider, this));
		this.factory = factory;
		this.fieldFilter = fieldFilter;
	}

	@Override
	public boolean check(Type javaType) {
		javaType = Util.getClass(javaType);
		return javaType == clz;
	}

	@Override
	public Object cast(Object value, Type javaType) {
		return value;
	}

	@Override
	public String getJavaName() {
		return clz.getName();
	}

	@Override
	Object unmarshal(Object val) throws ApiException {
		if (val == null) return null;

		if (!(val instanceof JSONObject)) {
			throw new ApiException("{value} should be an object");
		}
		JSONObject object = (JSONObject) val;

		Object inst = createInstance(object);

		if (inst == null) return null;

		getMapping(inst.getClass()).unmarshal(object, inst);

		return inst;
	}

	@Override
	public Object createInstance(JSONObject object) throws ApiException {
		Object inst = null;
		try {
			inst = factory.createInstance(name, clz, object);
		} catch (JSONException e) {
			throw new ApiException("Cannot create instance of "+name+": "+e.getMessage()).setHttpStatus(500);
		}
		return inst;
	}

	@Override
	Object marshal(Object val) throws ApiException {
		if (val == null) return null;
		if (!clz.isAssignableFrom(val.getClass())) {
			throw new ApiException("Unexpected value "+val+" where "+clz+" was expected").setHttpStatus(500);
		}
		JSONObject json = new JSONObject();

		getMapping(val.getClass()).marshal(json, val);
		return json;
	}

	@Override
	public Object unmarshalPatch(Object original, JSONObject object) throws ApiException {
		if (original == null) return null;

		Object patched = Util.deepClone(original);

		getMapping(original.getClass()).unmarshalPatch(object, patched);

		return patched;
	}

	@Override
	public List<com.rest4j.type.Field> getFields() throws ApiException {
		ConcreteClassMapping mapping = getMapping(clz);
		return mapping.getFields();
	}

	@Override
	public ObjectApiType getSubtype(Class subclass) throws ApiException {
		return getMapping(subclass);
	}

	@Override
	public List getExtra() {
		return model.getExtra().getAny();
	}

	@Override
	public Class getJavaClass() {
		return clz;
	}

	ConcreteClassMapping getMapping(Class clz) throws ApiException {
		for (ConcreteClassMapping ccm: mappings) {
			if (ccm.clz == clz) return ccm;
		}
		synchronized(this) {
			for (ConcreteClassMapping ccm: mappings) {
				if (ccm.clz == clz) return ccm;
			}

			try {
				ConcreteClassMapping ccm = new ConcreteClassMapping(marshaller, clz, model, fieldMapper, serviceProvider, this);
				ccm.link();
				mappings.add(ccm);
				return ccm;
			} catch (ConfigurationException e) {
				log.log(Level.SEVERE, "Cannot map class "+clz, e);
				throw new ApiException("Internal error: "+e.getMessage()).setHttpStatus(500);
			}

		}
	}

	void link() throws ConfigurationException {
		for (ConcreteClassMapping ccm: mappings) {
			ccm.link();
		}
	}

	FieldMapping checkFieldAsArgument(String name, Type paramType) throws ConfigurationException {
		for (Field field: model.getFields().getSimpleAndComplex()) {
			if (field.getName().equals(name)) {
				FieldMapping fieldMapping = new SimpleFieldMapping(marshaller, field, this.name);
				fieldMapping.link(marshaller);
				return fieldMapping;
			}
		}
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setInstantiate(Class instantiate) {
		this.instantiate = instantiate;
	}

	public Class getInstantiate() {
		return instantiate;
	}
}
