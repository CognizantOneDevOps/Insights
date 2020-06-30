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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.cognizant.devops.platformregressiontest.common.ConfigOptionsTest;
import com.google.gson.JsonObject;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class RegisterAgentAPITest extends TestData {

	TestData testData = new TestData();

	String jSessionID;
	String xsrfToken;
	private FileReader reader = null;
	Properties p = null;

	@BeforeMethod
	public Properties onInit() throws InterruptedException, IOException {
		jSessionID = testData.getJsessionId();
		xsrfToken = testData.getXSRFToken(jSessionID);

		String path = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator
				+ ConfigOptionsTest.CONFIG_DIR + File.separator + ConfigOptionsTest.PROP_FILE;

		reader = new FileReader(path);
		p = new Properties();

		p.load(reader);

		return p;
	}

	@Test(priority = 1, dataProvider = "agentdataprovider")
	public void registerAgentPost(String toolName, String agentVersion, String osversion, String configJson,
			String vault) {

		RestAssured.baseURI = p.getProperty("baseURI") + "/PlatformService/admin/agentConfiguration/2.0/registerAgent";

		RequestSpecification httpRequest = RestAssured.given();
		httpRequest.header(new Header("XSRF-TOKEN", xsrfToken));
		httpRequest.cookies("JSESSIONID", jSessionID, "grafanaOrg", p.getProperty("grafanaOrg"), "grafanaRole",
				p.getProperty("grafanaRole"), "XSRF-TOKEN", xsrfToken);
		httpRequest.header("Authorization", testData.authorization);

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
		System.out.println("SuccessResponse" + responseRegisterAgent);

		int statusCode = responseAgent.getStatusCode();

		System.out.println("StatusCode" + statusCode);
		Assert.assertEquals(statusCode, 200);
		Assert.assertEquals(responseRegisterAgent.contains("success"), true);

	}

	@Test(priority = 2, dataProvider = "agentdataprovider")
	public void registerAgentFail(String toolName, String agentVersion, String osversion, String configJson,
			String vault) {

		RestAssured.baseURI = p.getProperty("baseURI") + "/PlatformService/admin/agentConfiguration/2.0/registerAgent";

		RequestSpecification httpRequest = RestAssured.given();
		httpRequest.header(new Header("XSRF-TOKEN", xsrfToken));
		httpRequest.cookies("JSESSIONID", jSessionID, "grafanaOrg", p.getProperty("grafanaOrg"), "grafanaRole",
				p.getProperty("grafanaRole"), "XSRF-TOKEN", xsrfToken);

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
		System.out.println("FailureResponse" + responseRegisterAgent);

		// Statuscode Validation
		int FailureStatusCode = responseAgent.getStatusCode();
		Assert.assertEquals(FailureStatusCode, 400);
		Assert.assertTrue(responseRegisterAgent.contains("status"), "failure");

	}

	@Test(priority = 3, dataProvider = "agentdataprovider")
	public void registerAgentFailPost(String toolName, String agentVersion, String osversion, String configJson,
			String vault) {

		RestAssured.baseURI = p.getProperty("baseURI") + "/PlatformService/admin/agentConfiguration/2.0/registerAgent";

		RequestSpecification httpRequest = RestAssured.given();
		httpRequest.header(new Header("XSRF-TOKEN", xsrfToken));
		httpRequest.cookies("JSESSIONID", jSessionID, "grafanaOrg", p.getProperty("grafanaOrg"), "grafanaRole",
				p.getProperty("grafanaRole"), "XSRF-TOKEN", xsrfToken);

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
		System.out.println("FailureResponse" + responseRegisterAgent);

		// Statuscode Validation
		int FailureStatusCode = responseAgent.getStatusCode();
		Assert.assertEquals(FailureStatusCode, 400);
		Assert.assertTrue(responseRegisterAgent.contains("status"), "failure");

	}

}
