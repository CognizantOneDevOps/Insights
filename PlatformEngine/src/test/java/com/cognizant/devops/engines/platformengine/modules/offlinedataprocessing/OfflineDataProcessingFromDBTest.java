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
package com.cognizant.devops.engines.platformengine.modules.offlinedataprocessing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import com.cognizant.devops.engines.platformengine.modules.aggregator.EngineAggregatorModule;
import com.cognizant.devops.engines.testngInitializer.TestngInitializerTest;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.offlineDataProcessing.InsightsOfflineConfig;
import com.cognizant.devops.platformdal.offlineDataProcessing.InsightsOfflineConfigDAL;
import com.google.gson.JsonObject;

public class OfflineDataProcessingFromDBTest {
	private static final Logger log = LogManager.getLogger(OfflineDataProcessingFromDBTest.class.getName());

	OfflineDataProcessingExecutor executor = null;
	OfflineDataProcessingFromDB offlineDataProcessingDB = new OfflineDataProcessingFromDB();
	JsonObject testData = new JsonObject();
	InsightsOfflineConfig offlineConfig = new InsightsOfflineConfig();
	List<InsightsOfflineConfig> offlineDataConfig = new ArrayList<InsightsOfflineConfig>();
	InsightsOfflineConfigDAL offlineConfigDAL = new InsightsOfflineConfigDAL();
	OfflineProcessingTestData offlineData = new OfflineProcessingTestData();
	Map<String, String> loggingInfo = new ConcurrentHashMap<>();

	@BeforeClass
	public void onInit()
			throws IOException, TimeoutException, InsightsCustomException, InterruptedException, Exception {
		String path = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator
				+ TestngInitializerTest.TESTNG_TESTDATA + File.separator + TestngInitializerTest.TESTNG_PLATFORMENGINE
				+ File.separator + "OfflineDataProcessingFromDB.json";
		testData = JsonUtils.getJsonData(path).getAsJsonObject();

		/*
		 * Saving Offline Processing Data
		 */

		offlineData.saveOfflineDefinition(testData.get("OfflineConfig2").getAsJsonObject());

		/*
		 * Preparing Offline Processing Data
		 */

		offlineConfig.setQueryName(testData.get("queryName").toString());
		offlineConfig.setToolName(testData.get("toolName").toString());
		offlineConfig.setCronSchedule(OfflineProcessingTestData.cronSchedule);
		offlineConfig.setCypherQuery(testData.get("cypherQuery").toString());
		offlineConfig.setIsActive(testData.get("isActive").getAsBoolean());
		offlineConfig.setLastRunTime(testData.get("lastruntime").getAsLong());
		offlineConfig.setMessage(testData.get("message").toString());
		offlineConfig.setQueryProcessingTime(testData.get("queryProcessingTime").getAsLong());
		offlineConfig.setRecordsProcessed(testData.get("recordsProcessed").getAsInt());
		offlineConfig.setRetryCount(testData.get("retryCount").getAsInt());
		offlineConfig.setStatus(testData.get("status").toString());
		offlineDataConfig.add(offlineConfig);
		Thread.sleep(2000);

	}

	@BeforeMethod
	protected void setUp() throws Exception {
		executor = new OfflineDataProcessingExecutor();
	}

	@Test(priority = 1)
	public void testprocessOfflineConfigurationFromDB() {
		try {
			offlineDataProcessingDB.processOfflineConfigurationFromDB(offlineDataConfig, loggingInfo);
			Thread.sleep(2000);
		} catch (Exception e) {
			log.error(e);
		}
	}

	@Test(priority = 2)
	public void testOfflineWithEmptyCypherQuery() {
		try {
			offlineConfig.setQueryName(testData.get("OfflineConfig1").getAsJsonObject().get("queryName").toString());
			offlineConfig.setCypherQuery("");
			offlineDataConfig.add(offlineConfig);
			offlineDataProcessingDB.processOfflineConfigurationFromDB(offlineDataConfig, loggingInfo);
			Thread.sleep(2000);
		} catch (Exception e) {
			log.error(e);
		}
	}

	@Test(priority = 3)
	public void testIsQueryScheduledToRunFromDB() {
		try {
			offlineConfig.setQueryName(testData.get("OfflineConfig2").getAsJsonObject().get("queryName").toString());
			boolean result = offlineDataProcessingDB.isQueryScheduledToRunFromDB(OfflineProcessingTestData.lastRunTime,
					OfflineProcessingTestData.cronSchedule);
			Assert.assertEquals(true, result);
		} catch (Exception e) {
			log.error(e);
		}
	}

	@Test(priority = 4)
	public void testMaxRetryCount() {
		try {
			offlineConfig.setQueryName(testData.get("OfflineConfig2").getAsJsonObject().get("queryName").toString());
			offlineConfig.setIsActive(true);
			int retryCount = 3;
			String message = offlineConfig.getMessage();
			offlineData.updateRetryCount(retryCount, offlineConfig, message);
			boolean result = offlineConfig.getIsActive();
			Assert.assertEquals(false, result);
		} catch (Exception e) {
			log.error(e);
		}
	}

	@Test(priority = 5)
	public void testOfflineWithWrongCronSchedule() {
		try {
			offlineConfig.setRetryCount(0);
			offlineConfig.setQueryName(testData.get("OfflineConfig2").getAsJsonObject().get("queryName").toString());
			offlineConfig
					.setCypherQuery(testData.get("OfflineConfig2").getAsJsonObject().get("cypherQuery").toString());
			offlineConfig.setCronSchedule(OfflineProcessingTestData.wrongCronSchedule);
			offlineDataConfig.add(offlineConfig);
			offlineDataProcessingDB.processOfflineConfigurationFromDB(offlineDataConfig, loggingInfo);
		} catch (Exception e) {
			log.error(e);
		}
	}

	@AfterMethod
	protected void tearDown() throws Exception {
		EngineAggregatorModule em = new EngineAggregatorModule();
		em.executeJob();
	}

	@AfterClass
	public void cleanUp() {
		try {
			// Cleaning Postgre
			offlineData.deleteOfflineData(testData.get("queryName").toString());
			offlineData.deleteOfflineData(testData.get("OfflineConfig1").getAsJsonObject().get("queryName").toString());
			offlineData.deleteOfflineData(testData.get("OfflineConfig2").getAsJsonObject().get("queryName").toString());
		} catch (Exception e) {
			log.error("InsightsCustomException : " + e.toString());
		}
	}

}
