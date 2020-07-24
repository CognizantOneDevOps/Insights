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
package com.cognizant.devops.platformregressiontest.test.groupsanduser;

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

public class CreateUserAPITest extends GroupsAndUserTestData {

	private static final Logger log = LogManager.getLogger(CreateUserAPITest.class);

	String jSessionID;
	String xsrfToken;

	@BeforeMethod
	public void onInit() throws InterruptedException, IOException {

		jSessionID = CommonUtils.getJsessionId();
		xsrfToken = CommonUtils.getXSRFToken(jSessionID);
	}

	@Test(priority = 1)
	public void getCurrentUserOrgs() {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("getCurrentUserOrgs");
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(ConfigOptionsTest.AUTH_HEADER_KEY, CommonUtils.getProperty("authorization"));
		httpRequest.header(ConfigOptionsTest.CONTENT_HEADER_KEY, ConfigOptionsTest.CONTENT_TYPE_VALUE);
		Response response = httpRequest.request(Method.GET, "/");

		String getCurrentUserOrgResponse = response.getBody().asString();

		int statusCode = response.getStatusCode();
		Assert.assertEquals(statusCode, 200);
		Assert.assertTrue(getCurrentUserOrgResponse.contains("status"), "success");

	}

	@Test(priority = 2, dataProvider = "userdataprovider")
	public void addUser(String name, String email, String userName, String password, String role, String orgName,
			String orgId) {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("addUser");
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header(ConfigOptionsTest.CSRF_NAME_KEY, xsrfToken));
		httpRequest.cookies(ConfigOptionsTest.SESSION_ID_KEY, jSessionID, ConfigOptionsTest.GRAFANA_COOKIES_ORG,
				CommonUtils.getProperty("grafanaOrg"), ConfigOptionsTest.GRAFANA_COOKIES_ROLE,
				CommonUtils.getProperty("grafanaRole"), ConfigOptionsTest.CSRF_NAME_KEY, xsrfToken);
		httpRequest.header(ConfigOptionsTest.AUTH_HEADER_KEY, CommonUtils.getProperty("authorization"));

		// Request payload sending along with post request
		JsonObject requestParam = new JsonObject();
		requestParam.addProperty("name", name);
		requestParam.addProperty("email", email);
		requestParam.addProperty("userName", userName);
		requestParam.addProperty("password", password);
		requestParam.addProperty("role", role);
		requestParam.addProperty("orgName", orgName);
		requestParam.addProperty("orgId", orgId);

		httpRequest.header(ConfigOptionsTest.CONTENT_HEADER_KEY, ConfigOptionsTest.CONTENT_TYPE_VALUE);
		httpRequest.body(requestParam);

		Response response = httpRequest.request(Method.POST, "/");

		String userResponse = response.getBody().asString();

		log.debug("AddUserResponse {}", userResponse);

		int statusCode = response.getStatusCode();
		Assert.assertEquals(statusCode, 200);
		Assert.assertEquals(userResponse.contains("success"), true);
		Assert.assertTrue(userResponse.contains("data"), "User added to organization");

	}

	@Test(priority = 3)
	public void addUserFail() {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("addUser");
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header(ConfigOptionsTest.CSRF_NAME_KEY, xsrfToken));
		httpRequest.cookies(ConfigOptionsTest.SESSION_ID_KEY, jSessionID, ConfigOptionsTest.GRAFANA_COOKIES_ORG,
				CommonUtils.getProperty("grafanaOrg"), ConfigOptionsTest.GRAFANA_COOKIES_ROLE,
				CommonUtils.getProperty("grafanaRole"), ConfigOptionsTest.CSRF_NAME_KEY, xsrfToken);

		// Request payload sending along with post request

		JsonObject requestParam = new JsonObject();

		httpRequest.header(ConfigOptionsTest.CONTENT_HEADER_KEY, ConfigOptionsTest.CONTENT_TYPE_VALUE);
		httpRequest.body(requestParam);

		Response response = httpRequest.request(Method.POST, "/");

		String userResponseFail = response.getBody().asString();

		log.debug("AddUserResponseFail {}" , userResponseFail);

		int statusCode = response.getStatusCode();
		Assert.assertEquals(statusCode, 400);
		Assert.assertEquals(userResponseFail.contains("failure"), true);
		Assert.assertTrue(userResponseFail.contains("message"), "Invalid Request");

	}

}
