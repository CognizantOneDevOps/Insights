/*******************************************************************************
 * Copyright 2022 Cognizant Technology Solutions
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
package com.cognizant.devops.platformservice.emailconfiguration.controller;

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

import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.emailconfiguration.service.EmailConfigurationService;
import com.cognizant.devops.platformservice.emailconfiguration.service.EmailConfigurationServiceImpl;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/emailConfiguration")
public class InsightsEmailConfigurationController {

	private static Logger log = LogManager.getLogger(InsightsEmailConfigurationController.class);

	@Autowired
	EmailConfigurationService emailConfiguartionService ;

	@GetMapping(value = "/getAllEmailConfig", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getAllEmailConfigurations(@RequestParam String source) {
		try {
			
			JsonArray allGroupEmailConfigurations = emailConfiguartionService
					.getAllGroupEmailConfigurations(source);
			return PlatformServiceUtil.buildSuccessResponseWithHtmlData(allGroupEmailConfigurations);
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse("Unable to fetch Group Email Configs due to exception");
		}
	}

	@PostMapping(value = "/saveEmailConfig", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject saveEmailConfig(@RequestBody String emailConfig) {
		int id;
		try {
			emailConfig = emailConfig.replace("\n", "").replace("\r", "");
			String validatedResponse = ValidationUtils.validateRequestBody(emailConfig);
			JsonObject emailConfigJson = JsonUtils.parseStringAsJsonObject(validatedResponse);
			id = emailConfiguartionService.saveEmailConfig(emailConfigJson);
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil
					.buildFailureResponse("Unable to save Group Email Configuration due to exception");
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(id);

	}

	@PostMapping(value = "/updateEmailConfig", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject updateEmailConfiguration(@RequestBody String emailConfig) {
		String message = null;
		try {
			emailConfig = emailConfig.replace("\n", "").replace("\r", "");
			String validatedResponse = ValidationUtils.validateRequestBody(emailConfig);
			JsonObject emailConfigJson = JsonUtils.parseStringAsJsonObject(validatedResponse);
			message = emailConfiguartionService.updateGroupEmailConfig(emailConfigJson);
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil
					.buildFailureResponse("Unable to update Group Email Configuration due to exception");
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(message);

	}
	
	@PostMapping(value = "/updateEmailConfigState", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject updateEmailConfigurationState(@RequestBody String updateEmailConfigString) {
		String message = null;
		try {
			updateEmailConfigString = updateEmailConfigString.replace("\n", "").replace("\r", "");
			String validatedResponse = ValidationUtils.validateRequestBody(updateEmailConfigString);
			JsonObject updateEmailConfigJson = JsonUtils.parseStringAsJsonObject(validatedResponse);
			message = emailConfiguartionService.updateGroupEmailConfigState(updateEmailConfigJson);
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil
					.buildFailureResponse("Unable to update Group Email Configuration state due to exception");
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(message);

	}

	@PostMapping(value = "/deleteEmailConfig", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject deleteEmailConfiguration(@RequestParam int id) {
		String message = null;
		try {
			message = emailConfiguartionService.deleteGroupEmailConfiguration(id);
		}  catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil
					.buildFailureResponse("Unable to delete Group Email Configuration due to exception");
		}

		return PlatformServiceUtil.buildSuccessResponseWithData(message);

	}

	// report template needs userDetails
	@GetMapping(value = "/getReportTitles", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getAllReportTitles(@RequestParam String source, @RequestParam String userName)
			throws InsightsCustomException {
		try {
			JsonArray titleList = emailConfiguartionService.getAllReportTitles(source, userName);
			return PlatformServiceUtil.buildSuccessResponseWithData(titleList);
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse("Unable to retrieve report details.");
		}

	}

}
