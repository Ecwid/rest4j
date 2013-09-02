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
package com.rest4j.client;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.AbstractList;


/**
 * A java.util.List backed by JSONArray. You can add elements to the end, change, and remove elements in the middle, but not
 * insert elements in the middle. An protected read(v) method is used to convert
 * elements before retrieval, and write(v) to convert it before storage.
 *
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public abstract class JsonArrayList<T> extends AbstractList<T> {
	final JSONArray array;

	public JsonArrayList(JSONArray array) {
		this.array = array;
	}

	@Override
	public T get(int index) {
		try {
			Object val = array.get(index);
			if (val == JSONObject.NULL) return null;
			return read(val);
		} catch (JSONException e) {
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public void add(int index, T element) {
		if (index != size()) throw new UnsupportedOperationException();
		try {
			array.put(index, write(element));
		} catch (JSONException e) {
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public T set(int index, T element) {
		if (index >= size()) throw new IndexOutOfBoundsException();
		try {
			Object was = array.opt(index);
			array.put(index, write(element));
			return read(was);
		} catch (JSONException e) {
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public int size() {
		return array.length();
	}

	protected abstract T read(Object val);

	protected abstract Object write(T element);

}
