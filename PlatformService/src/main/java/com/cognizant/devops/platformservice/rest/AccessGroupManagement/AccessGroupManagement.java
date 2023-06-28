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
package com.cognizant.devops.platformservice.rest.AccessGroupManagement;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.dal.RestApiHandler;
import com.cognizant.devops.platformcommons.dal.grafana.GrafanaHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaOrgToken;
import com.cognizant.devops.platformservice.rest.es.models.DashboardResponse;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.cognizant.devops.platformservice.security.config.AuthenticationUtils;
import com.cognizant.devops.platformservice.security.config.InsightsAuthenticationTokenUtils;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import jakarta.ws.rs.core.NewCookie;

@RestController
@RequestMapping("/accessGrpMgmt")
public class AccessGroupManagement {
	private static Logger log = LogManager.getLogger(AccessGroupManagement.class);

	@Autowired
	private HttpServletRequest httpRequest;

	GrafanaHandler grafanaHandler = new GrafanaHandler();
	@Autowired
	AccessGroupManagementServiceImpl accessGrpMgmtServiceImpl;

	private static final String PATH = "/api/users/lookup?loginOrEmail=";
	private static final String USERDETAIL = "/api/users/search?&query=";
	private static final String COLON = ":";
	private static final String AUTHORIZATION = "Authorization";
	private static final String BASIC = "Basic ";
	
