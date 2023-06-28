/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.engines.platformwebhookengine.message.subscriber;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.engines.platformengine.message.core.EngineStatusLogger;
import com.cognizant.devops.engines.platformengine.message.factory.EngineSubscriberResponseHandler;
import com.cognizant.devops.engines.platformwebhookengine.parser.InsightsWebhookParserFactory;
import com.cognizant.devops.engines.platformwebhookengine.parser.InsightsWebhookParserInterface;
import com.cognizant.devops.engines.util.WebhookEventProcessing;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfig;
import com.google.gson.JsonObject;

public class WebHookDataSubscriber extends EngineSubscriberResponseHandler {

	private static Logger log = LogManager.getLogger(WebHookDataSubscriber.class);
	private GraphDBHandler dbHandler = new GraphDBHandler();
	private WebHookConfig webhookConfig;
	private String jobName = ""; 

	public WebHookDataSubscriber(WebHookConfig webhookConfig, String mqChannelName, String jobName) throws Exception {
		super(mqChannelName);
		this.webhookConfig = webhookConfig;
		this.jobName=jobName;
	}

	public void setWebhookConfig(WebHookConfig webhookConfigUpdated) {
		this.webhookConfig = webhookConfigUpdated;
	}

	@Override
	public void handleDelivery(String routingKey, String message) throws InsightsCustomException {
		try {
			long startTime = System.nanoTime();
			
			if (!message.equalsIgnoreCase("") || !message.isEmpty()) {
				
				insertIntoNeo4j(message);				
			
			} else {
				log.error(" No valid payload found for webhook  message{} {} ", this.webhookConfig.getWebHookName(),
						message);
			}
			long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
			log.debug("Type=WebhookEngine toolName={} category={} WebHookName={} routingKey={} dataSize={} execId={} ProcessingTime={} {} webhook data processed successfully ",this.webhookConfig.getToolName(),"-",this.webhookConfig.getWebHookName(),this.webhookConfig.getMQChannel(),0,"-",processingTime, this.webhookConfig.getWebHookName());
		} catch (Exception e) {
			log.error("Error in payload {} ",message);
			log.error(" toolName={} agentId={} routingKey={} Error while storing Webhook data",this.webhookConfig.getToolName(),this.webhookConfig.getWebHookName(),this.webhookConfig.getMQChannel(),e);
			EngineStatusLogger.getInstance().createSchedularTaskStatusNode(
					"Exception while pasring or DB issues " + e.getMessage(), PlatformServiceConstants.FAILURE,jobName);
			throw new InsightsCustomException(e.getMessage());
		}
	}
	

	private void insertIntoNeo4j(String message) throws Exception{
		
		InsightsWebhookParserInterface webHookParser = InsightsWebhookParserFactory
				.getParserInstance(this.webhookConfig.getToolName());
		List<JsonObject> toolData = webHookParser.parseToolData(this.webhookConfig, message);
		// Insert into Neo4j
		if (!toolData.isEmpty()) {
			Boolean b=webhookConfig.isEventProcessing();
			if (Boolean.TRUE.equals(b)) {
				WebhookEventProcessing wep = new WebhookEventProcessing(toolData, webhookConfig,false);
				boolean status = wep.doEvent();
				if (status) {
				}
			} else if (this.webhookConfig.getIsUpdateRequired().booleanValue()) {
				updateNeo4jNode(toolData, this.webhookConfig);
			} else {
				String query = "UNWIND $props AS properties " + "CREATE (n:RAW:"
						+ this.webhookConfig.getLabelName().toUpperCase() + ") " + "SET n = properties";
				dbHandler.bulkCreateNodes(toolData, query);
			}
			
		} else {
			log.error(" Unmatched Response Template found for {} ", this.webhookConfig.getWebHookName());
			EngineStatusLogger.getInstance().createSchedularTaskStatusNode(
					"No Webhook Nodes are inserted in DB for " + this.webhookConfig.getWebHookName(),
					PlatformServiceConstants.FAILURE,jobName);
		}
		
	}
	
	// Execution of the Query in which node updation in Neo4j is required,based on
	// the unique property.
	private void updateNeo4jNode(List<JsonObject> toolData, WebHookConfig webhookConfig2) throws Exception {
		try {
			String finalQuery = "";
			for (JsonObject jsonObject : toolData) {
				StringBuilder query = new StringBuilder();
				query.append("UNWIND $props AS properties MERGE (node:RAW:").append(webhookConfig2.getLabelName());
				if (webhookConfig2.getFieldUsedForUpdate() != null) {
					query.append(" { " + webhookConfig2.getFieldUsedForUpdate() + ": ");
					query.append(jsonObject.get(webhookConfig2.getFieldUsedForUpdate()));
					query.append(" }) ");
				}
				query.append(" set node+=properties ").append(" ");
				query.append("return count(node)").append(" ");
				finalQuery = query.toString();
				JsonObject graphresponse = dbHandler.createNodesWithSingleData(jsonObject, finalQuery);
				if (graphresponse.get("response").getAsJsonObject().get("errors").getAsJsonArray().size() > 0) {
					log.error(" toolName={} agentId={} routingKey={} Unable to insert nodes for routing key: {} and webhook Name {} , error occured: {} ",this.webhookConfig.getToolName(),this.webhookConfig.getWebHookName(),this.webhookConfig.getMQChannel(),
							webhookConfig2.getMQChannel(), webhookConfig2.getWebHookName(), graphresponse);
				}
			}
		} catch (Exception e) {
			log.error(" toolName={} agentId={} routingKey={} Error while featching DB record  ",this.webhookConfig.getToolName(),this.webhookConfig.getWebHookName(),this.webhookConfig.getMQChannel(), e);
			throw e;
		}
	}
}