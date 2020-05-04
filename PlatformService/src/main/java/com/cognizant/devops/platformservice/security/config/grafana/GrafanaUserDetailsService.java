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
package com.cognizant.devops.platformservice.security.config.grafana;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class GrafanaUserDetailsService implements UserDetailsService {
	static Logger log = LogManager.getLogger(GrafanaUserDetailsService.class.getName());

	@Autowired
	private HttpServletRequest request;
	
	/**
	 * used to loads user-specific data.
	 *
	 */
	@Override
	public UserDetails loadUserByUsername(String login) {
		log.debug(" In GrafanaUserDetailsService Grafana ...... ");
		BCryptPasswordEncoder encoder = passwordEncoder();
		UserDetails user = GrafanaUserDetailsUtil.getUserDetails(request);
		return new org.springframework.security.core.userdetails.User(user.getUsername(),
				encoder.encode(user.getPassword()), user.getAuthorities());
	}
	
	/**
	 * used default password encoder
	 * 
	 * @return
	 */
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
