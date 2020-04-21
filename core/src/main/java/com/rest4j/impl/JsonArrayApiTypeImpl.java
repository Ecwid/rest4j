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

import com.rest4j.ApiException;
import com.rest4j.Marshaller;
import com.rest4j.json.JSONArray;
import com.rest4j.type.JsonArrayApiType;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

public class JsonArrayApiTypeImpl extends SimpleApiTypeImpl implements JsonArrayApiType {
    JsonArrayApiTypeImpl(Marshaller marshaller) {
        super(marshaller);
    }

    @Override
    public boolean equals(Object val1, Object val2) {
        if (val1 != null) {
            return val1.equals(val2);
        } else {
            return val2 == null;
        }
    }

    @Override
    public boolean check(Type javaType) {
        return javaType == JSONArray.class;
    }

    @Override
    public Object cast(@Nullable Object value, Type javaType) throws NullPointerException {
        assert value == null || value instanceof JSONArray : value.getClass();
        assert javaType == JSONArray.class : javaType;
        return value;
    }

    @Override
    public String getJavaName() {
        return JSONArray.class.getName();
    }

    @Override
    Object unmarshal(Object val) throws ApiException {
        if (val == JSONArray.NULL) {
            return null;
        } else if (val instanceof JSONArray) {
            return val;
        } else {
            throw new ApiException("{value} should be an array");
        }
    }

    @Override
    Object marshal(Object val) throws ApiException {
        if (val == null) {
            return JSONArray.NULL;
        } else if (val instanceof JSONArray) {
            return val;
        } else {
            throw new ApiException("Expected " + getJavaName() + ", " + val.getClass() + " given").setHttpStatus(500);
        }
    }
}
