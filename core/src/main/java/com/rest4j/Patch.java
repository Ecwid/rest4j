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

import com.rest4j.json.JSONObject;

/**
 * An optional type of an API service parameter when using 'bod/patch' tag in API XML.
 * If the body of an API endpoint is declared as a patch, then you have an option to access
 * both the patched and the original (non-patched) objects, as long as the patching JSON
 * itself, by declaring a service parameter as Patch&lt;type>. Example:
 *
 * <pre>
 * PetUpdateResult updatePet(int petId, Patch&lt;Pet> patch) {
 *   if (!patch.getOriginal().getName().equals(patch.getPatched().getName())) {
 *       // pet name has been changed
 *   }
 * }
 * </pre>
 *
 * The original (non-patched) Java object is retrieved by invoking GET method on the same URL.
 *
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class Patch<T> {
	final private T original;
	final private T patched;
	final private JSONObject changedProperties;

	/**
	 * The instance is usually created by Rest4j, no need to call constructor.
	 */
	public Patch(T original, T patched, JSONObject patch) {
		this.original = original;
		this.patched = patched;
		this.changedProperties = patch;
	}

	/**
	 * The original (non-patched) Java object retrieved by invoking GET method on the same URL.
	 */
	public T getOriginal() {
		return original;
	}

	/**
	 * The original object, deep-copied, with che changed properties.
	 */
	public T getPatched() {
		return patched;
	}

	/**
	 * The incoming JSON that was used to change the object's properties.
	 */
	public JSONObject getChangedProperties() {
		return changedProperties;
	}
}
