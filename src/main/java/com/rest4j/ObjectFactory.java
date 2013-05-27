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

import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public interface ObjectFactory {

	/**
	 * Creates the java instance from JSON object during unmarshal. This is a pre-unmarshal operation: implementor
	 * may not fill the created object with any data, but may choose an appropriate class to instantiate depending on
	 * input data.
	 *
	 * @param modelName The 'name' attribute of the &lt;model>.
	 * @param clz The 'class' attribute of the &lt;model>.
	 * @param object The object being unmarshalled, not null.
	 * @param next The chain-of-responsibility delegate.
	 * @return The java object instance that will be returned. Can return null, in which case the unmarshalled object
	 *         becomes null.
	 * @throws JSONException
	 */
	@Nullable Object createInstance(
			@Nonnull String modelName,
			@Nonnull Class clz,
			@Nonnull JSONObject object,
			@Nonnull ObjectFactoryChain next) throws JSONException, ApiException;

	/**
	 * Replaces an object during marshalling.
	 *
	 * @param modelName The 'name' attribute of the &lt;model>.
	 * @param clz The 'class' attribute of the &lt;model>.
	 * @param object The java object being marshalled. May be null.
	 * @param next The chain-of-responsibility delegate.
	 * @return The object that will be marshalled instead. May be null, in which case no object is marshalled.
	 */
	@Nullable Object replaceModel(@Nonnull String modelName,
								  @Nonnull Class clz,
								  @Nullable Object object,
								  @Nonnull ObjectFactoryChain next) throws ApiException;
}
