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

package com.rest4j.spring;

import com.rest4j.ApiException;
import com.rest4j.ObjectFactory;
import com.rest4j.ObjectFactoryChain;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class TestObjectFactory implements ObjectFactory {
	@Nullable
	@Override
	public Object createInstance(@Nonnull String modelName, @Nonnull Class clz, @Nonnull JSONObject object, @Nonnull ObjectFactoryChain next) throws JSONException, ApiException {
		return next.createInstance(modelName, clz, object);
	}

	@Nullable
	@Override
	public Object replaceModel(@Nonnull String modelName, @Nonnull Class clz, @Nullable Object object, @Nonnull ObjectFactoryChain next) throws ApiException {
		return next.replaceModel(modelName, clz, object);
	}
}
