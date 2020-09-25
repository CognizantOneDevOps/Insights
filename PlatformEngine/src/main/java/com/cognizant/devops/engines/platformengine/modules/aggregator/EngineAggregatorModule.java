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
package com.cognizant.devops.engines.platformengine.modules.aggregator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.NodeData;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.agentConfig.AgentConfig;
import com.cognizant.devops.platformdal.agentConfig.AgentConfigDAL;
import com.cognizant.devops.engines.platformengine.message.core.EngineStatusLogger;
import com.cognizant.devops.engines.platformengine.message.factory.EngineSubscriberResponseHandler;
import com.cognizant.devops.engines.platformengine.message.subscriber.AgentDataSubscriber;
import com.cognizant.devops.engines.platformengine.message.subscriber.AgentHealthSubscriber;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 
 * @author 146414 This module will pull the data from Graph and accordingly
 *         subscribe for the incoming data
 */
public class EngineAggregatorModule extends TimerTask {
	private static Logger log = LogManager.getLogger(EngineAggregatorModule.class.getName());
	private static Map<String, EngineSubscriberResponseHandler> registry = new HashMap<String, EngineSubscriberResponseHandler>();

	@Override
	public void run() {
		log.debug(" EngineAggregatorModule start ====");
		ApplicationConfigProvider.performSystemCheck();
		GraphDBHandler graphDBHandler = new GraphDBHandler();
		AgentConfigDAL agentConfigDal = new AgentConfigDAL();
		List<AgentConfig> allAgentConfigurations = agentConfigDal.getAllEngineAggregatorAgentConfigurations();
		boolean enableOnlineDatatagging = ApplicationConfigProvider.getInstance().isEnableOnlineDatatagging();
		Map<String, List<BusinessMappingData>> businessMappinMap = new HashMap<String, List<BusinessMappingData>>(0);
		if (enableOnlineDatatagging) {
			businessMappinMap = getMetaData(graphDBHandler);
		}
		for (AgentConfig agentConfig : allAgentConfigurations) {
			String toolName = agentConfig.getToolName().toUpperCase();
			List<BusinessMappingData> businessMappingList = businessMappinMap.get(toolName);
			if (businessMappingList == null) {
				businessMappingList = new ArrayList<BusinessMappingData>(0);
			}
			registerAggragators(agentConfig, graphDBHandler, toolName, businessMappingList);
		}
		log.debug(" EngineAggregatorModule Completed ====");
	}

	private void registerAggragators(AgentConfig agentConfig, GraphDBHandler graphDBHandler, String toolName,
			List<BusinessMappingData> businessMappingList) {
			Boolean isEnrichmentRequired= false;
		String targetProperty="";
		String keyPattern="";
		String sourceProperty ="";
		try {
			JsonObject config = (JsonObject) new JsonParser().parse(agentConfig.getAgentJson());
			JsonObject json = config.get("publish").getAsJsonObject();
			String dataRoutingKey = json.get("data").getAsString();
			Boolean hasenrichTool = config.has("enrichData");
			
			
			if (hasenrichTool) {
				JsonObject enrichTool = config.get("enrichData").getAsJsonObject();
				isEnrichmentRequired = enrichTool.get("isEnrichmentRequired").getAsBoolean();
				targetProperty = enrichTool.get("targetProperty").getAsString();
				keyPattern = enrichTool.get("keyPattern").getAsString();
				sourceProperty = enrichTool.get("sourceProperty").getAsString();
			}
			log.debug(" dataRoutingKey " + dataRoutingKey + "  Tool Info " + toolName);

			if (dataRoutingKey != null && !registry.containsKey(dataRoutingKey)) {
				try {
					registry.put(dataRoutingKey, new AgentDataSubscriber(dataRoutingKey,agentConfig.isDataUpdateSupported(), agentConfig.getUniqueKey(),
							agentConfig.getToolCategory(), agentConfig.getLabelName(), toolName, businessMappingList,isEnrichmentRequired, targetProperty,
							keyPattern,sourceProperty));
				} catch (Exception e) {
					log.error("Unable to add subscriber for routing key: " + dataRoutingKey, e);
					EngineStatusLogger.getInstance().createEngineStatusNode(
							" Error occured while executing aggragator for data queue subscriber " + e.getMessage(),
							PlatformServiceConstants.FAILURE);
				}
				EngineStatusLogger.getInstance().createEngineStatusNode(
						" Agent data queue " + dataRoutingKey + " subscribed successfully ",
						PlatformServiceConstants.SUCCESS);
			} else if (registry.containsKey(dataRoutingKey)) {
				AgentDataSubscriber dataSubscriber = (AgentDataSubscriber) registry.get(dataRoutingKey);
				dataSubscriber.setMappingData(businessMappingList);
			}

			String healthRoutingKey = json.get("health").getAsString();
			if (healthRoutingKey != null && !registry.containsKey(healthRoutingKey)) {
				// Make sure that default health node is initialized
				String nodeLabels = ":LATEST:" + healthRoutingKey.replace(".", ":");
				try {
					graphDBHandler.executeCypherQuery("MERGE (n" + nodeLabels + ") return n");
					registry.put(healthRoutingKey, new AgentHealthSubscriber(healthRoutingKey));
				} catch (Exception e) {
					log.error("Unable to add subscriber for routing key: " + healthRoutingKey, e);
					EngineStatusLogger.getInstance().createEngineStatusNode(
							" Error occured while executing aggragator for health queue subscriber  " + e.getMessage(),
							PlatformServiceConstants.FAILURE);
				}
				EngineStatusLogger.getInstance().createEngineStatusNode(
						" Agent health queue " + healthRoutingKey + " subscribed successfully ",
						PlatformServiceConstants.SUCCESS);
			}

		} catch (Exception e) {
			log.error("Unable to add subscriber for routing key: " + agentConfig.getAgentKey(), e);
			EngineStatusLogger.getInstance().createEngineStatusNode(
					" Error occured while executing aggragator  " + agentConfig.getAgentKey() + e.getMessage(),
					PlatformServiceConstants.FAILURE);
		}
	}

