/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
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
package com.cognizant.devops.platformservice.security.config.grafana;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.util.Assert;

import com.cognizant.devops.platformservice.security.config.AuthenticationUtils;
import com.cognizant.devops.platformservice.security.config.InsightsAuthenticationToken;

public class InsightsGrafanaAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

	private static Logger log = LogManager.getLogger(InsightsGrafanaAuthenticationFilter.class);

	private String filterURL;

	public InsightsGrafanaAuthenticationFilter(String filterURL) {
		super(filterURL);
		this.filterURL = filterURL;
	}

	public InsightsGrafanaAuthenticationFilter(final String matcher, AuthenticationManager authenticationManager) {
		super(matcher);
		super.setAuthenticationManager(authenticationManager);
	}

	/**
	 * This used to validate Grafana user at first level 
	 *
	 * @param request request
	 * @return authentication object in case SAML data was found and valid
	 * @throws AuthenticationException authentication failure
	 */
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
		log.debug("message Inside InsightsGrafanaAuthenticationFilter **** ");
		Authentication authentication = null;
		String authToken = AuthenticationUtils.extractAndValidateAuthToken(request, response);
		List<GrantedAuthority> updatedAuthorities = new ArrayList<>();
		updatedAuthorities.add(AuthenticationUtils.getSpringAuthorityRole("Viewer"));
		authentication = new InsightsAuthenticationToken(authToken, null, null, updatedAuthorities);
		authentication = getAuthenticationManager().authenticate(authentication);
		updateSecurityContext(authentication);

		log.debug("message Inside InsightsGrafanaAuthenticationFilter After authentication completed **** ");
		return authentication;
	}

	/**
	 * Verifies that required entities .
	 */
	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		Assert.notNull(filterURL, "filter URL must be set");
	}

	/**
	 * used when Authentication Provider return successful Authentication
	 *
	 */
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		authResult.getAuthorities()
				.forEach(b -> log.debug(
						"In successfulAuthentication InsightsGrafanaAuthenticationFilter GrantedAuthority for user "));
		chain.doFilter(request, response);
	}

	/**
	 * used when authentication provider throws exception
	 *
	 */
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		log.error("unsuccessfulAuthentication  InsightsGrafanaAuthenticationFilter ====", authException);
		Throwable exceptionClass = authException.getCause();
		if (exceptionClass != null && exceptionClass.getClass().getName().contains("AccountExpiredException")) {
			AuthenticationUtils.setResponseMessage(response, AuthenticationUtils.TOKEN_EXPIRE_CODE, "Token Expire ");
		} else {
			log.error(" Error while validating authentication {} ", authException.getMessage());
			AuthenticationUtils.setResponseMessage(response, AuthenticationUtils.UNAUTHORISE,
					"Authentication not successful, Please relogin " + authException.getMessage());
		}
	}

	public void updateSecurityContext(Authentication authentication) {
		org.springframework.security.core.context.SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
	}

}
