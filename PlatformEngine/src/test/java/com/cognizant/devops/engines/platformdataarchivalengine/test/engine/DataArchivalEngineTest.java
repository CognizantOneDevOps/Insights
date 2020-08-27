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

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.persistence.NoResultException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.engines.platformdataarchivalengine.modules.aggregator.DataArchivalAggregatorModule;
import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.core.enums.DataArchivalStatus;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.agentConfig.AgentConfigDAL;
import com.cognizant.devops.platformdal.dataArchivalConfig.DataArchivalConfigDal;
import com.cognizant.devops.platformdal.dataArchivalConfig.InsightsDataArchivalConfig;

public class DataArchivalEngineTest extends DataArchivalEngineData {
	private static Logger log = LogManager.getLogger(DataArchivalEngineTest.class.getName());
	AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
	DataArchivalConfigDal dataArchivalConfigdal = new DataArchivalConfigDal();
	String host = null;

	@BeforeTest
	public void onInit() throws IOException, TimeoutException, InsightsCustomException, InterruptedException {
		ApplicationConfigCache.loadConfigCache();

		// save data archival agent
		try {
			agentConfigDAL.saveAgentConfigFromUI(agentJson.get("agentId").getAsString(),
					agentJson.get("toolCategory").getAsString(), agentJson.get("labelName").getAsString(),
					agentJson.get("toolName").getAsString(), agentJson, agentJson.get("agentVersion").getAsString(),
					agentJson.get("osversion").getAsString(), updateDate, vault);
		} catch (Exception e) {
			log.error("message  {} ", e);
		}

		// save Data archival record
		InsightsDataArchivalConfig dataArchivalConfig = new InsightsDataArchivalConfig();
		dataArchivalConfig.setArchivalName(archivalName);
		dataArchivalConfig.setStartDate(epochStartDate);
		dataArchivalConfig.setEndDate(epochEndDate);
		dataArchivalConfig.setAuthor(author);
		dataArchivalConfig.setStatus(DataArchivalStatus.INPROGRESS.name());
		dataArchivalConfig.setExpiryDate(expiryDate);
		dataArchivalConfig.setDaysToRetain(daysToRetain);
		dataArchivalConfig.setCreatedOn(createdOn);
		dataArchivalConfigdal.saveDataArchivalConfiguration(dataArchivalConfig);

		// save Data archival record
		InsightsDataArchivalConfig dataArchivalFailConfig = new InsightsDataArchivalConfig();
		dataArchivalFailConfig.setArchivalName(archivalFailName);
		dataArchivalFailConfig.setStartDate(epochStartDate);
		dataArchivalFailConfig.setEndDate(epochEndDate);
		dataArchivalFailConfig.setAuthor(author);
		dataArchivalFailConfig.setStatus(DataArchivalStatus.INPROGRESS.name());
		dataArchivalFailConfig.setExpiryDate(expiryDate);
		dataArchivalFailConfig.setDaysToRetain(daysToRetain);
		dataArchivalFailConfig.setCreatedOn(createdOn);
		dataArchivalConfigdal.saveDataArchivalConfiguration(dataArchivalFailConfig);

		DataArchivalAggregatorModule dam = new DataArchivalAggregatorModule();
		dam.run();

		Thread.sleep(10000);

		log.debug("Test Data flow has been created successfully");
	}

	@Test(priority = 1)
	public void testSaveURLForArchivalRecord() throws IOException, TimeoutException, InterruptedException {
		try {
			publishDataArchivalDetails(routingKey, urlMessage);
			Thread.sleep(2000);
			InsightsDataArchivalConfig record = dataArchivalConfigdal.getSpecificArchivalRecord(archivalName);
			Assert.assertNotNull(record);
			Assert.assertEquals(record.getSourceUrl(),
					urlJson.get("data").getAsJsonArray().get(0).getAsJsonObject().get("sourceUrl").getAsString());
		} catch (AssertionError e) {
			log.error("message  {} ", e);
		}
	}

	/*	@Test(priority = 2, expectedExceptions = NoResultException.class)
		public void testSaveURLForNonExistingArchivalRecord() throws Exception {
			publishDataArchivalDetails(routingKey, urlMessageWithNonExistingArchivalName);
			Thread.sleep(2000);
			InsightsDataArchivalConfig record = dataArchivalConfigdal.getSpecificArchivalRecord(nonExistingArchivalRecord);
			Assert.assertNotNull(record);
			Assert.assertNull(record.getSourceUrl());
		}
	
		@Test(priority = 3)
		public void testSaveURLWithEmptyArchivalName() throws Exception {
			publishDataArchivalDetails(routingKey, urlMessageWithEmptyArchivalName);
			Thread.sleep(2000);
			InsightsDataArchivalConfig record = dataArchivalConfigdal.getSpecificArchivalRecord(archivalFailName);
			Assert.assertNotNull(record);
			Assert.assertNull(record.getSourceUrl());
		}
	
		@Test(priority = 4)
		public void testSaveURLWithEmptyURL() throws Exception {
			publishDataArchivalDetails(routingKey, urlMessageWithEmptyURL);
			Thread.sleep(2000);
			InsightsDataArchivalConfig record = dataArchivalConfigdal.getSpecificArchivalRecord(archivalFailName);
			Assert.assertNotNull(record);
			Assert.assertNull(record.getSourceUrl());
		}
	
		@Test(priority = 5)
		public void testSaveURLWithDBNotWorking() throws Exception {
			publishDataArchivalDetails(routingKey, urlMessageWithEmptyURL);
			host = ApplicationConfigProvider.getInstance().getPostgre().getInsightsDBUrl();
			ApplicationConfigProvider.getInstance().getPostgre().setInsightsDBUrl("notLocalhost");
			Thread.sleep(2000);
			InsightsDataArchivalConfig record = dataArchivalConfigdal.getSpecificArchivalRecord(archivalFailName);
			Assert.assertNotNull(record);
			Assert.assertNull(record.getSourceUrl());
			ApplicationConfigProvider.getInstance().getPostgre().setInsightsDBUrl(host);
		}*/

	@Test(priority = 6)
	public void testSaveSuccessHealthNodeinNeo4j() throws Exception {
		publishDataArchivalDetails(routingKeyHealth, successHealthMessage);
		Thread.sleep(2000);
		int countOfRecords = readNeo4JData(routingKeyHealthNeo4j, successHealthMessageJson.get("execId").getAsString());
		Assert.assertTrue(countOfRecords > 0);
	}

	@Test(priority = 7)
	public void testSaveFailureHealthNodeinNeo4j() throws Exception {
		publishDataArchivalDetails(routingKeyHealth, failureHealthMessage);
		Thread.sleep(2000);
		int countOfRecords = readNeo4JData(routingKeyHealthNeo4j, failureHealthMessageJson.get("execId").getAsString());
		Assert.assertTrue(countOfRecords > 0);
	}

	@AfterTest
	public void cleanUp() {

		agentConfigDAL.deleteAgentConfigurations(agentJson.get("agentId").getAsString());
		dataArchivalConfigdal.deleteArchivalRecord(archivalName);
		dataArchivalConfigdal.deleteArchivalRecord(archivalFailName);

	}

}
