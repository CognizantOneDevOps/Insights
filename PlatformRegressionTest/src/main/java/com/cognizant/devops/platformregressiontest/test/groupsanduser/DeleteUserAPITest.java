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

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class DeleteUserAPITest extends GroupsAndUserTestData {
	
	private static final Logger log = LogManager.getLogger(DeleteUserAPITest.class);

	String jSessionID;
	String xsrfToken;

	@BeforeMethod
	public void onInit() throws InterruptedException, IOException {

		jSessionID = CommonUtils.getJsessionId();
		xsrfToken = CommonUtils.getXSRFToken(jSessionID);
	}

	@Test(priority = 1)
	public void getOrgsUsers() {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("getOrgUsers");
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header("XSRF-TOKEN", xsrfToken));
		httpRequest.cookies("JSESSIONID", jSessionID, "grafanaOrg", CommonUtils.getProperty("grafanaOrg"),
				"grafanaRole", CommonUtils.getProperty("grafanaRole"), "XSRF-TOKEN", xsrfToken);
		httpRequest.header(ConfigOptionsTest.AUTH_HEADER_KEY, CommonUtils.getProperty("authorization"));
		httpRequest.queryParams("orgId", CommonUtils.getProperty("orgId1"));

		httpRequest.header(ConfigOptionsTest.CONTENT_HEADER_KEY, ConfigOptionsTest.CONTENT_TYPE_VALUE);

		Response response = httpRequest.request(Method.POST, "/");

		String getOrgUserResponse = response.getBody().asString();

		log.debug("getOrgUserResponse" + getOrgUserResponse);

		int statusCode = response.getStatusCode();
		Assert.assertEquals(statusCode, 200);
		Assert.assertTrue(getOrgUserResponse.contains("status"), "success");

	}

	@Test(priority = 2, dataProvider = "updateuserdataprovider")
	public void deleteUser(String orgId, String userId, String role) {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("deleteUser");
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header(ConfigOptionsTest.CSRF_NAME_KEY, xsrfToken));
		httpRequest.cookies(ConfigOptionsTest.SESSION_ID_KEY, jSessionID, ConfigOptionsTest.GRAFANA_COOKIES_ORG,
				CommonUtils.getProperty("grafanaOrg"), ConfigOptionsTest.GRAFANA_COOKIES_ROLE,
				CommonUtils.getProperty("grafanaRole"), ConfigOptionsTest.CSRF_NAME_KEY, xsrfToken);
		httpRequest.header(ConfigOptionsTest.AUTH_HEADER_KEY, CommonUtils.getProperty("authorization"));
		httpRequest.queryParams("orgId", orgId, "userId", userId, "role", "role");

		httpRequest.header(ConfigOptionsTest.CONTENT_HEADER_KEY, ConfigOptionsTest.CONTENT_TYPE_VALUE);

		Response response = httpRequest.request(Method.POST, "/");

		String deleteUser = response.getBody().asString();

		log.debug("deleteUser" + deleteUser);

		int statusCode = response.getStatusCode();
		Assert.assertEquals(statusCode, 200);
		Assert.assertTrue(deleteUser.contains("message"), "User removed from organization");

	}

	@Test(priority = 3, dataProvider = "updateuserdataprovider")
	public void deleteUserFail(String orgId, String userId, String role) {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("deleteUser");
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header("XSRF-TOKEN", xsrfToken));
		httpRequest.cookies("JSESSIONID", jSessionID, "grafanaOrg", CommonUtils.getProperty("grafanaOrg"),
				"grafanaRole", CommonUtils.getProperty("grafanaRole"), "XSRF-TOKEN", xsrfToken);
		httpRequest.header(ConfigOptionsTest.AUTH_HEADER_KEY, CommonUtils.getProperty("authorization"));
		httpRequest.queryParams("orgId", orgId, "userId", "", "role", "role");

		httpRequest.header(ConfigOptionsTest.CONTENT_HEADER_KEY, ConfigOptionsTest.CONTENT_TYPE_VALUE);

		Response response = httpRequest.request(Method.POST, "/");

		String deleteUserResponseFail = response.getBody().asString();

		log.debug("deleteUserResponseFail" + deleteUserResponseFail);

		int statusCode = response.getStatusCode();
		Assert.assertEquals(statusCode, 400);
		Assert.assertEquals(deleteUserResponseFail.contains("failure"), false);
		Assert.assertTrue(deleteUserResponseFail.contains("message"), "Invalid Request");

	}

}
