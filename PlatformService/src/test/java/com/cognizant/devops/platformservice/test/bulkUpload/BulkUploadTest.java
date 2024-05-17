/*******************************************************************************
 *  * Copyright 2017 Cognizant Technology Solutions
 *  * 
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  * use this file except in compliance with the License.  You may obtain a copy
 *  * of the License at
 *  * 
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  * 
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  * License for the specific language governing permissions and limitations under
 *  * the License.
 *******************************************************************************/

package com.cognizant.devops.platformservice.test.bulkUpload;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.multipart.MultipartFile;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.bulkupload.controller.InsightsBulkUpload;
import com.cognizant.devops.platformservice.rest.filemanagement.service.FileManagementServiceImpl;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Test
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
@WebAppConfiguration
public class BulkUploadTest extends BulkUploadTestData {

	@Autowired
	FileManagementServiceImpl fileManagementService;

	boolean isDeleteFile = false;
	private static Logger log = LogManager.getLogger(BulkUploadTest.class);

	@Autowired
	InsightsBulkUpload insightsBulkUpload;

	@BeforeClass
	public void onInit() {
		try (FileInputStream input = new FileInputStream(toolDetailsFile)) {
			MultipartFile multipartToolDetailsFile = new MockMultipartFile("file", toolDetailsFile.getName(),
					"text/plain", IOUtils.toByteArray(input));
			String response = fileManagementService.uploadConfigurationFile(multipartToolDetailsFile,
					toolDetailFileName, "JSON", "TOOLDETAIL", false);
			if (response.equals("File uploaded"))
				isDeleteFile = true;
		} catch (Exception e) {
			log.error("File already exists");
		}
	}

	@Test(priority = 1)
	public void testGetToolDetailJson() throws InsightsCustomException {

		JsonObject response = insightsBulkUpload.getToolJson();

		JsonArray jsonArr = response.get("data").getAsJsonArray();
		JsonObject jsonElement = jsonArr.get(0).getAsJsonObject();
		String toolName = jsonElement.get("toolName").getAsString();
		String label = jsonElement.get("label").getAsString();

		Assert.assertNotNull(response.get("data"));
		Assert.assertEquals("GIT", toolName);
		Assert.assertEquals("SCM:GIT:DATA", label);

	}

