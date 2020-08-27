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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
	private static Logger LOG = LogManager.getLogger(InsightsWorkflowController.class);
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
			LOG.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			LOG.error(e);
			return PlatformServiceUtil.buildFailureResponse("Unable to save workflow task due to exception");
		}
	}

	@RequestMapping(value = "/getTaskList", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
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

	@RequestMapping(value = "/setRetryStatus", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)	
	public @ResponseBody JsonObject setRetryStatus(@RequestParam String configId) {
		String message = null;
		try {
			message = workflowService.setRetryStatus(configId);
			return PlatformServiceUtil.buildSuccessResponseWithData(message);
		} catch (InsightsCustomException e) {			
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}

	
}
