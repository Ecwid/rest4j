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
import com.rest4j.impl.model.Field;
import com.rest4j.impl.model.FieldAccessType;

import java.beans.PropertyDescriptor;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
class SimpleFieldMapping extends FieldMapping {
	SimpleFieldMapping(Marshaller marshaller, Field fld, String parent) throws ConfigurationException {
		super(marshaller, fld, parent);
	}

	@Override
	boolean initAccessors(Class clz) throws ConfigurationException {
		PropertyDescriptor descr = BeanInfo.getBeanInfo(clz).getPropertyDescription(getEffectivePropName());
		if (descr == null) {
			if (field.getAccess() != FieldAccessType.WRITEONLY && !isConstant()) {
				if (field.isOptional()) {
					return false;
				} else {
					throw new ConfigurationException("Cannot find property " + name + " in class " + clz);
				}
			}
		} else {
			propGetter = descr.getReadMethod();
			propSetter = descr.getWriteMethod();
			if (propGetter == null && field.getAccess() != FieldAccessType.WRITEONLY && !isConstant()) {
				if (field.isOptional()) {
					return false;
				} else {
					throw new ConfigurationException("No getter for " + parent+"."+name + ", but it is not declared as writeonly. Use access='writeonly' in <complex> and <simple> tags.");
				}
			}
		}
		return true;
	}
}
