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

package com.rest4j.impl.recursive;

import com.rest4j.impl.petapi.Duplicable;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class Root implements Duplicable<Root> {
	int number;
	Map<String, Integer> map;
	Map<String, Leaf> objectMap;
	Leaf object;
	List<Leaf> array;

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public Map<String, Integer> getMap() {
		return map;
	}

	public void setMap(Map<String, Integer> map) {
		this.map = map;
	}

	public Leaf getObject() {
		return object;
	}

	public void setObject(Leaf object) {
		this.object = object;
	}

	public List<Leaf> getArray() {
		return array;
	}

	public void setArray(List<Leaf> array) {
		this.array = array;
	}

	public Map<String, Leaf> getObjectMap() {
		return objectMap;
	}

	public void setObjectMap(Map<String, Leaf> objectMap) {
		this.objectMap = objectMap;
	}

	@Nonnull
	@Override
	public Root duplicate() {
		var copy = new Root();
		copy.number = number;
		copy.map = map == null ? null : new HashMap<>(map);
		copy.objectMap = objectMap == null
				? null
				: objectMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().duplicate()));
		copy.object = object == null ? null : object.duplicate();
		copy.array = array == null ? null : array.stream().map(Leaf::duplicate).collect(Collectors.toList());
		return copy;
	}
}
