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
package com.cognizant.devops.platformregressiontest.test.correlationbuilder;

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

public class UpdateCorrelationAPITest extends CorrelationTestData {

	CorrelationTestData correlationTestData = new CorrelationTestData();

	String jSessionID;
	String xsrfToken;
	private FileReader reader = null;
	Properties p = null;

	@BeforeMethod
	public Properties onInit() throws InterruptedException, IOException {
		jSessionID = correlationTestData.getJsessionId();
		xsrfToken = correlationTestData.getXSRFToken(jSessionID);

		String path = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator
				+ ConfigOptionsTest.CONFIG_DIR + File.separator + ConfigOptionsTest.PROP_FILE;

		reader = new FileReader(path);

		p = new Properties();

		p.load(reader);
		return p;
	}

	@Test(priority = 1)
	public void getToolsAndCategories() {

		RestAssured.baseURI = p.getProperty("baseURI") + "/PlatformService/datadictionary/getToolsAndCategories";
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header("Authorization", correlationTestData.authorization);
		httpRequest.header("Content-Type", "application/json");
		Response response = httpRequest.request(Method.GET, "/");

		String correlationJson = response.getBody().asString();

		int statusCode = response.getStatusCode();
		Assert.assertEquals(statusCode, 200);
		Assert.assertTrue(correlationJson.contains("status"), "success");

	}

	@Test(priority = 2, dataProvider = "correlationupdatedataprovider")
	public void updateCorrelation(String relationName, String correlationFlag) {

		RestAssured.baseURI = p.getProperty("baseURI") + "/PlatformService/admin/correlationbuilder/updateCorrelation";
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header("XSRF-TOKEN", xsrfToken));
		httpRequest.cookies("JSESSIONID", jSessionID, "grafanaOrg", p.getProperty("grafanaOrg"), "grafanaRole",
				p.getProperty("grafanaRole"), "XSRF-TOKEN", xsrfToken);
		httpRequest.header("Authorization", correlationTestData.authorization);

		// Request payload sending along with post request

		JsonObject requestParam = new JsonObject();
		requestParam.addProperty("relationName", relationName);
		requestParam.addProperty("correlationFlag", correlationFlag);

		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParam);

		Response response = httpRequest.request(Method.POST, "/");

		String correaltionResponse = response.getBody().asString();

		int statusCode = response.getStatusCode();
		Assert.assertTrue(correaltionResponse.contains("status"), "success");
		Assert.assertEquals(correaltionResponse.contains("success"), true);
		Assert.assertEquals(statusCode, 200);

	}

	@Test(priority = 3, dataProvider = "correlationupdatedataprovider")
	public void updateCorrelationFail(String relationName, String correlationFlag) {

		RestAssured.baseURI = p.getProperty("baseURI") + "/PlatformService/admin/correlationbuilder/updateCorrelation";
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header("XSRF-TOKEN", xsrfToken));
		httpRequest.cookies("JSESSIONID", jSessionID, "grafanaOrg", p.getProperty("grafanaOrg"), "grafanaRole",
				p.getProperty("grafanaRole"), "XSRF-TOKEN", xsrfToken);
		httpRequest.header("Authorization", correlationTestData.authorization);

		// Request payload sending along with post request

		JsonObject requestParam = new JsonObject();
		requestParam.addProperty("relationName", "");
		requestParam.addProperty("correlationFlag", correlationFlag);

		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParam);

		Response response = httpRequest.request(Method.POST, "/");

		String correaltionResponseFail = response.getBody().asString();

		System.out.println("correaltionResponseFail" + correaltionResponseFail);

		Assert.assertTrue(correaltionResponseFail.contains("message"), "Unable to update correlation");
		Assert.assertEquals(correaltionResponseFail.contains("failure"), true);

	}

}
