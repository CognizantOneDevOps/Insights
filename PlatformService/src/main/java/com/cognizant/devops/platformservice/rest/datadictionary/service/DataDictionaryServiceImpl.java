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

package com.cognizant.devops.platformservice.rest.datadictionary.service;

import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformdal.agentConfig.AgentConfig;
import com.cognizant.devops.platformdal.agentConfig.AgentConfigDAL;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service("dataDictionaryService")
public class DataDictionaryServiceImpl implements DataDictionaryService {
	Neo4jDBHandler neo4jDBHandler = new Neo4jDBHandler();
	private static Logger log = LogManager.getLogger(DataDictionaryServiceImpl.class);

	@Override
	public JsonObject getToolsAndCategories() {
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		JsonObject toolsLabelsJson = new JsonObject();
		JsonArray toolDetailArray = new JsonArray();
		try {
			List<AgentConfig> agentConfigList = agentConfigDAL.getAllDataAgentConfigurations();
			Iterator<AgentConfig> iterator = agentConfigList.iterator();
			while (iterator.hasNext()) {
				AgentConfig configDetails = iterator.next();
				JsonObject toolsDetailJson = new JsonObject();
				toolsDetailJson.addProperty("toolName", configDetails.getToolName().toUpperCase());
				toolsDetailJson.addProperty("categoryName", configDetails.getToolCategory().toUpperCase());
				toolDetailArray.add(toolsDetailJson);
				System.out.println("c");
			}
			toolsLabelsJson.add("data", toolDetailArray);

		} catch (Exception e) {
			log.error("Error Caught while capturing Graph Response!" + e);
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(toolsLabelsJson.get("data"));
	}

	@Override
	public JsonObject getToolProperties(String toolName, String categoryName) {
		JsonObject toolKeysJson = new JsonObject();
		StringBuilder stringBuilder = new StringBuilder();
		try {
			String toolPropertiesQuery = DataDictionaryConstants.GET_TOOL_PROPERTIES_QUERY;
			GraphResponse graphResponse = neo4jDBHandler.executeCypherQuery(
					toolPropertiesQuery.replace("__ToolName__", toolName).replace("__CategoryName__", categoryName));
			JsonObject jsonResponse = graphResponse.getJson();
			Iterator<JsonElement> iterator = jsonResponse.get("results").getAsJsonArray().iterator().next()
					.getAsJsonObject().get("data").getAsJsonArray().iterator().next().getAsJsonObject().get("row")
					.getAsJsonArray().iterator().next().getAsJsonArray().iterator();
			while (iterator.hasNext()) {
				String element = iterator.next().getAsString();
				if (!(element.equalsIgnoreCase(DataDictionaryConstants.EXEC_ID)
						|| element.equalsIgnoreCase(DataDictionaryConstants.UUID)))
					stringBuilder = stringBuilder.append(element).append(",");
			}
			String[] keysArray = stringBuilder.toString().split(",");
			Gson gson = new GsonBuilder().create();
			String keysArrayStr = gson.toJson(keysArray);
			JsonParser parser = new JsonParser();
			JsonArray keysArrayJson = parser.parse(keysArrayStr).getAsJsonArray();
			toolKeysJson.add("data", keysArrayJson);
		} catch (Exception e) {
			log.error("Error Caught while capturing Graph Response!" + e);
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(toolKeysJson.get("data"));
	}

	@Override
	public JsonObject getToolsRelationshipAndProperties(String startToolName, String startToolCategory,
			String endToolName, String endToolCatergory) {
		JsonObject toolsRealtionJson = new JsonObject();
		try {
			String toolsRelationshipQuery = DataDictionaryConstants.GET_TOOLS_RELATIONSHIP_QUERY;
			GraphResponse graphResponse = neo4jDBHandler.executeCypherQuery(toolsRelationshipQuery
					.replace("__StartToolCategory__", startToolCategory).replace("__StartToolName__", startToolName)
					.replace("__EndToolCategory__", endToolCatergory).replace("__EndToolName__", endToolName));
			JsonObject jsonResponse = graphResponse.getJson();
			Iterator<JsonElement> iterator = jsonResponse.get("results").getAsJsonArray().iterator().next()
					.getAsJsonObject().get("data").getAsJsonArray().iterator().next().getAsJsonObject().get("row")
					.getAsJsonArray().iterator();

			JsonObject dataJson = new JsonObject();
			String relationName = iterator.next().getAsString();
			dataJson.addProperty("relationName", relationName);
			dataJson.add("properties", iterator.next().getAsJsonObject());
			toolsRealtionJson.add("data", dataJson);
		} catch (Exception e) {
			log.error("Error Caught while capturing Graph Response!" + e);
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(toolsRealtionJson.get("data"));
	}
}
