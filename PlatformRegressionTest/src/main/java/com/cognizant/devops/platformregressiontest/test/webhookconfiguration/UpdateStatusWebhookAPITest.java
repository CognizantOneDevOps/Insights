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

public class UpdateStatusWebhookAPITest extends WebhookTestData {

	private static final Logger log = LogManager.getLogger(UpdateStatusWebhookAPITest.class);

	@BeforeMethod
	public void onInit() throws InterruptedException, IOException {
		jSessionID = getJsessionId();
		xsrfToken = getXSRFToken(jSessionID);
	}

	@Test(priority = 1)
	public void subscribeWebhook() {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("subscribeWebhook");
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header("XSRF-TOKEN", xsrfToken));
		httpRequest.cookies("JSESSIONID", jSessionID, "grafanaOrg", CommonUtils.getProperty("grafanaOrg"),
				"grafanaRole", CommonUtils.getProperty("grafanaRole"), "XSRF-TOKEN", xsrfToken);
		httpRequest.header("Authorization", authorization);

		// Request payload sending along with post request
		JsonObject requestParam = new JsonObject();

		requestParam.addProperty("webhookName", "pvt_webhook_test");
		requestParam.addProperty("statussubscribe", true);

		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParam);

		Response response = httpRequest.request(Method.POST, "/");

		String SaveWebhookStatus = response.getBody().asString();

		log.debug("SaveWebhookStatus" + SaveWebhookStatus);

		int statusCode = response.getStatusCode();
		Assert.assertTrue(SaveWebhookStatus.contains("status"), "success");
		Assert.assertEquals(SaveWebhookStatus.contains("success"), true);
		Assert.assertEquals(statusCode, 200);

	}

	@Test(priority = 2)
	public void unSubscribeWebhook() {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("subscribeWebhook");
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header("XSRF-TOKEN", xsrfToken));
		httpRequest.cookies("JSESSIONID", jSessionID, "grafanaOrg", CommonUtils.getProperty("grafanaOrg"),
				"grafanaRole", CommonUtils.getProperty("grafanaRole"), "XSRF-TOKEN", xsrfToken);
		httpRequest.header("Authorization", authorization);

		JsonObject requestParam = new JsonObject();

		requestParam.addProperty("webhookName", CommonUtils.getProperty("webhookName"));
		requestParam.addProperty("statussubscribe", CommonUtils.getProperty("statusUnsubscribe"));

		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParam);

		// Response Object
		Response response = httpRequest.request(Method.POST, "/");

		String unsubscribeResponse = response.getBody().asString();
		log.debug("unsubscribeResponse" + unsubscribeResponse);

		// Statuscode Validation
		int statusCode = response.getStatusCode();
		Assert.assertEquals(statusCode, 200);
		Assert.assertTrue(unsubscribeResponse.contains("status"), "success");
		Assert.assertEquals(unsubscribeResponse.contains("success"), true);

	}
}
