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

public class InsightsAuthenticationTokenUtils {

	private static Logger log = LogManager.getLogger(InsightsAuthenticationTokenUtils.class);

	/**
	 * used to create AbstractAuthenticationToken for Native Grafana
	 * 
	 * @param user
	 * @return
	 */
	public Authentication authenticateNativeGrafana(UserDetails user) {
		UsernamePasswordAuthenticationToken authenticationGrafana = null;
		if (user == null) {
			log.error(" Invalid Authentication for native Grafana ");
			throw new InsightsAuthenticationException(" Invalid Invalid Authentication for native Grafana ");
		} else {
			authenticationGrafana = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
			log.debug("In InsightsAuthenticationToken in grafana validation GrantedAuthority ==== {} ",
					authenticationGrafana.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(authenticationGrafana);
		}
		return authenticationGrafana;
	}
	
	/**
	 * used to create AbstractAuthenticationToken for Grafana JWT data
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	public Authentication authenticateGrafanaJWTData(HttpServletRequest request, HttpServletResponse response) {
		log.debug(" Inside authenticationGrafanaJWTData , url ==== {} ", request.getRequestURI());
		String authToken = AuthenticationUtils.extractAndValidateAuthToken(request, response);
		SecurityContext context = SecurityContextHolder.getContext();
		Authentication auth = context.getAuthentication();
		if (auth != null) {
			InsightsAuthenticationToken jwtAuthenticationToken = new InsightsAuthenticationToken(authToken,
					auth.getDetails(), auth.getCredentials(), auth.getAuthorities());
			log.debug(" Inside authenticationGrafanaJWTData , authorities ==== {} ", auth.getAuthorities());
			return jwtAuthenticationToken;
		} else {
			AuthenticationUtils.setResponseMessage(response, AuthenticationUtils.SECURITY_CONTEXT_CODE,
					"Authentication not successful ,Please relogin ");
			return null;
		}
	}

	/**
	 * used to create AbstractAuthenticationToken for SAML data
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	public Authentication authenticateSAMLData(HttpServletRequest request, HttpServletResponse response) {
		log.debug(" Inside authenticationSAMLData , url ==== {} ", request.getRequestURI());
		String authToken = AuthenticationUtils.extractAndValidateAuthToken(request, response);
		SecurityContext context = SecurityContextHolder.getContext();
		Authentication auth = context.getAuthentication();
		if (auth != null) {
			SAMLCredential credentials = (SAMLCredential) auth.getCredentials();
			InsightsAuthenticationToken jwtAuthenticationToken = new InsightsAuthenticationToken(authToken,
					auth.getDetails(), credentials, auth.getAuthorities());
			log.debug(" Inside authenticationSAMLData , authorities ==== {} ", auth.getAuthorities());
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
	public Authentication authenticateJWTData(HttpServletRequest request, HttpServletResponse response) {
		log.debug(" Inside authenticationJWTData , url ==== {} ", request.getRequestURI());
		String authToken = AuthenticationUtils.extractAndValidateAuthToken(request, response);
		SecurityContext context = SecurityContextHolder.getContext();
		Authentication auth = context.getAuthentication();
		InsightsAuthenticationToken jwtAuthenticationToken = null;
		if (auth != null) {
			Object credentials = auth.getCredentials();
			jwtAuthenticationToken=new InsightsAuthenticationToken(authToken,
					auth.getDetails(), credentials, auth.getAuthorities());
		} else {
			jwtAuthenticationToken= new InsightsAuthenticationToken(authToken, null, null,
					null);
		}
		return jwtAuthenticationToken;
	}

	
	/**
	 * Used to set AbstractAuthenticationToken when user role change mainly for
	 * Switch Org scenario.
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
			log.debug("In successfulAuthentication Older SAML GrantedAuthority ==== {} ", auth.getAuthorities());
			log.debug("In successfulAuthentication SAML GrantedAuthority ==== {} ", updatedAuthorities);
			SecurityContextHolder.getContext().setAuthentication(newAuth);
		} else if ("SAML".equalsIgnoreCase(ApplicationConfigProvider.getInstance().getAutheticationProtocol())) {
			Object principal = auth.getPrincipal();
			Date expDate = new Date(System.currentTimeMillis() + 60 * 60 * 1000);
			ExpiringUsernameAuthenticationToken autharization = new ExpiringUsernameAuthenticationToken(expDate,
					principal, auth.getCredentials(), updatedAuthorities);
			log.debug("In successfulAuthentication Older SAML GrantedAuthority ==== {} ", auth.getAuthorities());
			log.debug("In successfulAuthentication SAML GrantedAuthority ==== {} ", updatedAuthorities);

			SecurityContextHolder.getContext().setAuthentication(autharization);
		}else if ("Kerberos".equalsIgnoreCase(ApplicationConfigProvider.getInstance().getAutheticationProtocol())) {
			SecurityContext context = SecurityContextHolder.getContext();
			KerberosServiceRequestToken authKerberos = (KerberosServiceRequestToken) context.getAuthentication();
			KerberosServiceRequestToken responseAuth = new KerberosServiceRequestToken(authKerberos.getDetails(), authKerberos.getTicketValidation(),
					updatedAuthorities, authKerberos.getToken());
			log.debug("In successfulAuthentication Older Kerberos GrantedAuthority ==== {} ", auth.getAuthorities());
			log.debug("In successfulAuthentication Kerberos GrantedAuthority ==== {} ", updatedAuthorities);

			SecurityContextHolder.getContext().setAuthentication(responseAuth);
		} else if ("JWT".equalsIgnoreCase(ApplicationConfigProvider.getInstance().getAutheticationProtocol())) {
			SecurityContext context = SecurityContextHolder.getContext();
			InsightsAuthenticationToken authKerberos = (InsightsAuthenticationToken) context.getAuthentication();

			InsightsAuthenticationToken responseAuth = new InsightsAuthenticationToken(authKerberos.getPrincipal(),
					authKerberos.getDetails(), authKerberos.getCredentials(), updatedAuthorities);
			log.debug("In successfulAuthentication JWT Older GrantedAuthority ==== {} ", auth.getAuthorities());
			log.debug("In successfulAuthentication JWT GrantedAuthority ==== {} ", updatedAuthorities);

			SecurityContextHolder.getContext().setAuthentication(responseAuth);
		}
	}
}