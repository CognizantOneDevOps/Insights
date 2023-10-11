/*******************************************************************************
 * Copyright 2023 Cognizant Technology Solutions
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
package com.cognizant.devops.platformservice.offlinealerting.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.offlineAlerting.InsightsOfflineAlerting;
import com.cognizant.devops.platformservice.offlinealerting.service.OfflineAlertingService;
import com.cognizant.devops.platformservice.rest.AccessGroupManagement.AccessGroupManagement;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/insights/offlineAlerting")
public class OfflineAlertingController {
	private static Logger log = LogManager.getLogger(OfflineAlertingController.class);

	@Autowired
	AccessGroupManagement accessGroupManagement;

	@Autowired
	OfflineAlertingService offlineAlertingService;

	@GetMapping(value = "/fetchOfflineAlertList", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getAllOfflineDataList() {
		try {
			JsonArray jsonarray = new JsonArray();
			List<InsightsOfflineAlerting> result = offlineAlertingService.getAllOfflineAlertingConfig();
			for (InsightsOfflineAlerting alertConfig : result) {
				JsonObject jsonobject = new JsonObject();
				JsonObject alertJson = JsonUtils.parseStringAsJsonObject(alertConfig.getAlertJson());
				jsonobject.addProperty("dashboardName", alertJson.get("dashName").getAsString());
				jsonobject.addProperty("panelName", alertJson.get("panelName").getAsString());
				jsonobject.addProperty("alertName", alertConfig.getAlertName());
				jsonobject.addProperty("scheduleType", alertConfig.getScheduleType());
				jsonobject.addProperty("threshold", alertConfig.getThreshold());
				jsonobject.addProperty("frequency", alertConfig.getFrequency());
				jsonobject.addProperty("trend", alertConfig.getTrend());
				jsonobject.addProperty("alertJson", alertConfig.getAlertJson());
				jsonobject.addProperty("isActive", alertConfig.getWorkflowConfig().isActive());
				jsonobject.addProperty("status", alertConfig.getStatus());
				jsonobject.addProperty("nextRunTime", alertConfig.getWorkflowConfig().getNextRun());
				jsonarray.add(jsonobject);
			}

			return PlatformServiceUtil.buildSuccessResponseWithHtmlData(jsonarray);
		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}

	@PostMapping(value = "/saveAlertData", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject saveAlertData(@RequestBody String alertDetails) {
		log.debug("Alert details == {}", alertDetails);
		String message = null;
		try {
			JsonObject detailsJson = JsonUtils.parseStringAsJsonObject(alertDetails);
			offlineAlertingService.saveAlertConfig(detailsJson);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}

	@PostMapping(value = "/updateAlertData", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject updateAlertData(@RequestBody String alertDetails) {
		log.debug("Alert details == {}", alertDetails);
		String message = null;
		try {
			JsonObject detailsJson = JsonUtils.parseStringAsJsonObject(alertDetails);
			offlineAlertingService.updateAlertConfig(detailsJson);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}

	@PostMapping(value = "/updateOfflineAlertStatus", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject updateAlertConfigStatus(@RequestBody String statusConfig) {
		String message = null;
		try {
			JsonObject configStatusJson = JsonUtils.parseStringAsJsonObject(statusConfig);
			message = offlineAlertingService.updateAlertConfigStatus(configStatusJson);
			log.debug("Alert Status updated successfully .");
			return PlatformServiceUtil.buildSuccessResponseWithData(message);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}

	@PostMapping(value = "/alertExecutionRecordsByWorkflowId", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getWorkflowExecutionRecordsByWorkflowId(@RequestBody String workflowIdJson) {
		JsonObject records = null;
		try {
			workflowIdJson = workflowIdJson.replace("\n", "").replace("\r", "");
			String validatedRequestJson = ValidationUtils.validateRequestBody(workflowIdJson);
			JsonObject validatedConfigIdJson = JsonUtils.parseStringAsJsonObject(validatedRequestJson);
			records = offlineAlertingService.getAlertExecutionRecordsByWorkflowId(validatedConfigIdJson);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(records);
	}

	@PostMapping(value = "/deleteOfflineAlert", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject deleteOfflineAlert(@RequestBody String deleteAlertRequest) {
		try {
			JsonObject offlineAlertResponse = new JsonObject();
			deleteAlertRequest = deleteAlertRequest.replace("\n", "").replace("\r", "");
			String validatedEditResponse = ValidationUtils.validateRequestBody(deleteAlertRequest);
			JsonObject offlineAlertJson = JsonUtils.parseStringAsJsonObject(validatedEditResponse);
			boolean isRecordDeleted = offlineAlertingService.deleteOfflineAlert(offlineAlertJson);
			if (isRecordDeleted) {
				offlineAlertResponse.addProperty(PlatformServiceConstants.MESSAGE,
						"Offline alert deleted for alert name " + offlineAlertJson.get("alertName"));
				return PlatformServiceUtil.buildSuccessResponseWithData(offlineAlertResponse);
			} else {
				offlineAlertResponse.addProperty(PlatformServiceConstants.MESSAGE,
						"Offline alert not deleted for alert name " + offlineAlertJson.get("alertName"));
				return PlatformServiceUtil.buildFailureResponse(offlineAlertResponse.getAsString());
			}
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse("Unable to delete Offline Alert");
		}
	}
}
