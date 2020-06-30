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
package com.cognizant.devops.platformservice.rest.grafana;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformcommons.dal.grafana.GrafanaHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

@RestController
@RequestMapping("/admin/userMgmt")
public class UserManagementService {
	private static Logger log = LogManager.getLogger(UserManagementService.class.getName());

	@Autowired
	private HttpServletRequest httpRequest;
	
	GrafanaHandler grafanaHandler = new GrafanaHandler();
	private static final String PATH = "/api/orgs/";  

	@PostMapping(value = "/getOrgUsers", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject getOrgUsers(@RequestParam int orgId) throws InsightsCustomException {
		try {
			Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
			String response = grafanaHandler.grafanaGet(PATH + orgId + "/users", headers);
			return PlatformServiceUtil
					.buildSuccessResponseWithData(new JsonParser().parse(response));
		} catch (JsonSyntaxException e) {
			return PlatformServiceUtil.buildFailureResponse("Unable to get current org users");
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse("Unable to get current org users,Permission denide ");
		}
	}

	@PostMapping(value = "/createOrg", produces = MediaType.APPLICATION_JSON_VALUE)
	public String createOrg(@RequestParam String orgName) throws InsightsCustomException {
		JsonObject request = new JsonObject();
		request.addProperty("name", orgName);
		Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
		return grafanaHandler.grafanaPost(PATH, request, headers);
	}

	@PostMapping(value = "/editOrganizationUser", produces = MediaType.APPLICATION_JSON_VALUE)
	public String editOrganizationUser(@RequestParam int orgId, @RequestParam int userId, @RequestParam String role) throws InsightsCustomException {
		log.debug("%n%nInside editOrganizationUser method call");
		JsonObject request = new JsonObject();
		request.addProperty("role", role);
		Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
		return grafanaHandler.grafanaPatch(PATH + orgId + "/users/" + userId, request, headers);
	}

	@PostMapping(value = "/deleteOrganizationUser", produces = MediaType.APPLICATION_JSON_VALUE)
	public String deleteOrganizationUser(@RequestParam int orgId, @RequestParam int userId, @RequestParam String role) throws InsightsCustomException {
		log.debug("%n%nInside deleteOrganizationUser method call");
		Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
		return grafanaHandler.grafanaDelete(PATH + orgId + "/users/" + userId, headers);
		
	}
}
