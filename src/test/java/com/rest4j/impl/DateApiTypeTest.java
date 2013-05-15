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

import com.rest4j.APIException;
import org.junit.Test;

import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.Assert.*;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class DateApiTypeTest {
	DateApiType type = new DateApiType();

	@Test public void testUnmarshal_string() throws APIException {
		Date date = (Date) type.unmarshal("Wed, 24 Apr 2013 09:06:00 +0400");
		assertEquals(date, type.unmarshal("24 Apr 2013 09:06 +0400"));
		assertEquals(date, type.unmarshal("24 Apr 2013 09:06 MSK"));
		assertEquals(date, type.unmarshal("Wed, 24 Apr 2013 05:06"));
		assertEquals(date, type.unmarshal("24 Apr 2013 05:06"));
		assertEquals(date, type.unmarshal("2013-04-24T05:06:00Z"));
		assertEquals(date, type.unmarshal("2013-04-24T05:06:00+00:00"));
		assertEquals(date, type.unmarshal("2013-04-24T09:06:00+04:00"));
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(GregorianCalendar.HOUR_OF_DAY, -5);
		cal.add(GregorianCalendar.MINUTE, -6);
		assertEquals(cal.getTime(), type.unmarshal("2013-04-24"));
	}

	@Test public void testUnmarshal_number() throws APIException {
		Date date = (Date) type.unmarshal(1366786417);
		assertEquals(1366786417000l, date.getTime());
	}

	@Test public void testMarshal() throws APIException {
		assertEquals("2013-04-24T06:53:37.123Z", type.marshal(new Date(1366786417123l)));
	}
}
