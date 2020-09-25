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
package com.cognizant.devops.engines.platformwebhookengine.offlineprocessing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.engines.util.WebhookEventProcessing;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfig;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfigDAL;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Class for Webhook Offline Event Processing
 * 
 * Responsible for processing failed webhook events.
 *
 */
public class WebhookOfflineEventProcessing extends TimerTask {
	WebHookConfigDAL dal = new WebHookConfigDAL();
	private GraphDBHandler dbHandler = new GraphDBHandler();
	private static Logger log = LogManager.getLogger(WebhookOfflineEventProcessing.class);

	@Override
	public void run() {
		log.debug("Webhook Offline Event Processing Started ======");
		List<WebHookConfig> webhookEventConfigs = dal.getAllEventWebHookConfigurations();
		log.debug("Webhook events for processing ====== {}", webhookEventConfigs);
		execute(webhookEventConfigs);
	}

	public void execute(List<WebHookConfig> webhookEventConfigs) {

		for (WebHookConfig webhookEventConfig : webhookEventConfigs) {
			try {
				WebhookEventProcessing webhookEventProcessing = null;
				String neo4jLabel = webhookEventConfig.getLabelName();
				List<JsonObject> eventNodes = getNeo4JEventNodes(neo4jLabel);
				for (JsonObject eventNode : eventNodes) {
					String uuid = eventNode.get("uuid").getAsString();
					// check max processing time
					if (webhookEventProcessing == null) {
						webhookEventProcessing = new WebhookEventProcessing(Arrays.asList(eventNode),
								webhookEventConfig, true);
					} else {
						webhookEventProcessing.setEventPayload(Arrays.asList(eventNode));
					}
					if (checkDueMaxEventProcessTime(eventNode.get("eventTimestamp").getAsLong())
							|| webhookEventProcessing.doEvent()) {
						updateNeo4jNode(webhookEventConfig.getLabelName(), uuid);
						log.debug(
								"Webhook Offline Event Processing ====== Event processed successfully for payloads {}",
								eventNode);
					}
				}
			} catch (Exception e) {
				log.error("Webhook Offline Event Processing ====== something went wrong while offline processing {}");
			}
		}

	}

	private List<JsonObject> getNeo4JEventNodes(String neo4jLabel) {
		List<JsonObject> eventNodes = new ArrayList<>();
		String query = "match(n:" + neo4jLabel + ")" + "where not exists(n.isProcessed) return n";
		try {
			JsonObject resp = dbHandler.executeCypherQueryForJsonResponse(query);
			JsonArray responseData = resp.get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
					.getAsJsonArray();
			responseData.forEach(element -> {
				JsonObject eachNode = element.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject();
				eventNodes.add(eachNode);
			});
		} catch (InsightsCustomException e) {
			log.error("Webhook events for processing ======  Custom Exception while Query Execution{}", e.getMessage());
		} catch (Exception e) {
			log.error("Webhook events for processing ======  Exception while Query Execution{}", e.getMessage());
		}

		return eventNodes;
	}

	private void updateNeo4jNode(String neo4jLabel, String uuid) {
		String query = "match(n:" + neo4jLabel + ")" + "where n.uuid = \"" + uuid + "\""
				+ " set n.isProcessed = true return n";
		try {
			dbHandler.executeCypherQueryForJsonResponse(query);
		} catch (Exception e) {
			log.error("Webhook events for processing ======  Exception while Query Execution{}", e.getMessage());
		}

	}

	private boolean checkDueMaxEventProcessTime(long timeStamp) {
		boolean isDue = false;
		long duration = InsightsUtils.getDurationBetweenTime(timeStamp);
		long maxTime = ApplicationConfigProvider.getInstance().getWebhookEngine().getEventProcessingWindowInMin();
		if (duration / 60 >= maxTime) {
			isDue = true;
		}
		return isDue;
	}

}
