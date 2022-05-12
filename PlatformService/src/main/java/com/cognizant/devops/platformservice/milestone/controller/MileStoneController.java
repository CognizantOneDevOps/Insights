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
package com.cognizant.devops.platformservice.milestone.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import com.cognizant.devops.platformservice.milestone.service.MileStoneService;
import com.cognizant.devops.platformservice.milestone.service.MileStoneServiceImpl;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/milestone")
public class MileStoneController {

	static Logger log = LogManager.getLogger(MileStoneController.class);

	MileStoneService mileStoneServiceImpl = new MileStoneServiceImpl();
	
	@GetMapping(value = "/fetchOutcomeConfig", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject fetchOutcomeTools(){
		JsonArray jsonarray = new JsonArray();
		try{
			jsonarray = mileStoneServiceImpl.fetchOutcomeConfig();
			return PlatformServiceUtil.buildSuccessResponseWithData(jsonarray);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}
	
	@PostMapping(value = "/saveMileStoneConfig", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject saveMileStoneConfig(@RequestBody String config){
		String message = null;
		try{
			JsonObject configJson = JsonUtils.parseStringAsJsonObject(config);
			int result = mileStoneServiceImpl.saveMileStoneConfig(configJson);
			log.debug("MileStone config saved successfully === {} ",result);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}
	
	@GetMapping(value = "/fetchMileStoneConfig", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject fetchMileStoneConfig(){
		JsonArray jsonarray = new JsonArray();
		try{
			jsonarray = mileStoneServiceImpl.getMileStoneConfigs();
			return PlatformServiceUtil.buildSuccessResponseWithData(jsonarray);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}
	

	@PostMapping(value = "/updateMileStoneConfig", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject updateMileStoneConfig(@RequestBody String config){
		String message = null;
		try{
			JsonObject configJson =JsonUtils.parseStringAsJsonObject(config);
			mileStoneServiceImpl.updateMileStoneConfig(configJson);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}
	
	@PostMapping(value = "/restartMileStoneConfig", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject restartMileStoneConfig(@RequestBody String config){
		String message = null;
		try{
			JsonObject configJson = JsonUtils.parseStringAsJsonObject(config);
			mileStoneServiceImpl.restartMileStoneConfig(configJson);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}
	
	@PostMapping(value = "/deleteMileStoneConfig", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject deleteMileStoneConfig(@RequestParam int id) {
		log.debug("Deleting Dashboard details for == {}", id);
		String message = null;
		try {
			mileStoneServiceImpl.deleteMileStoneDetails(id);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}
	
}
