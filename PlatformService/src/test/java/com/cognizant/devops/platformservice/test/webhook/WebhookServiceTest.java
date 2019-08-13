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
		Assert.assertNotNull(webhookTestData.webhookname);
		Assert.assertNotNull(webhookTestData.toolName);
		Assert.assertNotNull(webhookTestData.labelDisplay);
		Assert.assertNotNull(webhookTestData.mqchannel);
		Assert.assertTrue(webhookTestData.subscribestatus);
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
		Assert.assertNotNull(webhookTestData.webhookname);
		Assert.assertNotNull(webhookTestData.toolName);
		Assert.assertNotNull(webhookTestData.labelDisplay);
		Assert.assertNotNull(webhookTestData.mqchannel);
		Assert.assertTrue(webhookTestData.subscribestatus);
		Assert.assertEquals(webhookcheck, expectedOutcome);
	}
	
}
