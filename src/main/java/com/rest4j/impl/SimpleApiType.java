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

/**
* @author Joseph Kapizza <joseph@rest4j.com>
*/
abstract class SimpleApiType extends ApiType { // SimpleField
	Object defaultValue;

	static SimpleApiType create(FieldType type, Object defaultValue, String[] enumValues) {
		SimpleApiType apiType;
		switch (type) {
			case STRING:
				apiType = new StringApiType(enumValues);
				break;
			case NUMBER:
				apiType = new NumberApiType();
				break;
			case BOOLEAN:
				apiType = new BooleanApiType();
				break;
			case DATE:
				apiType = new DateApiType();
				break;
			default:
				throw new AssertionError();

		}
		apiType.defaultValue = defaultValue;
		return apiType;
	}

	abstract boolean equals(Object val1, Object val2);

	@Override
	Object defaultValue() {
		return defaultValue;
	}

}
