/*********************************************************************************
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
 *******************************************************************************/

package com.cognizant.devops.platformservice.security.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.security.config.grafana.GrafanaExternalUserDetailsUtil;

public class InsightsExternalAPIAuthenticationFilter extends OncePerRequestFilter  {

	private static Logger log = LogManager.getLogger(InsightsExternalAPIAuthenticationFilter.class);
	GrafanaExternalUserDetailsUtil externalAPIValidator = new GrafanaExternalUserDetailsUtil();
	/**
	 * This authentication filter used to handle all External API login request
	 */
	@Override
	public void doFilterInternal(HttpServletRequest httpRequest, HttpServletResponse httpResponce,
			FilterChain filterChain)
			throws IOException, ServletException {
		log.debug("message Inside InsightsExternalAPIAuthenticationFilter **** {} ",httpRequest.getRequestURL());
		String authToken = AuthenticationUtils.extractAndValidateAuthToken(httpRequest, httpResponce);

		try {
				externalAPIValidator.validateExternalUserDetails(authToken);
				
				List<GrantedAuthority> updatedAuthorities = new ArrayList<>();
				updatedAuthorities.add(AuthenticationUtils.getSpringAuthorityRole("Viewer"));
				Authentication authentication = new InsightsAuthenticationToken(authToken,null, null, updatedAuthorities);
				
				SecurityContextHolder.getContext().setAuthentication(authentication);
			
		} catch (InsightsCustomException e) {
			log.error(e);
			throw new InsightsAuthenticationException(" Invalid Invalid Authentication for external API  ");
		} 
		log.debug("message Inside InsightsExternalAPIAuthenticationFilter completed **** ");
		filterChain.doFilter(httpRequest, httpResponce);
	}
}
