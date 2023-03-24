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

package com.rest4j;

import com.rest4j.impl.ext.FieldExt;
import com.rest4j.impl.petapi.DynamicPetMapper;
import com.rest4j.type.Field;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class APIFactoryTest {
	Object dao = new Object() {};
	Object mapper = null;
	ServiceProvider provider = new ServiceProvider() {
		@Override
		public Object lookupService(String name) {
			return dao;
		}

		@Override
		public Object lookupFieldMapper(String model, String name) {
			return mapper;
		}

		@Override
		public Converter lookupConverter(String name) {
			return null;
		}
	};

	@Test
	public void testCreateAPI_wrong_field_model_reference() throws ConfigurationException {
		String exceptionMessageSubstring = "complex-field-type";
		String xmlFile = "wrong_field_model_reference.xml";
		validate(exceptionMessageSubstring, xmlFile);
	}

	@Test
	public void testCreateAPI_wrong_response_model_reference() throws ConfigurationException {
		validate("body-or-response-type", "wrong_response_model_reference.xml");
	}

	@Test
	public void testCreateAPI_wrong_path_parameter_reference() throws ConfigurationException {
		validate("route-param-name", "wrong_path_parameter_reference.xml");
	}
	
	@Test
	public void testCreateAPI_mapping_method_with_dynamic_mapper() throws Exception {
		mapper = new DynamicPetMapper();
		validate("DynamicMapper", "mapper-method-with-dynamic-mapper.xml");
	}

	@Test public void testCreateApi_with_extensions() throws Exception {
		ApiFactory fac = new ApiFactory(getClass().getResource("impl/extendable-api.xml"), "", provider, null);
		fac.setExtSchema("com/rest4j/impl/api-ext.xsd", com.rest4j.impl.ext.ObjectFactory.class);
		dao = new Object() {
			public com.rest4j.impl.petapi.Pet get() {
				return new com.rest4j.impl.petapi.Pet();
			}
		};
		API api = fac.createAPI();
		List<Field> fields = api.getMarshaller().getObjectType("Pet").getFields();
		Field idField = fields.get(0);
		FieldExt ext = (FieldExt) idField.getExtra().get(0);
		assertEquals("ID", ext.getAttr());
		Field typeField = fields.get(1);
		ext = (FieldExt) typeField.getExtra().get(0);
		assertEquals("TYPE", ext.getAttr());
	}

	private void validate(String exceptionMessageSubstring, String xmlFile) {
		try {
			new ApiFactory(getClass().getResource(xmlFile), "", provider, null).createAPI();
			fail();
		} catch (ConfigurationException ex) {
			String message = ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage();
			if (!message.contains(exceptionMessageSubstring)) {
				ex.printStackTrace();
				fail(ex.getMessage());
			}
		}
	}

}
