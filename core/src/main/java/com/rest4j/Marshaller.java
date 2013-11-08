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

import com.rest4j.impl.PatchableType;
import com.rest4j.type.*;
import com.rest4j.json.JSONObject;

/**
 * Marshals/unmarshals objects to JSON according to the types described in the API XML.
 * You can get the reference to this object using {@link com.rest4j.API#getMarshaller()}.
 * Marshaller does all mappings between internal java object representation and JSON.
 *
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public interface Marshaller {
	/**
	 * Marshals a Java object to JSON. Do all the mapping described in the API XML
	 * for the given datatype.
	 *
	 * @param apiType The datatype in terms of API XML type system.
	 * @param content Java object to be marshalled. Can be null, in which case JSONObject.NULL
	 *                is returned
	 * @return A JSON data: {@link com.rest4j.json.JSONObject}, {@link com.rest4j.json.JSONArray}, or any scalar
	 * data supported by the JSON library.
	 */
	Object marshal(ApiType apiType, Object content) throws ApiException;

	/**
	 * Unmarshals a Java object from JSON. Do all the mapping described in the API XML
	 * for the given datatype.
	 *
	 * @param apiType The datatype in terms of API XML type system.
	 * @param json An JSON value to be unmarshalled. Cannot be null. {@link com.rest4j.json.JSONObject}, {@link com.rest4j.json.JSONArray}, or any scalar
	 * data supported by the JSON library.
	 * @return A Java object.
	 */
	Object unmarshal(ApiType apiType, Object json) throws ApiException;

	/**
	 * For objects and maps, update individual properties.
	 * The original object is not changed. Instead, a deep copy of the object is created and then patched.
	 * Any mappings defined in the API XML apply to this method as well.
	 *
	 * @param type The corresponding ApiType that supports patching.
	 * @param original Java object to be copied and then patched.
	 * @param object JSON properties that should be changed.
	 * @return A Java object with the changed properties.
	 *
	 */
	Object unmarshalPatch(PatchableType type, Object original, JSONObject object) throws ApiException;

	/**
	 * Finds a model declared int the API XML by its name. This type
	 * can be applied to both JSON fields and request/response bodies.
	 * @param model Model name, see 'name' attribute of the model tag.
	 */
	ObjectApiType getObjectType(String model);

	/**
	 * Returns an array type of the given element type. Nested arrays or arrays of maps are not supported.
	 * This type can be applied to both JSON fields and request/response bodies.
	 * @param type An element type; cannot be another array.
	 */
	ArrayApiType getArrayType(ApiType type);

	/**
	 * Returns a map type, which is a JSON object with dynamic key names and the given value type. Nested maps
	 * or maps of arrays are not supported.
	 * This type can be applied to both JSON fields and request/response bodies.
	 * @param type An element type; cannot be another array or map.
	 */
	MapApiType getMapType(ApiType type);

	/**
	 * Returns the string type with possible enum values (type='string' in API XML). This type
	 * can be applied to JSON fields.
	 * @param values Possible enum values. May be null.
	 */
	StringApiType getStringType(String[] values);

	/**
	 * Returns an API type representing numbers (type='number' in API XML). This type
	 * can be applied to JSON fields.
	 */
	NumberApiType getNumberType();

	/**
	 * Returns an API type representing booleans (type='boolean' in API XML). This type
	 * can be applied to JSON fields.
	 */
	BooleanApiType getBooleanType();

	/**
	 * Returns an API type representing numbers (type='date' in API XML). This type
	 * can be applied to JSON fields.
	 */
	DateApiType getDateType();
}
