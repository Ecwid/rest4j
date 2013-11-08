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
import com.rest4j.type.ObjectApiType;

/**
 * This is passed as the 'next' reference to {@link FieldFilter#marshal(Object, Object, com.rest4j.type.ObjectApiType, com.rest4j.type.Field, FieldFilterChain)}
 * and {@link FieldFilter#unmarshal(Object, Object, com.rest4j.type.ObjectApiType, com.rest4j.type.Field, FieldFilterChain)}.
 *
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public interface FieldFilterChain {
	/**
	 * Changes JSON field value during marshalling.
	 *
	 * @param json The field value, in com.rest4j.json.* type hierarchy. Nulls are encoded as JSONObject.Null.
	 * @param parentJavaObject The corresponding source Java object.
	 * @param parentType The object type as described in the API XML.
	 * @param field The field being filtered as described in the API XML.
	 * @return Same or changed field value, in com.rest4j.json.* type hierarchy. Null if you want the field removed.
	 */
	Object marshal(Object json, Object parentJavaObject, ObjectApiType parentType, Field field);

	/**
	 * Changes JSON before unmarshalling.
	 *
	 * @param json The field value, in com.rest4j.json.* type hierarchy. Nulls are encoded as JSONObject.Null.
	 * @param parentJavaObject The corresponding destination Java object.
	 * @param parentType The object type as described in the API XML.
	 * @param field The field being filtered as described in the API XML.
	 * @return Same or changed field value, in com.rest4j.json.* type hierarchy. Null if you want the field removed.
	 */
	Object unmarshal(Object json, Object parentJavaObject, ObjectApiType parentType, Field field);
}
