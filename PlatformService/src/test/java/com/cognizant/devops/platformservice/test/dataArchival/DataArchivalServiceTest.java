/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
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
package com.cognizant.devops.platformservice.test.dataArchival;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.agentConfig.AgentConfig;
import com.cognizant.devops.platformdal.agentConfig.AgentConfigDAL;
import com.cognizant.devops.platformdal.dataArchivalConfig.DataArchivalConfigDal;
import com.cognizant.devops.platformdal.dataArchivalConfig.InsightsDataArchivalConfig;
import com.cognizant.devops.platformservice.agentmanagement.service.AgentManagementServiceImpl;
import com.cognizant.devops.platformservice.dataarchival.service.DataArchivalServiceImpl;

public class DataArchivalServiceTest extends DataArchivalServiceData {

	private static final Logger log = LogManager.getLogger(DataArchivalServiceTest.class);

	DataArchivalServiceImpl dataArchivalServiceImpl = new DataArchivalServiceImpl();
	DataArchivalConfigDal dataArchivalConfigDal = new DataArchivalConfigDal();
	AgentManagementServiceImpl agentManagementServiceImpl = new AgentManagementServiceImpl();
	AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
	String host = null;

	@BeforeTest
	public void prepareData() throws InsightsCustomException {
		ApplicationConfigCache.loadConfigCache();
		// register agent in DB
		try {
			Boolean status = agentConfigDAL.saveAgentConfigFromUI(agentJson.get("agentId").getAsString(),
					agentJson.get("toolCategory").getAsString(), agentJson.get("labelName").getAsString(),
					agentJson.get("toolName").getAsString(), agentJson, agentJson.get("agentVersion").getAsString(),
					agentJson.get("osversion").getAsString(), updateDate, vault);
		} catch (Exception e) {
			log.error("message", e);
		}

	}

