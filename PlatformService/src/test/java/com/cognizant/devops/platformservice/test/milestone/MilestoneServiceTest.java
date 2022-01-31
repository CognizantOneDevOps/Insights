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
import com.cognizant.devops.platformcommons.core.enums.MilestoneEnum;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.outcome.InsightsTools;
import com.cognizant.devops.platformdal.outcome.OutComeConfigDAL;
import com.cognizant.devops.platformservice.milestone.controller.MileStoneController;
import com.cognizant.devops.platformservice.milestone.service.MileStoneServiceImpl;
import com.cognizant.devops.platformservice.outcome.controller.OutComeController;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class MilestoneServiceTest extends MilestoneOutcomeTestData {

	private static final Logger log = LogManager.getLogger(MilestoneServiceTest.class);

	MileStoneServiceImpl milestoneConfigServiceImpl = new MileStoneServiceImpl();
	MileStoneController milestoneConfigController = new MileStoneController();
	OutComeController outcomeConfigController = new OutComeController();
	
	OutcomeServiceTest outcometest = new OutcomeServiceTest();
	
	List<JsonObject> filterOutcomeList = new ArrayList<>();
	List<JsonObject> filterMilestoneList = new ArrayList<>();
	OutComeConfigDAL outComeConfigDAL = new OutComeConfigDAL();


	String host = null;
	Gson gson = new Gson();

	@BeforeClass
		public void prepareData() throws InsightsCustomException {
		try {
			ApplicationConfigCache.loadConfigCache();
			//prepareRequestData();
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
				filterOutcomeList = outcomeList.stream().filter(outcomeJson -> outcomeJson.get("outcomeName").getAsString().equalsIgnoreCase(outcomeNameString)).collect(Collectors.toList());
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
	public void testSaveMilestoneDefinitionRecord() throws InsightsCustomException {
		try {
			for (JsonObject recordForSave : filterOutcomeList) {
				saveMilestoneJson = saveMilestoneJson.replace("Outcommmme", recordForSave.get("id").getAsString());
			}
			
			JsonObject saveMilestoneConfigjson = milestoneConfigController.saveMileStoneConfig(saveMilestoneJson);
			Assert.assertNotNull(saveMilestoneConfigjson);
			if (saveMilestoneConfigjson.has(MilestoneConstants.STATUS) && saveMilestoneConfigjson.get(MilestoneConstants.STATUS).getAsString().equalsIgnoreCase(PlatformServiceConstants.SUCCESS)) {
				JsonObject MilestoneJsonList = milestoneConfigController.fetchMileStoneConfig();
				List<JsonObject> milestoneList = gson.fromJson(MilestoneJsonList.get("data"), new TypeToken<List<JsonObject>>(){}.getType());
				filterMilestoneList = milestoneList.stream().filter(outcomeJson -> outcomeJson.get("mileStoneName").getAsString().equalsIgnoreCase(milestoneNameString)).collect(Collectors.toList());
				Assert.assertTrue(milestoneList.stream().anyMatch(MilestoneJson -> MilestoneJson.get("mileStoneName").getAsString().equalsIgnoreCase(milestoneNameString)));
				Assert.assertTrue(saveMilestoneConfigjson.get("status").getAsString().equalsIgnoreCase(PlatformServiceConstants.SUCCESS));		
			}else {
				Assert.fail(" Save Milestone configure has issue  ");
			}
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		} catch (Exception e) {
				log.error(e);
				Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 3)
	public void testGetAllMilestoneList() throws InsightsCustomException {
		try {
			JsonObject MilestoneJsonList = milestoneConfigController.fetchMileStoneConfig();
			Assert.assertNotNull(MilestoneJsonList);
			if (MilestoneJsonList.has(MilestoneConstants.STATUS) && MilestoneJsonList.get(MilestoneConstants.STATUS).getAsString().equalsIgnoreCase(PlatformServiceConstants.SUCCESS)) {
				List<JsonObject> milestoneList = gson.fromJson(MilestoneJsonList.get("data"), new TypeToken<List<JsonObject>>(){}.getType());
				log.debug(" getServerConfigStatus retun  {} ",milestoneList.size());
				Assert.assertTrue(milestoneList.size() > 0);		
			}else {
				Assert.fail(" No Milestone configure in system ");
			}
			
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	/*
	@Test(priority = 3)
	public void testEditMilestoneRecord() throws InsightsCustomException {
		try {
			for (JsonObject recordForSave : filterOutcomeList) {
				editMilestoneJson =editMilestoneJson.replace("iiddee", recordForSave.get("id").getAsString()).replace("Techtype","Business");
			}
			JsonObject editMilestoneConfigjson = milestoneConfigController.updateMilestoneConfig(editMilestoneJson);
			Assert.assertNotNull(editMilestoneConfigjson);
			if (editMilestoneConfigjson.has(MilestoneConstants.STATUS) && editMilestoneConfigjson.get(MilestoneConstants.STATUS).getAsString().equalsIgnoreCase(PlatformServiceConstants.SUCCESS)) {
				JsonObject MilestoneJsonList = milestoneConfigController.getAllActiveMilestone();
				List<JsonObject> MilestoneList = gson.fromJson(MilestoneJsonList.get("data"), new TypeToken<List<JsonObject>>(){}.getType());
				Assert.assertTrue(MilestoneList.stream().anyMatch(MilestoneJson -> MilestoneJson.get("MilestoneType").getAsString().equalsIgnoreCase("Business")));
				Assert.assertTrue(editMilestoneConfigjson.get("status").getAsString().equalsIgnoreCase(PlatformServiceConstants.SUCCESS));		
			}else {
				Assert.fail(" Save Milestone configure has issue  ");
			}
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		} catch (Exception e) {
				Assert.fail(e.getMessage());
		}
	}
	*/
	
	@Test(priority = 5)
	public void testStatusupdateMilestoneRecord() throws InsightsCustomException {
		try {
			for (JsonObject recordForSave : filterMilestoneList) {
				statusUpdateMilstone =statusUpdateMilstone.replace("iiddee", recordForSave.get("id").getAsString()).replace("activee", "false");
			}
			JsonObject editMilestoneConfigjson = milestoneConfigController.restartMileStoneConfig(statusUpdateMilstone);
			Assert.assertNotNull(editMilestoneConfigjson);
			if (editMilestoneConfigjson.has(MilestoneConstants.STATUS) && editMilestoneConfigjson.get(MilestoneConstants.STATUS).getAsString().equalsIgnoreCase(PlatformServiceConstants.SUCCESS)) {
				JsonObject MilestoneJsonList = milestoneConfigController.fetchMileStoneConfig();
				List<JsonObject> MilestoneList = gson.fromJson(MilestoneJsonList.get("data"), new TypeToken<List<JsonObject>>(){}.getType());
				Assert.assertTrue(MilestoneList.stream().anyMatch(MilestoneJson -> MilestoneJson.get("status").getAsString().equalsIgnoreCase(MilestoneEnum.MilestoneStatus.RESTART.name())));
				Assert.assertTrue(editMilestoneConfigjson.get("status").getAsString().equalsIgnoreCase(PlatformServiceConstants.SUCCESS));		
			}else {
				Assert.fail(" Save Milestone configure has issue  ");
			}
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 6)
	public void fetchOutcomeConfig() throws InsightsCustomException {
		try {
			JsonObject milestoneOutcomeJsonToolList = milestoneConfigController.fetchOutcomeTools();
			Assert.assertNotNull(milestoneOutcomeJsonToolList);
			if (milestoneOutcomeJsonToolList.has(MilestoneConstants.STATUS) && milestoneOutcomeJsonToolList.get(MilestoneConstants.STATUS).getAsString().equalsIgnoreCase(PlatformServiceConstants.SUCCESS)) {
				List<JsonObject> MilestoneList = gson.fromJson(milestoneOutcomeJsonToolList.get("data"), new TypeToken<List<JsonObject>>(){}.getType());
				log.debug(" getServerConfigStatus retun  {} ",MilestoneList.size());
				Assert.assertTrue(MilestoneList.size() > 0);		
			}else {
				Assert.fail(" No Milestone configure in system ");
			}
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 7)
	public void testDeleteMilestoneRecord() throws InsightsCustomException {
		log.debug(" filterList {} ",filterMilestoneList);
		
		for (JsonObject recordForDelete : filterMilestoneList) {
			milestoneConfigController.deleteMileStoneConfig(recordForDelete.get("id").getAsInt());
		}
		for (JsonObject recordForDelete : filterOutcomeList) {
			outcomeConfigController.deleteOutcomeConfig(recordForDelete.get("id").getAsInt());
		}
	}
	 

	@AfterClass
	public void cleanUp() throws InsightsCustomException {
		
	}
}
