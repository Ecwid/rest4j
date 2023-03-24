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

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class StringApiTypeImplTest {
	StringApiTypeImpl type = new StringApiTypeImpl(null, null);

	@Test public void testCast_success() throws Exception {
		assertEquals(Character.valueOf('A'), type.cast("A", Character.class));
		assertEquals(Character.valueOf('A'), type.cast("A", char.class));
		assertEquals(Character.valueOf('\0'), type.cast("", char.class));
		assertNull(type.cast("", Character.class));
		assertEquals(TestEnum.TEST, type.cast("TEST", TestEnum.class));
		assertEquals("TEST", type.cast(TestEnum.TEST, String.class));
		assertEquals(Character.valueOf('S'), type.cast(TestEnum.S, char.class));
		assertEquals("SIMPL", type.cast("SIMPL", String.class));
	}

	@Test
	public void testCast_more_then_one() throws Exception {
		assertThrows(IllegalArgumentException.class, () -> {
			type.cast("AAA", Character.class);
		});
	}
}
