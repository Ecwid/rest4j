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

package com.rest4j.impl.converters;

import com.rest4j.Converter;
import com.rest4j.type.ApiType;
import com.rest4j.type.StringApiType;

import java.lang.reflect.Type;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class ValueConverter implements Converter<Value, String> {

	@Override
	public boolean checkInnerType(Type innerType, ApiType outerType) {
		return innerType == Value.class;
	}

	@Override
	public String getRequiredInnerType(ApiType outerType) {
		return Value.class.getName();
	}

	@Override
	public boolean checkOuterType(ApiType outerType) {
		return outerType instanceof StringApiType;
	}

	@Override
	public String getRequiredOuterType() {
		return "string";
	}

	@Override
	public String marshal(Value object, ApiType outerType) {
		if (object == null) return null;
		return object.value;
	}

	@Override
	public Value unmarshal(String object, Type innerType, ApiType outerType) {
		if (object == null) return null;
		Value v = new Value(object);
		return v;
	}
}
