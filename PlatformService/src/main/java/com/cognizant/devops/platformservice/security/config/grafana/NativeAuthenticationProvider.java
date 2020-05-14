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
package com.cognizant.devops.platformservice.security.config.grafana;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.cognizant.devops.platformservice.security.config.InsightsAuthenticationToken;

public class NativeAuthenticationProvider implements AuthenticationProvider {
	private static Logger Log = LogManager.getLogger(NativeAuthenticationProvider.class);

	/**
	 * Used to authenticate Native Grafana
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		Log.debug(" In NativeAuthenticationProvider first time login ... ");
		/*Grafana Authentication is already done, 
		This class is used to validate additional security like JWT token validation*/
		return authentication;
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

}
