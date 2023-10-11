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

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.dal.RestApiHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.JsonObject;

public class SystemStatus {
	private static Logger log = LogManager.getLogger(SystemStatus.class.getName());

	public static JsonObject addSystemInformationInNeo4j( List<JsonObject> dataList,
			List<String> labels) throws Exception {
		
		JsonObject response = null;
		try(GraphDBHandler graphDBHandler = new GraphDBHandler()) {
			StringBuilder queryLabel = new StringBuilder();
			for (String label : labels) {
				if (label != null && label.trim().length() > 0) {
					queryLabel.append(":");
					queryLabel.append(label);
				}
			}			
			String cypherQuery = "CREATE (n" + queryLabel + " $props ) return count(n)";
			response = graphDBHandler.executeQueryWithData(cypherQuery, dataList);

		} catch (Exception e) {
			log.error(" Neo4j Node not created{} " , e.getMessage());
			throw e;
		}
		return response;
	}

	public static String jerseyGetClientWithAuthentication(String url, String name, String password, String authtoken) throws InsightsCustomException {
		String response = null;
		String authStringEnc;
		if (authtoken == null) {
			String authString = name + ":" + password;
			authStringEnc = Base64.getEncoder().encodeToString(authString.getBytes());
		} else {
			authStringEnc = authtoken;
		}
		String headerValue = "Basic " + authStringEnc;
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", headerValue);
		return RestApiHandler.doGet(url, headers);
	}
}
