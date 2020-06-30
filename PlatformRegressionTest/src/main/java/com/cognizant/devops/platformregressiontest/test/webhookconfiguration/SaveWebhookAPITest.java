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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class SaveWebhookAPITest extends WebhookTestData {

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
	public void loadWebhookConfig() {

		RestAssured.baseURI = p.getProperty("baseURI") + "/PlatformService/admin/webhook/loadwebhookConfiguration";
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header("Authorization", webhookTestData.authorization);
		httpRequest.header("Content-Type", "application/json");
		Response response = httpRequest.request(Method.GET, "/");

		String webhookConfiguration = response.getBody().asString();

		int statusCode = response.getStatusCode();
		Assert.assertEquals(statusCode, 200);
		Assert.assertTrue(webhookConfiguration.contains("status"), "success");

	}

	@Test(priority = 2, dataProvider = "webhookdataprovider")
	public void saveWebhook(String toolName, String labelDisplay, String webhookName, String dataformat,
			String mqchannel, String responseTemplate, String statussubscribe, String timeField, String epochTime,
			String timeFormat, String wid, String operationName, String dynamicTemplate, String isUpdateRequired,
			String fieldUsedForUpdate) {

		RestAssured.baseURI = p.getProperty("baseURI") + "/PlatformService/admin/webhook/saveWebhook";
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header("XSRF-TOKEN", xsrfToken));
		httpRequest.cookies("JSESSIONID", jSessionID, "grafanaOrg", "1", "grafanaRole", "Admin", "XSRF-TOKEN",
				xsrfToken);
		httpRequest.header("Authorization", webhookTestData.authorization);

		// Request payload sending along with post request
		JsonObject requestParam = new JsonObject();

		requestParam.addProperty("toolName", toolName);
		requestParam.addProperty("labelDisplay", labelDisplay);
		requestParam.addProperty("webhookName", webhookName);
		requestParam.addProperty("dataformat", dataformat);
		requestParam.addProperty("mqchannel", mqchannel);
		requestParam.addProperty("responseTemplate", responseTemplate);
		requestParam.addProperty("statussubscribe", statussubscribe);

		JsonObject operationFields = new JsonObject();
		operationFields.addProperty("timeField", timeField);
		operationFields.addProperty("epochTime", epochTime);
		operationFields.addProperty("timeFormat", timeFormat);

		JsonObject webhookFields = new JsonObject();
		webhookFields.addProperty("wid", wid);
		webhookFields.addProperty("operationName", operationName);
		webhookFields.addProperty("webhookName", webhookName);

		webhookFields.add("operationFields", operationFields);
		JsonArray derivedOperations = new JsonArray();
		derivedOperations.add(webhookFields);

		requestParam.add("derivedOperations", derivedOperations);
		requestParam.addProperty("dynamicTemplate", dynamicTemplate);
		requestParam.addProperty("isUpdateRequired", isUpdateRequired);
		requestParam.addProperty("fieldUsedForUpdate", fieldUsedForUpdate);

		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParam);

		Response response = httpRequest.request(Method.POST, "/");

		String SaveWebhook = response.getBody().asString();

		System.out.println("SaveWebhook" + SaveWebhook);

		int statusCode = response.getStatusCode();
		Assert.assertTrue(SaveWebhook.contains("status"), "success");
		Assert.assertEquals(SaveWebhook.contains("success"), true);
		Assert.assertEquals(statusCode, 200);

	}

	@Test(priority = 3)
	public void saveWebhookFail() {

		RestAssured.baseURI = p.getProperty("baseURI") + "/PlatformService/admin/webhook/saveWebhook";

		RequestSpecification httpRequest = RestAssured.given();
		httpRequest.header(new Header("XSRF-TOKEN", xsrfToken));
		httpRequest.cookies("JSESSIONID", jSessionID, "grafanaOrg", "1", "grafanaRole", "Admin", "XSRF-TOKEN",
				xsrfToken);
		//httpRequest.header("Authorization", webhookTestData.authorization);

		JsonObject requestParam = new JsonObject();

		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParam);

		// Response Object
		Response response = httpRequest.request(Method.POST, "/");

		String responseWebhook = response.getBody().asString();
		System.out.println("responseWebhook" + responseWebhook);

		// Statuscode Validation
		int FailureStatusCode = response.getStatusCode();
		Assert.assertEquals(FailureStatusCode, 400);
		Assert.assertTrue(responseWebhook.contains("status"), "failure");
		Assert.assertEquals(responseWebhook.contains("failure"), true);

	}
}
