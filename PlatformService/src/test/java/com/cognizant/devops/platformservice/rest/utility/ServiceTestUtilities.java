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
package com.cognizant.devops.platformservice.rest.utility;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import com.cognizant.devops.platformcommons.dal.rest.RestHandler;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.ClientResponse;

public class ServiceTestUtilities {

	public static JsonObject makeServiceRequest(String path, String requestType, JsonElement jsonElement) {
		String requestUrl = ServiceTestConstants.BASE_URI + path;

		Map<String, String> requestHeaders = new HashMap<String, String>();
		requestHeaders.put("Authorization", ServiceTestConstants.AUTH);
		requestHeaders.put("Accept", MediaType.APPLICATION_JSON);
		ClientResponse response = null;
		JsonObject jsonObj = new JsonObject();
		if ("GET".equalsIgnoreCase(requestType)) {
			response = RestHandler.doGet(requestUrl, null, requestHeaders);
		} else if ("POST".equalsIgnoreCase(requestType)) {
			response = RestHandler.doPost(requestUrl, jsonElement, requestHeaders);
		}

		if (null != response) {
			if (response.getStatusInfo().getStatusCode() == 200) {
				String jsonResp = response.getEntity(String.class);
				System.out.println(jsonResp);
				JsonParser jsonParser = new JsonParser();
				jsonObj = (JsonObject) jsonParser.parse(jsonResp);
			} else {
			System.out.println(response.getStatus()+response.getStatusInfo().getReasonPhrase());
				jsonObj.addProperty("status","failure");
				jsonObj.add("statusInfo", new Gson().toJsonTree(response.getStatusInfo()));
			}
		}

		return jsonObj;
	}

}
