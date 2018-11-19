/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 *   
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * 	of the License at
 *   
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformservice.security.config;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.NewCookie;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.dal.rest.RestHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.ClientResponse;

public class GrafanaUserDetailsUtil {
	private static final Logger log = LogManager.getLogger(GrafanaUserDetailsUtil.class);

	public static UserDetails getUserDetails(HttpServletRequest httpRequest) {
		log.debug("\n\nInside getUserDetails function call!");
		ApplicationConfigProvider.performSystemCheck();
		String authHeader = httpRequest.getHeader("Authorization");
		Cookie[] requestCookies = httpRequest.getCookies();
		//log.debug("\nCookies:\n" + requestCookies);
		Map<String, String> grafanaResponseCookies = new HashMap<String, String>();
		Map<String, String> cookieMap = new HashMap<String, String>();
		if (requestCookies != null) {
			for (Cookie cookie : requestCookies) {
				cookieMap.put(cookie.getName(), cookie.getValue());
			}
		}
		//log.debug("\nCookies Map:\n" + cookieMap);
		String userName = null;
		String credential = null;
		// Validate if the Logged in user is same as that of grafana logged in user
		try {
			String decodedAuthHeader = new String(Base64.getDecoder().decode(authHeader.split(" ")[1]), "UTF-8");
			String[] authTokens = decodedAuthHeader.split(":");
			userName = authTokens[0];
			credential = authTokens[1];
			String grafanaUser = cookieMap.get("grafana_user");
			String grafanaSession = cookieMap.get("grafana_sess");
			//log.debug("Insights User Name is: " + userName);
			//log.debug("Grafana's User Name is: " + grafanaUser);
			if (userName.equals(grafanaUser) && grafanaSession != null) {
				log.debug("LDAP user is found. Insights username matches Grafana's username");
				grafanaResponseCookies.putAll(cookieMap);
				//log.debug("Grafana's resposne cookies are: " + grafanaResponseCookies);
				if ((grafanaResponseCookies.get("grafanaOrg") == null
						|| grafanaResponseCookies.get("grafanaOrg").isEmpty())
						&& (grafanaResponseCookies.get("grafanaRole") == null
								|| grafanaResponseCookies.get("grafanaRole").isEmpty())) {
					log.debug("NULL values found in Grafana response cookies");
					Map<String, String> headers = new HashMap<String, String>();
					StringBuffer grafanaCookie = new StringBuffer();
					for (Map.Entry<String, String> cookie : grafanaResponseCookies.entrySet()) {
						grafanaCookie.append(cookie.getKey()).append("=").append(cookie.getValue()).append(";");
					}
					headers.put("Cookie", grafanaCookie.toString());
					String grafanaCurrentOrg = getGrafanaCurrentOrg(headers);
					grafanaResponseCookies.put("grafanaOrg", grafanaCurrentOrg);
					String grafanaCurrentOrgRole = getCurrentOrgRole(headers, grafanaCurrentOrg);
					grafanaResponseCookies.put("grafanaRole", grafanaCurrentOrgRole);
					//log.debug("Re-initilized Grafana response cookies are: " + grafanaResponseCookies);
				}
			} else {
				log.debug("Grafana's native user found! Establishing valid garafna session");
				List<NewCookie> cookies = getValidGrafanaSession(authTokens[0], authTokens[1]);
				log.debug("Fetching cookies: " + cookies);
				StringBuffer grafanaCookie = new StringBuffer();
				for (NewCookie cookie : cookies) {
					grafanaResponseCookies.put(cookie.getName(), cookie.getValue());
					grafanaCookie.append(cookie.getName()).append("=").append(cookie.getValue()).append(";");
				}
				Map<String, String> headers = new HashMap<String, String>();
				headers.put("Cookie", grafanaCookie.toString());
				String grafanaCurrentOrg = getGrafanaCurrentOrg(headers);
				grafanaResponseCookies.put("grafanaOrg", grafanaCurrentOrg);
				String grafanaCurrentOrgRole = getCurrentOrgRole(headers, grafanaCurrentOrg);
				grafanaResponseCookies.put("grafanaRole", grafanaCurrentOrgRole);
				log.debug("The Grafana cookies are: " + grafanaResponseCookies);
			}
			List<GrantedAuthority> mappedAuthorities = new ArrayList<GrantedAuthority>();
			String grafanaRole = grafanaResponseCookies.get("grafanaRole");
			if (grafanaRole == null || grafanaRole.trim().length() == 0) {
				log.debug("Application role is found to be NULL");
				mappedAuthorities.add(SpringAuthority.valueOf("INVALID"));
			} else {
				mappedAuthorities.add(SpringAuthorityUtil.getSpringAuthorityRole(grafanaRole));
			}

			httpRequest.setAttribute("responseHeaders", grafanaResponseCookies);
			if (ApplicationConfigProvider.getInstance().isEnableNativeUsers()) {
				return new User(userName, credential, true, true, true, true, mappedAuthorities);
			} else {
				return new User(userName, "", true, true, true, true, mappedAuthorities);
			}
		} catch (Exception e) {
			log.error(e);
		}
		log.debug("No user details were found!");
		return null;
	}

	private static String getCurrentOrgRole(Map<String, String> headers, String grafanaCurrentOrg) {
		log.debug("\nInside getCurrentOrgRole function call!");
		String userOrgsApiUrl = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint()
				+ "/api/user/orgs";
		//log.debug("Fetching User's Organizations at: " + userOrgsApiUrl);
		ClientResponse grafanaCurrentOrgResponse = RestHandler.doGet(userOrgsApiUrl, null, headers);
		JsonArray grafanaOrgs = new JsonParser().parse(grafanaCurrentOrgResponse.getEntity(String.class))
				.getAsJsonArray();
		//log.debug("User's current organization ID is: " + grafanaCurrentOrg);
		String grafanaCurrentOrgRole = null;
		for (JsonElement org : grafanaOrgs) {
			if (grafanaCurrentOrg.equals(org.getAsJsonObject().get("orgId").toString())) {
				grafanaCurrentOrgRole = org.getAsJsonObject().get("role").getAsString();
				break;
			}
		}
		log.debug("User's current org role is: " + grafanaCurrentOrgRole);
		return grafanaCurrentOrgRole;
	}

	private static String getGrafanaCurrentOrg(Map<String, String> headers) {
		log.debug("Inside getGrafanaCurrentOrg method call");
		String loginApiUrl = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint() + "/api/user";
		//log.debug("Fetching user's detail at: " + loginApiUrl);
		ClientResponse grafanaCurrentOrgResponse = RestHandler.doGet(loginApiUrl, null, headers);
		JsonObject responseJson = new JsonParser().parse(grafanaCurrentOrgResponse.getEntity(String.class))
				.getAsJsonObject();
		log.debug("Response obtained after client call is: " + responseJson);
		String grafanaCurrentOrg = responseJson.get("orgId").toString();
		log.debug("User's current Org is: " + grafanaCurrentOrg);
		return grafanaCurrentOrg;
	}

	private static List<NewCookie> getValidGrafanaSession(String userName, String password) {
		log.debug("Inside getValidGrafanaSession method call");
		JsonObject loginRequestParams = new JsonObject();
		loginRequestParams.addProperty("user", userName);
		loginRequestParams.addProperty("password", password);
		String loginApiUrl = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint() + "/login";
		log.debug("Fetching valid session at: " + loginApiUrl);
		ClientResponse grafanaLoginResponse = RestHandler.doPost(loginApiUrl, loginRequestParams, null);
		return grafanaLoginResponse.getCookies();
	}
}
