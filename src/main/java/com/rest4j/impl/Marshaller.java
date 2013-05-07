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

import com.rest4j.ConfigurationException;
import com.rest4j.ObjectFactory;
import com.rest4j.ObjectFactoryChain;
import com.rest4j.impl.model.*;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class Marshaller {
	Map<String, ObjectApiType> models = new HashMap<String, ObjectApiType>();
	ArrayList<ObjectFactoryChain> factories = new ArrayList<ObjectFactoryChain>();
	ArrayList<ObjectFactory> chain = new ArrayList<ObjectFactory>();
	static final ObjectFactory defaultFactory = new ObjectFactory() {
		@Nullable
		@Override
		public Object createInstance(@Nonnull String modelName, @Nonnull Class clz, @Nonnull JSONObject object) {
			Object inst;
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

	public static class ModelConfig {
		Model model;
		Object customMapper;

		public ModelConfig(Model model, Object customMapping) {
			this.model = model;
			this.customMapper = customMapping;
		}
	}

	Marshaller(List<ModelConfig> modelConfigs) throws ConfigurationException {
		chain.add(defaultFactory);
		for (ModelConfig modelConfig : modelConfigs) {
			Model model = modelConfig.model;
			Object customMapper = modelConfig.customMapper;
			Class clz;
			try {
				clz = Class.forName(model.getClazz());
			} catch (ClassNotFoundException e) {
				throw new ConfigurationException("Cannot find class " + model.getClazz());
			}

			Map<String, PropertyDescriptor> descriptors = new HashMap<String, PropertyDescriptor>();
			try {
				BeanInfo info = Introspector.getBeanInfo(clz);
				for (PropertyDescriptor descr : info.getPropertyDescriptors()) {
					descriptors.put(descr.getName().toLowerCase(), descr);
				}
			} catch (IntrospectionException e) {
				throw new ConfigurationException("Cannot introspect bean " + clz, e);
			}
			FieldMapping[] fields = new FieldMapping[model.getFields().getSimpleAndComplex().size()];
			int i = 0;
			for (Field fld : model.getFields().getSimpleAndComplex()) {
				FieldMapping fieldImpl = new FieldMapping();
				fields[i] = fieldImpl;
				fieldImpl.customMapper = customMapper;
				fieldImpl.name = fld.getName();
				fieldImpl.optional = fld.isOptional();
				fieldImpl.access = fld.getAccess();

				String forClause = " for " + model.getName() + "." + fld.getName();
				if (fld.getMappingMethod() == null) {
					String propName;
					if (fld.getProp() == null) propName = fld.getName();
					else propName = fld.getProp();
					PropertyDescriptor descr = descriptors.get(propName.toLowerCase());
					if (descr == null) {
						if (fld.getAccess() != FieldAccessType.WRITEONLY && !isConstant(fld))
							throw new ConfigurationException("Cannot find property " + propName + " in class " + clz);
					} else {
						fieldImpl.propGetter = descr.getReadMethod();
						fieldImpl.propSetter = descr.getWriteMethod();
					}
				} else {
					fieldImpl.mapping = fld.getMappingMethod();
					for (Method method : customMapper.getClass().getMethods()) {
						if (method.getName().equals(fieldImpl.mapping)) {
							if (method.getParameterTypes().length <= 0 || method.getParameterTypes().length > 2) {
								throw new ConfigurationException("Wrong accessor " + method.getName() + " parameter count: " + method.getParameterTypes().length + forClause + ". Should be one parameter (getter) or two parameters (setter).");
							}
							Class paramType = method.getParameterTypes()[0];
							if (paramType != clz) {
								throw new ConfigurationException("Wrong accessor '" + method.getName() + "' parameter type: " + paramType + "; expected " + clz + forClause);
							}
							if (method.getParameterTypes().length == 1) {
								if (fieldImpl.propGetter != null) {
									throw new ConfigurationException("Ambiguous getter '" + method.getName() + "'" + forClause);
								}
								if (method.getReturnType() == void.class) {
									throw new ConfigurationException("Void return value of getter '" + method.getName() + "'" + forClause);
								}
								fieldImpl.propGetter = method;
							} else {
								if (fieldImpl.propSetter != null) {
									throw new ConfigurationException("Ambiguous setter '" + method.getName() + "'" + forClause);
								}
								if (method.getReturnType() != void.class) {
									throw new ConfigurationException("Non-void return value " + method.getReturnType() + " of setter '" + method.getName() + "'" + forClause);
								}
								fieldImpl.propSetter = method;
							}
						}
					}
				}
				if (fieldImpl.propGetter == null && fld.getAccess() != FieldAccessType.WRITEONLY && !isConstant(fld)) {
					throw new ConfigurationException("No getter " + forClause + ", but it is not declared as writeonly. Use access='writeonly' in <complex> and <simple> tags.");
				}
				fields[i++] = fieldImpl;
			}
			models.put(model.getName(), new ObjectApiType(model.getName(), clz, fields, getObjectFactory()));
		}

		// fill model interconnections and type-check
		for (ModelConfig modelConfig : modelConfigs) {
			Model model = modelConfig.model;
			Object customMapper = modelConfig.customMapper;

			ObjectApiType modelImpl = models.get(model.getName());
			for (int i = 0; i < modelImpl.fields.length; i++) {
				FieldMapping fieldImpl = modelImpl.fields[i];
				Field fld = model.getFields().getSimpleAndComplex().get(i);

				String inField = " in field " + modelImpl.name + "." + fieldImpl.name;

				ApiType elementType;
				if (fld instanceof ComplexField) {
					ComplexField complex = (ComplexField) fld;
					ObjectApiType reference = models.get(complex.getType());
					if (reference == null)
						throw new ConfigurationException("Field " + fld.getName() + " type not found: " + complex.getType());
					elementType = reference;
				} else {
					SimpleField simple = (SimpleField) fld;
					String values[] = null;
					if (simple.getValues() != null && simple.getType() == FieldType.STRING) {
						values = new String[simple.getValues().getValue().size()];
						for (int j = 0; j < values.length; j++) {
							values[j] = simple.getValues().getValue().get(j);
						}
					}
					if (isConstant(simple)) {
						// this field should have constant value
						if (simple.isArray()) {
							throw new ConfigurationException("Field " + fld.getName() + " cannot be an array and have 'value' attribute at the same time");
						}
						if (simple.getValues() != null) {
							throw new ConfigurationException("Field " + fld.getName() + " cannot have 'values' tag and a 'value' attribute at the same time");
						}
						if (simple.getDefault() != null) {
							throw new ConfigurationException("Field " + fld.getName() + " cannot have 'default' and 'value' attributes at the same time");
						}
						if (simple.getType() == FieldType.STRING) {
							values = new String[]{simple.getValue()};
						}
						fieldImpl.value = parse(simple.getValue(), simple.getType());
					}
					elementType = SimpleApiType.create(simple.getType(), parse(simple.getDefault(), simple.getType()), values);
				}

				if (fld.isArray()) {
					fieldImpl.type = new ArrayApiType(elementType);
				} else {
					fieldImpl.type = elementType;
				}

				if (fieldImpl.mapping != null) {
					if (customMapper == null)
						throw new ConfigurationException("'mapping' attribute used when no custom mapper supplied " +
								inField + ". Use 'mapping' attribute of <model> to specify custom mapper object.");
					if (fieldImpl.propGetter != null) {
						fieldImpl.type.check(fieldImpl.propGetter.getGenericReturnType());
					}
					if (fieldImpl.propSetter != null) {
						Type[] genericParameterTypes = fieldImpl.propSetter.getGenericParameterTypes();
						fieldImpl.type.check(genericParameterTypes[genericParameterTypes.length - 1]);
					}
				}
			}
		}
	}

	private boolean isConstant(Field fld) {
		return fld instanceof SimpleField && ((SimpleField)fld).getValue() != null;
	}

	public ObjectApiType getObjectType(String model) {
		return models.get(model);
	}

	public ApiType getArrayType(String model) {
		return new ArrayApiType(models.get(model));
	}

	public Class getClassForModel(String model) {
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
				if (option.toString().equals(declaredValue)) found = true;
			}
			if (!found) {
				throw new ConfigurationException("Cannot find a enum value "+declaredValue+" in "+clz);
			}
		}
	}

	public void addObjectFactory(ObjectFactoryChain of) {
		final int i = factories.size();
		factories.add(of);
		chain.add(i, new ObjectFactory() {

			@Nullable
			@Override
			public Object createInstance(@Nonnull String modelName, @Nonnull Class clz, @Nonnull JSONObject object) {
				return factories.get(i).createInstance(modelName, clz, object, chain.get(i + 1));
			}
		});
	}

	public ObjectFactory getObjectFactory() {
		return chain.get(0);
	}
}
