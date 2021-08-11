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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;

public class InsightsAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

	private static Logger log = LogManager.getLogger(InsightsAuthenticationFilter.class);

	public InsightsAuthenticationFilter(final String matcher, AuthenticationManager authenticationManager) {
		super(matcher);
		super.setAuthenticationManager(authenticationManager);
	}

	/**
	 * This method is used to perfrom authentication for every request based on
	 * authentication protocol,
	 * this method responsible to call authentication provider based on type
	 * 
	 * @param HttpServletRequest
	 *            request
	 * @param HttpServletResponse
	 *            response
	 */
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
		log.debug(" Inside InsightsAuthenticationFilter, attemptAuthentication ==== ");
		Authentication authentication = null;
		InsightsAuthenticationTokenUtils authenticationtokenUtils = new InsightsAuthenticationTokenUtils();
		if(AuthenticationUtils.WEB_IGNORE_URLS.contains(request.getPathInfo())) {
			authentication= new InsightsAuthenticationToken("", null, null,null);
			return authentication;
		}else if (ApplicationConfigProvider.getInstance().getAutheticationProtocol().equalsIgnoreCase("SAML")) {
			authentication = authenticationtokenUtils.authenticateSAMLData(request, response);
		} else if (AuthenticationUtils.IS_NATIVE_AUTHENTICATION) {
			authentication = authenticationtokenUtils.authenticateGrafanaJWTData(request, response);
		} else if (ApplicationConfigProvider.getInstance().getAutheticationProtocol().equalsIgnoreCase("JWT")) {
			authentication = authenticationtokenUtils.authenticateJWTData(request, response);
		}
		if (authentication != null) {
			authentication = getAuthenticationManager().authenticate(authentication);
		}
		return authentication;
	}

	/**
	 * used when Authentication Provider return sucess
	 *
	 */
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		authResult.getAuthorities().forEach(
				b -> log.debug("In successfulAuthentication GrantedAuthority ==== {} ", b.getAuthority()));
		SecurityContextHolder.getContext().setAuthentication(authResult);
		chain.doFilter(request, response);
	}

	/**
	 * used when authentication provider throws exception
	 *
	 */
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		log.error("unsuccessfulAuthentication ====", authException);
		Throwable exceptionClass = authException.getCause();
		if (exceptionClass != null && exceptionClass.getClass().getName().contains("AccountExpiredException")) {
			AuthenticationUtils.setResponseMessage(response, AuthenticationUtils.TOKEN_EXPIRE_CODE, "Token Expire ");
		} else {
			log.error(" Error while validating authentication {} ", authException.getMessage());
			AuthenticationUtils.setResponseMessage(response, AuthenticationUtils.UNAUTHORISE,
					"Authentication not successful, Please relogin " + authException.getMessage());
		}
	}
}