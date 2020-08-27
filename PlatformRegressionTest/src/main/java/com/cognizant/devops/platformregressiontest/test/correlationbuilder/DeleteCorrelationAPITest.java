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

public class DeleteCorrelationAPITest extends CorrelationTestData {

	private static final Logger log = LogManager.getLogger(DeleteCorrelationAPITest.class);

	String jSessionID;
	String xsrfToken;

	@BeforeMethod
	public void onInit() throws InterruptedException, IOException {

		jSessionID = CommonUtils.getJsessionId();
		xsrfToken = CommonUtils.getXSRFToken(jSessionID);
	}

	@Test(priority = 1, dataProvider = "correlationdeletedataprovider")
	public void deleteCorrelation(String relationName) {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("deleteCorrelation");

		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header("XSRF-TOKEN", xsrfToken));
		httpRequest.cookies("JSESSIONID", jSessionID, "grafanaOrg", CommonUtils.getProperty("grafanaOrg"),
				"grafanaRole", CommonUtils.getProperty("grafanaRole"), "XSRF-TOKEN", xsrfToken);
		httpRequest.header("Authorization", CommonUtils.getProperty("authorization"));

		// Request payload sending along with post request

		JsonObject requestParam = new JsonObject();
		requestParam.addProperty("relationName", relationName);

		httpRequest.header(ConfigOptionsTest.CONTENT_HEADER_KEY, ConfigOptionsTest.CONTENT_TYPE_VALUE);
		httpRequest.body(requestParam);

		Response response = httpRequest.request(Method.POST, "/");

		String correaltionResponse = response.getBody().asString();

		log.debug("correaltionResponse {}" , correaltionResponse);

		int statusCode = response.getStatusCode();
		Assert.assertTrue(correaltionResponse.contains("status"), "success");
		Assert.assertEquals(correaltionResponse.contains("success"), true);
		Assert.assertEquals(statusCode, 200);

	}

	@Test(priority = 2, dataProvider = "correlationdeletedataprovider")
	public void deleteCorrelationFail(String relationName) {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("deleteCorrelation");
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header("XSRF-TOKEN", xsrfToken));
		httpRequest.cookies("JSESSIONID", jSessionID, "grafanaOrg", CommonUtils.getProperty("grafanaOrg"),
				"grafanaRole", CommonUtils.getProperty("grafanaRole"), "XSRF-TOKEN", xsrfToken);
		httpRequest.header("Authorization", CommonUtils.getProperty("authorization"));

		// Request payload sending along with post request

		JsonObject requestParam = new JsonObject();
		requestParam.addProperty("relationName", "");

		httpRequest.header(ConfigOptionsTest.CONTENT_HEADER_KEY, ConfigOptionsTest.CONTENT_TYPE_VALUE);
		httpRequest.body(requestParam);

		Response response = httpRequest.request(Method.POST, "/");

		String deleteResponseFail = response.getBody().asString();

		log.debug("deleteResponseFail {}" , deleteResponseFail);

		int statusCode = response.getStatusCode();
		Assert.assertTrue(deleteResponseFail.contains("status"), "failure");
		Assert.assertTrue(deleteResponseFail.contains("message"), "Unable to update correlation");
		Assert.assertEquals(deleteResponseFail.contains("failure"), true);
		Assert.assertEquals(statusCode, 200);

	}

}
