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

package com.rest4j.type;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

/**
 * An abstract data type used in API XML. You can get an instance
 * using {@link com.rest4j.Marshaller} methods.
 *
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public interface ApiType {
	/**
	 * Check that this data type can be implemented by a given Java type.
	 * For example, a number can be implemented by java.lang.Integer, java.lang.Number etc.
	 *
	 * @return True if the API data type can be implemented by the given Java type.
	 */
	boolean check(Type javaType);

	/**
	 * Cast the unmarshalled value of this data type to a specific Java type.
	 * For example, a JSON string can be converted to a Java enum constant.
	 *
	 * @param value The unmarshalled value. Can be null.
	 * @param javaType The concrete type to cast value to. The {@link #check(java.lang.reflect.Type)}
	 *                 function must return true for this type.
	 * @return The casted value.
	 * @throws NullPointerException If there was an attempt to cast null to a primitive Java type (e.g. int).
	 */
	Object cast(@Nullable Object value, Type javaType) throws NullPointerException;

	/**
	 * Returns the names of the Java types that are supported by this API type.
	 * This string can be used in error messages.
	 */
	String getJavaName();

}
