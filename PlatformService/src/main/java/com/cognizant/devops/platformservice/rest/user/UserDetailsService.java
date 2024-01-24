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

import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.saml2.provider.service.authentication.DefaultSaml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.dal.grafana.GrafanaHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.cognizant.devops.platformservice.security.config.AuthenticationUtils;
import com.cognizant.devops.platformservice.security.config.InsightsAuthenticationToken;
import com.cognizant.devops.platformservice.security.config.InsightsAuthenticationTokenUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nimbusds.jwt.JWTClaimsSet;

@RestController
@RequestMapping("/user")
public class UserDetailsService {

	private static Logger log = LogManager.getLogger(UserDetailsService.class.getName());

	@Autowired
	private HttpServletRequest httpRequest;

	GrafanaHandler grafanaHandler = new GrafanaHandler();

	/**
	 * used to authenticate Grafana User
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping(value = "/authenticate", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@SuppressWarnings("unchecked")
	public JsonObject authenticateUser(HttpServletRequest request) {
		log.debug("Inside authenticateUser ");
		SecurityContext context = SecurityContextHolder.getContext();
		InsightsAuthenticationToken authGrafana = (InsightsAuthenticationToken) context.getAuthentication();
		Map<String, String> responseHeadersgrafanaAttr = (Map<String, String>) authGrafana.getDetails();
		if (responseHeadersgrafanaAttr != null) {
			UserDetails userDetail = (UserDetails) authGrafana.getPrincipal();
			Map<String, Object> params = new HashMap<>();
			params.put(AuthenticationUtils.AUTHORITY,
					new ArrayList<>(authGrafana.getAuthorities()).get(0).getAuthority());
			params.put(AuthenticationUtils.GRAFANA_DETAIL, authGrafana.getDetails());
			String jToken = AuthenticationUtils.getToken(userDetail.getUsername(), AuthenticationUtils.SESSION_TIME,
					params);
			responseHeadersgrafanaAttr.put(AuthenticationUtils.JTOKEN, jToken);
			for (Map.Entry<String, String> entry : responseHeadersgrafanaAttr.entrySet()) {
				ValidationUtils.cleanXSS(entry.getValue());
			}
			return PlatformServiceUtil.buildSuccessResponseWithData(responseHeadersgrafanaAttr);
		} else {
			return PlatformServiceUtil
					.buildFailureResponse("Error in authenticate Grafana User,unable to retrive grafana cookies ");
		}
	}

	/**
	 * used to authenticate SSO SAML User and Redirect to UI application
	 * 
	 * @return
	 * @throws InsightsCustomException
	 */
	@GetMapping(value = "/insightsso/authenticateSSO", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<Object> authenticateSSOUser(@AuthenticationPrincipal Saml2AuthenticatedPrincipal principal)
			throws InsightsCustomException {
		log.debug("Inside authenticateSSOUser");
		
		HttpHeaders httpHeaders = new HttpHeaders();
		JsonObject jsonResponse = new JsonObject();
		try {

			String givenname = principal
					.getFirstAttribute("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname");
			String userid = principal.getFirstAttribute("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name");
			String userEmail = principal
					.getFirstAttribute("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress");

			jsonResponse.addProperty(AuthenticationUtils.GRAFANA_WEBAUTH_HTTP_REQUEST_HEADER, givenname);


			validateGrafanaDetail(jsonResponse, givenname, userid, userEmail);

			httpHeaders.add(AuthenticationUtils.GRAFANA_WEBAUTH_USERKEY, givenname);

			URI uri = new URI(ApplicationConfigProvider.getInstance().getSingleSignOnConfig().getRelayStateUrl());

			httpHeaders.setLocation(uri);
		}catch (Exception e) {
			log.error("Error in authenticate SSO User  ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error while login using sso, For detail Please check log file ");
		}

		return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);// success
	}

	
	/**
	 * used to get SAML user detail after login application
	 * 
	 * @return
	 */
	@GetMapping(value = "/insightsso/getUserDetail", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getSAMLUserDetail() {

		log.debug("Inside getUserDetail");

		JsonObject jsonResponse = new JsonObject();

		try {
			SecurityContext context = SecurityContextHolder.getContext();
			Saml2Authentication auth = (Saml2Authentication) context.getAuthentication();
			DefaultSaml2AuthenticatedPrincipal principleDefault = (DefaultSaml2AuthenticatedPrincipal) auth
					.getPrincipal();

			String givenname = principleDefault
					.getFirstAttribute("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname");
			String userid = principleDefault
					.getFirstAttribute("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name");
			String userEmail = principleDefault
					.getFirstAttribute("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress");


			String grafanaCurrentOrgRole = validateGrafanaDetail(jsonResponse, givenname, userid, userEmail);
			List<GrantedAuthority> updatedAuthorities = new ArrayList<>();
			updatedAuthorities.add(AuthenticationUtils.getSpringAuthorityRole(grafanaCurrentOrgRole));

			Date expDate = new Date(System.currentTimeMillis() + 60 * 60 * 1000);

			Map<String, Object> params = new HashMap<>();
			params.put(AuthenticationUtils.AUTHORITY, updatedAuthorities);
			params.put("tokenExpiration", expDate);

			String jToken = AuthenticationUtils.getToken(userid, AuthenticationUtils.SESSION_TIME, params);
			jsonResponse.addProperty(AuthenticationUtils.JTOKEN, jToken);

			InsightsAuthenticationToken samlJwtAuthenticationToken = new InsightsAuthenticationToken(jToken,
					auth.getPrincipal(), auth.getCredentials(), updatedAuthorities);

			SecurityContextHolder.getContext().setAuthentication(samlJwtAuthenticationToken);

		} catch (Exception e) {
			log.error("Error in SSO Cookie  ", e);
			return PlatformServiceUtil.buildFailureResponse("Error in SSO Cookie " + e);
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(jsonResponse);
	}

	/**
	 * @return
	 * @throws InsightsCustomException
	 */
	@GetMapping(value = "/insightsso/authenticateJWT", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<Object> authenticateUserUsingJWT() {

		log.debug("Inside authenticateUserUsingJWT ======= {}", httpRequest.getRequestURI());
		Map<String, String> headersGrafana = new HashMap<>();
		HttpHeaders httpHeaders = new HttpHeaders();
		JsonObject jsonResponse = new JsonObject();
		try {
			SecurityContext context = SecurityContextHolder.getContext();
			InsightsAuthenticationToken authJWT = (InsightsAuthenticationToken) context.getAuthentication();
			if (authJWT != null) {
				String userid = String.valueOf(authJWT.getDetails());

				headersGrafana.put(AuthenticationUtils.GRAFANA_WEBAUTH_USERKEY, userid);
				headersGrafana.put(AuthenticationUtils.GRAFANA_WEBAUTH_USERKEY_NAME, userid);
				headersGrafana.put(AuthenticationUtils.HEADER_COOKIES_KEY, "username=" + userid);
				jsonResponse.addProperty(AuthenticationUtils.SSO_USER_HEADER_KEY, userid);
				jsonResponse.addProperty(AuthenticationUtils.SSO_LOGOUT_URL,
						ApplicationConfigProvider.getInstance().getSingleSignOnConfig().getPostLogoutURL());

				jsonResponse.addProperty("tokenauth", String.valueOf(authJWT.getPrincipal()));
				URI uri = new URI(ApplicationConfigProvider.getInstance().getSingleSignOnConfig().getRelayStateUrl());
				jsonResponse.addProperty("urlRedirect",
						ApplicationConfigProvider.getInstance().getSingleSignOnConfig().getRelayStateUrl());
				httpHeaders.setLocation(uri);
			} else {
				log.error(" authenticateJWT is Empty, Please try again  ");
			}
		} catch (Exception e) {
			log.error("Error in authenticate JWT User  ", e);
			String msg = "Error while login using JWT, For detail Please check log file ";
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
		}

		return new ResponseEntity<>(jsonResponse, httpHeaders, HttpStatus.OK);// success
	}

	/**
	 * used to get getJWTUserDetail user detail
	 * 
	 * @return
	 * @throws InsightsCustomException
	 */
	@GetMapping(value = "/insightsso/getJWTUserDetail", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getJWTUserDetail() throws InsightsCustomException {

		log.debug("Inside authenticateJWT  getJWTUserDetail");
		JsonObject jsonResponse = new JsonObject();
		try {
			SecurityContext context = SecurityContextHolder.getContext();
			InsightsAuthenticationToken authJWT = (InsightsAuthenticationToken) context.getAuthentication();
			JWTClaimsSet detailData = (JWTClaimsSet) authJWT.getDetails();
			String userid = detailData.getSubject();

			jsonResponse.addProperty(AuthenticationUtils.GRAFANA_WEBAUTH_HTTP_REQUEST_HEADER, userid);
			String grafanaCurrentOrgRole = validateGrafanaDetail(jsonResponse, userid, userid, userid);

			List<GrantedAuthority> updatedAuthorities = new ArrayList<>();
			updatedAuthorities.add(AuthenticationUtils.getSpringAuthorityRole(grafanaCurrentOrgRole));

			Map<String, Object> params = new HashMap<>();
			params.put(AuthenticationUtils.AUTHORITY, grafanaCurrentOrgRole);
			params.put("OriginalToken", String.valueOf(authJWT.getPrincipal()));
			params.put("exp", detailData.getClaim("exp"));
			String jToken = AuthenticationUtils.getToken(userid, AuthenticationUtils.SESSION_TIME, params);

			InsightsAuthenticationToken jwtAuthenticationToken = new InsightsAuthenticationToken(authJWT.getPrincipal(),
					detailData, null, updatedAuthorities);

			jsonResponse.addProperty(AuthenticationUtils.JTOKEN, jToken);

			InsightsAuthenticationTokenUtils authenticationtokenUtils = new InsightsAuthenticationTokenUtils();
			authenticationtokenUtils.updateSecurityContext(jwtAuthenticationToken);

		} catch (Exception e) {
			log.error("Error in authenticate JWT token User..  ", e);
			return PlatformServiceUtil
					.buildFailureResponseWithStatusCode("Error while authenticating JWT token request  ", "811");
		}

		return PlatformServiceUtil.buildSuccessResponseWithData(jsonResponse);
	}

	/**
	 * Used to validate grafana detail for SSO
	 * 
	 * @param jsonResponse
	 * @param userid
	 * @return
	 * @throws InsightsCustomException
	 */
	public String validateGrafanaDetail(JsonObject jsonResponse, String name, String loginname, String email)
			throws InsightsCustomException {
		Map<String, String> headersGrafana = new HashMap<>();
		StringBuilder cookies = new StringBuilder();
		cookies.append("username=" + loginname).append(";");
		headersGrafana.put(AuthenticationUtils.GRAFANA_WEBAUTH_USERKEY, loginname);
		headersGrafana.put(AuthenticationUtils.GRAFANA_WEBAUTH_USERKEY_NAME, loginname);
		headersGrafana.put(AuthenticationUtils.HEADER_COOKIES_KEY, cookies.toString());

		JsonObject userResponseJson = getGrafanaUserInformation(headersGrafana);
		updateUserDetails(userResponseJson, name, loginname, email);

		String grafanaCurrentOrg = getGrafanaCurrentOrg(headersGrafana, userResponseJson);

		String grafanaCurrentOrgRole = getCurrentOrgRole(headersGrafana, grafanaCurrentOrg);

		jsonResponse.addProperty(AuthenticationUtils.GRAFANA_COOKIES_ORG, grafanaCurrentOrg);
		jsonResponse.addProperty(AuthenticationUtils.GRAFANA_COOKIES_ROLE, grafanaCurrentOrgRole);
		jsonResponse.addProperty(AuthenticationUtils.GRAFANA_WEBAUTH_HTTP_REQUEST_HEADER, loginname);
		jsonResponse.addProperty(AuthenticationUtils.SSO_USER_HEADER_KEY, loginname);
		jsonResponse.addProperty(AuthenticationUtils.SSO_LOGOUT_URL,
				ApplicationConfigProvider.getInstance().getSingleSignOnConfig().getPostLogoutURL());
		
		String validatedGrafanaRole =   ValidationUtils.cleanXSS(
		        org.apache.commons.lang.StringEscapeUtils.escapeHtml(
		                org.apache.commons.lang.StringEscapeUtils.escapeJavaScript( grafanaCurrentOrgRole
		                )));
		
		log.debug(" validatedGrafanaRole   ================= {} ",validatedGrafanaRole);

		return validatedGrafanaRole;

	}

	/**
	 * used to logout
	 * 
	 * @param request
	 * @return
	 */
	@GetMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public JsonObject logout(HttpServletRequest request) {
		try {
			HttpSession session = request.getSession(false);
			SecurityContextHolder.clearContext();
			if (session != null) {
				session.invalidate();
			}
			boolean isGrafanaLogout = logoutGrafana();
			if (isGrafanaLogout) {
				Map<String, String> grafanaHeaders = new HashMap<>();
				grafanaHeaders.put(AuthenticationUtils.GRAFANA_COOKIES_ORG, null);
				grafanaHeaders.put(AuthenticationUtils.GRAFANA_COOKIES_ROLE, null);
				grafanaHeaders.put("grafana_user", null);
				grafanaHeaders.put("grafana_sess", null);
				grafanaHeaders.put("grafana_session", null);

			} else {
				String message = "Not able to logout Grafana";
				return PlatformServiceUtil.buildFailureResponse(message);
			}

		} catch (Exception e) {
			log.error(e);
		}
		return PlatformServiceUtil.buildSuccessResponse();
	}

	/**
	 * used to logout SAML and SSO application
	 * 
	 * @return
	 */
	@GetMapping(value = "/insightsso/logout", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public JsonObject logoutSSO() {
		try {
			HttpSession session = httpRequest.getSession(false);
			String authToken = AuthenticationUtils.extractAndValidateAuthToken(httpRequest);

			logoutGrafana();

			Map<String, String> grafanaHeaders = new HashMap<>();
			grafanaHeaders.put(AuthenticationUtils.GRAFANA_COOKIES_ORG, null);
			grafanaHeaders.put(AuthenticationUtils.GRAFANA_COOKIES_ROLE, null);
			grafanaHeaders.put("grafana_user", null);
			grafanaHeaders.put("grafana_sess", null);
			grafanaHeaders.put("grafana_session", null);

			// Remove token
			boolean isTokenRemoved = AuthenticationUtils.deleteToken(authToken);

			if (isTokenRemoved) {
				SecurityContextHolder.clearContext();
				if (session != null) {
					session.invalidate();
				}
			}
			log.debug("Logout URL response done");
		} catch (Exception e) {
			log.error("Error in logoutSSO User  ", e);
			String msg = "Error while login using sso, For detail Please check log file ";
			return PlatformServiceUtil.buildFailureResponse(msg);
		}
		return PlatformServiceUtil.buildSuccessResponse();
	}

	/**
	 * Get Gafana current organization Role
	 * 
	 * @param headers
	 * @param grafanaCurrentOrg
	 * @return
	 * @throws InsightsCustomException
	 */
	private String getCurrentOrgRole(Map<String, String> headers, String grafanaCurrentOrg)
			throws InsightsCustomException {
		String grafanaCurrentOrgResponse = grafanaHandler.grafanaGet("/api/user/orgs", headers);
		JsonArray grafanaOrgs = JsonUtils.parseStringAsJsonArray(grafanaCurrentOrgResponse);
		String grafanaCurrentOrgRole = null;
		for (JsonElement org : grafanaOrgs) {
			if (grafanaCurrentOrg.equals(org.getAsJsonObject().get("orgId").toString())) {
				grafanaCurrentOrgRole = org.getAsJsonObject().get("role").getAsString();
				break;
			}
		}
		return ValidationUtils.cleanXSS(grafanaCurrentOrgRole);
	}

	/**
	 * Get current Grafana organization
	 * 
	 * @param headers
	 * @return
	 * @throws InsightsCustomException
	 */
	private String getGrafanaCurrentOrg(Map<String, String> headers, JsonObject grafanaUserResponse)
			throws InsightsCustomException {
		String loginId = grafanaUserResponse.get("login").toString();
		if (loginId == null || loginId.contains("(null)")) {
			throw new InsightsCustomException(PlatformServiceConstants.GRAFANA_LOGIN_ISSUE);
		}
		return grafanaUserResponse.get("orgId").toString();
	}

	/**
	 * Update user information in Grafana
	 * 
	 * @param userResponseJson
	 * @param name
	 * @param loginname
	 * @param email
	 * @throws InsightsCustomException
	 */
	private void updateUserDetails(JsonObject userResponseJson, String name, String loginname, String email)
			throws InsightsCustomException {

		try {
			Optional<String> loginName = Optional.ofNullable(String.valueOf(userResponseJson.get("name")));
			if (loginName.isPresent() && !loginName.get().isEmpty()) {
				String userId = userResponseJson.get("id").toString();
				String authString = ApplicationConfigProvider.getInstance().getGrafana().getAdminUserName() + ":"
						+ ApplicationConfigProvider.getInstance().getGrafana().getAdminUserPassword();
				String encodedString = Base64.getEncoder().encodeToString(authString.getBytes());
				Map<String, String> headers = new HashMap<>();
				headers.put(AuthenticationUtils.AUTHORIZATION, AuthenticationUtils.BASIC + encodedString);
				JsonObject requestCreate = new JsonObject();
				requestCreate.addProperty("name", name);
				requestCreate.addProperty("login", loginname);
				requestCreate.addProperty("email", email);

				grafanaHandler.grafanaPut("/api/users/" + userId, requestCreate, headers);
			}
		} catch (InsightsCustomException e) {
			// log.error(e);
			log.error("Unable to update user details in SSO login {} ", e.getMessage());
		}
	}

	private JsonObject getGrafanaUserInformation(Map<String, String> headers) throws InsightsCustomException {
		String grafanaUserResponse = grafanaHandler.grafanaGet("/api/user", headers);
		return JsonUtils.parseString(ValidationUtils.validateResponseBody(grafanaUserResponse)).getAsJsonObject();
	}

	/**
	 * Used to logout grafana for user
	 * 
	 * @return
	 */
	private boolean logoutGrafana() {
		boolean isGrafanaLogout = Boolean.FALSE;
		String logoutResponseStr = null;
		try {
			log.debug("In logoutGrafana method");
			Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);

			String responseUser = grafanaHandler.grafanaGet("/api/user", headers);

			String authString = ApplicationConfigProvider.getInstance().getGrafana().getAdminUserName() + ":"
					+ ApplicationConfigProvider.getInstance().getGrafana().getAdminUserPassword();

			JsonObject userresponse = JsonUtils.parseStringAsJsonObject(responseUser);

			int grafanauserId = userresponse.get("id").getAsInt();

			String currentUserName = userresponse.get("login").getAsString();

			String encodedString = Base64.getEncoder().encodeToString(authString.getBytes());
			headers.put("Authorization", "Basic " + encodedString);

			if (currentUserName
					.equalsIgnoreCase(ApplicationConfigProvider.getInstance().getGrafana().getAdminUserName())) {
				log.debug(" admin user logout processing");
				String currentLoginAuthToken = grafanaHandler
						.grafanaGet(PlatformServiceConstants.API_ADMIN_USERS + grafanauserId + "/auth-tokens", headers);
				JsonArray listOfAuthToken = JsonUtils.parseStringAsJsonArray(currentLoginAuthToken);
				log.debug(" admin user logout processing tokal list are {} ", listOfAuthToken.size());
				for (JsonElement jsonElement : listOfAuthToken) {
					JsonObject authTokenRequest = new JsonObject();
					authTokenRequest.addProperty("authTokenId", jsonElement.getAsJsonObject().get("id").getAsInt());

					grafanaHandler.grafanaPost("/api/admin/users/" + grafanauserId + "/revoke-auth-token",
							authTokenRequest, headers);

				}
				logoutResponseStr = "All User auth token revoked";
			} else {
				log.debug("normal user logout processing started ");
				logoutResponseStr = grafanaHandler.grafanaPost("/api/admin/users/" + grafanauserId + "/logout",
						new JsonObject(), headers);
			}

			if (logoutResponseStr != null && (logoutResponseStr.contains("User auth token revoked")
					|| logoutResponseStr.contains("User logged out")
					|| logoutResponseStr.contains("All User auth token revoked"))) {
				log.debug("Grafana logout done successfully");
				isGrafanaLogout = Boolean.TRUE;
			} else {
				log.error("Error while logging out grafana appication {} ", logoutResponseStr);
			}
		} catch (Exception e) {
			log.error(" Logout Grafana  ", e);
		}
		return isGrafanaLogout;
	}
}
