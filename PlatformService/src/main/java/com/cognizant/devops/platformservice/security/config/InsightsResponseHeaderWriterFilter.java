/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformservice.security.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;

public class InsightsResponseHeaderWriterFilter extends OncePerRequestFilter {

	private static Logger log = LogManager.getLogger(InsightsResponseHeaderWriterFilter.class);

	/**
	 * used to validate and write header in response header
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		log.debug(" Inside Filter == InsightsResponseHeaderWriter  ........ {}  method {} ", request.getRequestURL(),
				request.getMethod());
		writeHeaders(request, response);
		filterChain.doFilter(request, response);
		log.debug(" Write Header in InsightsResponseHeaderWriterFilter ============ Completed");
	}

	/**
	 * Write important header in response header and cookies
	 * 
	 * @param request
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	public void writeHeaders(HttpServletRequest request, HttpServletResponse response) {
		try {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, request.getHeader(HttpHeaders.ORIGIN));
			response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
					request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS));
			response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
					request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD));
			response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
			response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		} catch (Exception e) {
			log.error("Invalid detail in  InsightsResponseHeaderWriter {}", e.getMessage());
			String msg = PlatformServiceUtil
					.buildFailureResponse("Invalid detail in  InsightsResponseHeaderWriter" + e)
					.toString();
			AuthenticationUtils.setResponseMessage(response, HttpServletResponse.SC_BAD_REQUEST, msg);
		}
	}
}