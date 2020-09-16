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
package com.cognizant.devops.platformreports.assessment.util;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformreports.assessment.datamodel.InsightsKPIConfigDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ReportEngineUtils {

	private static final Logger log = LogManager.getLogger(ReportEngineUtils.class);
	public static final String TIMEZONE = "GMT";
	public static final String NEO4J_RESULT_LABEL = "KPI:RESULTS";
	public static final String NEO4J_CONFIG_LABEL = "KPI:CONFIG";
	public static final String NEO4J_CONTENT_CONFIG_LABEL = "KPI:CONTENT_CONFIG";
	public static final String NEO4J_CONTENT_RESULT_LABEL = "KPI:CONTENT_RESULT";
	public static final String CONFIG_DIR = ".InSights";
	public static final String CONFIG_FILE = "ContentConfiguration.json";
	public static final String INSIGHTS_HOME = "INSIGHTS_HOME";
	public static final String CONTENT_CONFIG_FILE = System.getenv().get(INSIGHTS_HOME) + File.separator + CONFIG_DIR
			+ File.separator + CONFIG_FILE;
	public static final String STANDARD_MESSAGE_KEY = "contentMessage";
	public static final String NEUTRAL_MESSAGE_KEY = "neutralMessage";
	public static final String COLUMN_PROPERTY = "columnProperty";
	public static final String START_TIME_FIELD = "startTime";
	public static final String END_TIME_FIELD = "endTime";

	public static final String ES_KPI_RESULT_INDEX = "kpi-results";

	public static final String ES_CONTENT_RESULT_INDEX = "content-results";

	public static final String ES_CONTENT_RESULT_SORT_COLUMN = "executionId";

	public static final String REPORT_CONFIG_DIR = "assessmentReportPdfTemplate";
	public static final String REPORT_CONFIG_TEMPLATE_DIR = "reportTemplates";
	public static final String REPORT_PDF_RESOLVED_PATH = System.getenv().get(INSIGHTS_HOME) + File.separator
			+ REPORT_CONFIG_DIR + File.separator;
	public static final String REPORT_PDF_EXECUTION_RESOLVED_PATH = System.getenv().get(INSIGHTS_HOME) + File.separator
			+ REPORT_CONFIG_DIR + File.separator + "executionsDetail" + File.separator;

	public static final String REPORT_TYPE = "pdf";

	public static final String REPORT_MEDIA_TYPE = "application/pdf";

	private ReportEngineUtils() {
	}

	public static JsonObject mergeTwoJson(JsonObject json1Obj, JsonObject json2Obj) {

		Set<Entry<String, JsonElement>> entrySet1 = json1Obj.entrySet();
		for (Entry<String, JsonElement> entry : entrySet1) {
			String key1 = entry.getKey();
			if (json2Obj.get(key1) != null) {
				JsonElement tempEle2 = json2Obj.get(key1);
				JsonElement tempEle1 = entry.getValue();
				if (tempEle2.isJsonObject() && tempEle1.isJsonObject()) {
					JsonObject mergedObj = mergeTwoJson(tempEle1.getAsJsonObject(), tempEle2.getAsJsonObject());
					entry.setValue(mergedObj);
				}
			}
		}
		Set<Entry<String, JsonElement>> entrySet2 = json2Obj.entrySet();
		for (Entry<String, JsonElement> entry : entrySet2) {
			String key2 = entry.getKey();
			if (json1Obj.get(key2) == null) {
				json1Obj.add(key2, entry.getValue());
			}
		}
		return json1Obj;
	}

	/**
	 * get InferencePropertyJson from inference/job definition
	 * 
	 * @return
	 */
	public static JsonObject getInferencePropertyJson(InsightsKPIConfigDTO inferenceConfigDefinition) {
		Gson gson = new Gson();
		ObjectMapper oMapper = new ObjectMapper();
		@SuppressWarnings("unchecked")
		Map<String, Object> resultMap = oMapper.convertValue(inferenceConfigDefinition, Map.class);
		JsonParser parser = new JsonParser();
		String prettyJson = gson.toJson(resultMap);
		JsonObject dataJson = parser.parse(prettyJson).getAsJsonObject();
		return dataJson;
	}

	/**
	 * get InferencePropertyJson from inference/job definition
	 * 
	 * @return
	 */

	public static Object getNodeValue(String key, JsonNode valueNode) {
		Object value = null;
		if (!valueNode.getNodeType().toString().equalsIgnoreCase("NULL")) {
			if (valueNode.isBoolean()) {
				value = valueNode.asBoolean();
			} else if (valueNode.isLong() || valueNode.isBigInteger() || valueNode.isIntegralNumber()) {
				value = valueNode.asLong();
			} else if (valueNode.isDouble()) {
				value = valueNode.asDouble();
			} else if (valueNode.isTextual()) {
				value = valueNode.asText();
			} else if (valueNode.isObject()) {
				value = String.valueOf(valueNode);
			}
		} else {
			log.debug(" getNodeValue value is null for key {} ", key);
		}
		return value;
	}


	public static Object getJsonValue(JsonElement jsonElementResult) {
		Object result = null;
		if (jsonElementResult.isJsonPrimitive()) {
			if (jsonElementResult.getAsJsonPrimitive().isBoolean())
				return jsonElementResult.getAsBoolean();
			if (jsonElementResult.getAsJsonPrimitive().isString())
				return jsonElementResult.getAsString();
			if (jsonElementResult.getAsJsonPrimitive().isNumber()) {
				return jsonElementResult.getAsDouble();
			}
		} else {
			result = jsonElementResult.getAsString();
		}
		return result;
	}
}