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

import java.util.HashMap;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class ParamsImpl extends HashMap<String, Object> implements Params {

	@Override
	public String getString(String paramName) {
		return (String)get(paramName);
	}

	@Override
	public Number getNumber(String paramName) {
		return (Number)get(paramName);
	}

	@Override
	public Boolean getBoolean(String paramName) {
		return (Boolean)get(paramName);
	}

	@Override
	public Object getEnum(String paramName, Class enumClass) {
		return Util.getEnumConstant(enumClass, getString(paramName));
	}

	@Override
	public Object get(String name) {
		return super.get(name);
	}
}
