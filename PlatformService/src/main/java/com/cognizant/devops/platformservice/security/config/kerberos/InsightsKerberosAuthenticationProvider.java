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
package com.cognizant.devops.platformservice.security.config.kerberos;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.kerberos.authentication.KerberosServiceRequestToken;
import org.springframework.security.kerberos.authentication.KerberosTicketValidation;
import org.springframework.security.kerberos.authentication.KerberosTicketValidator;
import org.springframework.util.Assert;

import com.cognizant.devops.platformservice.security.config.InsightsAuthenticationException;

public class InsightsKerberosAuthenticationProvider implements AuthenticationProvider, InitializingBean {
	private static final Logger LOG = LogManager.getLogger(InsightsKerberosAuthenticationProvider.class);

	private KerberosTicketValidator ticketValidator;
	private UserDetailsService userDetailsService;
	private UserDetailsChecker userDetailsChecker = new AccountStatusUserDetailsChecker();

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		KerberosServiceRequestToken responseAuth=null;
		try {
			KerberosServiceRequestToken auth = (KerberosServiceRequestToken) authentication;
			byte[] token = auth.getToken();
			LOG.debug("Try to validate Kerberos Token");
			KerberosTicketValidation ticketValidation = this.ticketValidator.validateTicket(token);
			LOG.debug("Succesfully validated {} ",  ticketValidation.username());
			UserDetails userDetails = this.userDetailsService.loadUserByUsername(ticketValidation.username());
			userDetailsChecker.check(userDetails);
			additionalAuthenticationChecks(userDetails, auth);
			responseAuth = new KerberosServiceRequestToken(userDetails, ticketValidation,
					userDetails.getAuthorities(), token);
			responseAuth.setDetails(authentication.getDetails());
		} catch (Exception e) {
			LOG.error("Exception while validation of kerberos token  {} ",e);
			throw new InsightsAuthenticationException(e.getMessage(), e);
		}
		return responseAuth;
	}

	@Override
	public boolean supports(Class<? extends Object> auth) {
		return KerberosServiceRequestToken.class.isAssignableFrom(auth);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.ticketValidator, "ticketValidator must be specified");
		Assert.notNull(this.userDetailsService, "userDetailsService must be specified");
	}

	/**
	 * The <code>UserDetailsService</code> to use, for loading the user properties
	 * and the <code>GrantedAuthorities</code>.
	 *
	 * @param userDetailsService
	 *            the new user details service
	 */
	public void setUserDetailsService(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	/**
	 * The <code>KerberosTicketValidator</code> to use, for validating
	 * the Kerberos/SPNEGO tickets.
	 *
	 * @param ticketValidator
	 *            the new ticket validator
	 */
	public void setTicketValidator(KerberosTicketValidator ticketValidator) {
		this.ticketValidator = ticketValidator;
	}

	/**
	 * Allows subclasses to perform any additional checks of a returned
	 * <code>UserDetails</code>
	 * for a given authentication request.
	 *
	 * @param userDetails
	 *            as retrieved from the {@link UserDetailsService}
	 * @param authentication
	 *            validated {@link KerberosServiceRequestToken}
	 * @throws AuthenticationException
	 *             AuthenticationException if the credentials could not be validated
	 *             (generally a
	 *             <code>BadCredentialsException</code>, an
	 *             <code>AuthenticationServiceException</code>)
	 */
	protected void additionalAuthenticationChecks(UserDetails userDetails, KerberosServiceRequestToken authentication)
			throws AuthenticationException {
	}
}
