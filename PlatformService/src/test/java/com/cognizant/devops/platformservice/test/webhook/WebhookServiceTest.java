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
package com.cognizant.devops.platformservice.test.webhook;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.cognizant.devops.platformauditing.util.PdfSignUtil;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.webhook.service.IWebHook;
import com.cognizant.devops.platformservice.test.testngInitializer.TestngInitializerTest;
import com.cognizant.devops.platformservice.webhook.controller.WebHookController;
import com.google.gson.JsonObject;

@Test
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class WebhookServiceTest extends WebhookServiceTestData{
	private static final Logger log = LogManager.getLogger(WebhookServiceTest.class.getName());

	@Autowired
	WebHookController webhookController;
	
	@Autowired
	IWebHook webhookServiceImp;
	
    JsonObject testData = new JsonObject();
	
	@BeforeClass
	public void prepareData() throws InsightsCustomException {
		try {
		    String path = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator + TestngInitializerTest.TESTNG_TESTDATA + File.separator
                    + TestngInitializerTest.TESTNG_PLATFORMSERVICE + File.separator + "WebhookService.json";
			testData = JsonUtils.getJsonData(path).getAsJsonObject();
			
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	@Test(priority = 1)
	public void testsaveWebHookConfigurationIncorrectResponseTemp() throws InsightsCustomException{
		try {
			JsonObject responseIncorrectRT = webhookController.saveWebhook(testData.get("webhookJsonIncorrectRT").toString());
			String actual = responseIncorrectRT.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.FAILURE);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 2)
	public void testsaveWebHookConfiguration() throws InsightsCustomException{
		try {
			JsonObject response = webhookController.saveWebhook(webhookJson);
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 3)
	public void testsaveWebHookConfigurationException() throws InsightsCustomException{
		try {
			JsonObject response = webhookController.saveWebhook(webhookJson);
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.FAILURE);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 4)
	public void testsaveWebHookConfigurationValidationError() throws InsightsCustomException{
			JsonObject response = webhookController.saveWebhook("&amp;"+testData.get("webhookJsonValidation").toString());
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.FAILURE);
	}
	
	@Test(priority = 5)
	public void testsaveWebHookConfigurationEmptyResponseTemp() throws InsightsCustomException{
			JsonObject responseEmptyRT = webhookController.saveWebhook(testData.get("webhookJsonEmptyRT").toString());
			String actual = responseEmptyRT.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.FAILURE);
	}
	
	@Test(priority = 6)
	public void testsaveWebHookConfigurationRTMaxSizeException() throws InsightsCustomException{
			JsonObject responseEmptyRT = webhookController.saveWebhook(testData.get("webhookJsonMaxRTSize").toString());
			String actual = responseEmptyRT.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.FAILURE);
	}
	
	@Test(priority = 7)
	public void testgetRegisteredWebHooks() throws InsightsCustomException{
		try {
			JsonObject response = webhookController.getRegisteredWebHooks();
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
			} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 8)
	public void testupdateWebHookConfiguration() throws InsightsCustomException{
		try {
			JsonObject response = webhookController.updateWebhook(webhookUpdateJson);
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
			} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 9)
	public void testupdateWebHookConfigurationWrongData() throws InsightsCustomException{
			JsonObject response = webhookController.updateWebhook(testData.get("webhookUpdateWrongData").toString());
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.FAILURE);
	}
	
	@Test(priority = 10)
	public void testupdateWebHookConfigurationInvalidJsonKeys() throws InsightsCustomException{
			JsonObject response = webhookController.updateWebhook(testData.get("webhookUpdateInvalidJsonKeys").toString());
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.FAILURE);
	}
	
	@Test(priority = 11)
	public void testupdateWebHookConfigurationValidationError() throws InsightsCustomException{
			JsonObject response = webhookController.updateWebhook("&amp;"+testData.get("webhookUpdateJsonValidation").toString());
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.FAILURE);
	}

	@Test(priority = 12)
	public void updateWebhookStatus() throws InsightsCustomException{
		try {
			JsonObject response = webhookController.updateWebhookStatus(testData.get("webhookStatus").toString());
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
			} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 13)
	public void updateWebhookStatusValidationError() throws InsightsCustomException{
			JsonObject response = webhookController.updateWebhookStatus("&amp;"+testData.get("webhookStatusValidation").toString());
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.FAILURE);
	}
	
	@Test(priority = 14)
	public void updateWebhookStatusInvalidInput() throws InsightsCustomException{
			JsonObject response = webhookController.updateWebhookStatus(testData.get("webhookStatusInvalidInput").toString());
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.FAILURE);
	}
	
	@Test(priority = 15)
	public void testUninstallWebhook() throws InsightsCustomException{
		try {
			JsonObject response = webhookController.uninstallWebhook(webhookname);
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
			} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 16)
	public void testUninstallWebhookException() throws InsightsCustomException{
		try {
			JsonObject response = webhookController.uninstallWebhook("12345fghj");
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.FAILURE);
			} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 17)
	public void testsaveWebHookConfigurationEmptyResposneAndDynamic() throws InsightsCustomException{
		try {
			JsonObject response = webhookController.saveWebhook(testData.get("webhookEmptyDT").toString());
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
			JsonObject uninstallResponse = webhookController.uninstallWebhook("git_demo");
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

}
