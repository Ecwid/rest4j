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

package com.rest4j.impl.converters;

import java.util.Date;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class Some {
	Value simpleConvert;
	String complexConvert;
	Date date;

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Value getSimpleConvert() {
		return simpleConvert;
	}

	public void setSimpleConvert(Value simpleConvert) {
		this.simpleConvert = simpleConvert;
	}

	public String getComplexConvert() {
		return complexConvert;
	}

	public void setComplexConvert(String complexConvert) {
		this.complexConvert = complexConvert;
	}
}
