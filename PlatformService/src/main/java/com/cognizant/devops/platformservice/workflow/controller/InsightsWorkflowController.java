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
package com.cognizant.devops.platformservice.workflow.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.cognizant.devops.platformservice.workflow.service.WorkflowServiceImpl;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RestController
@RequestMapping("/insights/workflow")
public class InsightsWorkflowController {
	private static Logger log = LogManager.getLogger(InsightsWorkflowController.class);
	@Autowired
	WorkflowServiceImpl workflowService;

	@PostMapping(value = "/saveWorkflowTask", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject saveAssessmentTask(@RequestBody String assessmentTask) {

		try {
			assessmentTask = assessmentTask.replace("\n", "").replace("\r", "");
			String validatedResponse = ValidationUtils.validateRequestBody(assessmentTask);
			JsonParser parser = new JsonParser();
			JsonObject workflowTaskJson = (JsonObject) parser.parse(validatedResponse);
			int taskId = workflowService.saveWorkflowTask(workflowTaskJson);
			return PlatformServiceUtil.buildSuccessResponseWithData(" workflow Task created with Id " + taskId);
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse("Unable to save workflow task due to exception");
		}
	}

	@PostMapping(value = "/getTaskList", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getTaskList(@RequestParam String workflowType) {
		JsonArray data = null;
		try {
			data = workflowService.getTaskList(workflowType);
			return PlatformServiceUtil.buildSuccessResponseWithData(data);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}

	}

	@PostMapping(value = "/workFlowExecutionRecords", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getWorkflowExecutionRecords(@RequestBody String configIdJson) {
		JsonObject records = null;
		try {
			configIdJson = configIdJson.replace("\n", "").replace("\r", "");
			String validatedRequestJson = ValidationUtils.validateRequestBody(configIdJson);
			JsonParser parser = new JsonParser();
			JsonObject validatedConfigIdJson = parser.parse(validatedRequestJson).getAsJsonObject();
			records = workflowService.getWorkFlowExecutionRecords(validatedConfigIdJson);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(records);
	}

	@PostMapping(value = "/maxExecutionIDs", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getMaximumExecutionIds(@RequestBody String configIdJson) {
		JsonObject records = null;
		try {
			configIdJson = configIdJson.replace("\n", "").replace("\r", "");
			String validatedRequestJson = ValidationUtils.validateRequestBody(configIdJson);
			JsonParser parser = new JsonParser();
			JsonObject validatedConfigIdJson = parser.parse(validatedRequestJson).getAsJsonObject();
			records = workflowService.getMaximumExecutionIDs(validatedConfigIdJson);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(records);
	}

	@PostMapping(value = "/downloadReportPDF")
	@ResponseBody
	public ResponseEntity<byte[]> getReportPDF(@RequestBody String pdfDetailsJsonString) {
		ResponseEntity<byte[]> response = null;
		try {
			String validatedResponse = ValidationUtils.validateRequestBody(pdfDetailsJsonString);
			JsonParser parser = new JsonParser();
			JsonObject pdfDetailsJson = (JsonObject) parser.parse(validatedResponse);
			byte[] fileContent = workflowService.getReportPDF(pdfDetailsJson);
			String pdfName = pdfDetailsJson.get("pdfName").getAsString() + ".pdf";
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.parseMediaType("application/pdf"));
			headers.add("Access-Control-Allow-Methods", "POST");
			headers.add("Access-Control-Allow-Headers", "Content-Type");
			headers.add("Content-Disposition", "attachment; filename=" + pdfName);
			headers.add("Cache-Control", "no-cache, no-store, must-revalidate,post-check=0, pre-check=0");
			headers.add("Pragma", "no-cache");
			headers.add("Expires", "0");
			response = new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
		} catch (InsightsCustomException e) {
			log.error("Error, Failed to download pdf -- {} ", e.getMessage());
		}
		return response;

	}
}
