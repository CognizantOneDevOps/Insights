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
package com.cognizant.devops.platformcommons.core.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.dal.RestApiHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import sun.misc.BASE64Encoder;

public class SystemStatus {
	private static Logger log = LogManager.getLogger(SystemStatus.class.getName());

	public static JsonObject addSystemInformationInNeo4j(String version, List<JsonObject> dataList,
			List<String> labels) {
		GraphDBHandler graphDBHandler = new GraphDBHandler();
		JsonObject response = null;
		try {
			String queryLabel = "";
			for (String label : labels) {
				if (label != null && label.trim().length() > 0) {
					queryLabel += ":" + label;
				}
			}

			String cypherQuery = "CREATE (n" + queryLabel + " {props} ) return count(n)";

			response = graphDBHandler.executeQueryWithData(cypherQuery, dataList);

		} catch (Exception e) {
			log.error(" Neo4j Node not created " + e.getMessage());
		}

		return response;
	}

	public static String jerseyGetClientWithAuthentication(String url, String name, String password, String authtoken) throws InsightsCustomException {
		String response = null;
		String authStringEnc;
		if (authtoken == null) {
			String authString = name + ":" + password;
			authStringEnc = new BASE64Encoder().encode(authString.getBytes());
		} else {
			authStringEnc = authtoken;
		}
		String headerValue = "Basic " + authStringEnc;
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", headerValue);
		return RestApiHandler.doGet(url, headers);
	}

	public static String jerseyPostClientWithAuthentication(String url, String name, String password,
			String authtoken, String data) throws InsightsCustomException {
		String authStringEnc;
		if (authtoken == null) {
			String authString = name + ":" + password;
			authStringEnc = new BASE64Encoder().encode(authString.getBytes());
		} else {
			authStringEnc = authtoken;
		}
		JsonObject requestJson = new  JsonParser().parse(data).getAsJsonObject();
		JsonArray properties = new JsonArray();
		String headerValue = "Basic " + authStringEnc;
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", headerValue);
		return RestApiHandler.doPost(url, requestJson, headers);
	}
}
