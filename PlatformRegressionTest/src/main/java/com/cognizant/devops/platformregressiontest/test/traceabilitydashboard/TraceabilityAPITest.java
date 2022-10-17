/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
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
package com.cognizant.devops.platformregressiontest.test.traceabilitydashboard;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.cognizant.devops.platformregressiontest.test.common.CommonUtils;
import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class TraceabilityAPITest extends TraceabilityTest  {
	private static final Logger log = LogManager.getLogger(TraceabilityAPITest.class);
	public static final String ToolNAME = "JIRA";
	public static final String OTHER = "Other";

	@Test(priority = 1)
	public void getAvailableTools() {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("getAvailableTools");

		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header(ConfigOptionsTest.CSRF_NAME_KEY, CommonUtils.xsrfToken));
		httpRequest.cookies(ConfigOptionsTest.SESSION_ID_KEY, CommonUtils.jSessionID,
				ConfigOptionsTest.GRAFANA_COOKIES_ORG, CommonUtils.getProperty("grafanaOrg"),
				ConfigOptionsTest.GRAFANA_COOKIES_ROLE, CommonUtils.getProperty("grafanaRole"),
				ConfigOptionsTest.CSRF_NAME_KEY, CommonUtils.xsrfToken);
		httpRequest.header(ConfigOptionsTest.AUTH_HEADER_KEY, CommonUtils.jtoken);

		httpRequest.header(ConfigOptionsTest.CONTENT_HEADER_KEY, ConfigOptionsTest.CONTENT_TYPE_VALUE);
		//httpRequest.queryParams("version", agentVersion, "tool", toolName, "isWebhook", "false", "type", type);

		// Response Object
		Response responseTools = httpRequest.request(Method.GET, "/");

		String responseAvailableTools = responseTools.getBody().asString();
		log.debug("SuccessResponse {}", responseTools);

		int statusCode = responseTools.getStatusCode();

		log.debug("StatusCode {}", statusCode);
		Assert.assertEquals(statusCode, 200);
		Assert.assertEquals(responseAvailableTools.contains("success"), true);
	}

	@Test(priority = 2)
	public void getToolKeyset() {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("getToolKeyset");

		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header(ConfigOptionsTest.CSRF_NAME_KEY, CommonUtils.xsrfToken));
		httpRequest.cookies(ConfigOptionsTest.SESSION_ID_KEY, CommonUtils.jSessionID,
				ConfigOptionsTest.GRAFANA_COOKIES_ORG, CommonUtils.getProperty("grafanaOrg"),
				ConfigOptionsTest.GRAFANA_COOKIES_ROLE, CommonUtils.getProperty("grafanaRole"),
				ConfigOptionsTest.CSRF_NAME_KEY, CommonUtils.xsrfToken);
		httpRequest.header(ConfigOptionsTest.AUTH_HEADER_KEY, CommonUtils.jtoken);

		httpRequest.header(ConfigOptionsTest.CONTENT_HEADER_KEY, ConfigOptionsTest.CONTENT_TYPE_VALUE);
		httpRequest.queryParams("toolName", ToolNAME);

		// Response Object
		Response responseTools = httpRequest.request(Method.GET, "/");

		String responseAvailableTools = responseTools.getBody().asString();
		log.debug("SuccessResponse {}", responseTools);

		int statusCode = responseTools.getStatusCode();

		log.debug("StatusCode {}", statusCode);
		Assert.assertEquals(statusCode, 200);
		Assert.assertEquals(responseAvailableTools.contains("success"), true);
	}
	@Test(priority = 3, dataProvider = "traceabilitydataprovider")
	public void getEpicIssues(String toolName, String fieldName,
			 String fieldValue, String type) {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("getEpicIssues");

		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header(ConfigOptionsTest.CSRF_NAME_KEY, CommonUtils.xsrfToken));
		httpRequest.cookies(ConfigOptionsTest.SESSION_ID_KEY, CommonUtils.jSessionID,
				ConfigOptionsTest.GRAFANA_COOKIES_ORG, CommonUtils.getProperty("grafanaOrg"),
				ConfigOptionsTest.GRAFANA_COOKIES_ROLE, CommonUtils.getProperty("grafanaRole"),
				ConfigOptionsTest.CSRF_NAME_KEY, CommonUtils.xsrfToken);
		httpRequest.header(ConfigOptionsTest.AUTH_HEADER_KEY, CommonUtils.jtoken);

		httpRequest.header(ConfigOptionsTest.CONTENT_HEADER_KEY, ConfigOptionsTest.CONTENT_TYPE_VALUE);
		httpRequest.queryParams("toolName", toolName,"fieldName",fieldName,"fieldValue",fieldValue,"type",type);

		// Response Object
		Response responseTools = httpRequest.request(Method.GET, "/");

		String responseAvailableTools = responseTools.getBody().asString();
		log.debug("SuccessResponse {}", responseTools);

		int statusCode = responseTools.getStatusCode();

		log.debug("StatusCode {}", statusCode);
		Assert.assertEquals(statusCode, 200);
		Assert.assertEquals(responseAvailableTools.contains("success"), true);
	}

	@Test(priority = 4, dataProvider = "traceabilitydataprovider")
	public void getPipeline(String toolName, String fieldName,
			 String fieldValue, String type) {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("getPipeline");

		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header(ConfigOptionsTest.CSRF_NAME_KEY, CommonUtils.xsrfToken));
		httpRequest.cookies(ConfigOptionsTest.SESSION_ID_KEY, CommonUtils.jSessionID,
				ConfigOptionsTest.GRAFANA_COOKIES_ORG, CommonUtils.getProperty("grafanaOrg"),
				ConfigOptionsTest.GRAFANA_COOKIES_ROLE, CommonUtils.getProperty("grafanaRole"),
				ConfigOptionsTest.CSRF_NAME_KEY, CommonUtils.xsrfToken);
		httpRequest.header(ConfigOptionsTest.AUTH_HEADER_KEY, CommonUtils.jtoken);

		httpRequest.header(ConfigOptionsTest.CONTENT_HEADER_KEY, ConfigOptionsTest.CONTENT_TYPE_VALUE);
		httpRequest.queryParams("toolName", toolName,"fieldName",fieldName,"fieldValue",fieldValue,"type",OTHER);

		// Response Object
		Response responseTools = httpRequest.request(Method.GET, "/");

		String responseAvailableTools = responseTools.getBody().asString();
		log.debug("SuccessResponse {}", responseTools);

		int statusCode = responseTools.getStatusCode();

		log.debug("StatusCode {}", statusCode);
		Assert.assertEquals(statusCode, 200);
		Assert.assertEquals(responseAvailableTools.contains("success"), true);
	}

}
