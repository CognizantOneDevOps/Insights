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

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.cognizant.devops.platformservice.webhook.service.WebHookConfigTO;
import com.cognizant.devops.platformservice.webhook.service.WebHookService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RestController
@RequestMapping("/admin/webhook")
public class WebHookController {
	static Logger log = LogManager.getLogger(WebHookController.class.getName());
	@Autowired
	WebHookService webhookConfigurationService;

	@RequestMapping(value = "/webhookConfiguration", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject saveSettingsConfiguration(@RequestBody String registerWebhookJson) {
		try {
			JsonParser parser = new JsonParser();
			JsonObject registerWebhookjson = (JsonObject) parser.parse(registerWebhookJson);
			String toolName = registerWebhookjson.get("toolName").getAsString();
			String labelDisplay = registerWebhookjson.get("labelDisplay").getAsString();
			String webhookName = registerWebhookjson.get("webhookName").getAsString();
			String dataformat = registerWebhookjson.get("dataformat").getAsString();
			String mqchannel = registerWebhookjson.get("mqchannel").getAsString();
			String responseTemplate = registerWebhookjson.get("responseTemplate").getAsString();
			Boolean statussubscribe = registerWebhookjson.get("statussubscribe").getAsBoolean();
			Boolean result = webhookConfigurationService.saveWebHookConfiguration(webhookName, toolName,
					labelDisplay, dataformat, mqchannel, statussubscribe, responseTemplate);
			return PlatformServiceUtil.buildSuccessResponse();
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse("Webhook name already exists.");
		} catch (Exception e) {
			return PlatformServiceUtil
					.buildFailureResponse("Unable to save or update Setting Configuration for the request");
		}
	}

	@RequestMapping(value = "/loadwebhookConfiguration", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject getRegisteredAgents() {
		List<WebHookConfigTO> webhookList;
		try {
			webhookList = webhookConfigurationService.getRegisteredWebHooks();
			//log.debug(webhookList);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(webhookList);
	}

	@RequestMapping(value = "/uninstallWebHook", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject uninstallWebhook(@RequestParam String webhookname) {
		String message = null;
		try {
			message = webhookConfigurationService.uninstallWebhook(webhookname);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}

	@RequestMapping(value = "/updateWebhook", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject updateWebhook(@RequestBody String registerWebhookJson) {
		try {
			JsonParser parser = new JsonParser();
			JsonObject registerWebhookjson = (JsonObject) parser.parse(registerWebhookJson);
			String toolName = registerWebhookjson.get("toolName").getAsString();
			String labelDisplay = registerWebhookjson.get("labelDisplay").getAsString();
			String webhookName = registerWebhookjson.get("webhookName").getAsString();
			String dataformat = registerWebhookjson.get("dataformat").getAsString();
			String mqchannel = registerWebhookjson.get("mqchannel").getAsString();
			String responseTemplate = registerWebhookjson.get("responseTemplate").getAsString();
			Boolean statussubscribe = registerWebhookjson.get("statussubscribe").getAsBoolean();
			
			Boolean result = webhookConfigurationService.updateWebHook(webhookName, toolName,
					labelDisplay, dataformat, mqchannel, statussubscribe, responseTemplate);
			log.error(result);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData("Success update");
	}
}