	/*
	 * public boolean deregisterAggregator(String key){
	 * EngineSubscriberResponseHandler engineSubscriberResponseHandler =
	 * registry.get(key); if(engineSubscriberResponseHandler != null){ try {
	 * MessageSubscriberFactory.getInstance().unregisterSubscriber(key,
	 * engineSubscriberResponseHandler); } catch (IOException e) { log.error(e); }
	 * catch (TimeoutException e) { log.error(e); } } return false; }
	 */

	private Map<String, List<BusinessMappingData>> getMetaData(GraphDBHandler dbHandler) {
		List<NodeData> nodes = null;
		Map<String, List<BusinessMappingData>> businessMappinMap = new HashMap<String, List<BusinessMappingData>>(0);
		Gson gson = new Gson();
		Set<String> additionalProperties = new HashSet<>();
		additionalProperties.add("adminuser");
		additionalProperties.add("inSightsTime");
		additionalProperties.add("uuid");
		additionalProperties.add("inSightsTime");
		additionalProperties.add("inSightsTimeX");
		additionalProperties.add("categoryName");
		additionalProperties.add("deleted");
		additionalProperties.add("type");
		additionalProperties.add("businessmappinglabel");
		additionalProperties.add("toolName");
		additionalProperties.add("id");
		try {
			GraphResponse toolResponse = dbHandler
					.executeCypherQuery("MATCH (n:METADATA:BUSINESSMAPPING) return collect(distinct n.toolName)");
			JsonArray arrayToolRegistred = toolResponse.getJson().get("results").getAsJsonArray().get(0)
					.getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("row").getAsJsonArray()
					.get(0).getAsJsonArray();

			// arrayToolRegistred
			for (JsonElement registedTool : arrayToolRegistred) {
				String toolName = registedTool.toString().replaceAll("\"", "");
				GraphResponse response = dbHandler
						.executeCypherQuery("MATCH (n:METADATA:BUSINESSMAPPING) where n.toolName='" + toolName
								+ "' return n order by n.inSightsTime desc");
				nodes = response.getNodes();
				List<BusinessMappingData> toolDataList = new ArrayList<BusinessMappingData>(0);
				for (NodeData node : nodes) {

					BusinessMappingData toolData = new BusinessMappingData();
					String jsonString = gson.toJson(node);
					String businessMappingLabel = node.getPropertyMap().get("businessmappinglabel");
					toolData.setToolName(toolName);
					toolData.setBusinessMappingLabel(businessMappingLabel);
					Map<String, String> propertyMap = node.getPropertyMap();
					propertyMap.keySet().removeAll(additionalProperties);
					toolData.setPropertyMap(propertyMap);
					toolDataList.add(toolData);
				}
				log.debug("arg0 toolDataList  " + toolDataList);
				businessMappinMap.put(toolName, toolDataList);
			}
		} catch (InsightsCustomException e) {
			log.error(e);
		}
		return businessMappinMap;
	}
}