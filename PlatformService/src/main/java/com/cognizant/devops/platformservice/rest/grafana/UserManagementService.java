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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.dal.rest.RestHandler;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.ClientResponse;

@RestController
@RequestMapping("/admin/userMgmt")
public class UserManagementService {
	private static Logger log = LogManager.getLogger(UserManagementService.class.getName());

	@Autowired
	private HttpServletRequest httpRequest;

	@RequestMapping(value = "/getOrgUsers", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonObject getOrgUsers(@RequestParam int orgId) {
		String apiUrl = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint() + "/api/orgs/" + orgId
				+ "/users";
		Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
		ClientResponse response = RestHandler.doGet(apiUrl, null, headers);
		return PlatformServiceUtil
				.buildSuccessResponseWithData(new JsonParser().parse(response.getEntity(String.class)));
	}

	@RequestMapping(value = "/createOrg", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String createOrg(@RequestParam String orgName) {
		String apiUrl = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint() + "/api/orgs";
		JsonObject request = new JsonObject();
		request.addProperty("name", orgName);
		Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
		ClientResponse response = RestHandler.doPost(apiUrl, request, headers);
		return response.getEntity(String.class);
	}

	@RequestMapping(value = "/editOrganizationUser", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String editOrganizationUser(@RequestParam int orgId, @RequestParam int userId, @RequestParam String role) {
		log.debug("\n\nInside editOrganizationUser method call");
		String apiUrl = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint() + "/api/orgs/" + orgId
				+ "/users/" + userId;
		log.debug("API URL is: " + apiUrl);
		JsonObject request = new JsonObject();
		request.addProperty("role", role);
		Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
		ClientResponse response = RestHandler.doPatch(apiUrl, request, headers);
		return response.getEntity(String.class);
	}

	@RequestMapping(value = "/deleteOrganizationUser", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String deleteOrganizationUser(@RequestParam int orgId, @RequestParam int userId, @RequestParam String role) {
		log.debug("\n\nInside deleteOrganizationUser method call");
		String apiUrl = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint() + "/api/orgs/" + orgId
				+ "/users/" + userId;
		log.debug("API URL is: " + apiUrl);
		JsonObject request = new JsonObject();
		Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
		ClientResponse response = RestHandler.doDelete(apiUrl, request, headers);
		return response.getEntity(String.class);
	}
}
