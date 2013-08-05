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

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class BeanInfo {
	Map<String, PropertyDescriptor> descriptors = new HashMap<String, PropertyDescriptor>();
	static HashMap<Class, BeanInfo> beanInfos = new HashMap<Class, BeanInfo>();

	BeanInfo(Class clz) throws ConfigurationException {
		try {
			java.beans.BeanInfo info = Introspector.getBeanInfo(clz);
			for (PropertyDescriptor descr : info.getPropertyDescriptors()) {
				descriptors.put(descr.getName().toLowerCase(), descr);
			}
		} catch (IntrospectionException e) {
			throw new ConfigurationException("Cannot introspect bean " + clz, e);
		}

	}
	public static BeanInfo getBeanInfo(Class clz) throws ConfigurationException {
		BeanInfo info = beanInfos.get(clz);
		if (info == null) {
			info = new BeanInfo(clz);
			beanInfos.put(clz, info);
		}
		return info;
	}

	public PropertyDescriptor getPropertyDescription(String name) {
		return descriptors.get(name.toLowerCase(Locale.ENGLISH));
	}
}
