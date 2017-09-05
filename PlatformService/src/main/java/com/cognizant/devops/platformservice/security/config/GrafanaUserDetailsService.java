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

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.dal.rest.RestHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.ClientResponse;

@Component
public class GrafanaUserDetailsService implements UserDetailsService {
	static Logger log = Logger.getLogger(GrafanaUserDetailsService.class.getName());

	@Autowired
	private HttpServletRequest request;
	
	@Override
	public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
		String authHeader = request.getHeader("Authorization");
		String decodedAuthHeader;
		try {
			decodedAuthHeader = new String(Base64.getDecoder().decode(authHeader.split(" ")[1]), "UTF-8");
			String[] authTokens = decodedAuthHeader.split(":");
			JsonObject loginRequestParams = new JsonObject();
			loginRequestParams.addProperty("user", authTokens[0]);
			loginRequestParams.addProperty("password", authTokens[1]);
			String loginApiUrl = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint()+"/login";
			ClientResponse grafanaLoginResponse = RestHandler.doPost(loginApiUrl, loginRequestParams, null);
			JsonObject responseJson = new JsonParser().parse(grafanaLoginResponse.getEntity(String.class)).getAsJsonObject();
			Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
			authorities.add(new SimpleGrantedAuthority("VIEWER"));
			if("Logged in".equals(responseJson.get("message").getAsString())){
				return new org.springframework.security.core.userdetails.User(authTokens[0], authTokens[1], authorities);
			}
		} catch (UnsupportedEncodingException e) {
			log.error("Unable to load native user details", e);
		}
		return null;
	}

}
