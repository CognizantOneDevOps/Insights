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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.kerberos.authentication.KerberosServiceRequestToken;
import org.springframework.security.providers.ExpiringUsernameAuthenticationToken;
import org.springframework.security.saml.SAMLCredential;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;

public class InsightsAuthenticationTokenUtils {

	private static Logger Log = LogManager.getLogger(InsightsAuthenticationTokenUtils.class);

	/**
	 * used to create AbstractAuthenticationToken for Native Grafana
	 * 
	 * @param user
	 * @return
	 */
	public Authentication authenticationNativeGrafana(UserDetails user) {
		UsernamePasswordAuthenticationToken authenticationGrafana = null;
		if (user == null) {
			Log.error(" Invalid Authentication for native Grafana ");
			throw new InsightsAuthenticationException(" Invalid Invalid Authentication for native Grafana ");
		} else {
			authenticationGrafana = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
			Log.debug("In InsightsAuthenticationToken in grafana validation GrantedAuthority ==== {} ",
					authenticationGrafana.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(authenticationGrafana);
		}
		return authenticationGrafana;
	}

	/**
	 * used to create AbstractAuthenticationToken for SAML data
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	public Authentication authenticationSAMLData(HttpServletRequest request, HttpServletResponse response) {
		Log.debug(" Inside authenticationSAMLData , url ==== {} ", request.getRequestURI());
		String auth_token = extractAndValidateAuthToken(request, response);
		SecurityContext context = SecurityContextHolder.getContext();
		Authentication auth = context.getAuthentication();
		if (auth != null) {
			SAMLCredential credentials = (SAMLCredential) auth.getCredentials();
			InsightsAuthenticationToken jwtAuthenticationToken = new InsightsAuthenticationToken(auth_token,
					auth.getDetails(), credentials, auth.getAuthorities());
			return jwtAuthenticationToken;
		} else {
			AuthenticationUtils.setResponseMessage(response, AuthenticationUtils.SECURITY_CONTEXT_CODE,
					"Authentication not successful ,Please relogin ");
			return null;
		}
	}

	/**
	 * Used to validate JWT token Data
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	public Authentication authenticationJWTData(HttpServletRequest request, HttpServletResponse response) {
		Log.debug(" Inside authenticationJWTData , url ==== {} ", request.getRequestURI());
		String auth_token = extractAndValidateAuthToken(request, response);
		SecurityContext context = SecurityContextHolder.getContext();
		Authentication auth = context.getAuthentication();
		if (auth != null) {
			Object credentials = auth.getCredentials();
			InsightsAuthenticationToken jwtAuthenticationToken = new InsightsAuthenticationToken(auth_token,
					auth.getDetails(), credentials, auth.getAuthorities());
			return jwtAuthenticationToken;
		} else {
			//UserDetails user = GrafanaUserDetailsUtil.getUserDetails(request);user.getAuthorities()
			InsightsAuthenticationToken jwtAuthenticationToken = new InsightsAuthenticationToken(auth_token, null, null,
					null);
			/*AuthenticationUtils.setResponseMessage(response, AuthenticationUtils.SECURITY_CONTEXT_CODE,
					"Authentication not successful ,Please relogin ");*/
			return jwtAuthenticationToken;
		}
	}

	/**
	 * Extract and validate authrization token
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	private String extractAndValidateAuthToken(HttpServletRequest request, HttpServletResponse response) {
		String auth_token = request.getHeader(AuthenticationUtils.AUTH_HEADER_KEY);
		if (auth_token == null || auth_token.isEmpty()) {
			Log.error(" InsightsAuthenticationFilter Authorization is empty or not found ");
			String msg = PlatformServiceUtil.buildFailureResponse("Unauthorized Access ,Invalid Credentials..")
					.toString();
			AuthenticationUtils.setResponseMessage(response, HttpServletResponse.SC_BAD_REQUEST, msg);
		}
		return auth_token;
	}

	/**
	 * Used to set AbstractAuthenticationToken when user role change mainly for
	 * Switch Org scenerio.
	 * 
	 * @param grafanaCurrentOrgRole
	 */
	public void updateSecurityContextRoleBased(String grafanaCurrentOrgRole) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		List<GrantedAuthority> updatedAuthorities = new ArrayList<>();
		updatedAuthorities.add(AuthenticationUtils.getSpringAuthorityRole(grafanaCurrentOrgRole));

		if (AuthenticationUtils.IS_NATIVE_AUTHENTICATION) {
			Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(),
					updatedAuthorities);
			Log.debug("Get Credentials output {} output new   {}", auth, newAuth);
			SecurityContextHolder.getContext().setAuthentication(newAuth);
		} else if ("SAML".equalsIgnoreCase(ApplicationConfigProvider.getInstance().getAutheticationProtocol())) {
			Object principal = auth.getPrincipal();
			Date expDate = new Date(System.currentTimeMillis() + 60 * 60 * 1000);
			ExpiringUsernameAuthenticationToken autharization = new ExpiringUsernameAuthenticationToken(expDate,
					principal, auth.getCredentials(), updatedAuthorities);
			SecurityContextHolder.getContext().setAuthentication(autharization);
		}else if ("Kerberos".equalsIgnoreCase(ApplicationConfigProvider.getInstance().getAutheticationProtocol())) {
			SecurityContext context = SecurityContextHolder.getContext();
			KerberosServiceRequestToken authKerberos = (KerberosServiceRequestToken) context.getAuthentication();
			KerberosServiceRequestToken responseAuth = new KerberosServiceRequestToken(authKerberos.getDetails(), authKerberos.getTicketValidation(),
					updatedAuthorities, authKerberos.getToken());
			Log.debug("In successfulAuthentication Older Kerberos GrantedAuthority ==== {} ", authKerberos);
			Log.debug("In successfulAuthentication Kerberos GrantedAuthority ==== {} ", responseAuth);

			SecurityContextHolder.getContext().setAuthentication(responseAuth);
		} else if ("JWT".equalsIgnoreCase(ApplicationConfigProvider.getInstance().getAutheticationProtocol())) {
			SecurityContext context = SecurityContextHolder.getContext();
			InsightsAuthenticationToken authKerberos = (InsightsAuthenticationToken) context.getAuthentication();

			InsightsAuthenticationToken responseAuth = new InsightsAuthenticationToken(authKerberos.getPrincipal(),
					authKerberos.getDetails(), authKerberos.getCredentials(), updatedAuthorities);
			Log.debug("In successfulAuthentication JWT Older GrantedAuthority ==== {} ", authKerberos);
			Log.debug("In successfulAuthentication JWT GrantedAuthority ==== {} ", responseAuth);

			SecurityContextHolder.getContext().setAuthentication(responseAuth);
		}
	}
}