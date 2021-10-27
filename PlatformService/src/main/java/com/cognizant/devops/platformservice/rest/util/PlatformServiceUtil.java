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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.NewCookie;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.enums.FileDetailsEnum;
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
		JsonObject validatedData = ValidationUtils.replaceHTMLContentFormString(jsonResponse);
		if (validatedData == null) {
			validatedData = buildFailureResponse(PlatformServiceConstants.INVALID_RESPONSE_DATA);
		}
		return validatedData;
	}

	public static JsonObject buildFailureResponse(String message) {
		log.error("Error while running API message {} ",message);
		JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty(PlatformServiceConstants.STATUS, PlatformServiceConstants.FAILURE);
		jsonResponse.addProperty(PlatformServiceConstants.MESSAGE, message);
		return jsonResponse;
	}

	public static JsonObject buildFailureResponseWithStatusCode(String message, String statusCode) {
		log.error("Error while running API statusCode {} message {} ",statusCode,message);
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


	public static Cookie[] validateCookies(Cookie[] requestcookies) {
		Cookie[] cookiesArray = null;
		Cookie cookie = null;
		int cookiesArrayLength = 0;
		List<Cookie> cookiesList = new ArrayList<>();
		JsonObject cookiesValue = new JsonObject();
		if (requestcookies != null) {
			for (int i = 0; i < requestcookies.length; i++) {
				cookie = requestcookies[i];
				cookiesValue.addProperty(cookie.getName(), cookie.getValue());
				if (AuthenticationUtils.MASTER_COOKIES_KEY_LIST.contains(cookie.getName())) {
					cookie.setMaxAge(30 * 60);
					cookie.setHttpOnly(true);
					cookie.setValue(ValidationUtils.cleanXSSWithHTMLCheck(cookie.getValue()));
					cookiesList.add(cookie);
					cookiesArrayLength = cookiesArrayLength + 1;
				} else {
					log.debug("Cookie Name Not found in master cookies list name as {}", cookie.getName());
				}
			}
			cookiesArray = new Cookie[cookiesArrayLength];
			cookiesArray = cookiesList.toArray(cookiesArray);
		} else {
			cookiesArray = requestcookies;
			log.warn("No cookies founds");
		}
		return cookiesArray;
	}

	public static Map<String, String> getRequestCookies(HttpServletRequest httpRequest) {
		Map<String, String> cookieMap = new HashMap<>(0);
		Cookie cookie = null;
		Cookie[] cookiesList = httpRequest.getCookies();
		if (cookiesList != null) {
			for (int i = 0; i < cookiesList.length; i++) {
				cookie = cookiesList[i];
				if (AuthenticationUtils.MASTER_COOKIES_KEY_LIST.contains(cookie.getName())) {
					cookie.setMaxAge(30 * 60);
					cookie.setHttpOnly(true);
					cookie.setValue(ValidationUtils.cleanXSS(cookie.getValue()));
					cookieMap.put(cookie.getName(), cookie.getValue());
				} else {
					log.debug("Cookie Name Not found in master cookies list name as {} ", cookie.getName());
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
			log.debug("path {}", path);
			log.debug("canonical path {}",new File(path).getCanonicalPath());
			//check for canonical path
			if (path.equals(new File(path).getCanonicalPath())) {
				//check directory
				log.debug("canonical path check done- {} ", path);
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
				log.debug("canonical path check failed- {}",  path);
			}
		} catch (Exception e) {
			log.error("Not a valid path -- ", e);
		}
		return valid;
	}

	public static boolean validateFile(String filename) {
		Pattern pattern = null;
		try {
			String fileExt = FilenameUtils.getExtension(filename);
			if (fileExt.equalsIgnoreCase("json")) {
				pattern = Pattern.compile(AuthenticationUtils.JSON_FILE_VALIDATOR);
			} else if (filename.contains(".html")) {
				pattern = Pattern.compile(AuthenticationUtils.HTML_FILE_VALIDATOR);
			} else if (fileExt.equalsIgnoreCase("log")) {
				pattern = Pattern.compile(AuthenticationUtils.LOG_FILE_VALIDATOR);
			} else if (fileExt.equalsIgnoreCase("csv")) {
				pattern = Pattern.compile(AuthenticationUtils.CSV_FILE_VALIDATOR);
			} else if (fileExt.equalsIgnoreCase("css")) {
				pattern = Pattern.compile(AuthenticationUtils.CSS_FILE_VALIDATOR);
			} else if (fileExt.equalsIgnoreCase("webp")) {
				pattern = Pattern.compile(AuthenticationUtils.WEBP_FILE_VALIDATOR);
			}
			if (pattern != null) {
				final Matcher matcher = pattern.matcher(filename);
				if (matcher.find()) {
					log.debug("File name is Valid for regex -- {}", filename);
					return true;
				}
			}
		} catch (Exception e) {
			log.error("Not a valid path -- ", e);
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
			log.debug(" x-webauth-user {} ==== ", webAuthHeaderKey);
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
				} catch (Exception e) {
					log.error("Unable to get grafana session. Error is ", e);
				}
			}
		}
		String grafanaCookies="";
		for (Entry<String, String> entry : requestCookies.entrySet()) {
			if(!AuthenticationUtils.IS_NATIVE_AUTHENTICATION
					&& entry.getKey().equalsIgnoreCase(AuthenticationUtils.GRAFANA_WEBAUTH_USERKEY_NAME)) {
				continue;
			} else {
			 grafanaCookies = grafanaCookies.concat(entry.getKey()).concat("=").concat(entry.getValue()).concat(";") ;
			}
		}
		return grafanaCookies;
	}

	public static Map<String, String> getGrafanaCookies(HttpServletRequest httpRequest)
			throws InsightsCustomException {
		GrafanaHandler grafanaHandler = new GrafanaHandler();
		Map<String, String> requestCookies = new HashMap<>(0);
		String authHeader = ValidationUtils
				.decryptAutharizationToken(AuthenticationUtils.extractAndValidateAuthToken(httpRequest));
		String decodedAuthHeader = new String(Base64.getDecoder().decode(authHeader.split(" ")[1]), StandardCharsets.UTF_8);
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

	/**
	 * Method to get FileType
	 * 
	 * @param fileName
	 * @return String
	 * @throws InsightsCustomException
	 */
	public static String getFileType(String fileName) throws InsightsCustomException {
		String fileExtension = FilenameUtils.getExtension(fileName);
		if(fileExtension.equalsIgnoreCase(FileDetailsEnum.ReportTemplateFileType.CSS.name())) {
			return FileDetailsEnum.ReportTemplateFileType.CSS.name();
		}else if(fileExtension.equalsIgnoreCase(FileDetailsEnum.ReportTemplateFileType.HTML.name())) {
			return FileDetailsEnum.ReportTemplateFileType.HTML.name();
		}else if(fileExtension.equalsIgnoreCase(FileDetailsEnum.ReportTemplateFileType.JSON.name())) {
			return FileDetailsEnum.ReportTemplateFileType.JSON.name();
		}else if(fileExtension.equalsIgnoreCase(FileDetailsEnum.ReportTemplateFileType.WEBP.name())) {
			return FileDetailsEnum.ReportTemplateFileType.WEBP.name();
		}
		else {
			throw new InsightsCustomException("FileType not correct.");
		}
	}
	
	/**
	 * Method to check for Script tag in file
	 * 
	 * @param file
	 * @return boolean
	 * @throws IOException
	 */
	public static boolean checkScriptTag(File file) throws IOException {
		String fileData = FileUtils.readFileToString(file);
		if (fileData.contains("<script>")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * convert multipart to file
	 * 
	 * @param multipartFile
	 * @return
	 * @throws IOException
	 */
	public static File convertToFile(MultipartFile multipartFile) throws IOException {
		File file = new File(multipartFile.getOriginalFilename());
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(multipartFile.getBytes());
		}
		return file;
	}
}
