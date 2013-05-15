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

import com.rest4j.impl.petapi.RelationType;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class UtilTest {
	static List<String> listField;

	@Test public void testGetClass_simple() {
		assertSame(String.class, Util.getClass(String.class));
	}

	@Test public void testGetClass_list() throws NoSuchFieldException {
		assertSame(List.class, Util.getClass(getClass().getDeclaredField("listField").getGenericType()));
	}

	@Test(expected=AssertionError.class) public void testGetEnumConstant_failure() {
		Util.getEnumConstant(RelationType.class, "zzz");
	}

	@Test public void testGetEnumConstant_success() {
		assertEquals(RelationType.mated, Util.getEnumConstant(RelationType.class, "mated"));
	}

	@Test(expected=IllegalArgumentException.class) public void testGetParameterNames_not_found() throws IOException {
		Util.getParameterNames(UtilTest.class, "zzz");
	}

	@Test(expected=IllegalArgumentException.class) public void testGetParameterNames_static() throws IOException {
		Util.getParameterNames(Util.class, "getParameterNames");
	}

	@Test public void testGetParameterNames_no_params() throws IOException {
		String[] names = Util.getParameterNames(UtilTest.class, "noParamsMethod");
		assertEquals(0, names.length);
	}

	@Test public void testGetParameterNames_params() throws IOException {
		String[] names = Util.getParameterNames(UtilTest.class, "methodWithParams");
		assertArrayEquals(new String[]{"param1", "param2"}, names);
	}

	@Test public void testGetParameterNames_nested_class() throws IOException {
		Runnable some = new Runnable() {
			public void run() {}
			public void test(String a) {}
		};
		String[] names = Util.getParameterNames(some.getClass(), "test");
		assertArrayEquals(new String[]{"a"}, names);
	}

	static class Tree implements Serializable {
		String name;
		int a,b,c,d,e;
		double f,g,h;
		Tree left,right;
	}

	@Test public void testClone() {
		Tree test = new Tree();
		test.name = StringUtils.repeat("TEST", 10000);
		test.left = new Tree();
		test.right = test.left;

		ThreadMXBean threadMx = ManagementFactory.getThreadMXBean();
		long start = threadMx.getCurrentThreadCpuTime();
		int N = 10000;
		for (int i=0; i<N; i++) test = Util.deepClone(test);
		System.out.println("cloned in "+(threadMx.getCurrentThreadCpuTime()-start)/N+"ns");
		assertSame(test.left, test.right);
	}

	void noParamsMethod() {}
	void methodWithParams(String param1, String param2) {}

}
