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
package com.cognizant.devops.platformengine.modules.aggregator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.NodeData;
import com.cognizant.devops.platformdal.agentConfig.AgentConfig;
import com.cognizant.devops.platformdal.agentConfig.AgentConfigDAL;
import com.cognizant.devops.platformengine.message.core.EngineStatusLogger;
import com.cognizant.devops.platformengine.message.factory.EngineSubscriberResponseHandler;
import com.cognizant.devops.platformengine.message.subscriber.AgentDataSubscriber;
import com.cognizant.devops.platformengine.message.subscriber.AgentHealthSubscriber;
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
public class EngineAggregatorModule implements Job {
	private static Logger log = LogManager.getLogger(EngineAggregatorModule.class.getName());
	private static Map<String, EngineSubscriberResponseHandler> registry = new HashMap<String, EngineSubscriberResponseHandler>();


	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		ApplicationConfigProvider.performSystemCheck();
		Neo4jDBHandler graphDBHandler = new Neo4jDBHandler();
		AgentConfigDAL agentConfigDal = new AgentConfigDAL();
		List<AgentConfig> allAgentConfigurations = agentConfigDal.getAllAgentConfigurations();
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
	}


	private void registerAggragators(AgentConfig agentConfig, Neo4jDBHandler graphDBHandler, String toolName,
			List<BusinessMappingData> businessMappingList) {
		try {
			JsonObject config = (JsonObject) new JsonParser().parse(agentConfig.getAgentJson());
			JsonObject json = config.get("publish").getAsJsonObject();
			String dataRoutingKey = json.get("data").getAsString();

			log.debug(" dataRoutingKey " + dataRoutingKey + "  Tool Info " + toolName);

			if (dataRoutingKey != null && !registry.containsKey(dataRoutingKey)) {
				try {
					registry.put(dataRoutingKey,
							new AgentDataSubscriber(dataRoutingKey, agentConfig.isDataUpdateSupported(),
									agentConfig.getUniqueKey(), agentConfig.getToolCategory(),
									toolName, businessMappingList));
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

	private Map<String, List<BusinessMappingData>> getMetaData(Neo4jDBHandler dbHandler) {
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
			// log.info("arg0 tool node responce " + gson.toJson(toolResponse));
			JsonArray arrayToolRegistred= toolResponse.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
					.getAsJsonArray().get(0).getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonArray();
			
			/* arrayToolRegistred.forEach(a -> log.info("tool info " + a)); */
			// arrayToolRegistred
			for (JsonElement registedTool : arrayToolRegistred) {
				String toolName = registedTool.toString().replaceAll("\"", "");
				/* log.info("tool info toolName " + toolName); */
				GraphResponse response = dbHandler.executeCypherQuery(
						"MATCH (n:METADATA:BUSINESSMAPPING) where n.toolName='" + toolName
								+ "' return n order by n.inSightsTime desc");
				nodes = response.getNodes();
				List<BusinessMappingData> toolDataList = new ArrayList<BusinessMappingData>(0);
				for (NodeData node : nodes) {
					// JsonElement json = new JsonParser().parse(node.getProperty("propertyMap"));
					BusinessMappingData toolData = new BusinessMappingData();
					String jsonString = gson.toJson(node);
					/* log.info("arg0  node " + jsonString); */
					String businessMappingLabel = node.getPropertyMap().get("businessmappinglabel");
					/*
					 * log.info("toolName  === " + toolName + "  businessMappingLabel === " +
					 * businessMappingLabel);
					 */
					toolData.setToolName(toolName);
					toolData.setBusinessMappingLabel(businessMappingLabel);

					Map<String, String> propertyMap = node.getPropertyMap();
					propertyMap.keySet().removeAll(additionalProperties);

					toolData.setPropertyMap(propertyMap);
					/* log.info("arg0 toolData  " + toolData); */
					/*
					 * StringBuilder labelVal=new StringBuilder(); StringBuilder key=new
					 * StringBuilder(); nodepropertyMap=new HashMap<String,NodeData>();
					 * nodepropertyMap.put(
					 * StringUtils.stripEnd(labelVal.toString(),AgentDataConstants.COLON), node);
					 * metaDataMap.put(StringUtils.stripEnd(key.toString(),AgentDataConstants.COLON)
					 * , nodepropertyMap);
					 */
					toolDataList.add(toolData);
				}
				log.debug("arg0 toolDataList  " + toolDataList);
				businessMappinMap.put(toolName, toolDataList);
			}
		} catch (GraphDBException e) {
			log.error(e);
		}
		return businessMappinMap;
	}
}