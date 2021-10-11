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

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class Pet implements Duplicable<Pet> {
	int id;
	String type = "dog";
	String name;
	Double petWeight;
	Gender gender;
	List<Integer> friends = new ArrayList<Integer>();
	List<Integer> mated = new ArrayList<Integer>();
	List<Integer> ate = new ArrayList<Integer>();
	boolean writeonly;
	char middlename = 1;
	Map<String, String> extraData;

	public boolean isWriteonly() {
		return writeonly;
	}

	public void setWriteonly(boolean writeonly) {
		this.writeonly = writeonly;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getPetWeight() {
		return petWeight;
	}

	public void setPetWeight(Double petWeight) {
		this.petWeight = petWeight;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public List<Integer> getFriends() {
		return friends;
	}

	public void setFriends(List<Integer> friends) {
		this.friends = friends;
	}

	public List<Integer> getMated() {
		return mated;
	}

	public void setMated(List<Integer> mated) {
		this.mated = mated;
	}

	public List<Integer> getAte() {
		return ate;
	}

	public void setAte(List<Integer> ate) {
		this.ate = ate;
	}

	public char getMiddlename() {
		return middlename;
	}

	public void setMiddlename(char middlename) {
		this.middlename = middlename;
	}

	public Map<String, String> getExtraData() {
		return extraData;
	}

	public void setExtraData(Map<String, String> extraData) {
		this.extraData = extraData;
	}

	@Nonnull
	@Override
	public Pet duplicate() {
		var copy = new Pet();
		copy.id = id;
		copy.type = type;
		copy.name = name;
		copy.petWeight = petWeight;
		copy.gender = gender;
		copy.friends = friends == null ? null : new ArrayList<>(friends);
		copy.mated = mated == null ? null : new ArrayList<>(mated);
		copy.ate = ate == null ? null : new ArrayList<>(ate);
		copy.writeonly = writeonly;
		copy.middlename = middlename;
		copy.extraData = extraData == null ? null : new HashMap<>(extraData);
		return copy;
	}
}
