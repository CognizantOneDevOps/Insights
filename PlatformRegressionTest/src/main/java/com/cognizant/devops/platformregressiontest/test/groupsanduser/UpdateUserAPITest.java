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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.cognizant.devops.platformregressiontest.test.common.CommonUtils;
import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class UpdateUserAPITest extends GroupsAndUserTestData {

	private static final Logger log = LogManager.getLogger(UpdateUserAPITest.class);

	String getOrgUsers = "{}";

	@Test(priority = 1)
	public void getOrgsUsers() {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("getOrgUsers");
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header("XSRF-TOKEN", CommonUtils.xsrfToken));
		httpRequest.cookies(CommonUtils.cookiesMap);
		httpRequest.header(ConfigOptionsTest.AUTH_HEADER_KEY, CommonUtils.jtoken);
		httpRequest.queryParams("orgId", CommonUtils.getProperty("orgId1"));

		httpRequest.header(ConfigOptionsTest.CONTENT_HEADER_KEY, ConfigOptionsTest.CONTENT_TYPE_VALUE);

		Response response = httpRequest.request(Method.POST, "/");

		String getOrgUserResponse = response.getBody().asString();

		log.debug("getOrgUserResponse : {}", getOrgUserResponse);

		getOrgUsers = getOrgUserResponse;

		int statusCode = response.getStatusCode();
		Assert.assertEquals(statusCode, 200);
		Assert.assertTrue(getOrgUserResponse.contains("status"), "success");

	}

	@Test(priority = 2, dataProvider = "updateuserdataprovider")
	public void editUser(String orgId, String userId, String role) {

		String searchUserId = "-1";

		JsonArray getOrgUsersResponse = new JsonParser().parse(getOrgUsers).getAsJsonObject().get("data")
				.getAsJsonArray();

		for (JsonElement jsonElement : getOrgUsersResponse) {
			if (jsonElement.getAsJsonObject().get("login").getAsString().equalsIgnoreCase(userId)) {
				searchUserId = jsonElement.getAsJsonObject().get("userId").getAsString();
				break;
			}
		}

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("updateUser");
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header(ConfigOptionsTest.CSRF_NAME_KEY, CommonUtils.xsrfToken));
		httpRequest.cookies(CommonUtils.cookiesMap);
		httpRequest.header(ConfigOptionsTest.AUTH_HEADER_KEY, CommonUtils.jtoken);

		httpRequest.queryParams("orgId", orgId, "userId", searchUserId, "role", role);

		httpRequest.header(ConfigOptionsTest.CONTENT_HEADER_KEY, ConfigOptionsTest.CONTENT_TYPE_VALUE);

		Response response = httpRequest.request(Method.POST, "/");

		String updateResponse = response.getBody().asString();

		log.debug("updateResponse {}", updateResponse);

		int statusCode = response.getStatusCode();
		Assert.assertEquals(statusCode, 200);
		Assert.assertTrue(updateResponse.contains("message"), "Organization user updated");

	}

	@Test(priority = 3)
	public void editUserFail() {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("updateUser");
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header(ConfigOptionsTest.CSRF_NAME_KEY, CommonUtils.xsrfToken));
		httpRequest.cookies(ConfigOptionsTest.SESSION_ID_KEY, CommonUtils.jSessionID, ConfigOptionsTest.GRAFANA_COOKIES_ORG,
				CommonUtils.getProperty("grafanaOrg"), ConfigOptionsTest.GRAFANA_COOKIES_ROLE,
				CommonUtils.getProperty("grafanaRole"), ConfigOptionsTest.CSRF_NAME_KEY, CommonUtils.xsrfToken);

		// Request payload sending along with post request
		JsonObject requestParam = new JsonObject();

		httpRequest.header(ConfigOptionsTest.CONTENT_HEADER_KEY, ConfigOptionsTest.CONTENT_TYPE_VALUE);
		httpRequest.body(requestParam);

		Response response = httpRequest.request(Method.POST, "/");

		String userResponseFail = response.getBody().asString();

		log.debug("userResponseFail {}", userResponseFail);

		int statusCode = response.getStatusCode();
		Assert.assertEquals(statusCode, 400);
		Assert.assertEquals(userResponseFail.contains("failure"), true);
		Assert.assertTrue(userResponseFail.contains("message"), "Invalid Request");

	}

}
