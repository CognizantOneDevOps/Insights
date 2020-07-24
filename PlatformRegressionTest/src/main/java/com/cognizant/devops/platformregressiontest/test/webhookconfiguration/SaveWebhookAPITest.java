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
import com.cognizant.devops.platformregressiontest.test.common.CommonUtils;
import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class SaveWebhookAPITest extends WebhookTestData {

	private static final Logger log = LogManager.getLogger(SaveWebhookAPITest.class);
	String jSessionID;
	String xsrfToken;

	@BeforeMethod
	public void onInit() throws InterruptedException, IOException {

		jSessionID = CommonUtils.getJsessionId();
		xsrfToken = CommonUtils.getXSRFToken(jSessionID);
	}

	@Test(priority = 1)
	public void loadWebhookConfig() {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("loadWebhookCOnfiguration");
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(ConfigOptionsTest.AUTH_HEADER_KEY, CommonUtils.getProperty("authorization"));
		httpRequest.header(ConfigOptionsTest.CONTENT_HEADER_KEY, ConfigOptionsTest.CONTENT_TYPE_VALUE);
		Response response = httpRequest.request(Method.GET, "/");

		String webhookConfiguration = response.getBody().asString();

		int statusCode = response.getStatusCode();
		Assert.assertEquals(statusCode, 200);
		Assert.assertTrue(webhookConfiguration.contains("status"), "success");

	}

	@Test(priority = 2, dataProvider = "webhookdataprovider")
	public void saveWebHookConfiguration(String toolName, String labelDisplay, String webhookName, String dataformat,
			String mqchannel, String responseTemplate, String statussubscribe, String timeField, String epochTime,
			String timeFormat, String wid, String operationName, String dynamicTemplate, String isUpdateRequired,
			String fieldUsedForUpdate) {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("saveWebhook");
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header(ConfigOptionsTest.CSRF_NAME_KEY, xsrfToken));
		httpRequest.cookies(ConfigOptionsTest.SESSION_ID_KEY, jSessionID, ConfigOptionsTest.GRAFANA_COOKIES_ORG,
				CommonUtils.getProperty("grafanaOrg"), ConfigOptionsTest.GRAFANA_COOKIES_ROLE,
				CommonUtils.getProperty("grafanaRole"), ConfigOptionsTest.CSRF_NAME_KEY, xsrfToken);
		httpRequest.header(ConfigOptionsTest.AUTH_HEADER_KEY, CommonUtils.getProperty("authorization"));

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

		httpRequest.header(ConfigOptionsTest.CONTENT_HEADER_KEY, ConfigOptionsTest.CONTENT_TYPE_VALUE);
		httpRequest.body(requestParam);

		Response response = httpRequest.request(Method.POST, "/");

		String saveWebhook = response.getBody().asString();

		log.debug("SaveWebhook {}", saveWebhook);

		int statusCode = response.getStatusCode();
		Assert.assertTrue(saveWebhook.contains("status"), "success");
		Assert.assertEquals(saveWebhook.contains("success"), true);
		Assert.assertEquals(statusCode, 200);

	}

	@Test(priority = 3)
	public void saveWebhookFail() {

		RestAssured.baseURI = CommonUtils.getProperty("baseURI") + CommonUtils.getProperty("saveWebhook");

		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.header(new Header(ConfigOptionsTest.CSRF_NAME_KEY, xsrfToken));
		httpRequest.cookies(ConfigOptionsTest.SESSION_ID_KEY, jSessionID, ConfigOptionsTest.GRAFANA_COOKIES_ORG,
				CommonUtils.getProperty("grafanaOrg"), ConfigOptionsTest.GRAFANA_COOKIES_ROLE,
				CommonUtils.getProperty("grafanaRole"), ConfigOptionsTest.CSRF_NAME_KEY, xsrfToken);

		JsonObject requestParam = new JsonObject();
		httpRequest.header(ConfigOptionsTest.CONTENT_HEADER_KEY, ConfigOptionsTest.CONTENT_TYPE_VALUE);
		httpRequest.body(requestParam);

		Response response = httpRequest.request(Method.POST, "/");

		String responseWebhook = response.getBody().asString();
		log.debug("responseWebhook {}", responseWebhook);

		int failureStatusCode = response.getStatusCode();
		Assert.assertEquals(failureStatusCode, 400);
		Assert.assertTrue(responseWebhook.contains("status"), "failure");
		Assert.assertEquals(responseWebhook.contains("failure"), true);

	}
}
