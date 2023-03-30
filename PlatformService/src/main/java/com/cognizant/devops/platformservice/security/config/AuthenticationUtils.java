/*********************************************************************************
 * Copyright 2019 Cognizant Technology Solutions
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
 *******************************************************************************/
package com.cognizant.devops.platformservice.security.config;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.stereotype.Component;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.nimbusds.jwt.JWTClaimsSet;

@Component("authenticationUtils")
@DependsOn("platformServiceInitializer")
public class AuthenticationUtils {
	private static Logger log = LogManager.getLogger(AuthenticationUtils.class);

	public static final int TOKEN_EXPIRE_CODE = 810;
	public static final int SECURITY_CONTEXT_CODE = 811;
	public static final int INFORMATION_MISMATCH = 812;
	public static final int TOKEN_TIME = 60;
	public static final int UNAUTHORISE = 814;

	public static final String GRAFANA_WEBAUTH_USERKEY = "X-WEBAUTH-USER";
	public static final String GRAFANA_WEBAUTH_HEADER_KEY = "user";
	public static final String GRAFANA_WEBAUTH_USERKEY_NAME = "username";
	public static final String GRAFANA_SESSION_KEY = "grafana_session";
	public static final String GRAFANA_ROLE_KEY = "grafanaRole";
	public static final String GRAFANA_SESSION_COOKIE_KEY = "grafana_session";
	public static final String GRAFANA_WEBAUTH_HTTP_REQUEST_HEADER="insights-sso-token";
	public static final String GRAFANA_COOKIES_ORG="grafanaOrg";
	public static final String GRAFANA_COOKIES_ROLE="grafanaRole";

	public static final String SMAL_SCHEMA = "https";
	public static final String APPLICATION_CONTEXT_NAME = "/PlatformService";
	public static final int DEFAULT_PORT = 8080;
	public static final String HEADER_COOKIES_KEY = "Cookie";
	public static final String RESPONSE_HEADER_KEY = "responseHeaders";
	public static final Integer SESSION_TIME = 30;
	public static final String AUTH_HEADER_KEY = "Authorization";
	public static final String CUSTOM_AUTH_HEADER_KEY = "PLAFORM_AUTHORIZATION";
	public static final String SSO_USER_HEADER_KEY = "insights-sso-givenname";
	public static final String SSO_LOGOUT_URL = "postLogoutURL";
	public static final String JTOKEN="jtoken";
	public static final String AUTHORITY = "authority";
	public static final String GRAFANA_DETAIL = "details";
	public static final String KERBEROS_AUTH_HEADER_KEY = "authorization";

	public static final String NATIVE_AUTH_PROTOCOL = "NativeGrafana";
	public static final boolean IS_NATIVE_AUTHENTICATION = NATIVE_AUTH_PROTOCOL
			.equalsIgnoreCase(ApplicationConfigProvider.getInstance().getAutheticationProtocol());

	public static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
	public static final List<String> CSRF_IGNORE = Collections.unmodifiableList(Arrays.asList("/login/**", "/user/insightsso/authenticateSSO/**",
			"/user/authenticate/**", "/user/insightsso/**", "/saml/**", "/externalApi/**"));
	
	public static final List<String> WEB_IGNORE_URLS = Collections.unmodifiableList(Arrays.asList("/settings/getLogoImage"));
	
	public static final String EXTERNALAPI = "/externalApi/";
	
	public static final String JWT_LOGIN_URL = "/PlatformService/user/insightsso/authenticateJWT";
	
	public static final String JWT_USER_DETAIL_URL = "/PlatformService/user/insightsso/getJWTUserDetail";

	public static final List<String> SET_VALUES = Collections.unmodifiableList(Arrays.asList(GRAFANA_COOKIES_ORG, "grafana_user", GRAFANA_ROLE_KEY,
			"grafana_remember", "grafana_sess", CSRF_COOKIE_NAME , "JSESSIONID",  GRAFANA_SESSION_KEY, GRAFANA_WEBAUTH_HTTP_REQUEST_HEADER,
			GRAFANA_WEBAUTH_USERKEY_NAME, SSO_USER_HEADER_KEY,JTOKEN ));
	public static final Set<String> MASTER_COOKIES_KEY_LIST = Collections.unmodifiableSet(new HashSet<String>(SET_VALUES));

