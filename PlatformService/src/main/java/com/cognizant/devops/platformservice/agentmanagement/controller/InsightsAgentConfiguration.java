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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.agentmanagement.service.AgentConfigTO;
import com.cognizant.devops.platformservice.agentmanagement.service.AgentManagementService;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/admin/agentConfiguration")
public class InsightsAgentConfiguration {

	private static final Logger LOG = Logger.getLogger(InsightsAgentConfiguration.class);

	@Autowired
	AgentManagementService agentManagementService;

	@RequestMapping(value = "/registerAgent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject registerAgent(@RequestParam String toolName,@RequestParam String agentVersion,
			@RequestParam String osversion,@RequestParam String configDetails) {
		String message = agentManagementService.registerAgent(toolName, agentVersion, osversion, configDetails);
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}

	@RequestMapping(value = "/updateAgent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject updateAgent(@RequestParam String agentId,@RequestParam String configJson,
			@RequestParam String toolName,@RequestParam String agentVersion,
			@RequestParam String osversion) {
		String message = agentManagementService.updateAgent(agentId,configJson, toolName, agentVersion, osversion);
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}

	@RequestMapping(value = "/startStopAgent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject startStopAgent(@RequestParam String agentId,@RequestParam String action) {
		String message = agentManagementService.startStopAgent(agentId,action);
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}


	@RequestMapping(value = "/getSystemAvailableAgentList", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject getSystemAvailableAgentList() {
		Map<String, ArrayList<String>>  agentDetails = agentManagementService.getSystemAvailableAgentList();
		return PlatformServiceUtil.buildSuccessResponseWithData(agentDetails);
	}

	@RequestMapping(value = "/getToolRawConfigFile", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject getToolRawConfigFile(@RequestParam String version,@RequestParam String tool) {
		String details ;
		try{
			details = agentManagementService.getToolRawConfigFile(version,tool);
		}catch(InsightsCustomException e){
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(details);


	}

	@RequestMapping(value = "/getRegisteredAgents", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject getRegisteredAgents(){
		List<AgentConfigTO>  agentList = agentManagementService.getRegisteredAgents();
		return PlatformServiceUtil.buildSuccessResponseWithData(agentList);
	}

	@RequestMapping(value = "/getRegisteredAgentDetail", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject getAgentDetails(@RequestParam String agentId) {
		AgentConfigTO  agentDetails = agentManagementService.getAgentDetails(agentId);
		return PlatformServiceUtil.buildSuccessResponseWithData(agentDetails);
	}


}
