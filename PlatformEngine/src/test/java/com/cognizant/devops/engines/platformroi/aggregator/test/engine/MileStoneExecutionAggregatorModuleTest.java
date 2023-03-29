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
package com.cognizant.devops.engines.platformroi.aggregator.test.engine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.junit.Assert;

import com.cognizant.devops.engines.platformengine.test.engine.EngineTestData;
import com.cognizant.devops.engines.platformroi.aggregator.MileStoneStatusAggregatorModule;
import com.cognizant.devops.engines.platformroi.subscriber.MilestoneExecutor;
import com.cognizant.devops.engines.platformroi.subscriber.MilestoneStatusCommunicationSubscriber;
import com.cognizant.devops.engines.testngInitializer.TestngInitializerTest;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.constants.MQMessageConstants;
import com.cognizant.devops.platformcommons.constants.MilestoneConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.outcome.InsightsTools;
import com.cognizant.devops.platformdal.outcome.OutComeConfigDAL;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.JsonArray;

@Test
public class MileStoneExecutionAggregatorModuleTest {
	private static final Logger log = LogManager.getLogger(MileStoneExecutionAggregatorModuleTest.class.getName());
	MilestoneExecutor mileStoneExecutor = new MilestoneExecutor();
	MileStoneStatusAggregatorModule mileStoneStatusAggregatorModule = new MileStoneStatusAggregatorModule();
	MileStoneExecutionAggregatorModuleTestData mileStoneTestData = new MileStoneExecutionAggregatorModuleTestData();
	OutComeConfigDAL outComeConfigDAL = new OutComeConfigDAL();
	InsightsTools insightsTool;
	JsonObject testData = new JsonObject();
	List<JsonObject> outcomeList = new ArrayList<>();
	List<JsonObject> filterOutcomeList = new ArrayList<>();
	List<JsonObject> milestoneList = new ArrayList<>();
	List<JsonObject> filterMilestoneList = new ArrayList<>();
	Gson gson = new Gson();
	EngineTestData engineTestData = new EngineTestData();

	@BeforeClass
	public void onInit() throws IOException, InsightsCustomException, Exception {

		try {
			String path = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator
					+ TestngInitializerTest.TESTNG_TESTDATA + File.separator
					+ TestngInitializerTest.TESTNG_PLATFORMENGINE + File.separator + "MileStoneExecution.json";
			testData = JsonUtils.getJsonData(path).getAsJsonObject();

			insightsTool = engineTestData.prepareInsightsToolData(0, "NEWRELIC", "ROI", "NEWRELIC_MILESTONE_EXECUTION",
					"{}", true);
		} catch (Exception e) {
			log.error("Error preparing data at MilestoneExecutionAggregatorTest record ", e);
		}

	}

	@Test(priority = 1)
	public void testSaveOutcomeDefinitionRecord() throws InsightsCustomException {
		try {
			outComeConfigDAL.saveInsightsTools(insightsTool);
			InsightsTools insightsMilestoneTools = outComeConfigDAL.getOutComeByToolName("NEWRELIC");
			int toolId = insightsMilestoneTools.getId();
			String config = testData.get("saveOutcomeJson").toString().replace("2", String.valueOf(toolId));
			JsonObject configJson = JsonUtils.parseStringAsJsonObject(config);
			int result = mileStoneTestData.saveOutcomeConfig(configJson);
			JsonObject saveOutcomeConfigjson = mileStoneTestData.buildSuccessResponseWithData(null);
			Assert.assertNotNull(saveOutcomeConfigjson);
			if (saveOutcomeConfigjson.has(MilestoneConstants.STATUS) && saveOutcomeConfigjson
					.get(MilestoneConstants.STATUS).getAsString().equalsIgnoreCase(PlatformServiceConstants.SUCCESS)) {
				JsonArray element = mileStoneTestData.getAllActiveOutcome();
				JsonObject jsonElement = mileStoneTestData.buildSuccessResponseWithData(element);
				outcomeList = gson.fromJson(jsonElement.get("data"), new TypeToken<List<JsonObject>>() {
				}.getType());
				filterOutcomeList = outcomeList.stream().filter(
						outcomeJson -> outcomeJson.get("outcomeName").getAsString().equalsIgnoreCase("TestOutcome1"))
						.collect(Collectors.toList());
				Assert.assertTrue(outcomeList.stream().anyMatch(
						outcomeJson -> outcomeJson.get("outcomeName").getAsString().equalsIgnoreCase("TestOutcome1")));
				Assert.assertTrue(saveOutcomeConfigjson.get("status").getAsString()
						.equalsIgnoreCase(PlatformServiceConstants.SUCCESS));
			} else {
				Assert.fail(" Save outcome configure has issue  ");
			}
		} catch (Exception e) {
			log.error(e);
			Assert.fail(e.getMessage());
		}

	}

	@Test(priority = 2)
	public void testSaveMilestoneDefinitionRecord() throws InsightsCustomException {
		try {
			String saveMilestoneJson = "";
			for (JsonObject recordForSave : filterOutcomeList) {
				saveMilestoneJson = testData.get("saveMilestoneJson").toString().replace("TestOutcome1",
						recordForSave.get("outcomeName").getAsString());
			}
			JsonObject configJson = JsonUtils.parseStringAsJsonObject(saveMilestoneJson);
			int result = mileStoneTestData.saveMileStoneConfig(configJson);
			JsonObject saveMilestoneConfigjson = mileStoneTestData.buildSuccessResponseWithData(null);
			Assert.assertNotNull(saveMilestoneConfigjson);
			if (saveMilestoneConfigjson.has(MilestoneConstants.STATUS) && saveMilestoneConfigjson
					.get(MilestoneConstants.STATUS).getAsString().equalsIgnoreCase(PlatformServiceConstants.SUCCESS)) {
				JsonArray element = mileStoneTestData.getMileStoneConfigs();
				JsonObject jsonElement = mileStoneTestData.buildSuccessResponseWithData(element);
				milestoneList = gson.fromJson(jsonElement.get("data"), new TypeToken<List<JsonObject>>() {
				}.getType());
				filterMilestoneList = milestoneList.stream().filter(
						outcomeJson -> outcomeJson.get("mileStoneName").getAsString().equalsIgnoreCase("TestMile1"))
						.collect(Collectors.toList());
				Assert.assertTrue(milestoneList.stream().anyMatch(MilestoneJson -> MilestoneJson.get("mileStoneName")
						.getAsString().equalsIgnoreCase("TestMile1")));
				Assert.assertTrue(saveMilestoneConfigjson.get("status").getAsString()
						.equalsIgnoreCase(PlatformServiceConstants.SUCCESS));
			} else {
				Assert.fail(" Save Milestone configure has issue  ");
			}
		} catch (Exception e) {
			log.error(e);
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 3)
	public void testExecuteMilestone() {
		try {
			mileStoneExecutor.executeMilestone();
			Assert.assertTrue(true);
		} catch (Exception e) {
			log.error(e);
		}
	}

	@Test(priority = 4)
	public void testMilestoneStatusCommunicationSubscriber() {
		try {
			String statusRoutingKey = MQMessageConstants.MILESTONE_STATUS_QUEUE;
			new MilestoneStatusCommunicationSubscriber(statusRoutingKey);
			Assert.assertTrue(true);
		} catch (Exception e) {
			log.error(e);
		}
	}

}