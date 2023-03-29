/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformservice.test.milestone;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.constants.MilestoneConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.outcome.OutComeConfigDAL;
import com.cognizant.devops.platformservice.milestone.service.MileStoneServiceImpl;
import com.cognizant.devops.platformservice.outcome.controller.OutComeController;
import com.cognizant.devops.platformservice.test.testngInitializer.TestngInitializerTest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@Test
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class OutcomeServiceTest extends MilestoneOutcomeTestData {

	private static final Logger log = LogManager.getLogger(OutcomeServiceTest.class);

	@Autowired
	MileStoneServiceImpl milestoneConfigServiceImpl;
	@Autowired
	OutComeController outcomeConfigController;
	List<JsonObject> filterList = new ArrayList<>();
	OutComeConfigDAL outComeConfigDAL = new OutComeConfigDAL();

	String host = null;
	Gson gson = new Gson();
    JsonObject testData = new JsonObject();
	
	@BeforeClass
	public void prepareData() throws InsightsCustomException {
		try {
		    String path = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator + TestngInitializerTest.TESTNG_TESTDATA + File.separator
                    + TestngInitializerTest.TESTNG_PLATFORMSERVICE + File.separator + "OutcomeService.json";
			testData = JsonUtils.getJsonData(path).getAsJsonObject();
			
		} catch (Exception e) {
			log.error("Error preparing data at OutcomeServiceTest record ", e);
		}
	}
	
	@Test(priority = 1)
	public void testSaveOutcomeDefinitionRecord() throws InsightsCustomException {
		try {
			GetInsightsMilestoneTools();
			int toolId = insightsMilestoneTools.getId();
			String saveOutcomeJson = testData.get("saveOutcomeJson").toString().replace("toolNameeee1", String.valueOf(toolId));
			
			JsonObject saveOutcomeConfigjson = outcomeConfigController.saveOutcomeConfig(saveOutcomeJson);
			Assert.assertNotNull(saveOutcomeConfigjson);
			if (saveOutcomeConfigjson.has(MilestoneConstants.STATUS) && saveOutcomeConfigjson.get(MilestoneConstants.STATUS).getAsString().equalsIgnoreCase(PlatformServiceConstants.SUCCESS)) {
				JsonObject outcomeJsonList = outcomeConfigController.getAllActiveOutcome();
				List<JsonObject> outcomeList = gson.fromJson(outcomeJsonList.get("data"), new TypeToken<List<JsonObject>>(){}.getType());
				Assert.assertTrue(outcomeList.stream().anyMatch(outcomeJson -> outcomeJson.get("outcomeName").getAsString().equalsIgnoreCase(outcomeNameString)));
				Assert.assertTrue(saveOutcomeConfigjson.get("status").getAsString().equalsIgnoreCase(PlatformServiceConstants.SUCCESS));		
			}else {
				Assert.fail(" Save outcome configure has issue  ");
			}
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		} catch (Exception e) {
				log.error(e);
				Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 2)
	public void testSaveOutcomeDefinitionDuplicate() throws InsightsCustomException{
		try {
			GetInsightsMilestoneTools();
			int toolId = insightsMilestoneTools.getId();
			String saveOutcomeJson = testData.get("saveOutcomeJson").toString().replace("toolNameeee1", String.valueOf(toolId));
			JsonObject response = outcomeConfigController.saveOutcomeConfig(saveOutcomeJson);
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(PlatformServiceConstants.FAILURE, actual);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	
	@Test(priority = 3)
	public void testGetAlloutcomeList() throws InsightsCustomException {
		try {
			JsonObject outcomeJsonList = outcomeConfigController.getAllActiveOutcome();
			Assert.assertNotNull(outcomeJsonList);
			if (outcomeJsonList.has(MilestoneConstants.STATUS) && outcomeJsonList.get(MilestoneConstants.STATUS).getAsString().equalsIgnoreCase(PlatformServiceConstants.SUCCESS)) {
				List<JsonObject> outcomeList = gson.fromJson(outcomeJsonList.get("data"), new TypeToken<List<JsonObject>>(){}.getType());
				filterList = outcomeList.stream().filter(outcomeJson -> outcomeJson.get("outcomeName").getAsString().equalsIgnoreCase(outcomeNameString)).collect(Collectors.toList());
				log.debug(" getServerConfigStatus retun  {} ",outcomeList.size());
				Assert.assertTrue(outcomeList.size() > 0);		
			}else {
				Assert.fail(" No Outcome configure in system ");
			}
			
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 4)
	public void testEditOutcomeConfigError() throws InsightsCustomException {
		try {
			for (JsonObject recordForSave : filterList) {
				String editOutcomeJson =testData.get("editOutcomeJson").toString().replace("iiddee", recordForSave.get("id").getAsString()).replace("Techtype","Business");
			}
			JsonObject response = outcomeConfigController.updateOutcomeConfig(testData.get("statusUpdateError").toString());
			Assert.assertEquals(PlatformServiceConstants.FAILURE, response.get("status").getAsString().replace("\"", ""));	
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 5)
	public void testEditOutcomeRecord() throws InsightsCustomException {
		try {
			String editOutcomeJson="";
			for (JsonObject recordForSave : filterList) {
				editOutcomeJson =testData.get("editOutcomeJson").toString().replace("iiddee", recordForSave.get("id").getAsString()).replace("Techtype","Business");
			}
			JsonObject editOutcomeConfigjson = outcomeConfigController.updateOutcomeConfig(editOutcomeJson);
			Assert.assertNotNull(editOutcomeConfigjson);
			if (editOutcomeConfigjson.has(MilestoneConstants.STATUS) && editOutcomeConfigjson.get(MilestoneConstants.STATUS).getAsString().equalsIgnoreCase(PlatformServiceConstants.SUCCESS)) {
				JsonObject outcomeJsonList = outcomeConfigController.getAllActiveOutcome();
				List<JsonObject> outcomeList = gson.fromJson(outcomeJsonList.get("data"), new TypeToken<List<JsonObject>>(){}.getType());
				Assert.assertTrue(outcomeList.stream().anyMatch(outcomeJson -> outcomeJson.get("outcomeType").getAsString().equalsIgnoreCase("Business")));
				Assert.assertTrue(editOutcomeConfigjson.get("status").getAsString().equalsIgnoreCase(PlatformServiceConstants.SUCCESS));		
			}else {
				Assert.fail(" Save outcome configure has issue  ");
			}
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		} catch (Exception e) {
				log.error(e);
				Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 6)
	public void testStatusUpdateOutcomeRecordError() throws InsightsCustomException {
		try {
			for (JsonObject recordForSave : filterList) {
				String statusUpdate =testData.get("statusUpdate").toString().replace("iiddee", recordForSave.get("id").getAsString()).replace("activee", "false");
			}
			JsonObject response = outcomeConfigController.updateOutcomeConfigStatus(testData.get("statusUpdateError").toString());
	        Assert.assertEquals(PlatformServiceConstants.FAILURE, response.get("status").getAsString().replace("\"", ""));	
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	@Test(priority = 7)
	public void testStatusUpdateOutcomeRecord() throws InsightsCustomException {
		try {
			String statusUpdate="";
			for (JsonObject recordForSave : filterList) {
				statusUpdate =testData.get("statusUpdate").toString().replace("iiddee", recordForSave.get("id").getAsString()).replace("activee", "false");
			}
			JsonObject editOutcomeConfigjson = outcomeConfigController.updateOutcomeConfigStatus(statusUpdate);
			Assert.assertNotNull(editOutcomeConfigjson);
			if (editOutcomeConfigjson.has(MilestoneConstants.STATUS) && editOutcomeConfigjson.get(MilestoneConstants.STATUS).getAsString().equalsIgnoreCase(PlatformServiceConstants.SUCCESS)) {
				JsonObject outcomeJsonList = outcomeConfigController.getAllActiveOutcome();
				List<JsonObject> outcomeList = gson.fromJson(outcomeJsonList.get("data"), new TypeToken<List<JsonObject>>(){}.getType());
				Assert.assertTrue(outcomeList.stream().anyMatch(outcomeJson -> outcomeJson.get("isActive").getAsString().equalsIgnoreCase("false")));
				Assert.assertTrue(editOutcomeConfigjson.get("status").getAsString().equalsIgnoreCase(PlatformServiceConstants.SUCCESS));		
			}else {
				Assert.fail(" Save outcome configure has issue  ");
			}
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 8)
	public void fetchMileStoneTools() throws InsightsCustomException {
		try {
			JsonObject outcomeJsonToolList = outcomeConfigController.fetchOutcomeTools();
			Assert.assertNotNull(outcomeJsonToolList);
			if (outcomeJsonToolList.has(MilestoneConstants.STATUS) && outcomeJsonToolList.get(MilestoneConstants.STATUS).getAsString().equalsIgnoreCase(PlatformServiceConstants.SUCCESS)) {
				List<JsonObject> outcomeList = gson.fromJson(outcomeJsonToolList.get("data"), new TypeToken<List<JsonObject>>(){}.getType());
				log.debug(" getServerConfigStatus retun  {} ",outcomeList.size());
				Assert.assertTrue(outcomeList.size() > 0);		
			}else {
				Assert.fail(" No outcome configure in system ");
			}
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 9)
	public void testDeleteOutcomeRecord() throws InsightsCustomException {
		log.debug(" filterList {} ",filterList);
		try {
			for (JsonObject recordForDelete : filterList) {
				outcomeConfigController.deleteOutcomeConfig(recordForDelete.get("id").getAsInt());
			}
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 10)
	public void testSaveOutcomeDefinitionNoConfig() throws InsightsCustomException{
		try {
			GetInsightsMilestoneTools();
			int toolId = insightsMilestoneTools.getId();
		    String saveOutcomeJson = testData.get("saveOutcomeJsonNoToolConfigJson").toString().replace("toolNameeee1", String.valueOf(toolId));
			JsonObject response = outcomeConfigController.saveOutcomeConfig(saveOutcomeJson);
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(PlatformServiceConstants.SUCCESS, actual);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
}
