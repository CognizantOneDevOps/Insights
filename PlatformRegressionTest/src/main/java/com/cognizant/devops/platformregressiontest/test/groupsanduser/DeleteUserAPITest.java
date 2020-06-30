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

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class DeleteUserAPITest extends GroupsAndUserTestData {

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
	public void getOrgsUsers() {

		RestAssured.baseURI = p.getProperty("baseURI") + "/PlatformService/admin/userMgmt/getOrgUsers";
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header("XSRF-TOKEN", xsrfToken));
		httpRequest.cookies("JSESSIONID", jSessionID, "grafanaOrg", p.getProperty("grafanaOrg"), "grafanaRole",
				p.getProperty("grafanaRole"), "XSRF-TOKEN", xsrfToken);
		httpRequest.header("Authorization", groupsAndUserTestData.authorization);
		httpRequest.queryParams("orgId", p.getProperty("orgId1"));

		httpRequest.header("Content-Type", "application/json");

		Response response = httpRequest.request(Method.POST, "/");

		String getOrgUserResponse = response.getBody().asString();

		System.out.println("getOrgUserResponse" + getOrgUserResponse);

		int statusCode = response.getStatusCode();
		Assert.assertEquals(statusCode, 200);
		Assert.assertTrue(getOrgUserResponse.contains("status"), "success");

	}

	@Test(priority = 2, dataProvider = "updateuserdataprovider")
	public void deleteUser(String orgId, String userId, String role) {

		RestAssured.baseURI = p.getProperty("baseURI") + "/PlatformService/admin/userMgmt/deleteOrganizationUser";
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header("XSRF-TOKEN", xsrfToken));
		httpRequest.cookies("JSESSIONID", jSessionID, "grafanaOrg", p.getProperty("grafanaOrg"), "grafanaRole",
				p.getProperty("grafanaRole"), "XSRF-TOKEN", xsrfToken);
		httpRequest.header("Authorization", groupsAndUserTestData.authorization);
		httpRequest.queryParams("orgId", orgId, "userId", userId, "role", "role");

		httpRequest.header("Content-Type", "application/json");

		Response response = httpRequest.request(Method.POST, "/");

		String deleteUser = response.getBody().asString();

		System.out.println("deleteUser" + deleteUser);

		int statusCode = response.getStatusCode();
		Assert.assertEquals(statusCode, 200);
		Assert.assertTrue(deleteUser.contains("message"), "User removed from organization");

	}

	@Test(priority = 3, dataProvider = "updateuserdataprovider")
	public void deleteUserFail(String orgId, String userId, String role) {

		RestAssured.baseURI = p.getProperty("baseURI") + "/PlatformService/admin/userMgmt/deleteOrganizationUser";
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header("XSRF-TOKEN", xsrfToken));
		httpRequest.cookies("JSESSIONID", jSessionID, "grafanaOrg", p.getProperty("grafanaOrg"), "grafanaRole",
				p.getProperty("grafanaRole"), "XSRF-TOKEN", xsrfToken);
		httpRequest.header("Authorization", groupsAndUserTestData.authorization);
		httpRequest.queryParams("orgId", orgId, "userId", "", "role", "role");

		httpRequest.header("Content-Type", "application/json");

		Response response = httpRequest.request(Method.POST, "/");

		String deleteUserResponseFail = response.getBody().asString();

		System.out.println("deleteUserResponseFail" + deleteUserResponseFail);

		int statusCode = response.getStatusCode();
		Assert.assertEquals(statusCode, 400);
		Assert.assertEquals(deleteUserResponseFail.contains("failure"), false);
		Assert.assertTrue(deleteUserResponseFail.contains("message"), "Invalid Request");

	}

}
