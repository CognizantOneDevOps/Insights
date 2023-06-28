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
package com.cognizant.devops.platformservice.security.config.saml;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationToken;

import com.cognizant.devops.platformservice.security.config.AuthenticationUtils;
import com.cognizant.devops.platformservice.security.config.InsightsAuthenticationToken;

public class InsightsSAMLTokenAuthenticationImpl implements AuthenticationProvider {
	
	private static Logger log = LogManager.getLogger(InsightsSAMLTokenAuthenticationImpl.class);
	
    
    @Override
    public boolean supports(Class<?> authentication) {
        return InsightsAuthenticationToken.class.isAssignableFrom(authentication) 
        		|| Saml2AuthenticationToken.class.isAssignableFrom(authentication);
    }
    
	/**
	 * This method is used to validate all subsequent request token
	 *
	 */
	@Override
	public Authentication authenticate(Authentication authentication) {
		log.debug("Inside InsightsAuthenticationProviderImpl === ");
			if (!supports(authentication.getClass())) {
	            throw new IllegalArgumentException("Only SAMLAuthenticationToken is supported, " + authentication.getClass() + " was attempted");
	        }
			
	        if (authentication.getPrincipal() == null) {
			log.debug("Authentication token is missing - authentication.getPrincipal() {} ",
					authentication.getPrincipal());
	            throw new AuthenticationCredentialsNotFoundException("Authentication token is missing");
	        }
		/*validate request token*/
		AuthenticationUtils.validateIncomingToken(authentication.getPrincipal());
        return authentication;
    }

	

}
