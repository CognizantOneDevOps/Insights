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

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.cognizant.devops.platformregressiontest.common.CommonUtils;
import com.google.gson.JsonObject;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class RegisterAgentAPITest extends TestData {

	private static final Logger log = LogManager.getLogger(RegisterAgentAPITest.class);

	@BeforeMethod
	public void onInit() throws InterruptedException, IOException {

		jSessionID = getJsessionId();
		xsrfToken = getXSRFToken(jSessionID);
	}

	@Test(priority = 1, dataProvider = "agentdataprovider")
	public void registerAgent(String toolName, String agentVersion, String osversion, String configJson, String vault) {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("registerAgentBaseURI");
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header("XSRF-TOKEN", xsrfToken));
		httpRequest.cookies("JSESSIONID", jSessionID, "grafanaOrg", CommonUtils.getProperty("grafanaOrg"),
				"grafanaRole", CommonUtils.getProperty("grafanaRole"), "XSRF-TOKEN", xsrfToken);
		httpRequest.header("Authorization", authorization);

		// Request payload sending along with post request
		JsonObject requestParam = new JsonObject();
		requestParam.addProperty("toolName", toolName);
		requestParam.addProperty("agentVersion", agentVersion);
		requestParam.addProperty("osversion", osversion);
		requestParam.addProperty("configJson", configJson);
		requestParam.addProperty("trackingDetails", "");
		requestParam.addProperty("vault", vault);

		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParam);

		// Response Object
		Response responseAgent = httpRequest.request(Method.POST, "/");

		String responseRegisterAgent = responseAgent.getBody().asString();
		log.debug("SuccessResponse", responseRegisterAgent);

		int statusCode = responseAgent.getStatusCode();

		log.debug("StatusCode" + statusCode);
		Assert.assertEquals(statusCode, 200);
		Assert.assertEquals(responseRegisterAgent.contains("success"), true);

	}

	// @Test(priority = 2, dataProvider = "agentdataprovider")
	public void registerAgentAuthorizationFail(String toolName, String agentVersion, String osversion,
			String configJson, String vault) {

		RestAssured.baseURI = CommonUtils.getProperty("basURI") + CommonUtils.getProperty("registerAgentBaseURI");
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header("XSRF-TOKEN", xsrfToken));
		httpRequest.cookies("JSESSIONID", jSessionID, "grafanaOrg", CommonUtils.getProperty("grafanaOrg"),
				"grafanaRole", CommonUtils.getProperty("grafanaRole"), "XSRF-TOKEN", xsrfToken);
		// httpRequest.header("Authorization", authorization);

		// Request payload sending along with post request
		JsonObject requestParam = new JsonObject();
		requestParam.addProperty("toolName", toolName);
		requestParam.addProperty("agentVersion", agentVersion);
		requestParam.addProperty("osversion", osversion);
		requestParam.addProperty("configJson", configJson);
		requestParam.addProperty("trackingDetails", "");
		requestParam.addProperty("vault", vault);

		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParam);

		// Response Object
		Response responseAgent = httpRequest.request(Method.POST, "/");

		String responseRegisterAgent = responseAgent.getBody().asString();
		log.debug("FailureResponse" + responseRegisterAgent);

		// Statuscode Validation
		int FailureStatusCode = responseAgent.getStatusCode();
		Assert.assertEquals(FailureStatusCode, 400);
		Assert.assertTrue(responseRegisterAgent.contains("status"), "failure");

	}

	@Test(priority = 2, dataProvider = "agentdataprovider")
	public void registerAgentFail(String toolName, String agentVersion, String osversion, String configJson,
			String vault) {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("registerAgentBaseURI");

		RequestSpecification httpRequest = RestAssured.given();
		httpRequest.header(new Header("XSRF-TOKEN", xsrfToken));
		httpRequest.cookies("JSESSIONID", jSessionID, "grafanaOrg", CommonUtils.getProperty("grafanaOrg"),
				"grafanaRole", CommonUtils.getProperty("grafanaRole"), "XSRF-TOKEN", xsrfToken);

		// Request payload sending along with post request
		JsonObject requestParam = new JsonObject();
		requestParam.addProperty("toolName", toolName);
		requestParam.addProperty("agentVersion", agentVersion);
		requestParam.addProperty("osversion", osversion);
		requestParam.addProperty("configJson", configJson);
		requestParam.addProperty("trackingDetails", "");

		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParam);

		// Response Object
		Response responseAgent = httpRequest.request(Method.POST, "/");

		String responseRegisterAgent = responseAgent.getBody().asString();
		log.debug("FailureResponse" + responseRegisterAgent);

		// Statuscode Validation
		int FailureStatusCode = responseAgent.getStatusCode();
		Assert.assertEquals(FailureStatusCode, 400);
		Assert.assertTrue(responseRegisterAgent.contains("status"), "failure");

	}

}
