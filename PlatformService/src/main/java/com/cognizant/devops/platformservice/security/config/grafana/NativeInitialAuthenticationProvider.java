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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import com.cognizant.devops.platformservice.security.config.InsightsAuthenticationException;
import com.cognizant.devops.platformservice.security.config.InsightsAuthenticationToken;

public class NativeInitialAuthenticationProvider implements AuthenticationProvider {
	private static Logger log = LogManager.getLogger(NativeInitialAuthenticationProvider.class);

	/**
	 * Used to authenticate Native Grafana
	 */
	@Override
	public Authentication authenticate(Authentication authentication)  {
		log.debug(" In Initial NativeInitialAuthenticationProvider first time login ==== ");
		if (!supports(authentication.getClass())) {
			throw new IllegalArgumentException(
					"Only NativeAuthenticationProvider UsernamePasswordAuthenticationToken is supported, " + authentication.getClass() + " was attempted");
		}

		if (authentication.getPrincipal() == null) {
			log.error("Authentication token is missing - authentication.getPrincipal() {} ",
					authentication.getPrincipal());
			throw new AuthenticationCredentialsNotFoundException("Authentication token is missing");
		}
		
		UserDetails userDetail = GrafanaUserDetailsUtil.getUserDetails(String.valueOf(authentication.getPrincipal())); //UserDetails user
		
		InsightsAuthenticationToken authenticationGrafana = null;
		if (userDetail == null) {
			log.error(" Invalid Authentication for native Grafana ");
			throw new InsightsAuthenticationException(" Invalid Authentication for native Grafana ");
		} else {
			authenticationGrafana = new InsightsAuthenticationToken(userDetail,
					GrafanaUserDetailsUtil.getGrafanaResponseCookies(),
					userDetail.getPassword(), userDetail.getAuthorities());
			log.debug("In InsightsAuthenticationToken validation complete ==== ");
		}
		return authenticationGrafana;
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
