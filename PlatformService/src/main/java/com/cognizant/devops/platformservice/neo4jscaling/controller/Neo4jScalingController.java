/*******************************************************************************
 * Copyright 2024 Cognizant Technology Solutions
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
package com.cognizant.devops.platformservice.neo4jscaling.controller;

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
import com.cognizant.devops.platformservice.neo4jscaling.service.Neo4jScalingService;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/neo4jScaling")
public class Neo4jScalingController {
	static Logger log = LogManager.getLogger(Neo4jScalingController.class);

	@Autowired
	Neo4jScalingService neo4jScalingService;

	@GetMapping(value = "/configs", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getNeo4jScalingConfigs() {
		JsonObject response = new JsonObject();
		try {
			response = neo4jScalingService.getNeo4jScalingConfigs();
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(response);
	}
	
	
	@PostMapping(value = "/saveScalingConfigs", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject saveNeo4jScalingConfigs(@RequestBody String configString) {
		try {
			configString = configString.replace("\n", "").replace("\r", "");
			String validatedReqBody = ValidationUtils.validateRequestBody(configString);
			JsonObject configJson = JsonUtils.parseStringAsJsonObject(validatedReqBody);
			String message = neo4jScalingService.saveNeo4jScalingConfigs(configJson.get("sourceStreamsConfig").getAsJsonObject(), configJson.get("replicaConfig").getAsJsonObject());
			return PlatformServiceUtil.buildSuccessResponseWithData(message);
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse("Unable to save neo4j config");
		}
	}
	
	@GetMapping(value = "/replicas", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getAllReplicas() {
		JsonArray response = new JsonArray();
		try {
			response = neo4jScalingService.getAllReplicas();
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(response);
	}
	
	@PostMapping(value = "/deleteReplica", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject deleteReplica(@RequestParam String replicaName) {
		String response;
		try {
			response = neo4jScalingService.deleteReplica(replicaName);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(response);
	}
	
	@PostMapping(value = "/resyncReplicas", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject resyncAllReplicas() {
		String response;
		try {
			response = neo4jScalingService.resyncAll();
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(response);
	}
	
	@GetMapping(value = "/logDetails", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getNeo4jScalingLogDetails() {
		JsonObject response = new JsonObject();
		try {
			response = neo4jScalingService.getNeo4jScalingLogDetails();
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(response);
	}
}
