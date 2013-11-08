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

/**
 * An API type that corresponds to collection='array' in API XML. You can get an instance
 * using {@link com.rest4j.Marshaller} methods.
 *
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public interface ArrayApiType extends ApiType {
	/**
	 * The type of array elements. Cannot be another array or a map.
	 */
	ApiType getElementType();
}
