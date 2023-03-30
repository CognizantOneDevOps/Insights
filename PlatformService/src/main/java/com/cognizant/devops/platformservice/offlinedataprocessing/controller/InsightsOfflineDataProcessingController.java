/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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
package com.cognizant.devops.platformservice.offlinedataprocessing.controller;

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
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.offlineDataProcessing.InsightsOfflineConfig;
import com.cognizant.devops.platformservice.offlinedataprocessing.service.OfflineDataProcessingService;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/insights/offlinedataprocessing")
public class InsightsOfflineDataProcessingController {
	private static Logger log = LogManager.getLogger(InsightsOfflineDataProcessingController.class);

	@Autowired
	OfflineDataProcessingService offlineDataProcessingService;

	@PostMapping(value = "/saveOfflineDefinition", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject saveOfflineDefinition(@RequestBody String registerOfflineJson) {
		try {
			JsonObject offlineResponse = new JsonObject();
			registerOfflineJson = registerOfflineJson.replace("\n", "").replace("\r", "");
			String validatedResponse = ValidationUtils.validateRequestBody(registerOfflineJson);
			JsonObject offlineJson = JsonUtils.parseStringAsJsonObject(validatedResponse);
			String resultQueryName = offlineDataProcessingService.saveOfflineDefinition(offlineJson);
			offlineResponse.addProperty(PlatformServiceConstants.MESSAGE,
					"Offline data created with queryName " + resultQueryName);
			return PlatformServiceUtil.buildSuccessResponseWithData(offlineResponse);
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse("Unable to save Offline Setting Configuration");
		}
	}

	@PostMapping(value = "/saveBulkOfflineData", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public @ResponseBody JsonObject saveBulkOfflineData(@RequestParam("file") MultipartFile file) {
		try {
			String returnMessage = offlineDataProcessingService.saveOfflineDataInDatabase(file);
			return PlatformServiceUtil.buildSuccessResponseWithData(returnMessage);
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse("Unable to save Offline Setting Configuration ");
		}
	}

	@PostMapping(value = "/updateOfflineDefinition", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject updateOfflineDefinition(@RequestBody String updateOfflineRequest) {
		try {
			JsonObject offlineResponse = new JsonObject();
			updateOfflineRequest = updateOfflineRequest.replace("\n", "").replace("\r", "");
			String validatedEditResponse = ValidationUtils.validateRequestBody(updateOfflineRequest);
			JsonObject updateOfflineJson = JsonUtils.parseStringAsJsonObject(validatedEditResponse);
			String resultOfflineQuery = offlineDataProcessingService.updateOfflineDefinition(updateOfflineJson);
			offlineResponse.addProperty(PlatformServiceConstants.MESSAGE,
					"Offline definition updated for query name " + resultOfflineQuery);
			return PlatformServiceUtil.buildSuccessResponseWithData(offlineResponse);
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse("Unable to update Offline Setting Configuration");
		}
	}

	@PostMapping(value = "/updateOfflineConfigStatus", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject updateOutcomeConfigStatus(@RequestBody String statusConfig) {
		String message = null;
		try {
			JsonObject configStatusJson = JsonUtils.parseStringAsJsonObject(statusConfig);
			offlineDataProcessingService.updateOfflineConfigStatus(configStatusJson);
			log.debug("Outcome Config Status updated successfully .");
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}

	@PostMapping(value = "/deleteOfflineDefinition", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject deleteOfflineDefinition(@RequestBody String deleteOfflineRequest) {
		try {
			JsonObject offlineResponse = new JsonObject();
			deleteOfflineRequest = deleteOfflineRequest.replace("\n", "").replace("\r", "");
			String validatedEditResponse = ValidationUtils.validateRequestBody(deleteOfflineRequest);
			JsonObject offlineJson = JsonUtils.parseStringAsJsonObject(validatedEditResponse);
			boolean isRecordDeleted = offlineDataProcessingService.deleteOfflineDefinition(offlineJson);
			if (isRecordDeleted) {
				offlineResponse.addProperty(PlatformServiceConstants.MESSAGE,
						"Offline definition deleted for query name " + offlineJson.get("queryName"));
				return PlatformServiceUtil.buildSuccessResponseWithData(offlineResponse);
			} else {
				offlineResponse.addProperty(PlatformServiceConstants.MESSAGE,
						"Offline definition not deleted for query name " + offlineJson.get("queryName"));
				return PlatformServiceUtil.buildFailureResponse(offlineResponse.getAsString());
			}
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse("Unable to delete Offline Setting Configuration");
		}
	}

	@GetMapping(value = "/getAllOfflineDataList", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getAllOfflineDataList() {
		try {
			List<InsightsOfflineConfig> offlineDataList = offlineDataProcessingService.getOfflineDataList();
			return PlatformServiceUtil.buildSuccessResponseWithData(offlineDataList);
		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}

}
