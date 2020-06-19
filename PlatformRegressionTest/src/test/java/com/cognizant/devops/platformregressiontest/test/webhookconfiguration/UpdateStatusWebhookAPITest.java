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

public class UpdateStatusWebhookAPITest extends WebhookTestData {

	WebhookTestData webhookTestData = new WebhookTestData();
	String jSessionID;
	String xsrfToken;
	private FileReader reader = null;
	Properties p = null;

	@BeforeMethod
	public Properties onInit() throws InterruptedException, IOException {
		jSessionID = webhookTestData.getJsessionId();
		xsrfToken = webhookTestData.getXSRFToken(jSessionID);

		String path = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator
				+ ConfigOptionsTest.CONFIG_DIR + File.separator + ConfigOptionsTest.PROP_FILE;

		reader = new FileReader(path);

		p = new Properties();

		p.load(reader);
		return p;
	}

	@Test(priority = 1)
	public void subscribeWebhook() {

		RestAssured.baseURI = p.getProperty("baseURI") + "/PlatformService/admin/webhook/updateWebhookStatus";
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header("XSRF-TOKEN", xsrfToken));
		httpRequest.cookies("JSESSIONID", jSessionID, "grafanaOrg", "1", "grafanaRole", "Admin", "XSRF-TOKEN",
				xsrfToken);
		httpRequest.header("Authorization", webhookTestData.authorization);

		// Request payload sending along with post request
		JsonObject requestParam = new JsonObject();

		requestParam.addProperty("webhookName", "pvt_webhook_test");
		requestParam.addProperty("statussubscribe", true);

		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParam);

		Response response = httpRequest.request(Method.POST, "/");

		String SaveWebhookStatus = response.getBody().asString();

		System.out.println("SaveWebhookStatus" + SaveWebhookStatus);

		int statusCode = response.getStatusCode();
		Assert.assertTrue(SaveWebhookStatus.contains("status"), "success");
		Assert.assertEquals(SaveWebhookStatus.contains("success"), true);
		Assert.assertEquals(statusCode, 200);

	}

	@Test(priority = 2)
	public void unSubscribeWebhook() {

		RestAssured.baseURI = p.getProperty("baseURI") + "/PlatformService/admin/webhook/updateWebhookStatus";
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header("XSRF-TOKEN", xsrfToken));
		httpRequest.cookies("JSESSIONID", jSessionID, "grafanaOrg", "1", "grafanaRole", "Admin", "XSRF-TOKEN",
				xsrfToken);
		httpRequest.header("Authorization", webhookTestData.authorization);

		JsonObject requestParam = new JsonObject();

		requestParam.addProperty("webhookName", p.getProperty("webhookName"));
		requestParam.addProperty("statussubscribe", p.getProperty("statusUnsubscribe"));

		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParam);

		// Response Object
		Response response = httpRequest.request(Method.POST, "/");

		String unsubscribeResponse = response.getBody().asString();
		System.out.println("FailureResponse" + unsubscribeResponse);

		// Statuscode Validation
		int FailureStatusCode = response.getStatusCode();
		Assert.assertEquals(FailureStatusCode, 200);
		Assert.assertTrue(unsubscribeResponse.contains("status"), "success");
		Assert.assertEquals(unsubscribeResponse.contains("success"), true);

	}
}
