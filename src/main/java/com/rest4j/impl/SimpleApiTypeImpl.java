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

import com.rest4j.impl.model.FieldType;
import com.rest4j.type.SimpleApiType;

/**
* @author Joseph Kapizza <joseph@rest4j.com>
*/
abstract class SimpleApiTypeImpl extends ApiTypeImpl implements SimpleApiType { // SimpleField
	Object defaultValue;

	public static SimpleApiType create(FieldType type, Object defaultValue, String[] enumValues) {
		SimpleApiTypeImpl apiType;
		switch (type) {
			case STRING:
				apiType = new StringApiTypeImpl(enumValues);
				break;
			case NUMBER:
				apiType = new NumberApiTypeImpl();
				break;
			case BOOLEAN:
				apiType = new BooleanApiTypeImpl();
				break;
			case DATE:
				apiType = new DateApiTypeImpl();
				break;
			default:
				throw new AssertionError();

		}
		apiType.defaultValue = defaultValue;
		return apiType;
	}

	@Override
	public Object defaultValue() {
		return defaultValue;
	}

}
