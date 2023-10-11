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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.cognizant.devops.engines.platformengine.message.core.EngineStatusLogger;
import com.cognizant.devops.engines.platformengine.message.factory.EngineSubscriberResponseHandler;
import com.cognizant.devops.engines.platformengine.message.subscriber.AgentDataSubscriber;
import com.cognizant.devops.engines.platformengine.message.subscriber.AgentHealthSubscriber;
import com.cognizant.devops.platformcommons.config.ApplicationConfigInterface;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.EngineConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.NodeData;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.agentConfig.AgentConfig;
import com.cognizant.devops.platformdal.agentConfig.AgentConfigDAL;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * 
 * @author 146414 This module will pull the data from Graph and accordingly
 *         subscribe for the incoming data
 */
public class EngineAggregatorModule implements Job, ApplicationConfigInterface {

	private static Logger log = LogManager.getLogger(EngineAggregatorModule.class.getName());
	private static Map<String, EngineSubscriberResponseHandler> registry = new HashMap<>();
	String jobName="";
	
	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		log.debug(" Engine Scheduled Job ====  Schedular Inside EngineAggregatorModule ");
		long startTime = System.currentTimeMillis();
		jobName=context.getJobDetail().getKey().getName();
		EngineStatusLogger.getInstance().createSchedularTaskStatusNode("EngineAggregatorModule execution Start ",
				PlatformServiceConstants.SUCCESS,jobName);
		try {
			ApplicationConfigInterface.loadConfiguration();
			executeJob();
		} catch (Exception e) {
			log.error(e);
			EngineStatusLogger.getInstance().createSchedularTaskStatusNode("EngineAggregatorModule execution has some issue  ",
					PlatformServiceConstants.ERROR,jobName);			
		}
		long processingTime = System.currentTimeMillis() - startTime  ;
		EngineStatusLogger.getInstance().createSchedularTaskStatusNode("EngineAggregatorModule execution Completed",
				PlatformServiceConstants.SUCCESS,jobName,processingTime);
	}

	
	public void executeJob() {
		log.debug(" Engine Scheduled Job ==== EngineAggregatorModule start ====");
		try(GraphDBHandler graphDBHandler = new GraphDBHandler()) {
			ApplicationConfigInterface.loadConfiguration();
			ApplicationConfigProvider.performSystemCheck();
			AgentConfigDAL agentConfigDal = new AgentConfigDAL();
			List<AgentConfig> allAgentConfigurations = agentConfigDal.getAllEngineAggregatorAgentConfigurations();
			boolean enableOnlineDatatagging = ApplicationConfigProvider.getInstance().isEnableOnlineDatatagging();
			Map<String, List<BusinessMappingData>> businessMappinMap = new HashMap<>(0);
			if (enableOnlineDatatagging) {
				businessMappinMap = getMetaData(graphDBHandler);
			}
			for (AgentConfig agentConfig : allAgentConfigurations) {
				String toolName = agentConfig.getToolName().toUpperCase();
				List<BusinessMappingData> businessMappingList = businessMappinMap.get(toolName);
				if (businessMappingList == null) {
					businessMappingList = new ArrayList<>(0);
				}
				registerAggragators(agentConfig, graphDBHandler, toolName, businessMappingList);
			}
		}catch(InsightsCustomException e ) {
			log.error("Error while loading Engine Aggregator Module ",e);
			EngineStatusLogger.getInstance().createSchedularTaskStatusNode(
					" Error occured while initializing Engine Aggregator Module  " + e.getMessage(),
					PlatformServiceConstants.FAILURE,jobName);
		} catch (Exception ex) {
			log.error("Error while loading Engine Aggregator Module ", ex);
			EngineStatusLogger.getInstance().createSchedularTaskStatusNode(
					" Error occured while initializing Engine Aggregator Module  " + ex.getMessage(),
					PlatformServiceConstants.FAILURE,jobName);
		}
		log.debug(" EngineAggregatorModule Completed ====");
	}

	private void registerAggragators(AgentConfig agentConfig, GraphDBHandler graphDBHandler, String toolName,
			List<BusinessMappingData> businessMappingList) {
		try {
			JsonObject config = JsonUtils.parseStringAsJsonObject(agentConfig.getAgentJson());
			JsonObject json = config.get("publish").getAsJsonObject();
			String dataRoutingKey = json.get("data").getAsString();
			boolean hasenrichTool = config.has("enrichData");
			JsonObject enrichTool = null;
			
			if (hasenrichTool) {
				 enrichTool = config.get("enrichData").getAsJsonObject();
			}
			log.debug(" Type=AgentEngine toolName={} category={} agentId={} routingKey={} dataSize={} execId={} ProcessingTime={} dataRoutingKey {} Tool Info {}" ,toolName,agentConfig.getToolCategory(),agentConfig.getAgentKey(),dataRoutingKey,0,"-",0,dataRoutingKey,toolName);

			if (dataRoutingKey != null && !registry.containsKey(dataRoutingKey)) {
				try {
					registry.put(dataRoutingKey, new AgentDataSubscriber(dataRoutingKey,
							agentConfig.getToolCategory(), agentConfig.getLabelName(), toolName, businessMappingList,agentConfig.getAgentKey(),enrichTool));
					log.debug(" Type=AgentEngine toolName={} category={} agentId={} routingKey={} dataSize={} execId={} ProcessingTime={} Successfully registered data subscriber for routing key: {}  " ,toolName,agentConfig.getToolCategory(),agentConfig.getAgentKey(),dataRoutingKey,0,"-",0,dataRoutingKey);
				} catch (Exception e) {
					log.error(" toolName={} category={} agentId={} routingKey={} Unable to add data subscriber for routing key: {} " ,toolName,agentConfig.getToolCategory(),agentConfig.getAgentKey(),dataRoutingKey, dataRoutingKey, e);
					EngineStatusLogger.getInstance().createSchedularTaskStatusNode(
							" Error occured while executing aggragator for data queue subscriber " + e.getMessage(),
							PlatformServiceConstants.FAILURE,jobName);
				}
				EngineStatusLogger.getInstance().createSchedularTaskStatusNode(
						" Agent data queue " + dataRoutingKey + EngineConstants.SUBSCRIBED,
						PlatformServiceConstants.SUCCESS,jobName);
			} else if (registry.containsKey(dataRoutingKey)) {
				EngineStatusLogger.getInstance().createSchedularTaskStatusNode(
						" Agent data queue already has " + dataRoutingKey + EngineConstants.SUBSCRIBED,
						PlatformServiceConstants.SUCCESS,jobName);
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
					log.debug(" Type=AgentEngine toolName={} category={} agentId={} routingKey={} dataSize={} execId={} ProcessingTime={} Successfully registered health subscriber for routing key: {}  " ,toolName,agentConfig.getToolCategory(),agentConfig.getAgentKey(),healthRoutingKey,0,"-",0,healthRoutingKey);
				} catch (Exception e) {
					log.error(" toolName={} category={} agentId={} routingKey={} Unable to add health subscriber for routing key: {}",toolName,agentConfig.getToolCategory(),agentConfig.getAgentKey(),healthRoutingKey,healthRoutingKey, e);
					EngineStatusLogger.getInstance().createSchedularTaskStatusNode(
							" Error occured while executing aggragator for health queue subscriber  " + e.getMessage(),
							PlatformServiceConstants.FAILURE,jobName);
				}
				EngineStatusLogger.getInstance().createSchedularTaskStatusNode(
						" Agent health queue " + healthRoutingKey + EngineConstants.SUBSCRIBED,
						PlatformServiceConstants.SUCCESS,jobName);
			}

		} catch (Exception e) {
			log.error("Unable to add subscriber for routing key: {}",agentConfig.getAgentKey(), e);
			EngineStatusLogger.getInstance().createSchedularTaskStatusNode(
					" Error occured while executing aggragator  " + agentConfig.getAgentKey() + e.getMessage(),
					PlatformServiceConstants.ERROR,jobName);
		}
	}


	private Map<String, List<BusinessMappingData>> getMetaData(GraphDBHandler dbHandler) {
		List<NodeData> nodes = null;
		Map<String, List<BusinessMappingData>> businessMappinMap = new HashMap<>(0);
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
				List<BusinessMappingData> toolDataList = new ArrayList<>(0);
				for (NodeData node : nodes) {

					BusinessMappingData toolData = new BusinessMappingData();
					String businessMappingLabel = node.getPropertyMap().get("businessmappinglabel");
					toolData.setToolName(toolName);
					toolData.setBusinessMappingLabel(businessMappingLabel);
					Map<String, String> propertyMap = node.getPropertyMap();
					propertyMap.keySet().removeAll(additionalProperties);
					toolData.setPropertyMap(propertyMap);
					toolDataList.add(toolData);
				}
				log.debug("arg0 toolDataList {} ",  toolDataList);
				businessMappinMap.put(toolName, toolDataList);
			}
		} catch (InsightsCustomException e) {
			log.error(e);
		}
		return businessMappinMap;
	}
}