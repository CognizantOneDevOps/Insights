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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.NewCookie;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.providers.ExpiringUsernameAuthenticationToken;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.dal.rest.RestHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.es.models.DashboardModel;
import com.cognizant.devops.platformservice.rest.es.models.DashboardResponse;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.cognizant.devops.platformservice.security.config.AuthenticationUtils;
import com.cognizant.devops.platformservice.security.config.SpringAuthorityUtil;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.ClientResponse;

@RestController
@RequestMapping("/accessGrpMgmt")
public class AccessGroupManagement {
	private static Logger log = LogManager.getLogger(AccessGroupManagement.class);
	
	@Autowired
	private HttpServletRequest httpRequest;

	@RequestMapping(value = "/getOrgs", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject getOrgs() {
		log.debug("\n\nInside getOrgs method call");
		String apiUrl = PlatformServiceUtil.getGrafanaURL("/api/orgs");
		// log.debug("getOrgs API is: " + apiUrl);
		Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
		ClientResponse response = RestHandler.doGet(apiUrl, null, headers);
		return PlatformServiceUtil
				.buildSuccessResponseWithData(new JsonParser().parse(response.getEntity(String.class)));
	}

	@RequestMapping(value = "/switchUserOrg", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject switchUserOrg(@RequestParam int orgId) {
		log.debug("\n\nInside switchUserOrg method call, and the Org ID is: " + orgId);

		Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);

		String apiUrl = PlatformServiceUtil.getGrafanaURL("/api/user/using/"+ orgId);
		ClientResponse response = RestHandler.doPost(apiUrl, null, headers);
		log.debug("API URL is: " + apiUrl);
		log.debug("Headers: " + headers);

		/*
		 * Since Access group has changed, need to check and update user role to new
		 * Access group Update cookies and SpringAuthorities accordingly
		 */
		
		Map<String, String> grafanaResponseCookies = new HashMap<String, String>();
		String grafanaCurrentOrg = getGrafanaCurrentOrg(headers);
		grafanaResponseCookies.put("grafanaOrg", grafanaCurrentOrg);
		String grafanaCurrentOrgRole = getCurrentOrgRole(headers, grafanaCurrentOrg);
		grafanaResponseCookies.put("grafanaRole", grafanaCurrentOrgRole);

		grafanaResponseCookies.put(AuthenticationUtils.GRAFANA_WEBAUTH_USERKEY,
				httpRequest.getHeader(AuthenticationUtils.GRAFANA_WEBAUTH_HEADER_KEY));
		httpRequest.setAttribute("responseHeaders", grafanaResponseCookies);

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		List<GrantedAuthority> updatedAuthorities = new ArrayList<GrantedAuthority>();
		updatedAuthorities.add(SpringAuthorityUtil.getSpringAuthorityRole(grafanaCurrentOrgRole));
		if (ApplicationConfigProvider.getInstance().isEnableSSO()) {
			Object principal = auth.getPrincipal();
			Date expDate = new Date(System.currentTimeMillis() + 60 * 60 * 1000);
			ExpiringUsernameAuthenticationToken autharization = new ExpiringUsernameAuthenticationToken(expDate,
					principal, auth.getCredentials(), updatedAuthorities);
			SecurityContextHolder.getContext().setAuthentication(autharization);
		} else {
			Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(),
					updatedAuthorities);
			log.debug("Get Credentials output ", auth.getPrincipal());
			SecurityContextHolder.getContext().setAuthentication(newAuth);
		}

		return PlatformServiceUtil
				.buildSuccessResponseWithData(new JsonParser().parse(response.getEntity(String.class)));
	}

