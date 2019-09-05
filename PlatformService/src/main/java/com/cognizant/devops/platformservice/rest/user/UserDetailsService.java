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
package com.cognizant.devops.platformservice.rest.user;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.NewCookie;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.dal.rest.RestHandler;
import com.cognizant.devops.platformservice.core.ServiceResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.ClientResponse;

@RestController
@RequestMapping("/user")
public class UserDetailsService {
	private static Logger log = LogManager.getLogger(UserDetailsService.class.getName());

	private DefaultSpringSecurityContextSource contextSource;

	@RequestMapping(value = "/authenticate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public ServiceResponse authenticateUser(HttpServletRequest request) {
		ServiceResponse response = new ServiceResponse();
		response.setStatus(ConfigOptions.SUCCESS_RESPONSE);
		response.setData(request.getAttribute("responseHeaders"));
		request.getSession().setMaxInactiveInterval(30 * 60);
		return response;
	}

	@RequestMapping(value = "/logout", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public ServiceResponse logout(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		SecurityContextHolder.clearContext();
		if (session != null) {
			session.invalidate();
		}
		Map<String, String> grafanaHeaders = new HashMap<String, String>();
		grafanaHeaders.put("grafanaOrg", null);
		grafanaHeaders.put("grafanaRole", null);
		grafanaHeaders.put("grafana_user", null);
		grafanaHeaders.put("grafana_sess", null);
		// To Do : get a service to do a Grafana Logout
		request.setAttribute("responseHeaders", grafanaHeaders);
		ServiceResponse response = new ServiceResponse();
		response.setStatus(ConfigOptions.SUCCESS_RESPONSE);
		return response;
	}

	@RequestMapping(value = "/getCurrentOrgAndRole", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject getCurrentOrgAndRole(HttpServletRequest httpRequest) {
		String authHeader = ValidationUtils.extactAutharizationToken(httpRequest.getHeader("Authorization"));
		log.debug(" authTokenDecrypt  ========= " + authHeader);
		Map<String, String> grafanaResponseCookies = new HashMap<String, String>();
		JsonObject grafanaOrgRoleDataJsonObj = new JsonObject();
		try {
			String decodedAuthHeader = new String(Base64.getDecoder().decode(authHeader.split(" ")[1]), "UTF-8");
			String[] authTokens = decodedAuthHeader.split(":");
			List<NewCookie> cookies = getValidGrafanaSession(authTokens[0], authTokens[1]);
			StringBuffer grafanaCookie = new StringBuffer();
			for (NewCookie cookie : cookies) {
				grafanaResponseCookies.put(cookie.getName(), cookie.getValue());
				grafanaCookie.append(cookie.getName()).append("=").append(cookie.getValue()).append(";");
			}
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Cookie", grafanaCookie.toString());
			// String grafanaCurrentOrg = getGrafanaCurrentOrg(headers);
			JsonObject response = getGrafanaUserResponse(headers);
			String grafanaCurrentOrg = response.get("orgId").toString();
			String grafanaUserName = response.get("name").toString();
			String grafanaCurrentOrgRole = getCurrentOrgRole(headers, grafanaCurrentOrg);
			grafanaOrgRoleDataJsonObj.addProperty("grafanaCurrentOrg", grafanaCurrentOrg);
			grafanaOrgRoleDataJsonObj.addProperty("grafanaCurrentOrgRole", grafanaCurrentOrgRole);
			grafanaOrgRoleDataJsonObj.addProperty("userName", grafanaUserName);
		} catch (Exception e) {
			log.error("Unable to get the User Role for current org.", e);
		}
		return grafanaOrgRoleDataJsonObj;
	}

	private String getCurrentOrgRole(Map<String, String> headers, String grafanaCurrentOrg) {
		String userOrgsApiUrl = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint()
				+ "/api/user/orgs";
		ClientResponse grafanaCurrentOrgResponse = RestHandler.doGet(userOrgsApiUrl, null, headers);
		JsonArray grafanaOrgs = new JsonParser().parse(grafanaCurrentOrgResponse.getEntity(String.class))
				.getAsJsonArray();
		String grafanaCurrentOrgRole = null;
		for (JsonElement org : grafanaOrgs) {
			if (grafanaCurrentOrg.equals(org.getAsJsonObject().get("orgId").toString())) {
				grafanaCurrentOrgRole = org.getAsJsonObject().get("role").getAsString();
				break;
			}
		}
		return grafanaCurrentOrgRole;
	}

	private String getGrafanaCurrentOrg(Map<String, String> headers) {
		String loginApiUrl = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint() + "/api/user";
		ClientResponse grafanaCurrentOrgResponse = RestHandler.doGet(loginApiUrl, null, headers);
		JsonObject responseJson = new JsonParser().parse(grafanaCurrentOrgResponse.getEntity(String.class))
				.getAsJsonObject();
		String grafanaCurrentOrg = responseJson.get("orgId").toString();
		return grafanaCurrentOrg;
	}

	private JsonObject getGrafanaUserResponse(Map<String, String> headers) {
		String loginApiUrl = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint() + "/api/user";
		ClientResponse grafanaCurrentOrgResponse = RestHandler.doGet(loginApiUrl, null, headers);
		JsonObject responseJson = new JsonParser().parse(grafanaCurrentOrgResponse.getEntity(String.class))
				.getAsJsonObject();
		return responseJson;

	}

	private List<NewCookie> getValidGrafanaSession(String userName, String password) {
		JsonObject loginRequestParams = new JsonObject();
		loginRequestParams.addProperty("user", userName);
		loginRequestParams.addProperty("password", password);
		String loginApiUrl = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint() + "/login";
		ClientResponse grafanaLoginResponse = RestHandler.doPost(loginApiUrl, loginRequestParams, null);
		return grafanaLoginResponse.getCookies();
	}
}
