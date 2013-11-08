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

import com.rest4j.type.ApiType;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

/**
 * Converts JSON fields from Java representation to a JSON representations, potentially of a different type.
 * Converters are parameterized with two type parameters: the inner Java type and the outer Java type used for marshalling.
 * <p/>
 * Converters are specified with the optional 'converter' attribute of the 'fields/simple' or 'fields/complex'
 * XML tags in the API XML description.
 *
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public interface Converter<F,T> {

	/**
	 * During API creation, checks that the converter can be applied to the given Java property
	 * type.
	 * @param innerType The type of the Java property
	 * @param outerType The JSON field type as declared int the XML and checked with {@link #checkOuterType(com.rest4j.type.ApiType)}
	 * @return true if the converter can be applied.
	 */
	boolean checkInnerType(Type innerType, ApiType outerType);

	/**
	 * Says what inner type should the converter apply to. If {@link #checkInnerType(java.lang.reflect.Type, com.rest4j.type.ApiType)} returns false, this method is
	 * used to create the error message.
	 * @param outerType The JSON field type as declared int the XML and checked with {@link #checkOuterType(com.rest4j.type.ApiType)}
	 */
	String getRequiredInnerType(ApiType outerType);

	/**
	 * During API creation, checks that the converter can be applied to the given JSON field type.
	 * @param outerType The JSON field type as declared int the XML
	 * @return true if the converter can be applied.
	 */
	boolean checkOuterType(ApiType outerType);

	/**
	 * Says what JSON type should the converter apply to. If {@link #checkOuterType(com.rest4j.type.ApiType)} returns false, this method is
	 * used to create the error message.
	 */
	String getRequiredOuterType();

	/**
	 * This is called before transforming a property value into JSON form.
	 * @param object The property value
	 * @param outerType The JSON field type as declared int the XML
	 * @return The converted value. May be the same value or null. The type of the value should correspond the
	 * declared outerType.
	 */
	T marshal(F object, ApiType outerType);

	/**
	 * This is called after transforming a JSON field's value into Java object.
	 * @param object The JSON field value
	 * @param innerType The inner Java type
	 * @param outerType The JSON field type as declared int the XML
	 * @return The converted value. May be the same value or null. The type os the value should match the inner type
	 * of the Java property.
	 */
	F unmarshal(T object, @Nullable Type innerType, ApiType outerType) throws ApiException;
}
