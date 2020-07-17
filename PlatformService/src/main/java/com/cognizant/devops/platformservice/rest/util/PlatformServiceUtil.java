/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
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
 ******************************************************************************/
package com.cognizant.devops.platformservice.rest.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.dal.grafana.GrafanaHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.security.config.AuthenticationUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;


public class PlatformServiceUtil {
	private static final Logger log = LogManager.getLogger(PlatformServiceUtil.class);
	
	GrafanaHandler grafanaHandler = new GrafanaHandler();

	private PlatformServiceUtil() {

	}

	public static JsonObject buildSuccessResponseWithData(Object data) {

		JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty(PlatformServiceConstants.STATUS, PlatformServiceConstants.SUCCESS);
		jsonResponse.add(PlatformServiceConstants.DATA, new Gson().toJsonTree(data));
		//JsonObject validatedData = ValidationUtils.validateStringForHTMLContent(jsonResponse);
		JsonObject validatedData = ValidationUtils.replaceHTMLContentFormString(jsonResponse);
		if (validatedData == null) {
			validatedData = buildFailureResponse(PlatformServiceConstants.INVALID_RESPONSE_DATA);
		}
		return validatedData;
	}

	public static JsonObject buildFailureResponse(String message) {
		JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty(PlatformServiceConstants.STATUS, PlatformServiceConstants.FAILURE);
		jsonResponse.addProperty(PlatformServiceConstants.MESSAGE, message);
		return jsonResponse;
	}

	public static JsonObject buildFailureResponseWithStatusCode(String message, String statusCode) {
		JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty(PlatformServiceConstants.STATUS, PlatformServiceConstants.FAILURE);
		jsonResponse.addProperty(PlatformServiceConstants.MESSAGE, message);
		jsonResponse.addProperty("StatusCode", statusCode);
		return jsonResponse;
	}

	public static JsonObject buildSuccessResponseWithHtmlData(Object data) {

		JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty(PlatformServiceConstants.STATUS, PlatformServiceConstants.SUCCESS);
		jsonResponse.add(PlatformServiceConstants.DATA, new Gson().toJsonTree(data));
		return jsonResponse;
	}

	public static JsonObject buildSuccessResponse() {
		JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty(PlatformServiceConstants.STATUS, PlatformServiceConstants.SUCCESS);
		return jsonResponse;
	}


	public static Cookie[] validateCookies(Cookie[] request_cookies) {
		Cookie[] cookiesArray = null;
		Cookie cookie = null;
		int cookiesArrayLength = 0;
		List<Cookie> cookiesList = new ArrayList<Cookie>();
		if (request_cookies != null) {
			// log.debug("Request Cookies length " + request_cookies.length);
			for (int i = 0; i < request_cookies.length; i++) {
				cookie = request_cookies[i];
				log.debug(" cookie " + cookie.getName() + " " + cookie.getValue());
				if (AuthenticationUtils.MASTER_COOKIES_KEY_LIST.contains(cookie.getName())) {
					cookie.setMaxAge(30 * 60);
					cookie.setHttpOnly(true);
					cookie.setValue(ValidationUtils.cleanXSSWithHTMLCheck(cookie.getValue()));
					//cookies[i] = cookie;
					cookiesList.add(cookie);
					cookiesArrayLength = cookiesArrayLength + 1;
				} else {
					log.debug("Cookie Name Not found in master cookies list name as " + cookie.getName());
				}
			}
			cookiesArray = new Cookie[cookiesArrayLength];
			cookiesArray = cookiesList.toArray(cookiesArray);
			// log.debug("Request return Cookies length " + cookies.length);
		} else {
			cookiesArray = request_cookies;
			log.warn("No cookies founds");
		}
		return cookiesArray;
	}

	public static Map<String, String> getRequestCookies(HttpServletRequest httpRequest) {
		Map<String, String> cookieMap = new HashMap<String, String>(0);
		Cookie cookie = null;
		Cookie[] cookiesList = httpRequest.getCookies();
		if (cookiesList != null) {
			// log.debug("Request Cookies length " + request_cookies.length);
			for (int i = 0; i < cookiesList.length; i++) {
				cookie = cookiesList[i];
				//log.debug(" cookie " + cookie.getName() + " " + cookie.getValue());
				if (AuthenticationUtils.MASTER_COOKIES_KEY_LIST.contains(cookie.getName())) {
					cookie.setMaxAge(30 * 60);
					cookie.setHttpOnly(true);
					cookie.setValue(ValidationUtils.cleanXSS(cookie.getValue()));
					cookieMap.put(cookie.getName(), cookie.getValue());
				} else {
					log.debug("Cookie Name Not found in master cookies list name as " + cookie.getName());
				}
			}
		}
		return cookieMap;
	}

