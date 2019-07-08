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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformservice.customsettings.CustomAppSettings;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;

public class CrossScriptingFilter implements Filter {
	private static Logger LOG = LogManager.getLogger(CustomAppSettings.class);
	private FilterConfig filterConfig;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
	}

	@Override
	public void destroy() {
		this.filterConfig = null;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		//LOG.info("In doFilter CrossScriptingFilter ...............");

		HttpServletResponse httpResponce = (HttpServletResponse) response;
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		try {
			writeHeaders(httpRequest, httpResponce);
			RequestWrapper requestMapper = new RequestWrapper(httpRequest,
					httpResponce);
			chain.doFilter(requestMapper, httpResponce);
			//LOG.debug("Completed .. in CrossScriptingFilter");

		} catch (Exception e) {
			LOG.error("Invalid request in CrossScriptingFilter");
			String msg = PlatformServiceUtil.buildFailureResponse("Invalid request").toString();
			httpResponce.setContentType("application/json");
			httpResponce.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			httpResponce.getWriter().write(msg);
			httpResponce.getWriter().flush();
			httpResponce.getWriter().close();
		}
		// LOG.info("Out doFilter CrossScriptingFilter ...............");
	}
	
	
	public void writeHeaders(HttpServletRequest request, HttpServletResponse response) {
		//LOG.debug(" Write Header in CrossScriptingFilter ============ ");
		response.setStatus(HttpServletResponse.SC_OK);
		try {
			String origin = request.getHeader(HttpHeaders.ORIGIN);
			String referer = request.getHeader(HttpHeaders.REFERER);
			String host = "";
			if (request.getHeader(HttpHeaders.REFERER) != null) {
				URL url = new URL(referer);
				host = url.getHost();
			} else {
				URL url = new URL(origin);
				host = url.getHost();
			}
			//LOG.debug(" host information ===== " + host);
			if (!ApplicationConfigProvider.getInstance().getTrustedHosts().contains(host)) {
				LOG.error(" Invalid request " + PlatformServiceConstants.INVALID_REQUEST_ORIGIN);
				throw new RuntimeException(PlatformServiceConstants.INVALID_REQUEST_ORIGIN);
			}
			response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
			response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
					request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS));
			response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
					request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD));
			response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
			// 463188 - Response Headers for Control: no-cache, no-store header
			response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
		} catch (MalformedURLException e) {
			LOG.error("error while writing header in CrossScriptingFilter "+e.getMessage());
		}
        //Set the response headers for grafana details.
        Object attribute = request.getAttribute("responseHeaders");
        if(attribute != null){
        	Map<String, String> grafanaHeaders = (Map<String, String>)attribute;
        	for(Map.Entry<String, String> entry : grafanaHeaders.entrySet()){
				Cookie cookie = new Cookie(entry.getKey(), entry.getValue());
				// cookie.setHttpOnly(true); //3
				cookie.setMaxAge(60 * 30);
				cookie.setPath("/");
				response.addCookie(cookie);
        	}
        }
	}
}
