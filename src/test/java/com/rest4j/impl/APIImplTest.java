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
import com.rest4j.impl.model.API;
import com.rest4j.impl.model.*;
import com.rest4j.impl.petapi.*;
import com.rest4j.json.JSONArray;
import com.rest4j.json.JSONException;
import com.rest4j.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class APIImplTest {
	private PetMapping customMapping = new PetMapping();
	APIImpl api;
	int deleted;
	String access_token;
	Pet pet;
	int patchedId;
	Patch<Pet> patch;
	Pet patchedPet;
	String testProp;
	Pet created;
	String type;
	List<Pet> listedPets = new ArrayList<Pet>();
	boolean patchCalled;

	private Object pets = new Object() {
		public List<Pet> list(String type) {
			APIImplTest.this.type = type;
			return listedPets;
		}
		public Pet get(int id) { return pet; }
		public UpdateResult create(Pet newPet) { created = newPet; return new UpdateResult(); }
		public void delete(int id, String access_token) {
			deleted = id;
			APIImplTest.this.access_token = access_token;
		}
		public void put(int id, Patch<Pet> patch) {
			patchedId = id;
			APIImplTest.this.patch = patch;
		}
		public void patch(int id, Patch<Pet> patch) {
			patchCalled = true;
			put(id, patch);
		}
	};
	private boolean writeonly;
	private ServiceProvider serviceProvider;
	private API root;
	private JAXBContext context;
	private boolean called;

	@Before
	public void init() throws JAXBException, ConfigurationException {
		pet = new Pet();
		pet.setName("Max");
		pet.setGender(Gender.male);
		pet.setId(555);
		pet.setType("cat");
		pet.setWriteonly(true);
		pet.setAte(Collections.singletonList(666));

		context = JAXBContext.newInstance("com.rest4j.impl.model");

		JAXBElement<API> element = (JAXBElement<API>) context.createUnmarshaller().unmarshal(MarshallerImplTest.class.getResourceAsStream("petapi.xml"));
		root = element.getValue();

		serviceProvider = new ServiceProvider() {
			@Override
			public Object lookupService(String name) {
				if ("pets".equals(name)) return pets;
				return null;
			}

			@Override
			public Object lookupFieldMapper(String model, String name) {
				if ("petMapping".equals(name)) return customMapping;
				return null;
			}

			@Override
			public Converter lookupConverter(String name) {
				return null;
			}
		};
		api = new APIImpl(root, "/api/v2", serviceProvider);
	}

	@Test public void testConstructor_endpoints() throws NoSuchMethodException {
		// check that endpoints are constructed correctly
		assertEquals(6, api.endpoints.size());
		APIImpl.EndpointMapping endpoint = api.endpoints.get(0);
		assertSame(pets, endpoint.service);
		assertEquals(pets.getClass().getDeclaredMethod("list", String.class), endpoint.method);
		assertEquals("GET", endpoint.httpMethod);
		assertEquals(1, endpoint.args.length);
		assertNotNull(endpoint.endpoint);
		assertTrue(endpoint.pathMatcher.matches("/pets"));
	}

	@Test public void testFindEndpoint_not_found() throws IOException, ApiException {
		ApiRequest request = mock(ApiRequest.class);
		when(request.method()).thenReturn("GET");
		when(request.path()).thenReturn("/api/v2/pets/xxx/zzz");
		try {
			api.findEndpoint(request);
			fail();
		} catch (ApiException ex) {
			assertEquals(404, ex.getHttpStatus());
		}
	}

	@Test public void testFindEndpoint_wrong_method() throws IOException, ApiException {
		ApiRequest request = mock(ApiRequest.class);
		when(request.method()).thenReturn("SNAP");
		when(request.path()).thenReturn("/api/v2/pets/xxx");
		try {
			api.findEndpoint(request);
			fail();
		} catch (ApiException ex) {
			assertEquals(405, ex.getHttpStatus());
		}
	}

	@Test public void testFindEndpoint_success() throws IOException, ApiException {
		ApiRequest request = mock(ApiRequest.class);
		when(request.method()).thenReturn("GET");
		when(request.path()).thenReturn("/api/v2/pets/555");
		APIImpl.EndpointMapping endpoint = api.findEndpoint(request);
		assertEquals("get", endpoint.method.getName());
	}

	@Test public void testGetAllowedMethods() throws IOException, ApiException {
		ApiRequest request = mock(ApiRequest.class);
		when(request.method()).thenReturn("GET");
		when(request.path()).thenReturn("/api/v2/pets/555");
		List<String> allowedMethods = api.getAllowedMethods(request);
		assertEquals("[DELETE, GET, PUT, PATCH]", allowedMethods.toString());
	}

	@Test public void testParseParam_missing() {
		Parameter param = new Parameter();
		param.setType(FieldType.BOOLEAN);
		param.setName("test");
		param.setOptional(false);
		parseParam_exception(param, null, 400, "Absent parameter test");
	}

	@Test public void testParseParam_default() throws ApiException {
		Parameter param = new Parameter();
		param.setType(FieldType.STRING);
		param.setName("test");
		param.setOptional(true);
		param.setDefault("DEFLT");
		assertEquals("DEFLT", api.parseParam(param, null));
	}

	@Test public void testParseParam_wrong_boolean() throws ApiException {
		Parameter param = new Parameter();
		param.setType(FieldType.BOOLEAN);
		param.setName("test");
		parseParam_exception(param, "", 400, "Wrong parameter 'test' value:");
		parseParam_exception(param, "234", 400, "Wrong parameter 'test' value:");
	}

	@Test public void testParseParam_wrong_number() throws ApiException {
		Parameter param = new Parameter();
		param.setType(FieldType.NUMBER);
		param.setName("test");
		parseParam_exception(param, "", 400, "Wrong numeric parameter 'test' value:");
		parseParam_exception(param, "1122123123123123123123123123123", 400, "Wrong numeric parameter 'test' value:");
	}

	@Test public void testParseParam_good_boolean() throws ApiException {
		Parameter param = new Parameter();
		param.setType(FieldType.BOOLEAN);
		param.setName("test");
		assertEquals(true, api.parseParam(param, "yes"));
		assertEquals(true, api.parseParam(param, "true"));
		assertEquals(false, api.parseParam(param, "no"));
		assertEquals(false, api.parseParam(param, "false"));
	}

	@Test public void testParseParam_good_number() throws ApiException {
		Parameter param = new Parameter();
		param.setType(FieldType.NUMBER);
		param.setName("test");
		assertEquals(123, ((Number) api.parseParam(param, "123")).intValue());
		assertEquals(123.45, ((Number) api.parseParam(param, "123.45")).doubleValue(), 1e-10);
	}

	@Test public void testParseParam_enum() throws ApiException {
		Parameter param = new Parameter();
		param.setType(FieldType.STRING);
		param.setName("test");
		Values values = new Values();
		values.getValue().add(value("one"));
		values.getValue().add(value("two"));
		values.getValue().add(value("three"));
		param.setValues(values);
		assertEquals("one", api.parseParam(param, "one"));
		parseParam_exception(param, "four", 400, "Wrong parameter 'test' value: expected one of one, two, three");
	}

	Value value(String name) {
		Value v = new Value();
		v.setContent(name);
		return v;
	}

	void parseParam_exception(Parameter param, String val, int status, String msg) {
		try {
			api.parseParam(param, val);
			fail();
		} catch (ApiException ex) {
			assertEquals(status, ex.getHttpStatus());
			if (msg != null) assertTrue(ex.getMessage(), ex.getMessage().contains(msg));
		}
	}

	@Test(expected=ApiException.class) public void testServe_wrong_prefix() throws IOException, ApiException {
		ApiRequest request = mockRequest("GET", "/xxx");
		api.serve(request);
	}

	@Test public void testServe_options() throws IOException, ApiException {
		ApiRequest request = mockRequest("OPTIONS", "/api/v2/pets/555");
		ApiResponse response = api.serve(request);
		assertEquals("*", getHeader(response, "Access-Control-Allow-Origin"));
		assertEquals("DELETE, GET, PUT, PATCH, OPTIONS", getHeader(response, "Access-Control-Allow-Methods"));

		when(request.path()).thenReturn("/api/v2/xxx");
		try {
			api.serve(request);
			fail();
		} catch (ApiException ex) {
			assertEquals(404, ex.getHttpStatus());
		}
	}

	@Test public void testServe_invoke() throws IOException, ApiException {
		ApiRequest request = mockRequest("DELETE", "/api/v2/pets/555");
		when(request.param("access_token")).thenReturn("123123123");
		ApiResponse response = api.serve(request);
		assertEquals(555, deleted);
		assertEquals("123123123", access_token);
	}

	@Test public void testServe_get_json_object() throws IOException, ApiException, JSONException {
		ApiRequest request = mockRequest("GET", "/api/v2/pets/555");
		ApiResponse response = api.serve(request);
		assertEquals(0, deleted);
		assertNull(access_token);
		assertEquals(200, response.getStatus());
		JSONObject json = (JSONObject) ((JSONResource)response.getResource()).getJSONObject();
		assertEquals(555, json.getInt("id"));
		assertEquals("Max", json.getString("name"));
		assertEquals("male", json.getString("gender"));
		assertEquals("cat", json.getString("type"));
		assertNull(json.opt("writeonly"));
		JSONArray relations = json.getJSONArray("relations");
		assertEquals(1, relations.length());
		assertEquals("ate", relations.getJSONObject(0).getString("type"));
		assertEquals(666, relations.getJSONObject(0).getInt("petId"));
	}

	@Test public void testServe_etag() throws IOException, ApiException, JSONException {
		ApiRequest request = mockRequest("GET", "/api/v2/pets/555");
		when(request.header("If-None-Match")).thenReturn("\"xxx\"");
		ApiResponseImpl response = (ApiResponseImpl) api.serve(request);

		assertNotNull(response.response);
		String etag = response.response.getETag();

		when(request.header("If-None-Match")).thenReturn(etag);
		try {
			api.serve(request);
			fail();
		} catch (ApiException ex) {
			assertEquals(304, ex.getHttpStatus());
			assertEquals(etag, ex.getHeader("ETag"));
		}
	}

	@Test public void testServe_mandatory_param() throws IOException, ApiException, JSONException {
		ApiRequest request = mockRequest("PUT", "/api/v2/pets/555");
		try {
			api.serve(request);
			fail();
		} catch (ApiException ex) {
			assertEquals(400, ex.getHttpStatus());
			assertEquals("Absent parameter access_token", ex.getMessage());
		}
	}

	@Test public void testServe_post() throws Exception {
		ApiRequest request = mockRequest("POST", "/api/v2/pets");
		when(request.param("access_token")).thenReturn("xxx");
		JSONObject json = MarshallerImplTest.createMaxJson();
		when(request.objectInput()).thenReturn(json);
		ApiResponseImpl response = (ApiResponseImpl) api.serve(request);

		assertEquals(200, response.getStatus());
		assertTrue(response.getResource() instanceof JSONResource);
		assertEquals(0, created.getId());
		assertEquals(Collections.singletonList(234), created.getFriends());
		assertEquals(4.3, created.getPetWeight(), 1e-5);
	}

	@Test public void testServe_put_as_patch() throws Exception {
		ApiRequest request = mockRequest("PUT", "/api/v2/pets/555");
		when(request.param("access_token")).thenReturn("xxx");
		JSONObject json = new JSONObject("{id:5,weight:5.67}");
		when(request.objectInput()).thenReturn(json);
		api.serve(request);

		assertEquals(555, patchedId);
		assertEquals(555, patch.getPatched().getId()); // shouldn't change
		assertEquals(Collections.singletonList(666), patch.getPatched().getAte());
		assertEquals(5.67, patch.getPatched().getPetWeight(), 1e-5);
		assertFalse(patchCalled);
	}

	@Test public void testServe_patch() throws Exception {
		ApiRequest request = mockRequest("PATCH", "/api/v2/pets/555");
		JSONObject json = new JSONObject("{id:5,weight:5.67}");
		when(request.objectInput()).thenReturn(json);
		api.serve(request);

		assertEquals(555, patchedId);
		assertEquals(555, patch.getPatched().getId()); // shouldn't change
		assertTrue(patchCalled);
	}

	@Test public void testServe_put_as_object_and_params() throws Exception {
		pets = new Object() {
			public List<Pet> list() { return null; }
			public Pet get(int id) { return pet; }
			public UpdateResult create(Pet newPet) { return null; }
			public void delete(int id, String access_token) {
			}
			public void put(int id, Pet patchedPet, boolean writeonly) {
				patchedId = id;
				APIImplTest.this.patchedPet = patchedPet;
				APIImplTest.this.writeonly = writeonly;
			}
			public void patch(int id, Patch<Pet> patch) {}
		};
		api = new APIImpl(root, "/api/v2", serviceProvider);

		ApiRequest request = mockRequest("PUT", "/api/v2/pets/555");
		when(request.param("access_token")).thenReturn("xxx");
		JSONObject json = new JSONObject();
		json.put("id", 5);
		json.put("name", "Max1");
		json.put("writeonly", true);
		when(request.objectInput()).thenReturn(json);

		ApiResponseImpl response = (ApiResponseImpl) api.serve(request);

		assertEquals(555, patchedId);
		assertEquals(555, patchedPet.getId()); // shouldn't change
		assertEquals("Max1", patchedPet.getName());
		assertEquals("Max", pet.getName());
		assertEquals(Collections.singletonList(666), patchedPet.getAte());
		assertEquals(true, patchedPet.isWriteonly());
		assertEquals(true, APIImplTest.this.writeonly);
	}

	@Test public void testServe_if_match() throws Exception {
		ApiRequest request = mockRequest("GET", "/api/v2/pets/555");

		ApiResponseImpl response = (ApiResponseImpl) api.serve(request);
		String etag = response.response.getETag();

		when(request.method()).thenReturn("PUT");
		when(request.param("access_token")).thenReturn("xxx");
		when(request.header("If-Match")).thenReturn(etag);
		JSONObject json = new JSONObject();
		when(request.objectInput()).thenReturn(json);
		api.serve(request);
		try {
			when(request.header("If-Match")).thenReturn("xxx");
			api.serve(request);
			fail();
		} catch (ApiException ex) {
			assertEquals(412, ex.getHttpStatus());
		}
	}

	@Test public void testServe_not_mapped_prop_as_a_param() throws Exception {
		pets = new Object() {
			public Pet get(int id) { return pet; }
			public void put(int id, Pet patched, String testProp) {
				patchedId = id;
				APIImplTest.this.patchedPet = patched;
				APIImplTest.this.testProp = testProp;
			}
		};
		api = (APIImpl) new ApiFactory(getClass().getResource("petapi1.xml"), "/api/v2", serviceProvider).createAPI();

		ApiRequest request = mockRequest("PUT", "/api/v2/pets/555");
		JSONObject json = new JSONObject();
		json.put("testProp", "TEST");
		when(request.objectInput()).thenReturn(json);

		ApiResponseImpl response = (ApiResponseImpl) api.serve(request);

		assertEquals("TEST", testProp);
	}

	@Test public void testServe_httpsonly_param() throws Exception {
		pets = new Object() {
			public Pet get(int id, String access_token) { return new Pet(); }
			public void put(int id, Pet patched) { }
		};
		api = (APIImpl) new ApiFactory(getClass().getResource("securepetapi.xml"), "/api/v2", serviceProvider).createAPI();
		ApiRequest request = mockRequest("GET", "/api/v2/pets/555");
		ApiResponse response = api.serve(request);

		try {
			when(request.param("access_token")).thenReturn("TOKEN");
			api.serve(request);
			fail();
		} catch (ApiException ex) {
			assertEquals(400, ex.getHttpStatus());
			assertEquals("Bad request. Parameter access_token can only be sent over HTTPS.", ex.getMessage());
		}

		when(request.https()).thenReturn(true);
		api.serve(request);
	}

	@Test public void testServe_httpsonly_endpoint() throws Exception {
		pets = new Object() {
			public Pet get(int id, String access_token) { return null; }
			public void put(int id, Pet patched) { }
		};
		api = (APIImpl) new ApiFactory(getClass().getResource("securepetapi.xml"), "/api/v2", serviceProvider).createAPI();

		ApiRequest request = mockRequest("PUT", "/api/v2/pets/555");
		when(request.objectInput()).thenReturn(new JSONObject());

		try {
			api.serve(request);
			fail();
		} catch(ApiException ex) {
			assertEquals(400, ex.getHttpStatus());
			assertEquals("This request can only be sent over HTTPS.", ex.getMessage());
		}

		when(request.https()).thenReturn(true);
		api.serve(request);
	}

	@Test public void testServe_exception_handling() throws Exception {
		pets = new Object() {
			public Pet get(int petId) throws PetIndisposedException {
				throw new PetIndisposedException("Max");
			}
		};
		api = (APIImpl) new ApiFactory(getClass().getResource("petapi-exceptions.xml"), "/api/v2", serviceProvider).createAPI();
		ApiRequest request = mockRequest("GET", "/api/v2/pets/555");

		try {
			api.serve(request);
			fail();
		} catch (ApiException ex) {
			assertEquals(400, ex.getHttpStatus());
			assertEquals("INDISPOSED", ((JSONObject)ex.getJSONResponse().getJSONObject()).getString("code"));
		}
	}

	@Test public void testServe_enum_in_params() throws Exception {
		ApiRequest request = mockRequest("GET", "/api/v2/pets");
		when(request.param("type")).thenReturn("cat");
		listedPets = Collections.singletonList(pet);

		// 1. successful attempt
		ApiResponse response = api.serve(request);
		assertEquals("cat", type);
		JSONArray array = (JSONArray) ((JSONResource)response.getResource()).getJSONObject();
		assertEquals(1, array.length());
		assertEquals("Max", array.getJSONObject(0).getString("name"));

		// 2. failed attempt
		when(request.param("type")).thenReturn("WRONG");
		try {
			api.serve(request);
			fail();
		} catch (ApiException ex) {
			assertEquals(400, ex.getHttpStatus());
			assertEquals("Wrong parameter 'type' value: expected one of dog, cat, hamster", ex.getMessage());
		}
	}

	@Test public void testServer_null_primitive_param() throws Exception {
		pets = new Object() {
			public List<Pet> list(String type) { return null; }
			public Pet get(int id) { return null; }
			public UpdateResult create(Pet newPet, double weight /* weight should not be null */) {
				return null;
			}
			public void delete(int id, String access_token) {}
			public void put(int id, Patch<Pet> patch) {}
			public void patch(int id, Patch<Pet> patch) {}
		};
		api = new APIImpl(root, "/api/v2", serviceProvider);
		ApiRequest request = mockRequest("POST", "/api/v2/pets");
		when(request.param("access_token")).thenReturn("TOKEN");
		when(request.objectInput()).thenReturn(new JSONObject("{id:123,name:'max',gender:'male',writeonly:true}"));

		try {
			api.serve(request);
			fail();
		} catch (ApiException ex) {
			assertEquals(400, ex.getHttpStatus());
			assertEquals("Field Pet.weight is absent", ex.getMessage());
		}
	}

	@Test public void testServe_jsonp_json() throws Exception {
		iniJsonpApi();
		ApiRequest request = mockRequest("GET", "/api/v2/pet");
		when(request.param("callback")).thenReturn("callback_func");

		ApiResponse response = api.serve(request);
		assertEquals("callback_func({\"id\":0})", getBody(response));
	}

	@Test public void testServe_jsonp_no_callback_parameter() throws Exception {
		iniJsonpApi();
		ApiRequest request = mockRequest("GET", "/api/v2/pet");

		ApiResponse response = api.serve(request);
		assertEquals("{\"id\":0}", getBody(response));
	}

	@Test public void testServe_jsonp_plain_text() throws Exception {
		iniJsonpApi();
		ApiRequest request = mockRequest("GET", "/api/v2/pet/text");
		when(request.param("callback")).thenReturn("callback_func");

		ApiResponse response = api.serve(request);
		assertEquals("callback_func(\"Just a pet\\n\")", getBody(response));
	}

	@Test public void testServe_jsonp_binary() throws Exception {
		iniJsonpApi();
		ApiRequest request = mockRequest("GET", "/api/v2/pet/binary");
		when(request.param("callback")).thenReturn("callback_func");

		ApiResponse response = api.serve(request);
		assertEquals("\001\002\003\004\005", getBody(response)); // no jsonp support for binaries
	}

	@Test public void testServe_map_response_body() throws Exception {
		pets = new Object() {
			public Map<String,Pet> get() {
				Map<String,Pet> pets = new HashMap<String,Pet>();
				pets.put("Max", MarshallerImplTest.createMax());
				return pets;
			}
		};
		api = (APIImpl) new ApiFactory(getClass().getResource("map-type.xml"), "/api/v2", serviceProvider).createAPI();
		ApiRequest request = mockRequest("GET", "/api/v2/test");
		ApiResponse response = api.serve(request);
		assertEquals("{\"Max\":{\"id\":123,\"name\":\"Max\"}}", getBody(response));
	}

	@Test public void testServe_set_response_body() throws Exception {
		pets = new Object() {
			public Set<Pet> get() {
				Set<Pet> pets = new LinkedHashSet<Pet>();
				pets.add(MarshallerImplTest.createMax());
				return pets;
			}
		};
		api = (APIImpl) new ApiFactory(getClass().getResource("set-type.xml"), "/api/v2", serviceProvider).createAPI();
		ApiRequest request = mockRequest("GET", "/api/v2/test");
		ApiResponse response = api.serve(request);
		assertEquals("[{\"id\":123,\"name\":\"Max\"}]", getBody(response));
	}

	@Test public void testServe_optional_request() throws Exception {
		pets = new Object() {
			public Pet post(Pet pet) {
				called = true;
				assertNull(pet);
				pet = new Pet();
				pet.setId(123);
				return pet;
			}
		};
		api = (APIImpl) new ApiFactory(getClass().getResource("optional-body.xml"), "/api/v2", serviceProvider).createAPI();
		ApiRequest request = mockRequest("POST", "/api/v2/test");
		when(request.objectInput()).thenThrow(new ApiException("Missing body"));
		ApiResponse response = api.serve(request);
		assertTrue(called);
		assertEquals("{\"id\":123,\"name\":null}", getBody(response));
	}

	@Test public void testServe_optional_response() throws Exception {
		pets = new Object() {
			public Pet post(Pet pet) {
				called = true;
				return null;
			}
		};
		api = (APIImpl) new ApiFactory(getClass().getResource("optional-body.xml"), "/api/v2", serviceProvider).createAPI();
		ApiRequest request = mockRequest("POST", "/api/v2/test");
		ApiResponse response = api.serve(request);
		assertTrue(called);
		assertEquals("", getBody(response));
	}

	void iniJsonpApi() throws ConfigurationException {
		pets = new Object() {
			public Pet getJson() { return new Pet(); }
			public Reader getText() { return new StringReader("Just a pet\n"); }
			public InputStream getBinary() { return new ByteArrayInputStream(new byte[]{1,2,3,4,5}); }
		};
		api = (APIImpl) new ApiFactory(getClass().getResource("jsonp-api.xml"), "/api/v2", serviceProvider).createAPI();
	}

	String getBody(ApiResponse response) throws IOException {
		MockHttpServletResponse mockResponse = new MockHttpServletResponse();
		response.outputBody(mockResponse);
	    return mockResponse.getContentAsString();
	}

	ApiRequest mockRequest(String method, String path) {
		ApiRequest request = mock(ApiRequest.class);
		when(request.method()).thenReturn(method);
		when(request.path()).thenReturn(path);
		return request;
	}

	private String getHeader(ApiResponse response, String name) {
		for (Headers.Header header: ((ApiResponseImpl)response).headers.headers) {
			if (header.name.equalsIgnoreCase(name)) return header.value;
		}
		return null;
	}

}
