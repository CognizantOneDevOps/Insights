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
		Assert.assertNotNull(bulkUploadTestData.toolJson);

	}

	@Test(priority = 2)
	public void testUploadDataInDatabase() throws InsightsCustomException, IOException {

		FileInputStream input = new FileInputStream(file);
		MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain",
				IOUtils.toByteArray(input));

		boolean response = bulkUploadService.uploadDataInDatabase(multipartFile, bulkUploadTestData.toolName,
				bulkUploadTestData.label);

		Assert.assertEquals(response, true);
		Assert.assertFalse(multipartFile.isEmpty());
		Assert.assertTrue(multipartFile.getSize() < filesizeMaxValue);

	}

	@Test(priority = 3, expectedExceptions = InsightsCustomException.class)
	public void testFileSizeException() throws InsightsCustomException, IOException {

		FileInputStream input = new FileInputStream(fileSize);
		MultipartFile multipartFile = new MockMultipartFile("file", fileSize.getName(), "text/plain",
				IOUtils.toByteArray(input));

		boolean response = bulkUploadService.uploadDataInDatabase(multipartFile, bulkUploadTestData.toolName,
				bulkUploadTestData.label);

	}

	@Test(priority = 4, expectedExceptions = InsightsCustomException.class)
	public void testIncorrectFileException() throws InsightsCustomException, IOException {

		FileInputStream input = new FileInputStream(incorrectDataFile);
		MultipartFile multipartFile = new MockMultipartFile("file", incorrectDataFile.getName(), "text/plain",
				IOUtils.toByteArray(input));

		boolean response = bulkUploadService.uploadDataInDatabase(multipartFile, bulkUploadTestData.toolName,
				bulkUploadTestData.label);

	}

	@Test(priority = 5, expectedExceptions = InsightsCustomException.class)
	public void testFileFormatException() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(fileFormat);
		MultipartFile multipartFile = new MockMultipartFile("file", fileFormat.getName(), "text/plain",
				IOUtils.toByteArray(input));

		boolean response = bulkUploadService.uploadDataInDatabase(multipartFile, bulkUploadTestData.toolName,
				bulkUploadTestData.label);

	}

}
