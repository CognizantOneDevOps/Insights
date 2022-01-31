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

package com.cognizant.devops.platformregressiontest.test.agentmanagement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.cognizant.devops.platformregressiontest.test.common.CommonUtils;
import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.google.gson.JsonObject;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class UpdateAgentAPITest extends AgentTestData {

	private static final Logger log = LogManager.getLogger(UpdateAgentAPITest.class);

	@Test(priority = 1, dataProvider = "agentupdateprovider")
	public void updateAgent(String agentId, String toolName, String agentVersion, String osversion, String configJson,
			String vault, String type) {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("updateAgentBaseURI");
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header(ConfigOptionsTest.CSRF_NAME_KEY, CommonUtils.xsrfToken));
		httpRequest.cookies(ConfigOptionsTest.SESSION_ID_KEY, CommonUtils.jSessionID,
				ConfigOptionsTest.GRAFANA_COOKIES_ORG, CommonUtils.getProperty("grafanaOrg"),
				ConfigOptionsTest.GRAFANA_COOKIES_ROLE, CommonUtils.getProperty("grafanaRole"),
				ConfigOptionsTest.CSRF_NAME_KEY, CommonUtils.xsrfToken);
		httpRequest.header(ConfigOptionsTest.AUTH_HEADER_KEY, CommonUtils.jtoken);

		// Request payload sending along with post request
		JsonObject requestParam = new JsonObject();
		requestParam.addProperty("agentId", agentId);
		requestParam.addProperty("toolName", toolName);
		requestParam.addProperty("agentVersion", agentVersion);
		requestParam.addProperty("osversion", osversion);
		requestParam.addProperty("configJson", configJson);
		requestParam.addProperty("trackingDetails", "");
		requestParam.addProperty("vault", vault);
		requestParam.addProperty("isWebhook", false);
		requestParam.addProperty("type", type);

		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParam);

		// Response Object
		Response responseAgent = httpRequest.request(Method.POST, "/");

		String responseUpdateAgent = responseAgent.getBody().asString();
		log.debug("UpdatedResponse {}", responseUpdateAgent);

		int statusCode = responseAgent.getStatusCode();
		Assert.assertEquals(statusCode, 200);

	}

	@Test(priority = 2, dataProvider = "agentupdateprovider")
	public void updateAgentFail(String agentId, String toolName, String agentVersion, String osversion,
			String configJson, String vault, String type) {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("updateAgentBaseURI");
		RequestSpecification httpRequest = RestAssured.given();
		
		httpRequest.cookies(ConfigOptionsTest.SESSION_ID_KEY, CommonUtils.jSessionID,
				ConfigOptionsTest.GRAFANA_COOKIES_ORG, CommonUtils.getProperty("grafanaOrg"),
				ConfigOptionsTest.GRAFANA_COOKIES_ROLE, CommonUtils.getProperty("grafanaRole"),
				ConfigOptionsTest.CSRF_NAME_KEY, CommonUtils.xsrfToken);
		httpRequest.header(ConfigOptionsTest.AUTH_HEADER_KEY, CommonUtils.jtoken);
		// Request payload sending along with post request
		JsonObject requestParam = new JsonObject();
		requestParam.addProperty("agentId", agentId);
		requestParam.addProperty("toolName", toolName);
		requestParam.addProperty("agentVersion", agentVersion);
		requestParam.addProperty("osversion", osversion);
		requestParam.addProperty("configJson", configJson);
		requestParam.addProperty("trackingDetails", "");
		requestParam.addProperty("vault", vault);
		requestParam.addProperty("isWebhook", false);
		requestParam.addProperty("type", type);

		httpRequest.header(ConfigOptionsTest.CONTENT_HEADER_KEY, ConfigOptionsTest.CONTENT_TYPE_VALUE);
		httpRequest.body(requestParam);

		Response responseAgent = httpRequest.request(Method.POST, "/");

		String responseUpdateAgent = responseAgent.getBody().asString();
		log.debug("FailureResponse {}", responseUpdateAgent);

		int failureStatusCode = responseAgent.getStatusCode();
		Assert.assertEquals(failureStatusCode, 200);
		Assert.assertTrue(responseUpdateAgent.contains("status"), "failure");

	}
}
