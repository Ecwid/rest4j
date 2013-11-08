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

import org.w3c.dom.Document;

/**
 * Pre-processes API xml definition. Register pre-processors with ApiFactory#addPreprocessor.
 * You can make any changed to the DOM, as long as they adhere to the following conditions:
 *
 * <ul>
 *     <li>The resulting XML contains all the required tags and attributes declared in the api.xsd. You can add your own
 *     tags and attributes not declared in the api.xsd.</li>
 *     <li>All mandatory attributes have their default values, e.g. 'collection' is 'singleton' by default, 'access' is
 *     'readwrite' etc.</li>
 * </ul>
 *
 * Preprocessors are useful for implementing dynamic datatypes.
 *
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public interface Preprocessor {
	void process(ApiFactory apiFactory, Document xml) throws ConfigurationException;
}
