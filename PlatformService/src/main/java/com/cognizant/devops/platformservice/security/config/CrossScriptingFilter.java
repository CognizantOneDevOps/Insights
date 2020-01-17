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

import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
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

public class CrossScriptingFilter extends OncePerRequestFilter {
	private static Logger LOG = LogManager.getLogger(CrossScriptingFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest httpRequest, HttpServletResponse httpResponce,
			FilterChain filterChain) {
		LOG.info("Inside Filter == CrossScriptingFilter ...............");

		try {
			RequestWrapper requestMapper = new RequestWrapper(httpRequest,httpResponce);
			writeHeaders(httpRequest, httpResponce);
			filterChain.doFilter(requestMapper, httpResponce);
			LOG.debug("Completed .. in CrossScriptingFilter");

		} catch (Exception e) {
			String msg;
			if (e.getMessage().contains("InsightsCustomException")) {
				LOG.error("Invalid request in InsightsCustomException CrossScriptingFilter " + e.getMessage());
				JsonParser parser =new JsonParser();
				JsonObject element = parser.parse(e.getMessage()).getAsJsonObject();
				msg = PlatformServiceUtil.buildFailureResponse(
								"InsightsCustomException Invalid request,Someting is wrong in cookies,Header or Parameter"
										+ e.getMessage() + " status " + element.get("StatusCode"))
						.toString();
				AuthenticationUtils.setResponseMessage(httpResponce, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
			} else {
				LOG.error("Invalid request in CrossScriptingFilter " + e.getMessage());
				msg = PlatformServiceUtil.buildFailureResponse(
								"Invalid request,Someting is wrong in cookies,Header or Parameter" + e.getMessage())
						.toString();
				AuthenticationUtils.setResponseMessage(httpResponce, HttpServletResponse.SC_BAD_REQUEST, msg);
			}
		}
		LOG.info("Out doFilter CrossScriptingFilter ...............");
	}
	
	
	public void writeHeaders(HttpServletRequest request, HttpServletResponse response) {
		LOG.debug(" Write Header in CrossScriptingFilter ============ ");
		response.setStatus(HttpServletResponse.SC_OK);
			String origin = request.getHeader(HttpHeaders.ORIGIN);
			String host =AuthenticationUtils.getHost(request);
			LOG.debug(" host and origin information ===== " + host + " origin "+origin);
			if(host==null) {
				LOG.error(" Invalid request " + PlatformServiceConstants.HOST_NOT_FOUND);
				AuthenticationUtils.setResponseMessage(response, AuthenticationUtils.INFORMATION_MISMATCH, PlatformServiceConstants.HOST_NOT_FOUND);
				return;
			}
			if (!ApplicationConfigProvider.getInstance().getTrustedHosts().contains(host)) {
				LOG.error(" Invalid request " + PlatformServiceConstants.INVALID_REQUEST_ORIGIN);
				AuthenticationUtils.setResponseMessage(response, AuthenticationUtils.INFORMATION_MISMATCH, PlatformServiceConstants.INVALID_REQUEST_ORIGIN);
				//throw new RuntimeException(PlatformServiceConstants.INVALID_REQUEST_ORIGIN);
				return;
			}
			response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
			response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
					request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS));
			response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
					request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD));
			response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
			// 463188 - Response Headers for Control: no-cache, no-store header
			response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1

        //Set the response headers for grafana details.
        Object attribute = request.getAttribute("responseHeaders");
        if(attribute != null){
        	Map<String, String> grafanaHeaders = (Map<String, String>)attribute;
        	for(Map.Entry<String, String> entry : grafanaHeaders.entrySet()){
				Cookie cookie = new Cookie(entry.getKey(), entry.getValue());
				cookie.setHttpOnly(true);
				cookie.setMaxAge(60 * 30);
				cookie.setPath("/");
				response.addCookie(cookie);
        	}
        }
		LOG.debug(" Write Header in CrossScriptingFilter ============ Completed");
	}
}
