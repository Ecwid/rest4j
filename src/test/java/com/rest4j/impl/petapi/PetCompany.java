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

package com.rest4j.impl.petapi;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class PetCompany {
	Map<String, Pet> petsMap = new HashMap<String, Pet>();
	HashMap<String, Long> petsHashMap = new HashMap<String, Long>();
	Set<Pet> petsSet = new LinkedHashSet<Pet>();

	public Map<String, Pet> getPetsMap() {
		return petsMap;
	}

	public void setPetsMap(Map<String, Pet> petsMap) {
		this.petsMap = petsMap;
	}

	public HashMap<String, Long> getPetsHashMap() {
		return petsHashMap;
	}

	public Set<Pet> getPetsSet() {
		return petsSet;
	}

	public void setPetsSet(Set<Pet> petsSet) {
		this.petsSet = petsSet;
	}

	public void setPetsHashMap(HashMap<String, Long> petsHashMap) {
		this.petsHashMap = petsHashMap;
	}
}