	/**
	 * Check path for canonical and directory traversal.
	 * 
	 * @param path
	 * @return
	 */
	public static boolean checkValidPath(String path) {
		boolean valid = false;
		try {
			log.debug("path " + path);
			log.debug("canonical path " + new File(path).getCanonicalPath());
			//check for canonical path
			if (path.equals(new File(path).getCanonicalPath())) {
				//check directory
				log.debug("canonical path check done--" + path);
				String parts[] = path.split(Pattern.quote(File.separator));
				for (int i = 0; i < parts.length; i++) {
					if (i == 0 && parts[0].equals("")) {
						valid = true;
						continue;
					} else if (!parts[i].equals("")
							&& Pattern.compile("^[a-zA-Z0-9_.:\\-]+").matcher(parts[i]).matches()) {
						valid = true;
						continue;
					} else {
						return false;
					}
				}
			} else {
				log.debug("canonical path check failed--" + path);
			}
		} catch (Exception e) {
			log.error("Not a valid path -- " + e.getStackTrace());
		}
		return valid;
	}

	public static boolean validateFile(String filename) {
		final Pattern pattern;
		try {
			if (filename.contains(".json")) {
				pattern = Pattern.compile(AuthenticationUtils.JSON_FILE_VALIDATOR);
			} else {
				pattern = Pattern.compile(AuthenticationUtils.LOG_FILE_VALIDATOR);
			}
			final Matcher matcher = pattern.matcher(filename);

			if (matcher.find()) {
				log.debug("File name is Valid for regex -- {}", filename);
				return true;
			}
		} catch (Exception e) {
			log.error("Not a valid path -- {}", e.getStackTrace());
		}
		return false;
	}

	public static boolean checkFileForHTML(String fileContent) {
		String strRegEx = "<[^>]*>";
		String replacedContent = "";
		if (fileContent != null) {
			replacedContent = fileContent.replaceAll(strRegEx, "").replace("&nbsp;", " ").replace("&amp;", "&");
		}
		if (!replacedContent.equalsIgnoreCase(fileContent)) {
			log.error(" Invalid response data ");
			return false;
		}
		return true;
	}

	/**
	 * This Method is use prepare Grafana Header based on Request cookies and other
	 * value
	 * 
	 * @return Map of Grafana Header
	 * @throws InsightsCustomException 
	 */
	public static Map<String, String> prepareGrafanaHeader(HttpServletRequest httpRequest) throws InsightsCustomException {
		Map<String, String> headers = new HashMap<>();
		if (!AuthenticationUtils.IS_NATIVE_AUTHENTICATION) {
			String webAuthHeaderKey = ValidationUtils
					.cleanXSS(httpRequest.getHeader(AuthenticationUtils.GRAFANA_WEBAUTH_HEADER_KEY));
			log.debug(" x-webauth-user ==== {} ", webAuthHeaderKey);
			headers.put(AuthenticationUtils.GRAFANA_WEBAUTH_USERKEY, webAuthHeaderKey);
			headers.put(AuthenticationUtils.GRAFANA_WEBAUTH_USERKEY_NAME, webAuthHeaderKey);
		}
		String grafanaCookie = PlatformServiceUtil.getUserCookiesFromRequestCookies(httpRequest);
		headers.put("Cookie", grafanaCookie);
		return headers;
	}

	/**
	 * This method used to return grafana cookies string based on request cookies
	 * 
	 * @return string of cookies
	 * @throws InsightsCustomException 
	 */
	public static String getUserCookiesFromRequestCookies(HttpServletRequest httpRequest) throws InsightsCustomException {
		Map<String, String> requestCookies = PlatformServiceUtil.getRequestCookies(httpRequest);
		if (requestCookies.isEmpty() || !requestCookies.containsKey(AuthenticationUtils.GRAFANA_SESSION_COOKIE_KEY)) {
			if (AuthenticationUtils.IS_NATIVE_AUTHENTICATION) {
				try {
					requestCookies = getGrafanaCookies(httpRequest);
				} catch (UnsupportedEncodingException e) {
					log.error("Unable to get grafana session. Error is {} ", e);
				}
			}
		}
		String grafanaCookies = requestCookies.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
				.collect(Collectors.joining(";"));
		log.debug(" grafanaCookies ==== {} ", grafanaCookies);
		return grafanaCookies;
	}

	public static Map<String, String> getGrafanaCookies(HttpServletRequest httpRequest)
			throws UnsupportedEncodingException, InsightsCustomException {
		GrafanaHandler grafanaHandler = new GrafanaHandler();
		Map<String, String> requestCookies = new HashMap<>(0);
		String authHeader = ValidationUtils
				.decryptAutharizationToken(httpRequest.getHeader(AuthenticationUtils.AUTH_HEADER_KEY));
		String decodedAuthHeader = new String(Base64.getDecoder().decode(authHeader.split(" ")[1]), "UTF-8");
		String[] authTokens = decodedAuthHeader.split(":");
		JsonObject loginRequestParams = new JsonObject();
		loginRequestParams.addProperty("user", authTokens[0]);
		loginRequestParams.addProperty("password", authTokens[1]);
		String loginApiUrl = "/login";
		List<NewCookie> cookies2 = grafanaHandler.getGrafanaCookies(loginApiUrl, loginRequestParams, null);
		for (NewCookie cookie : cookies2) {
			requestCookies.put(cookie.getName(), ValidationUtils.cleanXSS(cookie.getValue()).concat("; HttpOnly"));
		}
		return requestCookies;
	}

	/*public static String getGrafanaURL(String urlPart) {
		return ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint() + urlPart;
	}*/
}
