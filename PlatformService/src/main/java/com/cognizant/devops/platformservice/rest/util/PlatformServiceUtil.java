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
package com.cognizant.devops.platformservice.rest.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class PlatformServiceUtil {
	private static final Logger log = LogManager.getLogger(PlatformServiceUtil.class);
	private static final String[] SET_VALUES = new String[] { "grafanaOrg", "grafana_user", "grafanaRole",
			"grafana_remember", "grafana_sess", "XSRF-TOKEN", "JSESSIONID","grafana_session" };
	private static final Set<String> masterCookiesList = new HashSet<String>(Arrays.asList(SET_VALUES));
	private PlatformServiceUtil(){
		
	}
	
	public static JsonObject buildFailureResponse(String message){
		JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty(PlatformServiceConstants.STATUS, PlatformServiceConstants.FAILURE);
		jsonResponse.addProperty(PlatformServiceConstants.MESSAGE, message);
		return jsonResponse;
	}
	
	public static JsonObject buildSuccessResponseWithData(Object data){

		JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty(PlatformServiceConstants.STATUS, PlatformServiceConstants.SUCCESS);
		jsonResponse.add(PlatformServiceConstants.DATA, new Gson().toJsonTree(data));
		JsonObject validatedData = ValidationUtils.validateStringForHTMLContent(jsonResponse);
		if (validatedData == null) {
			validatedData = buildFailureResponse(PlatformServiceConstants.INVALID_RESPONSE_DATA);
		}
		return validatedData;
	}
	
	public static JsonObject buildSuccessResponseWithHtmlData(Object data) {

		JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty(PlatformServiceConstants.STATUS, PlatformServiceConstants.SUCCESS);
		jsonResponse.add(PlatformServiceConstants.DATA, new Gson().toJsonTree(data));
		return jsonResponse;
	}

	public static JsonObject buildSuccessResponse(){
		JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty(PlatformServiceConstants.STATUS, PlatformServiceConstants.SUCCESS);
		return jsonResponse;
	}
	
	public static ClientResponse publishConfigChanges(String host, int port, JsonObject requestJson) {
		WebResource resource = Client.create()
				.resource("http://"+host+":"+port+"/PlatformEngine/refreshAggregators");
		ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
				.type(MediaType.APPLICATION_JSON)
				.entity(requestJson.toString())
				.post(ClientResponse.class);
		return response;
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
				//log.debug(" cookie " + cookie.getName() + " " + cookie.getValue());
				if (masterCookiesList.contains(cookie.getName())) {
					cookie.setMaxAge(30 * 60);
					cookie.setHttpOnly(true);
					cookie.setValue(ValidationUtils.cleanXSS(cookie.getValue()));
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
}
