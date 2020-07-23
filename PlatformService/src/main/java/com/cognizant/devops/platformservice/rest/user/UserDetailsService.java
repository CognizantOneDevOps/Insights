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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.kerberos.authentication.KerberosServiceRequestToken;
import org.springframework.security.providers.ExpiringUsernameAuthenticationToken;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.dal.grafana.GrafanaHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.cognizant.devops.platformservice.security.config.AuthenticationUtils;
import com.cognizant.devops.platformservice.security.config.InsightsAuthenticationToken;
import com.cognizant.devops.platformservice.security.config.TokenProviderUtility;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RestController
@RequestMapping("/user")
public class UserDetailsService {
	private static Logger log = LogManager.getLogger(UserDetailsService.class.getName());

	@Autowired
	private HttpServletRequest httpRequest;

	@Autowired
	private TokenProviderUtility tokenProviderUtility;
	
	GrafanaHandler grafanaHandler = new GrafanaHandler();

	/**used to authenticate Grafana User
	 * @param request
	 * @return
	 */
	@PostMapping(value = "/authenticate", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public JsonObject authenticateUser(HttpServletRequest request) {
		log.debug("Inside authenticateUser ");
		@SuppressWarnings("unchecked")
		Map<String, String> responseHeadersgrafanaAttr = (Map<String, String>) request
				.getAttribute(AuthenticationUtils.RESPONSE_HEADER_KEY);
		if(responseHeadersgrafanaAttr!=null) {
			for (Map.Entry<String, String> entry : responseHeadersgrafanaAttr.entrySet()) {
				ValidationUtils.cleanXSS(entry.getValue());
			}
			return PlatformServiceUtil.buildSuccessResponseWithData(responseHeadersgrafanaAttr);
		}else {
			return PlatformServiceUtil.buildFailureResponse("Error in authenticate Grafana User,unable to retrive grafana cookies ");
		}
	}

	/** used to authenticate SSO SAML User and Redirect to UI application
	 * @return
	 * @throws InsightsCustomException
	 */
	@GetMapping(value = "/insightsso/authenticateSSO", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<Object> authenticateSSOUser() throws InsightsCustomException {

		log.debug("Inside authenticateSSOUser");

		HttpHeaders httpHeaders = new HttpHeaders();
		JsonObject jsonResponse = new JsonObject();
		try {
			SecurityContext context = SecurityContextHolder.getContext();
			Authentication auth = context.getAuthentication();
			SAMLCredential credentials = (SAMLCredential) auth.getCredentials();
			String userid = credentials.getNameID().getValue();
			String givenname = credentials
					.getAttributeAsString("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname");

			jsonResponse.addProperty("insights-user-fullname", givenname);

			jsonResponse.addProperty(AuthenticationUtils.GRAFANA_WEBAUTH_HTTP_REQUEST_HEADER, userid);

			validateGrafanaDetail(jsonResponse, userid);

			httpRequest.setAttribute(AuthenticationUtils.RESPONSE_HEADER_KEY, jsonResponse);

			httpHeaders.add(AuthenticationUtils.GRAFANA_WEBAUTH_USERKEY, userid);

			URI uri = new URI(ApplicationConfigProvider.getInstance().getSingleSignOnConfig().getRelayStateUrl());

			httpHeaders.setLocation(uri);

		} catch (InsightsCustomException e) {
			log.error("Error in authenticate SSO User {} " , e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(PlatformServiceConstants.GRAFANA_LOGIN_ISSUE);
		} catch (Exception e) {
			log.error("Error in authenticate SSO User {} ", e);
			String msg = "Error while login using sso, For detail Please check log file ";
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
		}

		return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);//success
	}

	

	/** Used to validate authenticate Kerberos User and Redirect to UI application 
	 * @param request
	 * @return
	 */
	@GetMapping(value = "/insightsso/kerberosLogin", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> authenticateKerberosUser(HttpServletRequest request) {
		log.debug("Inside authenticateKerberosUser kerberosLogin ");
		HttpHeaders httpHeaders = new HttpHeaders();
		JsonObject jsonResponse = new JsonObject();
		try {
			SecurityContext context = SecurityContextHolder.getContext();
			KerberosServiceRequestToken authKerberos = (KerberosServiceRequestToken) context.getAuthentication();
			if(authKerberos !=null) {
				String userid = authKerberos.getName();
				
				String grafanaCurrentOrgRole = validateGrafanaDetail(jsonResponse, userid);

				httpRequest.setAttribute(AuthenticationUtils.RESPONSE_HEADER_KEY, jsonResponse);
				URI uri = new URI(ApplicationConfigProvider.getInstance().getSingleSignOnConfig().getRelayStateUrl());
	
				httpHeaders.setLocation(uri);
			}else {
				log.error(" KerberosServiceRequestToken is Empty, Please try again  ");
			}
		}catch (InsightsCustomException e) {
			log.error("Error in authenticate Kerberos User {}  " , e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(PlatformServiceConstants.GRAFANA_LOGIN_ISSUE);
		} catch (Exception e) {
			log.error("Error in authenticate Kerberos User {} ", e);
			String msg = "Error while login using Kerberos, For detail Please check log file ";
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
		}
		
		
		
		return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);//success
	}
	

	/** used to get SAML user detail after login application
	 * @return
	 */
	@GetMapping(value = "/insightsso/getUserDetail", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getSAMLUserDetail() {

		log.debug("Inside getUserDetail");
		/*		Map<String, String> headersGrafana = new HashMap<>();*/

		JsonObject jsonResponse = new JsonObject();

		try {
			SecurityContext context = SecurityContextHolder.getContext();
			Authentication auth = context.getAuthentication();
			SAMLCredential credentials = (SAMLCredential) auth.getCredentials();
			Object principal = auth.getPrincipal();
			String userid = credentials.getNameID().getValue();
			String givenname = credentials
					.getAttributeAsString("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname");

			String grafanaCurrentOrgRole = validateGrafanaDetail(jsonResponse, userid);

			String jToken = tokenProviderUtility.createToken(userid);
			jsonResponse.addProperty("jtoken", jToken);

			// set Authority to spring context
			List<GrantedAuthority> updatedAuthorities = new ArrayList<>();
			updatedAuthorities.add(AuthenticationUtils.getSpringAuthorityRole(grafanaCurrentOrgRole));

			Date expDate = new Date(System.currentTimeMillis() + 60 * 60 * 1000);
			ExpiringUsernameAuthenticationToken autharization = new ExpiringUsernameAuthenticationToken(expDate,
					principal, auth.getCredentials(), updatedAuthorities);
			SecurityContextHolder.getContext().setAuthentication(autharization);
			Authentication auth2 = SecurityContextHolder.getContext().getAuthentication();
			auth2.getAuthorities().forEach(a -> log.debug("GrantedAuthority  {} " , a.getAuthority()));

			httpRequest.setAttribute(AuthenticationUtils.RESPONSE_HEADER_KEY, jsonResponse);
		} catch (Exception e) {
			log.error("Error in SSO Cookie {} ", e);
			return PlatformServiceUtil.buildFailureResponse("Error in SSO Cookie " + e);
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(jsonResponse);
	}
	
	/** used to get kerberos user detail 
	 * @return
	 * @throws InsightsCustomException
	 */
	@GetMapping(value = "/insightsso/getKerberosUserDetail", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getKerberosUserDetail() throws InsightsCustomException {

		log.debug("Inside authenticateKerberos");
		/*Map<String, String> headersGrafana = new HashMap<>();*/
		JsonObject jsonResponse = new JsonObject();
		try {
			SecurityContext context = SecurityContextHolder.getContext();
			KerberosServiceRequestToken authKerberos = (KerberosServiceRequestToken) context.getAuthentication();

			String userid = authKerberos.getName();

			log.debug("Inside authenticateKerberos userid {} ", userid);

			String grafanaCurrentOrgRole = validateGrafanaDetail(jsonResponse, userid);

			String token = ValidationUtils.cleanXSS(httpRequest.getHeader(AuthenticationUtils.AUTH_HEADER_KEY));
			//String jToken = tokenProviderUtility.createToken(userid);
			jsonResponse.addProperty("jtoken", token);

			List<GrantedAuthority> updatedAuthorities = new ArrayList<>();
			updatedAuthorities.add(AuthenticationUtils.getSpringAuthorityRole(grafanaCurrentOrgRole));

			KerberosServiceRequestToken responseAuth = new KerberosServiceRequestToken(authKerberos.getDetails(),
					authKerberos.getTicketValidation(), updatedAuthorities, authKerberos.getToken());
			//log.debug("In successfulAuthentication authenticateKerberos Older Kerberos GrantedAuthority ==== {} ", authKerberos);
			log.debug("In successfulAuthentication authenticateKerberos Kerberos GrantedAuthority ==== {} ",
					responseAuth);

			SecurityContextHolder.getContext().setAuthentication(responseAuth);

			httpRequest.setAttribute(AuthenticationUtils.RESPONSE_HEADER_KEY, jsonResponse);
		} catch (Exception e) {
			log.error("Error in authenticate Kerberos User {} ", e);
			return PlatformServiceUtil
					.buildFailureResponseWithStatusCode("Error while authenticating kerberos request  ", "811");
		}

		return PlatformServiceUtil.buildSuccessResponseWithData(jsonResponse);
	}

	/**
	 * @return
	 * @throws InsightsCustomException
	 */
	@GetMapping(value = "/insightsso/authenticateJWT", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<Object> authenticateUserUsingJWT() throws InsightsCustomException {

		log.debug("Inside authenticateUserUsingJWT ======= ");
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

				//String grafanaCurrentOrgRole = validateGrafanaDetail(jsonResponse, userid);
				jsonResponse.addProperty("tokenauth", String.valueOf(authJWT.getPrincipal()));
				httpRequest.setAttribute(AuthenticationUtils.RESPONSE_HEADER_KEY, jsonResponse);
				URI uri = new URI(ApplicationConfigProvider.getInstance().getSingleSignOnConfig().getRelayStateUrl());
				jsonResponse.addProperty("urlRedirect",
						ApplicationConfigProvider.getInstance().getSingleSignOnConfig().getRelayStateUrl());
				httpHeaders.setLocation(uri);
			} else {
				log.error(" KerberosServiceRequestToken is Empty, Please try again  ");
			}
		} catch (Exception e) {
			log.error("Error in authenticate Kerberos User {} ", e);
			String msg = "Error while login using Kerberos, For detail Please check log file ";
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
		}

		return new ResponseEntity<>(jsonResponse, httpHeaders, HttpStatus.OK);//success
		//return PlatformServiceUtil.buildSuccessResponseWithData(jsonResponse);
	}

	/**
	 * used to get kerberos user detail
	 * 
	 * @return
	 * @throws InsightsCustomException
	 */
	@GetMapping(value = "/insightsso/getJWTUserDetail", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getJWTUserDetail() throws InsightsCustomException {

		log.debug("Inside authenticateKerberos");
		/*Map<String, String> headersGrafana = new HashMap<>();*/
		JsonObject jsonResponse = new JsonObject();
		try {
			SecurityContext context = SecurityContextHolder.getContext();
			InsightsAuthenticationToken authJWT = (InsightsAuthenticationToken) context.getAuthentication();

			String userid = String.valueOf(authJWT.getDetails());

			log.debug("Inside getJWTUserDetail userid {} ", userid);

			String grafanaCurrentOrgRole = validateGrafanaDetail(jsonResponse, userid);

			jsonResponse.addProperty("jtoken", String.valueOf(authJWT.getPrincipal()));

			List<GrantedAuthority> updatedAuthorities = new ArrayList<>();
			updatedAuthorities.add(AuthenticationUtils.getSpringAuthorityRole(grafanaCurrentOrgRole));

			InsightsAuthenticationToken jwtAuthenticationToken = new InsightsAuthenticationToken(authJWT.getPrincipal(),
					userid, null, updatedAuthorities);
			//log.debug("In successfulAuthentication authenticateKerberos Older Kerberos GrantedAuthority ==== {} ", authKerberos);
			log.debug("In successfulAuthentication authenticateKerberos Kerberos GrantedAuthority ==== {} ",
					jwtAuthenticationToken);

			SecurityContextHolder.getContext().setAuthentication(jwtAuthenticationToken);

			httpRequest.setAttribute(AuthenticationUtils.RESPONSE_HEADER_KEY, jsonResponse);
		} catch (Exception e) {
			log.error("Error in authenticate Kerberos User {} ", e);
			return PlatformServiceUtil
					.buildFailureResponseWithStatusCode("Error while authenticating kerberos request  ", "811");
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
	private String validateGrafanaDetail(JsonObject jsonResponse, String userid) throws InsightsCustomException {
		Map<String, String> headersGrafana = new HashMap<>();
		headersGrafana.put(AuthenticationUtils.GRAFANA_WEBAUTH_USERKEY, userid);
		headersGrafana.put(AuthenticationUtils.GRAFANA_WEBAUTH_USERKEY_NAME, userid);
		headersGrafana.put(AuthenticationUtils.HEADER_COOKIES_KEY, "username=" + userid);

		log.debug("Headers in headersGrafana {}", headersGrafana);
		String grafanaCurrentOrg = getGrafanaCurrentOrg(headersGrafana);

		String grafanaCurrentOrgRole = getCurrentOrgRole(headersGrafana, grafanaCurrentOrg);

		jsonResponse.addProperty(AuthenticationUtils.GRAFANA_COOKIES_ORG, grafanaCurrentOrg);
		jsonResponse.addProperty(AuthenticationUtils.GRAFANA_COOKIES_ROLE, grafanaCurrentOrgRole);
		jsonResponse.addProperty(AuthenticationUtils.GRAFANA_WEBAUTH_HTTP_REQUEST_HEADER, userid);
		jsonResponse.addProperty(AuthenticationUtils.SSO_USER_HEADER_KEY, userid);
		jsonResponse.addProperty(AuthenticationUtils.SSO_LOGOUT_URL,
				ApplicationConfigProvider.getInstance().getSingleSignOnConfig().getPostLogoutURL());

		return grafanaCurrentOrgRole;

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

				request.setAttribute("responseHeaders", grafanaHeaders);

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
			String auth_token = ValidationUtils.cleanXSS(httpRequest.getHeader("Authorization"));
			
			boolean isGrafanaLogout = logoutGrafana();

			Map<String, String> grafanaHeaders = new HashMap<>();
			grafanaHeaders.put(AuthenticationUtils.GRAFANA_COOKIES_ORG, null);
			grafanaHeaders.put(AuthenticationUtils.GRAFANA_COOKIES_ROLE, null);
			grafanaHeaders.put("grafana_user", null);
			grafanaHeaders.put("grafana_sess", null);
			grafanaHeaders.put("grafana_session", null);
	
			// Remove token
			boolean isTokenRemoved = tokenProviderUtility.deleteToken(auth_token);
			
			if(isTokenRemoved) {
				httpRequest.setAttribute("responseHeaders", grafanaHeaders);
				SecurityContextHolder.clearContext();
				if (session != null) {
					session.invalidate();
				}
			}
			log.debug("Logout URL response done");
		} catch (URISyntaxException e) {
			log.error("Error in logoutSSO User {}  " , e);
			return PlatformServiceUtil.buildFailureResponse(PlatformServiceConstants.INVALID_REQUEST);
		} catch (InsightsCustomException e) {
			log.error("Error in logoutSSO User {} " , e);
			return PlatformServiceUtil.buildFailureResponse(PlatformServiceConstants.INVALID_REQUEST);
		} catch (Exception e) {
			log.error("Error in logoutSSO User {} " , e);
			String msg = "Error while login using sso, For detail Please check log file ";
			return PlatformServiceUtil.buildFailureResponse(msg);
		}
		return PlatformServiceUtil.buildSuccessResponse();
	}

	/** Get Gafana current organization Role
	 * @param headers
	 * @param grafanaCurrentOrg
	 * @return
	 * @throws InsightsCustomException
	 */
	private String getCurrentOrgRole(Map<String, String> headers, String grafanaCurrentOrg) throws InsightsCustomException {
		GrafanaHandler grafanaHandler = new GrafanaHandler();
		String grafanaCurrentOrgResponse = grafanaHandler.grafanaGet("/api/user/orgs", headers);
		JsonArray grafanaOrgs = new JsonParser().parse(grafanaCurrentOrgResponse).getAsJsonArray();
		String grafanaCurrentOrgRole = null;
		for (JsonElement org : grafanaOrgs) {
			if (grafanaCurrentOrg.equals(org.getAsJsonObject().get("orgId").toString())) {
				grafanaCurrentOrgRole = org.getAsJsonObject().get("role").getAsString();
				break;
			}
		}
		return grafanaCurrentOrgRole;
	}

	/** Get current Grafana organization 
	 * @param headers
	 * @return
	 * @throws InsightsCustomException
	 */
	private String getGrafanaCurrentOrg(Map<String, String> headers) throws InsightsCustomException {
		GrafanaHandler grafanaHandler = new GrafanaHandler();
		String grafanaCurrentOrgResponse = grafanaHandler.grafanaGet("/api/user", headers);
		JsonObject responseJson = new JsonParser().parse(grafanaCurrentOrgResponse)
				.getAsJsonObject();
		log.debug(" Current user detail ==== {} ", responseJson);
		String loginId = responseJson.get("login").toString();
		if (loginId == null || loginId.contains("(null)")) {
			throw new InsightsCustomException(PlatformServiceConstants.GRAFANA_LOGIN_ISSUE);
		}
		return responseJson.get("orgId").toString();
	}
	
	/** Used to logout grafana for user 
	 * @return
	 */
	private boolean logoutGrafana() {
		boolean isGrafanaLogout = Boolean.FALSE;
		JsonParser parser = new JsonParser();
		String logoutResponseStr = null;
		try {
			log.debug("In logoutGrafana method");
			Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);

			String responseUser = grafanaHandler.grafanaGet("/api/user", headers);

			String authString = ApplicationConfigProvider.getInstance().getGrafana().getAdminUserName() + ":"
					+ ApplicationConfigProvider.getInstance().getGrafana().getAdminUserPassword();


			JsonObject userresponse = parser.parse(responseUser).getAsJsonObject();

			int grafanauserId = userresponse.get("id").getAsInt();

			String currentUserName = userresponse.get("login").getAsString();

			log.debug("Logout Grafana for user {} {} ", grafanauserId, currentUserName);

			String encodedString = Base64.getEncoder().encodeToString(authString.getBytes());
			headers.put("Authorization", "Basic " + encodedString);

			if (currentUserName
					.equalsIgnoreCase(ApplicationConfigProvider.getInstance().getGrafana().getAdminUserName())) {
				log.debug(" admin user logout processing");
				String currentLoginAuthToken = grafanaHandler
						.grafanaGet("/api/admin/users/" + grafanauserId + "/auth-tokens", 
						headers);
				JsonArray listOfAuthToken = parser.parse(currentLoginAuthToken).getAsJsonArray();
				log.debug(" admin user logout processing tokal list are {} ", listOfAuthToken.size());
				for (JsonElement jsonElement : listOfAuthToken) {
					log.debug(" auth token list {} ", jsonElement.getAsJsonObject());
					JsonObject authTokenRequest = new JsonObject();
					authTokenRequest.addProperty("authTokenId", jsonElement.getAsJsonObject().get("id").getAsInt());

					String revokeResponse = grafanaHandler.grafanaPost(
							"/api/admin/users/" + grafanauserId + "/revoke-auth-token", authTokenRequest, headers);
					log.debug(" auth token revokeResponse {} ", revokeResponse);

				}
				logoutResponseStr = "All User auth token revoked";
			} else {
				log.debug("normal user logout processing {} ", currentUserName);
				logoutResponseStr = grafanaHandler.grafanaPost("/api/admin/users/" + grafanauserId + "/logout",
					new JsonObject(), headers);
			}

			if (logoutResponseStr != null && (logoutResponseStr.contains("User auth token revoked")
					|| logoutResponseStr.contains("User logged out")
					|| logoutResponseStr.contains("All User auth token revoked"))) {
				log.debug("Grafana logout done successfully  {}", logoutResponseStr);
				isGrafanaLogout = Boolean.TRUE;
			} else {
				log.error("Error while logging out grafana appication {} ", logoutResponseStr);
			}
		} catch (Exception e) {
			log.error(" Logout Grafana " + e);
		}
		return isGrafanaLogout;
	}
}
