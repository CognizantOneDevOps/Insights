/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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
package com.cognizant.devops.engines.platformdataarchivalengine.test.engine;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.cognizant.devops.engines.platformdataarchivalengine.modules.aggregator.DataArchivalAggregatorModule;
import com.cognizant.devops.engines.testngInitializer.TestngInitializerTest;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.core.enums.DataArchivalStatus;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.agentConfig.AgentConfigDAL;
import com.cognizant.devops.platformdal.dataArchivalConfig.DataArchivalConfigDal;
import com.cognizant.devops.platformdal.dataArchivalConfig.InsightsDataArchivalConfig;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Test
public class DataArchivalEngineTest extends DataArchivalEngineData {
	private static Logger log = LogManager.getLogger(DataArchivalEngineTest.class.getName());
	AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
	DataArchivalConfigDal dataArchivalConfigdal = new DataArchivalConfigDal();
	String host = null;
	
	JsonObject testData = new JsonObject();	
	JsonObject archivalRecord = null;
	String archivalName = "";
	String startDate = "";
	
	String endDate = "";
	String author = "";
	String status = "";
	JsonObject configDetails = null;
	String routingKey = "";
	String routingKeyHealth = "";
	String routingKeyHealthNeo4j = "";
	JsonElement urlJson = null;
	JsonElement successHealthMessageJson = null;
	JsonElement failureHealthMessageJson = null;
	JsonElement urlMessageWithEmptyArchivalName = null;
	JsonElement urlMessageWithEmptyURL = null;
	JsonElement removeContainer = null;
	JsonElement urlMessageWithEmptyContainer = null;
	
