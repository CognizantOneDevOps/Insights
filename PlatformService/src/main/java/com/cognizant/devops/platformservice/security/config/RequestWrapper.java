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

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.datatagging.constants.DatataggingConstants;
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
	 * Constructor to set HttpServletRequest and HttpServletResponse
	 * 
	 * @param servletRequest
	 * @param servletResponse
	 * @throws InsightsCustomException 
	 */
	public RequestWrapper(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws InsightsCustomException {
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
	 * @throws InsightsCustomException 
	 */
	public void validateAllParameter() throws InsightsCustomException {
		log.debug("In getParameterValues .....");
		Map<String, String[]> parameterMap = request.getParameterMap();
		int maxParamCount = 50;
		int paramCount = parameterMap.size();
		if(paramCount > maxParamCount){
			log.debug("In validateAllParameter ==== parameter count exceeds max limit {}",paramCount);
			throw new InsightsCustomException("In validateAllParameter ==== parameter count exceeds max limit");
		} else {
			for(Map.Entry<String,String[]> entry : parameterMap.entrySet()){
				
				String paramName = ValidationUtils.cleanXSS(entry.getKey());
				ValidationUtils.cleanXSS(request.getParameter(paramName));
			}
			log.debug("In validateAllParameter ==== Completed ");
		}
	}

	/**
	 * Validate and Apply the XSS filter to the all Headers
	 * 
	 * @param parameters
	 * @throws InsightsCustomException 
	 */
	public void validatAllHeaders() throws InsightsCustomException {
		
		Enumeration<String> headerNames = request.getHeaderNames();
		List<String> headerNameslist = Collections.list(headerNames);
		log.debug("In validateAllHeaders started ==== ");
		StringBuilder headerInfo = new StringBuilder();
		int maxParamCount = 50;
		int headersCount = headerNameslist.size();
		if(headersCount > maxParamCount ) {
			log.debug("In validatAllHeaders ==== headers count exceeds max limit {}", headersCount);
			throw new InsightsCustomException("In validatAllHeaders ==== headers count exceeds max limit ");
		} else {
			for (int i =0; i < headersCount; i++ ) {
				String headerName = headerNameslist.get(i);
				String headersValue = request.getHeader(headerName);
				headerInfo.append(headerName.concat(DatataggingConstants.VALIDATE_ALLHEADERS_EQUALS).concat(headersValue).concat(DatataggingConstants.COMMA));
				ValidationUtils.cleanXSS(headerName,headersValue);				
			}
			log.debug("In validatedAllHeaders  ==== {} ",headerInfo);
			log.debug("In validatedAllHeaders  ====  Completed ");
		}
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
