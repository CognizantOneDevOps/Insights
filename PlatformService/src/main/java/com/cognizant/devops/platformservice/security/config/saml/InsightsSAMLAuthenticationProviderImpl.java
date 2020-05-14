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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensaml.common.SAMLException;
import org.opensaml.common.SAMLRuntimeException;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.validation.ValidationException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.providers.ExpiringUsernameAuthenticationToken;
import org.springframework.security.saml.SAMLAuthenticationProvider;
import org.springframework.security.saml.SAMLAuthenticationToken;
import org.springframework.security.saml.SAMLConstants;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.context.SAMLMessageContext;

import com.cognizant.devops.platformservice.security.config.SpringAuthority;

public class InsightsSAMLAuthenticationProviderImpl extends SAMLAuthenticationProvider {
	
	private static Logger log = LogManager.getLogger(InsightsSAMLAuthenticationProviderImpl.class);
    private boolean excludeCredential = false;
    
	/**
	 * Used to autheticate initial SAML login request, It will redirect user to
	 * service provider login URL and then fetch necessay user detail.
	 * It also create ExpiringUsernameAuthenticationToken and set it in spring
	 * security context
	 * 
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		
		log.debug(" Inside InsightsSAMLAuthenticationProviderImpl === ");

        if (!supports(authentication.getClass())) {
            throw new IllegalArgumentException("Only SAMLAuthenticationToken is supported, " + authentication.getClass() + " was attempted");
        }

        SAMLAuthenticationToken token = (SAMLAuthenticationToken) authentication;
        SAMLMessageContext context = token.getCredentials();

        if (context == null) {
            throw new AuthenticationServiceException("SAML message context is not available in the authentication token");
        }

        SAMLCredential credential;

        try {
            if (SAMLConstants.SAML2_WEBSSO_PROFILE_URI.equals(context.getCommunicationProfileId())) {
                credential = consumer.processAuthenticationResponse(context);
            } else if (SAMLConstants.SAML2_HOK_WEBSSO_PROFILE_URI.equals(context.getCommunicationProfileId())) {
                credential = hokConsumer.processAuthenticationResponse(context);
            } else {
                throw new SAMLException("Unsupported profile encountered in the context " + context.getCommunicationProfileId());
            }
        } catch (SAMLRuntimeException e) {
			log.debug("Error validating SAML message {}", e);
            samlLogger.log(SAMLConstants.AUTH_N_RESPONSE, SAMLConstants.FAILURE, context, e);
            throw new AuthenticationServiceException("Error validating SAML message", e);
        } catch (SAMLException e) {
			log.debug("Error validating SAML message {}", e);
            samlLogger.log(SAMLConstants.AUTH_N_RESPONSE, SAMLConstants.FAILURE, context, e);
            throw new AuthenticationServiceException("Error validating SAML message", e);
        } catch (ValidationException e) {
			log.debug("Error validating signature {}", e);
            samlLogger.log(SAMLConstants.AUTH_N_RESPONSE, SAMLConstants.FAILURE, context, e);
            throw new AuthenticationServiceException("Error validating SAML message signature", e);
        } catch (org.opensaml.xml.security.SecurityException e) {
            log.debug("Error validating signature", e);
            samlLogger.log(SAMLConstants.AUTH_N_RESPONSE, SAMLConstants.FAILURE, context, e);
            throw new AuthenticationServiceException("Error validating SAML message signature", e);
        } catch (DecryptionException e) {
            log.debug("Error decrypting SAML message", e);
            samlLogger.log(SAMLConstants.AUTH_N_RESPONSE, SAMLConstants.FAILURE, context, e);
            throw new AuthenticationServiceException("Error decrypting SAML message", e);
        }
        
        SamlUserDetails userDetails = (SamlUserDetails)getUserDetails(credential);
        Object principal = getPrincipal(credential, userDetails);
        
        List<GrantedAuthority> updatedAuthorities = new ArrayList<>();
		updatedAuthorities.add(SpringAuthority.valueOf("Viewer"));

        Date expiration = getExpirationDate(credential);

        SAMLCredential authenticationCredential = excludeCredential ? null : credential;
        ExpiringUsernameAuthenticationToken result = new ExpiringUsernameAuthenticationToken(expiration, principal, authenticationCredential, updatedAuthorities);
        result.setDetails(userDetails);
        samlLogger.log(SAMLConstants.AUTH_N_RESPONSE, SAMLConstants.SUCCESS, context, result, null);
        return result;
    }
}
