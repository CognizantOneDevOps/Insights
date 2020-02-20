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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;

public class InsightsAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

	private static Logger Log = LogManager.getLogger(InsightsAuthenticationFilter.class);

	public InsightsAuthenticationFilter(final String matcher, AuthenticationManager authenticationManager) {
		super(matcher);
		super.setAuthenticationManager(authenticationManager);
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
		String url = request.getRequestURI();
		Log.debug(" Inside InsightsAuthenticationFilter url ==== " + url);

		String auth_token = request.getHeader("Authorization");
		Log.debug(" Inside InsightsAuthenticationFilter, auth_token === " + auth_token);
		if (auth_token != null && !auth_token.isEmpty()) {
			auth_token = ValidationUtils.cleanXSS(auth_token);
			Log.debug(" In filter  InsightsAuthenticationFilter  ==== " + auth_token);
		} else {
			Log.error(" InsightsAuthenticationFilter Authorization is empty or not found ");
			String msg = PlatformServiceUtil.buildFailureResponse("Unauthorized Access ,Invalid Credentials..")
					.toString();
			AuthenticationUtils.setResponseMessage(response, HttpServletResponse.SC_BAD_REQUEST, msg);
		}
		
		SecurityContext context = SecurityContextHolder.getContext();
		
		Authentication auth = context.getAuthentication();
		if (auth != null) {

			//auth.getAuthorities().forEach(b -> Log.debug("In InsightsAuthenticationFilter GrantedAuthority ==== " + b.getAuthority().toString()));
			SAMLCredential credentials = (SAMLCredential) auth.getCredentials();
			InsightsAuthenticationToken jwtAuthenticationToken = new InsightsAuthenticationToken(auth_token,
					auth.getDetails(), credentials, auth.getAuthorities());
			jwtAuthenticationToken.getAuthorities().forEach(b -> Log
					.debug("In InsightsAuthenticationToken GrantedAuthority ==== " + b.getAuthority().toString()));
			return getAuthenticationManager().authenticate(jwtAuthenticationToken);
		} else {
			AuthenticationUtils.setResponseMessage(response, AuthenticationUtils.SECURITY_CONTEXT_CODE, "Authentication not successful ,Please relogin ");
			return null;
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {

		authResult.getAuthorities().forEach(
				b -> Log.debug("In successfulAuthentication GrantedAuthority ==== " + b.getAuthority().toString()));
		SecurityContextHolder.getContext().setAuthentication(authResult);
		chain.doFilter(request, response);
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		Log.error("unsuccessfulAuthentication ==== " + authException.getCause().getClass() + " message  "
				+ authException.getMessage());
		if (authException.getCause().getClass().getName().contains("AccountExpiredException")) {
			AuthenticationUtils.setResponseMessage(response, AuthenticationUtils.TOKEN_EXPIRE_CODE, "Token Expire ");
		} else {
			// SecurityContextHolder.clearContext();
			AuthenticationUtils.setResponseMessage(response,AuthenticationUtils.UNAUTHORISE, "Authentication not successful, Please relogin ");
		}
	}
}