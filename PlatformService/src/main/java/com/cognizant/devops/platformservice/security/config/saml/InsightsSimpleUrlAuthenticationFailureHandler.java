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
package com.cognizant.devops.platformservice.security.config.saml;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import com.cognizant.devops.platformservice.security.config.AuthenticationUtils;

public class InsightsSimpleUrlAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
	private static Logger Log = LogManager.getLogger(InsightsSimpleUrlAuthenticationFailureHandler.class);
	private String defaultFailureUrl;
	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	public InsightsSimpleUrlAuthenticationFailureHandler(String defaultFailureUrl) {
		super(defaultFailureUrl);
		this.defaultFailureUrl = defaultFailureUrl;
	}

	/**
	 * Used to redirect after unautheticated l
	 */
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		String logoutURL = AuthenticationUtils.getLogoutURL(request, AuthenticationUtils.UNAUTHORISE,
				"Unauthorized request , Authentication not successful ");
		Log.debug(" logoutURL  in InsightsSimpleUrlAuthenticationFailureHandler ==== {} {} ", defaultFailureUrl,
				logoutURL);
		saveException(request, exception);
		redirectStrategy.sendRedirect(request, response, logoutURL);

	}
}