	@Test(priority = 2)
	public void testUploadDataInDatabase() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(file);
		MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain",
				IOUtils.toByteArray(input));
		String expectedOutcome = "success";

		JsonObject response = null;
		try {
			response = insightsBulkUpload.uploadToolData(multipartFile, toolName, label, insightTimeField,
					nullInsightTimeFormat);
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		String actual = response.get("status").toString().replaceAll("\"", "");

		Assert.assertEquals(actual, expectedOutcome);
		Assert.assertFalse(multipartFile.isEmpty());
		Assert.assertTrue(multipartFile.getSize() < filesizeMaxValue);

	}

	@Test(priority = 3)
	public void testUploadDataWithVariedEpochTimesInDatabase() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(fileWithVariedEpochTimes);
		MultipartFile multipartFile = new MockMultipartFile("file", fileWithVariedEpochTimes.getName(), "text/plain",
				IOUtils.toByteArray(input));
		String expectedOutcome = "success";

		JsonObject response = null;
		try {
			response = insightsBulkUpload.uploadToolData(multipartFile, toolName, label, insightTimeField,
					nullInsightTimeFormat);
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		String actual = response.get("status").toString().replaceAll("\"", "");

		Assert.assertEquals(actual, expectedOutcome);
		Assert.assertFalse(multipartFile.isEmpty());
		Assert.assertTrue(multipartFile.getSize() < filesizeMaxValue);
	}

	@Test(priority = 4)
	public void testUploadDataWithZFormatEpochTimesInDatabase() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(fileWithZFormatEpochTimes);
		MultipartFile multipartFile = new MockMultipartFile("file", fileWithZFormatEpochTimes.getName(), "text/plain",
				IOUtils.toByteArray(input));

		String expectedOutcome = "success";

		JsonObject response = null;
		try {
			response = insightsBulkUpload.uploadToolData(multipartFile, toolName, label, insightTimeField,
					insightTimeZFormat);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		String actual = response.get("status").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);
		Assert.assertFalse(multipartFile.isEmpty());
		Assert.assertTrue(multipartFile.getSize() < filesizeMaxValue);
	}

	@Test(priority = 5)
	public void testUploadDataWithTimeZoneFormatEpochTimesInDatabase() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(fileWithTimeZoneFormatEpochTimes);
		MultipartFile multipartFile = new MockMultipartFile("file", fileWithTimeZoneFormatEpochTimes.getName(),
				"text/plain", IOUtils.toByteArray(input));
		String expectedOutcome = "success";

		JsonObject response = null;
		try {
			response = insightsBulkUpload.uploadToolData(multipartFile, toolName, label, insightTimeField,
					insightTimeWithTimeZoneFormat);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		String actual = response.get("status").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);
		Assert.assertFalse(multipartFile.isEmpty());
		Assert.assertTrue(multipartFile.getSize() < filesizeMaxValue);
	}

	@Test(priority = 6)
	public void testUploadDataWithNullEpochTimesInDatabase() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(fileWithNullEpochTime);
		MultipartFile multipartFile = new MockMultipartFile("file", fileWithNullEpochTime.getName(), "text/plain",
				IOUtils.toByteArray(input));
		String expectedOutcome = "failure";

		JsonObject response = null;
		try {
			response = insightsBulkUpload.uploadToolData(multipartFile, toolName, label, insightTimeField,
					insightTimeWithTimeZoneFormat);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		String actual = response.get("status").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);
		Assert.assertFalse(multipartFile.isEmpty());
		Assert.assertTrue(multipartFile.getSize() < filesizeMaxValue);
	}

	@Test(priority = 7)
	public void testUploadDataWithWrongInsightTimeFieldInDatabase() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(fileWithNullEpochTime);
		MultipartFile multipartFile = new MockMultipartFile("file", fileWithNullEpochTime.getName(), "text/plain",
				IOUtils.toByteArray(input));

		String expectedOutcome = "failure";
		String expectedOutcomeMessage = "Insights Time Field not present in csv file";

		JsonObject response = null;
		try {
			response = insightsBulkUpload.uploadToolData(multipartFile, toolName, label, wrongInsightTimeField,
					insightTimeWithTimeZoneFormat);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		String actual = response.get("status").toString().replaceAll("\"", "");
		String actualMessage = response.get("message").toString().replaceAll("\"", "");

		Assert.assertEquals(actual, expectedOutcome);
		Assert.assertEquals(actualMessage, expectedOutcomeMessage);
		Assert.assertFalse(multipartFile.isEmpty());
		Assert.assertTrue(multipartFile.getSize() < filesizeMaxValue);
	}

	@Test(priority = 8)
	public void testUploadDataWithWrongInsightTimeFormatInDatabase() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(fileWithNullEpochTime);
		MultipartFile multipartFile = new MockMultipartFile("file", fileWithNullEpochTime.getName(), "text/plain",
				IOUtils.toByteArray(input));
		String expectedOutcome = "failure";

		JsonObject response = null;
		try {
			response = insightsBulkUpload.uploadToolData(multipartFile, toolName, label, insightTimeField,
					wrongInsightTimeFormat);
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		String actual = response.get("status").toString().replaceAll("\"", "");

		Assert.assertEquals(actual, expectedOutcome);
		Assert.assertFalse(multipartFile.isEmpty());
		Assert.assertTrue(multipartFile.getSize() < filesizeMaxValue);

	}

	@Test(priority = 9)
	public void testUploadDataWithNullInsightTimeFieldInDatabase() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(fileWithVariedEpochTimes);
		MultipartFile multipartFile = new MockMultipartFile("file", fileWithVariedEpochTimes.getName(), "text/plain",
				IOUtils.toByteArray(input));
		String expectedOutcome = "failure";
		String expectedOutcomeMessage = "Insights Time Field not present in csv file";

		JsonObject response = null;
		try {
			response = insightsBulkUpload.uploadToolData(multipartFile, toolName, label, nullInsightTimeField,
					nullInsightTimeFormat);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		String actual = response.get("status").toString().replaceAll("\"", "");
		String actualMessage = response.get("message").toString().replaceAll("\"", "");

		Assert.assertEquals(actual, expectedOutcome);
		Assert.assertEquals(actualMessage, expectedOutcomeMessage);
		Assert.assertFalse(multipartFile.isEmpty());
		Assert.assertTrue(multipartFile.getSize() < filesizeMaxValue);
	}

	@Test(priority = 10)
	public void testUploadDataWithNullLabelInDatabase() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(fileWithVariedEpochTimes);
		MultipartFile multipartFile = new MockMultipartFile("file", fileWithVariedEpochTimes.getName(), "text/plain",
				IOUtils.toByteArray(input));

		String expectedOutcome = "failure";

		JsonObject response = null;
		try {
			response = insightsBulkUpload.uploadToolData(multipartFile, toolName, nullLabel, insightTimeField,
					insightTimeZFormat);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		String actual = response.get("status").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);
		Assert.assertFalse(multipartFile.isEmpty());
		Assert.assertTrue(multipartFile.getSize() < filesizeMaxValue);

	}

	@Test(priority = 11)
	public void testFileSizeException() throws InsightsCustomException, IOException {

		FileInputStream input = new FileInputStream(fileSize);
		MultipartFile multipartFile = new MockMultipartFile("file", fileSize.getName(), "text/plain",
				IOUtils.toByteArray(input));
		String expectedOutcome = "failure";
		String expectedOutcomeMessage = "File is exceeding the size.";

		JsonObject response = null;
		try {
			response = insightsBulkUpload.uploadToolData(multipartFile, toolName, label, insightTimeField,
					nullInsightTimeFormat);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		String actual = response.get("status").toString().replaceAll("\"", "");
		String actualMessage = response.get("message").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);
		Assert.assertEquals(actualMessage, expectedOutcomeMessage);
	}

	@Test(priority = 12)
	public void testIncorrectFileException() throws InsightsCustomException, IOException {

		FileInputStream input = new FileInputStream(incorrectDataFile);
		MultipartFile multipartFile = new MockMultipartFile("file", incorrectDataFile.getName(), "text/plain",
				IOUtils.toByteArray(input));
		String expectedOutcome = "failure";

		JsonObject response = null;
		try {
			response = insightsBulkUpload.uploadToolData(multipartFile, toolName, label, insightTimeField,
					insightTimeZFormat);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		String actual = response.get("status").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);
	}

	@Test(priority = 13)
	public void testFileFormatException() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(fileFormat);
		MultipartFile multipartFile = new MockMultipartFile("file", fileFormat.getName(), "text/plain",
				IOUtils.toByteArray(input));
		String expectedOutcome = "failure";
		String expectedOutcomeMessage = "Invalid file format.";

		JsonObject response = null;
		try {
			response = insightsBulkUpload.uploadToolData(multipartFile, toolName, label, insightTimeField,
					nullInsightTimeFormat);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		String actual = response.get("status").toString().replaceAll("\"", "");
		String actualMessage = response.get("message").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);
		Assert.assertEquals(actualMessage, expectedOutcomeMessage);

	}

	@Test(priority = 14)
	public void testFileWithNumericValues() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(fileWithNumericValues);
		MultipartFile multipartFile = new MockMultipartFile("file", fileWithNumericValues.getName(), "text/plain",
				IOUtils.toByteArray(input));
		String expectedOutcome = "success";

		JsonObject response = null;
		try {
			response = insightsBulkUpload.uploadToolData(multipartFile, toolName, labelForNumericCheck,
					fileWithNumericValues_insighstimeField, nullInsightTimeFormat);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		String actual = response.get("status").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);
		Assert.assertFalse(multipartFile.isEmpty());
		Assert.assertTrue(multipartFile.getSize() < filesizeMaxValue);

	}

	@Test(priority = 15)
	public void testUploadDataWithoutTimesZoneFormatInDatabase() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(fileWithoutTimeZoneFormatEpochTimes);
		MultipartFile multipartFile = new MockMultipartFile("file", fileWithoutTimeZoneFormatEpochTimes.getName(),
				"text/plain", IOUtils.toByteArray(input));
		String expectedOutcome = "success";

		JsonObject response = null;
		try {
			response = insightsBulkUpload.uploadToolData(multipartFile, toolName, label, insightTimeField,
					insightTimeWithoutTimeZoneFormat);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		String actual = response.get("status").toString().replaceAll("\"", "");
		Assert.assertEquals(actual, expectedOutcome);
		Assert.assertFalse(multipartFile.isEmpty());
		Assert.assertTrue(multipartFile.getSize() < filesizeMaxValue);

	}

	@AfterTest
	public void onDelete() {
		try {
			if (isDeleteFile) {
				fileManagementService.deleteConfigurationFile(toolDetailFileName);
			}
		} catch (Exception e) {
			log.error("File already exists");
		}
	}

}