	@RequestMapping(value = "/searchUser", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject searchUser(@RequestBody String reqname) {
		try {
			String name = ValidationUtils.validateRequestBody(reqname);

			Map<String, String> grafanaHeader = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);

			String apiUrlName = PlatformServiceUtil.getGrafanaURL("/api/users/lookup?loginOrEmail=" + name);
			String message = null;
			ClientResponse responsename = callgrafana(apiUrlName, null,RequestMethod.GET.toString(),grafanaHeader);
			JsonObject jsonResponseName = new JsonParser().parse(responsename.getEntity(String.class))
					.getAsJsonObject();
			if (jsonResponseName.has("id")) {
				int userId = jsonResponseName.get("id").getAsInt();
				String apiUrl = PlatformServiceUtil.getGrafanaURL("/api/users/" + userId + "/orgs");
				ClientResponse response = callgrafana(apiUrl, null,RequestMethod.GET.toString(),grafanaHeader);
				return PlatformServiceUtil
						.buildSuccessResponseWithData(new JsonParser().parse(response.getEntity(String.class)));
			} else {
				message = "User Not Found";
				return PlatformServiceUtil.buildSuccessResponseWithData(message);
			}
		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}

	@RequestMapping(value = "/assignUser", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
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
					String apiUrlName = PlatformServiceUtil.getGrafanaURL("/api/users/lookup?loginOrEmail=" + userName);
					// log.debug(userName);
					ClientResponse responsename = callgrafana(apiUrlName, null, RequestMethod.GET.toString(),grafanaHeader);
					JsonObject jsonResponseName = new JsonParser().parse(responsename.getEntity(String.class))
							.getAsJsonObject();
					if (jsonResponseName.get("id") == null) {
						message = "User does not exsist.";
					} else {

						String apiUrlorg = PlatformServiceUtil.getGrafanaURL("/api/orgs/" + orgId + "/users");
						JsonObject requestOrg = new JsonObject();
						requestOrg.addProperty("loginOrEmail", userName);
						requestOrg.addProperty("role", role);
						ClientResponse responseorg = callgrafana(apiUrlorg, requestOrg, RequestMethod.POST.toString(),grafanaHeader);
						message = message + "Org" + ": " + orgName + " " + responseorg.getEntity(String.class);
					}
				}
			}
			return PlatformServiceUtil.buildSuccessResponseWithData(message);
		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
	}

	@RequestMapping(value = "/getCurrentUserOrgs", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject getCurrentUserOrgs() {
		Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
		String apiUrl = PlatformServiceUtil.getGrafanaURL("/api/user/orgs");
		ClientResponse response = RestHandler.doGet(apiUrl, null, headers);
		// log.debug(" response " + response);
		return PlatformServiceUtil
				.buildSuccessResponseWithData(new JsonParser().parse(response.getEntity(String.class)));
	}

	@RequestMapping(value = "/getUser", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject getUser() throws InsightsCustomException {
		try {
			String apiUrl = PlatformServiceUtil.getGrafanaURL("/api/user");
			Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);

			log.debug("Headers in get User", headers);
			ClientResponse response = RestHandler.doGet(apiUrl, null, headers);
			List<NewCookie> cookieList = response.getCookies();
			for (NewCookie cookie : cookieList) {
				String value = ValidationUtils.cleanXSS(cookie.getValue());
				log.debug("getUser cookies =================" + cookie.getName() + "   ====  " + value);
			}
			log.debug("Response in get User", response.getCookies());
			return PlatformServiceUtil
					.buildSuccessResponseWithData(new JsonParser().parse(response.getEntity(String.class)));
		} catch (Exception e) {
			log.error(" Error in getUser API ");
			throw new InsightsCustomException(
					PlatformServiceUtil.buildFailureResponseWithStatusCode(e.getMessage(), "502").toString());
		}
	}

	@RequestMapping(value = "/getCurrentUserWithOrgs", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject getCurrentUserWithOrgs() {

		JsonObject responseJson = new JsonObject();
		JsonParser parser = new JsonParser();

		String apiUserUrl =PlatformServiceUtil.getGrafanaURL("/api/user");
		Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);

		log.debug("Headers in get User", headers);
		ClientResponse responseUser = RestHandler.doGet(apiUserUrl, null, headers);

		responseJson.add("userDetail", parser.parse(responseUser.getEntity(String.class)));

		String apiOrgUrl = PlatformServiceUtil.getGrafanaURL("/api/user/orgs");
		ClientResponse responseUserOrg = RestHandler.doGet(apiOrgUrl, null, headers);

		responseJson.add("orgArray", parser.parse(responseUserOrg.getEntity(String.class)));
		
		return PlatformServiceUtil.buildSuccessResponseWithData(responseJson);
	}

	

	@RequestMapping(value = "/addUserInOrg", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
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
			String apiUrlName = PlatformServiceUtil.getGrafanaURL("/api/users/lookup?loginOrEmail=" + userName);
			
			log.debug(" orgName "+orgName);
			
			ClientResponse responsename = callgrafana(apiUrlName, null, RequestMethod.GET.toString(),grafanaHeader);
			JsonObject jsonResponseName = new JsonParser().parse(responsename.getEntity(String.class))
					.getAsJsonObject();

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
				String apiUrlUserOrgs = PlatformServiceUtil.getGrafanaURL("/api/users/" + jsonResponseName.get("id").getAsInt() + "/orgs");
				Map<String, String> headersUserOrgs = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
				ClientResponse responseUserOrgs = RestHandler.doGet(apiUrlUserOrgs, null, headersUserOrgs);
				JsonArray userOrgs = new JsonParser().parse(responseUserOrgs.getEntity(String.class)).getAsJsonArray();
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
					// if the user exists in the or we entered , then we check if it is in the same role or not
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
					String apiUrlorg =PlatformServiceUtil.getGrafanaURL("/api/orgs/" + orgId + "/users");
					JsonObject requestOrg = new JsonObject();
					requestOrg.addProperty("loginOrEmail", email);
					requestOrg.addProperty("role", role);
					ClientResponse responseorg = callgrafana(apiUrlorg, requestOrg, RequestMethod.POST.toString(),grafanaHeader);
					message = responseorg.getEntity(String.class);
					return PlatformServiceUtil.buildSuccessResponseWithData(message);
				}
			} else if (jsonResponseName.get("id") != null && jsonResponseNameEmail.equals(email) != true) {
				message = "{\"message\":\"Username already exists\"}";
				return PlatformServiceUtil.buildSuccessResponseWithData(message);
			} else {
				// if the username is not present in grafana then we are checking for the email
				String apiUrlEmail = PlatformServiceUtil.getGrafanaURL("/api/users/lookup?loginOrEmail=" + email);
				ClientResponse responseEmail = callgrafana(apiUrlEmail, null, RequestMethod.GET.toString(),grafanaHeader);
				JsonObject jsonResponseEmail = new JsonParser().parse(responseEmail.getEntity(String.class))
						.getAsJsonObject();
				// log.error("jsonResponseEmail--------------------"+jsonResponseEmail);
				if (jsonResponseEmail.get("id") != null) {
					// if email id exists returning email exists
					message = "{\"message\":\"Email already exists\"}";
					return PlatformServiceUtil.buildSuccessResponseWithData(message);
				} else {
					// if email not exits then we are creating a new user
					String apiUrlCreate = PlatformServiceUtil.getGrafanaURL("/api/admin/users");
					JsonObject requestCreate = new JsonObject();
					requestCreate.addProperty("name", name);
					requestCreate.addProperty("login", userName);
					requestCreate.addProperty("email", email);
					requestCreate.addProperty("role", role);
					requestCreate.addProperty("password", ValidationUtils.getDeSealedObject(password));
					ClientResponse responseCreate = callgrafana(apiUrlCreate, requestCreate, RequestMethod.POST.toString(),grafanaHeader);

					JsonObject jsonResponse = new JsonParser().parse(responseCreate.getEntity(String.class))
							.getAsJsonObject();
					// log.error(jsonResponseCreate);
					if (jsonResponse.get("id") != null && orgId != 1) {
						// if the org is other than main org we are adding the created user to the org
						String apiUrlorg = PlatformServiceUtil.getGrafanaURL("/api/orgs/" + orgId + "/users");
						JsonObject requestOrg = new JsonObject();
						requestOrg.addProperty("loginOrEmail", email);
						requestOrg.addProperty("role", role);

						ClientResponse responseOrg = callgrafana(apiUrlorg, requestOrg, RequestMethod.POST.toString(),grafanaHeader);
						message = responseOrg.getEntity(String.class);
						// log.error(requestOrg+""+headersOrg+" "+responseOrg.getEntity(String.class));
						return PlatformServiceUtil.buildSuccessResponseWithData(message);
					} else if (jsonResponse.get("id") != null && orgId == 1 && role.equals("Viewer") != true) {
						// if the org is main org and the role is other than viewer we are adding the
						// role to the created user
						JsonObject createdUserId = jsonResponse.getAsJsonObject();
						int userIdRole = createdUserId.get("id").getAsInt();

						String apiUrlRole = PlatformServiceUtil.getGrafanaURL("/api/orgs/" + orgId + "/users/" + userIdRole);
						JsonObject requestRole = new JsonObject();
						requestRole.addProperty("role", role);
						Map<String, String> headersRole = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
						ClientResponse responseRole = RestHandler.doPatch(apiUrlRole, requestRole, headersRole);
						// log.error(responseRole);
						message = responseRole.getEntity(String.class);
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

	public ClientResponse callgrafana(String url, JsonElement requestJson, String type,Map<String, String> headers ) {
		if (type == RequestMethod.GET.toString()) {
			ClientResponse responseName = RestHandler.doGet(url, null, headers);
			return responseName;
		} else if (type == RequestMethod.POST.toString()) {
			ClientResponse responseName = RestHandler.doPost(url, requestJson, headers);
			return responseName;
		}
		return null;
	}

	@RequestMapping(value = "/getGrafanaVersion", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject getGrafanaVersion() {
		JsonObject grafanaVersionJson = new JsonObject();
		String grafanaVersion = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaVersion();
		if (grafanaVersion == null) {
			grafanaVersion = "4.6.2";
		}
		grafanaVersionJson.addProperty("version", grafanaVersion);
		return PlatformServiceUtil.buildSuccessResponseWithData(grafanaVersionJson);
	}

	private String getCurrentOrgRole(Map<String, String> headers, String grafanaCurrentOrg) {
		log.debug("\n\nInside getCurrentOrgRole method call");
		String userOrgsApiUrl = PlatformServiceUtil.getGrafanaURL("/api/user/orgs");
		// log.debug("Headers: " + headers);
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
		log.debug("Current grafana org role: " + grafanaCurrentOrgRole + "\n\n");
		return grafanaCurrentOrgRole;
	}

	private String getGrafanaCurrentOrg(Map<String, String> headers) {
		log.debug("\n\nInside getGrafanaCurrentOrg method call");
		String loginApiUrl = PlatformServiceUtil.getGrafanaURL("/api/user");
		ClientResponse grafanaCurrentOrgResponse = RestHandler.doGet(loginApiUrl, null, headers);
		JsonObject responseJson = new JsonParser().parse(grafanaCurrentOrgResponse.getEntity(String.class))
				.getAsJsonObject();
		String grafanaCurrentOrg = responseJson.get("orgId").toString();
		log.debug("The Current Grafana OrgId is: " + grafanaCurrentOrg + "\n\n");
		return grafanaCurrentOrg;
	}


	@RequestMapping(value = "/dashboards", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String loadDashboardData() {
		DashboardResponse dashboardResponse = new DashboardResponse();
		try {
			
			Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
			
			String dashboardApiUrl = PlatformServiceUtil.getGrafanaURL("/api/search");
			ClientResponse grafanaResponse = RestHandler.doGet(dashboardApiUrl, null, headers);
			JsonElement response = new JsonParser().parse(grafanaResponse.getEntity(String.class));
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
