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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.security.config.InsightsAuthenticationException;
import com.cognizant.devops.platformservice.security.config.InsightsAuthenticationToken;
import com.cognizant.devops.platformservice.security.config.TokenProviderUtility;
import com.nimbusds.jwt.JWTClaimsSet;

public class JWTAuthenticationProvider implements AuthenticationProvider {
	private static Logger Log = LogManager.getLogger(JWTAuthenticationProvider.class);



	/**
	 * Used to authenticate Native Grafana
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		Log.debug("Inside JWTAuthenticationProvider === ");
		if (!supports(authentication.getClass())) {
			throw new IllegalArgumentException(
					"Only JWTAuthenticationProvider is supported, " + authentication.getClass() + " was attempted");
		}

		if (authentication.getPrincipal() == null) {
			Log.debug(
					"In JWTAuthenticationProvider Authentication token is missing - authentication.getPrincipal() {} ",
					authentication.getPrincipal());
			throw new AuthenticationCredentialsNotFoundException("Authentication token is missing");
		}
		/*validate request token*/
		JWTClaimsSet jwtClaimsSet = validateIncomingToken(authentication.getPrincipal());
		if (jwtClaimsSet != null) {
			InsightsAuthenticationToken jwtAuthenticationToken = new InsightsAuthenticationToken(
					authentication.getPrincipal(), jwtClaimsSet.getSubject(), null, authentication.getAuthorities());
			return jwtAuthenticationToken;
		} else {
			Log.error(" Error while validating token and retriving claims ");
			throw new InsightsAuthenticationException(" Error while validating token and retriving claims {} ");
		}
	}

	/**
	 * Method retun supporrted provider ot Token
	 */
	@Override
	public boolean supports(Class<?> authentication) {
		return InsightsAuthenticationToken.class.isAssignableFrom(authentication)
				|| UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication)
				|| authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

	public JWTClaimsSet validateIncomingToken(Object principal) {
		JWTClaimsSet jwtClaimsSet = null;
		TokenProviderUtility tokenProviderUtility = new TokenProviderUtility();
		try {
			jwtClaimsSet = tokenProviderUtility.verifyAndFetchCliaimsToken(principal.toString());
			Log.debug(" isTokenVarified  jwtClaimsSet are ==== {} ", jwtClaimsSet);
		} catch (InsightsCustomException e) {
			Log.error(e);
			Log.error(" Exception while varifing token " + e.getMessage(), e);
			throw new InsightsAuthenticationException(e.getMessage());
		} catch (AuthenticationCredentialsNotFoundException e) {
			Log.error(e);
			Log.error(" Token not found in cache {} ", e.getMessage());
			throw new InsightsAuthenticationException(e.getMessage(), e);
		} catch (AccountExpiredException e) {
			Log.error(e);
			Log.error(" Token Expire {}", e.getMessage());
			throw new InsightsAuthenticationException(e.getMessage(), e);
		} catch (Exception e) {
			Log.error(e);
			Log.error(" Error while validating token {} ", e.getMessage());
			throw new InsightsAuthenticationException(e.getMessage(), e);
		}
		return jwtClaimsSet;
	}

}
