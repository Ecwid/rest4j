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

import com.rest4j.impl.model.FieldType;
import com.rest4j.impl.petapi.RelationType;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class SimpleApiTypeTest {

	@Test
	public void testCheck_boolean() throws Exception {
		SimpleApiType simpleApiType = SimpleApiType.create(FieldType.BOOLEAN, null, null);
		assertTrue(simpleApiType.check(Boolean.class));
		assertTrue(simpleApiType.check(boolean.class));
		assertFalse(simpleApiType.check(String.class));
	}

	@Test
	public void testCheck_number() throws Exception {
		SimpleApiType simpleApiType = SimpleApiType.create(FieldType.NUMBER, null, null);
		assertTrue(simpleApiType.check(Double.class));
		assertTrue(simpleApiType.check(long.class));
		assertTrue(simpleApiType.check(Number.class));
		assertFalse(simpleApiType.check(String.class));
	}

	@Test
	public void testCheck_string() throws Exception {
		SimpleApiType simpleApiType = SimpleApiType.create(FieldType.STRING, null, null);
		assertTrue(simpleApiType.check(String.class));
		assertFalse(simpleApiType.check(Number.class));
	}

	@Test
	public void testCheck_enum() throws Exception {
		SimpleApiType simpleApiType = SimpleApiType.create(FieldType.STRING, null, new String[]{"friend", "ate"});
		assertTrue(simpleApiType.check(String.class));
		assertTrue(simpleApiType.check(RelationType.class));
		assertFalse(simpleApiType.check(Number.class));
	}

	@Test
	public void testCheck_enum_notfound() throws Exception {
		SimpleApiType simpleApiType = SimpleApiType.create(FieldType.STRING, null, new String[]{"friend", "ate", "mated", "WRONG"});
		assertFalse(simpleApiType.check(RelationType.class));
	}

}
