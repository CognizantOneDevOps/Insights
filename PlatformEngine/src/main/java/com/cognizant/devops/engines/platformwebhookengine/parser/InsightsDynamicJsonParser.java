/*******************************************************************************
 * * Copyright 2017 Cognizant Technology Solutions
 * *
 * * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * * use this file except in compliance with the License. You may obtain a copy
 * * of the License at
 * *
 * * http://www.apache.org/licenses/LICENSE-2.0
 * *
 * * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * * License for the specific language governing permissions and limitations
 * under
 * * the License.
 *******************************************************************************/
package com.cognizant.devops.engines.platformwebhookengine.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.engines.util.EngineUtils;
import com.cognizant.devops.platformcommons.core.enums.ResultOutputType;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class InsightsDynamicJsonParser {
	private static Logger LOG = LogManager.getLogger(InsightsDynamicJsonParser.class);
	static String nodeCreationType = ResultOutputType.COMBINED_NODE.getValue();

	/***
	 * Method written to parse the dynamic template from the payload
	 * 
	 * @param nodeResponseTemplate3
	 * @param nodeOriginalData
	 * @param recordHirarchy
	 */
	public List<JsonObject> parserResponseTemplate(JsonNode nodeResponseTemplate, JsonNode nodeOriginalData) {
		LOG.debug("  nodeCreationType {} getNodeType {} isContainerNode {} ", nodeCreationType,
				nodeOriginalData.getNodeType(), nodeOriginalData.isContainerNode());
		List<JsonObject> responceDataList = new ArrayList<>(0);
		List<JsonObject> responceFinalDataList = new ArrayList<>(0);
		JsonObject responceData = new JsonObject();
		try {
			//LOG.debug(" inside parser Response Template Object", nodeOriginalData);			
			for (Iterator<Map.Entry<String, JsonNode>> it = nodeResponseTemplate.fields(); it.hasNext();) {
				Map.Entry<String, JsonNode> field = it.next();
				String key = field.getKey();
				JsonNode valueResponse = field.getValue();
				// LOG.debug("key: {} value: {} ", key, valueResponse);
				JsonNode jsonChildNode = nodeOriginalData.get(key);
				/*LOG.debug(
						" Response Template Field Name ===== key {} responseTemplatevalue {}  is {} data value type {}  ",
						key, valueResponse.asText(), jsonChildNode.asText(), jsonChildNode.getNodeType()); */
				if (jsonChildNode != null) {
					if (jsonChildNode.isArray()) {
						processJsonChildArray(responceDataList, responceData, key, valueResponse, jsonChildNode);
					} else if (jsonChildNode.isObject()) {
						parseResponseChildObject(valueResponse, jsonChildNode, responceData, key);
					} else if (!jsonChildNode.isContainerNode()) {
						getNodeValue(jsonChildNode.toString(), jsonChildNode, valueResponse, responceData);
					}
				} else if (valueResponse != null && valueResponse.isArray() && key.equalsIgnoreCase("extensions")) {
					processExtension(valueResponse, nodeOriginalData, responceData);
				}
			}

			prepareFinalExtractedDataList(responceDataList, responceFinalDataList, responceData);
		} catch (Exception e) {
			LOG.error(" Error while parsing dynamic response template", e);
		}
		// LOG.debug(" responceFinalDataList {} ", responceFinalDataList);
		return responceFinalDataList;
	}

	/**
	 * process child array
	 * 
	 * @param responceDataList
	 * @param responceData
	 * @param key
	 * @param valueResponse
	 * @param jsonChildNode
	 */
	private void processJsonChildArray(List<JsonObject> responceDataList, JsonObject responceData, String key,
			JsonNode valueResponse, JsonNode jsonChildNode) {
		if (valueResponse.isArray()) {
			List<JsonObject> returnArray = parseResponseChildArray(valueResponse.get(0), jsonChildNode, responceData,
					key);
			responceDataList.addAll(returnArray);
		} else {
			parseResponseChildArray(valueResponse, jsonChildNode, responceData, key);
		}
	}

	/**
	 * Prepare final response based on type
	 * 
	 * @param responceDataList
	 * @param responceFinalDataList
	 * @param responceData
	 */
	private void prepareFinalExtractedDataList(List<JsonObject> responceDataList,
			List<JsonObject> responceFinalDataList, JsonObject responceData) {
		if (nodeCreationType.equals(ResultOutputType.INDIVIDUAL_NODE.getValue()) && !responceDataList.isEmpty()) {// createIndividualNode
			for (JsonObject jsonObject : responceDataList) {
				JsonObject mergeJson = EngineUtils.mergeTwoJson(jsonObject, responceData);
				responceFinalDataList.add(mergeJson);
			}
		} else {
			responceFinalDataList.add(responceData);
		}
	}

	/**
	 * Method to fetch out the array ,if found in the payload ,and mentioned in the
	 * dynamic template
	 * 
	 * @param jsonChildResponseNode
	 * @param jsonArrayNode
	 * @param responceData
	 * @param jsonKey
	 * @return
	 */
	public List<JsonObject> parseResponseChildArray(JsonNode jsonChildResponseNode, JsonNode jsonArrayNode,
			JsonObject responceData, String jsonKey) {
		List<JsonObject> responceDataList = new ArrayList<>(0);
		Iterator<JsonNode> datasetElements = jsonArrayNode.iterator();
		int nodeCount = 0;
		boolean isArrayContainOnlyValueNode = isArrayContainOnlyValueNode(jsonArrayNode);

		// LOG.debug(" inside parseResponseChildArray Array check isArrayContainOnlyValueNode {}  ", isArrayContainOnlyValueNode);

		if (!isArrayContainOnlyValueNode) {
			while (datasetElements.hasNext()) {
				JsonObject childJsonObject;
				nodeCount = nodeCount + 1;
				if (nodeCreationType.equals(ResultOutputType.INDIVIDUAL_NODE.getValue())) {
					childJsonObject = new JsonObject();// merge(childJsonObject, responceData)
				} else if (nodeCreationType.equals(ResultOutputType.COMBINED_NODE.getValue())) {
					childJsonObject = responceData;
				} else if (nodeCreationType.equals(ResultOutputType.COMBINED_WITH_SUB_NODE.getValue())) {
					childJsonObject = new JsonObject();
				} else {
					childJsonObject = new JsonObject();
				}
				JsonNode datasetElement = datasetElements.next();
				Iterator<String> datasetElementFields = datasetElement.fieldNames();
				parseChildDatasetElement(jsonChildResponseNode, jsonKey, childJsonObject, datasetElement,
						datasetElementFields);
				responceDataList.add(childJsonObject);
				if (nodeCreationType.equals(ResultOutputType.COMBINED_WITH_SUB_NODE.getValue())) {
					responceData.add(jsonKey + nodeCount, childJsonObject);
				}
			}
		} else {
			// LOG.debug("Array contain only values {} ", jsonArrayNode);
			getNodeValue(jsonChildResponseNode.asText(), jsonArrayNode, jsonChildResponseNode, responceData);
		}
		// LOG.debug("Array for build " + responceDataList.toString());
		return responceDataList;
	}

	private void parseChildDatasetElement(JsonNode jsonChildResponseNode, String jsonKey, JsonObject childJsonObject,
			JsonNode datasetElement, Iterator<String> datasetElementFields) {
		while (datasetElementFields.hasNext()) {
			String datasetElementField = datasetElementFields.next();
			JsonNode jsonChildNode = datasetElement.get(datasetElementField);
			JsonNode jsonChildNodeResponse = jsonChildResponseNode.get(datasetElementField);
			List<JsonNode> jsonChildNodeResponseList = jsonChildResponseNode.findValues(datasetElementField);
			if (!jsonChildNodeResponseList.isEmpty() && jsonChildNode != null) {
				// LOG.debug("datasetElementField {} ", datasetElementField);
				if (jsonChildNode.isArray()) {
					parseResponseChildArray(jsonChildNodeResponse, jsonChildNode, childJsonObject, jsonKey);
				} else if (jsonChildNode.isObject()) {
					parseResponseChildObject(jsonChildNodeResponse, jsonChildNode, childJsonObject, jsonKey);
				} else if (!jsonChildNode.isContainerNode()) {

					getNodeValue(datasetElementField, jsonChildNode, jsonChildNodeResponse, childJsonObject);
				}
			} else {
				LOG.debug("Field Not in response template or Field Not present in original data {}  ",
						datasetElementField);
			}
		}
	}

	/**
	 * Fetching the details fromm a JSON Object having Key-Value Pair
	 * 
	 * @param jsonChildResponseNode
	 * @param jsonArrayNode
	 * @param responceData
	 * @param jsonKey
	 */
	private void parseResponseChildObject(JsonNode jsonChildResponseNode, JsonNode jsonArrayNode,
			JsonObject responceData, String jsonKey) {
		getResposeNodeDetail(jsonChildResponseNode, jsonArrayNode, responceData, jsonKey);
	}

	/**
	 * Method to fetch the values of the JSON object mentioned in the parent method
	 * 
	 * @param jsonChildResponseNode
	 * @param nodeOriginalData
	 * @param responceData
	 * @param jsonKey
	 */
	public void getResposeNodeDetail(JsonNode jsonChildResponseNode, JsonNode nodeOriginalData, JsonObject responceData,
			String jsonKey) {

		for (Iterator<Map.Entry<String, JsonNode>> it = jsonChildResponseNode.fields(); it.hasNext();) {
			Map.Entry<String, JsonNode> field = it.next();
			String key = field.getKey();
			JsonNode valueResponse = field.getValue();
			// LOG.debug("key: {} value: {}", key, valueResponse);
			JsonNode jsonChildNode = nodeOriginalData.get(key);
			/*
			 * LOG.debug(
			 * "getResposeNodeDetail key: {}  value: {} parseChildArray for field name for NodeValue {}  value is {} "
			 * , key, valueResponse, key, jsonChildNode);
			 */
			if (jsonChildNode != null) {
				if (jsonChildNode.isArray()) {
					if (valueResponse.isArray()) {
						parseResponseChildArray(valueResponse.get(0), jsonChildNode, responceData, jsonKey);
					} else {
						parseResponseChildArray(valueResponse, jsonChildNode, responceData, jsonKey);
					}
				} else if (jsonChildNode.isObject()) {
					// LOG.debug(" parseChildObject for field name for NodeValue {} value is {} ",
					// key, valueResponse);
					parseResponseChildObject(valueResponse, jsonChildNode, responceData, jsonKey);
				} else if (!jsonChildNode.isContainerNode()) {
					getNodeValue(jsonChildNode.toString(), jsonChildNode, valueResponse, responceData);

				}
			} else {
				LOG.error(" Field Not present in original data +  {}  ", key);
			}
		}
	}

	/**
	 * Fetch the JSON node based on the type.
	 * 
	 * @param key
	 * @param valueNode
	 * @param jsonChildResponseNode
	 * @param responceData
	 * @return
	 */
	private Object getNodeValue(String key, JsonNode valueNode, JsonNode jsonChildResponseNode,
			JsonObject responceData) {
		Object value = null;
		if (!valueNode.getNodeType().toString().equalsIgnoreCase("NULL")) {
			if (valueNode.isBoolean()) {
				value = valueNode.asBoolean();
				responceData.addProperty(jsonChildResponseNode.asText(), valueNode.asBoolean());
			} else if (valueNode.isLong()) {
				value = valueNode.asLong();
				responceData.addProperty(jsonChildResponseNode.asText(), valueNode.asLong());
			} else if (valueNode.isDouble()) {
				value = valueNode.asDouble();
				responceData.addProperty(jsonChildResponseNode.asText(), valueNode.asDouble());
			} else if (valueNode.isTextual()) {
				value = valueNode.asText();
				responceData.addProperty(jsonChildResponseNode.asText(), valueNode.asText());
			} else {
				if (valueNode.isArray()) {
					JsonElement tradeElement = JsonUtils.parseString(valueNode.toString());
					JsonArray trade = tradeElement.getAsJsonArray();
					responceData.add(jsonChildResponseNode.asText(), trade);
				} else {
					value = valueNode.toString();
					responceData.addProperty(jsonChildResponseNode.asText(), valueNode.toString());
				}
			}
			LOG.debug(
					" getNodeValue .. only isValueNode  {}  value is {}  data type value {} response template filed {} ",
					key, value, valueNode.getNodeType(), jsonChildResponseNode.asText());
		} else {
			LOG.debug(" getNodeValue Node is null so skipping that node {}   value is {} ",
					jsonChildResponseNode.asText(), valueNode);
		}
		return value;
	}

	/**
	 * Method to check that array contains only values
	 * 
	 * @param jsonArrayNode
	 * @return
	 */
	private boolean isArrayContainOnlyValueNode(JsonNode jsonArrayNode) {
		boolean returnvalue = Boolean.FALSE;
		Iterator<JsonNode> datasetElements = jsonArrayNode.iterator();
		while (datasetElements.hasNext()) {
			JsonNode datasetElement = datasetElements.next();
			if (datasetElement.isValueNode()) {
				returnvalue = Boolean.TRUE;
				break;
			}
		}
		return returnvalue;
	}

	/**
	 * Method to process extensions block present in dynamic template
	 * according to the processing strategy mentioned.
	 * 
	 * @param extensionTemplateNode
	 * @param nodeOriginalData
	 * @param responceData
	 */
	private void processExtension(JsonNode extensionTemplateNode, JsonNode payloadDataNode, JsonObject responceData) {
		try {
			for (JsonNode eachNode : extensionTemplateNode) {
				String processingStrategy = eachNode.get("processingStrategy").asText();
				Iterator<String> extensionsElements = eachNode.fieldNames();
				String templateFieldName = extensionsElements.next();
				JsonNode templateFieldValue = eachNode.get(templateFieldName);
				if (processingStrategy.equalsIgnoreCase("SingleValueArray") && !templateFieldValue.isEmpty()
						&& templateFieldValue.isArray()) {
					JsonNode actualfieldArray = payloadDataNode.get(templateFieldName);
						if (actualfieldArray != null && actualfieldArray.isArray() && actualfieldArray.size() > 0) {
							processExtensionArray(templateFieldValue.get(0), actualfieldArray, responceData);
						} else if (actualfieldArray == null || actualfieldArray.isEmpty()) {
							LOG.debug(" Extension template field - {}  not found in payload, empty value updation ",
									templateFieldName);
						processEmptyExtensionArray(responceData, templateFieldValue);
						}
				} else {
					LOG.error(
							" Processing strategy not found in extension template or Field - {}  is not an array or it's empty in extension template ",
							templateFieldName);
				}
			}
		} catch (Exception e) {
			LOG.error(" Error while processing extensions in dynamic template ", e);
		}
	}

	/**
	 * This method use to process empty extension Array
	 * 
	 * @param responceData
	 * @param templateFieldValue
	 */
	private void processEmptyExtensionArray(JsonObject responceData, JsonNode templateFieldValue) {
		Iterator<String> subElements = templateFieldValue.get(0).fieldNames();
		while (subElements.hasNext()) {
			String subElementName = subElements.next();
			JsonNode subElementValue = templateFieldValue.get(0).get(subElementName);
			responceData.add(subElementValue.asText(), new JsonArray());
		}
	}

	/**
	 * Method to process array present in extensions template as
	 * single value array.
	 * 
	 * @param template
	 * @param actualfieldArray
	 * @param responceData
	 */
	private void processExtensionArray(JsonNode template, JsonNode actualfieldArray, JsonObject responceData) {
		try {
			Iterator<String> fieldNames = template.fieldNames();
			while (fieldNames.hasNext()) {
				String fieldName = fieldNames.next();
				JsonNode fieldValue = template.get(fieldName);
				JsonArray arrayValues = new JsonArray();
				Iterator<JsonNode> arrayElements = actualfieldArray.iterator();
				while (arrayElements.hasNext()) {
					JsonNode arrayElement = arrayElements.next();
					JsonNode valueNode = arrayElement.get(fieldName);
					if (!valueNode.isNull()) {
						if (valueNode.isBoolean()) {
							arrayValues.add(valueNode.asBoolean());
						} else if (valueNode.isLong()) {
							arrayValues.add(valueNode.asLong());
						} else if (valueNode.isDouble()) {
							arrayValues.add(valueNode.asDouble());
						} else if (valueNode.isTextual()) {
							arrayValues.add(valueNode.asText());
						} else {
							arrayValues.add(valueNode.toString());
						}
					}
				}
				responceData.add(fieldValue.asText(), arrayValues);
			}
		} catch (Exception e) {
			LOG.error(" Error while processing array in extension ", e);
		}
	}
}