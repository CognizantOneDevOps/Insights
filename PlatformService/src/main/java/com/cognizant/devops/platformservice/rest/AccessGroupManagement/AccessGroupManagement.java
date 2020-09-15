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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.NewCookie;

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
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.dal.grafana.GrafanaHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.es.models.DashboardModel;
import com.cognizant.devops.platformservice.rest.es.models.DashboardResponse;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.cognizant.devops.platformservice.security.config.AuthenticationUtils;
import com.cognizant.devops.platformservice.security.config.InsightsAuthenticationTokenUtils;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RestController
@RequestMapping("/accessGrpMgmt")
public class AccessGroupManagement {
	private static Logger log = LogManager.getLogger(AccessGroupManagement.class);

	@Autowired
	private HttpServletRequest httpRequest;

	GrafanaHandler grafanaHandler = new GrafanaHandler();
	private static final String PATH = "/api/users/lookup?loginOrEmail=";
	private static final String USERDETAIL = "/api/users/search?&query=";

	@GetMapping(value = "/getOrgs", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject getOrgs() throws InsightsCustomException {
		log.debug("%n%nInside getOrgs method call");
		Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
		String response = grafanaHandler.grafanaGet("/api/orgs", headers);
		return PlatformServiceUtil.buildSuccessResponseWithData(new JsonParser().parse(response));
	}

	@PostMapping(value = "/switchUserOrg", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject switchUserOrg(@RequestParam int orgId) throws InsightsCustomException {
		log.debug("%n%nInside switchUserOrg method call, and the Org ID is: {}", orgId);
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
		httpRequest.setAttribute("responseHeaders", grafanaResponseCookies);

		InsightsAuthenticationTokenUtils authenticationProviderImpl = new InsightsAuthenticationTokenUtils();
		authenticationProviderImpl.updateSecurityContextRoleBased(grafanaCurrentOrgRole);

		return PlatformServiceUtil.buildSuccessResponseWithData(new JsonParser().parse(response));
	}

	@PostMapping(value = "/searchUser", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject searchUser(@RequestBody String reqname) {
		try {
			String name = ValidationUtils.validateRequestBody(reqname);

			Map<String, String> grafanaHeader = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);

			String message = null;
			String responsename = grafanaHandler.grafanaGet(PATH + name, grafanaHeader);
			JsonObject jsonResponseName = new JsonParser().parse(responsename).getAsJsonObject();
			if (jsonResponseName.has("id")) {
				int userId = jsonResponseName.get("id").getAsInt();
				String apiUrl = "/api/users/" + userId + "/orgs";
				String response = grafanaHandler.grafanaGet(apiUrl, grafanaHeader);
				return PlatformServiceUtil.buildSuccessResponseWithData(new JsonParser().parse(response));
			} else {
				message = "User Not Found";
				return PlatformServiceUtil.buildSuccessResponseWithData(message);
			}
		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}

	@Deprecated
	public String lastSeenOfUser(String name) {
		try {
			String lastSeen = "";
			GrafanaHandler grafanaHandler = new GrafanaHandler();
			String authString = ApplicationConfigProvider.getInstance().getGrafana().getAdminUserName() + ":"
					+ ApplicationConfigProvider.getInstance().getGrafana().getAdminUserPassword();
			String encodedString = Base64.getEncoder().encodeToString(authString.getBytes());
			Map<String, String> headers = new HashMap<>();
			headers.put("Authorization", "Basic " + encodedString);
			String response = grafanaHandler.grafanaGet(USERDETAIL + name, headers);
			if (response.isEmpty()) {
				lastSeen = "-";
			} else {
				JsonObject responseJson = new JsonParser().parse(response).getAsJsonObject();
				JsonArray userArray = responseJson.get("users").getAsJsonArray();
				for (JsonElement userArrayElement : userArray) {
					if (userArrayElement.getAsJsonObject().get("login").getAsString().equals(name)) {
						lastSeen = userArrayElement.getAsJsonObject().get("lastSeenAt").getAsString();
						break;
					} else {
						lastSeen = "-";
					}
				}
			}
			return lastSeen;
		} catch (Exception e) {
			log.error("Error while getting last seen of the user {} ", e.getMessage());
			return "-";
		}

	}

	@PostMapping(value = "/assignUser", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject assignUser(@RequestBody String reqassignUserdata) {
		String message = " ";
		try {

			String assignUserdata = ValidationUtils.validateRequestBody(reqassignUserdata);
			JsonParser parser = new JsonParser();
			JsonElement updateAgentJson = parser.parse(assignUserdata);
			Map<String, String> grafanaHeader = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
			if (updateAgentJson.isJsonArray()) {
				JsonArray arrayOfOrg = updateAgentJson.getAsJsonArray();
				int size = arrayOfOrg.size();
				// log.debug(size);
				for (int i = 0; i < size; i++) {
					JsonElement aorg = arrayOfOrg.get(i);
					// log.debug(aorg);

					int orgId = aorg.getAsJsonObject().get("orgId").getAsInt();
					String userName = aorg.getAsJsonObject().get("userName").getAsString();
					String orgName = aorg.getAsJsonObject().get("orgName").getAsString();
					String role = aorg.getAsJsonObject().get("roleName").getAsString();
					// log.debug(userName);
					String responsename = grafanaHandler.grafanaGet(PATH + userName, grafanaHeader);
					JsonObject jsonResponseName = new JsonParser().parse(responsename).getAsJsonObject();
					if (jsonResponseName.get("id") == null) {
						message = "User does not exsist.";
						return PlatformServiceUtil.buildFailureResponse(message);
					} else {

						String apiUrlorg = "/api/orgs/" + orgId + "/users";
						JsonObject requestOrg = new JsonObject();
						requestOrg.addProperty("loginOrEmail", userName);
						requestOrg.addProperty("role", role);
						String responseorg = grafanaHandler.grafanaPost(apiUrlorg, requestOrg, grafanaHeader);
						message = message + "Org" + ": " + orgName + " " + responseorg;
					}
				}
			}
			return PlatformServiceUtil.buildSuccessResponseWithData(message);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse("User already exsists in the Org.");
		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}

	@GetMapping(value = "/getCurrentUserOrgs", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject getCurrentUserOrgs() throws InsightsCustomException {
		Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
		String response = grafanaHandler.grafanaGet("/api/user/orgs", headers);
		// log.debug(" response " + response);
		return PlatformServiceUtil.buildSuccessResponseWithData(new JsonParser().parse(response));
	}

	@GetMapping(value = "/getUser", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject getUser() throws InsightsCustomException {
		try {
			Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);

			log.debug("Headers in get User", headers);
			String response = grafanaHandler.grafanaGet("/api/user", headers);
			List<NewCookie> cookieList = grafanaHandler.getGrafanaCookies("/api/user", null, headers);
			for (NewCookie cookie : cookieList) {
				String value = ValidationUtils.cleanXSS(cookie.getValue());
				log.debug("getUser cookies =================" + cookie.getName() + "   ====  " + value);
			}
			log.debug("Response in get User", cookieList);
			return PlatformServiceUtil.buildSuccessResponseWithData(new JsonParser().parse(response));
		} catch (Exception e) {
			log.error(" Error in getUser API ");
			throw new InsightsCustomException(
					PlatformServiceUtil.buildFailureResponseWithStatusCode(e.getMessage(), "502").toString());
		}
	}

	@GetMapping(value = "/getCurrentUserWithOrgs", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject getCurrentUserWithOrgs() throws InsightsCustomException {
		JsonObject responseJson = new JsonObject();
		JsonParser parser = new JsonParser();
		Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
		log.debug("Headers in get User {}", headers);
		String responseUser = grafanaHandler.grafanaGet("/api/user", headers);
		JsonObject jsonObject = parser.parse(responseUser).getAsJsonObject();		
		responseJson.add("userDetail", (jsonObject));
		String responseUserOrg = grafanaHandler.grafanaGet("/api/user/orgs", headers);
		responseJson.add("orgArray", parser.parse(responseUserOrg));
		return PlatformServiceUtil.buildSuccessResponseWithData(responseJson);
	}

	@PostMapping(value = "/addUserInOrg", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject addUser(@RequestBody String requserPropertyList) {
		String message = null;
		// log.debug("getOrgs API is: " + userPropertyList);
		try {
			String userPropertyList = ValidationUtils.validateRequestBody(requserPropertyList);
			JsonParser parser = new JsonParser();
			JsonObject updateAgentJson = (JsonObject) parser.parse(userPropertyList);
			int orgId = updateAgentJson.get("orgId").getAsInt();
			String name = updateAgentJson.get("name").getAsString();
			String email = updateAgentJson.get("email").getAsString();
			String userName = updateAgentJson.get("userName").getAsString();
			String role = updateAgentJson.get("role").getAsString();
			String password = ValidationUtils.getSealedObject(updateAgentJson.get("password").getAsString());
			String orgName = updateAgentJson.get("orgName").getAsString();

			Map<String, String> grafanaHeader = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
			log.debug(" orgName {}", orgName);

			String responsename = grafanaHandler.grafanaGet(PATH + userName, grafanaHeader);
			JsonObject jsonResponseName = new JsonParser().parse(responsename).getAsJsonObject();

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
				JsonArray userOrgs = new JsonParser().parse(responseUserOrgs).getAsJsonArray();
				boolean orgFlag = false;
				String orgCurrentRole = "";
				for (JsonElement totalOrgs : userOrgs) {
					JsonObject orgs = totalOrgs.getAsJsonObject();
					int responseOrgId = orgs.get("orgId").getAsInt();
					String responseOrgRole = orgs.get("role").getAsString();
					if (responseOrgId == orgId) {
						orgFlag = true;
						orgCurrentRole = responseOrgRole;
					}
				}
				// checking whether the user exists in the org we entered in UI
				if (orgFlag) {
					// if the user exists in the or we entered , then we check if it is in the same
					// role or not
					if (role.equals(orgCurrentRole)) {
						message = "{\"message\":\"User exists in currrent org with same role\"}";
						// message="User exists in currrent org with same role as "+orgCurrentRole;
						return PlatformServiceUtil.buildSuccessResponseWithData(message);
					} else {
						message = "{\"message\":\"User exists in currrent org with different role\"}";
						return PlatformServiceUtil.buildSuccessResponseWithData(message);
					}
				} else {
					// if the user is not exists in the org we entered, then we add it to the org
					String apiUrlorg = "/api/orgs/" + orgId + "/users";
					JsonObject requestOrg = new JsonObject();
					requestOrg.addProperty("loginOrEmail", email);
					requestOrg.addProperty("role", role);
					String responseorg = grafanaHandler.grafanaPost(apiUrlorg, requestOrg, grafanaHeader);
					message = responseorg;
					return PlatformServiceUtil.buildSuccessResponseWithData(message);
				}
			} else if (jsonResponseName.get("id") != null && jsonResponseNameEmail.equals(email) != true) {
				message = "{\"message\":\"Username already exists\"}";
				return PlatformServiceUtil.buildSuccessResponseWithData(message);
			} else {
				// if the username is not present in grafana then we are checking for the email
				String responseEmail = grafanaHandler.grafanaGet(PATH + email, grafanaHeader);
				JsonObject jsonResponseEmail = new JsonParser().parse(responseEmail).getAsJsonObject();
				// log.error("jsonResponseEmail--------------------"+jsonResponseEmail);
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

					JsonObject jsonResponse = new JsonParser().parse(responseCreate).getAsJsonObject();
					// log.error(jsonResponseCreate);
					if (jsonResponse.get("id") != null && orgId != 1) {
						// if the org is other than main org we are adding the created user to the org
						String apiUrlorg = "/api/orgs/" + orgId + "/users";
						JsonObject requestOrg = new JsonObject();
						requestOrg.addProperty("loginOrEmail", email);
						requestOrg.addProperty("role", role);

						String responseOrg = grafanaHandler.grafanaPost(apiUrlorg, requestOrg, grafanaHeader);
						message = responseOrg;
						// log.error(requestOrg+""+headersOrg+" "+responseOrg.getEntity(String.class));
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
						String responseRole = grafanaHandler.grafanaPatch(apiUrlRole, requestRole, headersRole);
						// log.error(responseRole);
						message = responseRole;
						return PlatformServiceUtil.buildSuccessResponseWithData(message);
					} else {
						// if the org is main org and the role is viewer then we are not doing anything
						message = jsonResponse.toString();
						// log.debug(PlatformServiceUtil.buildSuccessResponseWithData(message));
						return PlatformServiceUtil.buildSuccessResponseWithData(message);
					}
				}
			}

		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
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
		log.debug("\n\nInside getCurrentOrgRole method call");
		// log.debug("Headers: " + headers);
		String grafanaCurrentOrgResponse = grafanaHandler.grafanaGet("/api/user/orgs", headers);
		JsonArray grafanaOrgs = new JsonParser().parse(grafanaCurrentOrgResponse).getAsJsonArray();
		String grafanaCurrentOrgRole = null;
		for (JsonElement org : grafanaOrgs) {
			if (grafanaCurrentOrg.equals(org.getAsJsonObject().get("orgId").toString())) {
				grafanaCurrentOrgRole = org.getAsJsonObject().get("role").getAsString();
				break;
			}
		}
		log.debug("Current grafana org role: " + grafanaCurrentOrgRole + "\n\n");
		return grafanaCurrentOrgRole;
	}

	private String getGrafanaCurrentOrg(Map<String, String> headers) throws InsightsCustomException {
		log.debug("\n\nInside getGrafanaCurrentOrg method call");
		String grafanaCurrentOrgResponse = grafanaHandler.grafanaGet("/api/user", headers);
		JsonObject responseJson = new JsonParser().parse(grafanaCurrentOrgResponse).getAsJsonObject();
		String grafanaCurrentOrg = responseJson.get("orgId").toString();
		log.debug("The Current Grafana OrgId is: " + grafanaCurrentOrg + "\n\n");
		return grafanaCurrentOrg;
	}

	@GetMapping(value = "/getDashboardsFoldersDetail", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject loadDashboardDataGrafana() {
		Map<String, JsonArray> mapOfFolders = new HashMap<String, JsonArray>();
		JsonObject finalJson = new JsonObject();
		try {
			Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
			String grafanaResponse = grafanaHandler.grafanaGet("/api/search", headers);
			JsonElement response = new JsonParser().parse(grafanaResponse);
			JsonArray dashboardsJsonArray = response.getAsJsonArray();
			if (dashboardsJsonArray.size() == 0) {
				return PlatformServiceUtil.buildSuccessResponseWithData(finalJson);
			}
			String grafanaBaseUrl = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaExternalEndPoint();
			if (grafanaBaseUrl == null) {
				grafanaBaseUrl = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint();
			}
			String grafanaUrl = grafanaBaseUrl + "/dashboard/";
			String grafanaIframeUrl = grafanaBaseUrl + "/dashboard/script/iSight_ui3.js?url=";
			String grafanaDomainUrl = grafanaUrl(grafanaBaseUrl);
			String grafanaVersion = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaVersion();
			if (grafanaVersion == null) {
				grafanaVersion = "7.0.3";
			}
			JsonArray starrreddashboardsArray = new JsonArray();
			JsonArray generalDashboardArray = new JsonArray();
			for (JsonElement data : dashboardsJsonArray) {
				JsonObject dashboardData = data.getAsJsonObject();
				JsonObject datamodel = new JsonObject();
				datamodel.addProperty("title", dashboardData.get("title").getAsString());
				datamodel.addProperty("id", dashboardData.get("id").getAsInt());
				if ("dash-db".equals(dashboardData.get("type").getAsString())) {
					if (grafanaVersion.contains("5.")) {
						datamodel.addProperty("url",
								(grafanaIframeUrl + grafanaDomainUrl + dashboardData.get("url").getAsString()));
					} else {
						datamodel.addProperty("url",
								(grafanaIframeUrl + grafanaUrl + dashboardData.get("uri").getAsString()));
					}
					if (dashboardData.get("isStarred").getAsBoolean()) {
						starrreddashboardsArray.add(datamodel);
					}
					if (dashboardData.has("folderTitle")) {
						String key = dashboardData.get("folderTitle").getAsString();
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
				}
			}
			finalJson.add("starred", starrreddashboardsArray);
			finalJson.add("general", generalDashboardArray);
			for (Map.Entry<String, JsonArray> header : mapOfFolders.entrySet()) {
				finalJson.add(header.getKey().toString(), header.getValue());
			}
			return PlatformServiceUtil.buildSuccessResponseWithData(finalJson);
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}

	}

	@GetMapping(value = "/dashboards", produces = MediaType.APPLICATION_JSON_VALUE)
	public String loadDashboardData() {
		DashboardResponse dashboardResponse = new DashboardResponse();
		try {

			Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);

			String grafanaResponse = grafanaHandler.grafanaGet("/api/search", headers);
			JsonElement response = new JsonParser().parse(grafanaResponse);
			JsonArray dashboardsJsonArray = response.getAsJsonArray();
			String grafanaBaseUrl = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaExternalEndPoint();
			if (grafanaBaseUrl == null) {
				grafanaBaseUrl = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint();
			}
			String grafanaUrl = grafanaBaseUrl + "/dashboard/";
			String grafanaIframeUrl = grafanaBaseUrl + "/dashboard/script/iSight.js?url=";
			String grafanaDomainUrl = grafanaUrl(grafanaBaseUrl);
			String grafanaVersion = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaVersion();
			if (grafanaVersion == null) {
				grafanaVersion = "4.6.2";
			}
			for (JsonElement data : dashboardsJsonArray) {
				JsonObject dashboardData = data.getAsJsonObject();
				DashboardModel model = new DashboardModel();
				model.setId(dashboardData.get("title").getAsString());
				model.setTitle(dashboardData.get("title").getAsString());
				if (dashboardData.has("type")) {
					if ("dash-db".equals(dashboardData.get("type").getAsString())) {
						if (grafanaVersion.contains("5.")) {
							model.setUrl(grafanaIframeUrl + grafanaDomainUrl + dashboardData.get("url").getAsString());
						} else {
							model.setUrl(grafanaIframeUrl + grafanaUrl + dashboardData.get("uri").getAsString());
						}
						dashboardResponse.addDashboard(model);
					}
				}
			}

		} catch (Exception e) {
			log.error(e);
		}
		return new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(dashboardResponse);
	}

	private String grafanaUrl(String baseUrl) {
		String parsedUrl = null;
		try {
			URL uri = new URL(baseUrl);
			parsedUrl = uri.getProtocol() + "://" + uri.getHost();
			if (uri.getPort() > -1) {
				parsedUrl = parsedUrl + ":" + uri.getPort();
			}
		} catch (MalformedURLException e) {
			log.error("Error in Parsing Grafana URL", e);
		}
		return parsedUrl;
	}
}