	public static final String JSON_FILE_VALIDATOR = "^([a-zA-Z0-9_.\\s-])+(.json)$";
	public static final String LOG_FILE_VALIDATOR = "^([a-zA-Z0-9_.\\s-])+(.log)$";
	public static final String HTML_FILE_VALIDATOR = "^([a-zA-Z0-9_.\\s-])+(.html)$";
	public static final String CSV_FILE_VALIDATOR = "^([a-zA-Z0-9_.\\s-])+(.csv)$";
	public static final String CSS_FILE_VALIDATOR = "^([a-zA-Z0-9_.\\s-])+(.css)$";
	public static final String WEBP_FILE_VALIDATOR = "^([a-zA-Z0-9_.\\s-])+(.webp)$";

	public static final List<String> SUPPORTED_TYPE = Collections.unmodifiableList(Arrays.asList("SAML", "Kerberos", NATIVE_AUTH_PROTOCOL, "JWT" ));
	public static final Set<String> AUTHENTICATION_PROTOCOL_LIST = Collections.unmodifiableSet(new HashSet<String>(SUPPORTED_TYPE));

	protected static List<SecurityFilterChain> securityFilterchains = new ArrayList<>();
	
	private static TokenProviderUtility tokenProviderUtility = new TokenProviderUtility();

	public static void setResponseMessage(HttpServletResponse response, int statusCode, String message) {
		try {
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setStatus(statusCode);
			response.getWriter().write("Error while processing request..");
			response.getWriter().flush();
			response.getWriter().close();

		} catch (IOException e) {
			log.error("Error in setUnauthorizedResponse ", e);
		}
	}

	public static String getHost(HttpServletRequest httpRequest) {
		URL url;
		String urlString;
		String hostInfo = null;
		try {
			if (httpRequest == null) {
				url = new URL(ApplicationConfigProvider.getInstance().getInsightsServiceURL());
				hostInfo = url.getHost();
			} else {
				urlString = httpRequest.getHeader(HttpHeaders.ORIGIN) == null
						? httpRequest.getHeader(HttpHeaders.REFERER)
						: httpRequest.getHeader(HttpHeaders.ORIGIN);
				if (urlString == null) {
					urlString = httpRequest.getHeader(HttpHeaders.HOST);
					hostInfo = urlString;
					if (urlString.contains(":")) {
						int index = urlString.indexOf(':');
						hostInfo = urlString.substring(0, index);
					}
				} else {
					url = new URL(urlString);
					hostInfo = url.getHost();
				}
			}
			return hostInfo;
		} catch (MalformedURLException e) {
			log.error("Unable to retrive host information ",e);
			log.error(e);
			return null;
		}
	}

	public static String getRelayStateURL(HttpServletRequest httpRequest) {
		return httpRequest.getHeader(HttpHeaders.REFERER) + "/#/ssologin";
	}

	public static String getLogoutURL(int logoutCode, String message) {
		try {
			String url = URLEncoder.encode(
					ApplicationConfigProvider.getInstance().getSingleSignOnConfig().getPostLogoutURL(), "UTF-8");
			String returnLogoutStr = String.format("%s/#/logout/%s?logout_url=%s&message=%s",
					ApplicationConfigProvider.getInstance().getInsightsServiceURL(), logoutCode, url, message);
			log.debug("Logout URL ++++ {} ", returnLogoutStr);
			return returnLogoutStr;
		} catch (Exception e) {
			log.error("Unable to retrive logout information ");
			return null;
		}
	}

	public CsrfTokenRepository csrfTokenRepository() {
		HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
		repository.setHeaderName(AuthenticationUtils.CSRF_COOKIE_NAME);
		return repository;
	}

	public static List<SecurityFilterChain> getSecurityFilterchains() {
		return AuthenticationUtils.securityFilterchains;
	}
	
	public static void setSecurityFilterchain(SecurityFilterChain securityFilterchains) {
		AuthenticationUtils.securityFilterchains.add(securityFilterchains);
	}

	public static SpringAuthority getSpringAuthorityRole(String grafanaCurrentOrgRole) {
		try {
			return SpringAuthority.valueOf(grafanaCurrentOrgRole.replaceAll("\\s", "_"));
		} catch (Exception e) {
			log.error("Unable to find grafana role in Spring Authority. {}", e.getMessage());
		}
		
		return SpringAuthority.valueOf("Viewer");	
		
	}
	
