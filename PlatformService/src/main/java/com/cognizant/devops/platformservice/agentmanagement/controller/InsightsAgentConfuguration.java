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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformservice.agentmanagement.service.AgentManagementService;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/admin/agentConfiguration")
public class InsightsAgentConfuguration {
	
	private static final Logger LOG = Logger.getLogger(InsightsAgentConfuguration.class);
	
	@Autowired
	AgentManagementService agentManagementService;
	
	@RequestMapping(value = "/registerAgent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject registerAgent(@RequestParam String configJson) {
		String message = agentManagementService.registerAgent(configJson);
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}
	
	@RequestMapping(value = "/updateAgent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject updateAgent(@RequestParam String agentId,@RequestParam String configJson) {
		String message = agentManagementService.updateAgent(agentId,configJson);
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}
	
	@RequestMapping(value = "/installAgent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject installAgent(@RequestParam String agentId,@RequestParam String configJson) {
		String message = agentManagementService.installAgent(agentId,configJson);
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}
	
	@RequestMapping(value = "/installAgent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject startStopAgent(@RequestParam String agentId,@RequestParam String action) {
		String message = agentManagementService.startStopAgent(agentId,action);
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}
	
}
