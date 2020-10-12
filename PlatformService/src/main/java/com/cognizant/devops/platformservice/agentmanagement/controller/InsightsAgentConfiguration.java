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

package com.cognizant.devops.platformservice.agentmanagement.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.agentmanagement.service.AgentConfigTO;
import com.cognizant.devops.platformservice.agentmanagement.service.AgentManagementService;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RestController
@RequestMapping("/admin/agentConfiguration")
public class InsightsAgentConfiguration {

	@Autowired
	AgentManagementService agentManagementService;

	@RequestMapping(value = "/registerAgent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject registerAgent(@RequestParam String toolName, @RequestParam String agentVersion,
			@RequestParam String osversion, @RequestParam String configDetails, @RequestParam String trackingDetails) {
		String message = null;
		try {
			message = agentManagementService.registerAgent(toolName, agentVersion, osversion, configDetails,
					trackingDetails, false);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}

	@RequestMapping(value = "/2.0/registerAgent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject registerAgentV2(@RequestBody String registerAgentJson) {
		String message = null;

		try {
			String validatedResponse = ValidationUtils.validateRequestBody(registerAgentJson);
			JsonParser parser = new JsonParser();
			JsonObject registerAgentjson = (JsonObject) parser.parse(validatedResponse);
			String toolName = registerAgentjson.get("toolName").getAsString();
			String agentVersion = registerAgentjson.get("agentVersion").getAsString();
			String osversion = registerAgentjson.get("osversion").getAsString();
			String configDetails = registerAgentjson.get("configJson").getAsString();
			String trackingDetails = registerAgentjson.get("trackingDetails").getAsString();
			boolean vault = registerAgentjson.get("vault").getAsBoolean();
			message = agentManagementService.registerAgent(toolName, agentVersion, osversion, configDetails,
					trackingDetails, vault);
			return PlatformServiceUtil.buildSuccessResponseWithData(message);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}

	}

	@RequestMapping(value = "/uninstallAgent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject uninstallAgent(@RequestParam String agentId, @RequestParam String toolName,
			@RequestParam String osversion) {
		String message = null;
		try {
			message = agentManagementService.uninstallAgent(agentId, toolName, osversion);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}

	@RequestMapping(value = "/updateAgent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject updateAgent(@RequestParam String agentId, @RequestParam String configJson,
			@RequestParam String toolName, @RequestParam String agentVersion, @RequestParam String osversion) {
		String message = null;
		try {
			message = agentManagementService.updateAgent(agentId, configJson, toolName, agentVersion, osversion, false);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}

	@RequestMapping(value = "/2.0/updateAgent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject updateAgent(@RequestBody String updateAgentJsonRequest) {
		String message = null;
		try {
			String validatedResponse = ValidationUtils.validateRequestBody(updateAgentJsonRequest);
			JsonParser parser = new JsonParser();
			JsonObject updateAgentJson = (JsonObject) parser.parse(validatedResponse);
			String agentId = updateAgentJson.get("agentId").getAsString();
			String toolName = updateAgentJson.get("toolName").getAsString();
			String agentVersion = updateAgentJson.get("agentVersion").getAsString();
			String osversion = updateAgentJson.get("osversion").getAsString();
			String configDetails = updateAgentJson.get("configJson").getAsString();
			boolean vault = updateAgentJson.get("vault").getAsBoolean();
			message = agentManagementService.updateAgent(agentId, configDetails, toolName, agentVersion, osversion,
					vault);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}

	@RequestMapping(value = "/startStopAgent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject startStopAgent(@RequestParam String agentId, @RequestParam String toolName,
			@RequestParam String osversion, @RequestParam String action) {
		String message = null;
		try {
			message = agentManagementService.startStopAgent(agentId, toolName, osversion, action);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}

	@RequestMapping(value = "/getSystemAvailableAgentList", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject getSystemAvailableAgentList() {
		Map<String, ArrayList<String>> agentDetails;
		try {
			if (!ApplicationConfigProvider.getInstance().getAgentDetails().isOnlineRegistration()) {
				agentDetails = agentManagementService.getOfflineSystemAvailableAgentList();
			} else if (ApplicationConfigProvider.getInstance().getAgentDetails().getOnlineRegistrationMode()
					.equalsIgnoreCase(ConfigOptions.ONLINE_REGISTRATION_MODE_NEXUS)) {
				agentDetails = agentManagementService.getRepoAvailableAgentList();
			} else {
				agentDetails = agentManagementService.getDocrootAvailableAgentList();
			}
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(agentDetails);
	}

	@RequestMapping(value = "/getToolRawConfigFile", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject getToolRawConfigFile(@RequestParam String version, @RequestParam String tool) {
		String details = null;
		try {
			details = agentManagementService.getToolRawConfigFile(version, tool);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return PlatformServiceUtil.buildSuccessResponseWithHtmlData(details);
	}

	@RequestMapping(value = "/getRegisteredAgents", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject getRegisteredAgents() {
		List<AgentConfigTO> agentList;
		try {
			agentList = agentManagementService.getRegisteredAgents();
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(agentList);
	}

	@RequestMapping(value = "/getRegisteredAgentDetail", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject getAgentDetails(@RequestParam String agentId) {
		AgentConfigTO agentDetails;
		try {
			agentDetails = agentManagementService.getAgentDetails(agentId);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(agentDetails);
	}

}
