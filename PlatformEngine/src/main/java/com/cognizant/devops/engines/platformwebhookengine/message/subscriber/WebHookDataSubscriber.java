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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.cognizant.devops.engines.platformengine.message.core.EngineStatusLogger;
import com.cognizant.devops.engines.platformwebhookengine.message.factory.EngineSubscriberResponseHandler;
import com.cognizant.devops.engines.platformwebhookengine.parser.InsightsWebhookParserFactory;
import com.cognizant.devops.engines.platformwebhookengine.parser.InsightsWebhookParserInterface;
import com.cognizant.devops.engines.util.WebhookEventProcessing;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfig;
import com.google.gson.JsonObject;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Envelope;

public class WebHookDataSubscriber extends EngineSubscriberResponseHandler {

	private static Logger log = LogManager.getLogger(WebHookDataSubscriber.class);
	private GraphDBHandler dbHandler = new GraphDBHandler();
	private WebHookConfig webhookConfig;


	public WebHookDataSubscriber(WebHookConfig webhookConfig, String mqChannelName) throws Exception {
		super(mqChannelName);
		this.webhookConfig = webhookConfig;

	}

	public void setWebhookConfig(WebHookConfig webhookConfigUpdated) {
		this.webhookConfig = webhookConfigUpdated;
	}

	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
			throws IOException {

		try {
			String message = new String(body, StandardCharsets.UTF_8);
			if (!message.equalsIgnoreCase("") || !message.isEmpty()) {
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
							getChannel().basicAck(envelope.getDeliveryTag(), false);
						}
					} else if (this.webhookConfig.getIsUpdateRequired().booleanValue()) {
						updateNeo4jNode(toolData, this.webhookConfig);
						getChannel().basicAck(envelope.getDeliveryTag(), false);
					} else {
						String query = "UNWIND {props} AS properties " + "CREATE (n:RAW:"
								+ this.webhookConfig.getLabelName().toUpperCase() + ") " + "SET n = properties";
						dbHandler.bulkCreateNodes(toolData, null, query);
						getChannel().basicAck(envelope.getDeliveryTag(), false);
					}

				} else {
					log.error("Unmatched Response Template found for {} ", this.webhookConfig.getWebHookName());
					EngineStatusLogger.getInstance().createWebhookEngineStatusNode(
							"No Webhook Nodes are inserted in DB for " + this.webhookConfig.getWebHookName(),
							PlatformServiceConstants.FAILURE);
				}
			} else {
				log.error(" No valid payload found for webhook {}  message {} ", this.webhookConfig.getWebHookName(),
						message);
			}
			log.debug(" {} webhook data processed successfully ", this.webhookConfig.getWebHookName());
		} catch (Exception e) {
			log.error(e);
			EngineStatusLogger.getInstance().createWebhookEngineStatusNode(
					"Exception while pasring or DB issues " + e.getMessage(), PlatformServiceConstants.FAILURE);
		}
	}

	// Execution of the Query in which node updation in Neo4j is required,based on
	// the unique property.
	private void updateNeo4jNode(List<JsonObject> toolData, WebHookConfig webhookConfig2) {
		try {
			String finalQuery = "";
			for (JsonObject jsonObject : toolData) {
				StringBuilder query = new StringBuilder();
				query.append("UNWIND {props} AS properties MERGE (node:RAW:").append(webhookConfig2.getLabelName());
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
					log.error("Unable to insert nodes for routing key: {} and webhook Name {} , error occured: {} ",
							webhookConfig2.getMQChannel(), webhookConfig2.getWebHookName(), graphresponse);
				}
			}
		} catch (Exception e) {
			log.error(" Error while featching DB record {} ", e);
		}
	}

}