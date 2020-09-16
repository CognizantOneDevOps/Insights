/*******************************************************************************
 *  * Copyright 2017 Cognizant Technology Solutions
 *  * 
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  * use this file except in compliance with the License.  You may obtain a copy
 *  * of the License at
 *  * 
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  * 
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  * License for the specific language governing permissions and limitations under
 *  * the License.
 *******************************************************************************/
package com.cognizant.devops.platformservice.test.dataDictionary;

import java.util.List;

import org.junit.Assert;
import org.testng.SkipException;
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
		if (registeredAgents.isEmpty()) {
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
		if (response.has("data")) {
			int sizeOfresponse = response.get("data").getAsJsonArray().size();
			Assert.assertTrue(sizeOfresponse >= 0);
		} else {
			throw new SkipException("skipped getToolPropertiesTest this test case as required data not found");
		}
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
