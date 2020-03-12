/*******************************************************************************
 *  * Copyright 2017 Cognizant Technology Solutions
 *  * 
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  * use this file except in compliance with the License.  You may obtain a copy
 *  * of the License at
 *  * 
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  * 
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  * License for the specific language governing permissions and limitations under
 *  * the License.
 *******************************************************************************/
package com.cognizant.devops.engines.platformwebhookengine.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.engines.util.DataEnrichUtils;
import com.cognizant.devops.platformcommons.core.enums.DerivedOperations;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.webhookConfig.WebhookDerivedConfig;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class InsightsGeneralParser implements InsightsWebhookParserInterface {
	private static Logger LOG = LogManager.getLogger(InsightsGeneralParser.class.getName());

	@Override
	public List<JsonObject> parseToolData(String responseTemplate, String toolData, String toolName, String labelName,
			String webhookName, Set<WebhookDerivedConfig> webhookDerivedConfigs) throws InsightsCustomException {

		try {
			String keyMqInitial;
			JsonParser parser = new JsonParser();
			List<JsonObject> retrunJsonList = new ArrayList<>(0);
			Map<String, Object> finalJson = new HashMap<>();
			JsonElement json = parser.parse(toolData);
			Map<String, Object> rabbitMqflattenedJsonMap = JsonFlattener.flattenAsMap(json.toString());
			Map<String, String> responseTemplateMap = getResponseTemplateMap(responseTemplate);
			for (Map.Entry<String, String> entry : responseTemplateMap.entrySet()) {
				keyMqInitial = entry.getKey();
				Object toolValue = rabbitMqflattenedJsonMap.get(keyMqInitial);
				if (toolValue != null) {
					finalJson.put(entry.getValue(), toolValue);
				}
			}

			if (!finalJson.isEmpty()) {
				finalJson.put("source", "webhook");
				finalJson.put("toolName", toolName);
				finalJson.put("webhookName", webhookName);
				finalJson.put("labelName", labelName);
				Iterator<WebhookDerivedConfig> iterator = webhookDerivedConfigs.iterator();
				while (iterator.hasNext()) {
					WebhookDerivedConfig webhookDerivedConfig = iterator.next();
					try {
						String dateFormat = "";
						String timeFieldValue = "";
						boolean isEpochTime = false;
						long epochTime = 0;
						String dateTimeFromEpoch = "";
						String operationName = webhookDerivedConfig.getOperationName();
						JsonObject operationFieldsList = parser.parse(webhookDerivedConfig.getOperationFields())
								.getAsJsonObject();
						if (operationName.equalsIgnoreCase(DerivedOperations.INSIGHTSTIMEX.getValue())) {
							isEpochTime = operationFieldsList.get("epochTime").getAsBoolean();
							String timeFieldKey = operationFieldsList.get("timeField").getAsString();
							timeFieldValue = fetchValuefromJson(timeFieldKey, finalJson);
							if (!isEpochTime) {
								dateFormat = operationFieldsList.get("timeFormat").getAsString();
								epochTime = InsightsUtils.getEpochTime(timeFieldValue, dateFormat);
								finalJson.put("inSightsTime", epochTime);
								if (dateFormat.equals(InsightsUtils.DATE_TIME_FORMAT)) {
									finalJson.put("inSightsTimeX", timeFieldValue);
								} else {
									dateTimeFromEpoch = InsightsUtils.insightsTimeXFormat(epochTime);
									finalJson.put("inSightsTimeX", dateTimeFromEpoch);
								}
							} else {
								finalJson.put("inSightsTime", timeFieldValue);
								dateTimeFromEpoch = InsightsUtils.insightsTimeXFormat(Long.parseLong(timeFieldValue));
								finalJson.put("inSightsTimeX", dateTimeFromEpoch);
							}
						} else if (operationName.equalsIgnoreCase(DerivedOperations.TIMEFIELDMAPPING.getValue())) {
							String timeFieldKey = operationFieldsList.get("mappingTimeField").getAsString();
							timeFieldValue = fetchValuefromJson(timeFieldKey, finalJson);
							dateFormat = operationFieldsList.get("mappingTimeFormat").getAsString();
							epochTime = InsightsUtils.getEpochTime(timeFieldValue, dateFormat);
							finalJson.put(timeFieldKey + "_epoch", epochTime);
						} else if (operationName.equalsIgnoreCase(DerivedOperations.DATAENRICHMENT.getValue())) {
							processDataEnrichment(operationFieldsList, finalJson);
						}
					} catch (Exception e) {
						LOG.error("Error while Webhook derived operation {} and configuration {} Error is {}",
								webhookDerivedConfig.getOperationName(), webhookDerivedConfig.getOperationFields(), e);
						throw new InsightsCustomException(e.getMessage());
					}
				}

				Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
				String prettyJson = prettyGson.toJson(finalJson);
				JsonElement element = parser.parse(prettyJson);
				retrunJsonList.add(element.getAsJsonObject());
			}

			return retrunJsonList;
		} catch (Exception e) {
			LOG.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
	}

	/**
	 * @param webhookDerivedConfig
	 * @param parser
	 * @param finalJson
	 * @param rabbitMqflattenedJsonMap
	 */

	public String fetchValuefromJson(String timeFieldKey, Map<String, Object> finalJson) {
		try {
			String timeFieldValue = finalJson.get(timeFieldKey).toString();
			return timeFieldValue;
		} catch (Exception e) {
			LOG.error("Failed to fetch the value of the field" + e.getMessage());
			throw e;
		}
	}

	public void processDataEnrichment(JsonObject jsonOperationField, Map<String, Object> finalJson) {
		try {
			String sourceFieldValue = finalJson.get(jsonOperationField.get("sourceProperty").getAsString()).toString();
			String keyPattern = jsonOperationField.get("keyPattern").getAsString();
			String targetProperty = jsonOperationField.get("targetProperty").getAsString();
			String enrichedData = DataEnrichUtils.dataExtractor(sourceFieldValue, keyPattern);
			if (enrichedData != null) {
				finalJson.put(targetProperty, enrichedData);
			}
		} catch (Exception e) {
			LOG.error(" Error while processDataEnrichment " + e);
			throw e;
		}
	}

	private Map<String, String> getResponseTemplateMap(String responseTemplate) {

		Map<String, String> responseTemplateMap = new HashMap<>();
		String value = responseTemplate.replace("\n", "").replace("\r", "");
		String[] keyValuePairs = value.split(","); // split the string to creat key-value pairs
		for (String pair : keyValuePairs) // iterate over the pairs
		{
			String[] dataKeyMapper = pair.split("="); // split the pairs to get key and value
			responseTemplateMap.put(dataKeyMapper[0].trim(), dataKeyMapper[1].trim());
		}
		return responseTemplateMap;
	}

}