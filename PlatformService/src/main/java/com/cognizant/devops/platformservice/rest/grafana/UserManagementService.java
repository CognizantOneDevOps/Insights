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
package com.cognizant.devops.platformservice.rest.grafana;

import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.core.util.AES256Cryptor;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.dal.grafana.GrafanaHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

@RestController
@RequestMapping("/admin/userMgmt")
public class UserManagementService {
	private static Logger log = LogManager.getLogger(UserManagementService.class.getName());

	@Autowired
	private HttpServletRequest httpRequest;
	
	GrafanaHandler grafanaHandler = new GrafanaHandler();
	private static final String PATH = "/api/orgs/"; 
	private static final String USERS = "/users"; 
	private static final String USER_PREFERENCE = "/api/user/preferences";
	private static final String FORWARD_SLASH = "/";
	private static final String ORG_ID = "orgId";
	private static final String NAME = "name";
	private static final String LOGIN = "login";
	private static final String ROLE = "role";
	private static final String THEME = "theme";
	private static final String USER_ID = "userId";
	private static final String EMAIL = "email";
	private static final String COLON = ":";
	private static final String AUTHORIZATION = "Authorization";
	private static final String BASIC = "Basic ";
	
	@PostMapping(value = "/getOrgUsers", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject getOrgUsers(@RequestParam int orgId) throws InsightsCustomException {
		try {
			Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
			String response = grafanaHandler.grafanaGet(PATH + orgId + USERS, headers);
			return PlatformServiceUtil
					.buildSuccessResponseWithData(JsonUtils.parseString(response));
		} catch (JsonSyntaxException e) {
			return PlatformServiceUtil.buildFailureResponse("Unable to get current org users");
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse("Unable to get current org users,Permission denide ");
		}
	}
	
	@PostMapping(value = "/v2/getOrgUsers", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject getOrgUsersV2(@RequestParam int orgId) throws InsightsCustomException {
		try {
			Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
			String response = grafanaHandler.grafanaGet(PATH + orgId + USERS, headers);
			JsonArray respJson = new Gson().fromJson(response, JsonArray.class);
			JsonArray restrictedArray = new JsonArray();
			for (JsonElement jsonElement : respJson) {
				JsonObject restrictedObject = new JsonObject();
				JsonObject inpJson = jsonElement.getAsJsonObject();
				restrictedObject.add(ORG_ID, inpJson.get(ORG_ID));
				restrictedObject.add(NAME, inpJson.get(NAME));
				restrictedObject.add(LOGIN, inpJson.get(LOGIN));
				restrictedObject.add(ROLE, inpJson.get(ROLE));
				restrictedObject.add(USER_ID, inpJson.get(USER_ID));
				restrictedObject.add(EMAIL, inpJson.get(EMAIL));
				restrictedArray.add(restrictedObject);
			}
			String passKey = UUID.randomUUID().toString().substring(0, 15);
		    String encodedData = passKey+AES256Cryptor.encrypt(restrictedArray.toString(), passKey);
			return PlatformServiceUtil
					.buildSuccessResponseWithData(encodedData);
		} catch (JsonSyntaxException e) {
			return PlatformServiceUtil.buildFailureResponse("Unable to get current org users");
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse("Unable to get current org users,Permission denide ");
		}
	}

	@PostMapping(value = "/createOrg", produces = MediaType.APPLICATION_JSON_VALUE)
	public String createOrg(@RequestParam String orgName) throws InsightsCustomException {
		JsonObject request = new JsonObject();
		request.addProperty(NAME, orgName);
		Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
		return grafanaHandler.grafanaPost(PATH, request, headers);
	}

	@PostMapping(value = "/editOrganizationUser", produces = MediaType.APPLICATION_JSON_VALUE)
	public String editOrganizationUser(@RequestParam int orgId, @RequestParam int userId, @RequestParam String role) throws InsightsCustomException {
		log.debug("%n%nInside editOrganizationUser method call");
		JsonObject request = new JsonObject();
		request.addProperty(ROLE, role);
		Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
		String authString = ApplicationConfigProvider.getInstance().getGrafana().getAdminUserName() + COLON
				+ ApplicationConfigProvider.getInstance().getGrafana().getAdminUserPassword();
		String encodedString = Base64.getEncoder().encodeToString(authString.getBytes());
		headers.put(AUTHORIZATION, BASIC + encodedString);
		return grafanaHandler.grafanaPatch(PATH + orgId + USERS + FORWARD_SLASH + userId, request, headers);
	}

	@PostMapping(value = "/deleteOrganizationUser", produces = MediaType.APPLICATION_JSON_VALUE)
	public String deleteOrganizationUser(@RequestParam int orgId, @RequestParam int userId, @RequestParam String role) throws InsightsCustomException {
		log.debug("%n%nInside deleteOrganizationUser method call");
		Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
		String authString = ApplicationConfigProvider.getInstance().getGrafana().getAdminUserName() + COLON
				+ ApplicationConfigProvider.getInstance().getGrafana().getAdminUserPassword();
		String encodedString = Base64.getEncoder().encodeToString(authString.getBytes());
		headers.put(AUTHORIZATION, BASIC + encodedString);
		return grafanaHandler.grafanaDelete(PATH + orgId + USERS + FORWARD_SLASH + userId, headers);
	}
	
	@GetMapping(value = "/getThemePreference", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject getThemePreference() {
		log.debug("Getting user preference");
		try {
			Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
			String response = grafanaHandler.grafanaGet(USER_PREFERENCE, headers);
			return PlatformServiceUtil
					.buildSuccessResponseWithData(JsonUtils.parseString(response));
		} catch (JsonSyntaxException e) {
			return PlatformServiceUtil.buildFailureResponse("Unable to get current org users");
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse("Unable to get current org users,Permission denide ");
		}
	}

	@PutMapping(value = "/updateThemePreference", produces = MediaType.APPLICATION_JSON_VALUE)
	public String updateThemePreference(@RequestParam String themePreference) throws InsightsCustomException {
		log.debug("Updating user preference details to Grafana with details {}", themePreference);
		JsonObject request = new JsonObject();
		request.addProperty(THEME, themePreference);
		Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
		return grafanaHandler.grafanaPut(USER_PREFERENCE, request, headers);
	}
}
