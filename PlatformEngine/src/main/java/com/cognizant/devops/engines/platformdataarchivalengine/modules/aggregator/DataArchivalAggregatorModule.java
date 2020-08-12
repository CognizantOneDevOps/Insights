/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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

package com.cognizant.devops.engines.platformdataarchivalengine.modules.aggregator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.engines.platformdataarchivalengine.message.subscriber.DataArchivalDataSubscriber;
import com.cognizant.devops.engines.platformdataarchivalengine.message.subscriber.DataArchivalHealthSubscriber;
import com.cognizant.devops.engines.platformengine.message.core.EngineStatusLogger;
import com.cognizant.devops.engines.platformengine.message.factory.EngineSubscriberResponseHandler;
import com.cognizant.devops.engines.platformengine.message.subscriber.AgentHealthSubscriber;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.DataArchivalConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.agentConfig.AgentConfig;
import com.cognizant.devops.platformdal.agentConfig.AgentConfigDAL;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DataArchivalAggregatorModule extends TimerTask {
	private static Logger log = LogManager.getLogger(DataArchivalAggregatorModule.class.getName());
	private static Map<String, EngineSubscriberResponseHandler> registry = new HashMap<String, EngineSubscriberResponseHandler>();
	Neo4jDBHandler neo4jDBHandler = new Neo4jDBHandler();
	AgentConfigDAL agentConfigDAL = new AgentConfigDAL();

	@Override
	public void run() {
		log.debug("Data Archival Aggregator Module start");
		ApplicationConfigProvider.performSystemCheck();
		try {
		List<AgentConfig> agentConfig = agentConfigDAL.getAgentConfigurations(DataArchivalConstants.toolName, DataArchivalConstants.toolCategory);
		if(!agentConfig.isEmpty()) {
		registerAggregator(agentConfig.get(0));
		}
		else {
			throw new InsightsCustomException("Data archival agent not present.");
		}
		}catch(Exception e) {
			log.error("Error running Data Archival Aggregator.{}",e);
		}

	}

	private void registerAggregator(AgentConfig agentConfig) {
		try {
			JsonObject config = (JsonObject) new JsonParser().parse(agentConfig.getAgentJson());
			JsonObject publishJson = config.get("publish").getAsJsonObject();
			String dataRoutingKey = publishJson.get("data").getAsString();
			if (dataRoutingKey != null && !registry.containsKey(dataRoutingKey)) {
				try {
					registry.put(dataRoutingKey, new DataArchivalDataSubscriber(dataRoutingKey));
				} catch (Exception e) {
					log.error("Unable to add subscriber for routing key: " + dataRoutingKey, e);
					EngineStatusLogger.getInstance().createDataArchivalStatusNode(
							" Error occured while executing aggragator for data queue subscriber " + e.getMessage(),
							PlatformServiceConstants.FAILURE);
				}
				EngineStatusLogger.getInstance().createDataArchivalStatusNode(
						" Data archival data queue " + dataRoutingKey + " subscribed successfully ",
						PlatformServiceConstants.SUCCESS);
			} else if (registry.containsKey(dataRoutingKey)) {
				DataArchivalDataSubscriber dataSubscriber = (DataArchivalDataSubscriber) registry.get(dataRoutingKey);
			}
			
			String healthRoutingKey = publishJson.get("health").getAsString();
			if (healthRoutingKey != null && !registry.containsKey(healthRoutingKey)) {
				// Make sure that default health node is initialized
				String nodeLabels = ":LATEST:" + healthRoutingKey.replace(".", ":");
				try {
					neo4jDBHandler.executeCypherQuery("MERGE (n" + nodeLabels + ") return n");
					registry.put(healthRoutingKey, new DataArchivalHealthSubscriber(healthRoutingKey));
				} catch (Exception e) {
					log.error("Unable to add subscriber for routing key: " + healthRoutingKey, e);
					EngineStatusLogger.getInstance().createDataArchivalStatusNode(
							" Error occured while executing aggregator for Data archival health queue subscriber  " + e.getMessage(),
							PlatformServiceConstants.FAILURE);
				}
				EngineStatusLogger.getInstance().createDataArchivalStatusNode(
						" Data Archival Agent health queue " + healthRoutingKey + " subscribed successfully ",
						PlatformServiceConstants.SUCCESS);
			}
		} catch (Exception e) {
			log.error("Unable to add subscriber for routing key: " + agentConfig.getAgentKey(), e);
			EngineStatusLogger.getInstance().createDataArchivalStatusNode(
					" Error occured while executing aggragator  " + agentConfig.getAgentKey() + e.getMessage(),
					PlatformServiceConstants.FAILURE);
		}

	}
}
