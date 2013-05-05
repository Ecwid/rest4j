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
import com.rest4j.impl.model.FieldType;
import com.rest4j.impl.model.Parameter;
import com.rest4j.impl.model.Values;
import com.rest4j.impl.petapi.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class APIImplUnitTest {
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
	List<Pet> listedPets;

	private Object pets = new Object() {
		public List<Pet> list(String type) {
			APIImplUnitTest.this.type = type;
			return listedPets;
		}
		public Pet get(int id) { return pet; }
		public UpdateResult create(Pet newPet) { created = newPet; return null; }
		public void delete(int id, String access_token) {
			deleted = id;
			APIImplUnitTest.this.access_token = access_token;
		}
		public void put(int id, Patch<Pet> patch) {
			patchedId = id;
			APIImplUnitTest.this.patch = patch;
		}
	};
	private boolean writeonly;
	private ServiceProvider serviceProvider;
	private API root;
	private JAXBContext context;

	@Before
	public void init() throws JAXBException, ConfigurationException {
		pet = new Pet();
		pet.setName("Barsik");
		pet.setGender(Gender.male);
		pet.setId(555);
		pet.setType("cat");
		pet.setWriteonly(true);
		pet.setAte(Collections.singletonList(666));

		context = JAXBContext.newInstance("com.rest4j.impl.model");

		JAXBElement<API> element = (JAXBElement<API>) context.createUnmarshaller().unmarshal(MarshallerUnitTest.class.getResourceAsStream("petapi.xml"));
		root = element.getValue();

		serviceProvider = new ServiceProvider() {
			@Override
			public Object lookup(String name) {
				if ("pets".equals(name)) return pets;
				return null;
			}
		};
		api = new APIImpl(root, "/api/v2", customMapping, serviceProvider);
	}

	@Test public void testConstructor_endpoints() throws NoSuchMethodException {
		// check that endpoints are constructed correctly
		assertEquals(5, api.endpoints.size());
		APIImpl.EndpointImpl endpoint = api.endpoints.get(0);
		assertSame(pets, endpoint.service);
		assertEquals(pets.getClass().getDeclaredMethod("list", String.class), endpoint.method);
		assertEquals("GET", endpoint.httpMethod);
		assertEquals(1, endpoint.args.length);
		assertNotNull(endpoint.endpoint);
		assertTrue(endpoint.pathMatcher.matches("/pets"));
	}

	@Test public void testFindEndpoint_not_found() throws IOException, APIException {
		APIRequest request = mock(APIRequest.class);
		when(request.method()).thenReturn("GET");
		when(request.path()).thenReturn("/api/v2/pets/xxx/zzz");
		try {
			api.findEndpoint(request);
			fail();
		} catch (APIException ex) {
			assertEquals(404, ex.getStatus());
		}
	}

	@Test public void testFindEndpoint_wrong_method() throws IOException, APIException {
		APIRequest request = mock(APIRequest.class);
		when(request.method()).thenReturn("SNAP");
		when(request.path()).thenReturn("/api/v2/pets/xxx");
		try {
			api.findEndpoint(request);
			fail();
		} catch (APIException ex) {
			assertEquals(405, ex.getStatus());
		}
	}

	@Test public void testFindEndpoint_success() throws IOException, APIException {
		APIRequest request = mock(APIRequest.class);
		when(request.method()).thenReturn("GET");
		when(request.path()).thenReturn("/api/v2/pets/555");
		APIImpl.EndpointImpl endpoint = api.findEndpoint(request);
		assertEquals("get", endpoint.method.getName());
	}

	@Test public void testGetAllowedMethods() throws IOException, APIException {
		APIRequest request = mock(APIRequest.class);
		when(request.method()).thenReturn("GET");
		when(request.path()).thenReturn("/api/v2/pets/555");
		List<String> allowedMethods = api.getAllowedMethods(request);
		assertEquals("[DELETE, GET, PUT]", allowedMethods.toString());
	}

	@Test public void testParseParam_missing() {
		Parameter param = new Parameter();
		param.setType(FieldType.BOOLEAN);
		param.setName("test");
		param.setOptional(false);
		parseParam_exception(param, null, 400, "Absent parameter test");
	}

	@Test public void testParseParam_default() throws APIException {
		Parameter param = new Parameter();
		param.setType(FieldType.STRING);
		param.setName("test");
		param.setOptional(true);
		param.setDefault("DEFLT");
		assertEquals("DEFLT", api.parseParam(param, null));
	}

	@Test public void testParseParam_wrong_boolean() throws APIException {
		Parameter param = new Parameter();
		param.setType(FieldType.BOOLEAN);
		param.setName("test");
		parseParam_exception(param, "", 400, "Wrong parameter 'test' value:");
		parseParam_exception(param, "234", 400, "Wrong parameter 'test' value:");
	}

	@Test public void testParseParam_wrong_number() throws APIException {
		Parameter param = new Parameter();
		param.setType(FieldType.NUMBER);
		param.setName("test");
		parseParam_exception(param, "", 400, "Wrong numeric parameter 'test' value:");
		parseParam_exception(param, "1122123123123123123123123123123", 400, "Wrong numeric parameter 'test' value:");
	}

	@Test public void testParseParam_good_boolean() throws APIException {
		Parameter param = new Parameter();
		param.setType(FieldType.BOOLEAN);
		param.setName("test");
		assertEquals(true, api.parseParam(param, "yes"));
		assertEquals(true, api.parseParam(param, "true"));
		assertEquals(false, api.parseParam(param, "no"));
		assertEquals(false, api.parseParam(param, "false"));
	}

	@Test public void testParseParam_good_number() throws APIException {
		Parameter param = new Parameter();
		param.setType(FieldType.NUMBER);
		param.setName("test");
		assertEquals(123, ((Number) api.parseParam(param, "123")).intValue());
		assertEquals(123.45, ((Number) api.parseParam(param, "123.45")).doubleValue(), 1e-10);
	}

	@Test public void testParseParam_enum() throws APIException {
		Parameter param = new Parameter();
		param.setType(FieldType.STRING);
		param.setName("test");
		Values values = new Values();
		values.getValue().add("one");
		values.getValue().add("two");
		values.getValue().add("three");
		param.setValues(values);
		assertEquals("one", api.parseParam(param, "one"));
		parseParam_exception(param, "four", 400, "Wrong parameter 'test' value: expected one of one, two, three");
	}

	void parseParam_exception(Parameter param, String val, int status, String msg) {
		try {
			api.parseParam(param, val);
			fail();
		} catch (APIException ex) {
			assertEquals(status, ex.getStatus());
			if (msg != null) assertTrue(ex.getMessage(), ex.getMessage().contains(msg));
		}
	}

	@Test(expected=APIException.class) public void testServe_wrong_prefix() throws IOException, APIException {
		APIRequest request = mock(APIRequest.class);
		when(request.method()).thenReturn("GET");
		when(request.path()).thenReturn("/xxx");
		api.serve(request);
	}

	@Test public void testServe_options() throws IOException, APIException {
		APIRequest request = mock(APIRequest.class);
		when(request.method()).thenReturn("OPTIONS");
		when(request.path()).thenReturn("/api/v2/pets/555");
		APIResponse response = api.serve(request);
		assertEquals("*", getHeader(response, "Access-Control-Allow-Origin"));
		assertEquals("DELETE, GET, PUT, OPTIONS", getHeader(response, "Access-Control-Allow-Methods"));

		when(request.path()).thenReturn("/api/v2/xxx");
		try {
			api.serve(request);
			fail();
		} catch (APIException ex) {
			assertEquals(404, ex.getStatus());
		}
	}

	@Test public void testServe_invoke() throws IOException, APIException {
		APIRequest request = mock(APIRequest.class);
		when(request.method()).thenReturn("DELETE");
		when(request.path()).thenReturn("/api/v2/pets/555");
		when(request.param("access_token")).thenReturn("123123123");
		APIResponse response = api.serve(request);
		assertEquals(555, deleted);
		assertEquals("123123123", access_token);
	}

	@Test public void testServe_get_json_object() throws IOException, APIException, JSONException {
		APIRequest request = mock(APIRequest.class);
		when(request.method()).thenReturn("GET");
		when(request.path()).thenReturn("/api/v2/pets/555");
		APIResponse response = api.serve(request);
		assertEquals(0, deleted);
		assertNull(access_token);
		assertEquals(200, response.getStatus());
		JSONObject json = (JSONObject) response.getJSONResponse();
		assertEquals(555, json.getInt("id"));
		assertEquals("Barsik", json.getString("name"));
		assertEquals("male", json.getString("gender"));
		assertEquals("cat", json.getString("type"));
		assertNull(json.opt("writeonly"));
		JSONArray relations = json.getJSONArray("relations");
		assertEquals(1, relations.length());
		assertEquals("ate", relations.getJSONObject(0).getString("type"));
		assertEquals(666, relations.getJSONObject(0).getInt("petId"));
	}

	@Test public void testServe_etag() throws IOException, APIException, JSONException {
		APIRequest request = mock(APIRequest.class);
		when(request.method()).thenReturn("GET");
		when(request.path()).thenReturn("/api/v2/pets/555");
		when(request.header("If-None-Match")).thenReturn("\"xxx\"");
		APIResponseImpl response = (APIResponseImpl) api.serve(request);

		assertNotNull(response.response);
		String etag = response.response.getETag();

		when(request.header("If-None-Match")).thenReturn(etag);
		try {
			api.serve(request);
			fail();
		} catch (APIException ex) {
			assertEquals(304, ex.getStatus());
			assertEquals(etag, ex.getHeader("ETag"));
		}
	}

	@Test public void testServe_mandatory_param() throws IOException, APIException, JSONException {
		APIRequest request = mock(APIRequest.class);
		when(request.method()).thenReturn("PUT");
		when(request.path()).thenReturn("/api/v2/pets/555");
		try {
			api.serve(request);
			fail();
		} catch (APIException ex) {
			assertEquals(400, ex.getStatus());
			assertEquals("Absent parameter access_token", ex.getMessage());
		}
	}

	@Test public void testServe_post() throws Exception {
		APIRequest request = mock(APIRequest.class);
		when(request.method()).thenReturn("POST");
		when(request.path()).thenReturn("/api/v2/pets");
		when(request.param("access_token")).thenReturn("xxx");
		JSONObject json = MarshallerUnitTest.createBarsikJson();
		when(request.objectInput()).thenReturn(json);
		APIResponseImpl response = (APIResponseImpl) api.serve(request);

		assertEquals(200, response.getStatus());
		assertNull(response.getJSONResponse());
		assertEquals(0, created.getId());
		assertEquals(Collections.singletonList(234), created.getFriends());
		assertEquals(4.3, created.getPetWeight(), 1e-5);
	}

	@Test public void testServe_put_as_patch() throws Exception {
		APIRequest request = mock(APIRequest.class);
		when(request.method()).thenReturn("PUT");
		when(request.path()).thenReturn("/api/v2/pets/555");
		when(request.param("access_token")).thenReturn("xxx");
		JSONObject json = new JSONObject();
		json.put("id", 5); // can't change read-only prop
		json.put("weight", 5.67);
		when(request.objectInput()).thenReturn(json);
		APIResponseImpl response = (APIResponseImpl) api.serve(request);

		assertEquals(555, patchedId);
		assertEquals(555, patch.getPatched().getId()); // shouldn't change
		assertEquals(Collections.singletonList(666), patch.getPatched().getAte());
		assertEquals(5.67, patch.getPatched().getPetWeight(), 1e-5);
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
				APIImplUnitTest.this.patchedPet = patchedPet;
				APIImplUnitTest.this.writeonly = writeonly;
			}
		};
		api = new APIImpl(root, "/api/v2", customMapping, serviceProvider);

		APIRequest request = mock(APIRequest.class);
		when(request.method()).thenReturn("PUT");
		when(request.path()).thenReturn("/api/v2/pets/555");
		when(request.param("access_token")).thenReturn("xxx");
		JSONObject json = new JSONObject();
		json.put("id", 5);
		json.put("name", "Barsuk");
		json.put("writeonly", true);
		when(request.objectInput()).thenReturn(json);

		APIResponseImpl response = (APIResponseImpl) api.serve(request);

		assertEquals(555, patchedId);
		assertEquals(555, patchedPet.getId()); // shouldn't change
		assertEquals("Barsuk", patchedPet.getName());
		assertEquals("Barsik", pet.getName());
		assertEquals(Collections.singletonList(666), patchedPet.getAte());
		assertEquals(true, patchedPet.isWriteonly());
		assertEquals(true, APIImplUnitTest.this.writeonly);
	}

	@Test public void testServe_if_match() throws Exception {
		APIRequest request = mock(APIRequest.class);
		when(request.method()).thenReturn("GET");
		when(request.path()).thenReturn("/api/v2/pets/555");
		APIResponseImpl response = (APIResponseImpl) api.serve(request);
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
		} catch (APIException ex) {
			assertEquals(412, ex.getStatus());
		}
	}

	@Test public void testServe_not_mapped_prop_as_a_param() throws Exception {
		pets = new Object() {
			public Pet get(int id) { return pet; }
			public void put(int id, Pet patched, String testProp) {
				patchedId = id;
				APIImplUnitTest.this.patchedPet = patched;
				APIImplUnitTest.this.testProp = testProp;
			}
		};
		api = (APIImpl) new APIFactory(getClass().getResource("petapi1.xml"), "/api/v2", customMapping, serviceProvider).getObject();

		APIRequest request = mock(APIRequest.class);
		when(request.method()).thenReturn("PUT");
		when(request.path()).thenReturn("/api/v2/pets/555");
		JSONObject json = new JSONObject();
		json.put("testProp", "TEST");
		when(request.objectInput()).thenReturn(json);

		APIResponseImpl response = (APIResponseImpl) api.serve(request);

		assertEquals("TEST", testProp);
	}

	@Test public void testServe_httpsonly_param() throws Exception {
		pets = new Object() {
			public Pet get(int id, String access_token) { return null; }
			public void put(int id, Pet patched) { }
		};
		api = (APIImpl) new APIFactory(getClass().getResource("securepetapi.xml"), "/api/v2", customMapping, serviceProvider).getObject();

		APIRequest request = mock(APIRequest.class);
		when(request.method()).thenReturn("GET");
		when(request.path()).thenReturn("/api/v2/pets/555");
		APIResponse response = api.serve(request);

		try {
			when(request.param("access_token")).thenReturn("TOKEN");
			api.serve(request);
			fail();
		} catch (APIException ex) {
			assertEquals(400, ex.getStatus());
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
		api = (APIImpl) new APIFactory(getClass().getResource("securepetapi.xml"), "/api/v2", customMapping, serviceProvider).getObject();

		APIRequest request = mock(APIRequest.class);
		when(request.method()).thenReturn("PUT");
		when(request.path()).thenReturn("/api/v2/pets/555");
		when(request.objectInput()).thenReturn(new JSONObject());

		try {
			api.serve(request);
			fail();
		} catch(APIException ex) {
			assertEquals(400, ex.getStatus());
			assertEquals("This request can only be sent over HTTPS.", ex.getMessage());
		}

		when(request.https()).thenReturn(true);
		api.serve(request);
	}

	@Test public void testServe_exception_handling() throws Exception {
		pets = new Object() {
			public Pet get(int petId) throws PetIndisposedException {
				throw new PetIndisposedException("Barsik");
			}
		};
		api = (APIImpl) new APIFactory(getClass().getResource("petapi-exceptions.xml"), "/api/v2", customMapping, serviceProvider).getObject();

		APIRequest request = mock(APIRequest.class);
		when(request.method()).thenReturn("GET");
		when(request.path()).thenReturn("/api/v2/pets/555");

		try {
			api.serve(request);
			fail();
		} catch (APIException ex) {
			assertEquals(400, ex.getStatus());
			assertEquals("INDISPOSED", ex.getJSONResponse().getString("code"));
		}
	}

	@Test public void testServe_enum_in_params() throws Exception {
		APIRequest request = mock(APIRequest.class);
		when(request.method()).thenReturn("GET");
		when(request.path()).thenReturn("/api/v2/pets");
		when(request.param("type")).thenReturn("cat");
		listedPets = Collections.singletonList(pet);

		// 1. successful attempt
		APIResponse response = api.serve(request);
		assertEquals("cat", type);
		JSONArray array = (JSONArray) response.getJSONResponse();
		assertEquals(1, array.length());
		assertEquals("Barsik", array.getJSONObject(0).getString("name"));

		// 2. failed attempt
		when(request.param("type")).thenReturn("WRONG");
		try {
			api.serve(request);
			fail();
		} catch (APIException ex) {
			assertEquals(400, ex.getStatus());
			assertEquals("Wrong parameter 'type' value: expected one of dog, cat, hamster", ex.getMessage());
		}
	}

	private String getHeader(APIResponse response, String name) {
		for (Headers.Header header: ((APIResponseImpl)response).headers.headers) {
			if (header.name.equalsIgnoreCase(name)) return header.value;
		}
		return null;
	}

}
