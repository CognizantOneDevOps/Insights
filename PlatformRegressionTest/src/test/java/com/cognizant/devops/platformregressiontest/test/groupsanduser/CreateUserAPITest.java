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

public class CreateUserAPITest extends GroupsAndUserTestData {

	GroupsAndUserTestData groupsAndUserTestData = new GroupsAndUserTestData();

	String jSessionID;
	String xsrfToken;
	private FileReader reader = null;
	Properties p = null;

	@BeforeMethod
	public Properties onInit() throws InterruptedException, IOException {
		jSessionID = groupsAndUserTestData.getJsessionId();
		xsrfToken = groupsAndUserTestData.getXSRFToken(jSessionID);

		String path = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator
				+ ConfigOptionsTest.CONFIG_DIR + File.separator + ConfigOptionsTest.PROP_FILE;

		reader = new FileReader(path);

		p = new Properties();

		p.load(reader);
		return p;
	}

	@Test(priority = 1)
	public void getCurrentUserOrgs() {

		RestAssured.baseURI = p.getProperty("baseURI") + "/PlatformService/accessGrpMgmt/getCurrentUserOrgs";
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header("Authorization", groupsAndUserTestData.authorization);
		httpRequest.header("Content-Type", "application/json");
		Response response = httpRequest.request(Method.GET, "/");

		String getCurrentUserOrgResponse = response.getBody().asString();

		int statusCode = response.getStatusCode();
		Assert.assertEquals(statusCode, 200);
		Assert.assertTrue(getCurrentUserOrgResponse.contains("status"), "success");

	}

	@Test(priority = 2, dataProvider = "userdataprovider")
	public void addUser(String name, String email, String userName, String password, String role, String orgName,
			String orgId) {

		RestAssured.baseURI = p.getProperty("baseURI") + "/PlatformService/accessGrpMgmt/addUserInOrg";
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header("XSRF-TOKEN", xsrfToken));
		httpRequest.cookies("JSESSIONID", jSessionID, "grafanaOrg", p.getProperty("grafanaOrg"), "grafanaRole",
				p.getProperty("grafanaRole"), "XSRF-TOKEN", xsrfToken);
		httpRequest.header("Authorization", groupsAndUserTestData.authorization);

		// Request payload sending along with post request

		JsonObject requestParam = new JsonObject();

		requestParam.addProperty("name", name);
		requestParam.addProperty("email", email);
		requestParam.addProperty("userName", userName);
		requestParam.addProperty("password", password);
		requestParam.addProperty("role", role);
		requestParam.addProperty("orgName", orgName);
		requestParam.addProperty("orgId", orgId);

		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParam);

		Response response = httpRequest.request(Method.POST, "/");

		String userResponse = response.getBody().asString();

		System.out.println("userResponse" + userResponse);

		int statusCode = response.getStatusCode();
		Assert.assertEquals(statusCode, 200);
		Assert.assertEquals(userResponse.contains("success"), true);
		Assert.assertTrue(userResponse.contains("data"), "User added to organization");

	}

	@Test(priority = 3)
	public void addUserFail() {

		RestAssured.baseURI = p.getProperty("baseURI") + "/PlatformService/accessGrpMgmt/addUserInOrg";
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header("XSRF-TOKEN", xsrfToken));
		httpRequest.cookies("JSESSIONID", jSessionID, "grafanaOrg", p.getProperty("grafanaOrg"), "grafanaRole",
				p.getProperty("grafanaRole"), "XSRF-TOKEN", xsrfToken);

		// Request payload sending along with post request

		JsonObject requestParam = new JsonObject();

		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParam);

		Response response = httpRequest.request(Method.POST, "/");

		String userResponseFail = response.getBody().asString();

		System.out.println("userResponseFail" + userResponseFail);

		int statusCode = response.getStatusCode();
		Assert.assertEquals(statusCode, 400);
		Assert.assertEquals(userResponseFail.contains("failure"), true);
		Assert.assertTrue(userResponseFail.contains("message"), "Invalid Request");

	}

}
