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
import com.rest4j.Marshaller;
import com.rest4j.type.DateApiType;
import com.rest4j.json.JSONObject;

import javax.xml.bind.DatatypeConverter;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class DateApiTypeImpl extends SimpleApiTypeImpl implements DateApiType {
	protected DateApiTypeImpl(Marshaller marshaller) {
		super(marshaller);
	}

	@Override
	public boolean check(Type javaType) {
		if (!(javaType instanceof Class)) return false;
		Class clz = (Class)javaType;
		if (clz == null) return false;
		return clz == java.util.Date.class || clz == java.sql.Date.class;
	}

	@Override
	public boolean equals(Object val1, Object val2) {
		return cast(val1, java.util.Date.class).equals(cast(val2, java.util.Date.class));
	}

	@Override
	public Object cast(Object value, Type javaType) {
		if (value == null) return null;
		if (javaType == java.util.Date.class) return value;
		java.util.Date date = (java.util.Date) value;
		return new java.sql.Date(date.getTime());
	}

	@Override
	public String getJavaName() {
		return "java.util.Date or java.sql.Date";
	}

	Pattern iso8601Timezone = Pattern.compile("[^T]*T.*(Z|[+-]([0-9]{4,4}|[0-9][0-9]|[0-9][0-9]:[0-9][0-9]))");

	@Override
	Object unmarshal(Object val) throws ApiException {
		if (JSONObject.NULL == val) val = null;
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
			return new java.util.Date(((Number)val).longValue()*1000);
		}
		throw new ApiException("{value} is expected to be a unix timestamp or a string in either ISO 8601 or RFC 2822 format");
	}

	ThreadLocal<SimpleDateFormat> JSONDateFormat = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			return sdf;
		}
	};

	@Override
	Object marshal(Object val) throws ApiException {
		if (val == null) return JSONObject.NULL;
		// equivalent of Date.toJSON in JavaScript
		if (val instanceof java.sql.Date) {
			val = new java.util.Date(((java.sql.Date)val).getTime());
		} else if (!(val instanceof java.util.Date)) {
			throw new ApiException("Expected Date, "+val.getClass()+" given").setHttpStatus(500);
		}
		return JSONDateFormat.get().format((java.util.Date)val);
	}
}
