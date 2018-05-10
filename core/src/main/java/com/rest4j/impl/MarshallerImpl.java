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
import com.rest4j.impl.model.CollectionType;
import com.rest4j.impl.model.FieldType;
import com.rest4j.impl.model.Model;
import com.rest4j.type.*;
import com.rest4j.json.JSONException;
import com.rest4j.json.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class MarshallerImpl implements Marshaller {
	Map<String, ObjectApiTypeImpl> models = new HashMap<String, ObjectApiTypeImpl>();
	ObjectFactoryChain objectFactoryChain = new ObjectFactoryChain() {
		@Nullable
		@Override
		public Object createInstance(@Nonnull String modelName, @Nonnull Class clz, @Nonnull JSONObject object) {
			Object inst;
			Class instantiate = getObjectType(modelName).getInstantiate();
			if (instantiate != null) {
				clz = instantiate;
			}
			try {
				inst = clz.newInstance();
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				// we have already checked instance creation in the configuration phase, so this should work
				throw new AssertionError(e);
			}
			return inst;
		}

	};
	ServiceProvider serviceProvider;
	FieldFilterChain fieldFilterChain = new FieldFilterChain() {
		@Override
		public Object marshal(Object json, Object parentObject, ObjectApiType parentType, com.rest4j.type.Field field) {
			return json;
		}

		@Override
		public Object unmarshal(Object json, Object parentObject, ObjectApiType parentType, com.rest4j.type.Field field) {
			return json;
		}
	};

	public static class ModelConfig {
		Model model;
		Object customMapper;

		public ModelConfig(Model model, Object customMapping) {
			this.model = model;
			this.customMapper = customMapping;
		}
	}

	MarshallerImpl(List<ModelConfig> modelConfigs) throws ConfigurationException {
		this(modelConfigs, new com.rest4j.ObjectFactory[0]);
	}

	MarshallerImpl(List<ModelConfig> modelConfigs, com.rest4j.ObjectFactory factory) throws ConfigurationException {
		this(modelConfigs, new com.rest4j.ObjectFactory[]{factory});
	}

	public MarshallerImpl(List<ModelConfig> modelConfig, ObjectFactory[] factories) throws ConfigurationException {
		this(modelConfig, factories, null);
	}

	MarshallerImpl(List<ModelConfig> modelConfigs, com.rest4j.ObjectFactory[] factories, ServiceProvider serviceProvider) throws ConfigurationException {
		this(modelConfigs, factories, new FieldFilter[0], serviceProvider);
	}

	MarshallerImpl(List<ModelConfig> modelConfigs, com.rest4j.ObjectFactory[] factories, FieldFilter[] fieldFilters, ServiceProvider serviceProvider) throws ConfigurationException {
		this.serviceProvider = serviceProvider;
		for (com.rest4j.ObjectFactory of: factories) {
			addObjectFactory(of);
		}
		for (FieldFilter ff: fieldFilters) {
			addFieldFilter(ff);
		}
		for (ModelConfig modelConfig : modelConfigs) {
			Model model = modelConfig.model;
			Object customMapper = modelConfig.customMapper;
			Class clz;
			try {
				clz = Class.forName(model.getClazz());
			} catch (ClassNotFoundException e) {
				throw new ConfigurationException("Cannot find class " + model.getClazz());
			}

			ObjectApiTypeImpl objectType = new ObjectApiTypeImpl(this, model.getName(), clz, model, customMapper, objectFactoryChain, fieldFilterChain, serviceProvider);
			if (model.getInstantiate() != null) {
				Class instantiate;
				try {
					instantiate = Class.forName(model.getInstantiate());
				} catch (ClassNotFoundException e) {
					throw new ConfigurationException("Cannot find class " + model.getInstantiate());
				}
				if (!clz.isAssignableFrom(instantiate)) {
					throw new ConfigurationException(clz+" should be assignable from "+model.getInstantiate());
				}
				try {
					// check that it can be instantiated
					instantiate.newInstance();
				} catch (InstantiationException e) {
					throw new ConfigurationException("Cannot instantiate " + model.getInstantiate()+": "+e.getMessage());
				} catch (IllegalAccessException e) {
					throw new ConfigurationException("Cannot instantiate " + model.getInstantiate()+": "+e.getMessage());
				}
				objectType.setInstantiate(instantiate);
			}
			models.put(model.getName(), objectType);
		}

		// fill model interconnections and type-check
		for (ModelConfig modelConfig : modelConfigs) {
			Model model = modelConfig.model;
			ObjectApiTypeImpl modelImpl = models.get(model.getName());
			modelImpl.link();
		}
	}

	@Override
	public ObjectApiTypeImpl getObjectType(String model) {
		return models.get(model);
	}

	@Override
	public ArrayApiTypeImpl getArrayType(ApiType type) {
		return new ArrayApiTypeImpl(this, type);
	}

	@Override
	public MapApiTypeImpl getMapType(ApiType type) {
		return new MapApiTypeImpl(this, type);
	}

	@Override
	public StringApiType getStringType(String[] values) {
		return new StringApiTypeImpl(this, values);
	}

	@Override
	public NumberApiType getNumberType() {
		return new NumberApiTypeImpl(this);
	}

	@Override
	public BooleanApiType getBooleanType() {
		return new BooleanApiTypeImpl(this);
	}

	@Override
	public DateApiType getDateType() {
		return new DateApiTypeImpl(this);
	}

	@Override
	public JsonObjectApiType getJsonObjectType() {
		return new JsonObjectApiTypeImpl(this);
	}

	@Override
	public Object marshal(ApiType apiType, Object value) throws ApiException {
		if (apiType instanceof ConcreteClassMapping) apiType = ((ConcreteClassMapping)apiType).objectApiType;
		return ((ApiTypeImpl)apiType).marshal(value);
	}

	@Override
	public Object unmarshal(ApiType apiType, Object value) throws ApiException {
		if (apiType instanceof ConcreteClassMapping) apiType = ((ConcreteClassMapping)apiType).objectApiType;
		return ((ApiTypeImpl)apiType).unmarshal(value);
	}

	@Override
	public Object unmarshalPatch(PatchableType apiType, Object original, JSONObject object) throws ApiException {
		if (apiType instanceof ConcreteClassMapping) apiType = ((ConcreteClassMapping)apiType).objectApiType;
		return apiType.unmarshalPatch(original, object);
	}

	SimpleApiType createSimpleType(FieldType type, String[] enumValues) {
		SimpleApiType apiType;
		switch (type) {
			case STRING:
				apiType = getStringType(enumValues);
				break;
			case NUMBER:
				apiType = getNumberType();
				break;
			case BOOLEAN:
				apiType = getBooleanType();
				break;
			case DATE:
				apiType = getDateType();
				break;
			case JSON_OBJECT:
				apiType = getJsonObjectType();
				break;
			default:
				throw new AssertionError();

		}
		return apiType;
	}

	ApiType createType(CollectionType collection, ApiType elementType) {
		switch (collection) {
			case ARRAY:
				return getArrayType(elementType);
			case SINGLETON:
				return elementType;
			case MAP:
				return getMapType(elementType);
			default:
				throw new AssertionError();
		}
	}

	Class getClassForModel(String model) {
		return models.get(model).clz;
	}

	static private final Pattern ISDOUBLE = Pattern.compile("[\\.eE]");

	Object parse(String val, FieldType type) {
		if (val == null) return null;
		switch (type) {
			case BOOLEAN: return Boolean.parseBoolean(val);
			case NUMBER:
				if (ISDOUBLE.matcher(val).find()) {
					return Double.parseDouble(val);
				} else {
					return Long.parseLong(val);
				}
			case STRING: return val;
		}
		throw new AssertionError();
	}

	static void checkEnum(Class clz, String[] enumValues) throws ConfigurationException {
		// check that the declared enum values are a subset of java enum
		for (String declaredValue: enumValues) {
			boolean found = false;
			for (Object option: clz.getEnumConstants()) {
				if (((Enum)option).name().equals(declaredValue)) found = true;
			}
			if (!found) {
				throw new ConfigurationException("Cannot find a enum value "+declaredValue+" in "+clz);
			}
		}
	}

	private void addObjectFactory(final com.rest4j.ObjectFactory factory) {
		final ObjectFactoryChain nextChain = objectFactoryChain;
		objectFactoryChain = new ObjectFactoryChain() {

			@Nullable
			@Override
			public Object createInstance(@Nonnull String modelName, @Nonnull Class clz, @Nonnull JSONObject object) throws JSONException, ApiException {
				return factory.createInstance(modelName, clz, object, nextChain);
			}

		};
	}

	private void addFieldFilter(final FieldFilter ff) {
		final FieldFilterChain nextChain = fieldFilterChain;
		fieldFilterChain = new FieldFilterChain() {
			@Override
			public Object marshal(Object json, Object parentObject, ObjectApiType parentType, Field field) {
				return ff.marshal(json, parentObject, parentType, field, nextChain);
			}

			@Override
			public Object unmarshal(Object json, Object parentObject, ObjectApiType parentType, Field field) {
				return ff.unmarshal(json, parentObject, parentType, field, nextChain);
			}
		};
	}

}
