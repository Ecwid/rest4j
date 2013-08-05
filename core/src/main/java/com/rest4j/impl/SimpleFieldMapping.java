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

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
class SimpleFieldMapping extends FieldMapping {
	Method propGetter;
	Method propSetter;

	SimpleFieldMapping(MarshallerImpl marshaller, Field fld, String parent) throws ConfigurationException {
		super(marshaller, fld, parent);
	}

	@Override
	boolean initAccessors(Class clz) throws ConfigurationException {
		PropertyDescriptor descr = BeanInfo.getBeanInfo(clz).getPropertyDescription(getEffectivePropName());
		if (descr == null) {
			if (field.getAccess() != FieldAccessType.WRITEONLY && !isConstant()) {
				if (isOptional()) {
					return false;
				} else {
					throw new ConfigurationException("Cannot find property " + name + " in class " + clz);
				}
			}
		} else {
			propGetter = descr.getReadMethod();
			propSetter = descr.getWriteMethod();
			if (propGetter == null && field.getAccess() != FieldAccessType.WRITEONLY && !isConstant()) {
				if (isOptional()) {
					return false;
				} else {
					throw new ConfigurationException("No getter for " + parent+"."+name + ", but it is not declared as writeonly. Use access='writeonly' in <complex> and <simple> tags.");
				}
			}
			if (propSetter != null) propType = propSetter.getGenericParameterTypes()[0];
		}
		return true;
	}

	@Override
	protected void checkType() throws ConfigurationException {
		if (propGetter != null && !converter.checkInnerType(propGetter.getGenericReturnType(), type)) {
			throw new ConfigurationException("Wrong getter type: "+propGetter.getGenericReturnType()+" for " + parent+"."+name+"; expected "+converter.getRequiredInnerType(type));
		}
		if (propSetter != null && !converter.checkInnerType(propSetter.getGenericParameterTypes()[0], type)) {
			throw new ConfigurationException("Wrong getter type: "+propSetter.getGenericParameterTypes()[0]+" for " + parent+"."+name+"; expected "+converter.getRequiredInnerType(type));
		}
	}

	public void set(Object inst, Object fieldVal) throws ApiException {
		if (propSetter == null) return; // the field is probably mapped to a Service method argument
		try {
			fieldVal = cast(fieldVal);
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

	@Override
	boolean isReadonly() {
		return propGetter == null;
	}

}
