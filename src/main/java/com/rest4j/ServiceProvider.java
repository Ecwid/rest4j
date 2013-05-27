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

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public interface ServiceProvider {

	/**
	 * Find an object that will execute REST API requests.
	 *
	 * @param name The value of the service/@name attribute of the API description or null if absent.
	 * @return
	 */
	Object lookupService(String name);

	/**
	 * Finds a field mapper corresponding to a name specified in the field-mapper attribute of the API description.
	 * Field mappers are looked up once during API initialization.
	 *
	 * @param model The name of the model for with the field mapper should be found.
	 * @param name The field-mapper attribute value or null if not specified.
	 * @return
	 */
	Object lookupFieldMapper(String model, String name);

	Converter lookupConverter(String name);
}
