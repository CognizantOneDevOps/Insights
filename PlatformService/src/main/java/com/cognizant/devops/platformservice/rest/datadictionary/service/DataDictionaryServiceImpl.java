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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformdal.agentConfig.AgentConfig;
import com.cognizant.devops.platformdal.agentConfig.AgentConfigDAL;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfig;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfigDAL;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Service("dataDictionaryService")
public class DataDictionaryServiceImpl implements DataDictionaryService {
	GraphDBHandler GraphDBHandler = new GraphDBHandler();
	private static Logger log = LogManager.getLogger(DataDictionaryServiceImpl.class);

	@Override
	public JsonObject getToolsAndCategories() {
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		WebHookConfigDAL webhookConfigDal = new WebHookConfigDAL();
		JsonArray uniqueToolDetailArray = new JsonArray();
		Set<JsonObject> uniquetoolDetailSet = new HashSet<>();

		try {
			List<AgentConfig> agentConfigList = agentConfigDAL.getAllDataAgentConfigurations();
			List<WebHookConfig> webHookConfigList = webhookConfigDal.getAllWebHookConfigurations();
			Iterator<AgentConfig> iteratorAgent = agentConfigList.iterator();
			Iterator<WebHookConfig> iteratorWebhook = webHookConfigList.iterator();
			while (iteratorAgent.hasNext()) {
				AgentConfig configDetails = iteratorAgent.next();
				JsonObject toolsDetailJson = new JsonObject();
				toolsDetailJson.addProperty("toolName", configDetails.getToolName().toUpperCase());
				toolsDetailJson.addProperty("categoryName", configDetails.getToolCategory().toUpperCase());
				toolsDetailJson.addProperty("labelName", configDetails.getLabelName());
				uniquetoolDetailSet.add(toolsDetailJson);
			}
			while (iteratorWebhook.hasNext()) {
				WebHookConfig configDetails = iteratorWebhook.next();
				JsonObject webhookDetailJson = new JsonObject();
				webhookDetailJson.addProperty("toolName", configDetails.getToolName().toUpperCase());
				
				if (configDetails.getLabelName().split(":").length == 0 ||  configDetails.getLabelName().split(":").length == 1) {
					webhookDetailJson.addProperty("categoryName", configDetails.getLabelName());
					webhookDetailJson.addProperty("labelName", configDetails.getLabelName());
				} else {
					webhookDetailJson.addProperty("categoryName", configDetails.getLabelName().split(":", 0)[0]);
					webhookDetailJson.addProperty("labelName", configDetails.getLabelName().split(":", 0)[1]);
				}
				uniquetoolDetailSet.add(webhookDetailJson);

			}
			for (JsonObject uniqueObject : uniquetoolDetailSet) {
				uniqueToolDetailArray.add(uniqueObject);
			}
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse("Unable to fetch the tool details.");
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(uniqueToolDetailArray);
	}

	@Override
	public JsonObject getToolProperties(String labelName, String categoryName) {
		JsonArray keysArrayJson = new JsonArray();
		try {
			String toolPropertiesQuery = DataDictionaryConstants.GET_TOOL_PROPERTIES_QUERY;
			GraphResponse graphResponse = GraphDBHandler.executeCypherQuery(
					toolPropertiesQuery.replace("__labelName__", labelName).replace("__CategoryName__", categoryName));
			JsonObject jsonResponse = graphResponse.getJson();
			Iterator<JsonElement> iterator = jsonResponse.get("results").getAsJsonArray().iterator().next()
					.getAsJsonObject().get("data").getAsJsonArray().iterator().next().getAsJsonObject().get("row")
					.getAsJsonArray().iterator().next().getAsJsonArray().iterator();
			while (iterator.hasNext()) {
				String element = iterator.next().getAsString();
				if (!(element.equalsIgnoreCase(DataDictionaryConstants.EXEC_ID)
						|| element.equalsIgnoreCase(DataDictionaryConstants.UUID))) {
					keysArrayJson.add(element);
				}
			}
			if (keysArrayJson.size() > 0) {
				return PlatformServiceUtil.buildSuccessResponseWithData(keysArrayJson);
			} else {
				return PlatformServiceUtil.buildFailureResponse("No Data found.");
			}
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse("Properties of the label could not be fetched from Neo4j");
		}
	}

	@Override
	public JsonObject getToolsRelationshipAndProperties(String startLabelName, String startToolCategory,
			String endLabelName, String endToolCatergory) {
		JsonArray toolsRealtionJson = new JsonArray();
		try {
			String toolsRelationshipQuery = DataDictionaryConstants.GET_TOOLS_RELATIONSHIP_QUERY;
			GraphResponse graphResponse = GraphDBHandler.executeCypherQuery(toolsRelationshipQuery
					.replace("__StartToolCategory__", startToolCategory).replace("__StartLabelName__", startLabelName)
					.replace("__EndToolCategory__", endToolCatergory).replace("__EndLabelName__", endLabelName));
			JsonObject jsonResponse = graphResponse.getJson();
			Iterator<JsonElement> dataIterator = jsonResponse.get("results").getAsJsonArray().iterator().next()
					.getAsJsonObject().get("data").getAsJsonArray().iterator();

			while (dataIterator.hasNext()) {
				Iterator<JsonElement> rowIterator = dataIterator.next().getAsJsonObject().get("row").getAsJsonArray()
						.iterator();
				while (rowIterator.hasNext()) {
					String relationName = rowIterator.next().getAsString();
					JsonObject relationJson = new JsonObject();
					relationJson.addProperty("relationName", relationName);
					toolsRealtionJson.add(relationJson);
				}
			}
			return PlatformServiceUtil.buildSuccessResponseWithData(toolsRealtionJson);
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		
	}
}
