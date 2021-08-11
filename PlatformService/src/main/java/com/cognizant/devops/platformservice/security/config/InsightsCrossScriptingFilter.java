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

import java.net.UnknownHostException;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class InsightsCrossScriptingFilter extends OncePerRequestFilter {
	private static Logger log = LogManager.getLogger(InsightsCrossScriptingFilter.class);

	/**
	 * This filter is used to validate Header,Cookies and Parameter for each and
	 * every request
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest httpRequest, HttpServletResponse httpResponce,
			FilterChain filterChain) {
		log.info("Inside Filter == InsightsCrossScriptingFilter ............... {} method {}  ",
				httpRequest.getRequestURL(),
				httpRequest.getMethod());

		try {
			validateHeaders(httpRequest, httpResponce);
			RequestWrapper requestMapper = new RequestWrapper(httpRequest, httpResponce);
			filterChain.doFilter(requestMapper, httpResponce);
			log.debug("Completed .. in InsightsCrossScriptingFilter");

		} catch (Exception e) {
			String msg;
			if (e.getMessage().contains("InsightsCustomException")) {
				log.error(
						"Invalid request in InsightsCustomException CrossScriptingFilter  InsightsCustomException {} ",
						e.getMessage());
				JsonParser parser = new JsonParser();
				JsonObject element = parser.parse(e.getMessage()).getAsJsonObject();
				msg = PlatformServiceUtil.buildFailureResponse(
						"InsightsCustomException Invalid request,Someting is wrong in cookies,Header or Parameter"
								+ e.getMessage() + " status " + element.get("StatusCode"))
						.toString();
				AuthenticationUtils.setResponseMessage(httpResponce, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
			} else if (e.getMessage().contains("InsightsAuthenticationException")) {
				log.error(
						"Invalid request in InsightsAuthenticationException CrossScriptingFilter InsightsAuthenticationException {} ",
						e.getMessage());
				AuthenticationUtils.setResponseMessage(httpResponce, AuthenticationUtils.SECURITY_CONTEXT_CODE,
						"Authentication not successful ,Please relogin ");
			} else if (e.getMessage().contains("UnknownHostException")) {
				log.error(
						"Invalid request in InsightsAuthenticationException CrossScriptingFilter UnknownHostException {} ",
						e.getMessage());
				AuthenticationUtils.setResponseMessage(httpResponce, AuthenticationUtils.INFORMATION_MISMATCH,
						e.getMessage());
			} else {
				log.error("Invalid request in CrossScriptingFilter {}", e.getMessage());
				msg = PlatformServiceUtil.buildFailureResponse(
								"Invalid request,Someting is wrong in cookies,Header or Parameter" + e.getMessage())
						.toString();
				AuthenticationUtils.setResponseMessage(httpResponce, HttpServletResponse.SC_BAD_REQUEST, msg);
			}
		}
		log.info("Out doFilter CrossScriptingFilter ...............");
	}

	/**
	 * used to validate header host with trusted host list
	 * 
	 * @param request
	 * @param response
	 * @throws UnknownHostException
	 */
	public void validateHeaders(HttpServletRequest request, HttpServletResponse response) throws UnknownHostException {
		log.info(" InsightsCrossScriptingFilter validate header ............... ");
		String origin = request.getHeader(HttpHeaders.ORIGIN);
		String host = AuthenticationUtils.getHost(request);
		if (host == null) {
			log.error(" Invalid request " + PlatformServiceConstants.HOST_NOT_FOUND);
			log.debug(" host and origin information ===== {}  origin {}", host, origin);
			AuthenticationUtils.setResponseMessage(response, AuthenticationUtils.INFORMATION_MISMATCH,
					PlatformServiceConstants.HOST_NOT_FOUND);
			throw new UnknownHostException(PlatformServiceConstants.HOST_NOT_FOUND);
		}
		if (!ApplicationConfigProvider.getInstance().getTrustedHosts().contains(host)) {
			log.error(" Invalid request " + PlatformServiceConstants.INVALID_REQUEST_ORIGIN);
			log.debug(" host and origin information ===== {}  origin {}", host, origin);
			AuthenticationUtils.setResponseMessage(response, AuthenticationUtils.INFORMATION_MISMATCH,
					PlatformServiceConstants.INVALID_REQUEST_ORIGIN);
			throw new UnknownHostException(PlatformServiceConstants.INVALID_REQUEST_ORIGIN);
		}
		log.debug("Host validated successfully ==== host and origin information ===== {}  origin {}", host, origin);
	}
}
