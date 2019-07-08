package com.cognizant.devops.platformservice.security.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.GenericFilterBean;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformservice.customsettings.CustomAppSettings;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;

/*********************************************************************************
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
 *******************************************************************************/

public class AdvanceAuthenticationFilter extends GenericFilterBean {
	private static Logger LOG = LogManager.getLogger(CustomAppSettings.class);

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException {
		//LOG.debug(" Inside AdvanceAuthenticationFilter ==== ");
		HttpServletResponse httpResponce = (HttpServletResponse) response;
		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		try {
			//LOG.debug(" AdvanceAuthenticationFilter Url " + httpRequest.getRequestURL().toString());
			UserDetails user = GrafanaUserDetailsUtil.getUserDetails(httpRequest);
			if (user == null) {
				LOG.error(" InsightsCustomException Invalid Autharization Token ");
				throw new RuntimeException(PlatformServiceConstants.INVALID_TOKEN);
			} else {
				//LOG.debug("inside AdvanceAuthenticationFilter arg0 athentication for user " + user.getUsername() + "   "
						//+ user.getPassword());
				final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user,
						null, user.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(authentication);
				chain.doFilter(request, response);
			}
		} catch (Exception e) {
			LOG.error("Invalid request in AdvanceAuthenticationFilter");
			String msg = PlatformServiceUtil.buildFailureResponse("Unauthorized Access..").toString();
			httpResponce.setContentType("application/json");
			httpResponce.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			httpResponce.getWriter().write(msg);
			httpResponce.getWriter().flush();
			httpResponce.getWriter().close();
		}
	}
}
