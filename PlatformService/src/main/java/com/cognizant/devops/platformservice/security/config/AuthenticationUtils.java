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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;

public class AuthenticationUtils {
	private static Logger Log = LogManager.getLogger(AuthenticationUtils.class);
	
	public final static int TOKEN_EXPIRE_CODE = 810;
	public final static int SECURITY_CONTEXT_CODE = 811;
	public final static int INFORMATION_MISMATCH=812;
	public final static int TOKEN_TIME = 60;
	public final static int UNAUTHORISE = 814;
	public final static String GRAFANA_WEBAUTH_USERKEY = "X-WEBAUTH-USER";
	public final static String GRAFANA_WEBAUTH_HEADER_KEY = "user";
	public final static String GRAFANA_WEBAUTH_USERKEY_NAME = "username";
	public static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
	public static final String SMAL_SCHEMA = "https";
	public static final String APPLICATION_CONTEXT_NAME = "/PlatformService";
	public static final int DEFAULT_PORT = 8080;
	public static final String HEADER_COOKIES_KEY ="Cookie";
	public static final Integer sessionTime= 60;
	public static final String GRAFANA_SESSION_COOKIE_KEY ="grafana_session";
	public static final String[] CSRF_IGNORE = { "/login/**", "/user/insightsso/authenticateSSO/**",
		 	"/user/authenticate/**", "/user/insightsso/**","/saml/**" };
	public static final String[] SET_VALUES = new String[] { "grafanaOrg", "grafana_user", "grafanaRole",
			"grafana_remember", "grafana_sess", "XSRF-TOKEN", "JSESSIONID","grafana_session","insights-sso-token","username","insights-sso-givenname" };
	public static final Set<String> MASTER_COOKIES_KEY_LIST = new HashSet<String>(Arrays.asList(SET_VALUES));
	public final static String JSON_FILE_VALIDATOR = "^([a-zA-Z0-9_.\\s-])+(.json)$";
	public final static String LOG_FILE_VALIDATOR = "^([a-zA-Z0-9_.\\s-])+(.log)$";
	
	public static void setResponseMessage(HttpServletResponse response, int statusCode, String message) {
		try {
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setStatus(statusCode);
			response.getWriter().write(message);
			response.getWriter().flush();
			response.getWriter().close();

		} catch (IOException e) {
			Log.error("Error in setUnauthorizedResponse ", e);
		}
	}
	
	public static String getHost(HttpServletRequest httpRequest) {
		URL url;
		try {
			if(httpRequest == null) {
				url = new URL(ApplicationConfigProvider.getInstance().getInsightsServiceURL());
			}else {
				String urlString = httpRequest.getHeader(HttpHeaders.ORIGIN)==null ? httpRequest.getHeader(HttpHeaders.REFERER) : httpRequest.getHeader(HttpHeaders.ORIGIN) ;
				url = new URL(urlString);
			}
			return url.getHost();
		} catch (MalformedURLException e) {
			Log.error("Unable to retrive host information ");
			return null;
		}
	}
	
	public static String getLogoutURL(HttpServletRequest httpRequest,int logoutCode,String message)  {
		try {
			String url = URLEncoder.encode(ApplicationConfigProvider.getInstance().getSingleSignOnConfig().getPostLogoutURL(),"UTF-8");
			String returnLogoutStr= String.format("%s/#/logout/%s?logout_url=%s&message=%s",ApplicationConfigProvider.getInstance().getInsightsServiceURL(),logoutCode,url,message); 
			Log.debug("Logout URL ++++ "+returnLogoutStr);
			return returnLogoutStr;
		} catch (Exception e) {
			Log.error("Unable to retrive logout information ");
			return null;
		}
		
	}
	
}
