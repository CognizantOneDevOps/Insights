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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.providers.ExpiringUsernameAuthenticationToken;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;

public class InsightsAuthenticationProviderImpl implements AuthenticationProvider {
	
	private static Logger LOG = LogManager.getLogger(InsightsAuthenticationProviderImpl.class);
	
    
    @Override
    public boolean supports(Class<?> authentication) {
        return InsightsAuthenticationToken.class.isAssignableFrom(authentication) 
        		|| ExpiringUsernameAuthenticationToken.class.isAssignableFrom(authentication);
    }
    
	@Override
	public Authentication authenticate(Authentication authentication) throws InsightsAuthenticationException {
		LOG.debug("Inside InsightsAuthenticationProviderImpl === ");
		//if(!(authentication.getCredentials() instanceof SAMLCredential)) {
			
	        
			if (!supports(authentication.getClass())) {
	            throw new IllegalArgumentException("Only SAMLAuthenticationToken is supported, " + authentication.getClass() + " was attempted");
	        }
			
			/*
			 * Assert.isInstanceOf(InsightsAuthenticationToken.class, authentication,
			 * "This method only accepts JwtAuthenticationToken");
			 */
			
	        if (authentication.getPrincipal() == null) {
	        	LOG.debug("Authentication token is missing - authentication.getPrincipal() "+authentication.getPrincipal());
	            throw new AuthenticationCredentialsNotFoundException("Authentication token is missing");
	        }
	    
			try {
				TokenProviderUtility tokenProviderUtility = new TokenProviderUtility();
				boolean isTokenVarified=tokenProviderUtility.verifyToken(authentication.getPrincipal().toString());
				LOG.debug(" isTokenVarified ==== "+isTokenVarified);
			}catch (InsightsCustomException e) {
				LOG.error(e);
				LOG.error(" Exception while varifing token "+e.getMessage(),e);
				throw new InsightsAuthenticationException(e.getMessage());
			}catch (AuthorizationServiceException e) {
				LOG.error(e);
				LOG.error(" Exception while validating token "+e.getMessage());
				throw new InsightsAuthenticationException(e.getMessage(),e);
			}catch (AuthenticationCredentialsNotFoundException e) {
				LOG.error(e);
				LOG.error(" Token not found in cache "+e.getMessage());
				throw new InsightsAuthenticationException(e.getMessage(),e);
			}catch (AccountExpiredException e) {
				LOG.error(e);
				LOG.error(" Token Expire "+e.getMessage());
				throw new InsightsAuthenticationException(e.getMessage(),e);
			}catch (Exception e) {
				LOG.error(e);
				LOG.error(" Error while validating token "+e.getMessage());
				throw new InsightsAuthenticationException(e.getMessage(),e);
			}
		//}
        return authentication;

    }
	
}
