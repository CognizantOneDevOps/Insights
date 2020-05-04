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

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.saml.SAMLEntryPoint;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.websso.WebSSOProfileOptions;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;

public class SamlWithRelayStateEntryPoint extends SAMLEntryPoint {


	/**
	 * used to forword to UI application after API validation
	 */
    @Override
    protected WebSSOProfileOptions getProfileOptions(SAMLMessageContext context, AuthenticationException exception) {

        WebSSOProfileOptions ssoProfileOptions;
        if (defaultOptions != null) {
            ssoProfileOptions = defaultOptions.clone();
        } else {
            ssoProfileOptions = new WebSSOProfileOptions();
        }

        // Not :
        // Add your custom logic here if you need it.
        // Original HttpRequest can be extracted from the context param
        // So you can let the caller pass you some special param which can be used to build an on-the-fly custom
        // relay state param


        ssoProfileOptions.setRelayState(ApplicationConfigProvider.getInstance().getSingleSignOnConfig().getRelayStateUrl());

        return ssoProfileOptions;
    }

}
