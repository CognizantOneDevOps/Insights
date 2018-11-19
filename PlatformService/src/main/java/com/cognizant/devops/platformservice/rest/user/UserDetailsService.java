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

import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.NewCookie;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.LDAPAttributes;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.constants.ErrorMessage;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformcommons.dal.rest.RestHandler;
import com.cognizant.devops.platformdal.user.UserPortfolioDAL;
import com.cognizant.devops.platformdal.user.UserPortfolioEnum;
import com.cognizant.devops.platformservice.core.ServiceResponse;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.ClientResponse;

@RestController
@RequestMapping("/user")
public class UserDetailsService {
	private static Logger log = LogManager.getLogger(UserDetailsService.class.getName());
	// private static final String[] ldapAttributeIds =
	// {"sAMAccountName","distinguishedName", "sn", "givenname", "mail",
	// "telephonenumber", "thumbnailPhoto", "title"};

	@Autowired
	private DefaultSpringSecurityContextSource contextSource;

	/**
	 * Given the query param, search on the LDAP and return the valid user
	 * details.
	 * 
	 * @param query
	 * @return
	 */
	@RequestMapping(value = "/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public String searchUser(@RequestParam String query) {
		if (ApplicationConfigProvider.getInstance().isDisableAuth()) {
			return "{\"error\": \"LDAP authentication is disabled\"}";
		}
		if (query == null || query.trim().length() == 0) {
			return "{\"error\": \"Search parameter is missing\"}";
		}
		LDAPAttributes ldapAttributes = ApplicationConfigProvider.getInstance().getLdapConfiguration()
				.getLdapAttributes();
		String[] ldapAttributeIds = ldapAttributes.getAttributeList();
		ServiceResponse response = new ServiceResponse();
		DirContextOperations userDataFromLdap = null;
		FilterBasedLdapUserSearch userSearch = null;
		if (query.contains("@")) {
			String searchFiler = ldapAttributes.getEmail();
			userSearch = new FilterBasedLdapUserSearch("", "(" + searchFiler + "={0})", contextSource);
		} else {
			String searchFiler = ldapAttributes.getUsername();
			userSearch = new FilterBasedLdapUserSearch("", "(" + searchFiler + "={0})", contextSource);
		}
		userSearch.setSearchSubtree(true);
		userSearch.setReturningAttributes(ldapAttributeIds);
		try {
			userDataFromLdap = userSearch.searchForUser(query);
		} catch (Exception e) {
			return "{\"error\": \"User not found in LDAP\"}";
		}
		if (userDataFromLdap != null) {
			UserData user = new UserData();
			/*
			 * byte[] buf =
			 * (byte[])userDataFromLdap.getObjectAttribute("thumbnailPhoto");
			 * if(buf != null){ String imageString = "data:image/jpeg;base64," +
			 * DatatypeConverter.printBase64Binary(buf);
			 * user.setProfileImage(imageString); }
			 */
			/*
			 * user.setEmployeeId(extractAttributeValue(userDataFromLdap,
			 * "sAMAccountName"));
			 * user.setDistinguishedName(userDataFromLdap.getDn().toString());
			 * user.setGivenName(extractAttributeValue(userDataFromLdap,
			 * "givenname"));
			 * user.setFamilyName(extractAttributeValue(userDataFromLdap,
			 * "sn"));
			 * user.setEmailAddress(extractAttributeValue(userDataFromLdap,
			 * "mail"));
			 * user.setContactNumber(extractAttributeValue(userDataFromLdap,
			 * "telephonenumber"));
			 * user.setTitle(extractAttributeValue(userDataFromLdap, "title"));
			 */

			user.setEmployeeId(extractAttributeValue(userDataFromLdap, ldapAttributes.getUsername()));
			// user.setDistinguishedName(userDataFromLdap.getDn().toString());
			user.setGivenName(extractAttributeValue(userDataFromLdap, ldapAttributes.getName()));
			user.setFamilyName(extractAttributeValue(userDataFromLdap, ldapAttributes.getSurname()));
			user.setEmailAddress(extractAttributeValue(userDataFromLdap, ldapAttributes.getEmail()));
			// user.setContactNumber(extractAttributeValue(userDataFromLdap,
			// "telephonenumber"));
			// user.setTitle(extractAttributeValue(userDataFromLdap, "title"));
			response.setStatus(ConfigOptions.SUCCESS_RESPONSE);
			response.setData(user);
		}
		return new GsonBuilder().disableHtmlEscaping().create().toJson(response);
	}

