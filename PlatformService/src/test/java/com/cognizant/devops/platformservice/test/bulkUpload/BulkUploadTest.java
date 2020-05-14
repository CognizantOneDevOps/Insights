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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.multipart.MultipartFile;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.bulkupload.service.BulkUploadService;

@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class BulkUploadTest extends BulkUploadTestData {

	public static final BulkUploadService bulkUploadService = new BulkUploadService();
	public static final BulkUploadTestData bulkUploadTestData = new BulkUploadTestData();

	@Test(priority = 1)
	public void testGetToolDetailJson() throws InsightsCustomException {
		String response = bulkUploadService.getToolDetailJson().toString();
		Assert.assertNotNull(response);
		Assert.assertTrue(response.length() > 0);

	}

	@Test(priority = 2)
	public void testUploadDataInDatabase() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(file);
		MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain",
				IOUtils.toByteArray(input));
		boolean response = bulkUploadService.uploadDataInDatabase(multipartFile, bulkUploadTestData.toolName,
				bulkUploadTestData.label, bulkUploadTestData.insightTimeField,
				bulkUploadTestData.nullInsightTimeFormat);
		Assert.assertEquals(response, true);
		Assert.assertFalse(multipartFile.isEmpty());
		Assert.assertTrue(multipartFile.getSize() < filesizeMaxValue);
	}

	@Test(priority = 3)
	public void testUploadDataWithVariedEpochTimesInDatabase() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(fileWithVariedEpochTimes);
		MultipartFile multipartFile = new MockMultipartFile("file", fileWithVariedEpochTimes.getName(), "text/plain",
				IOUtils.toByteArray(input));
		boolean response = bulkUploadService.uploadDataInDatabase(multipartFile, bulkUploadTestData.toolName,
				bulkUploadTestData.label, bulkUploadTestData.insightTimeField,
				bulkUploadTestData.nullInsightTimeFormat);
		Assert.assertEquals(response, true);
		Assert.assertFalse(multipartFile.isEmpty());
		Assert.assertTrue(multipartFile.getSize() < filesizeMaxValue);
	}

	@Test(priority = 4)
	public void testUploadDataWithZFormatEpochTimesInDatabase() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(fileWithZFormatEpochTimes);
		MultipartFile multipartFile = new MockMultipartFile("file", fileWithZFormatEpochTimes.getName(), "text/plain",
				IOUtils.toByteArray(input));
		boolean response = bulkUploadService.uploadDataInDatabase(multipartFile, bulkUploadTestData.toolName,
				bulkUploadTestData.label, bulkUploadTestData.insightTimeField, bulkUploadTestData.insightTimeZFormat);
		Assert.assertEquals(response, true);
		Assert.assertFalse(multipartFile.isEmpty());
		Assert.assertTrue(multipartFile.getSize() < filesizeMaxValue);
	}

	@Test(priority = 5)
	public void testUploadDataWithTimeZoneFormatEpochTimesInDatabase() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(fileWithTimeZoneFormatEpochTimes);
		MultipartFile multipartFile = new MockMultipartFile("file", fileWithTimeZoneFormatEpochTimes.getName(),
				"text/plain", IOUtils.toByteArray(input));
		boolean response = bulkUploadService.uploadDataInDatabase(multipartFile, bulkUploadTestData.toolName,
				bulkUploadTestData.label, bulkUploadTestData.insightTimeField,
				bulkUploadTestData.insightTimeWithTimeZoneFormat);
		Assert.assertEquals(response, true);
		Assert.assertFalse(multipartFile.isEmpty());
		Assert.assertTrue(multipartFile.getSize() < filesizeMaxValue);
	}

	@Test(priority = 6, expectedExceptions = InsightsCustomException.class)
	public void testUploadDataWithNullEpochTimesInDatabase() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(fileWithNullEpochTime);
		MultipartFile multipartFile = new MockMultipartFile("file", fileWithNullEpochTime.getName(), "text/plain",
				IOUtils.toByteArray(input));
		boolean response = bulkUploadService.uploadDataInDatabase(multipartFile, bulkUploadTestData.toolName,
				bulkUploadTestData.label, bulkUploadTestData.insightTimeField,
				bulkUploadTestData.insightTimeWithTimeZoneFormat);
