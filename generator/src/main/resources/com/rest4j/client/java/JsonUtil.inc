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
package com.rest4j.client;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.bind.DatatypeConverter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class JsonUtil {
	public static Number asNumber(Object val) {
		if (val instanceof Number) return (Number) val;
		if (val == JSONObject.NULL) return null;
		if (val instanceof String) {
			String str = (String) val;
			try { return Integer.parseInt(str); } catch (Exception ex) {}
			try { return Long.parseLong(str); } catch (Exception ex) {}
			try { return Double.parseDouble(str); } catch (Exception ex) {}
		}
		throw new IllegalArgumentException("Not a number: "+val);
	}
	public static String asString(Object val) {
		if (val == JSONObject.NULL) return null;
		return val.toString();
	}
	public static Boolean asBoolean(Object val) {
		if (val == JSONObject.NULL) return null;
		if (val instanceof Boolean) return (Boolean) val;
		if (val instanceof String) return Boolean.parseBoolean((String)val);
		if (val instanceof Number) return ((Number)val).intValue() != 0;
		throw new IllegalArgumentException("Not a boolean: "+val);
	}

	static Pattern iso8601Timezone = Pattern.compile("[^T]*T.*(Z|[+-]([0-9]{4,4}|[0-9][0-9]|[0-9][0-9]:[0-9][0-9]))");

	public static Date asDate(Object val) {
		if (val == JSONObject.NULL) return null;
		if (val instanceof String) {
			String stringValue = (String) val;
			try {
				// try ISO 8601
				Calendar cal = DatatypeConverter.parseDateTime(stringValue);
				if (!iso8601Timezone.matcher(stringValue).matches()) {
					// timezone is absent from input string. Assume UTC
					cal.setTimeZone(TimeZone.getTimeZone("UTC"));
				}
				return cal.getTime();
			} catch (IllegalArgumentException iae) {
				// fallback to RFC 2822
				for (String format: new String[] {
						"EEE, dd MMM yyyy HH:mm:ss Z",
						"dd MMM yyyy HH:mm:ss Z",
						"EEE, dd MMM yyyy HH:mm Z",
						"dd MMM yyyy HH:mm Z",
						"EEE, dd MMM yyyy HH:mm:ss",
						"dd MMM yyyy HH:mm:ss",
						"EEE, dd MMM yyyy HH:mm",
						"dd MMM yyyy HH:mm"
				}) {
					SimpleDateFormat rfc2822Fmt = new SimpleDateFormat(format, Locale.ENGLISH);
					rfc2822Fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
					try {
						return rfc2822Fmt.parse(stringValue);
					} catch (ParseException e) {
					}
				}
			}
		} else if (val instanceof Number) {
			return new Date(((Number)val).longValue()*1000);
		}
		throw new IllegalArgumentException("Value is expected to be a unix timestamp or a string in either ISO 8601 or RFC 2822 format: "+val);
	}

	static ThreadLocal<SimpleDateFormat> JSONDateFormat = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			return sdf;
		}
	};

	public static Object asJsonSingleton(Date val) {
		if (val == null) return JSONObject.NULL;
		// equivalent of Date.toJSON in JavaScript
		return JSONDateFormat.get().format(val);
	}

	public static Object asJsonSingleton(Object val) {
		if (val == null) return JSONObject.NULL;
		return val;
	}

	public static Object asJsonArray(List list, JsonElementFactory fac) {
		if (list == null) return JSONObject.NULL;
		if (list instanceof JsonArrayList) return ((JsonArrayList)list).array;
		JSONArray array = new JSONArray();
		int i=0;
		try {
			for (Object element: list) {
					array.put(i++, fac.json(element));
			}
		} catch (JSONException e) {
			throw new IllegalArgumentException(e);
		}
		return array;
	}

	public static Object asJsonMap(Map<String,?> map, JsonElementFactory fac) {
		if (map == null) return JSONObject.NULL;
		if (map instanceof JsonObjectMap) return ((JsonObjectMap)map).object;
		JSONObject obj = new JSONObject();
		try {
			for (Map.Entry<String, ?> entry: map.entrySet()) {
				obj.put(entry.getKey(), fac.json(entry.getValue()));
			}
		} catch (JSONException e) {
			throw new IllegalArgumentException(e);
		}
		return obj;
	}

}
