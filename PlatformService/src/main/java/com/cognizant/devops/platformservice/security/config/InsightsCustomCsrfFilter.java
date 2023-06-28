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
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import com.cognizant.devops.platformcommons.constants.LogMessageConstants;

public class InsightsCustomCsrfFilter extends OncePerRequestFilter {

	private static Logger log = LogManager.getLogger(InsightsCustomCsrfFilter.class);
	

	/**
	 * Filter used to extract CSRF token and add it in response header
	 *
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		long startTime = System.currentTimeMillis();
		try {
			updateLogInformation(request);
			log.debug(" Inside Filter == CustomCsrfFilter token ........ {} method {} ", request.getRequestURL(),
					request.getMethod());
			
			CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
			if (csrf != null) {
				Cookie cookie = WebUtils.getCookie(request, AuthenticationUtils.CSRF_COOKIE_NAME);
				String token = csrf.getToken();
				if (cookie == null || token != null && !token.equals(cookie.getValue())) {
					cookie = new Cookie(AuthenticationUtils.CSRF_COOKIE_NAME, token);
					cookie.setPath("/");
					response.addCookie(cookie);
				}
			} else {
				log.error(" csrf token is empty for url {}  ", request.getRequestURL());
			}
			
			
			filterChain.doFilter(request, response);
		} catch (Exception e) {
			log.error(e);
		} finally {
			long processingTime = System.currentTimeMillis() - startTime;
			ThreadContext.put(LogMessageConstants.PROCESSINGTIME, String.valueOf(processingTime));
			log.debug(" processing time for method {} is {}",request.getRequestURI() , processingTime);
		}
		log.debug("Out doFilter CustomCsrfFilter ...............");
	}

	private void updateLogInformation(HttpServletRequest request) {
		final String token;
		
		token = UUID.randomUUID().toString().toUpperCase().replace("-", "");

		ThreadContext.put(LogMessageConstants.PROCESSINGTIME, String.valueOf(0));
		ThreadContext.put(LogMessageConstants.TRACEID, token);
		ThreadContext.put(LogMessageConstants.TYPE, LogMessageConstants.APILOGSTYPE);
		ThreadContext.put(LogMessageConstants.HTTPMETHOD, request.getMethod());
		ThreadContext.put(LogMessageConstants.ENDPOINT, request.getRequestURI());
	}
}