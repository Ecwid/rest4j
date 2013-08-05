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

import com.rest4j.impl.model.StringWithParams;

import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class StringWithParamsMatcher {
	final Pattern pattern;
	final List<String> names = new ArrayList<String>();

	public StringWithParamsMatcher(StringWithParams stringWithParams) {
		StringBuilder regexp = new StringBuilder();
		for (Object part: stringWithParams.getContent()) {
			if (part instanceof String) {
				regexp.append(Pattern.quote((String)part));
			} else if (part instanceof JAXBElement) {
				JAXBElement element = (JAXBElement)part;
				names.add(element.getValue().toString());
				regexp.append("([^/]*)");
			}
		}
		pattern = Pattern.compile(regexp.toString());
	}

	public Map<String, String> match(CharSequence str) {
		Matcher matcher = pattern.matcher(str);
		if (!matcher.matches()) return null;
		Map<String, String> map = new HashMap<String, String>();
		for (int i=0; i<matcher.groupCount(); i++) {
			map.put(names.get(i), matcher.group(i + 1));
		}
		return map;
	}

	public boolean matches(CharSequence str) {
		return pattern.matcher(str).matches();
	}
}
