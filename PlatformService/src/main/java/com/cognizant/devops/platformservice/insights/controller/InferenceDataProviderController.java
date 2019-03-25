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

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformservice.insights.service.InsightsInference;
import com.cognizant.devops.platformservice.insights.service.InsightsInferenceService;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


@RestController
@RequestMapping("/datasource/inference")
public class InferenceDataProviderController{

	private static Logger LOG = LogManager.getLogger(InsightsInferenceController.class);

	@Autowired
	InsightsInferenceService insightsInferenceService;

	@RequestMapping(value = "/data", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public JsonArray getInferenceData(HttpServletRequest request) {
		String input = null;
		try {
			input = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		} catch (Exception e) {
			LOG.error(e);
		}
		JsonArray result = new JsonArray();
		JsonArray inferenceInputFromPanel = new JsonParser().parse(input).getAsJsonArray();
		for(JsonElement jsonElemFromDS: inferenceInputFromPanel.getAsJsonArray()) 
		{
			String schedule = jsonElemFromDS.getAsJsonObject().get("vectorSchedule").getAsString();
			List<InsightsInference> inferences = insightsInferenceService.getInferenceDetails(schedule);
			JsonObject responseOutputFromES = PlatformServiceUtil.buildSuccessResponseWithData(inferences);
			for(JsonElement jsonElemES : responseOutputFromES.get("data").getAsJsonArray())
			{
				if(((JsonObject) jsonElemES).get("heading").getAsString().equals(
						jsonElemFromDS.getAsJsonObject().get("vectorType").getAsString())) {
							result.add(jsonElemES.getAsJsonObject());
							break;
				}
			}
		}
		return result;
	}
	@RequestMapping(value = "/data/testDataSource", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public JsonObject checkInferenceDS() {
		JsonObject result = new JsonObject();
		result.addProperty("result", "success");
		return result;
	}
}
