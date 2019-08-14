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

import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.webhook.service.WebHookService;

@Test
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class WebhookServiceTest extends WebHookService{
	public static final WebHookService webhookServiceImp =
			new WebHookService();
	public static final WebhookServiceTestData webhookTestData = new WebhookServiceTestData();

	@Test(priority = 1, expectedExceptions = InsightsCustomException.class)
	public void testsaveWebHookConfiguration() throws InsightsCustomException {
		WebHookService webhookserviceImp = new WebHookService();
		Boolean webhookcheck = webhookserviceImp.saveWebHookConfiguration(webhookTestData.webhookname,webhookTestData.toolName,webhookTestData.labelDisplay, webhookTestData.dataformat, webhookTestData.mqchannel, webhookTestData.subscribestatus, webhookTestData.responseTemplate);
		Boolean expectedOutcome = true;
		Assert.assertEquals(webhookcheck, expectedOutcome);
	}
	
	@Test(priority = 2)
	public void testgetRegisteredWebHooks() throws InsightsCustomException {
		
		Assert.assertFalse(webhookServiceImp.getRegisteredWebHooks().isEmpty());		
	}
	@Test(priority = 3)
	public void testUninstallWebhook() throws InsightsCustomException{

		String expectedOutCome = "SUCCESS"; 
		String response = webhookServiceImp.uninstallWebhook(webhookTestData.webhookname);
		Assert.assertEquals(expectedOutCome, response);
		
	}
	@Test(priority = 4, expectedExceptions = InsightsCustomException.class)
	public void testUninstallWebhookForException() throws InsightsCustomException{

		String expectedOutCome =  "No entity found for query";
		String response = webhookServiceImp.uninstallWebhook(webhookTestData.webhookname);
		Assert.assertEquals(expectedOutCome, response);
	}
	@Test(priority = 5)
	public void testupdateWebHookConfiguration() throws InsightsCustomException {
		WebHookService webhookserviceImp = new WebHookService();
		Boolean webhookcheck = webhookserviceImp.updateWebHook(webhookTestData.webhookname,webhookTestData.toolName,webhookTestData.labelDisplay, webhookTestData.dataformat, webhookTestData.mqchannel, webhookTestData.subscribestatus, webhookTestData.responseTemplate);
		Boolean expectedOutcome = true;
		Assert.assertEquals(webhookcheck, expectedOutcome);
	}
	
}
