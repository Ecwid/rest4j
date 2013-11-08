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

package com.rest4j;

import com.rest4j.type.Field;

/**
 * An interface that can be implemented by a field mapper which gets and sets properties dynamically.
 * See <a href="https://code.google.com/p/rest4j/wiki/ApiXmlSchema#model_tag">filed-mapper attribute description</a> in the API XML Schema.
 *
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public interface DynamicMapper<T> {
	/**
	 * Gets the field value.
	 * @param instance The source Java object.
	 * @param field The JSON field.
	 * @return The field value, corresponding to the field type described in the XML.
	 */
	Object get(T instance, Field field);

	/**
	 * Sets the field value
	 * @param instance The source Java object.
	 * @param field The JSON field.
	 * @param value The field value obtained from unmarshalling a JSON and optionally converting with a converter.
	 */
	void set(T instance, Field field, Object value) throws ApiException;
}
