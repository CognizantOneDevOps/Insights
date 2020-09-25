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
package com.cognizant.devops.engines.util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * Gereric class for all webhook event processing. *
 * 
 * 
 */

public class WebhookEventProcessing {

	private static Logger log = LogManager.getLogger(WebhookEventProcessing.class);
	private List<JsonObject> eventPayload = new ArrayList<>();
	private Map<String, JsonObject> eventConfigMap = new HashMap<>();
	private GraphDBHandler dbHandler = new GraphDBHandler();
	private WebHookConfig webhookConfig;
	private boolean isOfflineProcessing = false;

	public void setEventPayload(List<JsonObject> eventPayload) {
		this.eventPayload = eventPayload;
	}

	public void setWebhookConfig(WebHookConfig webhookConfig) {
		this.webhookConfig = webhookConfig;
	}

	public WebhookEventProcessing(List<JsonObject> eventPayload, WebHookConfig webhookConfig,
			boolean isOfflineProcessing) {
		this.eventPayload = eventPayload;
		this.webhookConfig = webhookConfig;
		this.isOfflineProcessing = isOfflineProcessing;
		getEventConfigMap(webhookConfig.getEventConfigJson(), eventConfigMap);
	}

	public boolean doEvent() {
		// Check if it has webhook event
		List<Boolean> status = new ArrayList<>();
		if (!eventPayload.get(0).has("webhookEvent")) {
			log.error(
					"Webhook event processing ===== webhookEvent property not present in webhook response for webhook {}",
					webhookConfig.getWebHookName());
			return true;
		}
		String event = eventPayload.get(0).get("webhookEvent").getAsString();
		JsonObject eventJson = eventConfigMap.get(event);
		if (eventJson == null) {
			log.error(
					"Webhook event processing ===== webhookEvent or query property not present in event processing config json {}",
					 eventConfigMap);
			return true;
		}
		Map<String, String> keyValuesMap = getKeyValues();
		StringSubstitutor substitutor = new StringSubstitutor(keyValuesMap, "{", "}");
		JsonArray queryArray = eventJson.get("query").getAsJsonArray();
		for (JsonElement element : queryArray) {
			String rawquery = element.getAsString();
			String query = substitutor.replace(rawquery);
			status.add(executeQuery(query));
		}
		return !status.contains(Boolean.FALSE);

	}

	private boolean executeQuery(String query) {
		try {
			JsonObject resp = dbHandler.executeCypherQueryForJsonResponse(query);
			log.debug("Webhook event processing ==== query {} executed and response {} ", resp, query);
			return isSuccess(resp);
		} catch (Exception e) {
			/* case 4 where neo4j might be down will be caught here */
			log.error("Webhook event processing ==== has failed due to {} ", e.getMessage());
			return false;

		}

	}

	private boolean isSuccess(JsonObject resp) {
		boolean success = true;
		/*
		 * case 1 resp has errors means either neo4j query has syntax errors or
		 * substitutor didn't replace string properly
		 */
		if ((resp.has("errors") && resp.get("errors").getAsJsonArray().size() > 0) || (resp.has("results") && resp
				.get("results").getAsJsonArray().get(0).getAsJsonObject().get("data").getAsJsonArray().size() == 0)) {
			if (!isOfflineProcessing) {
				return saveEvent();
			} else {
				return false;
			}
		}
		/* case 3 we get data means query executed successfully */

		return success;

	}

	private boolean saveEvent() {

		long currentTimeStamp = InsightsUtils.getCurrentTimeInSeconds();
		JsonObject payload = eventPayload.get(0);
		payload.addProperty("eventTimestamp", currentTimeStamp);
		String query = "UNWIND {props} AS properties " + "CREATE (n:" + this.webhookConfig.getLabelName().toUpperCase()
				+ ") " + "SET n = properties";
		try {
			dbHandler.bulkCreateNodes(Arrays.asList(payload), null, query);
			log.debug("Webhook event processing ==== event node saved for offline processing payload {} and query {} ",
					payload, query);
		} catch (InsightsCustomException e) {
			log.error("Webhook event processing ==== enable to save event to neo4j due to {}", e.getMessage());
			return false;
		}
		return true;
	}

	private Map<String, String> getKeyValues() {
		Gson gson = new Gson();
		Type type = new TypeToken<Map<String, String>>() {
		}.getType();
		return gson.fromJson(eventPayload.get(0), type);
	}

	private void getEventConfigMap(String eventConfig, Map<String, JsonObject> config) {
		try {
			JsonElement json = new JsonParser().parse(eventConfig);
			JsonObject eachConfig = json.getAsJsonObject();
			if (eachConfig.has("config")) {
				JsonArray array = eachConfig.get("config").getAsJsonArray();
				array.forEach(element -> {
					JsonObject obj = element.getAsJsonObject();
					if (obj.has("event") && obj.has("query")) {
						String eventName = obj.get("event").getAsString();
						config.put(eventName, obj);
					}
				});
				log.debug("Webhook event processing ==== eventConfig map prepared successfully {}", config);
			} else {
				log.debug(
						"Webhook event processing ==== unable to prepare eventConfig map as config property is missing in event config  {}",
						config);
			}

		} catch (Exception e) {
			log.error("Webhook event processing ==== enable to process event config map {}", e.getMessage());
		}
	}

}
