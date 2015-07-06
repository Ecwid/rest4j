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

import com.rest4j.ApiException;
import com.rits.cloning.Cloner;
import com.sun.org.apache.bcel.internal.classfile.ClassParser;
import com.sun.org.apache.bcel.internal.classfile.JavaClass;
import com.sun.org.apache.bcel.internal.classfile.LocalVariable;
import com.sun.org.apache.bcel.internal.classfile.Method;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;


/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class Util {

	public static Class getClass(Type javaClass) {
		if (javaClass instanceof GenericArrayType) {
			Class elementClass = getClass(((GenericArrayType) javaClass).getGenericComponentType());
			return Array.newInstance(elementClass,0).getClass();
		}
		if (javaClass instanceof Class) {
			return (Class) javaClass;
		} else if (javaClass instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) javaClass;
			if (pt.getRawType() instanceof Class) {
				return (Class) pt.getRawType();
			}
		}
		return null;
	}

	public static ApiException replaceValue(ApiException apiex, String s) {
		return apiex.replaceMessage(apiex.getMessage().replace("{value}", s));
	}

	public static Object getEnumConstant(Class clz, Object value) {
		for (Object option: clz.getEnumConstants()) {
			if (((Enum)option).name().equals(value)) {
				return option;
			}
		}
		throw new AssertionError();
	}

	public static String[] getParameterNames(Class owner, String name) throws IOException {
		owner = getNonSyntheticClass(owner);
		String className = owner.getName();
		int idx = className.lastIndexOf('.');
		if (idx >= 0) {
			className = className.substring(idx+1);
		}
		InputStream classFile = owner.getResourceAsStream(className + ".class");
		if (classFile == null) throw new IllegalArgumentException("Cannot find class file for "+owner);
		ClassParser parser = new ClassParser(classFile, owner.getSimpleName() + ".class");
		JavaClass clazz = parser.parse();

		for (Method m : clazz.getMethods()) {
			if (!m.getName().equals(name) || m.isStatic()) continue;
			int size = m.getArgumentTypes().length;
			String[] names = new String[size]; // exclude 'this'
			for (int i = 0; i < size; i++) {
				LocalVariable variable = m.getLocalVariableTable().getLocalVariable(i+1);
				names[i] = variable.getName();
			}
			return names;
		}
		throw new IllegalArgumentException("No such method "+name);
	}

	public static Class getNonSyntheticClass(Class owner) {
		while (owner.getName().contains("$$")) {
			owner = owner.getSuperclass();
		}
		return owner;
	}

	public static Cloner cloner = new Cloner();

	public static void dontClone(final Class<?>... c) {
		cloner.dontClone(c);
	}

	static <T> T deepClone(T object) {
		try {
			return cloner.deepClone(object);
		} catch (java.lang.IncompatibleClassChangeError e) {
			System.out.println(object.getClass());
			throw e;
		}
	}

//	static <T> T deepClone(T object) {
//		ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
//		try {
//			ObjectOutputStream oos = new ObjectOutputStream(baos);
//			oos.writeObject(object);
//			oos.close();
//			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
//			ObjectInputStream ois = new ObjectInputStream(bais);
//			return (T)ois.readObject();
//		} catch (RuntimeException e) {
//			throw e;
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}

	public static Node find(Node element, String name) {
		for (Node child: it(element.getChildNodes())) {
			if (name.equals(child.getNodeName())) {
				return child;
			}
		}
		return null;
	}

	public static Iterable<Node> it(final NodeList nodeList) {
		return new Iterable<Node>() {
			@Override
			public Iterator<Node> iterator() {
				return new Iterator<Node>() {
					int i = 0;
					@Override
					public boolean hasNext() {
						return i < nodeList.getLength();
					}

					@Override
					public Node next() {
						return nodeList.item(i++);
					}

					@Override
					public void remove() {
					}
				};
			}
		};
	}

}
