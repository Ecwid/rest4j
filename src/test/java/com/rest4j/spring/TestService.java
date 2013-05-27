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
import com.rest4j.Patch;
import com.rest4j.impl.petapi.Pet;
import com.rest4j.impl.petapi.UpdateResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class TestService {
	public List<Pet> list(String type) throws JSONException, ApiException {
		if (type == null) {
			throw new ApiException("No type parameter", new JSONObject("{field:'type'}"));
		}
		Pet pet = new Pet();
		pet.setType(type);
		return Collections.singletonList(pet);
	}
	public Pet get(int id) {
		return null;
	}
	public UpdateResult create(Pet newPet) {
		return null;
	}
	public void delete(int id, String access_token) {
	}
	public void put(int id, Patch<Pet> patch) {
	}
	public void patch(int id, Patch<Pet> patch) {
	}
}
