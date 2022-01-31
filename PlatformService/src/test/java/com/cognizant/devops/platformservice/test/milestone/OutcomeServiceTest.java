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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.constants.MilestoneConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.outcome.InsightsTools;
import com.cognizant.devops.platformdal.outcome.OutComeConfigDAL;
import com.cognizant.devops.platformservice.milestone.service.MileStoneServiceImpl;
import com.cognizant.devops.platformservice.outcome.controller.OutComeController;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class OutcomeServiceTest extends MilestoneOutcomeTestData {

	private static final Logger log = LogManager.getLogger(OutcomeServiceTest.class);

	MileStoneServiceImpl milestoneConfigServiceImpl = new MileStoneServiceImpl();
	OutComeController outcomeConfigController = new OutComeController();
	List<JsonObject> filterList = new ArrayList<>();
	OutComeConfigDAL outComeConfigDAL = new OutComeConfigDAL();

	String host = null;
	Gson gson = new Gson();

	@BeforeClass
		public void prepareData() throws InsightsCustomException {
		try {
			ApplicationConfigCache.loadConfigCache();
		} catch (Exception e) {
			log.error("message", e);
		}

	}
	
	@Test(priority = 1)
	public void testSaveOutcomeDefinitionRecord() throws InsightsCustomException {
		try {
			InsightsTools insightsMilestoneTools = outComeConfigDAL.getOutComeByToolName(toolName);
			if(insightsMilestoneTools == null) {
				prepareRequestData();
				insightsMilestoneTools = outComeConfigDAL.getOutComeByToolName(toolName);
			}
			int toolId = insightsMilestoneTools.getId();
			saveOutcomeJson = saveOutcomeJson.replace("toolNameeee1", String.valueOf(toolId));
			
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
	
	@Test(priority = 3)
	public void testEditOutcomeRecord() throws InsightsCustomException {
		try {
			for (JsonObject recordForSave : filterList) {
				editOutcomeJson =editOutcomeJson.replace("iiddee", recordForSave.get("id").getAsString()).replace("Techtype","Business");
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
	
	@Test(priority = 4)
	public void testStatusupdateOutcomeRecord() throws InsightsCustomException {
		try {
			for (JsonObject recordForSave : filterList) {
				statusUpdate =statusUpdate.replace("iiddee", recordForSave.get("id").getAsString()).replace("activee", "false");
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
	
	@Test(priority = 5)
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
	
	@Test(priority = 6)
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
	 

	@AfterClass
	public void cleanUp() throws InsightsCustomException {
		
	}
}
