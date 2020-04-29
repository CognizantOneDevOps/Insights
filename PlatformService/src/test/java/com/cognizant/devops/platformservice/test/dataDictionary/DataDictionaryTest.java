package com.cognizant.devops.platformservice.test.dataDictionary;

import java.util.List;

import org.junit.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfig;
import com.cognizant.devops.platformservice.agentmanagement.service.AgentConfigTO;
import com.cognizant.devops.platformservice.agentmanagement.service.AgentManagementServiceImpl;
import com.cognizant.devops.platformservice.rest.datadictionary.service.DataDictionaryServiceImpl;
import com.cognizant.devops.platformservice.webhook.service.WebHookServiceImpl;
import com.google.gson.JsonObject;

public class DataDictionaryTest {
	public static final DataDictionaryTestData dataDictionaryTestData = new DataDictionaryTestData();
	public static final DataDictionaryServiceImpl dataDictionaryImpl = new DataDictionaryServiceImpl();

	@BeforeMethod
	public void beforeMethod() throws InsightsCustomException {
		WebHookServiceImpl webhookServiceImp = new WebHookServiceImpl();
		AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
		List<WebHookConfig> webhookConfigList = webhookServiceImp.getRegisteredWebHooks();
		if (webhookConfigList.isEmpty()) {
			Boolean webhookcheck = webhookServiceImp
					.saveWebHookConfiguration(dataDictionaryTestData.registeredWebhookJson);
		}

		List<AgentConfigTO> registeredAgents = agentServiceImpl.getRegisteredAgents();
		if (registeredAgents.isEmpty())
		{
			String configJson = agentServiceImpl.getToolRawConfigFile("v5.2", "git");
			ApplicationConfigProvider.getInstance().getAgentDetails().setOnlineRegistration(false);			
			String response = agentServiceImpl.registerAgent(dataDictionaryTestData.toolName, 
					dataDictionaryTestData.agentVersion, dataDictionaryTestData.osversion, 
					dataDictionaryTestData.configDetails, dataDictionaryTestData.trackingDetails, false);

		}
	}

	@Test(priority = 1)
	public void getToolsAndCategoriesTest() throws InsightsCustomException {
		JsonObject response = dataDictionaryImpl.getToolsAndCategories();
		int sizeOfresponse = response.get("data").getAsJsonArray().size();
		Assert.assertTrue(sizeOfresponse >= 0);

	}

	@Test(priority = 2)
	public void getToolPropertiesTest() throws InsightsCustomException {
		JsonObject response = dataDictionaryImpl.getToolProperties(dataDictionaryTestData.destLabel,
				dataDictionaryTestData.destCat);
		int sizeOfresponse = response.get("data").getAsJsonArray().size();
		Assert.assertTrue(sizeOfresponse >= 0);
	}

	@Test(priority = 3)
	public void getToolPropertiesTestWithoutData() throws InsightsCustomException {
		JsonObject response = dataDictionaryImpl.getToolProperties(dataDictionaryTestData.emptylabel, "NO_CATEGORY");
		Assert.assertEquals("No Data found.", response.get("message").getAsString());

	}

	@Test(priority = 4)
	public void getToolsRelationshipAndPropertiesTest() throws InsightsCustomException {
		JsonObject response = dataDictionaryImpl.getToolsRelationshipAndProperties(dataDictionaryTestData.sourcelabel,
				dataDictionaryTestData.sourceCat, dataDictionaryTestData.destLabel, dataDictionaryTestData.destCat);
		int sizeOfresponse = response.get("data").getAsJsonArray().size();
		Assert.assertTrue(sizeOfresponse >= 0);

	}

}
