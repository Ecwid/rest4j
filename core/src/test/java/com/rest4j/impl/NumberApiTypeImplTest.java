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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class NumberApiTypeImplTest {
	NumberApiTypeImpl type = new NumberApiTypeImpl(null);

	@Test public void testCast() throws Exception {
		assertEquals(Double.valueOf(1.23), type.cast(1.23, double.class));
		assertEquals(Long.valueOf(23l), type.cast(23, Long.class));
		assertEquals('\1', type.cast(1, Character.class));
		assertEquals('\1', type.cast(1l, char.class));
	}

	@Test public void testMarshal() throws Exception {
		assertEquals(1, type.marshal('\1'));
		assertEquals(1, type.marshal(1));
	}
}
