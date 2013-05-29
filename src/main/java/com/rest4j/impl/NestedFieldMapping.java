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

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class NestedFieldMapping extends FieldMapping {
	Method[] propGetters;
	Class[] types;
	Method[] propSetters;

	NestedFieldMapping(Marshaller marshaller, Field fld, String parent) throws ConfigurationException {
		super(marshaller, fld, parent);
	}

	@Override
	boolean initAccessors(Class clz) throws ConfigurationException {
		String[] parts = getEffectivePropName().split("\\.");
		if (parts.length <= 1) {
			throw new AssertionError(getEffectivePropName());
		}
		propGetters = new Method[parts.length];
		propSetters = new Method[parts.length];
		types = new Class[parts.length];
		int i = 0;
		for (String part: parts) {
			PropertyDescriptor descr = BeanInfo.getBeanInfo(clz).getPropertyDescription(part);
			if (descr == null) {
				if (!isConstant()) {
					if (field.isOptional()) {
						return false;
					} else {
						throw new ConfigurationException("Cannot find property " + part + " in class " + clz);
					}
				}
			} else {
				clz = descr.getPropertyType();
				types[i] = clz;
				if (descr.getReadMethod() == null) {
					throw new ConfigurationException("No getter for property " + part + " in class " + clz);
				}
				propGetters[i] = descr.getReadMethod();
				propSetters[i] = descr.getWriteMethod();
			}
			i++;
		}
		return true;
	}

	boolean isReadonly() {
		return propGetter == null;
	}

	@Override
	public void set(Object inst, Object fieldVal) throws ApiException {
		try {
			for (int i = 0; i<propGetters.length-1; i++) {
				Object newInst;
				try {
					newInst = propGetters[i].invoke(inst);
				} catch (IllegalAccessException e) {
					throw new ApiException("Cannot invoke "+propGetters[i]+" "+e.getMessage()).setHttpStatus(500);
				}
				if (newInst == null) {
					if (propSetters[i] == null) {
						throw new ApiException("Cannot set "+name+" because the object "+types[i].getName()+
								" does not exist and cannot be set.").setHttpStatus(500);
					}

					// try creating parent object chain
					try {
						newInst = types[i].newInstance();
					} catch (InstantiationException e) {
						throw new ApiException("Cannot create instance of "+types[i].getName()+" "+e.getMessage()).setHttpStatus(500);
					} catch (IllegalAccessException e) {
						throw new ApiException("Cannot create instance of "+types[i].getName()+" "+e.getMessage()).setHttpStatus(500);
					}

					// attach the child object to the parent
					try {
						propSetters[i].invoke(inst, newInst);
					} catch (IllegalAccessException e) {
						throw new ApiException("Cannot invoke "+propSetters[i]+" "+e.getMessage()).setHttpStatus(500);
					}
				}
				inst = newInst;
			}
			// set field value
			Method setter = propSetters[propSetters.length - 1];
			try {
				if (propType == null) {
					propType = setter.getGenericParameterTypes()[0];
				}
				fieldVal = cast(fieldVal);
				setter.invoke(inst, fieldVal);
			} catch (IllegalAccessException e) {
				throw new ApiException("Cannot invoke "+setter+" "+e.getMessage()).setHttpStatus(500);
			}

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
			for (int i = 0; i<propGetters.length; i++) {
				try {
					inst = propGetters[i].invoke(inst);
					if (inst == null) return null;
				} catch (IllegalAccessException e) {
					throw new ApiException("Cannot invoke "+propGetters[i]+" "+e.getMessage()).setHttpStatus(500);
				}
			}
			return inst;
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
	protected void checkType() throws ConfigurationException {
		Method propGetter = propGetters[propGetters.length-1];
		Method propSetter = propSetters[propSetters.length-1];

		if (propGetter != null && !type.check(propGetter.getGenericReturnType())) {
			throw new ConfigurationException("Wrong getter type: "+propGetter.getGenericReturnType()+" for " + parent+"."+name+"; expected "+type.getJavaName());
		}
		if (propSetter != null && !type.check(propSetter.getGenericParameterTypes()[0])) {
			throw new ConfigurationException("Wrong getter type: "+propSetter.getGenericParameterTypes()[0]+" for " + parent+"."+name+"; expected "+type.getJavaName());
		}
	}

}
