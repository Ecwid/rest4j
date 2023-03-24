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

import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class ArrayApiTypeImplTest {
	ArrayApiTypeImpl type = new ArrayApiTypeImpl(null, new NumberApiTypeImpl(null));

	@Test public void testCast_array_array() throws Exception {
		double[] numbers = (double[]) type.cast(new int[]{1,2,3}, double[].class);
		assertArrayEquals(new double[]{1,2,3}, numbers, 1e-5);
	}

	@Test public void testCast_array_list() throws Exception {
		List<Double> numbers = (List<Double>) type.cast(new int[]{1,2,3}, getListType(Double.class));
		assertEquals(java.util.Arrays.asList(1.0, 2.0, 3.0), numbers);
	}

	@Test public void testCast_list_array() throws Exception {
		final Class elementClass = Double.class;
		double[] numbers = (double[]) type.cast(java.util.Arrays.asList(1,2,3), double[].class);
		assertArrayEquals(new double[]{1, 2, 3}, numbers, 1e-5);
	}

	@Test public void testCast_list_set() throws Exception {
		Set<Double> set = (Set<Double>) type.cast(java.util.Arrays.asList(1,2,3), getSetType(Double.class));
		Set<Double> expect = new LinkedHashSet<Double>();
		expect.addAll(java.util.Arrays.asList(1.0,2.0,3.0));
		assertEquals(expect, set);
	}

	private ParameterizedType getListType(final Class elementClass) {
		return new ParameterizedType(){

				@Override
				public Type[] getActualTypeArguments() {
					return new Type[]{elementClass};
				}

				@Override
				public Type getRawType() {
					return List.class;
				}

				@Override
				public Type getOwnerType() {
					return null;
				}
			};
	}

	private ParameterizedType getSetType(final Class elementClass) {
		return new ParameterizedType(){

			@Override
			public Type[] getActualTypeArguments() {
				return new Type[]{elementClass};
			}

			@Override
			public Type getRawType() {
				return Set.class;
			}

			@Override
			public Type getOwnerType() {
				return null;
			}
		};
	}
}
