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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaDashboardPdfConfig;
import com.cognizant.devops.platformservice.grafanadashboard.service.GrafanaPdfService;
import com.cognizant.devops.platformservice.rest.AccessGroupManagement.AccessGroupManagement;
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
	
	@Autowired
	AccessGroupManagement accessGroupManagement;

	
	@PostMapping(value = "/exportPDF/saveDashboardAsPDF", produces = MediaType.APPLICATION_JSON_VALUE)
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
		JsonObject currentUserWithOrgs = new JsonObject();
		Map<String, String> userOrgsMap = new HashMap<>();
		try{
			currentUserWithOrgs = accessGroupManagement.getCurrentUserWithOrgs();
			JsonObject currentUserWithOrgsData = (JsonObject) currentUserWithOrgs.get("data");
			JsonArray orgArray = (JsonArray) currentUserWithOrgsData.get("orgArray");
			for(int i = 0; i < orgArray.size(); i++) {
				JsonObject jsonObject = orgArray.get(i).getAsJsonObject();
				userOrgsMap.put(jsonObject.get("orgId").toString(), jsonObject.get("name").getAsString());
			}
			result = grafanaPdfServiceImpl.getAllGrafanaDashboardConfigs();
			for (GrafanaDashboardPdfConfig dashboardConfig : result) {
				JsonParser parser = new JsonParser();
				JsonObject dashJson = (JsonObject) parser.parse(dashboardConfig.getDashboardJson());
				String orgId = dashJson.get("organisation").getAsString();
				if(!userOrgsMap.containsKey(orgId)) {
					continue;
				}
				JsonObject jsonobject = new JsonObject();
				jsonobject.addProperty("id", dashboardConfig.getId());
				jsonobject.addProperty("dashboardJson", dashboardConfig.getDashboardJson());
				jsonobject.addProperty("pdfType", dashboardConfig.getPdfType());
				jsonobject.addProperty("scheduleType", dashboardConfig.getScheduleType());
				jsonobject.addProperty("status", dashboardConfig.getWorkflowConfig().getStatus());
				jsonobject.addProperty("title", dashboardConfig.getTitle());
				jsonobject.addProperty("variables", dashboardConfig.getVariables());
				jsonobject.addProperty("source", dashboardConfig.getSource());
				jsonobject.addProperty("workflowId", dashboardConfig.getWorkflowConfig().getWorkflowId());
				jsonobject.addProperty("orgName", userOrgsMap.get(orgId));
				jsonobject.addProperty("isActive", dashboardConfig.getWorkflowConfig().isActive());
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

	@PostMapping(value = "/updateDasboardStatus", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject updateDashboardStatus(@RequestBody String dashboardIdJsonString) {
		String message = null;
		try {
			JsonParser parser = new JsonParser();
			JsonObject dashboardIdJson = (JsonObject) parser.parse(dashboardIdJsonString);
			message = grafanaPdfServiceImpl.updateDashboardPdfConfigStatus(dashboardIdJson);		
			return PlatformServiceUtil.buildSuccessResponseWithData(message);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}
	
	@GetMapping(value = "/getEmailConfigurationStatus", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getEmailConfigurationStatus() {
		Boolean isEmailConfigured = ApplicationConfigProvider.getInstance().getEmailConfiguration().getSendEmailEnabled();		
		return PlatformServiceUtil.buildSuccessResponseWithData(isEmailConfigured);
	}
	
	@PostMapping(value = "/setDashboardActiveState", produces = MediaType.APPLICATION_JSON_VALUE) 
	public @ResponseBody  JsonObject setDashboardActiveState(@RequestBody String dashboardUpdateJsonString) {
		String message = null;
		try {
			JsonParser parser = new JsonParser();
			JsonObject dashboardUpdateJson = (JsonObject) parser.parse(dashboardUpdateJsonString);
			message = grafanaPdfServiceImpl.setDashboardActiveState(dashboardUpdateJson);
			return PlatformServiceUtil.buildSuccessResponseWithData(message);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}
	
}
