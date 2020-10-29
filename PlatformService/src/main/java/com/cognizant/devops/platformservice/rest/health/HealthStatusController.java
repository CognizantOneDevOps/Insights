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
package com.cognizant.devops.platformservice.rest.health;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformcommons.constants.ErrorMessage;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformservice.rest.health.service.HealthStatusServiceImpl;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/admin/health")
public class HealthStatusController {

	@Autowired
	HealthStatusServiceImpl healthStatusServiceImpl;

	static Logger log = LogManager.getLogger(HealthStatusController.class);

	@GetMapping(value = "/globalHealth", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getHealthStatus() {
		JsonObject servicesHealthStatus = null;
		try {
			servicesHealthStatus = healthStatusServiceImpl.getHealthStatus();
		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(servicesHealthStatus);
	}

	@GetMapping(value = "/globalAgentsHealth", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getAgentsHealthStatus() {
		JsonObject servicesAgentsHealthStatus = new JsonObject();
		try {
			servicesAgentsHealthStatus = healthStatusServiceImpl.getAgentsHealthStatus();
			log.debug(" servicesAgentsHealthStatus {}", servicesAgentsHealthStatus);
		} catch (Exception e) {
			log.error(e.getMessage());
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(servicesAgentsHealthStatus);
	}

	@GetMapping(value = "/detailHealth", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject loadAgentsHealth(@RequestParam String category, @RequestParam String tool,
			@RequestParam String agentId) {
		GraphResponse response = new GraphResponse();
		try {
			if (StringUtils.isEmpty(category) || StringUtils.isEmpty(tool)) {
				return PlatformServiceUtil.buildFailureResponse(ErrorMessage.CATEGORY_AND_TOOL_NAME_NOT_SPECIFIED);
			}
			response = healthStatusServiceImpl.getDetailHealth(category, tool, agentId);
		} catch (Exception e) {
			log.error(e.getMessage());
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithHtmlData(response);
	}

	@GetMapping(value = "/getAgentFailureDetails", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getAgentFailureDetails(@RequestParam String category, @RequestParam String tool,
			@RequestParam String agentId) {
		try {
			log.debug("############ inside getAgentFailureDetails  -----");
			return healthStatusServiceImpl.createAgentFailureHealthLabel(category, tool, agentId);
		} catch (Exception e) {
			log.error(e.getMessage());
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}

}
