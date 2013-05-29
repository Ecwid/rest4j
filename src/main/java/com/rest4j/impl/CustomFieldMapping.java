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
import com.rest4j.impl.model.Field;
import com.rest4j.impl.model.FieldAccessType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class CustomFieldMapping extends FieldMapping {
	private final Object customMapper;
	String mapping; // call getter/setter on a CustomMapping object, not the bean itself

	CustomFieldMapping(Marshaller marshaller, Field fld, Object customMapper, String parent) throws ConfigurationException {
		super(marshaller, fld, parent);
		this.customMapper = customMapper;
		mapping = fld.getMappingMethod();
	}

	@Override
	boolean initAccessors(Class clz) throws ConfigurationException {
		String forClause = " for " + parent + "." + name;
		if (customMapper == null) {
			throw new ConfigurationException("No field mapper specified, but mapping method is set"+forClause);
		}
		for (Method method : customMapper.getClass().getMethods()) {
			if (method.getName().equals(mapping)) {
				if (method.getParameterTypes().length <= 0 || method.getParameterTypes().length > 2) {
					throw new ConfigurationException("Wrong accessor " + method.getName() + " parameter count: " + method.getParameterTypes().length + forClause + ". Should be one parameter (getter) or two parameters (setter).");
				}
				Class paramType = method.getParameterTypes()[0];
				if (!paramType.isAssignableFrom(clz)) {
					throw new ConfigurationException("Wrong accessor '" + method.getName() + "' parameter type: " + paramType + "; expected " + clz + forClause);
				}
				if (method.getParameterTypes().length == 1) {
					if (propGetter != null) {
						throw new ConfigurationException("Ambiguous getter '" + method.getName() + "'" + forClause);
					}
					if (method.getReturnType() == void.class) {
						throw new ConfigurationException("Void return value of getter '" + method.getName() + "'" + forClause);
					}
					propGetter = method;
				} else if (method.getParameterTypes().length == 2) {
					if (propSetter != null) {
						throw new ConfigurationException("Ambiguous setter '" + method.getName() + "'" + forClause);
					}
					if (method.getReturnType() != void.class) {
						throw new ConfigurationException("Non-void return value " + method.getReturnType() + " of setter '" + method.getName() + "'" + forClause);
					}
					if (!method.getParameterTypes()[0].isAssignableFrom(clz)) {
						throw new ConfigurationException("Wrong first parameter type of setter '" + method.getName() + "'" + forClause+". Expected "+clz.getName()+" or its ancestors.");
					}
					propSetter = method;
				}
			}
		}
		if (propGetter == null && field.getAccess() != FieldAccessType.WRITEONLY && !isConstant()) {
			if (field.isOptional()) {
				return false;
			} else {
				throw new ConfigurationException("No getter for " + parent+"."+name + ", but it is not declared as writeonly. Use access='writeonly' in <complex> and <simple> tags.");
			}
		}
		return true;
	}

	@Override
	public void set(Object inst, Object fieldVal) throws ApiException {
		if (propSetter == null) return;
		try {
			if (propType == null) {
				propType = propSetter.getGenericParameterTypes()[1];
			}
			fieldVal = cast(fieldVal);
			propSetter.invoke(customMapper, inst, fieldVal);

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

	@Override
	public Object get(Object inst) throws ApiException {
		try {
			return propGetter.invoke(customMapper, inst);
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

	@Override
	void link(Marshaller marshaller) throws ConfigurationException {
		super.link(marshaller);
		if (customMapper == null)
			throw new ConfigurationException("'mapping' attribute used when no custom mapper supplied " +
					" in field " + parent + "." + name + ". Use 'mapping' attribute of <model> to specify custom mapper object.");
		if (propGetter != null) {
			type.check(propGetter.getGenericReturnType());
		}
		if (propSetter != null) {
			Type[] genericParameterTypes = propSetter.getGenericParameterTypes();
			type.check(genericParameterTypes[genericParameterTypes.length - 1]);
		}
	}

	@Override
	protected void checkType() throws ConfigurationException {
		if (propGetter != null && !type.check(propGetter.getGenericReturnType())) {
			throw new ConfigurationException("Wrong getter type: "+propGetter.getGenericReturnType()+" for " + parent+"."+name+"; expected "+type.getJavaName());
		}
		if (propSetter != null && !type.check(propSetter.getGenericParameterTypes()[1])) {
			throw new ConfigurationException("Wrong getter type: "+propSetter.getGenericParameterTypes()[1]+" for " + parent+"."+name+"; expected "+type.getJavaName());
		}
	}
}
