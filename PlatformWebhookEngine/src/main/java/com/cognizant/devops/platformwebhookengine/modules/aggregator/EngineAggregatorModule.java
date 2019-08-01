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
package com.cognizant.devops.platformwebhookengine.modules.aggregator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfig;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfigDAL;
import com.cognizant.devops.platformwebhookengine.message.factory.EngineSubscriberResponseHandler;
import com.cognizant.devops.platformwebhookengine.message.subscriber.WebHookDataSubscriber;

public class EngineAggregatorModule implements Job {
	private static Logger log = LogManager.getLogger(EngineAggregatorModule.class.getName());
	private static Map<String, EngineSubscriberResponseHandler> registry = new HashMap<String, EngineSubscriberResponseHandler>();


	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		ApplicationConfigProvider.performSystemCheck();
		Neo4jDBHandler graphDBHandler = new Neo4jDBHandler();
		WebHookConfigDAL webhookConfigDal = new WebHookConfigDAL();
		List<WebHookConfig> allWebhookConfigurations = webhookConfigDal.getAllWebHookConfigurations();
		//boolean enableOnlineDatatagging = ApplicationConfigProvider.getInstance().isEnableOnlineDatatagging();
		Map<String, List<WebhookMappingData>> webhookMappinMap = new HashMap<String, List<WebhookMappingData>>(0);
		//if (enableOnlineDatatagging) {
			//webhookMappinMap = getMetaData(graphDBHandler);
	//	}
		for (WebHookConfig webhookConfig : allWebhookConfigurations) {
			String webhookname = webhookConfig.getWebHookName().toUpperCase();
			String toolName = webhookConfig.getToolName().toUpperCase();
			Boolean subscribeStatus = webhookConfig.getSubscribeStatus();
			
			
			log.error(toolName);
			List<WebhookMappingData> webhookMappingList = webhookMappinMap.get(webhookname);
			if (webhookMappingList == null) {
				webhookMappingList = new ArrayList<WebhookMappingData>(0);
			}
			if(subscribeStatus == true)
			{
				registerAggragators(webhookConfig, graphDBHandler, toolName, webhookMappingList);}
		}
	}
	private void registerAggragators(WebHookConfig webhookConfig, Neo4jDBHandler graphDBHandler, String toolName,
			List<WebhookMappingData> webhookMappingList) {
		try {
			
			String dataRoutingKey = webhookConfig.getMQChannel();
			String responseTemplate = webhookConfig.getResponseTemplate();
		      log.debug(" dataRoutingKey " + dataRoutingKey + "  Tool Info " + toolName);
		      
		
		
		
		      if (dataRoutingKey != null && !registry.containsKey(dataRoutingKey)) {
					// Make sure that default health node is initialized
					//String nodeLabels = ":LATEST:" + dataRoutingKey.replace(".", ":");
					try {
						//graphDBHandler.executeCypherQuery("MERGE (n" + nodeLabels + ") return n");
						registry.put(dataRoutingKey, new WebHookDataSubscriber(dataRoutingKey,responseTemplate,toolName));
					}
					catch (Exception e) {
						log.error("Unable to add subscriber for routing key: "+e);
						
					}
		
		}
		}
			 catch (Exception e) {
					log.error("Unable to add subscriber for routing key: "+e);
					
				}
				
			
		
	}
	}
	
