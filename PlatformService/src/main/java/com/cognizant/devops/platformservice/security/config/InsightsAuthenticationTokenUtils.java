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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.kerberos.authentication.KerberosServiceRequestToken;
import org.springframework.security.providers.ExpiringUsernameAuthenticationToken;
import org.springframework.security.saml.SAMLCredential;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.nimbusds.jwt.JWTClaimsSet;

public class InsightsAuthenticationTokenUtils {

	private static Logger log = LogManager.getLogger(InsightsAuthenticationTokenUtils.class);

	/**
	 * used to create AbstractAuthenticationToken for Grafana JWT data
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	public Authentication authenticateGrafanaJWTData(HttpServletRequest request, HttpServletResponse response) {
		log.debug(" Inside GrafanaJWTAuthenticationData , url ==== {} ", request.getRequestURI());
		String authToken = AuthenticationUtils.extractAndValidateAuthToken(request, response);
		
		JWTClaimsSet claimSet = AuthenticationUtils.validateIncomingToken(authToken);
		
		Map<String,Object> claimMap = claimSet.getClaims();
		List<GrantedAuthority> updatedAuthorities = new ArrayList<>();
		updatedAuthorities.add(AuthenticationUtils.getSpringAuthorityRole((String)claimMap.get(AuthenticationUtils.AUTHORITY)));
		
		InsightsAuthenticationToken jwtAuthenticationToken = new InsightsAuthenticationToken(authToken,
				claimMap.get(AuthenticationUtils.GRAFANA_DETAIL), null, updatedAuthorities);
		
		log.debug(" Inside GrafanaJWTAuthenticationData authenticateGrafanaJWTData completed..  ");
		return jwtAuthenticationToken;
		
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
			log.debug(" Inside authenticationSAMLData completed ");
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
		InsightsAuthenticationToken jwtAuthenticationToken = null;
		
		log.debug("Inside authenticateUserUsingJWT ======= {}",request.getServletPath());
		
		/*
		 * This Block use to validate External Token received from clinet
		 */
		if(AuthenticationUtils.JWT_LOGIN_URL.contains(request.getRequestURI()) || 
			AuthenticationUtils.JWT_USER_DETAIL_URL.contains(request.getRequestURI()) ) {
			log.debug("Inside JWTAuthenticationProvider for Authentication started === ");
			JWTClaimsSet jwtClaimsSet = AuthenticationUtils.validateIncomingToken(authToken);
			if (jwtClaimsSet != null) {
				List<GrantedAuthority> updatedAuthorities = new ArrayList<>();
				updatedAuthorities.add(AuthenticationUtils.getSpringAuthorityRole("Viewer"));
				
				jwtAuthenticationToken =  new InsightsAuthenticationToken(
						authToken, jwtClaimsSet, null, updatedAuthorities);
			} else {
				log.error(" Error while validating token and retriving claims ");
				throw new InsightsAuthenticationException(" Error while validating token and retriving claims {} ");
			}
			log.debug(" Inside authenticateUserUsingJWT , processing completed  ");
		} else {
			/*
			 * This Block use to validate Insights generated Token.
			 */
			JWTClaimsSet claimSet = AuthenticationUtils.validateIncomingToken(authToken);
			
			Map<String,Object> claimMap = claimSet.getClaims();
			List<GrantedAuthority> updatedAuthorities = new ArrayList<>();
			updatedAuthorities.add(AuthenticationUtils.getSpringAuthorityRole((String)claimMap.get(AuthenticationUtils.AUTHORITY)));
			
			jwtAuthenticationToken = new InsightsAuthenticationToken(authToken,
					claimSet, null, updatedAuthorities);
			
			log.debug(" Inside GrafanaJWTAuthenticationData , processing completed  ");
		}
		
		return jwtAuthenticationToken;
	}
	

	/**
	 * Used to set AbstractAuthenticationToken when user role change mainly for
	 * Switch Org scenario.
	 * 
	 * @param grafanaCurrentOrgRole
	 * @throws InsightsCustomException 
	 */
	public String updateSecurityContextRoleBased(String grafanaCurrentOrgRole) {
		String jToken ="";
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		List<GrantedAuthority> updatedAuthorities = new ArrayList<>();
		updatedAuthorities.add(AuthenticationUtils.getSpringAuthorityRole(grafanaCurrentOrgRole));

		if (AuthenticationUtils.IS_NATIVE_AUTHENTICATION) {
			Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(),
					updatedAuthorities);
		
			Map<String, Object> params = new HashMap<>();
			params.put(AuthenticationUtils.AUTHORITY, grafanaCurrentOrgRole);
			params.put(AuthenticationUtils.GRAFANA_DETAIL,null);
			
			jToken=AuthenticationUtils.getToken(String.valueOf(auth.getPrincipal()), AuthenticationUtils.SESSION_TIME, params);
			updateSecurityContext(newAuth);
			
		} else if ("SAML".equalsIgnoreCase(ApplicationConfigProvider.getInstance().getAutheticationProtocol())) {
			Object principal = auth.getPrincipal();
			Date expDate = new Date(System.currentTimeMillis() + 60 * 60 * 1000);
			ExpiringUsernameAuthenticationToken autharization = new ExpiringUsernameAuthenticationToken(expDate,
					principal, auth.getCredentials(), updatedAuthorities);
			
			updateSecurityContext(autharization);

		}else if ("Kerberos".equalsIgnoreCase(ApplicationConfigProvider.getInstance().getAutheticationProtocol())) {
			SecurityContext context = SecurityContextHolder.getContext();
			KerberosServiceRequestToken authKerberos = (KerberosServiceRequestToken) context.getAuthentication();
			KerberosServiceRequestToken responseAuth = new KerberosServiceRequestToken(authKerberos.getDetails(), authKerberos.getTicketValidation(),
					updatedAuthorities, authKerberos.getToken());
			
			updateSecurityContext(responseAuth);

		} else if ("JWT".equalsIgnoreCase(ApplicationConfigProvider.getInstance().getAutheticationProtocol())) {
			SecurityContext context = SecurityContextHolder.getContext();
			InsightsAuthenticationToken authJWT = (InsightsAuthenticationToken) context.getAuthentication();
			
			JWTClaimsSet detailData = (JWTClaimsSet) authJWT.getDetails();

			InsightsAuthenticationToken responseAuth = new InsightsAuthenticationToken(authJWT.getPrincipal(),
					authJWT.getDetails(), authJWT.getCredentials(), updatedAuthorities);
			
			Map<String, Object> params = new HashMap<>();
			params.put(AuthenticationUtils.AUTHORITY, grafanaCurrentOrgRole);
			params.put("OriginalToken",detailData.getClaim("OriginalToken"));
			params.put("exp",detailData.getClaim("exp"));
			
			jToken=AuthenticationUtils.getToken(detailData.getSubject(), AuthenticationUtils.SESSION_TIME, params);
			
			log.debug("In successfulAuthentication JWT GrantedAuthority completed ==== ");
			updateSecurityContext(responseAuth);
		}
		return jToken;
	}
	
	public void updateSecurityContext(Authentication authentication) {
		org.springframework.security.core.context.SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context); 
	}
}