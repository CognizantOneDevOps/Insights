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

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class InsightsAuthenticationToken extends AbstractAuthenticationToken {

	private static final long serialVersionUID = -5778228220445024521L;
	private final transient Object principal;
	private final transient Object credentials;

	public InsightsAuthenticationToken(Object principal) {
		super(null);
		this.principal = principal;
		this.credentials = null;
	}

	/**
	 * used to set AbstractAuthenticationToken
	 * 
	 * @param principal
	 * @param details
	 * @param credentials
	 * @param authorities
	 */
	public InsightsAuthenticationToken(Object principal, Object details, Object credentials,
			Collection<? extends GrantedAuthority> authorities) {
		super(authorities);
		this.principal = principal;
		this.credentials = credentials;
		super.setDetails(details);
		super.setAuthenticated(true);
	}

	/**
	 * used to get user credential based on Authentication protocol
	 */
	@Override
	public Object getCredentials() {
		return credentials;
	}

	/**
	 * used to get Auth user detail
	 */
	@Override
	public Object getPrincipal() {
		return principal;
	}

}
