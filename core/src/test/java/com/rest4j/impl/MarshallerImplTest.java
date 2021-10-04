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
import com.rest4j.impl.array.Array;
import com.rest4j.impl.array.Element;
import com.rest4j.impl.converters.*;
import com.rest4j.impl.model.API;
import com.rest4j.impl.model.FieldType;
import com.rest4j.impl.model.Model;
import com.rest4j.impl.petapi.*;
import com.rest4j.impl.polymorphic.Bird;
import com.rest4j.impl.polymorphic.Cat;
import com.rest4j.impl.polymorphic.ObjectFactory;
import com.rest4j.impl.recursive.Leaf;
import com.rest4j.impl.recursive.Root;
import com.rest4j.type.Field;
import com.rest4j.type.ObjectApiType;
import com.rest4j.json.JSONArray;
import com.rest4j.json.JSONException;
import com.rest4j.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class MarshallerImplTest {

	private Object customMapping = new PetMapping();
	private Object someMapping = new SomeMapping();
	List<MarshallerImpl.ModelConfig> modelConfig;
	MarshallerImpl marshaller;
	private ServiceProvider serviceProvider = new ServiceProvider() {
		@Override
		public Object lookupService(String name) {
			return null;
		}

		@Override
		public Object lookupFieldMapper(String model, String name) {
			return null;
		}

		@Override
		public Converter lookupConverter(String name) {
			if (name.equals("valueConverter")) return new ValueConverter();
			if (name.equals("someConverter")) return new SomeConverter();
			return null;  //To change body of implemented methods use File | Settings | File Templates.
		}
	};
	private FieldFilter filter;

	@Before
	public void init() throws JAXBException, ConfigurationException {
		createMarshaller("petapi.xml");
	}

	private void createMarshaller(String xml, ObjectFactory... ofs) throws JAXBException, ConfigurationException {
		JAXBContext context = JAXBContext.newInstance("com.rest4j.impl.model");

		JAXBElement<API> element = (JAXBElement<API>) context.createUnmarshaller().unmarshal(MarshallerImplTest.class.getResourceAsStream(xml));
		API root = element.getValue();
		modelConfig = new ArrayList<MarshallerImpl.ModelConfig>();
		for (Object entry: root.getEndpointAndModel()) {
			if (entry instanceof Model) {
				Model model = (Model) entry;
				Object mapper = null;
				if ("petMapping".equals(model.getFieldMapper())) {
					mapper = customMapping;
				} else if ("someMapping".equals(model.getFieldMapper())) {
					mapper = someMapping;
				}
				modelConfig.add(new MarshallerImpl.ModelConfig(model, mapper));
			}
		}
		marshaller = new MarshallerImpl(modelConfig, ofs, filter == null ? new FieldFilter[0] : new FieldFilter[]{filter}, serviceProvider, new TestCloner());
	}

	@Test public void testParse_number() throws Exception {
		assertEquals(new Long(555), marshaller.parse("555", FieldType.NUMBER));
		assertEquals(new Double(567.89), marshaller.parse("567.89", FieldType.NUMBER));
	}

	@Test public void testUnmarshal_pet() throws Exception {
		JSONObject pet = createMaxJson();
		Pet Max = (Pet) marshaller.getObjectType("Pet").unmarshal(pet);

		assertEquals(0, Max.getId()); // read-only prop should not get unmarshalled
		assertEquals("Max", Max.getName());
		assertEquals("cat", Max.getType());
		assertEquals(Gender.male, Max.getGender());
		assertEquals(4.3, Max.getPetWeight(), 1e-5);
		assertEquals(Collections.singletonList(234), Max.getFriends());
		assertEquals(Collections.emptyList(), Max.getMated());
		assertEquals(Collections.emptyList(), Max.getAte());
	}

	@Test public void testUnmarshal_optional() throws Exception {
		JSONObject pet = createMaxJson();
		pet.remove("type");
		pet.remove("weight");
		pet.remove("relations");
		Pet Max = (Pet) marshaller.getObjectType("Pet").unmarshal(pet);
		assertNull(Max.getPetWeight()); // no default value
		assertEquals("dog", Max.getType()); // default value
		assertEquals(Collections.emptyList(), Max.getFriends());
		assertEquals(Collections.emptyList(), Max.getMated());
		assertEquals(Collections.emptyList(), Max.getAte());
	}

	@Test public void testUnmarshal_missing_field() throws Exception {
		try {
			JSONObject pet = createMaxJson();
			pet.remove("gender");
			marshaller.getObjectType("Pet").unmarshal(pet);
			fail();
		} catch (ApiException apiex) {
			assertEquals("Field Pet.gender is absent", apiex.getMessage());
		}
	}

	abstract class ExpectAPIException {
		ExpectAPIException(int httpStatus, String message) throws Exception {
			try {
				test();
				fail("Should fail with status "+httpStatus+" and message "+message);
			} catch (ApiException apiex) {
				assertEquals(httpStatus, apiex.getHttpStatus());
				assertEquals(message, apiex.getMessage());
			}
		}

		abstract protected void test() throws Exception;
	}

	@Test public void testUnmarshal_wrong_field_type_simple() throws Exception {
		new ExpectAPIException(400, "Field Pet.gender is expected to be a string") {
			@Override
			public void test() throws Exception {
				JSONObject pet = createMaxJson();
				pet.put("gender", 1);
				marshaller.getObjectType("Pet").unmarshal(pet);
			}
		};
	}

	@Test public void testUnmarshal_wrong_field_type_array() throws Exception {
		new ExpectAPIException(400, "Field Pet.relations should be an array") {
			@Override
			protected void test() throws Exception {
				JSONObject pet = createMaxJson();
				pet.put("relations", new JSONObject());
				marshaller.getObjectType("Pet").unmarshal(pet);
			}
		};
	}

	@Test public void testUnmarshal_wrong_array_element_type() throws Exception {
		new ExpectAPIException(400, "Field Pet.relations[0] should be an object") {
			@Override
			protected void test() throws Exception {
				JSONObject pet = createMaxJson();
				pet.put("relations", new JSONArray("[234]"));
				marshaller.getObjectType("Pet").unmarshal(pet);
			}
		};
	}

	@Test public void testUnmarshal_null_array_element() throws Exception {
		new ExpectAPIException(400, "Field Pet.relations[0] should not be null") {
			@Override
			protected void test() throws Exception {
				JSONObject pet = createMaxJson();
				pet.put("relations", new JSONArray("[null]"));
				marshaller.getObjectType("Pet").unmarshal(pet);
			}
		};
	}

	@Test public void testUnmarshal_wrong_enum_constant() throws Exception {
		new ExpectAPIException(400, "Field Pet.gender is expected to be one of male, female") {
			@Override
			public void test() throws Exception {
				JSONObject pet = createMaxJson();
				pet.put("gender", "gay");
				marshaller.getObjectType("Pet").unmarshal(pet);
			}
		};
	}

	@Test public void testUnmarshal_null_field() throws Exception {
		JSONObject pet = createMaxJson();
		pet.put("type", JSONObject.NULL);
		Pet Max = (Pet)marshaller.getObjectType("Pet").unmarshal(pet);
		assertNull(Max.getType());
	}

	@Test public void testUnmarshal_absent_field() throws Exception {
		JSONObject pet = createMaxJson();
		pet.remove("type");
		Pet Max = (Pet)marshaller.getObjectType("Pet").unmarshal(pet);
		assertEquals("dog", Max.getType()); // default
	}

	@Test public void testUnmarshal_wrong_body_type() throws Exception {
		new ExpectAPIException(400, "{value} should be an object") {
			@Override
			protected void test() throws Exception {
				marshaller.getObjectType("Pet").unmarshal(JSONObject.NULL);
			}
		};
	}

	@Test public void testUnmarshal_custom_mapping_exception() throws Exception {
		((PetMapping)customMapping).customMappingException = new ApiException("Test");
		new ExpectAPIException(400, "Test") {
			@Override
			protected void test() throws Exception {
				JSONObject pet = createMaxJson();
				marshaller.getObjectType("Pet").unmarshal(pet);
			}
		};
	}

	@Ignore // broken in "New type 'jsonObject' for simple fields", added "[extraData":null,"]"
	@Test public void testMarshal_pet() throws Exception {
		JSONObject pet = (JSONObject) marshaller.getObjectType("Pet").marshal(createMax());
		assertFalse(pet.has("writeonly"));
		JSONObject maxJson = createMaxJson();
		maxJson.remove("writeonly");
		assertEquals(maxJson.toString(), pet.toString());
	}

	@Test public void testMarshal_custom_mapping_exception() throws Exception {
		((PetMapping)customMapping).customMappingException = new ApiException("Test");
		new ExpectAPIException(400, "Test") {
			@Override
			protected void test() throws Exception {
				Pet Max = createMax();
				marshaller.getObjectType("Pet").marshal(Max);
			}
		};
	}

	@Test public void testMarshal_constants() throws Exception {
		createMarshaller("constants.xml");
		TestWithConstants test = new TestWithConstants();
		final ObjectApiTypeImpl type = marshaller.getObjectType("Test");
		JSONObject json = (JSONObject) type.marshal(test);
		assertEquals(555, json.getInt("integer"));
		assertEquals("TEST", json.getString("notMapped"));
		assertEquals("TEST", json.getString("enumProp"));

		json = new JSONObject("{integer:555, notMapped:'TEST', enumProp:'TEST'}");
		test = (TestWithConstants) type.unmarshal(json);
		assertEquals(555, test.getInteger());
		assertEquals(TestEnum.TEST, test.getEnumProp());

		new ExpectAPIException(400, "Field Test.integer should have value 555") {
			@Override
			protected void test() throws Exception {
				type.unmarshal(new JSONObject("{integer:666, notMapped:'TEST', enumProp:'TEST'}"));
			}
		};
		new ExpectAPIException(400, "Field Test.enumProp should have value TEST") {
			@Override
			protected void test() throws Exception {
				type.unmarshal(new JSONObject("{integer:555, notMapped:'TEST', enumProp:'TEST1'}"));
			}
		};
	}

	@Test public void testMarshal_polymorpic() throws Exception {
		customMapping = new com.rest4j.impl.polymorphic.PetMapping();
		createMarshaller("polymorphic-api.xml");
		Cat cat = new Cat();
		cat.setId(555);
		cat.setLongFur(true);
		JSONObject catJson = (JSONObject) marshaller.getObjectType("Pet").marshal(cat);
		assertEquals("cat", catJson.getString("type"));
		assertEquals(555, catJson.getInt("id"));
		assertEquals(true, catJson.getBoolean("longFur"));
		assertFalse(catJson.has("beakStrength"));
	}

	@Test public void testUnmarshal_polymorpic_with_object_factory() throws Exception {
		customMapping = new com.rest4j.impl.polymorphic.PetMapping();
		createMarshaller("polymorphic-api.xml", new ObjectFactory());
		Cat cat = (Cat) marshaller.getObjectType("Pet").unmarshal(new JSONObject("{id:555,longFur:true,type:'cat'}"));
		assertEquals(555, cat.getId());
		assertTrue(cat.isLongFur());

	}

	@Test public void testUnmarshal_polymorpic_present_primitive_prop() throws Exception {
		customMapping = new com.rest4j.impl.polymorphic.PetMapping();
		createMarshaller("polymorphic-api.xml", new ObjectFactory());
		Bird bird = (Bird) marshaller.getObjectType("Pet").unmarshal(new JSONObject("{id:555,beakStrength:1.23,type:'bird'}"));
		assertEquals(555, bird.getId());
		assertEquals(1.23, bird.getBeakStrength(), 1e-5);
	}

	@Test public void testUnmarshal_nested_property() throws Exception {
		createMarshaller("nested-properties.xml");
		NestedPet pet = (NestedPet) marshaller.getObjectType("Pet").unmarshal(new JSONObject("{id:555,weight:0.55}"));
		assertEquals(555, pet.getId());
		assertEquals(0.55, pet.getPhysicalCharacteristics().getWeight(), 1e-5);
	}

	@Test public void testUnmarshal_nested_property_creates_intermediate() throws Exception {
		createMarshaller("nested-properties.xml", new ObjectFactory() {
			@Nullable
			@Override
			public Object createInstance(@Nonnull String modelName, @Nonnull Class clz, @Nonnull JSONObject object, @Nonnull ObjectFactoryChain next) throws JSONException, ApiException {
				NestedPet pet = new NestedPet();
				pet.setPhysicalCharacteristics(null); // create this object
				return pet;
			}
		});
		NestedPet pet = (NestedPet) marshaller.getObjectType("Pet").unmarshal(new JSONObject("{id:555,weight:0.55}"));
		assertEquals(555, pet.getId());
		assertEquals(0.55, pet.getPhysicalCharacteristics().getWeight(), 1e-5);
	}

	@Test public void testMarshal_nested_property() throws Exception {
		createMarshaller("nested-properties.xml");
		NestedPet pet = new NestedPet();
		pet.setId(555);
		pet.getPhysicalCharacteristics().setWeight(0.77);
		JSONObject json = (JSONObject) marshaller.getObjectType("Pet").marshal(pet);
		assertEquals(555, json.getInt("id"));
		assertEquals(0.77, json.getDouble("weight"), 1e-5);
	}

	@Test public void testMarshal_maps() throws Exception {
		createMarshaller("map-type.xml");
		PetCompany company = new PetCompany();
		company.getPetsHashMap().put("Max", Long.valueOf(123)); // Max is a lonely cat
		company.getPetsMap().put("Max", createMax());
		JSONObject json = (JSONObject) marshaller.getObjectType("PetCompany").marshal(company);
		assertEquals("{\"Max\":{\"id\":123,\"name\":\"Max\"}}", json.getJSONObject("petsMap").toString());
		assertEquals("{\"Max\":123}", json.getJSONObject("petsHashMap").toString());
	}

	@Test public void testUnmarshal_maps() throws Exception {
		createMarshaller("map-type.xml");
		PetCompany company = (PetCompany) marshaller.getObjectType("PetCompany")
				.unmarshal(new JSONObject("{petsHashMap:{'Max':123},petsMap:{Max:{id:123,name:'Max'}}}"));
		assertEquals("{Max=123}", company.getPetsHashMap().toString());
		assertEquals("[Max]", company.getPetsMap().keySet().toString());
		assertEquals("Max", company.getPetsMap().get("Max").getName());
	}

	@Test public void testMarshal_char() throws Exception {
		createMarshaller("char-type.xml");
		Pet pet = new Pet();
		pet.setId(123);
		pet.setMiddlename('\1');
		JSONObject json = (JSONObject) marshaller.getObjectType("Pet").marshal(pet);
		assertEquals(123, json.getInt("id"));

		pet.setMiddlename('\0');
		json = (JSONObject) marshaller.getObjectType("Pet").marshal(pet);
		assertEquals("\0", json.getString("middlename"));
		assertEquals(0, json.getInt("middlename-int"));
	}

	@Test public void testUnmarshal_char() throws Exception {
		createMarshaller("char-type.xml");
		Pet pet = (Pet) marshaller.getObjectType("Pet").unmarshal(new JSONObject("{middlename:'A',\"middlename-int\":32}"));
		assertEquals(' ', pet.getMiddlename()); // middlename-int takes over cause it is the latest
	}

	@Test public void testMarshal_weaker_enum() throws Exception {
		createMarshaller("weaker-enum.xml");
		Pet pet = new Pet();
		pet.setGender(Gender.male);
		JSONObject json = (JSONObject) marshaller.getObjectType("Pet").marshal(pet);
		assertEquals("male", json.getString("gender"));
	}

	@Test public void testUnmarshal_weaker_enum() throws Exception {
		createMarshaller("weaker-enum.xml");
		Pet pet = (Pet) marshaller.getObjectType("Pet").unmarshal(new JSONObject("{gender:'male'}"));
		assertEquals(Gender.male, pet.getGender());
	}

	@Test public void testMarshal_with_dynamic_mapper() throws Exception {
		customMapping = new DynamicPetMapper();
		createMarshaller("petapi-dynamic.xml");
		HashMap<String, Object> pet = createDynamicMax();
		JSONObject object = (JSONObject) marshaller.getObjectType("Pet").marshal(pet);
		JSONObject max = createMaxJson();
		max.remove("writeonly");
		assertEquals(max.toString(), object.toString());
	}

	private HashMap<String, Object> createDynamicMax() {
		HashMap<String, Object> pet = new HashMap<String, Object>();
		pet.put("id", 123);
		pet.put("type", "cat");
		pet.put("name", "Max");
		pet.put("gender", Gender.male);
		pet.put("weight", 4.3);
		pet.put("relations", Collections.singletonList(new PetRelation(RelationType.friend, 234)));
		pet.put("writeonly", true);
		return pet;
	}

	@Test public void testUnmarshal_with_dynamic_mapper() throws Exception {
		customMapping = new DynamicPetMapper();
		createMarshaller("petapi-dynamic.xml");
		HashMap<String, Object> pet = (HashMap<String, Object>) marshaller.getObjectType("Pet").unmarshal(createMaxJson());
		HashMap<String, Object> expected = createDynamicMax();
		expected.remove("id"); // the read-only property should be lost
		assertEquals(expected.toString(), pet.toString());
	}

	@Test public void testMarshal_set() throws Exception {
		createMarshaller("set-type.xml");
		PetCompany company = new PetCompany();
		company.getPetsSet().add(createMax());
		JSONObject json = (JSONObject) marshaller.getObjectType("PetCompany").marshal(company);
		assertEquals("{\"petsSet\":[{\"id\":123,\"name\":\"Max\"}]}", json.toString());
	}

	@Test public void testUnmarshal_set() throws Exception {
		createMarshaller("set-type.xml");
		PetCompany company = (PetCompany) marshaller.getObjectType("PetCompany").unmarshal(new JSONObject("{\"petsSet\":[{\"name\":\"Max\"}]}"));
		assertEquals(1, company.getPetsSet().size());
		Pet pet = company.getPetsSet().iterator().next();
		assertEquals("Max", pet.getName());
	}

	@Test public void testMarshal_enum_map() throws Exception {
		createMarshaller("map-type.xml");
		PetCompany company = new PetCompany();
		company.getEnumMap().put(RelationType.ate, createMax());
		JSONObject json = (JSONObject) marshaller.getObjectType("PetCompany").marshal(company);
		assertEquals("{\"ate\":{\"id\":123,\"name\":\"Max\"}}", json.getJSONObject("enumMap").toString());
	}

	@Test public void testUnmarshal_enum_map() throws Exception {
		createMarshaller("map-type.xml");
		PetCompany company = (PetCompany) marshaller.getObjectType("PetCompany").unmarshal(new JSONObject("{\"enumMap\":{\"ate\":{\"name\":\"Max\"}}}"));
		assertEquals(1, company.getEnumMap().size());
		Pet pet = company.getEnumMap().get(RelationType.ate);
		assertEquals("Max", pet.getName());
	}

	@Test public void testInit_with_converters_success() throws Exception {
		createMarshaller("converters.xml");
	}

	@Test public void testInit_with_converters_wrong_inner_type() throws Exception {
		try {
			createMarshaller("converters-wrong-inner-type.xml");
			fail();
		} catch (ConfigurationException ce) {
			assertTrue(ce.getMessage(), ce.getMessage().startsWith("Wrong getter type: class java.util.Date"));
		}
	}

	@Test public void testInit_with_converters_wrong_outer_type() throws Exception {
		try {
			createMarshaller("converters-wrong-outer-type.xml");
			fail();
		} catch (ConfigurationException ce) {
			assertTrue(ce.getMessage(), ce.getMessage().startsWith("The property Some.simpleConvert type does not correspond to converter"));
		}
	}

	@Test public void testInit_with_converters_wrong_mapper_type() throws Exception {
		try {
			createMarshaller("converters-wrong-mapper-type.xml");
			fail();
		} catch (ConfigurationException ce) {
			assertTrue(ce.getMessage(), ce.getMessage().startsWith("Wrong getter type: class java.util.Date"));
		}
	}

	@Test public void testMarshal_with_converters() throws Exception {
		createMarshaller("converters.xml");
		Some some = new Some();
		Value value = new Value("xxx");
		some.setSimpleConvert(value);
		some.setComplexConvert("yyy");
		JSONObject json = (JSONObject) marshaller.getObjectType("Some").marshal(some);
		assertEquals("{\"simpleConvert\":\"xxx\",\"mappedConvert\":\"xxx\",\"complexConvert\":{\"simpleConvert\":\"yyy\",\"mappedConvert\":\"yyy\",\"complexConvert\":null}}", json.toString());
	}

	@Test public void testUnmarshal_with_converters() throws Exception {
		createMarshaller("converters.xml");
		Some some = (Some)marshaller.getObjectType("Some").unmarshal(new JSONObject("{\"simpleConvert\":\"xxx\",\"mappedConvert\":\"xxx\",\"complexConvert\":{\"simpleConvert\":\"yyy\",\"mappedConvert\":\"yyy\",\"complexConvert\":null}}"));
		assertEquals("xxx", some.getSimpleConvert().getValue());
		assertEquals("yyy", some.getComplexConvert());
	}

	@Test public void testUnmarshalPatch_array_nopatch() throws Exception {
		createMarshaller("recursive-patch.xml");
		Root root = new Root();
		try {
			marshaller.getObjectType("Root").unmarshalPatch(root, new JSONObject("{map:{\"one\":1},object:{string:\"test\"},array:[{string:\"test\"}]}"));
			fail();
		} catch (ApiException ex) {
			assertEquals("Field Leaf.number is absent", ex.getMessage());
		}
	}

	@Test public void testUnmarshalPatch_recursive_new_object() throws Exception {
		createMarshaller("recursive-patch.xml");
		Root root = new Root();
		Root patched = (Root) marshaller.getObjectType("Root").unmarshalPatch(root, new JSONObject("{map:{\"one\":1},objectMap:{\"key\":{string:\"test\"}},object:{string:\"test\"},array:[{string:\"test\",number:1}]}"));
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put("one", 1);
		assertEquals(map, patched.getMap());
		assertEquals("test", patched.getObjectMap().get("key").getString());
		assertEquals(1, patched.getArray().size());
		assertEquals("test", patched.getObject().getString());
	}

	@Test public void testUnmarshalPatch_recursive_change() throws Exception {
		createMarshaller("recursive-patch.xml");
		Root root = new Root();
		Leaf leaf = new Leaf();
		leaf.setNumber(5);
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("two", 2);
		root.setMap(map);
		root.setObject(leaf);
		Map<String, Leaf> omap = new HashMap<String, Leaf>();
		omap.put("key", leaf);
		root.setObjectMap(omap);
		List<Leaf> array = new ArrayList<Leaf>();
		array.add(leaf);
		root.setArray(array);
		Root patched = (Root) marshaller.getObjectType("Root").unmarshalPatch(root, new JSONObject("{map:{\"one\":1},objectMap:{\"key\":{string:\"test\"}},object:{string:\"test\"},array:[{string:\"test\",number:1}]}"));
		map = new HashMap<String, Integer>();
		map.put("one", 1);
		map.put("two", 2);
		assertEquals(map, patched.getMap());
		assertEquals("test", patched.getObjectMap().get("key").getString());
		assertEquals(5, patched.getObjectMap().get("key").getNumber());
		assertEquals(1, patched.getArray().size());
		assertEquals("test", patched.getObject().getString());
		assertEquals(5, patched.getObject().getNumber());
	}

	@Test public void testMarshal_java_arrays() throws Exception {
		createMarshaller("arrays.xml");
		Array array = new Array();
		array.setNumbers(new int[]{1,2,3,4,5});
		array.setObjects(new Element[]{new Element(),new Element()});
		JSONObject json = (JSONObject)marshaller.getObjectType("Array").marshal(array);
		assertEquals("{\"numbers\":[1,2,3,4,5],\"objects\":[{},{}]}", json.toString());
	}

	@Test public void testUnmarshal_with_instantiate_attribute() throws Exception {
		createMarshaller("instantiate.xml");
		Pet pet = (Pet) marshaller.getObjectType("Pet").unmarshal(new JSONObject("{friends:{}}"));
		assertEquals(LinkedList.class, pet.getFriends().getClass());
	}

	@Ignore // broken in "New type 'jsonObject' for simple fields", added "[extraData":null,"]"
	@Test public void testMarshal_with_field_filter() throws Exception {
		filter = new FieldFilter() {
			@Override
			public Object marshal(Object json, Object parentJavaObject, ObjectApiType parentType, Field field, FieldFilterChain next) {
				if (!parentType.getName().equals("Pet")) {
					return next.marshal(json, parentJavaObject, parentType, field);
				}
				assertEquals(Pet.class, parentJavaObject.getClass());
				if (field.getName().equals("weight")) {
					json = next.marshal(json, parentJavaObject, parentType, field);
					return ((Number)json).doubleValue()*2;
				} else if (field.getName().equals("relations")) return null;
				return next.marshal(json, parentJavaObject, parentType, field);
			}

			@Override
			public Object unmarshal(Object json, Object parentJavaObject, ObjectApiType parentType, Field field, FieldFilterChain next) {
				throw new AssertionError();
			}
		};
		createMarshaller("petapi.xml");
		Object json = marshaller.getObjectType("Pet").marshal(createMax());
		assertEquals("{\"id\":123,\"type\":\"cat\",\"name\":\"Max\",\"weight\":8.6,\"gender\":\"male\"}", json.toString());
	}

	@Test public void testUnmarshal_with_field_filter() throws Exception {
		filter = new FieldFilter() {
			@Override
			public Object marshal(Object json, Object parentJavaObject, ObjectApiType parentType, Field field, FieldFilterChain next) {
				throw new AssertionError();
			}

			@Override
			public Object unmarshal(Object json, Object parentJavaObject, ObjectApiType parentType, Field field, FieldFilterChain next) {
				if (!parentType.getName().equals("Pet")) {
					return next.unmarshal(json, parentJavaObject, parentType, field);
				}
				assertEquals(Pet.class, parentJavaObject.getClass());
				if (field.getName().equals("weight")) {
					json = next.unmarshal(json, parentJavaObject, parentType, field);
					return ((Number)json).doubleValue()/2;
				} else if (field.getName().equals("relations")) return new JSONArray();
				return next.unmarshal(json, parentJavaObject, parentType, field);
			}
		};
		createMarshaller("petapi.xml");
		Pet pet = (Pet) marshaller.getObjectType("Pet").unmarshal(createMaxJson());
		assertEquals("cat", pet.getType());
		assertEquals(2.15, pet.getPetWeight().doubleValue(), 1e-5);
		assertEquals(0, pet.getFriends().size());
		assertEquals(0, pet.getAte().size());
		assertEquals(0, pet.getMated().size());
	}

	static JSONObject createMaxJson() throws JSONException {
		JSONObject pet = new JSONObject();
		pet.put("id", 123);
		pet.put("type", "cat");
		pet.put("name", "Max");
		pet.put("weight", 4.3);
		pet.put("gender", "male");
		pet.put("writeonly", true);
		pet.put("relations", new JSONArray("[{type:'friend',petId: 234}]"));
		return pet;
	}

	static Pet createMax() {
		Pet pet = new Pet();
		pet.setId(123);
		pet.setType("cat");
		pet.setName("Max");
		pet.setGender(Gender.male);
		pet.setPetWeight(4.3);
		pet.setFriends(Collections.singletonList(234));
		pet.setMated(Collections.<Integer>emptyList());
		pet.setAte(Collections.<Integer>emptyList());
		return pet;
	}
}
