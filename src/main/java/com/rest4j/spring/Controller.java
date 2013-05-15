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

import com.rest4j.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Logger;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class Controller implements org.springframework.web.servlet.mvc.Controller {
	API api;
	Logger log = Logger.getLogger(Controller.class.getName());

	@Override
	public ModelAndView handleRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
		APIResponse response;
		try {
			response = api.serve(APIRequest.from(httpServletRequest));
		} catch (APIException e) {
			response = e.createResponse();
		}
		response.outputBody(httpServletResponse);
		return null;
	}

	public API getApi() {
		return api;
	}

	public void setApi(API api) {
		this.api = api;
	}

	@PostConstruct
	public void check() throws ConfigurationException {
		if (api == null) throw new ConfigurationException("api property not configured in Controller");
	}
}
