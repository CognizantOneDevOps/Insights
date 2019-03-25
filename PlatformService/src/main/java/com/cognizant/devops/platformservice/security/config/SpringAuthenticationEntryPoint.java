/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformservice.security.config;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;

@Component
public class SpringAuthenticationEntryPoint implements AuthenticationEntryPoint {
	static Logger log = LogManager.getLogger(SpringAuthenticationEntryPoint.class.getName());

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		log.error(authException);
		//String msg = "{ \"error\" : { \"message\" : \"Invalid Credentials\"}}";
		String msg = PlatformServiceUtil.buildFailureResponse("Invalid Credentials").toString();
		PrintWriter writer = response.getWriter();
		writer.write(msg);
		writer.flush();
		writer.close();
	}
}