	@Test(priority = 1)
	public void testSaveArchivalRecord() throws InsightsCustomException {
		try {
			Long expectedCreatedOn = InsightsUtils.getTodayTime() / 1000;
			Long expectedExpiredDate = getExpiryDate(expectedCreatedOn, expectedDaysToRetain);
			String status = dataArchivalServiceImpl.saveDataArchivalDetails(saveArchivalRecordsJson);
			InsightsDataArchivalConfig dataArchivalConfig = dataArchivalConfigDal
					.getSpecificArchivalRecord(saveArchivalRecordsJson.get("archivalName").getAsString());
			Assert.assertEquals(status, "SUCCESS");
			Assert.assertEquals("Start Date assertion failed", expectedStartDate, dataArchivalConfig.getStartDate());
			Assert.assertEquals("End Date assertion failed", expectedEndDate, dataArchivalConfig.getEndDate());
			Assert.assertEquals("Expiry Date assertion failed", expectedExpiredDate,
					dataArchivalConfig.getExpiryDate());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 2)
	public void testSaveArchivalRecordForDeleting() throws InsightsCustomException {
		try {
			Long expectedCreatedOnForDeleteCase = InsightsUtils.getTodayTime() / 1000;
			Long expectedExpiryDateForDeleteCase = getExpiryDate(expectedCreatedOnForDeleteCase,
					expectedDaysToRetainForDeleteCase);
			String status = dataArchivalServiceImpl.saveDataArchivalDetails(saveArchivalRecordsForDeleteCaseJson);
			InsightsDataArchivalConfig dataArchivalConfig = dataArchivalConfigDal
					.getSpecificArchivalRecord(saveArchivalRecordsForDeleteCaseJson.get("archivalName").getAsString());
			Assert.assertEquals(status, "SUCCESS");
			Assert.assertEquals("Start Date assertion failed", expectedStartDateForDeleteCase,
					dataArchivalConfig.getStartDate());
			Assert.assertEquals("End Date assertion failed", expectedEndDateForDeleteCase,
					dataArchivalConfig.getEndDate());
			Assert.assertEquals("Expiry Date assertion failed", expectedExpiryDateForDeleteCase,
					dataArchivalConfig.getExpiryDate());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 3, expectedExceptions = InsightsCustomException.class)
	public void testSaveDuplicateArchivalRecord() throws InsightsCustomException {
		String status = dataArchivalServiceImpl.saveDataArchivalDetails(saveArchivalRecordsJson);
	}

	@Test(priority = 4, expectedExceptions = InsightsCustomException.class)
	public void testSaveArchivalRecordWithoutArchivalName() throws InsightsCustomException {
		String status = dataArchivalServiceImpl.saveDataArchivalDetails(saveArchivalRecordsWithoutNameJson);
	}

	// @Test(priority = 4,expectedExceptions = InsightsCustomException.class)
	public void testSaveArchivalRecordWithIncompleteData() throws InsightsCustomException {
		String status = dataArchivalServiceImpl.saveDataArchivalDetails(saveArchivalRecordsIncompleteDataJson);
	}

	@Test(priority = 5, expectedExceptions = InsightsCustomException.class)
	public void testSaveArchivalRecordWithIncorrectNameData() throws InsightsCustomException {
		String status = dataArchivalServiceImpl.saveDataArchivalDetails(saveArchivalRecordsIncorrectNameDataJson);
	}

	@Test(priority = 6, expectedExceptions = InsightsCustomException.class)
	public void testSaveArchivalRecordWithIncorrectDateFormat() throws InsightsCustomException {
		String status = dataArchivalServiceImpl.saveDataArchivalDetails(saveArchivalRecordsIncorrectDateDataJson);
	}

	@Test(priority = 7, expectedExceptions = InsightsCustomException.class)
	public void testSaveArchivalRecordWithIncorrectDaysToRetain() throws InsightsCustomException {
		String status = dataArchivalServiceImpl
				.saveDataArchivalDetails(saveArchivalRecordsIncorrectDaysToRetainDataJson);
	}

	@Test(priority = 8, expectedExceptions = InsightsCustomException.class)
	public void testSaveArchivalRecordWithLargeDaysToRetain() throws InsightsCustomException {
		String status = dataArchivalServiceImpl.saveDataArchivalDetails(saveArchivalRecordsLargeDaysToRetainDataJson);
	}

	@Test(priority = 9, expectedExceptions = InsightsCustomException.class)
	public void testsaveArchivalRecordsWithStartDateGreaterThanEndDate() throws InsightsCustomException {
		String status = dataArchivalServiceImpl
				.saveDataArchivalDetails(saveArchivalRecordsWithStartDateGreaterThanEndDateDataJson);
	}

	/*
	 * @Test(priority = 10, expectedExceptions = InsightsCustomException.class)
	 * public void testsaveArchivalRecordsWithRabbitMqDown() throws
	 * InsightsCustomException { host =
	 * ApplicationConfigProvider.getInstance().getMessageQueue().getHost();
	 * ApplicationConfigProvider.getInstance().getMessageQueue().setHost(
	 * "notLocalhost"); String status = dataArchivalServiceImpl
	 * .saveDataArchivalDetails(saveArchivalRecordsJson);
	 * ApplicationConfigProvider.getInstance().getMessageQueue().setHost(host); }
	 */

	@Test(priority = 11, expectedExceptions = InsightsCustomException.class)
	public void testsaveArchivalRecordsWithNoDataArchivalAgent() throws InsightsCustomException {
		List<AgentConfig> agentConfigs = agentConfigDAL
				.deleteAgentConfigurations(agentJson.get("agentId").getAsString());
		String status = dataArchivalServiceImpl.saveDataArchivalDetails(saveArchivalRecordsJson);
	}

	@Test(priority = 12)
	public void testUpdateArchivalSourceURL() throws InsightsCustomException {
		try {
			Boolean status = dataArchivalServiceImpl.updateArchivalSourceUrl(updateSourceURLJson);
			InsightsDataArchivalConfig dataArchivalConfig = dataArchivalConfigDal
					.getSpecificArchivalRecord(updateSourceURLJson.get("archivalName").getAsString());
			Assert.assertTrue(status);
			Assert.assertEquals(dataArchivalConfig.getSourceUrl(), updateSourceURLJson.get("sourceUrl").getAsString());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 13, expectedExceptions = InsightsCustomException.class)
	public void testUpdateArchivalSourceURLWithEmptyArchivalName() throws InsightsCustomException {
		Boolean status = dataArchivalServiceImpl.updateArchivalSourceUrl(updateSourceURLJsonWithEmptyName);
	}

	@Test(priority = 14)
	public void testInActivateArchivalRecord() throws InsightsCustomException {
		try {
			Boolean status = dataArchivalServiceImpl
					.inactivateArchivalRecord(saveArchivalRecordsJson.get("archivalName").getAsString());
			InsightsDataArchivalConfig dataArchivalConfig = dataArchivalConfigDal
					.getSpecificArchivalRecord(updateSourceURLJson.get("archivalName").getAsString());
			Assert.assertTrue(status);
			Assert.assertEquals(dataArchivalConfig.getStatus(), "INACTIVE");
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 15, expectedExceptions = InsightsCustomException.class)
	public void testInActivateArchivalRecordWithWrongName() throws InsightsCustomException {
		Boolean status = dataArchivalServiceImpl.inactivateArchivalRecord(wrongRecordName);
	}

	@Test(priority = 16)
	public void testActivateArchivalRecord() throws InsightsCustomException {
		try {
			Boolean status = dataArchivalServiceImpl
					.activateArchivalRecord(saveArchivalRecordsJson.get("archivalName").getAsString());
			InsightsDataArchivalConfig dataArchivalConfig = dataArchivalConfigDal
					.getSpecificArchivalRecord(updateSourceURLJson.get("archivalName").getAsString());
			Assert.assertTrue(status);
			Assert.assertEquals(dataArchivalConfig.getStatus(), "ACTIVE");
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 17, expectedExceptions = InsightsCustomException.class)
	public void testActivateArchivalRecordWithWrongName() throws InsightsCustomException {
		Boolean status = dataArchivalServiceImpl.activateArchivalRecord(wrongRecordName);
	}

	@Test(priority = 18)
	public void testGetActivateArchivalRecords() throws InsightsCustomException {
		try {
			List<InsightsDataArchivalConfig> activeRecords = dataArchivalServiceImpl.getActiveArchivalList();
			Assert.assertNotNull(activeRecords);
			Assert.assertTrue(activeRecords.size() > 0);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 19)
	public void testGetAllArchivalRecords() throws InsightsCustomException {
		List<InsightsDataArchivalConfig> activeRecords = dataArchivalServiceImpl.getAllArchivalRecord();
		Assert.assertNotNull(activeRecords);
		Assert.assertTrue(activeRecords.size() > 0);
	}

	@Test(priority = 20)
	public void testDeleteArchivalRecords() throws InsightsCustomException {
		try {
			Boolean status = dataArchivalServiceImpl
					.deleteArchivalRecord(saveArchivalRecordsForDeleteCaseJson.get("archivalName").getAsString());
			Assert.assertTrue(status);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 21, expectedExceptions = InsightsCustomException.class)
	public void testDeleteArchivalRecordsWithWrongName() throws InsightsCustomException {
		Boolean status = dataArchivalServiceImpl.deleteArchivalRecord(wrongRecordName);
	}

	@AfterTest
	public void cleanUp() throws InsightsCustomException {
		try {
			Boolean statusAfterInActivating = dataArchivalServiceImpl
					.inactivateArchivalRecord(saveArchivalRecordsJson.get("archivalName").getAsString());
		} catch (Exception e) {
			log.error("Error cleaning data in  DataArchivalServiceTest statusInactive record ", e);
		}
		try {
			Boolean statusAfterDeleting = dataArchivalServiceImpl
					.deleteArchivalRecord(saveArchivalRecordsJson.get("archivalName").getAsString());
		} catch (Exception e) {
			log.error("Error cleaning data in DataArchivalServiceTest archival record ", e);
		}
	}
}
