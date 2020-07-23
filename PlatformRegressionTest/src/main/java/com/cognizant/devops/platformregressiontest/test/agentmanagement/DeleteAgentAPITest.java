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
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.cognizant.devops.platformregressiontest.common.CommonUtils;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class DeleteAgentAPITest extends TestData {

	private static final Logger log = LogManager.getLogger(RegisterAgentAPITest.class);

	@BeforeMethod
	public Properties onInit() throws InterruptedException, IOException {
		jSessionID = getJsessionId();
		xsrfToken = getXSRFToken(jSessionID);

		Properties CommonUtils = null;

		return CommonUtils;
	}

	@Test(priority = 1, dataProvider = "agentdeletedataprovider")
	public void deleteAgent(String agentId, String toolName, String osversion, String action) {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("deleteAgentBaseURI");

		RequestSpecification httpRequest = RestAssured.given();
		httpRequest.header(new Header("XSRF-TOKEN", xsrfToken));
		httpRequest.cookies("JSESSIONID", jSessionID, "grafanaOrg", CommonUtils.getProperty("grafanaOrg"),
				"grafanaRole", CommonUtils.getProperty("grafanaRole"), "XSRF-TOKEN", xsrfToken);
		httpRequest.header("Authorization", authorization);
		httpRequest.queryParams("agentId", agentId, "toolName", toolName, "osversion", osversion, "action", action);

		httpRequest.header("Content-Type", "application/json");

		// Response Object
		Response responseAgent = httpRequest.request(Method.POST, "/");

		String responseDeleteAgent = responseAgent.getBody().asString();
		log.debug("Response" + responseDeleteAgent);

		// Statuscode Validation
		int statusCode = responseAgent.getStatusCode();
		log.debug("StatusCode" + statusCode);
		Assert.assertEquals(statusCode, 200);
		Assert.assertTrue(responseDeleteAgent.contains("status"), "success");

	}

	@Test(priority = 2, dataProvider = "agentdeletedataprovider")
	public void deleteAgentFail(String agentId, String toolName, String osversion, String action) {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("deleteAgentBaseURI");

		RequestSpecification httpRequest = RestAssured.given();
		httpRequest.header(new Header("XSRF-TOKEN", xsrfToken));
		httpRequest.cookies("JSESSIONID", jSessionID, "grafanaOrg", CommonUtils.getProperty("grafanaOrg"),
				"grafanaRole", CommonUtils.getProperty("grafanaRole"), "XSRF-TOKEN", xsrfToken);
		httpRequest.header("Authorization", authorization);
		httpRequest.queryParams("agentId", agentId, "toolName", toolName, "osversion", osversion, "action", action);

		httpRequest.header("Content-Type", "application/json");

		// Response Object
		Response responseAgent = httpRequest.request(Method.POST, "/");

		String responseDeleteAgent = responseAgent.getBody().asString();
		log.debug("responseDeleteAgent" + responseDeleteAgent);

		// Statuscode Validation
		int FailurestatusCode = responseAgent.getStatusCode();
		Assert.assertEquals(FailurestatusCode, 200);
		Assert.assertTrue(responseDeleteAgent.contains("status"), "failure");
		Assert.assertTrue(responseDeleteAgent.contains("message"), "No entity found for query");

	}

}
