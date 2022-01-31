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
import com.google.gson.JsonObject;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class SearchUserAPITest extends GroupsAndUserTestData {

	private static final Logger log = LogManager.getLogger(SearchUserAPITest.class);

	@Test(priority = 1)
	public void searchUser() {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("searchUser");
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header(ConfigOptionsTest.CSRF_NAME_KEY, CommonUtils.xsrfToken));
		httpRequest.cookies(CommonUtils.cookiesMap);
		httpRequest.header(ConfigOptionsTest.AUTH_HEADER_KEY, CommonUtils.jtoken);

		httpRequest.body(CommonUtils.getProperty("userName"));

		httpRequest.header(ConfigOptionsTest.CONTENT_HEADER_KEY, ConfigOptionsTest.CONTENT_TYPE_VALUE);

		Response response = httpRequest.request(Method.POST, "/");

		String searchUser = response.getBody().asString();

		log.debug("searchUser {}", searchUser);

		int statusCode = response.getStatusCode();
		Assert.assertEquals(statusCode, 200);
		Assert.assertTrue(searchUser.contains("status"), "success");
		//Assert.assertTrue(searchUser.contains("data"), "User list displayed");

	}

	@Test(priority = 2)
	public void searchUserFail() {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("searchUser");
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header(ConfigOptionsTest.CSRF_NAME_KEY, CommonUtils.xsrfToken));
		httpRequest.cookies(CommonUtils.cookiesMap);
		httpRequest.header(ConfigOptionsTest.AUTH_HEADER_KEY, CommonUtils.jtoken);
		// Request payload sending along with post request
		JsonObject requestParam = new JsonObject();

		httpRequest.header(ConfigOptionsTest.CONTENT_HEADER_KEY, ConfigOptionsTest.CONTENT_TYPE_VALUE);
		httpRequest.body(requestParam);

		Response response = httpRequest.request(Method.POST, "/");

		String userResponseFail = response.getBody().asString();

		log.debug("SearchUserResponseFail {}", userResponseFail);

		int statusCode = response.getStatusCode();
		Assert.assertEquals(statusCode, 200);
		Assert.assertEquals(userResponseFail.contains("failure"), true);
		Assert.assertTrue(userResponseFail.contains("message"), "Invalid Request");

	}

}
