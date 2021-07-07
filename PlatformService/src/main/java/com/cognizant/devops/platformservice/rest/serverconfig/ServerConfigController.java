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
package com.cognizant.devops.platformservice.rest.serverconfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/configMgmt")
public class ServerConfigController {
	private static Logger log = LogManager.getLogger(ServerConfigController.class);
	
	ServerConfigServiceImpl serverConfigServiceImpl = new ServerConfigServiceImpl();

	@GetMapping(value = "/getServerConfigStatus", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject getServerConfigStatus() {
		try {
			JsonObject serverConfigDetail = new JsonObject();
			boolean status = serverConfigServiceImpl.getServerConfigStatus();
			serverConfigDetail.addProperty("isServerConfigAvailable", status);
			return PlatformServiceUtil.buildSuccessResponseWithData(serverConfigDetail);
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}

	@PostMapping(value = "/saveServerConfigDetail", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject saveServerConfigDetail(@RequestBody String serverConfigJson) {
		JsonObject serverConfigDetail = new JsonObject();
		try {
			serverConfigJson = serverConfigJson.replace("\n", "").replace("\r", "");
			String validatedResponse = ValidationUtils.validateRequestBody(serverConfigJson);
			log.debug("validatedResponse  ===== {}",validatedResponse);
			
			boolean isAdded = serverConfigServiceImpl.saveServerConfigTemplate(serverConfigJson,"local");
			serverConfigDetail.addProperty("message", "Server Config successfully added ... "+isAdded);
			return PlatformServiceUtil.buildSuccessResponseWithData(serverConfigDetail);
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
	}
	
	@GetMapping(value = "/getServerConfigDetail", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getServerConfigTemplate() {
		try {
			String serverConfig = serverConfigServiceImpl.getServerConfigTemplate();
			return PlatformServiceUtil.buildSuccessResponseWithData(serverConfig);
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} 
	}
}
