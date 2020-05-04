/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 *   
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * 	of the License at
 *   
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package com.cognizant.devops.platformservice.security.config;

import java.util.Enumeration;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;

/**
 * This class is responsible to validate Request Header, Cookies and Paramanter
 * 
 * @author 716660
 *
 */
public final class RequestWrapper extends HttpServletRequestWrapper {
	private static Logger log = LogManager.getLogger(RequestWrapper.class);
	HttpServletRequest request;
	HttpServletResponse response;
	Boolean validationStatus = false;

	/**
	 * constructer to set HttpServletRequest and HttpServletResponse
	 * 
	 * @param servletRequest
	 * @param servletResponse
	 */
	public RequestWrapper(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		super(servletRequest);
		this.request = servletRequest;
		this.response = servletResponse;
		validatAllHeaders();
		inValidateAllCookies();
		validateAllParameter();
	}

	/**
	 * Validate and Apply the XSS filter to the parameters
	 * 
	 * @param parameters
	 */
	public void validateAllParameter() {
		log.debug("In getParameterValues .....");
		Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String paramName = ValidationUtils.cleanXSS(parameterNames.nextElement());
			ValidationUtils.cleanXSS(request.getParameter(paramName));
		}
		log.debug("In validateAllParameter ==== Completed ");
	}

	/**
	 * Validate and Apply the XSS filter to the all Headers
	 * 
	 * @param parameters
	 */
	public void validatAllHeaders() {
		log.debug("In validatAllHeaders ");
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			String headersValue = request.getHeader(headerName);
			log.debug("In validatAllHeaders {} headersValue  {} ", headerName, headersValue);
			ValidationUtils.cleanXSS(headersValue);
		}
		log.debug("In validatAllHeaders ==== Complated");
	}

	/**
	 * Validate request cookies from XSS and HTTP_Response_Splitting
	 * @param parameters
	 */
	public Cookie[] inValidateAllCookies() {
		log.debug(" in RequestWrapper get cookies ==== ");
		Cookie[] cookies = null;
		cookies = PlatformServiceUtil.validateCookies(request.getCookies());
		log.debug(" in RequestWrapper cookies ==== Complated ");
		return cookies;
	}

}
