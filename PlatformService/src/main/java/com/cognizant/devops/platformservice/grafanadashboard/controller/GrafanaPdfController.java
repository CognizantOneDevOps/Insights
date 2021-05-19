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
package com.cognizant.devops.platformservice.grafanadashboard.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.grafanadashboard.service.GrafanaPdfService;
import com.cognizant.devops.platformservice.rest.querycaching.service.QueryCachingConstants;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RestController
@RequestMapping("/datasource")
public class GrafanaPdfController {

	static Logger log = LogManager.getLogger(GrafanaPdfController.class);

	@Autowired
	GrafanaPdfService grafanaPdfServiceImpl;
	
	@PostMapping(value = "/exportPDF/getDashboardAsPDF", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject publishGrafanaDashboardDetails(@RequestBody String dashboardDetails) {
		log.debug("Dashboard details to generate pdf == {}", dashboardDetails);
		String message = null;
		JsonObject result = new JsonObject();
		try {
			JsonObject detailsJson = new JsonParser().parse(dashboardDetails).getAsJsonObject();
			String isTestDBConnectivity = detailsJson.get(QueryCachingConstants.METADATA).getAsJsonArray()
					.get(QueryCachingConstants.ZEROTH_INDEX).getAsJsonObject().get(QueryCachingConstants.TEST_DATABASE)
					.toString();
			if (isTestDBConnectivity.equals("true")) {
				log.debug("\n\nGrafana PDF export Test Request For Data Source Connectivity Found.");
				JsonArray jsonArray = new JsonArray();
				jsonArray.add("GRAFANA_EXPORT_TEST");
				result.add("results", jsonArray);
				return result;
			} else {
				grafanaPdfServiceImpl.saveGrafanaDashboardConfig(detailsJson);
			}
			
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}
	
}
