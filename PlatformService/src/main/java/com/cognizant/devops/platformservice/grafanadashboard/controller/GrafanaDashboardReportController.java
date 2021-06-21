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

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaDashboardPdfConfig;
import com.cognizant.devops.platformservice.grafanadashboard.service.GrafanaPdfService;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RestController
@RequestMapping("/dashboardReport")
public class GrafanaDashboardReportController {

	static Logger log = LogManager.getLogger(GrafanaDashboardReportController.class);

	@Autowired
	GrafanaPdfService grafanaPdfServiceImpl;
	
	@PostMapping(value = "/exportPDF/getDashboardAsPDF", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject publishGrafanaDashboardDetails(@RequestBody String dashboardDetails) {
		log.debug("Dashboard details to generate pdf == {}", dashboardDetails);
		String message = null;
		try {
			JsonObject detailsJson = new JsonParser().parse(dashboardDetails).getAsJsonObject();
			grafanaPdfServiceImpl.saveGrafanaDashboardConfig(detailsJson);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}
	
	@GetMapping(value = "/exportPDF/fetchGrafanaDashboardConfigs", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject fetchGrafanaDashboardConfigs(){
		List<GrafanaDashboardPdfConfig> result = null;
		JsonArray jsonarray = new JsonArray();
		try{
			result = grafanaPdfServiceImpl.getAllGrafanaDashboardConfigs();
			for (GrafanaDashboardPdfConfig reportTemplate : result) {
				JsonObject jsonobject = new JsonObject();
				jsonobject.addProperty("id", reportTemplate.getId());
				jsonobject.addProperty("dashboardJson", reportTemplate.getDashboardJson());
				jsonobject.addProperty("email", reportTemplate.getEmail());
				jsonobject.addProperty("emailbody", reportTemplate.getEmailbody());
				jsonobject.addProperty("pdfType", reportTemplate.getPdfType());
				jsonobject.addProperty("scheduleType", reportTemplate.getScheduleType());
				jsonobject.addProperty("status", reportTemplate.getStatus());
				jsonobject.addProperty("title", reportTemplate.getTitle());
				jsonobject.addProperty("variables", reportTemplate.getVariables());
				jsonobject.addProperty("source", reportTemplate.getSource());
				jsonobject.addProperty("workflowId", reportTemplate.getWorkflowConfig().getWorkflowId());
				jsonarray.add(jsonobject);
			}
			return PlatformServiceUtil.buildSuccessResponseWithData(jsonarray);
		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}
	
	@PostMapping(value = "/exportPDF/updateDashboardConfig", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject updateGrafanaDashboardDetails(@RequestBody String dashboardDetails) {
		log.debug("Updating Dashboard details to generate pdf == {}", dashboardDetails);
		String message = null;
		try {
			JsonObject detailsJson = new JsonParser().parse(dashboardDetails).getAsJsonObject();
			grafanaPdfServiceImpl.updateGrafanaDashboardDetails(detailsJson);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}
	
	@PostMapping(value = "/exportPDF/deleteDashboardConfig", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject deleteGrafanaDashboardDetails(@RequestParam int id) {
		log.debug("Deleting Dashboard details for == {}", id);
		String message = null;
		try {
			grafanaPdfServiceImpl.deleteGrafanaDashboardDetails(id);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}
	
}