	/**
	 * Extract and validate authrization token
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	public static String extractAndValidateAuthToken(HttpServletRequest request, HttpServletResponse response) {
		String platformAuth = request.getHeader(AuthenticationUtils.CUSTOM_AUTH_HEADER_KEY);
		String authToken = "";
		if("".equals(platformAuth) || platformAuth==null) {
			authToken = ValidationUtils.cleanXSS(request.getHeader(AuthenticationUtils.AUTH_HEADER_KEY));
		}else {
			log.debug(" Featching record from custom Auth header {} ",request.getHeader(AuthenticationUtils.CUSTOM_AUTH_HEADER_KEY));
			authToken = ValidationUtils.cleanXSS(request.getHeader(AuthenticationUtils.CUSTOM_AUTH_HEADER_KEY));
		}
		if (authToken == null || authToken.isEmpty()) {
			log.error(" InsightsAuthenticationFilter Authorization is empty or not found ");
			String msg = PlatformServiceUtil.buildFailureResponse("Unauthorized Access ,Invalid Credentials..")
					.toString();
			AuthenticationUtils.setResponseMessage(response, HttpServletResponse.SC_BAD_REQUEST, msg);
		}
		return authToken;
	}
	
	public static  String extractAndValidateAuthToken(HttpServletRequest request) {
		String platformAuth = request.getHeader(AuthenticationUtils.CUSTOM_AUTH_HEADER_KEY);
		String authToken = "";
		if("".equals(platformAuth) || platformAuth==null) {
			authToken = ValidationUtils.cleanXSS(request.getHeader(AuthenticationUtils.AUTH_HEADER_KEY));
		}else {
			log.debug(" Featching record from custom Auth header only request {} ",request.getHeader(AuthenticationUtils.CUSTOM_AUTH_HEADER_KEY));
			authToken = ValidationUtils.cleanXSS(request.getHeader(AuthenticationUtils.CUSTOM_AUTH_HEADER_KEY));
		}
		if (authToken == null || authToken.isEmpty()) {
			throw new InsightsAuthenticationException(" InsightsAuthenticationFilter Authorization is empty or not found ");
		}
		return authToken;
	}
	
	public static JWTClaimsSet validateIncomingToken(Object principal) {
		JWTClaimsSet jwtClaimsSet = null;
		try {
			if(ApplicationConfigProvider.getInstance().getAutheticationProtocol().equalsIgnoreCase("JWT")) {
				jwtClaimsSet =tokenProviderUtility.verifyExternalTokenAndFetchClaims(principal.toString());
			} else {
				jwtClaimsSet =tokenProviderUtility.verifyAndFetchCliaimsToken(principal.toString());
			}
		} catch (InsightsCustomException e) {
			log.error(e);
			log.error(" Exception while varifing token " + e.getMessage(), e);
			throw new InsightsAuthenticationException(e.getMessage());
		} catch (AuthorizationServiceException e) {
			log.error(e);
			log.error(" Exception while validating token {}", e.getMessage());
			throw new InsightsAuthenticationException(e.getMessage(), e);
		} catch (AuthenticationCredentialsNotFoundException e) {
			log.error(e);
			log.error(" Token not found in cache {} ", e.getMessage());
			throw new InsightsAuthenticationException(e.getMessage(), e);
		} catch (AccountExpiredException e) {
			log.error(e);
			log.error(" Token Session Expire {}", e.getMessage());
			throw new InsightsAuthenticationException(e.getMessage(), e);
		} catch (Exception e) {
			log.error(e);
			log.error(" Error while validating token {} ", e.getMessage());
			throw new InsightsAuthenticationException(e.getMessage(), e);
		}
		return jwtClaimsSet ;
	}
	
	public static String getToken(String userid, int tokenTime, Map<String, Object> params)  {
		String jToken ="";
		try {
			jToken= tokenProviderUtility.createToken(userid,tokenTime, params);
		} catch (Exception e) {
			log.error(e);
			throw new AuthorizationServiceException(" Error while creating data ");
		}
		return jToken;
	}
	
	public static boolean deleteToken(String authToken) {
		return tokenProviderUtility.deleteToken(authToken);
	}
	
}
