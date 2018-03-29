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
package com.cognizant.devops.platformservice.settingsconfig.controller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformdal.settingsconfig.SettingsConfiguration;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.cognizant.devops.platformservice.settingsconfig.service.SettingsConfigurationService;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/admin/settingsConfiguration")
public class InsightsSettingsConfiguration {

	private static final Logger LOG = Logger.getLogger(InsightsSettingsConfiguration.class);

	@Autowired
	SettingsConfigurationService settingsConfigurationService;

	@RequestMapping(value = "/saveSettingsConfiguration", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject saveSettingsConfiguration(@RequestParam String settingsJson, @RequestParam String settingsType,
													@RequestParam String activeFlag,@RequestParam String lastModifiedByUser) {
		Boolean result = settingsConfigurationService.saveSettingsConfiguration(settingsJson,settingsType,activeFlag,lastModifiedByUser);
		if (result) {
			return PlatformServiceUtil.buildSuccessResponse();
		} else {
			return PlatformServiceUtil.buildFailureResponse("Unable to save or update Setting Configuration for the request");
		}
	}

	@RequestMapping(value = "/loadSettingsConfiguration", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject loadSettingsConfiguration(@RequestParam int id ) {
		SettingsConfiguration settingsConfiguration = settingsConfigurationService.loadSettingsConfiguration(id);		
		return PlatformServiceUtil.buildSuccessResponseWithData(settingsConfiguration);
	}

	


}
