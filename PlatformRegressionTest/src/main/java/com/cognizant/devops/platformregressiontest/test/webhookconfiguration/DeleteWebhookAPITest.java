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
package com.cognizant.devops.platformregressiontest.test.webhookconfiguration;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.cognizant.devops.platformregressiontest.common.CommonUtils;
import com.google.gson.JsonObject;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class DeleteWebhookAPITest extends WebhookTestData {

	private static final Logger log = LogManager.getLogger(DeleteWebhookAPITest.class);

	@BeforeMethod
	public void onInit() throws InterruptedException, IOException {
		jSessionID = getJsessionId();
		xsrfToken = getXSRFToken(jSessionID);
	}

	@Test(priority = 1)
	public void deleteWebhook() {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("deleteWebhook");
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header("XSRF-TOKEN", xsrfToken));
		httpRequest.cookies("JSESSIONID", jSessionID, "grafanaOrg", CommonUtils.getProperty("grafanaOrg"),
				"grafanaRole", CommonUtils.getProperty("grafanaRole"), "XSRF-TOKEN", xsrfToken);
		httpRequest.header("Authorization", authorization);

		httpRequest.queryParam("webhookname", CommonUtils.getProperty("webhookName"));

		// Request payload sending along with post request
		httpRequest.header("Content-Type", "application/json");

		Response response = httpRequest.request(Method.POST, "/");

		String deleteWebhook = response.getBody().asString();

		log.debug("deleteWebhook" + deleteWebhook);

		int statusCode = response.getStatusCode();
		Assert.assertTrue(deleteWebhook.contains("status"), "success");
		Assert.assertEquals(deleteWebhook.contains("success"), true);
		Assert.assertEquals(statusCode, 200);

	}

	@Test(priority = 2)
	public void deleteWebhookFail() {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("deleteWebhook");
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header("XSRF-TOKEN", xsrfToken));
		httpRequest.cookies("JSESSIONID", jSessionID, "grafanaOrg", CommonUtils.getProperty("grafanaOrg"),
				"grafanaRole", CommonUtils.getProperty("grafanaRole"), "XSRF-TOKEN", xsrfToken);

		JsonObject requestParam = new JsonObject();

		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParam);

		// Response Object
		Response response = httpRequest.request(Method.POST, "/");

		String responseWebhook = response.getBody().asString();
		log.debug("DeleteWebhookResponseFail" + responseWebhook);

		// Statuscode Validation
		int failureStatusCode = response.getStatusCode();
		Assert.assertEquals(failureStatusCode, 400);
		Assert.assertTrue(responseWebhook.contains("status"), "failure");
		Assert.assertEquals(responseWebhook.contains("failure"), true);

	}
}
