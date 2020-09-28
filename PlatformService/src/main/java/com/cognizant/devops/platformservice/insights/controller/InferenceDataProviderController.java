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
package com.cognizant.devops.platformservice.insights.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformservice.insights.service.InsightsInference;
import com.cognizant.devops.platformservice.insights.service.InsightsInferenceService;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RestController
@RequestMapping("/datasource/inference")
public class InferenceDataProviderController {

	private static Logger LOG = LogManager.getLogger(InferenceDataProviderController.class);

	@Autowired
	InsightsInferenceService insightsInferenceService;

	@Autowired
	InsightsInferenceService insightsInferenceReportService;

	@PostMapping(value = "/data", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonArray getInferenceData(HttpServletRequest request) {
		LOG.debug(
				" inside getInferenceData call /datasource/inference/data ============================================== ");
		String input = null;
		List<InsightsInference> inferences = null;
		try {
			input = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		} catch (Exception e) {
			LOG.error(e);
		}
		JsonArray result = new JsonArray();
		JsonArray inferenceInputFromPanel = new JsonParser().parse(input).getAsJsonArray();
		for (JsonElement jsonElemFromDS : inferenceInputFromPanel.getAsJsonArray()) {
			String schedule = jsonElemFromDS.getAsJsonObject().get("vectorSchedule").getAsString();
			String vectorType = jsonElemFromDS.getAsJsonObject().get("vectorType").getAsString();
			LOG.debug(" for vector type {} ", vectorType);
			if (vectorType == null || "".equalsIgnoreCase(vectorType)) {
				inferences = insightsInferenceService.getInferenceDetails(schedule);
			} else {
				inferences = insightsInferenceService.getInferenceDetailsVectorWise(schedule, vectorType);
			}
			JsonObject responseOutputFromES = PlatformServiceUtil.buildSuccessResponseWithData(inferences);
			for (JsonElement jsonElemES : responseOutputFromES.get("data").getAsJsonArray()) {
				if (((JsonObject) jsonElemES).get("heading").getAsString()
						.equals(jsonElemFromDS.getAsJsonObject().get("vectorType").getAsString())) {
					result.add(jsonElemES.getAsJsonObject());
					break;
				}
			}
		}
		return result;
	}

	@GetMapping(value = "/data/testDataSource", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject checkInferenceDS() {
		JsonObject result = new JsonObject();
		result.addProperty("result", "success");
		return result;
	}

	/** for Grafana 7.1.0 **/
	@PostMapping(value = "/data/v7", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonArray getInferenceData7(HttpServletRequest request) {
		LOG.debug(
				" inside getInferenceData call /datasource/inference/data ============================================== ");
		String input = null;
		List<InsightsInference> inferences = null;
		try {
			input = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		} catch (Exception e) {
			LOG.error(e);
		}
		JsonArray result = new JsonArray();
		JsonArray inferenceInputFromPanel = new JsonParser().parse(input).getAsJsonArray();
		for (JsonElement jsonElemFromDS : inferenceInputFromPanel.getAsJsonArray()) {
			String schedule = jsonElemFromDS.getAsJsonObject().get("vectorSchedule").getAsString();
			String vectorType = jsonElemFromDS.getAsJsonObject().get("vectorType").getAsString();
			LOG.debug(" for vector type {} ", vectorType);
			if (vectorType == null || "".equalsIgnoreCase(vectorType)) {
				inferences = insightsInferenceService.getInferenceDetails(schedule);
			} else {
				inferences = insightsInferenceService.getInferenceDetailsVectorWise(schedule, vectorType);
			}
			JsonObject responseOutputFromES = PlatformServiceUtil.buildSuccessResponseWithData(inferences);
			for (JsonElement jsonElemES : responseOutputFromES.get("data").getAsJsonArray()) {
				if (((JsonObject) jsonElemES).get("heading").getAsString()
						.equals(jsonElemFromDS.getAsJsonObject().get("vectorType").getAsString())) {
					JsonArray ja = new JsonArray();
					ja.add(jsonElemES.getAsJsonObject());
					result.add(ja);
					break;
				}
			}
		}
		return result;
	}

	/** for Grafana 7.1.0 and InferencePanel Report **/
	@PostMapping(value = "/data/report", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonArray getInferenceReportData(HttpServletRequest request) {
		LOG.debug(
				" inside getInferenceData call /datasource/inference/data/report/getInferenceReportData ========================== ");
		String input = null;
		JsonArray result = new JsonArray();
		List<InsightsInference> inferences = new ArrayList<>();
		try {
			input = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			JsonArray inferenceInputFromPanel = new JsonParser().parse(input).getAsJsonArray();
			JsonObject jsonInputFromPanel = inferenceInputFromPanel.get(0).getAsJsonObject();
			String schedule = jsonInputFromPanel.get("vectorSchedule").getAsString();
			String vectorType = jsonInputFromPanel.get("vectorType").getAsString();
			LOG.debug(" get getInferenceReportData for group {} ", vectorType);
			inferences = insightsInferenceReportService.getInferenceDetailsVectorWise(schedule, vectorType);
			if (!inferences.isEmpty()) {
				JsonObject inferenceDetailsJson = new Gson().toJsonTree(inferences).getAsJsonArray().get(0)
						.getAsJsonObject();
				JsonArray inferenceJsonArray = new JsonArray();
				inferenceJsonArray.add(inferenceDetailsJson);
				result.add(inferenceJsonArray);
			}
		} catch (Exception e) {
			LOG.error(e);
		}
		return result;
	}

	/** for Grafana 7.1.0 **/
	@PostMapping(value = "/data/v7/testDataSource", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject checkInferenceDS7() {
		JsonObject result = new JsonObject();
		result.addProperty("result", "success");
		return result;
	}
}
