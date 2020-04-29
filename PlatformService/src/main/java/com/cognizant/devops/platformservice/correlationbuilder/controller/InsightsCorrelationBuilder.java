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
package com.cognizant.devops.platformservice.correlationbuilder.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.correlationConfig.CorrelationConfiguration;
import com.cognizant.devops.platformservice.correlationbuilder.service.CorrelationBuilderService;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RestController
@RequestMapping("/admin/correlationbuilder")
public class InsightsCorrelationBuilder {
	static Logger log = LogManager.getLogger(InsightsCorrelationBuilder.class.getName());
	@Autowired
	CorrelationBuilderService correlationBuilderService;

	@RequestMapping(value = "/getCorrelationJson", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject getCorrelationJson() {
		List<CorrelationConfiguration> details = new ArrayList<>();
		try {
			details = correlationBuilderService.getAllCorrelations();
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}

		return PlatformServiceUtil.buildSuccessResponseWithData(details);
	}

	
	@RequestMapping(value = "/saveConfig", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject saveConfig(@RequestBody String configDetails) {

		try {
			String configDeatilsValidate = ValidationUtils.validateRequestBody(configDetails);
			if (correlationBuilderService.saveConfig(configDeatilsValidate)) {
				return PlatformServiceUtil.buildSuccessResponse();
			} else {
				return PlatformServiceUtil.buildFailureResponse("Unable to update Correlation");
			}
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}

	}

	@RequestMapping(value = "/updateCorrelation", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject updateCorrelation(@RequestBody String flagDeatils) {

		try {
			String flagDeatilsValidate = ValidationUtils.validateRequestBody(flagDeatils);
			if (correlationBuilderService.updateCorrelationStatus(flagDeatilsValidate)) {
				return PlatformServiceUtil.buildSuccessResponse();
			} else {
				return PlatformServiceUtil.buildFailureResponse("Unable to update Correlation");
			}
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}

	}

	@RequestMapping(value = "/deleteCorrelation", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject deleteCorrelation(@RequestBody String relationName) {

		try {
			String relationNameValidate = ValidationUtils.validateRequestBody(relationName);
			if (correlationBuilderService.deleteCorrelation(relationNameValidate)) {
			} else {
				return PlatformServiceUtil.buildFailureResponse("Unable to delete Correlation");
			}
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return PlatformServiceUtil.buildSuccessResponse();
	}

}
