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

package com.rest4j.spring;

import com.rest4j.ApiException;
import com.rest4j.ApiRequest;
import com.rest4j.ApiResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * An utility class that can be used as a Spring controller. Supports Spring transactions. Rollbacks
 * a transaction on any exception.
 *
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class TransactionalController extends Controller {

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ModelAndView handleRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
		ApiResponse response;
		try {
			response = api.serve(ApiRequest.from(httpServletRequest));
			response.outputBody(httpServletResponse);
			return null;
		} catch (ApiException e) {
			response = e.createResponse();
			response.outputBody(httpServletResponse);
			throw e;
		} catch (Exception e) {
			try {
				httpServletResponse.setStatus(500);
			} catch (Exception ex) {
				throw e; // do not mask the original exception
			}
			throw e;
		}
	}
}
