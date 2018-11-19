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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformdal.agentConfig.AgentConfig;
import com.cognizant.devops.platformdal.agentConfig.AgentConfigDAL;
import com.cognizant.devops.platformengine.message.core.EngineStatusLogger;
import com.cognizant.devops.platformengine.message.factory.EngineSubscriberResponseHandler;
import com.cognizant.devops.platformengine.message.subscriber.AgentDataSubscriber;
import com.cognizant.devops.platformengine.message.subscriber.AgentHealthSubscriber;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 
 * @author 146414 
 * This module will pull the data from Graph and accordingly
 * subscribe for the incoming data
 */
public class EngineAggregatorModule implements Job{
	private static Logger log = LogManager.getLogger(EngineAggregatorModule.class.getName());
	private static Map<String, EngineSubscriberResponseHandler> registry = new HashMap<String, EngineSubscriberResponseHandler>();
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		ApplicationConfigProvider.performSystemCheck();
		Neo4jDBHandler graphDBHandler = new Neo4jDBHandler();
		AgentConfigDAL agentConfigDal = new AgentConfigDAL();
		List<AgentConfig> allAgentConfigurations = agentConfigDal.getAllAgentConfigurations();
		for(AgentConfig agentConfig : allAgentConfigurations){
			registerAggragators(agentConfig, graphDBHandler);
			//publishAgentConfig(agentConfig);
		}
		//EngineStatusLogger.getInstance().createEngineStatusNode("Engine Aggregator Module (Data Collection ) run successfully  ",PlatformServiceConstants.SUCCESS);
		//agentConfigDal.updateAgentSubscriberConfigurations(allAgentConfigurations);
	}
	
	/*private void publishAgentConfig(AgentConfig agentConfig){
		JsonObject config = AgentUtils.buildAgentConfig((JsonObject)new JsonParser().parse(agentConfig.getAgentJson()));
		String routingKey = config.get("subscribe").getAsJsonObject().get("config").getAsString();
		try {
			MessagePublisherFactory.publish(routingKey, config);
		} catch (Exception e) {
			log.error(e);
		}
	}*/
	private void registerAggragators(AgentConfig agentConfig, Neo4jDBHandler graphDBHandler){
		try {
			JsonObject config = (JsonObject)new JsonParser().parse(agentConfig.getAgentJson());
			JsonObject json = config.get("publish").getAsJsonObject();
			String dataRoutingKey = json.get("data").getAsString();
			log.info(" dataRoutingKey "+dataRoutingKey);
			if(dataRoutingKey!= null && !registry.containsKey(dataRoutingKey)){
				try {
					registry.put(dataRoutingKey, new AgentDataSubscriber(dataRoutingKey, 
														agentConfig.isDataUpdateSupported(), 
														agentConfig.getUniqueKey(),
														agentConfig.getToolCategory(),
														agentConfig.getToolName()));
				} catch (Exception e) {
					log.error("Unable to add subscriber for routing key: "+dataRoutingKey,e);
					EngineStatusLogger.getInstance().createEngineStatusNode(" Error occured while executing aggragator for data queue subscriber "+e.getMessage(),PlatformServiceConstants.FAILURE);
				}
				EngineStatusLogger.getInstance().createEngineStatusNode(" Agent data queue "+ dataRoutingKey +" subscribed successfully ",PlatformServiceConstants.SUCCESS);
			}
			
			String healthRoutingKey = json.get("health").getAsString();
			if(healthRoutingKey!= null && !registry.containsKey(healthRoutingKey)){
				//Make sure that default health node is initialized
				String nodeLabels = ":LATEST:" + healthRoutingKey.replace(".",":");
				try {
					graphDBHandler.executeCypherQuery("MERGE (n"+nodeLabels+") return n");
					registry.put(healthRoutingKey, new AgentHealthSubscriber(healthRoutingKey));
				} catch (Exception e) {
					log.error("Unable to add subscriber for routing key: "+healthRoutingKey,e);
					EngineStatusLogger.getInstance().createEngineStatusNode(" Error occured while executing aggragator for health queue subscriber  "+e.getMessage(),PlatformServiceConstants.FAILURE);
				}
				EngineStatusLogger.getInstance().createEngineStatusNode(" Agent health queue "+ healthRoutingKey +" subscribed successfully ",PlatformServiceConstants.SUCCESS);
			}
		
		} catch (Exception e) {
			log.error("Unable to add subscriber for routing key: "+agentConfig.getAgentKey() ,e);
			EngineStatusLogger.getInstance().createEngineStatusNode(" Error occured while executing aggragator  "+ agentConfig.getAgentKey() +e.getMessage(),PlatformServiceConstants.FAILURE);
		}
	}
	
	/*public boolean deregisterAggregator(String key){
		EngineSubscriberResponseHandler engineSubscriberResponseHandler = registry.get(key);
		if(engineSubscriberResponseHandler != null){
			try {
				MessageSubscriberFactory.getInstance().unregisterSubscriber(key, engineSubscriberResponseHandler);
			} catch (IOException e) {
				log.error(e);
			} catch (TimeoutException e) {
				log.error(e);
			}
		}
		return false;
	}*/
}