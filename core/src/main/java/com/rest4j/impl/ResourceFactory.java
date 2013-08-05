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

import com.rest4j.*;
import com.rest4j.impl.model.ContentType;
import com.rest4j.type.ApiType;
import com.rest4j.type.ObjectApiType;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class ResourceFactory {
	Marshaller marshaller;

	public ResourceFactory(Marshaller marshaller) {
		this.marshaller = marshaller;
	}

	public void checkContentType(ContentType contentType, Method method) throws ConfigurationException {
		Type returnType = method.getGenericReturnType();
		Class returnClass = null;
		if (returnType instanceof Class) {
			returnClass = (Class) returnType;
		} else if (returnType instanceof ParameterizedType && ((ParameterizedType)returnType).getRawType() instanceof Class) {
			returnClass = (Class) ((ParameterizedType)returnType).getRawType();
		}
		if (returnClass != null && Resource.class.isAssignableFrom(returnClass)) return;
		if (contentType.getJson() != null) {
			ApiType apiType = getApiType(contentType);
			if (!apiType.check(returnType)) {
				apiType.check(returnType);
				throw new ConfigurationException("Wrong return type of "+method+". Expected "+apiType.getJavaName()+" or "+Resource.class.getName());
			}
			return;
		} else if (contentType.getBinary() != null) {
			if (returnClass != null &&
					(InputStream.class.isAssignableFrom(returnClass) ||
				byte[].class == returnClass)) return;
			throw new ConfigurationException("Wrong return type of "+method+". Expected one of InputStream, byte[], or "+Resource.class.getName());
		} else if (contentType.getText() != null) {
			if (returnClass != null &&
					(Reader.class.isAssignableFrom(returnClass) ||
					String.class == returnClass)) return;
			throw new ConfigurationException("Wrong return type of "+method+". Expected one of Reader, String, or "+Resource.class.getName());
		}
		throw new AssertionError();
	}

	ApiType getApiType(ContentType contentType) {
		ApiType apiType;
		switch (contentType.getJson().getCollection()) {
			case ARRAY:
				apiType = marshaller.getArrayType(marshaller.getObjectType(contentType.getJson().getType()));
				break;
			case SINGLETON:
				apiType = marshaller.getObjectType(contentType.getJson().getType());
				break;
			case MAP:
			default:
				apiType = marshaller.getMapType(marshaller.getObjectType(contentType.getJson().getType()));
				break;
		}
		return apiType;
	}

	public Resource createResourceFrom(Object content, ContentType contentType) throws ApiException {
		if (content instanceof Resource) return (Resource)content;
		if (contentType == null) return null; // no body expected
		if (contentType.getJson() != null) {
			if (content == null) {
				if (contentType.getJson().isOptional()) return null;
				throw new ApiException("no response").setHttpStatus(500);
			}
			ApiType apiType = getApiType(contentType);
			ApiType concreteType = apiType;
			if (apiType instanceof ObjectApiType) {
				concreteType = ((ObjectApiType)apiType).getSubtype(content.getClass());
			}
			return new JSONResource(marshaller.marshal(apiType, content), concreteType);
		} else if (contentType.getBinary() != null) {
			if (content instanceof InputStream) {
				return new BinaryResource("application/octet-stream", null, (InputStream)content);
			} else if (content instanceof byte[]) {
				return new BinaryResource((byte[])content);
			} else {
				throw new ApiException("Wrong content type: "+content.getClass()+"; expected either BinaryResource or InputStream").setHttpStatus(500);
			}
		} else if (contentType.getText() != null) {
			if (content instanceof String) {
				return new TextResource((String)content);
			} else if (content instanceof Reader) {
				return new TextResource(null, (Reader)content);
			} else {
				throw new ApiException("Wrong content type: "+content.getClass()+"; expected one of TextResource, Reader, or String").setHttpStatus(500);
			}
		}
		throw new AssertionError("Cannot return a patch");
	}
}
