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

package com.cognizant.devops.platformservice.traceabilitydashboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.cognizant.devops.platformservice.traceabilitydashboard.service.TraceabilityDashboardServiceImpl;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/traceabilitydashboard")
public class TraceabilityDashboardController {

	@Autowired
	TraceabilityDashboardServiceImpl traceabilityDashboardServiceImpl;

	@RequestMapping(value = "/getToolSummary", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public JsonObject getToolSummary(@RequestParam String toolName, @RequestParam String cacheKey) {
		try {
			return PlatformServiceUtil
					.buildSuccessResponseWithData(traceabilityDashboardServiceImpl.getToolSummary(toolName, cacheKey));
		} catch (Exception e) {
			return PlatformServiceUtil.buildSuccessResponseWithData("Unable to load data from cache");
		}

	}
	@RequestMapping(value = "/getAvailableTools", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public JsonObject getAvailableTools() {
		try {
			return PlatformServiceUtil
					.buildSuccessResponseWithData(traceabilityDashboardServiceImpl.getAvailableTools());
		} catch (Exception e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}

	}

	@RequestMapping(value = "/getToolKeyset", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public JsonObject getToolKeyset(@RequestParam String toolName) {
		try {
			return PlatformServiceUtil
					.buildSuccessResponseWithData(traceabilityDashboardServiceImpl.getToolKeyset(toolName));
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}

	}

	@RequestMapping(value = "/getPipeline", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public JsonObject getPipeline(@RequestParam String toolName, @RequestParam String fieldName,
			@RequestParam String fieldValue) {
		try {
			JsonObject response = traceabilityDashboardServiceImpl.getPipeline(toolName, fieldName, fieldValue);
			return PlatformServiceUtil.buildSuccessResponseWithData(response);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.getMessage()); 
		}

	}
}
