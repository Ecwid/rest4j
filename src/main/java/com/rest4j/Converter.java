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
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public interface Converter<F,T> {
	boolean checkInnerType(Type innerType, ApiType outerType);
	String getRequiredInnerType(ApiType outerType);
	boolean checkOuterType(ApiType outerType);
	String getRequiredOuterType();
	T marshal(F object, ApiType outerType);
	F unmarshal(T object, @Nullable Type innerType, ApiType outerType) throws ApiException;
}
