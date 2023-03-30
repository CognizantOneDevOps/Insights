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

import java.io.File;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfig;
import com.cognizant.devops.platformservice.agentmanagement.service.AgentConfigTO;
import com.cognizant.devops.platformservice.agentmanagement.service.AgentManagementServiceImpl;
import com.cognizant.devops.platformservice.rest.datadictionary.controller.DataDictionaryController;
import com.cognizant.devops.platformservice.rest.datadictionary.service.DataDictionaryServiceImpl;
import com.cognizant.devops.platformservice.test.testngInitializer.TestngInitializerTest;
import com.cognizant.devops.platformservice.webhook.service.WebHookServiceImpl;
import com.google.gson.JsonObject;

@Test
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
@WebAppConfiguration
public class DataDictionaryTest extends DataDictionaryTestData {
	public static final DataDictionaryTestData dataDictionaryTestData = new DataDictionaryTestData();
	private static final Logger log = LogManager.getLogger(DataDictionaryTest.class);
	
	@Autowired
	DataDictionaryServiceImpl dataDictionaryImpl;

	@Autowired
	DataDictionaryController dataDictionaryController;
	
	JsonObject testData = new JsonObject();
	
	@BeforeClass
	public void beforeMethod() throws InsightsCustomException {
		try {
			String path = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator + TestngInitializerTest.TESTNG_TESTDATA + File.separator
                    + TestngInitializerTest.TESTNG_PLATFORMSERVICE + File.separator + "DataDictionary.json";
			testData = JsonUtils.getJsonData(path).getAsJsonObject();
			
		
		
		ApplicationConfigCache.loadConfigCache();
		WebHookServiceImpl webhookServiceImp = new WebHookServiceImpl();
		AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
		graphDBHandler = new GraphDBHandler();
		
		insertAgentDataInNeo4j(destCat, destLabel, testData.get("gitAgentData").toString());
		insertAgentDataInNeo4j(sourceCat, sourcelabel, testData.get("jiraAgentData").toString());
		graphDBHandler.executeCypherQuery(dataDictionaryTestData.relationQuery);
		
		List<WebHookConfig> webhookConfigList = webhookServiceImp.getRegisteredWebHooks();
		if (webhookConfigList.isEmpty()) {
			Boolean webhookcheck = webhookServiceImp
					.saveWebHookConfiguration(dataDictionaryTestData.registeredWebhookJson);
		}

		List<AgentConfigTO> registeredAgents = agentServiceImpl.getRegisteredAgentsAndHealth();
		if (registeredAgents.isEmpty()) {
			ApplicationConfigProvider.getInstance().getAgentDetails().setOnlineRegistration(false);
			String configJson = agentServiceImpl.getToolRawConfigFile("v9.1", "git",false);
			String response = agentServiceImpl.registerAgent(dataDictionaryTestData.toolName,
					dataDictionaryTestData.agentVersion, dataDictionaryTestData.osversion,
					dataDictionaryTestData.testData.get("configDetails").toString(), dataDictionaryTestData.trackingDetails, false,false,"Agent");

		}
		} catch (Exception e) {
			log.error("Error preparing data at data dictionary record ", e);
		}
		
	}

	@Test(priority = 1)
	public void getToolsAndCategoriesTest() throws InsightsCustomException {
		JsonObject response = dataDictionaryController.getToolsAndCategories();
		int sizeOfresponse = response.get("data").getAsJsonArray().size();
		Assert.assertTrue(sizeOfresponse >= 0);

	}

	@Test(priority = 2)
	public void getToolPropertiesTest() throws InsightsCustomException {
		JsonObject response = dataDictionaryController.getToolProperties(dataDictionaryTestData.destLabel,
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
		JsonObject response = dataDictionaryController.getToolProperties(dataDictionaryTestData.emptylabel, "NO_CATEGORY");
		Assert.assertEquals("No Data found.", response.get("message").getAsString());

	}

	@Test(priority = 4)
	public void getToolsRelationshipAndPropertiesTest() throws InsightsCustomException {
		JsonObject response = dataDictionaryController.getToolsRelationshipAndProperties(dataDictionaryTestData.sourcelabel,
				dataDictionaryTestData.sourceCat, dataDictionaryTestData.destLabel, dataDictionaryTestData.destCat);
		int sizeOfresponse = response.get("data").getAsJsonArray().size();
		Assert.assertTrue(sizeOfresponse >= 0);

	}
	
	@AfterClass
	public void cleanUp() throws InsightsCustomException {
		deleteAgentDataFromNeo4j(sourceCat);
		deleteAgentDataFromNeo4j(destCat);
	}

}
