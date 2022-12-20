/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
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
package com.cognizant.devops.platformservice.outcome.controller;

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
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.outcome.service.OutComeServiceImpl;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/outcome")
public class OutComeController {

	static Logger log = LogManager.getLogger(OutComeController.class);

	@Autowired
	OutComeServiceImpl outComeServiceImpl;
	
	@GetMapping(value = "/fetchMileStoneTools", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject fetchOutcomeTools(){
		JsonArray jsonarray = new JsonArray();
		try{
			jsonarray = outComeServiceImpl.getMileStoneTools();
			return PlatformServiceUtil.buildSuccessResponseWithData(jsonarray);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}
	
	@PostMapping(value = "/saveOutcomeConfig", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject saveOutcomeConfig(@RequestBody String config){
		String message = null;
		try{
			JsonObject configJson = JsonUtils.parseStringAsJsonObject(config);
			int result = outComeServiceImpl.saveOutcomeConfig(configJson);
			log.debug("Outcome config saved successfully === {} ",result);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}
	
	
	
	@PostMapping(value = "/updateOutcomeConfig", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject updateOutcomeConfig(@RequestBody String config){
		String message = null;
		try{
			JsonObject configJson = JsonUtils.parseStringAsJsonObject(config);
			outComeServiceImpl.updateOutcomeConfig(configJson);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}
	
	@PostMapping(value = "/updateOutcomeConfigStatus", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject updateOutcomeConfigStatus(@RequestBody String statusConfig){
		String message = null;
		try{
			JsonObject configStatusJson = JsonUtils.parseStringAsJsonObject(statusConfig);
			outComeServiceImpl.updateOutcomeConfigStatus(configStatusJson);
			log.debug("Outcome Config Status updated successfully .");
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}
	
	@PostMapping(value = "/deleteOutcomeConfig", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject deleteOutcomeConfig(@RequestParam int id) {
		log.debug("Deleting Dashboard details for == {}", id);
		String message = null;
		try {
			outComeServiceImpl.deleteOutcomeDetails(id);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}
	
	@GetMapping(value = "/getAllActiveOutcome", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getAllActiveOutcome(){
		JsonArray jsonarray = new JsonArray();
		try{
			jsonarray = outComeServiceImpl.getAllActiveOutcome();
			return PlatformServiceUtil.buildSuccessResponseWithData(jsonarray);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}
	
}
