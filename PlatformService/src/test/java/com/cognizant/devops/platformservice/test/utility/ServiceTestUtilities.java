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
package com.cognizant.devops.platformservice.test.utility;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import com.cognizant.devops.platformcommons.dal.grafana.GrafanaHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ServiceTestUtilities {

	public static JsonObject makeServiceRequest(String path, String requestType, JsonElement jsonElement) throws InsightsCustomException {
		GrafanaHandler grafanaHandler = new GrafanaHandler();
		String requestUrl = ServiceTestConstants.BASE_URI + path;

		Map<String, String> requestHeaders = new HashMap<String, String>();
		requestHeaders.put("Authorization", ServiceTestConstants.AUTH);
		requestHeaders.put("Accept", MediaType.APPLICATION_JSON);
		String response = null;
		JsonObject jsonObj = new JsonObject();
		if ("GET".equalsIgnoreCase(requestType)) {
			response = grafanaHandler.grafanaGet(requestUrl, requestHeaders);
		} else if ("POST".equalsIgnoreCase(requestType)) {
			response = grafanaHandler.grafanaPost(requestUrl, jsonElement, requestHeaders);
		}

		/*if (null != response) {
			if (response.getStatusInfo().getStatusCode() == 200) {
				String jsonResp = response.getEntity(String.class);
				JsonParser jsonParser = new JsonParser();
				jsonObj = (JsonObject) jsonParser.parse(jsonResp);
			} else {
				jsonObj.addProperty("status","failure");
				jsonObj.add("statusInfo", new Gson().toJsonTree(response.getStatusInfo()));
			}
		}*/
		
		if( null != response ){
    		String jsonResp = response;
    		JsonParser jsonParser = new JsonParser();
    		jsonObj = (JsonObject)jsonParser.parse(jsonResp);
    	} else {
			jsonObj.addProperty("status","failure");
		}

		return jsonObj;
	}

}
