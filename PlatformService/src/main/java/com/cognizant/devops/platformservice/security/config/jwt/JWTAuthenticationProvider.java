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
package com.cognizant.devops.platformservice.security.config.jwt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.cognizant.devops.platformservice.security.config.AuthenticationUtils;
import com.cognizant.devops.platformservice.security.config.InsightsAuthenticationException;
import com.cognizant.devops.platformservice.security.config.InsightsAuthenticationToken;
import com.nimbusds.jwt.JWTClaimsSet;

public class JWTAuthenticationProvider implements AuthenticationProvider {
	private static Logger log = LogManager.getLogger(JWTAuthenticationProvider.class);



	/**
	 * Used to authenticate Native Grafana
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		log.debug("Inside JWTAuthenticationProvider === ");;
		Authentication authenticationJWT =	 null ;
		if (!supports(authentication.getClass())) {
			throw new IllegalArgumentException(
					"Only JWTAuthenticationProvider is supported, " + authentication.getClass() + " was attempted");
		}

		if (authentication.getPrincipal() == null) {
			log.debug(
					"In JWTAuthenticationProvider Authentication token is missing - authentication.getPrincipal() {} ",
					authentication.getPrincipal());
			throw new AuthenticationCredentialsNotFoundException("Authentication token is missing");
		}
		/*validate request token*/
		JWTClaimsSet jwtClaimsSet = AuthenticationUtils.validateIncomingToken(authentication.getPrincipal());
		if (jwtClaimsSet != null) {
			log.debug("Inside JWTAuthenticationProvider jwtClaimsSet {} === {} ",jwtClaimsSet, authentication);
			authentication.getAuthorities().forEach(
					b -> log.debug("In successfulAuthentication JWTAuthenticationProvider assigned "));
			authenticationJWT =  new InsightsAuthenticationToken(
					authentication.getPrincipal(), jwtClaimsSet.getSubject(), null, authentication.getAuthorities());
		} else {
			log.error(" Error while validating token and retriving claims ");
			throw new InsightsAuthenticationException(" Error while validating token and retriving claims {} ");
		}
		return authenticationJWT; 
	}

	/**
	 * Method return supported provider of Token
	 */
	@Override
	public boolean supports(Class<?> authentication) {
		return InsightsAuthenticationToken.class.isAssignableFrom(authentication)
				|| UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication)
				|| authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

	
}