//		String expectedOutcome = "Null values in column commitTime";
//		Assert.assertEquals(response, expectedOutcome);
		Assert.assertFalse(multipartFile.isEmpty());
		Assert.assertTrue(multipartFile.getSize() < filesizeMaxValue);
	}

	@Test(priority = 7, expectedExceptions = InsightsCustomException.class)
	public void testUploadDataWithWrongInsightTimeFieldInDatabase() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(fileWithNullEpochTime);
		MultipartFile multipartFile = new MockMultipartFile("file", fileWithNullEpochTime.getName(), "text/plain",
				IOUtils.toByteArray(input));
		boolean response = bulkUploadService.uploadDataInDatabase(multipartFile, bulkUploadTestData.toolName,
				bulkUploadTestData.label, bulkUploadTestData.wrongInsightTimeField,
				bulkUploadTestData.insightTimeWithTimeZoneFormat);
		String expectedOutcome = "Insight Time Field not present in the file";
		Assert.assertEquals(response, expectedOutcome);
		Assert.assertFalse(multipartFile.isEmpty());
		Assert.assertTrue(multipartFile.getSize() < filesizeMaxValue);
	}

	@Test(priority = 8, expectedExceptions = InsightsCustomException.class)
	public void testUploadDataWithWrongInsightTimeFormatInDatabase() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(fileWithNullEpochTime);
		MultipartFile multipartFile = new MockMultipartFile("file", fileWithNullEpochTime.getName(), "text/plain",
				IOUtils.toByteArray(input));
		boolean response = bulkUploadService.uploadDataInDatabase(multipartFile, bulkUploadTestData.toolName,
				bulkUploadTestData.label, bulkUploadTestData.insightTimeField,
				bulkUploadTestData.wrongInsightTimeFormat);
		String expectedOutcome = "Illegal pattern character 'c'";
		Assert.assertEquals(response, expectedOutcome);
		Assert.assertFalse(multipartFile.isEmpty());
		Assert.assertTrue(multipartFile.getSize() < filesizeMaxValue);
	}

	@Test(priority = 9, expectedExceptions = InsightsCustomException.class)
	public void testUploadDataWithNullInsightTimeFieldInDatabase() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(fileWithVariedEpochTimes);
		MultipartFile multipartFile = new MockMultipartFile("file", fileWithVariedEpochTimes.getName(), "text/plain",
				IOUtils.toByteArray(input));
		boolean response = bulkUploadService.uploadDataInDatabase(multipartFile, bulkUploadTestData.toolName,
				bulkUploadTestData.label, bulkUploadTestData.nullInsightTimeField,
				bulkUploadTestData.nullInsightTimeFormat);
		String expectedOutcome = "Insight Time Field not present in the file";
		Assert.assertEquals(response, expectedOutcome);
		Assert.assertFalse(multipartFile.isEmpty());
		Assert.assertTrue(multipartFile.getSize() < filesizeMaxValue);
	}

	@Test(priority = 10, expectedExceptions = InsightsCustomException.class)
	public void testUploadDataWithNullLabelInDatabase() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(fileWithVariedEpochTimes);
		MultipartFile multipartFile = new MockMultipartFile("file", fileWithVariedEpochTimes.getName(), "text/plain",
				IOUtils.toByteArray(input));
		String expectedOutcome = "Label cannot be empty";
		boolean response = bulkUploadService.uploadDataInDatabase(multipartFile, bulkUploadTestData.toolName,
				bulkUploadTestData.nullLabel, bulkUploadTestData.insightTimeField,
				bulkUploadTestData.nullInsightTimeFormat);
		Assert.assertEquals(response, expectedOutcome);
		Assert.assertFalse(multipartFile.isEmpty());
		Assert.assertTrue(multipartFile.getSize() < filesizeMaxValue);
	}

	@Test(priority = 10, expectedExceptions = InsightsCustomException.class)
	public void testFileSizeException() throws InsightsCustomException, IOException {

		FileInputStream input = new FileInputStream(fileSize);
		MultipartFile multipartFile = new MockMultipartFile("file", fileSize.getName(), "text/plain",
				IOUtils.toByteArray(input));

		boolean response = bulkUploadService.uploadDataInDatabase(multipartFile, bulkUploadTestData.toolName,
				bulkUploadTestData.label, bulkUploadTestData.insightTimeField,
				bulkUploadTestData.nullInsightTimeFormat);

	}

	@Test(priority = 11, expectedExceptions = InsightsCustomException.class)
	public void testIncorrectFileException() throws InsightsCustomException, IOException {

		FileInputStream input = new FileInputStream(incorrectDataFile);
		MultipartFile multipartFile = new MockMultipartFile("file", incorrectDataFile.getName(), "text/plain",
				IOUtils.toByteArray(input));

		boolean response = bulkUploadService.uploadDataInDatabase(multipartFile, bulkUploadTestData.toolName,
				bulkUploadTestData.label, bulkUploadTestData.insightTimeField,
				bulkUploadTestData.nullInsightTimeFormat);

	}

	@Test(priority = 12, expectedExceptions = InsightsCustomException.class)
	public void testFileFormatException() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(fileFormat);
		MultipartFile multipartFile = new MockMultipartFile("file", fileFormat.getName(), "text/plain",
				IOUtils.toByteArray(input));
		boolean response = bulkUploadService.uploadDataInDatabase(multipartFile, bulkUploadTestData.toolName,
				bulkUploadTestData.label, bulkUploadTestData.insightTimeField,
				bulkUploadTestData.nullInsightTimeFormat);

	}

	@Test(priority = 13)
	public void testFileWithNumericValues() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(fileWithNumericValues);
		MultipartFile multipartFile = new MockMultipartFile("file", fileWithNumericValues.getName(), "text/plain",
				IOUtils.toByteArray(input));
		boolean response = bulkUploadService.uploadDataInDatabase(multipartFile, bulkUploadTestData.toolName,
				bulkUploadTestData.labelForNumericCheck, bulkUploadTestData.fileWithNumericValues_insighstimeField,
				bulkUploadTestData.nullInsightTimeFormat);
		Assert.assertEquals(response, true);
		Assert.assertFalse(multipartFile.isEmpty());
	}

}