	private String extractAttributeValue(DirContextOperations ldapUserData, String attribute) {
		Object object = ldapUserData.getObjectAttribute(attribute);
		if (object != null) {
			return object.toString();
		} else {
			return null;
		}
	}

	/**
	 * Given the query param, search on the LDAP and return the valid user
	 * details.
	 * 
	 * @param query
	 * @return
	 */
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

	@RequestMapping(value = "/updateRole", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonElement updateRole(@RequestParam String userId, @RequestParam String roles) {
		if (StringUtils.isEmpty(userId)) {
			return PlatformServiceUtil.buildFailureResponse(ErrorMessage.USERNAME_MISSING);
		}
		if (StringUtils.isEmpty(roles)) {
			return PlatformServiceUtil.buildFailureResponse(ErrorMessage.ROLES_MISSING);
		}
		List<String> roleList = Arrays.asList(roles.split(","));
		JsonArray rolesJson = new JsonArray();
		for (String role : roleList) {
			rolesJson.add(role);
		}
		StringBuffer query = new StringBuffer();
		// MERGE (n:USER { id: '146414'}) SET n.role='["ADMIN","USER"]' return n
		query.append("MERGE (n:USER { id: '").append(userId).append("'}) SET  n.role='").append(rolesJson.toString())
				.append("' return n");
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		GraphResponse executeCypherQuery;
		try {
			executeCypherQuery = dbHandler.executeCypherQuery(query.toString());
			if (executeCypherQuery.getNodes().size() > 0) {
				return PlatformServiceUtil.buildSuccessResponse();
			}
		} catch (GraphDBException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(ErrorMessage.DB_INSERTION_FAILED);
		}
		return PlatformServiceUtil.buildFailureResponse(ErrorMessage.UNEXPECTED_ERROR);
	}

	@RequestMapping(value = "/remove", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonElement removeUser(@RequestParam String userId) {
		if (StringUtils.isEmpty(userId)) {
			return PlatformServiceUtil.buildFailureResponse(ErrorMessage.USERNAME_MISSING);
		}
		StringBuffer query = new StringBuffer();
		query.append("MATCH (n:USER { id: '").append(userId).append("'}) DETACH DELETE n");
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		try {
			dbHandler.executeCypherQuery(query.toString());
			return PlatformServiceUtil.buildSuccessResponse();
		} catch (GraphDBException e) {
			log.error(e);
		}
		return PlatformServiceUtil.buildFailureResponse(ErrorMessage.UNEXPECTED_ERROR);
	}

	@RequestMapping(value = "/addUserPortfolio", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject addUserPortfolio(@RequestParam int orgId, @RequestParam int userId,
			@RequestParam String portfolio) {
		UserPortfolioDAL userPortfolioDAL = new UserPortfolioDAL();
		boolean result = userPortfolioDAL.addUserPortfolio(orgId, userId, UserPortfolioEnum.valueOf(portfolio));
		if (result) {
			return PlatformServiceUtil.buildSuccessResponse();
		} else {
			return PlatformServiceUtil.buildFailureResponse("Unable to create portfolio");
		}
	}

	@RequestMapping(value = "/getUserPortfolio", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject getUserPortfolio(@RequestParam int userId) {
		UserPortfolioDAL userPortfolioDAL = new UserPortfolioDAL();
		return PlatformServiceUtil.buildSuccessResponseWithData(userPortfolioDAL.getUserPortfolio(userId));
	}

	@RequestMapping(value = "/getCurrentOrgAndRole", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject getCurrentOrgAndRole(HttpServletRequest httpRequest) {
		String authHeader = httpRequest.getHeader("Authorization");
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
			//String grafanaCurrentOrg = getGrafanaCurrentOrg(headers);
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
