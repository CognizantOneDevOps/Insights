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
package com.cognizant.devops.platformservice.webhook.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfig;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.cognizant.devops.platformservice.webhook.service.WebHookServiceImpl;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RestController
@RequestMapping("/admin/webhook")
public class WebHookController {
	static Logger log = LogManager.getLogger(WebHookController.class);

	@Autowired
	WebHookServiceImpl webhookConfigurationService;

	@RequestMapping(value = "/saveWebhook", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject saveWebhook(@RequestBody String registerWebhookJson) {

		try {
			String validatedResponse = ValidationUtils.validateRequestBody(registerWebhookJson);
			JsonParser parser = new JsonParser();
			JsonObject registerWebhookjson = (JsonObject) parser.parse(validatedResponse);
			Boolean result = webhookConfigurationService.saveWebHookConfiguration(registerWebhookjson);
			if (result) {
				return PlatformServiceUtil.buildSuccessResponse();
			} else {
				return PlatformServiceUtil
						.buildFailureResponse("Something went wrong,while saving the data of webhook.");
			}

		} catch (InsightsCustomException e) {
			log.error(e);
			if (e.getMessage().equals(PlatformServiceConstants.INCORRECT_RESPONSE_TEMPLATE) ){
				return PlatformServiceUtil.buildFailureResponse(PlatformServiceConstants.INCORRECT_RESPONSE_TEMPLATE);
			} else if (e.getMessage().equals(PlatformServiceConstants.WEBHOOK_NAME)){
				return PlatformServiceUtil.buildFailureResponse(PlatformServiceConstants.WEBHOOK_NAME);
			} else {
				return PlatformServiceUtil.buildFailureResponse(e.getMessage());
			}
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil
					.buildFailureResponse("Unable to save or update Setting Configuration for the request");
		}
	}

	@RequestMapping(value = "/loadwebhookConfiguration", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getRegisteredWebHooks() {
		List<WebHookConfig> webhookList;
		try {
			webhookList = webhookConfigurationService.getRegisteredWebHooks();
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(webhookList);
	}

	@RequestMapping(value = "/uninstallWebHook", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject uninstallWebhook(@RequestParam String webhookname) {
		String message = null;
		try {
			message = webhookConfigurationService.uninstallWebhook(webhookname);
			return PlatformServiceUtil.buildSuccessResponseWithData(message);
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}

	}

	@RequestMapping(value = "/updateWebhook", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject updateWebhook(@RequestBody String registerWebhookJson) {
		try {
			String validatedResponse = ValidationUtils.validateRequestBody(registerWebhookJson);
			JsonParser parser = new JsonParser();
			JsonObject registerWebhookjson = (JsonObject) parser.parse(validatedResponse);
			Boolean result = webhookConfigurationService.updateWebHook(registerWebhookjson);
			if (result.booleanValue()) {
				return PlatformServiceUtil.buildSuccessResponse();
			} else {
				return PlatformServiceUtil.buildFailureResponse("Something went wrong,while saving the data.");
			}
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}

	}

	@RequestMapping(value = "/updateWebhookStatus", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject updateWebhookStatus(@RequestBody String updateWebhookJson) {
		String message = null;
		try {
			String validatedResponse = ValidationUtils.validateRequestBody(updateWebhookJson);
			JsonParser parser = new JsonParser();
			JsonObject updateWebhookJsonValidated = (JsonObject) parser.parse(validatedResponse);
			message = webhookConfigurationService.updateWebhookStatus(updateWebhookJsonValidated);
			log.debug(" Response in updateWebhookStatus {} ", message);
			return PlatformServiceUtil.buildSuccessResponse();
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}
}
