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
package com.cognizant.devops.platformservice.insights.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformservice.insights.service.InsightsInference;
import com.cognizant.devops.platformservice.insights.service.InsightsInferenceService;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonObject;



	
@RestController
@RequestMapping("/insights")
public class InsightsInferenceController{

	private static Logger LOG = LogManager.getLogger(InsightsInferenceController.class);
	
		
	@Autowired
	InsightsInferenceService insightsInferenceService;

	@RequestMapping(value = "/inferences", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public  JsonObject getInferences( @RequestParam(value = "schedule", required = false, defaultValue = "DAILY")String schedule,String accessGroup) {
		LOG.debug(" inside getInferences call ============================================== ");
		List<InsightsInference> inferences = insightsInferenceService.getInferenceDetails(schedule);
		return PlatformServiceUtil.buildSuccessResponseWithData(inferences);

	}
	
	
}
