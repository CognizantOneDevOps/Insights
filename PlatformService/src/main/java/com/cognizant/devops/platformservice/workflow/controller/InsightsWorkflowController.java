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

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.cognizant.devops.platformservice.workflow.service.WorkflowServiceImpl;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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
			JsonObject workflowTaskJson = JsonUtils.parseStringAsJsonObject(validatedResponse);
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
			JsonObject validatedConfigIdJson = JsonUtils.parseStringAsJsonObject(validatedRequestJson);
			records = workflowService.getWorkFlowExecutionRecords(validatedConfigIdJson);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(records);
	}

	@PostMapping(value = "/workFlowExecutionRecordsByWorkflowId", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getWorkflowExecutionRecordsByWorkflowId(@RequestBody String workflowIdJson) {
		JsonObject records = null;
		try {
			workflowIdJson = workflowIdJson.replace("\n", "").replace("\r", "");
			String validatedRequestJson = ValidationUtils.validateRequestBody(workflowIdJson);
			JsonObject validatedConfigIdJson = JsonUtils.parseStringAsJsonObject(validatedRequestJson);
			records = workflowService.getWorkFlowExecutionRecordsByWorkflowId(validatedConfigIdJson);
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
			JsonObject validatedConfigIdJson = JsonUtils.parseStringAsJsonObject(validatedRequestJson);
			records = workflowService.getMaximumExecutionIDs(validatedConfigIdJson);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(records);
	}

	@PostMapping(value = "/getLatestExecutionId", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getLatestExecutionId(@RequestBody String workflowId) {
		JsonObject records = null;
		try {
			records = workflowService.getLatestExecutionId(workflowId);
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
			String decodedString = new String(Base64.getDecoder().decode(pdfDetailsJsonString),
					StandardCharsets.UTF_8);
			String validatedResponse =  ValidationUtils.validateRequestBody(decodedString);
			JsonObject pdfDetailsJson = JsonUtils.parseStringAsJsonObject(validatedResponse);
			byte[] fileContent = workflowService.getReportPDF(pdfDetailsJson);
			
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			byteArrayOutputStream.write(fileContent);
			org.apache.pdfbox.pdmodel.PDDocument document = org.apache.pdfbox.Loader.loadPDF(fileContent, "12345");
			
			if (document == null) {
				throw new InsightsCustomException("Invalid file ");
			}else {
				String pdfName = pdfDetailsJson.get("pdfName").getAsString() + ".pdf";
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.parseMediaType("application/pdf"));
				headers.add("Access-Control-Allow-Methods", "POST");
				headers.add("Access-Control-Allow-Headers", "Content-Type");
				headers.add("Content-Disposition", "attachment; filename=" + pdfName);
				headers.add("Cache-Control", "no-cache, no-store, must-revalidate,post-check=0, pre-check=0");
				headers.add("Pragma", "no-cache");
				headers.add("Expires", "0");
				response = new ResponseEntity<>(byteArrayOutputStream.toByteArray(), headers, HttpStatus.OK);
			}
		} catch (Exception e) {
			log.error("Error, Failed to download pdf -- {} ", e.getMessage());
		}
		return response;

	}
	
	@PostMapping(value = "/updateHealthNotification", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject enableHealthNotification(@RequestBody String status) {

		try {
			status = status.replace("\n", "").replace("\r", "");
			String validatedResponse = ValidationUtils.validateRequestBody(status);
			JsonObject statusJson = JsonUtils.parseStringAsJsonObject(validatedResponse);
			String message = workflowService.updateHealthNotification(statusJson);
			return PlatformServiceUtil.buildSuccessResponse();
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse("Unable to update System notification due to exception");
		}
	}

	@GetMapping(value = "/getHealthNotificationStatus", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getTaskList() {
		JsonObject data = null;
		try {
			data = workflowService.getHealthNotificationStatus();
			return PlatformServiceUtil.buildSuccessResponseWithData(data);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}
	
	@PostMapping(value = "/updateWorkflowTask", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject updateWorkflowTask(@RequestBody String workflowTask) {
		try {
			workflowTask = workflowTask.replace("\n", "").replace("\r", "");
			String validatedResponse = ValidationUtils.validateRequestBody(workflowTask);
			JsonObject workflowTaskJson = JsonUtils.parseStringAsJsonObject(validatedResponse);
			workflowService.updateWorkflowTask(workflowTaskJson);
			return PlatformServiceUtil.buildSuccessResponseWithData(" workflow Task was updated");
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse("Unable to update workflow task due to exception");
		}
	}
	
	/**
	 * get the task lists from the table
	 * @return JsonArray
	 */
	@GetMapping(value = "/getAllWorkflowTask", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getTaskDetail() {
		try {
			return PlatformServiceUtil.buildSuccessResponseWithData(workflowService.getTaskDetail());
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}

	/**
	 * get the workflow type from the table
	 * @return JsonArray
	 * @throws InsightsCustomException
	 */
	@GetMapping(value = "/getAllWorkflowType", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody List<String> getWorkflowType() throws InsightsCustomException {
		try {
			return workflowService.getAllWorkflowTypes();
		}
		catch (InsightsCustomException e) {
			log.error(e);
			throw new InsightsCustomException(e.toString());
		}
	}

	/**
	 * Deleting a task
	 * @param taskId
	 * @return JsonObject
	 */
	@PostMapping(value = "/deleteTaskList", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject deleteWorkflowtask(@RequestParam int taskId) {
		log.debug("TASK ID : {}", taskId);
		try {
			boolean status = workflowService.deleteTaskDetail(taskId);
			if (!status) 
				return PlatformServiceUtil.buildFailureResponse("The workflow task in use");
			else
				return PlatformServiceUtil.buildSuccessResponseWithData(status);
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}
}
