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
package com.cognizant.devops.platformservice.test.offlineDataProcessing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.multipart.MultipartFile;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.offlineDataProcessing.InsightsOfflineConfig;
import com.cognizant.devops.platformdal.offlineDataProcessing.InsightsOfflineConfigDAL;
import com.cognizant.devops.platformservice.offlinedataprocessing.controller.InsightsOfflineDataProcessingController;
import com.cognizant.devops.platformservice.offlinedataprocessing.service.OfflineDataProcessingServiceImpl;
import com.cognizant.devops.platformservice.test.testngInitializer.TestngInitializerTest;
import com.google.gson.JsonObject;

@Test
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class OfflineDataProcessingTest extends OfflineDataProcessingTestData {

	@Autowired
	InsightsOfflineDataProcessingController offlineDataProcessingController;
	@Autowired
	OfflineDataProcessingServiceImpl offlineDataProcessingService;
	InsightsOfflineConfigDAL offlineConfigDAL = new InsightsOfflineConfigDAL();
	JsonObject testData = new JsonObject();
	private static final Logger log = LogManager.getLogger(OfflineDataProcessingTest.class);

	@BeforeClass
	public void prepareData() throws InsightsCustomException {
		try {
			String path = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator
					+ TestngInitializerTest.TESTNG_TESTDATA + File.separator
					+ TestngInitializerTest.TESTNG_PLATFORMSERVICE + File.separator + "OfflineDataProcessing.json";
			testData = JsonUtils.getJsonData(path).getAsJsonObject();
		} catch (Exception e) {
			log.error("message", e);
		}

	}

	@Test(priority = 1)
	public void testUploadOfflineDefinition() throws InsightsCustomException, IOException {
		try (FileInputStream input = new FileInputStream(offlineFile)) {
			String expectedOutcome = "All Offline Data records are inserted successfully. ";
			MultipartFile multipartFile = new MockMultipartFile("file", offlineFile.getName(), "text/plain",
					IOUtils.toByteArray(input));
			JsonObject response = offlineDataProcessingController.saveBulkOfflineData(multipartFile);
			String actualOutcome = response.get("data").getAsString().replace("\"", "");
			Assert.assertEquals(actualOutcome, expectedOutcome);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 2)
	public void testSaveOfflineDefinition() throws InsightsCustomException {
		try {
			String qName = testData.get("saveOfflineDataConfig").getAsJsonObject().get("queryName").getAsString();
			JsonObject response = offlineDataProcessingController
					.saveOfflineDefinition(testData.get("saveOfflineDataConfig").toString());
			InsightsOfflineConfig config = offlineConfigDAL.getOfflineDataConfig(qName);
			Assert.assertEquals(qName, config.getQueryName());
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 3)
	public void testSaveOfflineDefinitionWithDuplicateQueryName() throws InsightsCustomException {
		try {
			JsonObject response = offlineDataProcessingController
					.saveOfflineDefinition(testData.get("saveOfflineDataConfig").toString());
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.FAILURE);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 4)
	public void testUpdateOfflineDefinition() throws InsightsCustomException {
		try {
			JsonObject response = offlineDataProcessingController
					.updateOfflineDefinition(testData.get("updateOfflineDataConfig").toString());
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 5)
	public void testUpdateOutcomeConfigStatus() throws InsightsCustomException {
		try {
			String qName = testData.get("saveOfflineDataConfig").getAsJsonObject().get("queryName").getAsString();
			boolean isActive = testData.get("statusConfig").getAsJsonObject().get("isActive").getAsBoolean();
			JsonObject response = offlineDataProcessingController
					.updateOutcomeConfigStatus(testData.get("statusConfig").toString());
			InsightsOfflineConfig config = offlineConfigDAL.getOfflineDataConfig(qName);
			Assert.assertEquals(isActive, config.getIsActive());
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 6)
	public void testGetAllOfflineDataList() throws InsightsCustomException {
		try {
			JsonObject response = offlineDataProcessingController.getAllOfflineDataList();
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 7)
	public void testDeleteOfflineData() throws InsightsCustomException {
		try {
			String offlineConfig = testData.get("saveOfflineDataConfig").toString();
			JsonObject response = offlineDataProcessingController.deleteOfflineDefinition(offlineConfig);
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 8)
	public void testSaveOfflineDefinitionWithEmptyToolName() throws InsightsCustomException {
		try {
			String response = offlineDataProcessingService
					.saveBulkOfflineDefinition(testData.get("saveOfflineWithEmptyToolName").getAsJsonArray());
			String expectedResponse = "All Offline Data records are not inserted, Please check Platform Service log for more detail. ";
			Assert.assertEquals(expectedResponse, response);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 9)
	public void testSaveOfflineDefinitionWithEmptyQueryName() throws InsightsCustomException {
		try {
			String response = offlineDataProcessingService
					.saveBulkOfflineDefinition(testData.get("saveOfflineWithEmptyQueryName").getAsJsonArray());
			String expectedResponse = "All Offline Data records are not inserted, Please check Platform Service log for more detail. ";
			Assert.assertEquals(expectedResponse, response);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 10)
	public void testSaveOfflineDefinitionWithEmptyCypherQuery() throws InsightsCustomException {
		try {
			String response = offlineDataProcessingService
					.saveBulkOfflineDefinition(testData.get("saveOfflineWithEmptyCypherQuery").getAsJsonArray());
			String expectedResponse = "All Offline Data records are not inserted, Please check Platform Service log for more detail. ";
			Assert.assertEquals(expectedResponse, response);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
}
