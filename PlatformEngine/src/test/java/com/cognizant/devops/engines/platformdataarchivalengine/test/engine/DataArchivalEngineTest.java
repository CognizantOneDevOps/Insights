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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.cognizant.devops.engines.platformdataarchivalengine.modules.aggregator.DataArchivalAggregatorModule;
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

	@BeforeClass
	public void onInit() throws IOException, TimeoutException, InsightsCustomException, InterruptedException {

		// save data archival agent
		try {
			agentConfigDAL.saveAgentConfigFromUI(agentJson.get("agentId").getAsString(),
					agentJson.get("toolCategory").getAsString(), agentJson.get("labelName").getAsString(),
					agentJson.get("toolName").getAsString(), agentJson, agentJson.get("agentVersion").getAsString(),
					agentJson.get("osversion").getAsString(), updateDate, vault,false);
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

		DataArchivalAggregatorModule dam = new DataArchivalAggregatorModule();
		dam.run();

		Thread.sleep(1000);

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
			Assert.assertEquals(record.getBoltPort(),
					urlJson.get("data").getAsJsonArray().get(0).getAsJsonObject().get("boltPort").getAsInt());
		} catch (AssertionError e) {
			log.error("message  {} ", e);
		}
	}

	@Test(priority = 2)
	public void testSaveURLWithEmptyArchivalName() throws Exception {
		try {
			publishDataArchivalDetails(routingKey, urlMessageWithEmptyArchivalName);
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
			publishDataArchivalDetails(routingKey, urlMessageWithEmptyURL);
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
			publishDataArchivalDetails(routingKey, removeContainer);
			Thread.sleep(2000);
			InsightsDataArchivalConfig record = dataArchivalConfigdal.getSpecificArchivalRecord(archivalName);
			Assert.assertNotNull(record);
			Assert.assertEquals(record.getStatus(), "TERMINATED");
		} catch (AssertionError e) {
			log.error("message  {} ", e);
		}
	}

	@Test(priority = 5)
	public void testSaveURLWithEmptyContainerId() throws Exception {
		try {
			publishDataArchivalDetails(routingKey, urlMessageWithEmptyContainer);
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
			publishDataArchivalDetails(routingKey, urlMessageWithEmptyURL);
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
			publishDataArchivalDetails(routingKeyHealth, successHealthMessage);
			Thread.sleep(2000);
			int countOfRecords = readNeo4JData(routingKeyHealthNeo4j,
					successHealthMessageJson.get("execId").getAsString());
			Assert.assertTrue(countOfRecords > 0);
		} catch (AssertionError e) {
			log.error("message  {} ", e);
		}
	}

	@Test(priority = 8)
	public void testSaveFailureHealthNodeinNeo4j() throws Exception {
		try {
			publishDataArchivalDetails(routingKeyHealth, failureHealthMessage);
			Thread.sleep(2000);
			int countOfRecords = readNeo4JData(routingKeyHealthNeo4j,
					failureHealthMessageJson.get("execId").getAsString());
			Assert.assertTrue(countOfRecords > 0);
		} catch (AssertionError e) {
			log.error("message  {} ", e);
		}
	}

	@AfterClass
	public void cleanUp() {
		try {
			agentConfigDAL.deleteAgentConfigurations(agentJson.get("agentId").getAsString());
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
