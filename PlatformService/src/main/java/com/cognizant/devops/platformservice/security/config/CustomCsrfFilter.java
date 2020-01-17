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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

public class CustomCsrfFilter extends OncePerRequestFilter {

	private static Logger LOG = LogManager.getLogger(CustomCsrfFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		LOG.debug(" Inside Filter == CustomCsrfFilter token ");
		CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
		// LOG.debug(" arg0 CsrfToken " + CsrfToken.class.getName() + " " + csrf);
		if (csrf != null) {
			Cookie cookie = WebUtils.getCookie(request, AuthenticationUtils.CSRF_COOKIE_NAME);
			String token = csrf.getToken();

			// LOG.debug(" arg0 CsrfToken value " + token);
			if (cookie == null || token != null && !token.equals(cookie.getValue())) {
				cookie = new Cookie(AuthenticationUtils.CSRF_COOKIE_NAME, token);
				cookie.setPath("/");
				response.addCookie(cookie);
			}
		} else {
			LOG.debug(" csrf token is empty ");
		}
		filterChain.doFilter(request, response);
	}

}