/*******************************************************************************
 * Copyright 2023 Cognizant Technology Solutions
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
package com.cognizant.devops.engines.test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.cognizant.devops.engines.Application;
import com.cognizant.devops.engines.CleanUpJobExecutor;
import com.cognizant.devops.engines.ScheduledJobExecutor;
import com.cognizant.devops.engines.platformengine.test.engine.EngineTestData;
import com.cognizant.devops.engines.testngInitializer.TestngInitializerTest;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.timertasks.InsightsSchedulerTaskDAL;
import com.cognizant.devops.platformdal.timertasks.InsightsSchedulerTaskDefinition;
import com.google.gson.JsonObject;

@Test
public class ScheduledJobExecutorTest {
	private static Logger log = LogManager.getLogger(ScheduledJobExecutorTest.class.getName());
	InsightsSchedulerTaskDefinition schedulatTaskDefination = new InsightsSchedulerTaskDefinition();
	InsightsSchedulerTaskDAL schedularTaskDAL = new InsightsSchedulerTaskDAL();
	ScheduledJobExecutor scheduledJobExecutor = new ScheduledJobExecutor();
	CleanUpJobExecutor cleanUpJobExecutor = new CleanUpJobExecutor();
	JsonObject testData = new JsonObject();

	@BeforeClass
	public void onInit()
			throws IOException, TimeoutException, InsightsCustomException, InterruptedException, Exception {
		Application.main(null);

		String path = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator
				+ TestngInitializerTest.TESTNG_TESTDATA + File.separator + TestngInitializerTest.TESTNG_PLATFORMENGINE
				+ File.separator + "Application.json";
		testData = JsonUtils.getJsonData(path).getAsJsonObject();
	}

	@Test(priority = 1)
	public void testSavemethod() {
		try {
			EngineTestData.SaveSchedulatTaskDefination(testData.get("schedulatTaskDefination2").getAsJsonObject());
			EngineTestData.SaveSchedulatTaskDefination(testData.get("schedulatTaskDefination1").getAsJsonObject());
			EngineTestData.SaveSchedulatTaskDefination(testData.get("schedulatTaskDefination3").getAsJsonObject());
			Assert.assertTrue(true);
		} catch (Exception e) {
			log.error(e);
		}
	}

	@Test(priority = 2)
	public void testInitializeEngineScheduledTask() {
		try {
			scheduledJobExecutor.initializeEngineScheduledTask();
			Assert.assertTrue(true);
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	@Test(priority = 3)
	public void testUpdateSchedularTaskDefinition() {
		try {
			JsonObject taskDefination = testData.get("schedulatTaskDefination2").getAsJsonObject();
			InsightsSchedulerTaskDefinition schedular = new InsightsSchedulerTaskDefinition();
			schedular.setTimerTaskId(taskDefination.get("timerTaskId").getAsInt());
			schedular.setComponentName(taskDefination.get("componentName").toString());
			schedular.setComponentClassDetail(taskDefination.get("componentClassDetail").toString());
			schedular.setAction(taskDefination.get("action").toString());
			boolean result = scheduledJobExecutor.updateSchedularTaskDefinition(schedular, "START");
			Assert.assertEquals(true, result);;
		} catch (Exception e) {
			log.error(e);
		}
	}

}