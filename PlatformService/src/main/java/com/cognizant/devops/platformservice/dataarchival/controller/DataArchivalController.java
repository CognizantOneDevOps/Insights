/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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
package com.cognizant.devops.platformservice.dataarchival.controller;

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

import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.dataArchivalConfig.InsightsDataArchivalConfig;
import com.cognizant.devops.platformservice.dataarchival.service.DataArchivalServiceImpl;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RestController
@RequestMapping("/admin/dataarchival")
public class DataArchivalController {
	
	static Logger log = LogManager.getLogger(DataArchivalController.class);
	
	@Autowired
	DataArchivalServiceImpl dataArchivalService = new DataArchivalServiceImpl();
	
	@RequestMapping(value = "/saveDataArchivalDetails", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject saveDataArchivalDetails(@RequestBody String archivalDetails) {
		String message = null;
		try {
			JsonObject detailsJson = new JsonParser().parse(archivalDetails).getAsJsonObject();
			message = dataArchivalService.saveDataArchivalDetails(detailsJson);
		} catch (InsightsCustomException e) {
			if (e.getMessage().equals("Archival Name already exists.") ){
				return PlatformServiceUtil.buildFailureResponse("Archival Name already exists.");
			} else {
				return PlatformServiceUtil.buildFailureResponse(e.getMessage());
			}
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}
	
	@RequestMapping(value = "/getAllArchivalRecord", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getAllArchivalRecord() {
		List<InsightsDataArchivalConfig> archivedDataList;
		try {
			archivedDataList = dataArchivalService.getAllArchivalRecord();
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(archivedDataList);
	}
	
	@RequestMapping(value = "/getActiveArchivalRecord", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getRegisteredWebHooks() {
		List<InsightsDataArchivalConfig> activeList;
		try {
			activeList = dataArchivalService.getActiveArchivalList();
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(activeList);
	}
	
	@RequestMapping(value = "/inactivateArchivalRecord", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject inactivateArchivalRecord(@RequestParam String archivalName) {
		Boolean result = false;
		try {
			result = dataArchivalService.inactivateArchivalRecord(archivalName);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(result);
	}
	
	@RequestMapping(value = "/activateArchivalRecord", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject activateArchivalRecord(@RequestParam String archivalName) {
		Boolean result = false;
		try {
			result = dataArchivalService.activateArchivalRecord(archivalName);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(result);
	}
	
	@RequestMapping(value = "/deleteArchivedRecord", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject deleteArchivalRecord(@RequestParam String archivalName) {
		Boolean result = false;
		try {
			result = dataArchivalService.deleteArchivalRecord(archivalName);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(result);
	}

	@RequestMapping(value = "/updateArchivalSourceUrl", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject updateArchivalSourceUrl(@RequestBody String archivalURLDetailsJson) {
		Boolean result = false;
		try {
			archivalURLDetailsJson = archivalURLDetailsJson.replace("\n", "").replace("\r", "");
			String validatedRequestJson = ValidationUtils.validateRequestBody(archivalURLDetailsJson);
			JsonParser parser = new JsonParser();
			JsonObject validatedArchivalURLDetailsJson = parser.parse(validatedRequestJson).getAsJsonObject();
			result = dataArchivalService.updateArchivalSourceUrl(validatedArchivalURLDetailsJson);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(result);
	}

}
