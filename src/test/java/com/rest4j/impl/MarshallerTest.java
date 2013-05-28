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
import com.rest4j.ConfigurationException;
import com.rest4j.ObjectFactoryChain;
import com.rest4j.impl.model.API;
import com.rest4j.impl.model.FieldType;
import com.rest4j.impl.model.Model;
import com.rest4j.impl.petapi.*;
import com.rest4j.impl.polymorphic.Bird;
import com.rest4j.impl.polymorphic.Cat;
import com.rest4j.impl.polymorphic.ObjectFactory;
import com.rest4j.type.ObjectApiType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class MarshallerTest {

	private Object customMapping = new PetMapping();
	List<Marshaller.ModelConfig> modelConfig;
	Marshaller marshaller;

	@Before
	public void init() throws JAXBException, ConfigurationException {
		String xml = "petapi.xml";
		createMarshaller(xml);
	}

	private void createMarshaller(String xml, ObjectFactory... ofs) throws JAXBException, ConfigurationException {
		JAXBContext context = JAXBContext.newInstance("com.rest4j.impl.model");

		JAXBElement<API> element = (JAXBElement<API>) context.createUnmarshaller().unmarshal(MarshallerTest.class.getResourceAsStream(xml));
		API root = element.getValue();
		modelConfig = new ArrayList<Marshaller.ModelConfig>();
		for (Object entry: root.getEndpointAndModel()) {
			if (entry instanceof Model) {
				modelConfig.add(new Marshaller.ModelConfig((Model)entry, customMapping));
			}
		}
		marshaller = new Marshaller(modelConfig, ofs);
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

	@Test public void testMarshal_pet() throws Exception {
		JSONObject pet = (JSONObject) marshaller.getObjectType("Pet").marshal(createMax());
		assertFalse(pet.has("writeonly"));
		pet.put("writeonly", true);
		assertEquals(createMaxJson().toString(), pet.toString());
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
		final ObjectApiType type = marshaller.getObjectType("Test");
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

	@Test public void testMarshal_default_optional_field() throws Exception {
		Pet pet = createMax();
		JSONObject json = (JSONObject) marshaller.getObjectType("Pet").marshal(pet);
		assertEquals("cat", json.getString("type"));
		pet.setType("dog");
		json = (JSONObject) marshaller.getObjectType("Pet").marshal(pet);
		assertFalse(json.has("type"));
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
	@Test public void testUnmarshal_polymorpic_absent_primitive_prop() throws Exception {
		customMapping = new com.rest4j.impl.polymorphic.PetMapping();
		createMarshaller("polymorphic-api.xml", new ObjectFactory());
		try {
			Bird bird = (Bird) marshaller.getObjectType("Pet").unmarshal(new JSONObject("{id:555,longFur:true,type:'bird'}"));
			fail();
		} catch (ApiException ex) {
			assertEquals("Field Pet.beakStrength value is absent", ex.getMessage());
		}
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

	static JSONObject createMaxJson() throws JSONException {
		JSONObject pet = new JSONObject();
		pet.put("id", 123);
		pet.put("type", "cat");
		pet.put("name", "Max");
		pet.put("gender", "male");
		pet.put("weight", 4.3);
		pet.put("relations", new JSONArray("[{petId: 234}]"));
		pet.put("writeonly", true);
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