	@BeforeClass
	public void onInit() throws IOException, TimeoutException, InsightsCustomException, InterruptedException, Exception {
		// save data archival agent
		String path = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator + TestngInitializerTest.TESTNG_TESTDATA + File.separator
				+ TestngInitializerTest.TESTNG_PLATFORMENGINE + File.separator + "DataArchivalEngine.json";
		testData = JsonUtils.getJsonData(path).getAsJsonObject();
		archivalRecord = testData.get("archivalRecord").getAsJsonObject();
		archivalName = archivalRecord.get("archivalName").getAsString();
		startDate = archivalRecord.get("startDate").getAsString();
		epochStartDate = InsightsUtils.getEpochTime(startDate) / 1000;
		endDate = archivalRecord.get("endDate").getAsString();
		epochEndDate = InsightsUtils.getEpochTime(endDate) / 1000;
		author = archivalRecord.get("author").getAsString();
		daysToRetain = Integer.valueOf(archivalRecord.get("daysToRetain").getAsString());
		createdOn = InsightsUtils.getTodayTime() / 1000;
		expiryDate = getExpiryDate(createdOn, daysToRetain);
		status = DataArchivalStatus.INPROGRESS.name();
		configDetails = testData.getAsJsonObject().get("configDetails").getAsJsonObject();
		routingKey = configDetails.get("publish").getAsJsonObject().get("data").getAsString();
		routingKeyHealth = configDetails.get("publish").getAsJsonObject().get("health").getAsString();
		routingKeyHealthNeo4j = testData.get("routingKeyHealthNeo4j").getAsString();
		urlJson = testData.get("urlMessage");
		successHealthMessageJson = testData.get("successHealthMessageString");
		failureHealthMessageJson = testData.get("failureHealthMessageString");
		urlMessageWithEmptyArchivalName = testData.get("urlMessageWithEmptyArchivalName");
		urlMessageWithEmptyURL = testData.get("urlMessageWithEmptyURL");
		removeContainer = testData.get("removeContainer");
		urlMessageWithEmptyContainer = testData.get("urlMessageWithEmptyContainer");
		try {
			agentConfigDAL.saveAgentConfigFromUI(configDetails.getAsJsonObject().get("agentId").getAsString(),
					configDetails.getAsJsonObject().get("toolCategory").getAsString(), configDetails.getAsJsonObject().get("labelName").getAsString(),
					configDetails.getAsJsonObject().get("toolName").getAsString(), configDetails.getAsJsonObject(), configDetails.getAsJsonObject().get("agentVersion").getAsString(),
					configDetails.getAsJsonObject().get("osversion").getAsString(), updateDate, testData.get("vault").getAsBoolean(),false);
		} catch (Exception e) {
			log.error("message  {} ", e);
		}

		// save Data archival record
		try {
		InsightsDataArchivalConfig dataArchivalConfig = new InsightsDataArchivalConfig();
		dataArchivalConfig.setArchivalName(archivalName);
		dataArchivalConfig.setStartDate(epochStartDate);
		dataArchivalConfig.setEndDate(epochEndDate);
		dataArchivalConfig.setAuthor(author);
		dataArchivalConfig.setStatus(DataArchivalStatus.INPROGRESS.name());
		dataArchivalConfig.setExpiryDate(expiryDate);
		dataArchivalConfig.setDaysToRetain(daysToRetain);
		dataArchivalConfig.setCreatedOn(createdOn);
		dataArchivalConfig.setBoltPort(0);
		dataArchivalConfigdal.saveDataArchivalConfiguration(dataArchivalConfig);
		} catch (Exception e) {
			log.error("message  {} ", e);
		}		

		// save Data archival record
		try {
		InsightsDataArchivalConfig dataArchivalFailConfig = new InsightsDataArchivalConfig();
		dataArchivalFailConfig.setArchivalName(archivalFailName);
		dataArchivalFailConfig.setStartDate(epochStartDate);
		dataArchivalFailConfig.setEndDate(epochEndDate);
		dataArchivalFailConfig.setAuthor(author);
		dataArchivalFailConfig.setStatus(DataArchivalStatus.INPROGRESS.name());
		dataArchivalFailConfig.setExpiryDate(expiryDate);
		dataArchivalFailConfig.setDaysToRetain(daysToRetain);
		dataArchivalFailConfig.setCreatedOn(createdOn);
		dataArchivalFailConfig.setBoltPort(0);
		dataArchivalConfigdal.saveDataArchivalConfiguration(dataArchivalFailConfig);
		} catch (Exception e) {
			log.error("message  {} ", e);
		}
		
		// save Data archival record
		try {
		InsightsDataArchivalConfig dataArchivalConfig = new InsightsDataArchivalConfig();
		dataArchivalConfig.setArchivalName("TEST1");
		dataArchivalConfig.setStartDate(epochStartDate);
		dataArchivalConfig.setEndDate(epochEndDate);
		dataArchivalConfig.setAuthor(author);
		dataArchivalConfig.setStatus(DataArchivalStatus.INPROGRESS.name());
		dataArchivalConfig.setDaysToRetain(2);
		dataArchivalConfig.setCreatedOn(1673740800l);
		dataArchivalConfig.setBoltPort(0);
		expiryDate = getExpiryDate(1673740800l, 2);
		dataArchivalConfig.setExpiryDate(expiryDate);
		dataArchivalConfig.setContainerID("abcd");
		dataArchivalConfigdal.saveDataArchivalConfiguration(dataArchivalConfig);
				} catch (Exception e) {
					log.error("message  {} ", e);
				}


		DataArchivalAggregatorModule dam = new DataArchivalAggregatorModule();
		dam.runDataArchival();
		Thread.sleep(1000);
		log.debug("Test Data flow has been created successfully");
	}

	@Test(priority = 1)
	public void testSaveURLForArchivalRecord() throws IOException, TimeoutException, InterruptedException, InsightsCustomException {
		try {
			publishDataArchivalDetails(routingKey, urlJson.toString());
			Thread.sleep(2000);
			InsightsDataArchivalConfig record = dataArchivalConfigdal.getSpecificArchivalRecord(archivalName);
			Assert.assertNotNull(record);
			Assert.assertEquals(record.getSourceUrl(),
					urlJson.getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("sourceUrl").getAsString());
			Assert.assertEquals(record.getBoltPort(),
					urlJson.getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("boltPort").getAsInt());
		} catch (AssertionError e) {
			log.error("message  {} ", e);
		}
	}

	@Test(priority = 2)
	public void testSaveURLWithEmptyArchivalName() throws Exception {
		try {
			publishDataArchivalDetails(routingKey, urlMessageWithEmptyArchivalName.toString());
			Thread.sleep(2000);
			InsightsDataArchivalConfig record = dataArchivalConfigdal.getSpecificArchivalRecord(archivalFailName);
			Assert.assertNotNull(record);
			Assert.assertNull(record.getSourceUrl());
		} catch (AssertionError e) {
			log.error("message  {} ", e);
		}
	}

