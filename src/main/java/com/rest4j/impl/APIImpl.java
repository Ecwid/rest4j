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

import com.rest4j.API;
import com.rest4j.*;
import com.rest4j.impl.model.ContentType;
import com.rest4j.impl.model.*;
import com.rest4j.impl.model.Error;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class APIImpl implements API {
	final com.rest4j.impl.model.API root;
	final String pathPrefix;
	final Marshaller marshaller;
	final List<EndpointImpl> endpoints = new ArrayList<EndpointImpl>();
	final ServiceProvider serviceProvider;
	final ResourceFactory resourceFactory;

	public APIImpl(com.rest4j.impl.model.API root, String pathPrefix, CustomMapping customMapping, ServiceProvider daoProvider) throws ConfigurationException {
		this.pathPrefix = pathPrefix;
		this.root = root;
		this.serviceProvider = daoProvider;
		List<Model> modelConfig = new ArrayList<Model>();
		for (Object child: root.getEndpointAndModel()) {
			if (child instanceof Model) {
				modelConfig.add((Model)child);
			}
		}
		marshaller = new Marshaller(modelConfig, customMapping);
		resourceFactory = new ResourceFactory(marshaller);
		for (Object child: root.getEndpointAndModel()) {
			if (child instanceof Endpoint) {
				Endpoint endpoint = (Endpoint)child;
				endpoints.add(new EndpointImpl(endpoint));
			}
		}
	}

	public void addObjectFactory(ObjectFactoryChain of) {
		marshaller.addObjectFactory(of);
	}

	@Override
	public APIResponse serve(APIRequest request) throws IOException, APIException {
		if (!request.path().startsWith(pathPrefix)) {
			throw new APIException(404, "Wrong path: " + request.path() + ", does not match the path prefix '" + pathPrefix + "'");
		}
		if (request.method().equals("OPTIONS")) {
			try {
				findEndpoint(request);
			} catch (APIException apiex) {
				if (apiex.getStatus() != 405) {
					throw apiex;
				}
			}
			// enable cross-domain queries
			return new APIResponseImpl()
					.addHeader("Access-Control-Allow-Origin", "*")
					.addHeader("Access-Control-Allow-Methods", getAllowedMethodsString(request))
					.addHeader("Access-Control-Max-Age", "10000000");
		}
		EndpointImpl endpoint = findEndpoint(request);

		if (endpoint.httpsonly && !request.https()) {
			throw new APIException(400, "This request can only be sent over HTTPS.");
		}
		Object getResult = null;
		if (!request.method().equals("GET")) {
			APIRequest get = changeMethod(request, "GET");
			EndpointImpl getEndpoint = findEndpoint(get);
			if (request.header("If-Match") != null || endpoint.isPatch()) {
				// first perform GET, then decide if we should change the resource
				getResult = getEndpoint.invokeRaw(request, null);
			}
			if (request.header("If-Match") != null) {
				Resource getResource = resourceFactory.createResourceFrom(getResult, getEndpoint.getResponseContentType());
				if (!parseList(request.header("If-Match")).contains(getResource.getETag())) {
					throw new APIException(412, "Precondition failed");
				}
			}
		}
		Resource result = endpoint.invoke(request, getResult);
		if (request.header("If-None-Match") != null && request.method().equals("GET") && result != null) {
			String etag = result.getETag();
			if (parseList(request.header("If-None-Match")).contains(etag)) {
				throw new APIException(304, "Not modified").addHeader("ETag", etag);
			}
		}
		return new APIResponseImpl(request, result).addHeader("Vary", "Accept-Encoding");
	}

	interface ArgHandler {
		Object get(APIRequest request, Object getResponse, Params params) throws IOException, APIException;
	}

	class EndpointImpl {
		Endpoint endpoint;
		StringWithParamsMatcher pathMatcher;
		String httpMethod;
		Object service;
		Method method;
		ArgHandler[] args;
		public boolean httpsonly;
		private boolean patch;

		EndpointImpl(Endpoint ep) throws ConfigurationException {
			endpoint = ep;
			pathMatcher = new StringWithParamsMatcher(ep.getRoute());
			httpMethod = ep.getHttp().name();
			service = serviceProvider.lookup(ep.getService().getName());
			httpsonly = ep.isHttpsonly();
			patch = ep.getBody() != null && ep.getBody().getPatch() != null;
			if (service == null) {
				throw new ConfigurationException("No service with name "+ep.getService().getName());
			}
			String method = ep.getService().getMethod();
			if (method == null) {
				method = httpMethod.toLowerCase();
			}
			for (Method m: service.getClass().getMethods()) {
				if ((m.getModifiers()& Modifier.STATIC) != 0 || !m.getName().equals(method)) continue;
				this.method = m;
			}
			if (this.method == null) {
				throw new ConfigurationException("Cannot find non-static method with name "+method+" in "+ service.getClass());
			}

			int argCount = this.method.getParameterTypes().length;
			String[] paramNames;
			try {
				paramNames = Util.getParameterNames(service.getClass(), method);
			} catch (IOException e) {
				throw new ConfigurationException("Cannot parse class "+ service.getClass()+": "+e.getMessage());
			}
			assert paramNames.length == argCount;

			Type[] paramTypes = this.method.getGenericParameterTypes();
			boolean bodyParamFound = false;
			boolean paramsParamFound = false;
			args = new ArgHandler[argCount];
			for (int i=0; i<argCount; i++) {
				final String name = paramNames[i];
				Parameter param = null;
				for (Parameter x : ep.getParameters().getParameter()) {
					if (!name.equals(x.getName())) continue;
					param = x;
					break;
				}
				final Type paramType = paramTypes[i];
				final FieldImpl writeOnlyField;
				if (param == null) {
					// try 'params' param
					if (paramType == Params.class) {
						if (paramsParamFound) {
							throw new ConfigurationException("Cannot be two Params arguments in method "+this.method);
						}
						paramsParamFound = true;
						args[i] = new ArgHandler() {
							@Override
							public Object get(APIRequest request, Object getResponse, Params params) {
								return params;
							}
						};
					} else if ((writeOnlyField = checkFieldAsArgument(name, paramType)) != null) {
						args[i] = new ArgHandler() {
							@Override
							public Object get(APIRequest request, Object getResponse, Params params) throws IOException, APIException {
								Object val = request.objectInput().opt(name);
								val = writeOnlyField.unmarshal(val, "body");
								return writeOnlyField.type.cast(val, paramType);
							}
						};
					} else {
						if (bodyParamFound || endpoint.getBody() == null) {
							throw new ConfigurationException("Unrecognized parameter '"+name+"' in method "+this.method+": should be either of the type Params or be one of the declared parameters of the API call");
						}
						// try body parameter
						args[i] = checkBodyType(endpoint.getBody(), paramType);
						bodyParamFound = true;
					}
				} else {
					String[] enumValues = null;
					if (param.getValues() != null) {
						List<String> list = param.getValues().getValue();
						enumValues = list.toArray(new String[list.size()]);
					}
					final SimpleApiType paramApiType;
					try {
						Object defaultValue = param.getDefault() == null ? null : parseParam(param, param.getDefault());
						paramApiType = SimpleApiType.create(param.getType(), defaultValue, enumValues);
					} catch (APIException e) {
						throw new ConfigurationException("Cannot parse default param value "+param.getDefault()+": "+e.getMessage());
					}
					if (!paramApiType.check(paramType)) {
						throw new ConfigurationException("Wrong argument '"+name+"' type: expected "+paramApiType.getJavaName());
					}
					args[i] = new ArgHandler() {
						@Override
						public Object get(APIRequest request, Object getResponse, Params params) throws IOException, APIException {
							return paramApiType.cast(params.get(name), paramType);
						}
					};
				}
			}

			if (endpoint.getResponse() != null) {
				resourceFactory.checkContentType(endpoint.getResponse(), this.method);
			}
		}

		private FieldImpl checkFieldAsArgument(String name, Type paramType) throws ConfigurationException {
			if (endpoint.getBody() == null || endpoint.getBody().getJson() == null && endpoint.getBody().getPatch() == null)
				return null;

			JsonType jsonType = endpoint.getBody().getJson();
			final ObjectApiType objectType;
			if (jsonType != null) objectType = marshaller.getObjectType(jsonType.getType());
			else objectType = marshaller.getObjectType(endpoint.getBody().getPatch().getType());

			for (FieldImpl field: objectType.fields) {
				if (field.name.equals(name) && field.access != FieldAccessType.READONLY) {
					// both name and type match
					if (field.type.check(paramType)) return field;
				}
			}
			return null;
		}

		private ArgHandler checkBodyType(ContentType contentType, Type type) throws ConfigurationException {
			Class expect;
			ArgHandler argHandler;
			if (contentType.getBinary() != null) {
				expect = InputStream.class;
				argHandler = new ArgHandler() {
					@Override
					public Object get(APIRequest request, Object getResponse, Params params) throws IOException, APIException {
						return request.binaryInput();
					}
				};
			} else if (endpoint.getBody().getText() != null) {
				expect = Reader.class;
				argHandler = new ArgHandler() {
					@Override
					public Object get(APIRequest request, Object getResponse, Params params) throws IOException, APIException {
						return request.textInput();
					}
				};
			} else {
				if (endpoint.getBody().getJson() != null) {
					JsonType jsonType = endpoint.getBody().getJson();
					expect = marshaller.getClassForModel(jsonType.getType());
					if (jsonType.isArray()) {
						if (type instanceof ParameterizedType) {
							ParameterizedType ptype = (ParameterizedType) type;
							if (ptype.getRawType() == List.class) {
								if (ptype.getActualTypeArguments()[0] == expect) {
									final ApiType arrayType = marshaller.getArrayType(jsonType.getType());
									return new ArgHandler() {
										@Override
										public Object get(APIRequest request, Object getResponse, Params params) throws IOException, APIException {
											return arrayType.unmarshal(request.arrayInput());
										}
									};
								}
							}
						}
						throw new ConfigurationException("Body argument of " + this.method + " is expected to be List<" + expect.getName() + ">");
					}
					final ApiType objectType = marshaller.getObjectType(jsonType.getType());
					argHandler = new ArgHandler() {
						@Override
						public Object get(APIRequest request, Object getResponse, Params params) throws IOException, APIException {
							return objectType.unmarshal(request.objectInput());
						}
					};
				} else if (endpoint.getBody().getPatch() != null) {
					PatchType patchType = endpoint.getBody().getPatch();
					expect = marshaller.getClassForModel(patchType.getType());
					if (type instanceof ParameterizedType) {
						ParameterizedType ptype = (ParameterizedType) type;
						if (ptype.getRawType() == Patch.class) {
							if (ptype.getActualTypeArguments()[0] == expect) {
								final ObjectApiType objectType = marshaller.getObjectType(patchType.getType());
								return new ArgHandler() {
									@Override
									public Object get(APIRequest request, Object getResponse, Params params) throws IOException, APIException {
										return objectType.unmarshalPatch(getResponse, request.objectInput());
									}
								};
							}
						}
					} else if (type == expect) {
						final ObjectApiType objectType = marshaller.getObjectType(patchType.getType());
						return new ArgHandler() {
							@Override
							public Object get(APIRequest request, Object getResponse, Params params) throws IOException, APIException {
								Patch patch = objectType.unmarshalPatch(getResponse, request.objectInput());
								return patch.getPatched();
							}
						};
					}
					throw new ConfigurationException("Body argument of " + this.method + " is expected to be either "+expect.getName()+" or Patch<" + expect.getName() + ">");
				} else {
					throw new ConfigurationException("Not a well-formed <body> tag");
				}
			}
			if (type != expect) {
				throw new ConfigurationException("Body argument of "+this.method+" is expected to be "+expect);
			}
			return argHandler;
		}

		Resource invoke(APIRequest request, Object getResult) throws IOException, APIException {
			return resourceFactory.createResourceFrom(invokeRaw(request, getResult), endpoint.getResponse());
		}

		Object invokeRaw(APIRequest request, Object getResult) throws IOException, APIException {
			Map<String, String> pathParams = match(getPath(request));
			ParamsImpl params = new ParamsImpl();
			for (Parameter param: endpoint.getParameters().getParameter()) {
				String paramStringValue = pathParams.get(param.getName());
				if (paramStringValue == null) {
					paramStringValue = request.param(param.getName());
				}
				if (!StringUtils.isEmpty(paramStringValue) && param.isHttpsonly() && !request.https()) {
					throw new APIException(400, "Bad request. Parameter "+param.getName()+" can only be sent over HTTPS.");
				}
				params.put(param.getName(), parseParam(param, paramStringValue));
			}
			Object result;
			try {
				Object[] argValues = new Object[args.length];
				for (int i=0; i<args.length; i++) {
					argValues[i] = args[i].get(request, getResult, params);
				}
				result = method.invoke(service, argValues);
				return result;
			} catch (InvocationTargetException ite) {
				Throwable cause = ite.getCause();
				if (cause instanceof APIException) {
					throw (APIException)cause;
				}
				Error error = findError(cause);
				if (error != null) {
					JSONObject errJSON = (JSONObject) marshaller.getObjectType(error.getType()).marshal(cause);
					throw new APIException(error.getStatus(), cause.getMessage(), errJSON);
				}
				if (ite.getCause() instanceof RuntimeException) {
					throw (RuntimeException)ite.getCause();
				}
				throw new RuntimeException(ite.getCause());
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}

		}

		// find exception by its type
		private Error findError(Throwable cause) {
			for (Error err: endpoint.getErrors().getError()) {
				ObjectApiType type = marshaller.getObjectType(err.getType());
				if (type.clz.isAssignableFrom(cause.getClass())) {
					return err;
				}
			}
			return null;
		}

		private Map<String,String> match(String path) {
			return pathMatcher.match(path);
		}

		public boolean matches(String path) {
			return pathMatcher.matches(path);
		}

		public ContentType getResponseContentType() {
			return endpoint.getResponse();
		}

		public boolean isPatch() {
			return patch;
		}
	}

	private List<String> parseList(String header) {
		ArrayList<String> parsed = new ArrayList<String>();
		for (String part: header.split(",")) {
			parsed.add(part.trim());
		}
		return parsed;
	}

	private DelegatingAPIRequest changeMethod(final APIRequest request, final String newMethod) {
		return new DelegatingAPIRequest(request) {
			@Override
			public String method() {
				return newMethod;
			}
		};
	}

	EndpointImpl findEndpoint(APIRequest request) throws IOException, APIException {
		boolean pathFound = false;
		for (EndpointImpl endpoint: endpoints) {
			Map<String, String> pathParams = endpoint.match(getPath(request));
			if (pathParams == null) continue;
			pathFound = true;

			if (!request.method().equals(endpoint.httpMethod)) {
				continue;
			}

			return endpoint;

		}
		if (pathFound) {
			String allowMethods = getAllowedMethodsString(request);
			throw new APIException(405, "Method not allowed").addHeader("Allow", allowMethods);
		}
		throw new APIException(404, "File not found");
	}

	private String getPath(APIRequest request) {
		return request.path().substring(pathPrefix.length());
	}

	private String getAllowedMethodsString(APIRequest request) throws IOException, APIException {
		List<String> methods = getAllowedMethods(request);
		methods.add("OPTIONS");
		return StringUtils.join(methods, ", ");
	}

	@Override
	public List<String> getAllowedMethods(APIRequest request) throws IOException, APIException {
		List<String> methods = new ArrayList<String>();
		for (EndpointImpl endpoint: endpoints) {
			if (endpoint.matches(getPath(request))) {
				methods.add(endpoint.httpMethod);
			}
		}
		return methods;
	}

	static private final Pattern ISDOUBLE = Pattern.compile("[\\.eE]");

	Object parseParam(Parameter param, String valueStr) throws APIException {
		if (valueStr == null) {
			if (!param.isOptional()) {
				throw new APIException(400, "Absent parameter "+param.getName());
			}
			valueStr = param.getDefault();
		}

		if (valueStr == null) return null;
		switch (param.getType()) {
		case BOOLEAN:
			if (valueStr.equals("true") || valueStr.equals("yes") || valueStr.equals("on") || valueStr.equals("1")) {
				return Boolean.TRUE;
			}
			if (valueStr.equals("false") || valueStr.equals("no") || valueStr.equals("off") || valueStr.equals("0")) {
				return Boolean.FALSE;
			}
			throw new APIException(400, "Wrong parameter '"+param.getName()+"' value: expected one of 'true', 'false', 'yes', 'no', 'on', 'off', '1', '0'");
		case NUMBER:
			valueStr = valueStr.trim();
			try {
				if (ISDOUBLE.matcher(valueStr).find()) {
					return Double.parseDouble(valueStr);
				} else {
					return Long.parseLong(valueStr);
				}
			} catch (NumberFormatException ex) {
				throw new APIException(400, "Wrong numeric parameter '"+param.getName()+"' value: not a number or a number out of range");
			}
		case STRING:
			if (param.getValues() != null) {
				// check allowed values
				if (!param.getValues().getValue().contains(valueStr)) {
					throw new APIException(400, "Wrong parameter '"+param.getName()+"' value: expected one of "+
						StringUtils.join(param.getValues().getValue(), ", "));
				}
			}
			return valueStr;
		}
		throw new AssertionError();
	}

}
