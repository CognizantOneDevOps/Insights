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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformservice.insights.service.InsightsInference;
import com.cognizant.devops.platformservice.insights.service.InsightsInferenceReportServiceImpl;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/externalApi")
public class InferenceDataProviderController {

	public static final String VECTORSCHEDULE = "vectorSchedule";
	public static final String VECTORTYPE = "vectorType";
	private static Logger log = LogManager.getLogger(InferenceDataProviderController.class);
	
	@Autowired
	InsightsInferenceReportServiceImpl insightsInferenceReportService;
	
	/** for Grafana 7.1.0 and InferencePanel Report **/
	@PostMapping(value = "/inference/data/report", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonArray getInferenceReportData(HttpServletRequest request) {
		log.debug(
				" inside getInferenceData call /datasource/inference/data/report/getInferenceReportData ========================== ");
		String input = null;
		JsonArray result = new JsonArray();
		List<InsightsInference> inferences = new ArrayList<>();
		try {
			input = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			JsonArray inferenceInputFromPanel = JsonUtils.parseStringAsJsonArray(input);
			JsonObject jsonInputFromPanel = inferenceInputFromPanel.get(0).getAsJsonObject();
			String schedule = jsonInputFromPanel.get(VECTORSCHEDULE).getAsString();
			String vectorType = jsonInputFromPanel.get(VECTORTYPE).getAsString();
			log.debug(" get getInferenceReportData for group {} ", vectorType);
			inferences = insightsInferenceReportService.getInferenceDetailsVectorWise(schedule, vectorType);
			if (!inferences.isEmpty()) {
				JsonObject inferenceDetailsJson = new Gson().toJsonTree(inferences).getAsJsonArray().get(0)
						.getAsJsonObject();
				JsonArray inferenceJsonArray = new JsonArray();
				inferenceJsonArray.add(inferenceDetailsJson);
				result.add(inferenceJsonArray);
			}
		} catch (Exception e) {
			log.error(e);
		}
		return result;
	}

	/** for Grafana 7.1.0 **/
	@PostMapping(value = "/inference/data/report/testDataSource", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject checkInferenceDSReport() {
		JsonObject result = new JsonObject();

		result.addProperty(PlatformServiceConstants.RESULT, PlatformServiceConstants.SUCCESS);

		return result;
	}
}