	@GetMapping(value = "/getOrgs", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject getOrgs() throws InsightsCustomException {
		log.debug("%n%nInside getOrgs method call");
		Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
		String response = grafanaHandler.grafanaGet("/api/orgs", headers);
		return PlatformServiceUtil.buildSuccessResponseWithData(JsonUtils.parseString(response));
	}

	@PostMapping(value = "/switchUserOrg", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject switchUserOrg(@RequestParam int orgId) throws InsightsCustomException {
		log.debug("Inside switchUserOrg method call and the request Org ID is: {}", orgId);
		JsonObject json = new JsonObject();
		Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
		String response = grafanaHandler.grafanaPost("/api/user/using/" + orgId, json, headers);
		log.debug("Headers: {}", headers);
		/*
		 * Since Access group has changed, need to check and update user role to new
		 * Access group Update cookies and SpringAuthorities accordingly
		 */

		Map<String, String> grafanaResponseCookies = new HashMap<>();
		String grafanaCurrentOrg = getGrafanaCurrentOrg(headers);
		grafanaResponseCookies.put("grafanaOrg", grafanaCurrentOrg);
		String grafanaCurrentOrgRole = getCurrentOrgRole(headers, grafanaCurrentOrg);
		grafanaResponseCookies.put("grafanaRole", grafanaCurrentOrgRole);

		grafanaResponseCookies.put(AuthenticationUtils.GRAFANA_WEBAUTH_USERKEY,
				httpRequest.getHeader(AuthenticationUtils.GRAFANA_WEBAUTH_HEADER_KEY));

		InsightsAuthenticationTokenUtils authenticationProviderImpl = new InsightsAuthenticationTokenUtils();
		String jtoken = authenticationProviderImpl.updateSecurityContextRoleBased(grafanaCurrentOrgRole);

		JsonObject responseJson = JsonUtils.parseStringAsJsonObject(response);
		responseJson.addProperty("jtoken", jtoken);

		return PlatformServiceUtil.buildSuccessResponseWithData(responseJson);
	}

	@PostMapping(value = "/searchUser", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject searchUser(@RequestBody String reqname) {
		try {
			String name = ValidationUtils.validateRequestBody(reqname);

			Map<String, String> grafanaHeader = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);

			String message = null;
			String responsename = grafanaHandler.grafanaGet(PATH + name, grafanaHeader);
			JsonObject jsonResponseName = JsonUtils.parseStringAsJsonObject(responsename);
			if (jsonResponseName.has("id")) {
				int userId = jsonResponseName.get("id").getAsInt();
				String apiUrl = "/api/users/" + userId + "/orgs";
				String response = grafanaHandler.grafanaGet(apiUrl, grafanaHeader);
				return PlatformServiceUtil.buildSuccessResponseWithData(JsonUtils.parseString(response));
			} else {
				message = "User Not Found";
				return PlatformServiceUtil.buildSuccessResponseWithData(message);
			}
		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}

	@PostMapping(value = "/assignUser", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject assignUser(@RequestBody String reqassignUserdata) {
		String message = " ";
		try {

			String assignUserdata = ValidationUtils.validateRequestBody(reqassignUserdata);
			JsonElement updateAgentJson = JsonUtils.parseString(assignUserdata);
			Map<String, String> grafanaHeader = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
			if (updateAgentJson.isJsonArray()) {
				JsonArray arrayOfOrg = updateAgentJson.getAsJsonArray();
				int size = arrayOfOrg.size();
				for (int i = 0; i < size; i++) {
					JsonElement aorg = arrayOfOrg.get(i);

					int orgId = aorg.getAsJsonObject().get(PlatformServiceConstants.ORGID).getAsInt();
					String userName = aorg.getAsJsonObject().get("userName").getAsString();
					String orgName = aorg.getAsJsonObject().get("orgName").getAsString();
					String role = aorg.getAsJsonObject().get("roleName").getAsString();
					String responsename = grafanaHandler.grafanaGet(PATH + userName, grafanaHeader);
					JsonObject jsonResponseName = JsonUtils.parseStringAsJsonObject(responsename);
					if (jsonResponseName.get("id") == null) {
						message = "User does not exsist.";
						return PlatformServiceUtil.buildFailureResponse(message);
					} else {

						String apiUrlorg = PlatformServiceConstants.API_ORGS + orgId + PlatformServiceConstants.USERS;
						JsonObject requestOrg = new JsonObject();
						requestOrg.addProperty(PlatformServiceConstants.LOGIN_OR_EMAIL, userName);
						requestOrg.addProperty("role", role);
						String responseorg = grafanaHandler.grafanaPost(apiUrlorg, requestOrg, grafanaHeader);
						message = message + "Org" + ": " + orgName + " " + responseorg;
					}
				}
			} else {
				throw new InsightsCustomException("Request parameter is not valid.");
			}
			return PlatformServiceUtil.buildSuccessResponseWithData(message);
		}  catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}

	@GetMapping(value = "/getCurrentUserOrgs", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject getCurrentUserOrgs() throws InsightsCustomException {
		Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
		String response = grafanaHandler.grafanaGet(PlatformServiceConstants.API_USER_ORGS, headers);
		log.debug("getCurrentUserOrgs response {} ", response);
		return PlatformServiceUtil.buildSuccessResponseWithData(JsonUtils.parseString(response));
	}

	@GetMapping(value = "/getUser", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject getUser() throws InsightsCustomException {
		try {
			Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
			log.debug("Headers in get User {}", headers);
			String response = grafanaHandler.grafanaGet(PlatformServiceConstants.API_USER, headers);
			return PlatformServiceUtil.buildSuccessResponseWithData(JsonUtils.parseString(response));
		} catch (Exception e) {
			log.error(" Error in getUser API ");
			throw new InsightsCustomException(
					PlatformServiceUtil.buildFailureResponseWithStatusCode(e.getMessage(), "502").toString());
		}
	}

	@GetMapping(value = "/getCurrentUserWithOrgs", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject getCurrentUserWithOrgs() throws InsightsCustomException {
		JsonObject responseJson = new JsonObject();
		Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
		log.debug("Headers in get User {}", headers);
		String responseUser = grafanaHandler.grafanaGet(PlatformServiceConstants.API_USER, headers);
		JsonObject jsonObject = JsonUtils.parseStringAsJsonObject(responseUser);
		responseJson.add("userDetail", (jsonObject));
		String responseUserOrg = grafanaHandler.grafanaGet(PlatformServiceConstants.API_USER_ORGS, headers);
		responseJson.add("orgArray", JsonUtils.parseString(responseUserOrg));
		return PlatformServiceUtil.buildSuccessResponseWithData(responseJson);
	}

	@PostMapping(value = "/addUserInOrg", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject addUser(@RequestBody String requserPropertyList) {
		String message = null;
		try {
			String userPropertyList = ValidationUtils.validateRequestBody(requserPropertyList);
			JsonObject updateAgentJson = JsonUtils.parseStringAsJsonObject(userPropertyList);
			int orgId = updateAgentJson.get(PlatformServiceConstants.ORGID).getAsInt();
			String name = updateAgentJson.get("name").getAsString();
			String email = updateAgentJson.get(PlatformServiceConstants.EMAIL).getAsString();
			String userName = updateAgentJson.get("userName").getAsString();
			String role = updateAgentJson.get("role").getAsString();
			String password = ValidationUtils.getSealedObject(updateAgentJson.get("password").getAsString());
			String orgName = updateAgentJson.get("orgName").getAsString();

			Map<String, String> grafanaHeader = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
			log.debug(" orgName {}", orgName);

			String responsename = grafanaHandler.grafanaGet(PATH + userName, grafanaHeader);
			JsonObject jsonResponseName = JsonUtils.parseStringAsJsonObject(responsename);

			String jsonResponseNameEmail = "";
			if (jsonResponseName.get("id") != null) {
				jsonResponseNameEmail = jsonResponseName.get("email").getAsString();
			}
			/* checking whether user name exists */

			if (jsonResponseName.get("id") != null && jsonResponseNameEmail.equals(email)) {
				/*
				 * if the user exists then we are getting the list of orgs in which the user is
				 * already present
				 */
				String apiUrlUserOrgs = "/api/users/" + jsonResponseName.get("id").getAsInt() + "/orgs";
				Map<String, String> headersUserOrgs = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
				String responseUserOrgs = grafanaHandler.grafanaGet(apiUrlUserOrgs, headersUserOrgs);
				JsonArray userOrgs = JsonUtils.parseStringAsJsonArray(responseUserOrgs);
				boolean orgFlag = false;
				String orgCurrentRole = "";
				for (JsonElement totalOrgs : userOrgs) {
					JsonObject orgs = totalOrgs.getAsJsonObject();
					int responseOrgId = orgs.get(PlatformServiceConstants.ORGID).getAsInt();
					String responseOrgRole = orgs.get("role").getAsString();
					if (responseOrgId == orgId) {
						orgFlag = true;
						orgCurrentRole = responseOrgRole;
					}
				}
				// checking whether the user exists in the org we entered in UI

				return checkUserExistsInOrg(orgFlag, role, orgCurrentRole, orgId, email, grafanaHeader);

			} else if (jsonResponseName.get("id") != null && jsonResponseNameEmail.equals(email) != true) {
				message = "{\"message\":\"Username already exists\"}";
				return PlatformServiceUtil.buildSuccessResponseWithData(message);
			} else {
				// if the username is not present in grafana then we are checking for the email
				String responseEmail = grafanaHandler.grafanaGet(PATH + email, grafanaHeader);
				JsonObject jsonResponseEmail = JsonUtils.parseStringAsJsonObject(responseEmail);
				if (jsonResponseEmail.get("id") != null) {
					// if email id exists returning email exists
					message = "{\"message\":\"Email already exists\"}";
					return PlatformServiceUtil.buildSuccessResponseWithData(message);
				} else {
					// if email not exits then we are creating a new user
					JsonObject requestCreate = new JsonObject();
					requestCreate.addProperty("name", name);
					requestCreate.addProperty("login", userName);
					requestCreate.addProperty("email", email);
					requestCreate.addProperty("role", role);
					requestCreate.addProperty("password", ValidationUtils.getDeSealedObject(password));
					String responseCreate = grafanaHandler.grafanaPost("/api/admin/users", requestCreate,
							grafanaHeader);

					JsonObject jsonResponse = JsonUtils.parseStringAsJsonObject(responseCreate);

					return addUserIfEmailNotExist(jsonResponse, orgId, email, role, message, grafanaHeader);

				}
			}

		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
	}

	private JsonObject checkUserExistsInOrg(boolean orgFlag, String role, String orgCurrentRole, int orgId,
			String email, Map<String, String> grafanaHeader) throws InsightsCustomException {
		String message = "";
		if (orgFlag) {
			// if the user exists in the or we entered , then we check if it is in the same
			// role or not
			if (role.equals(orgCurrentRole)) {
				message = "{\"message\":\"User exists in currrent org with same role\"}";
				return PlatformServiceUtil.buildSuccessResponseWithData(message);
			} else {
				message = "{\"message\":\"User exists in currrent org with different role\"}";
				return PlatformServiceUtil.buildSuccessResponseWithData(message);
			}
		} else {
			// if the user is not exists in the org we entered, then we add it to the org
			String apiUrlorg = PlatformServiceConstants.API_ORGS + orgId + "/users";
			JsonObject requestOrg = new JsonObject();
			requestOrg.addProperty("loginOrEmail", email);
			requestOrg.addProperty("role", role);
			String responseorg = grafanaHandler.grafanaPost(apiUrlorg, requestOrg, grafanaHeader);
			message = responseorg;
			return PlatformServiceUtil.buildSuccessResponseWithData(message);
		}
	}

	private JsonObject addUserIfEmailNotExist(JsonObject jsonResponse, int orgId, String email, String role,
			String message, Map<String, String> grafanaHeader) throws InsightsCustomException {
		if (jsonResponse.get("id") != null && orgId != 1) {
			// if the org is other than main org we are adding the created user to the org
			String apiUrlorg = "/api/orgs/" + orgId + "/users";
			JsonObject requestOrg = new JsonObject();
			requestOrg.addProperty("loginOrEmail", email);
			requestOrg.addProperty("role", role);
			String responseOrg = grafanaHandler.grafanaPost(apiUrlorg, requestOrg, grafanaHeader);
			message = responseOrg;
			return PlatformServiceUtil.buildSuccessResponseWithData(message);
		} else if (jsonResponse.get("id") != null && orgId == 1 && role.equals("Viewer") != true) {
			// if the org is main org and the role is other than viewer we are adding the
			// role to the created user
			JsonObject createdUserId = jsonResponse.getAsJsonObject();
			int userIdRole = createdUserId.get("id").getAsInt();
			String apiUrlRole = "/api/orgs/" + orgId + "/users/" + userIdRole;
			JsonObject requestRole = new JsonObject();
			requestRole.addProperty("role", role);
			Map<String, String> headersRole = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
			String authString = ApplicationConfigProvider.getInstance().getGrafana().getAdminUserName() + COLON
					+ ApplicationConfigProvider.getInstance().getGrafana().getAdminUserPassword();
			String encodedString = Base64.getEncoder().encodeToString(authString.getBytes());
			headersRole.put(AUTHORIZATION, BASIC + encodedString);
			String responseRole = grafanaHandler.grafanaPatch(apiUrlRole, requestRole, headersRole);
			message = responseRole;
			return PlatformServiceUtil.buildSuccessResponseWithData(message);
		} else {
			// if the org is main org and the role is viewer then we are not doing anything
			message = jsonResponse.toString();
			return PlatformServiceUtil.buildSuccessResponseWithData(message);
		}
	}

	@GetMapping(value = "/getGrafanaVersion", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject getGrafanaVersion() {
		JsonObject grafanaVersionJson = new JsonObject();
		String grafanaVersion = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaVersion();
		if (grafanaVersion == null) {
			grafanaVersion = "4.6.2";
		}
		grafanaVersionJson.addProperty("version", grafanaVersion);
		return PlatformServiceUtil.buildSuccessResponseWithData(grafanaVersionJson);
	}

	private String getCurrentOrgRole(Map<String, String> headers, String grafanaCurrentOrg)
			throws InsightsCustomException {
		log.debug("Inside getCurrentOrgRole method call");
		String grafanaCurrentOrgResponse = grafanaHandler.grafanaGet("/api/user/orgs", headers);
		JsonArray grafanaOrgs = JsonUtils.parseStringAsJsonArray(grafanaCurrentOrgResponse);
		String grafanaCurrentOrgRole = null;
		for (JsonElement org : grafanaOrgs) {
			if (grafanaCurrentOrg.equals(org.getAsJsonObject().get("orgId").toString())) {
				grafanaCurrentOrgRole = org.getAsJsonObject().get("role").getAsString();
				break;
			}
		}
		log.debug("Current grafana org role: {} ", grafanaCurrentOrgRole);
		return grafanaCurrentOrgRole;
	}

	private String getGrafanaCurrentOrg(Map<String, String> headers) throws InsightsCustomException {
		log.debug("Inside getGrafanaCurrentOrg method call");
		String grafanaCurrentOrgResponse = grafanaHandler.grafanaGet("/api/user", headers);
		JsonObject responseJson = JsonUtils.parseString(ValidationUtils.validateResponseBody(grafanaCurrentOrgResponse))
				.getAsJsonObject();
		return responseJson.get("orgId").toString();
	}

	@GetMapping(value = "/getDashboardsFoldersDetail", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject loadDashboardDataGrafana() {
		Map<String, JsonArray> mapOfFolders = new HashMap<>();
		JsonObject finalJson = new JsonObject();
		try {
			Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
			String grafanaResponse = grafanaHandler.grafanaGet("/api/search", headers);
			JsonElement response = JsonUtils.parseString(grafanaResponse);
			JsonArray dashboardsJsonArray = response.getAsJsonArray();
			if (dashboardsJsonArray.size() == 0) {
				return PlatformServiceUtil.buildSuccessResponseWithData(finalJson);
			}
			String grafanaBaseUrl = "INSIGHTS_GRAFANA_HOST";
			String grafanaUrl = grafanaBaseUrl;
			String grafanaIframeUrl = grafanaBaseUrl + "/dashboard/script/iSight_ui3.js?url=";
			JsonArray starrreddashboardsArray = new JsonArray();
			JsonArray generalDashboardArray = new JsonArray();
			for (JsonElement data : dashboardsJsonArray) {
				JsonObject dashboardData = data.getAsJsonObject();
				JsonObject datamodel = new JsonObject();
				datamodel.addProperty("uid", dashboardData.get("uid").getAsString());
				datamodel.addProperty(PlatformServiceConstants.TITLE,
						dashboardData.get(PlatformServiceConstants.TITLE).getAsString());
				datamodel.addProperty("id", dashboardData.get("id").getAsInt());
				if ("dash-db".equals(dashboardData.get("type").getAsString())) {
					String dashboardUrlStr = dashboardData.get("url").getAsString();
					dashboardUrlStr = dashboardUrlStr.substring(dashboardUrlStr.indexOf("/d"));
					datamodel.addProperty("url", (grafanaIframeUrl + grafanaUrl + dashboardUrlStr));
					if (dashboardData.get("isStarred").getAsBoolean()) {
						starrreddashboardsArray.add(datamodel);
					}
					getGeneralDashboardArray(dashboardData, mapOfFolders, datamodel, generalDashboardArray);
				}
			}
			finalJson.add("starred", starrreddashboardsArray);
			finalJson.add("general", generalDashboardArray);
			for (Map.Entry<String, JsonArray> header : mapOfFolders.entrySet()) {
				finalJson.add(header.getKey(), header.getValue());
			}
			return PlatformServiceUtil.buildSuccessResponseWithData(finalJson);
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}

	private JsonArray getGeneralDashboardArray(JsonObject dashboardData, Map<String, JsonArray> mapOfFolders,
			JsonObject datamodel, JsonArray generalDashboardArray) {
		if (dashboardData.has("folderTitle")) {
			String key = dashboardData.get("folderTitle").getAsString();
			log.debug(key);
			if (mapOfFolders.containsKey(key)) {
				JsonArray folderlist = mapOfFolders.get(key);
				folderlist.add(datamodel);
			} else {
				JsonArray folderArray = new JsonArray();
				folderArray.add(datamodel);
				mapOfFolders.put(key, folderArray);
			}
		} else {
			generalDashboardArray.add(datamodel);
		}
		return generalDashboardArray;
	}

	@GetMapping(value = "/dashboards", produces = MediaType.APPLICATION_JSON_VALUE)
	public String loadDashboardData() {
		DashboardResponse dashboardResponse = accessGrpMgmtServiceImpl.loadGrafanaDashboardData();
		return new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(dashboardResponse);
	}

	@PostMapping(value = "/getTemplateQueryResults", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject getTemplateQueryResults(@RequestBody String queryJson) throws InsightsCustomException {
		log.debug("%n%nInside getTemplateQueryResults method call  ==== ");
		JsonObject query = JsonUtils.parseStringAsJsonObject(queryJson);
		GraphDBHandler dbHandler = new GraphDBHandler();
		return dbHandler.executeCypherQueryForJsonResponse(query.get("query").getAsString());
	}

	@GetMapping(value = "/getDashboardByUid", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject getDashboardByUid(@RequestParam String uuid, @RequestParam int orgId)
			throws InsightsCustomException {
		log.debug("%n%nInside getDashboardByUid method call changed");// getGrafanaDashboardByAPIUid
		Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
		headers.put("x-grafana-org-id", String.valueOf(orgId));
		String response = grafanaHandler.grafanaGet("/api/dashboards/uid/" + uuid, headers);
		return PlatformServiceUtil.buildSuccessResponseWithHtmlData(JsonUtils.parseString(response));
	}

	@GetMapping(value = "/getDashboardByDBUid", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject getDashboardByDBUid(@RequestParam String uuid, @RequestParam int orgId) {
		log.debug("%n%nInside getDashboardByDBUid method call");// getDashboardByUid
		JsonObject responseDashbaoard = accessGrpMgmtServiceImpl.getDashboardByUid(uuid, orgId);
		return PlatformServiceUtil.buildSuccessResponseWithData(responseDashbaoard);
	}

	@PostMapping(value = "/getDashboardByOrg", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject getDashboardByOrg(@RequestParam int orgId) {
		log.debug("%n%nInside getDashboardByOrg method call");
		List<JsonObject> responseDashbaoardList = accessGrpMgmtServiceImpl.getDashboardByOrg(orgId);
		return PlatformServiceUtil.buildSuccessResponseWithData(responseDashbaoardList);
	}
}