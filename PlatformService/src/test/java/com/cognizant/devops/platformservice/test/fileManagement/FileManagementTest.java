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
package com.cognizant.devops.platformservice.test.fileManagement;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFiles;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFilesDAL;
import com.cognizant.devops.platformservice.rest.filemanagement.controller.FileManagementController;
import com.cognizant.devops.platformservice.rest.filemanagement.service.FileManagementServiceImpl;
import com.cognizant.devops.platformservice.test.testngInitializer.TestngInitializerTest;
import com.google.gson.JsonObject;

@Test
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class FileManagementTest extends FileManagementTestData {

	@Autowired
	FileManagementController fileManagementController;
	@Autowired
	FileManagementServiceImpl fileManagementService;
	InsightsConfigFilesDAL configFilesDAL = new InsightsConfigFilesDAL();
	JsonObject testData = new JsonObject();
	private static Logger log = LogManager.getLogger(FileManagementTest.class);

	@BeforeClass
	public void prepareData() throws InsightsCustomException {
		try {
			String path = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator
					+ TestngInitializerTest.TESTNG_TESTDATA + File.separator
					+ TestngInitializerTest.TESTNG_PLATFORMSERVICE + File.separator + "FileManagement.json";
			testData = JsonUtils.getJsonData(path).getAsJsonObject();
		} catch (Exception e) {
			log.error("message", e);
		}
	}

	@Test(priority = 1)
	public void testGetFileTypeList() throws InsightsCustomException {
		try {
			JsonObject response = fileManagementController.getFileTypeList();
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 2)
	public void testGetFileModuleList() throws InsightsCustomException {
		try {
			JsonObject response = fileManagementController.getModuleList();
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 3)
	public void testUploadConfigurationFilewithWrongModule() throws InsightsCustomException, IOException {
		try (FileInputStream input = new FileInputStream(file)) {
			MultipartFile multipartConfigFile = new MockMultipartFile("file", file.getName(), "text/plain",
					IOUtils.toByteArray(input));
			JsonObject response = fileManagementController.saveConfigurationFile(multipartConfigFile, fileName,
					fileType, module1, false);
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.FAILURE);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 4)
	public void testUploadConfigurationFile() throws InsightsCustomException, IOException {
		try (FileInputStream input = new FileInputStream(file)) {
			MultipartFile multipartConfigFile = new MockMultipartFile("file", file.getName(), "text/plain",
					IOUtils.toByteArray(input));
			JsonObject response = fileManagementController.saveConfigurationFile(multipartConfigFile, fileName,
					fileType, module, false);
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 5)
	public void testUploadConfigurationFileAgain() throws InsightsCustomException, IOException {
		try (FileInputStream input = new FileInputStream(file)) {
			MultipartFile multipartConfigFile = new MockMultipartFile("file", file.getName(), "text/plain",
					IOUtils.toByteArray(input));
			JsonObject response = fileManagementController.saveConfigurationFile(multipartConfigFile, fileName,
					fileType, module, false);
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.FAILURE);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 6)
	public void testGetConfigurationFiles() throws InsightsCustomException {
		try {
			JsonObject response = fileManagementController.getConfigurationFiles();
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	// last updated time
	@Test(priority = 7)
	public void testGetLastUpdatedTime() throws InsightsCustomException {
		try {
			JsonObject response = fileManagementController.getConfigurationFiles();
			String actual = response.get("data").getAsJsonArray().get(0).getAsJsonObject().get("lastUpdatedTime")
					.getAsString();
			Assert.assertNotNull(actual, PlatformServiceConstants.SUCCESS);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 8)
	public void testUpdateConfigFile() throws InsightsCustomException, IOException {
		try (FileInputStream input = new FileInputStream(file)) {
			MultipartFile multipartConfigFile = new MockMultipartFile("file", file.getName(), "text/plain",
					IOUtils.toByteArray(input));
			JsonObject response = fileManagementController.saveConfigurationFile(multipartConfigFile, fileName,
					fileType, module, true);
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 9)
	public void testDownloadConfigFile() throws InsightsCustomException {
		try {
			String encodeString = new String(
					Base64.getEncoder().encodeToString(testData.get("fileDetails").toString().getBytes()));
			ResponseEntity<byte[]> response = fileManagementController.downloadConfigFile(encodeString);
			Boolean actual = response.toString().isEmpty();
			Assert.assertEquals(actual, false);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 10)
	public void testDeleteConfigurationFile() throws InsightsCustomException {
		try {
			JsonObject response = fileManagementController.deleteConfigurationFile(fileName);
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
			InsightsConfigFiles configFile = configFilesDAL.getConfigurationFile(fileName);
			Assert.assertNull(configFile);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 11)
	public void testGetFileDataException() throws InsightsCustomException {
		JsonObject response = fileManagementController
				.deleteConfigurationFile(testData.get("fileDetails").getAsJsonObject().get("fileName").getAsString());
		String actual = response.get("status").getAsString().replace("\"", "");
		Assert.assertEquals(actual, PlatformServiceConstants.FAILURE);
	}

	@Test(priority = 12)
	public void testDownloadConfigFileException() throws InsightsCustomException, IOException {
		String encodeString = new String(
				Base64.getEncoder().encodeToString(testData.get("fileDetails").toString().getBytes()));
		ResponseEntity<byte[]> response = fileManagementController.downloadConfigFile(encodeString);
		Assert.assertEquals(response, null);
	}
}