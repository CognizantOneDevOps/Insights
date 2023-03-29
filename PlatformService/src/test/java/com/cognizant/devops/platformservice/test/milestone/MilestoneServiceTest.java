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
import com.cognizant.devops.platformcommons.core.enums.MilestoneEnum;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.milestone.controller.MileStoneController;
import com.cognizant.devops.platformservice.milestone.service.MileStoneServiceImpl;
import com.cognizant.devops.platformservice.outcome.controller.OutComeController;
import com.cognizant.devops.platformservice.test.testngInitializer.TestngInitializerTest;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.cognizant.devops.platformdal.outcome.InsightsTools;

@Test
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class MilestoneServiceTest extends MilestoneOutcomeTestData {

	private static final Logger log = LogManager.getLogger(MilestoneServiceTest.class);
	@Autowired
	MileStoneServiceImpl milestoneConfigServiceImpl;
	@Autowired
	MileStoneController milestoneConfigController;
	@Autowired
	OutComeController outcomeConfigController;
	OutcomeServiceTest outcometest= new OutcomeServiceTest();
	JsonObject testData = new JsonObject();
		
	@BeforeClass
	public void prepareData() throws InsightsCustomException {
		try {
			   String path = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator + TestngInitializerTest.TESTNG_TESTDATA + File.separator
	                    + TestngInitializerTest.TESTNG_PLATFORMSERVICE + File.separator + "MilestoneService.json";
				testData = JsonUtils.getJsonData(path).getAsJsonObject();
				
		} catch (Exception e) {
				log.error("Error preparing data at MilestoneService record ", e);
			}
	}
	
	@Test(priority = 1)
	public void testSaveOutcomeDefinitionRecord() throws InsightsCustomException {
		try {
			InsightsTools insightsMilestoneTools = outComeConfigDAL.getOutComeByToolName(toolName);
			int toolId = insightsMilestoneTools.getId();
			JsonObject saveOutcomeConfigjson = outcomeConfigController.saveOutcomeConfig(testData.get("saveOutcomeJson").toString().replace("toolNameeee1", String.valueOf(toolId)));
			Assert.assertNotNull(saveOutcomeConfigjson);
			if (saveOutcomeConfigjson.has(MilestoneConstants.STATUS) && saveOutcomeConfigjson.get(MilestoneConstants.STATUS).getAsString().equalsIgnoreCase(PlatformServiceConstants.SUCCESS)) {
				outcomeList = gson.fromJson(outcomeConfigController.getAllActiveOutcome().get("data"), new TypeToken<List<JsonObject>>(){}.getType());
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
	public void testSaveMilestoneDefinitionInvalidStartTime() throws InsightsCustomException {
		try {
			String saveMilestoneJsonWrongStartTime="";
			for (JsonObject recordForSave : filterOutcomeList) {
				saveMilestoneJsonWrongStartTime = testData.get("saveMilestoneJsonWrongStartTime").toString().replace("Outcommmme", recordForSave.get("outcomeName").getAsString());
			}
			JsonObject response = milestoneConfigController.saveMileStoneConfig(saveMilestoneJsonWrongStartTime);
			Assert.assertEquals(PlatformServiceConstants.FAILURE, response.get("status").getAsString().replace("\"", ""));
		
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
	}
	}
	
	@Test(priority = 3)
	public void testSaveMilestoneDefinitionRecord() throws InsightsCustomException {
		try {
			String saveMilestoneJson="";
			for (JsonObject recordForSave : filterOutcomeList) {
				saveMilestoneJson = testData.get("saveMilestoneJson").toString().replace("Outcommmme", recordForSave.get("outcomeName").getAsString());
			}
			JsonObject saveMilestoneConfigjson = milestoneConfigController.saveMileStoneConfig(saveMilestoneJson);
			Assert.assertNotNull(saveMilestoneConfigjson);
			if (saveMilestoneConfigjson.has(MilestoneConstants.STATUS) && saveMilestoneConfigjson.get(MilestoneConstants.STATUS).getAsString().equalsIgnoreCase(PlatformServiceConstants.SUCCESS)) {
				milestoneList = gson.fromJson(milestoneConfigController.fetchMileStoneConfig().get("data"), new TypeToken<List<JsonObject>>(){}.getType());
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
	
	@Test(priority = 4)
	public void testSaveMilestoneDefinitionRecordDuplicate() throws InsightsCustomException {
		try {
			JsonObject response = milestoneConfigController.saveMileStoneConfig(testData.get("saveMilestoneJson").toString());
			Assert.assertEquals(PlatformServiceConstants.FAILURE, response.get("status").getAsString().replace("\"", ""));
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 5)
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
	
	@Test(priority = 6)
	public void testEditMilestoneInvalidStartTime() throws InsightsCustomException {
		try {
			milestoneList = gson.fromJson(milestoneConfigController.fetchMileStoneConfig().get("data"), new TypeToken<List<JsonObject>>(){}.getType());
			filterMilestoneList = milestoneList.stream().filter(outcomeJson -> outcomeJson.get("mileStoneName").getAsString().equalsIgnoreCase(milestoneNameString)).collect(Collectors.toList());
			String editMilestoneConfig = milestoneList.get(0).toString();
			String startDate = milestoneList.get(0).get("startDate").toString();
			String endDate = milestoneList.get(0).get("endDate").toString();
			editMilestoneConfig=editMilestoneConfig.replace("\"startDate\":"+startDate,"\"startDate\":\"2025-10-10T00:00:00Z\"");
			editMilestoneConfig=editMilestoneConfig.replace("\"endDate\":"+endDate, "\"endDate\":\"2020-10-22T00:00:00Z\"");
			JsonObject response = milestoneConfigController.updateMileStoneConfig(editMilestoneConfig);
			Assert.assertEquals(PlatformServiceConstants.FAILURE, response.get("status").getAsString().replace("\"", ""));
		
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
	}
	}
	
	@Test(priority = 7)
	public void testEditMilestoneRecord() throws InsightsCustomException {
		try {
			milestoneList = gson.fromJson(milestoneConfigController.fetchMileStoneConfig().get("data"), new TypeToken<List<JsonObject>>(){}.getType());
			filterMilestoneList = milestoneList.stream().filter(outcomeJson -> outcomeJson.get("mileStoneName").getAsString().equalsIgnoreCase(milestoneNameString)).collect(Collectors.toList());
			int id = filterMilestoneList.get(0).get("listOfOutcomes").getAsJsonArray().get(0).getAsJsonObject().get("id").getAsInt();
			arr.add(id);
			milestoneList.get(0).add("existingOutcomeList", arr);
			String OutcomeName = filterMilestoneList.get(0).get("listOfOutcomes").getAsJsonArray().get(0).getAsJsonObject().get("outcomeName").getAsString();
			milestoneList.get(0).remove("listOfOutcomes");
			outcomeList1.add(OutcomeName);
			milestoneList.get(0).add("outcomeList", outcomeList1);
			String editMilestoneConfig = milestoneList.get(0).toString();
			editMilestoneConfig=editMilestoneConfig.replace("NEWRELIC_MILESTONE_EXECUTION", "NEWRELIC_MILESTONE");
			JsonObject editMilestoneConfigjson = milestoneConfigController.updateMileStoneConfig(editMilestoneConfig);
			Assert.assertNotNull(editMilestoneConfigjson);
			if (editMilestoneConfigjson.has(MilestoneConstants.STATUS) && editMilestoneConfigjson.get(MilestoneConstants.STATUS).getAsString().equalsIgnoreCase(PlatformServiceConstants.SUCCESS)) {
				milestoneList = gson.fromJson(milestoneConfigController.fetchMileStoneConfig().get("data"), new TypeToken<List<JsonObject>>(){}.getType());
				filterMilestoneList = milestoneList.stream().filter(outcomeJson -> outcomeJson.get("mileStoneName").getAsString().equalsIgnoreCase(milestoneNameString)).collect(Collectors.toList());
				Assert.assertTrue(milestoneList.stream().anyMatch(MilestoneJson -> MilestoneJson.get("mileStoneName").getAsString().equalsIgnoreCase(milestoneNameString)));
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
	
	@Test(priority = 8)
	public void testEditMilestoneException() throws InsightsCustomException {
		try {
			milestoneList = gson.fromJson(milestoneConfigController.fetchMileStoneConfig().get("data"), new TypeToken<List<JsonObject>>(){}.getType());
			filterMilestoneList = milestoneList.stream().filter(outcomeJson -> outcomeJson.get("mileStoneName").getAsString().equalsIgnoreCase(milestoneNameString)).collect(Collectors.toList());
			milestoneOutcomeConfigId = filterMilestoneList.get(0).get("listOfOutcomes").getAsJsonArray().get(0).getAsJsonObject().get("id").getAsInt();
			arr.add(milestoneOutcomeConfigId);
			milestoneList.get(0).add("existingOutcomeList", arr);
			String editMilestoneConfig = milestoneList.get(0).toString();
			String startDate = milestoneList.get(0).get("startDate").toString();
			String endDate = milestoneList.get(0).get("endDate").toString();
			editMilestoneConfig=editMilestoneConfig.replace("\"startDate\":"+startDate,"\"startDate\":\"2025-10-10T00:00:00Z\"");
			editMilestoneConfig=editMilestoneConfig.replace("\"endDate\":"+endDate, "\"endDate\":\"2020-10-22T00:00:00Z\"");
			JsonObject response = milestoneConfigController.updateMileStoneConfig(editMilestoneConfig);
			Assert.assertEquals(PlatformServiceConstants.FAILURE, response.get("status").getAsString().replace("\"", ""));
		
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
	}
	}

	@Test(priority = 9)
	public void testDeleteMilestoneRecord() throws InsightsCustomException {
		log.debug(" filterList {} ",filterMilestoneList);
		 for (JsonObject recordForDelete : filterMilestoneList) {
			 milestoneConfigController.deleteMileStoneConfig(recordForDelete.get("id").getAsInt()); 
			 } 
		 for (JsonObject recordForDelete : filterOutcomeList) {
			 outcomeConfigController.deleteOutcomeConfig(recordForDelete.get("id").getAsInt()); 
			 }	    
	}
	
	@Test(priority = 10)
	public void testDeleteMilestoneNoRecordException() throws InsightsCustomException {
		log.debug(" filterList {} ",filterMilestoneList);
		JsonObject response = null;
		 for (JsonObject recordForDelete : filterMilestoneList) {
			 response = milestoneConfigController.deleteMileStoneConfig(recordForDelete.get("id").getAsInt()); 
			 } 
		 Assert.assertEquals(PlatformServiceConstants.FAILURE, response.get("status").getAsString().replace("\"", ""));
		 InsightsTools insightsMilestoneTools = outComeConfigDAL.getOutComeByToolName(toolName);
			int toolId = insightsMilestoneTools.getId();
			outcomeConfigController.saveOutcomeConfig(testData.get("saveOutcomeJson").toString().replace("toolNameeee1", String.valueOf(toolId)));
			outcomeList = gson.fromJson(outcomeConfigController.getAllActiveOutcome().get("data"), new TypeToken<List<JsonObject>>(){}.getType());
			filterOutcomeList = outcomeList.stream().filter(outcomeJson -> outcomeJson.get("outcomeName").getAsString().equalsIgnoreCase(outcomeNameString)).collect(Collectors.toList());
		    String saveMilestoneJson="";
			for (JsonObject recordForSave : filterOutcomeList) {
				saveMilestoneJson = testData.get("saveMilestoneJson").toString().replace("Outcommmme", recordForSave.get("outcomeName").getAsString());
			}
			milestoneConfigController.saveMileStoneConfig(saveMilestoneJson);
			milestoneList = gson.fromJson(milestoneConfigController.fetchMileStoneConfig().get("data"), new TypeToken<List<JsonObject>>(){}.getType());
			filterMilestoneList = milestoneList.stream().filter(outcomeJson -> outcomeJson.get("mileStoneName").getAsString().equalsIgnoreCase(milestoneNameString)).collect(Collectors.toList());
	 }
	
	@Test(priority = 11)
	public void testDeleteMilestoneConfigWithError() throws InsightsCustomException {
		try {
			milestoneList = gson.fromJson(milestoneConfigController.fetchMileStoneConfig().get("data"), new TypeToken<List<JsonObject>>(){}.getType());
			filterMilestoneList = milestoneList.stream().filter(outcomeJson -> outcomeJson.get("mileStoneName").getAsString().equalsIgnoreCase(milestoneNameString)).collect(Collectors.toList());
		    
			arr.add("NewOutcomeList: Outcommmme");
			milestoneList.get(0).add("existingOutcomeList", arr);
			String editMilestoneConfig = milestoneList.get(0).toString();
			editMilestoneConfig = editMilestoneConfig.replace("listOfOutcomes", "outcomeList");
			JsonObject configJson =JsonUtils.parseStringAsJsonObject(editMilestoneConfig);
			updateMileStoneConfig(configJson, MilestoneEnum.OutcomeStatus.ERROR.name());
			milestoneList = gson.fromJson(milestoneConfigController.fetchMileStoneConfig().get("data"), new TypeToken<List<JsonObject>>(){}.getType());
			filterMilestoneList = milestoneList.stream().filter(outcomeJson -> outcomeJson.get("mileStoneName").getAsString().equalsIgnoreCase(milestoneNameString)).collect(Collectors.toList());
		   
			JsonObject response = null;
			 for (JsonObject recordForDelete : filterMilestoneList) {
				 response = milestoneConfigController.deleteMileStoneConfig(recordForDelete.get("id").getAsInt()); 
				 } 
			 Assert.assertEquals(PlatformServiceConstants.FAILURE, response.get("status").getAsString().replace("\"", ""));
				
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 12)
	public void testStatusUpdateMilestoneRecordInvalidConfig() throws InsightsCustomException {
		milestoneList = gson.fromJson(milestoneConfigController.fetchMileStoneConfig().get("data"), new TypeToken<List<JsonObject>>(){}.getType());
		filterMilestoneList = milestoneList.stream().filter(outcomeJson -> outcomeJson.get("mileStoneName").getAsString().equalsIgnoreCase(milestoneNameString)).collect(Collectors.toList());
	    
		try {
			String statusUpdateMilstone="";
			for (JsonObject recordForSave : filterMilestoneList) {
				statusUpdateMilstone =testData.get("statusUpdateMilstone").toString().replace("iiddee", recordForSave.get("id").getAsString()).replace("activee", "false");
			}
			JsonObject response = milestoneConfigController.restartMileStoneConfig(statusUpdateMilstone);
			Assert.assertEquals(PlatformServiceConstants.FAILURE, response.get("status").getAsString().replace("\"", ""));
			milestoneList = gson.fromJson(milestoneConfigController.fetchMileStoneConfig().get("data"), new TypeToken<List<JsonObject>>(){}.getType());
			filterMilestoneList = milestoneList.stream().filter(outcomeJson -> outcomeJson.get("mileStoneName").getAsString().equalsIgnoreCase(milestoneNameString)).collect(Collectors.toList());
		    
			for (JsonObject recordForDelete : filterMilestoneList) {
				 mileStoneDAL.deleteMileStoneConfig(recordForDelete.get("id").getAsInt());
				 } 
			String saveMilestoneJson="";
			for (JsonObject recordForSave : filterOutcomeList) {
					saveMilestoneJson = testData.get("saveMilestoneJson").toString().replace("Outcommmme", recordForSave.get("outcomeName").getAsString());
				}
				milestoneConfigController.saveMileStoneConfig(saveMilestoneJson);
				milestoneList = gson.fromJson(milestoneConfigController.fetchMileStoneConfig().get("data"), new TypeToken<List<JsonObject>>(){}.getType());
				filterMilestoneList = milestoneList.stream().filter(outcomeJson -> outcomeJson.get("mileStoneName").getAsString().equalsIgnoreCase(milestoneNameString)).collect(Collectors.toList());
		
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 13)
	public void testStatusUpdateMilestoneRecord() throws InsightsCustomException {
		try {
			milestoneList = gson.fromJson(milestoneConfigController.fetchMileStoneConfig().get("data"), new TypeToken<List<JsonObject>>(){}.getType());
			filterMilestoneList = milestoneList.stream().filter(outcomeJson -> outcomeJson.get("mileStoneName").getAsString().equalsIgnoreCase(milestoneNameString)).collect(Collectors.toList());
	        String statusUpdateMilstone="";
			for (JsonObject recordForSave : filterMilestoneList) {
				statusUpdateMilstone =testData.get("statusUpdate").toString().replace(testData.get("statusUpdate").getAsJsonObject().get("id").toString(), recordForSave.get("id").getAsString()).replace("activee", "false");
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
	
	@Test(priority = 14)
	public void testDeleteMilestoneRecordError() throws InsightsCustomException {
		log.debug(" filterList {} ",filterMilestoneList);
		JsonObject response = null;
		 for (JsonObject recordForDelete : filterMilestoneList) {
			response = milestoneConfigController.deleteMileStoneConfig(recordForDelete.get("id").getAsInt()); 
			 } 
		 Assert.assertEquals(PlatformServiceConstants.FAILURE, response.get("status").getAsString());			
	}

	@Test(priority = 15)
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
}