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

import com.cognizant.devops.platformregressiontest.test.common.CommonUtils;
import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.google.gson.JsonObject;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class RegisterAgentAPITest extends AgentTestData {

	private static final Logger log = LogManager.getLogger(RegisterAgentAPITest.class);

	String jSessionID;
	String xsrfToken;

	@BeforeMethod
	public void onInit() throws InterruptedException, IOException {

		jSessionID = CommonUtils.getJsessionId();
		xsrfToken = CommonUtils.getXSRFToken(jSessionID);
	}

	@Test(priority = 1, dataProvider = "agentdataprovider")
	public void registerAgent(String toolName, String agentVersion, String osversion, String configJson, String vault) {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("registerAgentBaseURI");
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header(ConfigOptionsTest.CSRF_NAME_KEY, xsrfToken));
		httpRequest.cookies(ConfigOptionsTest.SESSION_ID_KEY, jSessionID, ConfigOptionsTest.GRAFANA_COOKIES_ORG,
				CommonUtils.getProperty("grafanaOrg"), ConfigOptionsTest.GRAFANA_COOKIES_ROLE,
				CommonUtils.getProperty("grafanaRole"), ConfigOptionsTest.CSRF_NAME_KEY, xsrfToken);
		httpRequest.header(ConfigOptionsTest.AUTH_HEADER_KEY, CommonUtils.getProperty("authorization"));

		// Request payload sending along with post request
		JsonObject requestParam = new JsonObject();
		requestParam.addProperty("toolName", toolName);
		requestParam.addProperty("agentVersion", agentVersion);
		requestParam.addProperty("osversion", osversion);
		requestParam.addProperty("configJson", configJson);
		requestParam.addProperty("trackingDetails", "");
		requestParam.addProperty("vault", vault);

		httpRequest.header(ConfigOptionsTest.CONTENT_HEADER_KEY, ConfigOptionsTest.CONTENT_TYPE_VALUE);
		httpRequest.body(requestParam);

		// Response Object
		Response responseAgent = httpRequest.request(Method.POST, "/");

		String responseRegisterAgent = responseAgent.getBody().asString();
		log.debug("SuccessResponse {}", responseRegisterAgent);

		int statusCode = responseAgent.getStatusCode();

		log.debug("StatusCode {}", statusCode);
		Assert.assertEquals(statusCode, 200);
		Assert.assertEquals(responseRegisterAgent.contains("success"), true);

	}

	@Test(priority = 2, dataProvider = "agentdataprovider")
	public void registerAgentAuthorizationFail(String toolName, String agentVersion, String osversion,
			String configJson, String vault) {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("getRegiterAgent");
		RequestSpecification httpRequest = RestAssured.given();
		httpRequest.header("Authorization", CommonUtils.getProperty("authorization"));

		httpRequest.header(ConfigOptionsTest.CONTENT_HEADER_KEY, ConfigOptionsTest.CONTENT_TYPE_VALUE);
		Response response = httpRequest.request(Method.GET, "/");

		String responseRegisterAgentList = response.getBody().asString();
		log.debug("responseRegisterAgentList {}", responseRegisterAgentList);

		int statusCode = response.getStatusCode();
		Assert.assertEquals(statusCode, 200);
		Assert.assertTrue(responseRegisterAgentList.contains("status"), "failure");

	}

	@Test(priority = 3, dataProvider = "agentdataprovider")
	public void registerAgentFail(String toolName, String agentVersion, String osversion, String configJson,
			String vault) throws InterruptedException, IOException {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("registerAgentBaseURI");

		RequestSpecification httpRequest = RestAssured.given();
		httpRequest.header(new Header(ConfigOptionsTest.CSRF_NAME_KEY, xsrfToken));
		httpRequest.cookies(ConfigOptionsTest.SESSION_ID_KEY, jSessionID, ConfigOptionsTest.GRAFANA_COOKIES_ORG,
				CommonUtils.getProperty("grafanaOrg"), ConfigOptionsTest.GRAFANA_COOKIES_ROLE,
				CommonUtils.getProperty("grafanaRole"), ConfigOptionsTest.CSRF_NAME_KEY, xsrfToken);

		// Request payload sending along with post request
		JsonObject requestParam = new JsonObject();
		requestParam.addProperty("toolName", toolName);
		requestParam.addProperty("agentVersion", agentVersion);
		requestParam.addProperty("osversion", osversion);
		requestParam.addProperty("configJson", configJson);
		requestParam.addProperty("trackingDetails", "");

		httpRequest.header(ConfigOptionsTest.CONTENT_HEADER_KEY, ConfigOptionsTest.CONTENT_TYPE_VALUE);
		httpRequest.body(requestParam);

		Response responseAgent = httpRequest.request(Method.POST, "/");

		String responseRegisterAgent = responseAgent.getBody().asString();
		log.debug("FailureResponse {}", responseRegisterAgent);

		int failureStatusCode = responseAgent.getStatusCode();
		Assert.assertEquals(failureStatusCode, 400);
		Assert.assertTrue(responseRegisterAgent.contains("status"), "failure");

	}

}
