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
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class DeleteAgentAPITest extends AgentTestData {
	
	private static final Logger log = LogManager.getLogger(DeleteAgentAPITest.class);

	String jSessionID;
	String xsrfToken;

	@BeforeMethod
	public void onInit() throws InterruptedException, IOException {

		jSessionID = CommonUtils.getJsessionId();
		xsrfToken = CommonUtils.getXSRFToken(jSessionID);
	}

	@Test(priority = 1, dataProvider = "agentdeletedataprovider")
	public void deleteAgent(String agentId, String toolName, String osversion, String action) {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("deleteAgentBaseURI");

		RequestSpecification httpRequest = RestAssured.given();
		httpRequest.header(new Header(ConfigOptionsTest.CSRF_NAME_KEY, xsrfToken));
		httpRequest.cookies(ConfigOptionsTest.SESSION_ID_KEY, jSessionID, ConfigOptionsTest.GRAFANA_COOKIES_ORG,
				CommonUtils.getProperty("grafanaOrg"), ConfigOptionsTest.GRAFANA_COOKIES_ROLE,
				CommonUtils.getProperty("grafanaRole"), ConfigOptionsTest.CSRF_NAME_KEY, xsrfToken);
		httpRequest.header(ConfigOptionsTest.AUTH_HEADER_KEY, CommonUtils.getProperty("authorization"));
		httpRequest.queryParams("agentId", agentId, "toolName", toolName, "osversion", osversion, "action", action);

		httpRequest.header(ConfigOptionsTest.CONTENT_HEADER_KEY, ConfigOptionsTest.CONTENT_TYPE_VALUE);

		Response responseAgent = httpRequest.request(Method.POST, "/");

		String responseDeleteAgent = responseAgent.getBody().asString();
		log.debug("Response {}" , responseDeleteAgent);

		int statusCode = responseAgent.getStatusCode();
		log.debug("StatusCode {}" , statusCode);
		Assert.assertEquals(statusCode, 200);
		Assert.assertTrue(responseDeleteAgent.contains("status"), "success");

	}

	@Test(priority = 2, dataProvider = "agentdeletedataprovider")
	public void deleteAgentFail(String agentId, String toolName, String osversion, String action) {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("deleteAgentBaseURI");

		RequestSpecification httpRequest = RestAssured.given();
		httpRequest.header(new Header(ConfigOptionsTest.CSRF_NAME_KEY, xsrfToken));
		httpRequest.cookies(ConfigOptionsTest.SESSION_ID_KEY, jSessionID, ConfigOptionsTest.GRAFANA_COOKIES_ORG,
				CommonUtils.getProperty("grafanaOrg"), ConfigOptionsTest.GRAFANA_COOKIES_ROLE,
				CommonUtils.getProperty("grafanaRole"), ConfigOptionsTest.CSRF_NAME_KEY, xsrfToken);
		httpRequest.header(ConfigOptionsTest.AUTH_HEADER_KEY, CommonUtils.getProperty("authorization"));
		
		httpRequest.queryParams("agentId", agentId, "toolName", toolName, "osversion", osversion, "action", action);
		
		httpRequest.header(ConfigOptionsTest.CONTENT_HEADER_KEY, ConfigOptionsTest.CONTENT_TYPE_VALUE);
		
		Response responseAgent = httpRequest.request(Method.POST, "/");

		String responseDeleteAgent = responseAgent.getBody().asString();
		log.debug("responseDeleteAgent {}" , responseDeleteAgent);

		int failurestatusCode = responseAgent.getStatusCode();
		Assert.assertEquals(failurestatusCode, 200);
		Assert.assertTrue(responseDeleteAgent.contains("status"), "failure");
		Assert.assertTrue(responseDeleteAgent.contains("message"), "No entity found for query");

	}

}
