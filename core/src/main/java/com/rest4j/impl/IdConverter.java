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

import com.rest4j.Converter;
import com.rest4j.type.ApiType;

import java.lang.reflect.Type;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class IdConverter implements Converter<Object, Object> {
	static final IdConverter instance = new IdConverter();

	public static IdConverter getInstance() {
		return instance;
	}

	private IdConverter(){}

	@Override
	public boolean checkInnerType(Type innerType, ApiType outerType) {
		return outerType.check(innerType);
	}

	@Override
	public String getRequiredInnerType(ApiType outerType) {
		return outerType.getJavaName();
	}

	@Override
	public boolean checkOuterType(ApiType outerType) {
		return true;
	}

	@Override
	public String getRequiredOuterType() {
		return null;
	}

	@Override
	public Object marshal(Object object, ApiType outerType) {
		return object;
	}

	@Override
	public Object unmarshal(Object object, Type innerType, ApiType outerType) {
		if (innerType == null) return object;
		return outerType.cast(object, innerType);
	}
}
