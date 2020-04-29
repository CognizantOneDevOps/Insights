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
import com.cognizant.devops.engines.util.EngineUtils;
import com.cognizant.devops.platformcommons.core.enums.DerivedOperations;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfig;
import com.cognizant.devops.platformdal.webhookConfig.WebhookDerivedConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class InsightsGeneralParser implements InsightsWebhookParserInterface {
	private static Logger LOG = LogManager.getLogger(InsightsGeneralParser.class);

	/**
	 * Creating the final JSON which will be inserted in Neo4j
	 */
	@Override

	public List<JsonObject> parseToolData(WebHookConfig webhookConfig, String message) throws InsightsCustomException {

		try {
			List<JsonObject> retrunJsonList = new ArrayList<>(0);
			JsonObject responseTemplateJson = new JsonObject();
			if (webhookConfig.getResponseTemplate() != null) {
				responseTemplateJson = processResponseTemplate(webhookConfig, message);
			}
			// dynamic template is not empty
			List<JsonObject> responseDynamicTemplateList = new ArrayList(0);
			if (webhookConfig.getDynamicTemplate() != null
					&& !webhookConfig.getDynamicTemplate().equalsIgnoreCase("{}")) {
				responseDynamicTemplateList = parseDynamicTemplate(webhookConfig, message);
			}

			// merge both response template data
			if (!responseDynamicTemplateList.isEmpty()) {
				mergeData(responseDynamicTemplateList, responseTemplateJson, retrunJsonList);
			} else if (!responseTemplateJson.entrySet().isEmpty()) {
				LOG.debug(" dynamic template data is empty or No valid dynamic template found... for webhook {}  ",
						webhookConfig.getWebHookName());
				retrunJsonList.add(responseTemplateJson);
			}

			// Apply additional field mapping using webhook derived cofiguration to combined
			// data
			for (JsonObject jsonObject : retrunJsonList) {
				jsonObject.addProperty("source", "webhook");
				jsonObject.addProperty("toolName", webhookConfig.getToolName());
				jsonObject.addProperty("webhookName", webhookConfig.getWebHookName());
				jsonObject.addProperty("labelName", webhookConfig.getLabelName());
				applyWebhookDerivedConfigs(webhookConfig.getWebhookDerivedConfig(), jsonObject);
			}
			LOG.debug(" retrunJsonList {}  ", retrunJsonList);
			return retrunJsonList;
		} catch (Exception e) {
			LOG.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
	}

	/**
	 * This method is used to process Response Template
	 * 
	 * @param webhookConfig
	 * @param message
	 * @return
	 */
	private JsonObject processResponseTemplate(WebHookConfig webhookConfig, String message) {
		String keyMqInitial;
		JsonObject responseTemplateJson;
		JsonParser parser = new JsonParser();
		Gson gson = new Gson();
		JsonElement json = parser.parse(message);
		Map<String, Object> rabbitMqflattenedJsonMap = JsonFlattener.flattenAsMap(json.toString());
		Map<String, String> responseTemplateMap = getResponseTemplateMap(webhookConfig.getResponseTemplate());
		Map<String, Object> extractedMap = new HashMap(0);
		for (Map.Entry<String, String> entry : responseTemplateMap.entrySet()) {
			keyMqInitial = entry.getKey();
			Object toolValue = rabbitMqflattenedJsonMap.get(keyMqInitial);
			if (toolValue != null) {
				extractedMap.put(entry.getValue(), toolValue);
			}
		}
		String prettyJson = gson.toJson(extractedMap);
		responseTemplateJson = parser.parse(prettyJson).getAsJsonObject();
		return responseTemplateJson;
	}

	/**
	 * This Method is used to apply WebhookDerivedConfigurations,the additional
	 * Operations eg.Addition of InsightsTimeX
	 * 
	 * @param webhookDerivedConfigs
	 * @param responseTemplateJson
	 * @throws InsightsCustomException
	 */
	private void applyWebhookDerivedConfigs(Set<WebhookDerivedConfig> webhookDerivedConfigs,
			JsonObject responseTemplateJson) throws InsightsCustomException {
		JsonParser parser = new JsonParser();
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
					timeFieldValue = fetchValuefromJson(timeFieldKey, responseTemplateJson);
					if (timeFieldValue != null) {
						if (!isEpochTime) {
							dateFormat = operationFieldsList.get("timeFormat").getAsString();
							epochTime = InsightsUtils.getEpochTime(timeFieldValue, dateFormat);
							responseTemplateJson.addProperty(DerivedOperations.INSIGHTSTIME_DB.getValue(), epochTime);
							if (dateFormat.equals(InsightsUtils.DATE_TIME_FORMAT)) {
								responseTemplateJson.addProperty(DerivedOperations.INSIGHTSTIMEX_DB.getValue(),
										timeFieldValue);
							} else {
								dateTimeFromEpoch = InsightsUtils.insightsTimeXFormat(epochTime);
								responseTemplateJson.addProperty(DerivedOperations.INSIGHTSTIMEX_DB.getValue(),
										dateTimeFromEpoch);
							}
						} else {
							responseTemplateJson.addProperty(DerivedOperations.INSIGHTSTIME_DB.getValue(),
									timeFieldValue);
							dateTimeFromEpoch = InsightsUtils.insightsTimeXFormat(Long.parseLong(timeFieldValue));
							responseTemplateJson.addProperty(DerivedOperations.INSIGHTSTIMEX_DB.getValue(),
									dateTimeFromEpoch);
						}
					}
				} else if (operationName.equalsIgnoreCase(DerivedOperations.TIMEFIELDMAPPING.getValue())) {
					String timeFieldKey = operationFieldsList.get("mappingTimeField").getAsString();
					timeFieldValue = fetchValuefromJson(timeFieldKey, responseTemplateJson);
					if (timeFieldValue != null) {
						dateFormat = operationFieldsList.get("mappingTimeFormat").getAsString();
						epochTime = InsightsUtils.getEpochTime(timeFieldValue, dateFormat);
						responseTemplateJson.addProperty(timeFieldKey + "_epoch", epochTime);
					}
				} else if (operationName.equalsIgnoreCase(DerivedOperations.DATAENRICHMENT.getValue())) {
					processDataEnrichment(operationFieldsList, responseTemplateJson);
				}
			} catch (Exception e) {
				LOG.error("Error while Webhook derived operation {} and configuration {} Error is {}",
						webhookDerivedConfig.getOperationName(), webhookDerivedConfig.getOperationFields(), e);
				throw new InsightsCustomException(e.getMessage());
			}
		}
	}

	/**
	 * This method is use to apply parsing of the Dynamic Template.
	 * 
	 * @param webhookConfig
	 * @param finalJson
	 */
	private List<JsonObject> parseDynamicTemplate(WebHookConfig webhookConfig, String message) {
		InsightsDynamicJsonParser dynamicParser = new InsightsDynamicJsonParser();
		ObjectMapper mapper = new ObjectMapper();
		List<JsonObject> responceDynamicTemplateFinalDataList = new ArrayList(0);
		try {
			JsonNode nodePayloadData = mapper.readTree(message);
			JsonNode nodeResponseTemplate = mapper.readTree(webhookConfig.getDynamicTemplate());
			responceDynamicTemplateFinalDataList = dynamicParser.parserResponseTemplate(nodeResponseTemplate,
					nodePayloadData);
		} catch (Exception e) {
			LOG.error(" Error while parseDynamicTemplate {} ", e);
		}
		return responceDynamicTemplateFinalDataList;
	}

	/**
	 * This method is used to merge both json and create one json object list
	 * 
	 * @param responseDynamicTemplateList
	 * @param responseTemplateJson
	 * @return
	 */
	private void mergeData(List<JsonObject> responseDynamicTemplateList, JsonObject responseTemplateJson,
			List<JsonObject> retrunJsonList) {
		if (!responseTemplateJson.entrySet().isEmpty()) {
			for (JsonObject jsonObject : responseDynamicTemplateList) {
				JsonObject mergeJson = EngineUtils.mergeTwoJson(jsonObject, responseTemplateJson);
				retrunJsonList.add(mergeJson);
			}
		} else {
			retrunJsonList.addAll(responseDynamicTemplateList);
		}
	}

	/**
	 * Method to fetch the property field required for derived operation,from the
	 * JSON created from Response/Dynamic template
	 * 
	 * @param webhookDerivedConfig
	 * @param parser
	 * @param responseTemplateJson
	 * @param rabbitMqflattenedJsonMap
	 */

	public String fetchValuefromJson(String timeFieldKey, JsonObject responseTemplateJson) {
		try {
			if (responseTemplateJson.has(timeFieldKey)) {
				return responseTemplateJson.get(timeFieldKey).getAsString();
			} else {
				LOG.error(
						" property not found in extracted data json for insightsTimex or timeFieldSeriesMapping in timeFieldValue");
				return null;
			}
		} catch (Exception e) {
			LOG.error("Failed to fetch the value of the field {}", e);
			throw e;
		}
	}

	/**
	 * This method is used to process Data Enrichment
	 * 
	 * @param jsonOperationField
	 * @param responseTemplateJson
	 */
	public void processDataEnrichment(JsonObject jsonOperationField, JsonObject responseTemplateJson) {
		try {
			if (responseTemplateJson.has(jsonOperationField.get("sourceProperty").getAsString())) {
				String sourceFieldValue = responseTemplateJson
						.get(jsonOperationField.get("sourceProperty").getAsString()).toString();
				String keyPattern = jsonOperationField.get("keyPattern").getAsString();
				String targetProperty = jsonOperationField.get("targetProperty").getAsString();
				String enrichedData = DataEnrichUtils.dataExtractor(sourceFieldValue, keyPattern);
				if (enrichedData != null) {
					responseTemplateJson.addProperty(targetProperty, enrichedData);
				}
			} else {
				LOG.error(" Data Enrichment source property not found in extracted data json ");
			}
		} catch (Exception e) {
			LOG.error(" Error while processDataEnrichment {} ", e);
			throw e;
		}
	}

	/**
	 * Method used to create the map of the "String" Response template,entered by
	 * the user.
	 * 
	 * @param responseTemplate
	 * @return
	 */
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