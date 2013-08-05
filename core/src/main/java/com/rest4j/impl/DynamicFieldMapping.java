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
import com.rest4j.DynamicMapper;
import com.rest4j.impl.model.Field;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class DynamicFieldMapping extends FieldMapping {
	private final DynamicMapper customMapper;

	public DynamicFieldMapping(MarshallerImpl marshaller, Field fld, DynamicMapper customMapper, String parent) throws ConfigurationException {
		super(marshaller, fld, parent);
		this.customMapper = customMapper;
	}

	@Override
	boolean initAccessors(Class clz) throws ConfigurationException {
		return true;
	}

	@Override
	boolean isReadonly() {
		return false;
	}

	@Override
	public void set(Object inst, Object fieldVal) throws ApiException {
		customMapper.set(inst, this, fieldVal);
	}

	@Override
	public Object get(Object inst) throws ApiException {
		return customMapper.get(inst, this);
	}

	@Override
	protected void checkType() throws ConfigurationException {
	}

	@Override
	protected Object cast(Object fieldVal) throws ApiException {
		return converter.unmarshal(fieldVal, null, type);
	}
}
