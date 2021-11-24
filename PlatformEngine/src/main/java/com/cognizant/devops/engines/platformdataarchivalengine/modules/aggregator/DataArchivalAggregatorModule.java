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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.cognizant.devops.engines.platformdataarchivalengine.message.subscriber.DataArchivalDataSubscriber;
import com.cognizant.devops.engines.platformdataarchivalengine.message.subscriber.DataArchivalHealthSubscriber;
import com.cognizant.devops.engines.platformengine.message.core.EngineStatusLogger;
import com.cognizant.devops.engines.platformengine.message.factory.EngineSubscriberResponseHandler;
import com.cognizant.devops.engines.util.EngineUtils;
import com.cognizant.devops.platformcommons.config.ApplicationConfigInterface;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.DataArchivalConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.agentConfig.AgentConfig;
import com.cognizant.devops.platformdal.agentConfig.AgentConfigDAL;
import com.cognizant.devops.platformdal.dataArchivalConfig.DataArchivalConfigDal;
import com.cognizant.devops.platformdal.dataArchivalConfig.InsightsDataArchivalConfig;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DataArchivalAggregatorModule implements Job, ApplicationConfigInterface {
	private static Logger log = LogManager.getLogger(DataArchivalAggregatorModule.class);
	private static Map<String, EngineSubscriberResponseHandler> registry = new HashMap<>();
	AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
	DataArchivalConfigDal dataArchivalConfigDal = new DataArchivalConfigDal();
	private Map<String,String> loggingInfo = new HashMap<>();
	String jobName="";
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.debug("Data Archival Aggregator Module start");
		long startTime =System.currentTimeMillis();
		jobName=context.getJobDetail().getKey().getName();
		EngineStatusLogger.getInstance().createSchedularTaskStatusNode("Data Archival Aggregator execution Start ",
				PlatformServiceConstants.SUCCESS,jobName);
		
		try {
			ApplicationConfigInterface.loadConfiguration();
			ApplicationConfigProvider.performSystemCheck();
			runDataArchival();
		} catch (Exception e) {
			log.error("Unable to add subscriber ", e);
			EngineStatusLogger.getInstance().createSchedularTaskStatusNode("Data Archival Aggregator execution has some issue  ",
					PlatformServiceConstants.FAILURE,jobName);
			EngineStatusLogger.getInstance().createDataArchivalStatusNode(
					"Data Archival Aggregator execution has some issue   "+e.getMessage(), PlatformServiceConstants.FAILURE);
		}
		long processingTime = System.currentTimeMillis() - startTime ;
		EngineStatusLogger.getInstance().createSchedularTaskStatusNode("Data Archival Aggregator execution Completed",
				PlatformServiceConstants.SUCCESS,jobName,processingTime);
		EngineStatusLogger.getInstance().createDataArchivalStatusNode(
				"Data Archival Aggregator execution started successfully  ", PlatformServiceConstants.SUCCESS);
		log.debug("Data Archival Aggregator Module completed");
	}

	public void runDataArchival() {
		List<AgentConfig> agentConfigs = agentConfigDAL.getAgentConfigurations(DataArchivalConstants.TOOLNAME,
				DataArchivalConstants.TOOLCATEGORY);
		for(AgentConfig agentConfig: agentConfigs) {
			JsonObject config = (JsonObject) new JsonParser().parse(agentConfig.getAgentJson());
			JsonObject publishJson = config.get("publish").getAsJsonObject();
			String dataRoutingKey = publishJson.get("data").getAsString();
			loggingInfo.put("toolName", String.valueOf(config.get("toolName")));
			loggingInfo.put("category", String.valueOf(config.get("toolCategory")));
			loggingInfo.put("agentId", String.valueOf(config.get("agentId")));
			loggingInfo.put("routingKey", dataRoutingKey);
			registerDataAggregator(dataRoutingKey);
			String healthRoutingKey = publishJson.get("health").getAsString();
			registerHealthAggregator(healthRoutingKey);
			String routingKey = config.get("subscribe").getAsJsonObject().get("dataArchivalQueue").getAsString();
			performExpiredRecordCheck(routingKey);
		}
	}

	private void performExpiredRecordCheck(String routingKey) {
		List<InsightsDataArchivalConfig> expiredArchivalrecords = dataArchivalConfigDal.getExpiredArchivalrecords();
		if (!expiredArchivalrecords.isEmpty()) {
			try {
				for(InsightsDataArchivalConfig archivalConfig: expiredArchivalrecords) {
					JsonObject mqRequestJson = new JsonObject();
					mqRequestJson.addProperty(DataArchivalConstants.TASK, "remove_container");
					if(archivalConfig.getContainerID() != null && !archivalConfig.getContainerID().isEmpty()) {
						mqRequestJson.addProperty(DataArchivalConstants.CONTAINERID, archivalConfig.getContainerID());
					} else {
						throw new InsightsCustomException("ContainerID is null or empty for " + archivalConfig.getArchivalName());
					}
					EngineUtils.publishMessageInMQ(routingKey, mqRequestJson.toString());
				}
				
			} catch (Exception e) {
				log.error(e);
			}
		} else {
			log.debug("No archival records are currently on due to expire");
		}

		
	}

	private void registerDataAggregator(String dataRoutingKey) {

		if (dataRoutingKey != null && !registry.containsKey(dataRoutingKey)) {
			try {
				registry.put(dataRoutingKey, new DataArchivalDataSubscriber(dataRoutingKey,loggingInfo.get("agentId")));
				log.debug(" Type=DataArchival toolName={} category={} agentId={} routingKey={} execId={} Data archival data queue {} subscribed successfully ",loggingInfo.get("toolName"),loggingInfo.get("category"),loggingInfo.get("agentId"),loggingInfo.get("routingKey"),"-",dataRoutingKey);
				EngineStatusLogger.getInstance().createSchedularTaskStatusNode(
						" Data archival data queue " + dataRoutingKey + " subscribed successfully ",
						PlatformServiceConstants.SUCCESS,jobName);
			} catch (Exception e) {
				log.error(" toolName={} category={} agentId={} routingKey={} Unable to add subscriber for routing key:{} ",loggingInfo.get("toolName"),loggingInfo.get("category"),loggingInfo.get("agentId"),loggingInfo.get("routingKey"),dataRoutingKey, e);
				EngineStatusLogger.getInstance().createSchedularTaskStatusNode(
						" Error occured while executing aggragator for data queue subscriber " + e.getMessage(),
						PlatformServiceConstants.FAILURE,jobName);
			}
		}
	}

	private void registerHealthAggregator(String healthRoutingKey) {

		if (healthRoutingKey != null && !registry.containsKey(healthRoutingKey)) {
			try {
				registry.put(healthRoutingKey, new DataArchivalHealthSubscriber(healthRoutingKey));
				log.debug(" Type=DataArchival toolName={} category={} agentId={} routingKey={} execId={} Data archival health queue {} subscribed successfully ",loggingInfo.get("toolName"),loggingInfo.get("category"),loggingInfo.get("agentId"),healthRoutingKey,"-",healthRoutingKey);
				EngineStatusLogger.getInstance().createSchedularTaskStatusNode(
						" Data Archival Agent health queue " + healthRoutingKey + " subscribed successfully ",
						PlatformServiceConstants.SUCCESS,jobName);
			} catch (Exception e) {
				log.error(" toolName={} category={} agentId={} routingKey={} Unable to add subscriber for routing key:{}" ,loggingInfo.get("toolName"),loggingInfo.get("category"),loggingInfo.get("agentId"),healthRoutingKey,healthRoutingKey, e);
				EngineStatusLogger.getInstance().createSchedularTaskStatusNode(
						" Error occured while executing aggregator for Data archival health queue subscriber  "
								+ e.getMessage(),
						PlatformServiceConstants.FAILURE,jobName);
			}
		}
	}

}
