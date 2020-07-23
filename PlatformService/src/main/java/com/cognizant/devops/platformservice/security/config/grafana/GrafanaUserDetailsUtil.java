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
package com.cognizant.devops.platformservice.security.config.grafana;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.NewCookie;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.dal.grafana.GrafanaHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.security.config.AuthenticationUtils;
import com.cognizant.devops.platformservice.security.config.SpringAuthority;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class GrafanaUserDetailsUtil {
	private static final Logger log = LogManager.getLogger(GrafanaUserDetailsUtil.class);
	
	

	/**
	 * used to validate grafana user detail and add grafana cookies in request
	 * header
	 * 
	 * @param request
	 * @return
	 */
	public static UserDetails getUserDetails(HttpServletRequest request) {
		log.debug(" Inside getUserDetails function call!");
		ApplicationConfigProvider.performSystemCheck();
		String token = ValidationUtils.cleanXSS(request.getHeader(AuthenticationUtils.AUTH_HEADER_KEY)); // 
		String authHeader = ValidationUtils.decryptAutharizationToken(token);
		Map<String, String> grafanaResponseCookies = new HashMap<>();
		Map<String, String> headers = new HashMap<>();
		String userName = null;
		String credential = null;
		// Validate if the Logged in user is same as that of grafana logged in user
		try {
			String decodedAuthHeader = new String(Base64.getDecoder().decode(authHeader.split(" ")[1]),
					StandardCharsets.UTF_8);
			String[] authTokens = decodedAuthHeader.split(":");
			userName = authTokens[0];
			credential = authTokens[1];

			log.debug("GrafanaUserDetailsUtil ====Establishing valid Grafana's session ");
			List<NewCookie> currentGrafanacookies = getValidGrafanaSession(authTokens[0], authTokens[1]);

			grafanaResponseCookies = currentGrafanacookies.stream()
					.collect(Collectors.toMap(cookie -> ValidationUtils.cleanXSS(cookie.getName()),
							cookie -> ValidationUtils.cleanXSS(cookie.getValue())));
			log.debug("GrafanaUserDetailsUtil ==== grafanaResponseCookies {} ", grafanaResponseCookies);

			String grafanaCookie = grafanaResponseCookies.entrySet().stream()
					.map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("; HttpOnly "));
			log.debug("GrafanaUserDetailsUtil ==== grafanaCookie string {} ", grafanaCookie);

			headers.put("Cookie", grafanaCookie);
			getCurrentOrgAndRole(headers, grafanaResponseCookies);

			List<GrantedAuthority> mappedAuthorities = new ArrayList<>();
			String grafanaRole = grafanaResponseCookies.get(AuthenticationUtils.GRAFANA_ROLE_KEY);

			if (grafanaResponseCookies.containsKey(AuthenticationUtils.GRAFANA_ROLE_KEY)) {
				mappedAuthorities.add(AuthenticationUtils.getSpringAuthorityRole(grafanaRole));
			} else {
				log.debug("GrafanaUserDetailsUtil ==== Application role is found to be NULL");
				mappedAuthorities.add(SpringAuthority.valueOf("INVALID"));
			}

			request.setAttribute("responseHeaders", grafanaResponseCookies);
			return new User(userName, credential, true, true, true, true, mappedAuthorities);

		} catch (Exception e) {
			log.error(e);
		}
		log.debug("No user details were found!");
		return null;
	}

	/**
	 * used to fetch grafana current organization and based on that organization it
	 * fetch role, both will be added to grafanaResponseCookies.
	 * 
	 * @param headers
	 * @param grafanaResponseCookies
	 * @throws InsightsCustomException
	 */
	private static void getCurrentOrgAndRole(Map<String, String> headers, Map<String, String> grafanaResponseCookies)
			throws InsightsCustomException {
		GrafanaHandler grafanaHandler = new GrafanaHandler();
		log.debug("Inside getCurrentOrgRole function call!");
		String grafanaCurrentOrgResponse = grafanaHandler.grafanaGet("/api/user", headers);
		JsonObject responseJson = new JsonParser().parse(grafanaCurrentOrgResponse).getAsJsonObject();
		String grafanaCurrentOrg = responseJson.get("orgId").toString();
		if (grafanaCurrentOrg != null) {
			grafanaResponseCookies.put("grafanaOrg", ValidationUtils.cleanXSS(grafanaCurrentOrg));
			String userOrgsApiUrl = "/api/user/orgs";
			String grafanaRoleResponse = grafanaHandler.grafanaGet(userOrgsApiUrl, headers);
			JsonArray grafanaOrgs = new JsonParser().parse(grafanaRoleResponse).getAsJsonArray();
			String grafanaCurrentOrgRole = null;
			for (JsonElement org : grafanaOrgs) {
				if (grafanaCurrentOrg.equals(org.getAsJsonObject().get("orgId").toString())) {
					grafanaCurrentOrgRole = org.getAsJsonObject().get("role").getAsString();
					break;
				}
			}
			if (grafanaCurrentOrgRole != null) {
				grafanaResponseCookies.put("grafanaRole", ValidationUtils.cleanXSS(grafanaCurrentOrgRole));
			}
		}
	}

	/**
	 * used to create new session with grafana (login with grafana)
	 * 
	 * @param userName
	 * @param password
	 * @return
	 * @throws InsightsCustomException
	 */
	private static List<NewCookie> getValidGrafanaSession(String userName, String password)
			throws InsightsCustomException {
		GrafanaHandler grafanaHandler = new GrafanaHandler();
		log.debug("Inside getValidGrafanaSession method call");
		JsonObject loginRequestParams = new JsonObject();
		loginRequestParams.addProperty("user", userName);
		loginRequestParams.addProperty("password", password);
		String loginApiUrl =  "/login";
		return grafanaHandler.getGrafanaCookies(loginApiUrl, loginRequestParams, null);
		
	}

}
