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

import com.esotericsoftware.kryo.Kryo;
import com.rest4j.ApiException;
import com.sun.org.apache.bcel.internal.classfile.ClassParser;
import com.sun.org.apache.bcel.internal.classfile.JavaClass;
import com.sun.org.apache.bcel.internal.classfile.LocalVariable;
import com.sun.org.apache.bcel.internal.classfile.Method;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class Util {

	public static Class getClass(Type javaClass) {
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

	static Object getEnumConstant(Class clz, Object value) {
		for (Object option: clz.getEnumConstants()) {
			if (option.toString().equals(value)) {
				return option;
			}
		}
		throw new AssertionError();
	}

	static String[] getParameterNames(Class owner, String name) throws IOException {
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

	static Kryo kryo = new Kryo();

	static <T> T deepClone(T object) {
		return kryo.copy(object);
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

}