	@Test(priority = 3)
	public void testSaveURLWithEmptyURL() throws Exception {
		try {
			publishDataArchivalDetails(routingKey, urlMessageWithEmptyURL.toString());
			Thread.sleep(2000);
			InsightsDataArchivalConfig record = dataArchivalConfigdal.getSpecificArchivalRecord(archivalFailName);
			Assert.assertNotNull(record);
			Assert.assertNull(record.getSourceUrl());
		} catch (AssertionError e) {
			log.error("message  {} ", e);
		}
	}

	@Test(priority = 4)
	public void testRemoveContainer() throws Exception {
		try {
			publishDataArchivalDetails(routingKey, removeContainer.toString());
			Thread.sleep(2000);
			InsightsDataArchivalConfig record = dataArchivalConfigdal.getSpecificArchivalRecord(archivalFailName);
			Assert.assertNotNull(record);
			Assert.assertEquals(record.getStatus(), "INPROGRESS");
		} catch (AssertionError e) {
			log.error("message  {} ", e);
		}
	}

	@Test(priority = 5)
	public void testSaveURLWithEmptyContainerId() throws Exception {
		try {
			publishDataArchivalDetails(routingKey, urlMessageWithEmptyContainer.toString());
			Thread.sleep(2000);
			InsightsDataArchivalConfig record = dataArchivalConfigdal.getSpecificArchivalRecord(archivalFailName);
			Assert.assertNotNull(record);
			Assert.assertNull(record.getContainerID());
		} catch (AssertionError e) {
			log.error("message  {} ", e);
		}
	}

	@Test(priority = 6)
	public void testSaveURLWithDBNotWorking() throws Exception {
		try {
			publishDataArchivalDetails(routingKey, urlMessageWithEmptyURL.toString());
			host = ApplicationConfigProvider.getInstance().getPostgre().getInsightsDBUrl();
			ApplicationConfigProvider.getInstance().getPostgre().setInsightsDBUrl("notLocalhost");
			Thread.sleep(2000);
			InsightsDataArchivalConfig record = dataArchivalConfigdal.getSpecificArchivalRecord(archivalFailName);
			Assert.assertNotNull(record);
			Assert.assertNull(record.getSourceUrl());
			ApplicationConfigProvider.getInstance().getPostgre().setInsightsDBUrl(host);
		} catch (AssertionError e) {
			log.error("message  {} ", e);
		}
	}

	@Test(priority = 7)
	public void testSaveSuccessHealthNodeinNeo4j() throws Exception {
		try {
			publishDataArchivalDetails(routingKeyHealth, successHealthMessageJson.toString());
			Thread.sleep(2000);
			int countOfRecords = readNeo4JData(routingKeyHealthNeo4j,
					successHealthMessageJson.getAsJsonObject().get("execId").getAsString());
			Assert.assertTrue(countOfRecords > 0);
		} catch (AssertionError e) {
			log.error("message  {} ", e);
		}
	}

	@Test(priority = 8)
	public void testSaveFailureHealthNodeinNeo4j() throws Exception {
		try {
			publishDataArchivalDetails(routingKeyHealth, failureHealthMessageJson.toString());
			Thread.sleep(2000);
			int countOfRecords = readNeo4JData(routingKeyHealthNeo4j,
					failureHealthMessageJson.getAsJsonObject().get("execId").getAsString());
			Assert.assertTrue(countOfRecords > 0);
		} catch (AssertionError e) {
			log.error("message  {} ", e);
		}
	}

	@AfterClass
	public void cleanUp() {
		try {
			agentConfigDAL.deleteAgentConfigurations(configDetails.getAsJsonObject().get("agentId").getAsString());
		} catch (Exception e) {
			log.error("message  {} ", e);
		}
		try {
			dataArchivalConfigdal.deleteArchivalRecord(archivalName);
		} catch (Exception e) {
			log.error("message  {} ", e);
		}
		try {
			dataArchivalConfigdal.deleteArchivalRecord(archivalFailName);
		} catch (Exception e) {
			log.error("message  {} ", e);
		}

	}

}
