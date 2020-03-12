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

import java.util.List;

import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfig;
import com.cognizant.devops.platformservice.webhook.service.WebHookServiceImpl;
import com.google.gson.JsonArray;

@Test
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class WebhookServiceTest {
	public static final WebHookServiceImpl webhookServiceImp = new WebHookServiceImpl();
	public static final WebhookServiceTestData webhookTestData = new WebhookServiceTestData();


	@Test(priority = 1)
	public void testsaveWebHookConfiguration() throws InsightsCustomException {
		Boolean webhookcheck = webhookServiceImp.saveWebHookConfiguration(webhookTestData.webhookname,
				webhookTestData.toolName,
				webhookTestData.labelDisplay, webhookTestData.dataformat, webhookTestData.mqchannel,
				webhookTestData.subscribestatus, webhookTestData.responseTemplate,
				webhookTestData.derivedOperationsArray);
		Boolean expectedOutcome = true;
		Assert.assertEquals(webhookcheck, expectedOutcome);
	}

	@Test(priority = 2, expectedExceptions = InsightsCustomException.class)
	public void testsaveWebHookConfigurationException() throws InsightsCustomException {
		Boolean webhookcheck = webhookServiceImp.saveWebHookConfiguration(webhookTestData.webhookname,
				webhookTestData.toolName, webhookTestData.labelDisplay, webhookTestData.dataformat,
				webhookTestData.mqchannel, webhookTestData.subscribestatus, webhookTestData.responseTemplate,
				webhookTestData.derivedOperationsArray);
		Boolean expectedOutcome = true;
		Assert.assertEquals(webhookcheck, expectedOutcome);
	}

	@Test(priority = 3)
	public void testgetRegisteredWebHooks() throws InsightsCustomException {

		List<WebHookConfig> webhookConfigList = webhookServiceImp.getRegisteredWebHooks();
		for (WebHookConfig webHookConfig : webhookConfigList) {
			if (webHookConfig.getWebHookName() == webhookTestData.webhookname) {
				webhookTestData.setupdateWebhook(webHookConfig);
			}
		}
		Assert.assertFalse(webhookConfigList.isEmpty());
	}

	@Test(priority = 4)
	public void testupdateWebHookConfiguration() throws InsightsCustomException {
		JsonArray array = new JsonArray();
		if (webhookTestData.updateWebhook != null) {
			array = webhookTestData.getupdateWebhookDerivedOperationsArray();
		}

		Boolean webhookcheck = webhookServiceImp.updateWebHook(webhookTestData.webhookname, webhookTestData.toolName,
				webhookTestData.labelNewDisplay, webhookTestData.dataformat, webhookTestData.mqchannel,
				webhookTestData.subscribestatus, webhookTestData.responseTemplate,
				array);
		Assert.assertTrue(webhookcheck);
	}

	@Test(priority = 5)
	public void updateWebhookStatus() throws InsightsCustomException {
		String expectedOutCome = PlatformServiceConstants.SUCCESS;
		String response = webhookServiceImp.updateWebhookStatus(webhookTestData.getWebhookStatus());
		Assert.assertEquals(expectedOutCome, response);

	}

	@Test(priority = 6)
	public void testUninstallWebhook() throws InsightsCustomException {
		String expectedOutCome = PlatformServiceConstants.SUCCESS;
		String response = webhookServiceImp.uninstallWebhook(webhookTestData.webhookname);
		Assert.assertEquals(expectedOutCome, response);

	}

	@Test(priority = 7, expectedExceptions = InsightsCustomException.class)
	public void testUninstallWebhookForException() throws InsightsCustomException {
		String expectedOutCome = "No entity found for query";
		String response = webhookServiceImp.uninstallWebhook("12345fghj");
		Assert.assertEquals(expectedOutCome, response);
	}

}
