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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.compress.utils.IOUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.multipart.MultipartFile;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFiles;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFilesDAL;
import com.cognizant.devops.platformservice.rest.filemanagement.service.FileManagementServiceImpl;
import com.google.gson.JsonArray;

@Test
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class FileManagementTest extends FileManagementTestData {
	
	FileManagementServiceImpl fileManagementService = new FileManagementServiceImpl();
	FileManagementTestData fileManagementTestData = new FileManagementTestData();
	InsightsConfigFilesDAL configFilesDAL = new InsightsConfigFilesDAL();
	
	@BeforeTest
	public void onInit() throws InsightsCustomException {
		ApplicationConfigCache.loadConfigCache();
	
	}
	
	@Test(priority = 1)
	public void testGetFileTypeList() {
		List<String> fileTypeList = fileManagementService.getFileType();
		Assert.assertNotNull(fileTypeList);
		Assert.assertTrue(fileTypeList.size() > 0);
	}
	
	@Test(priority = 2)
	public void testGetFileModuleList() {
		List<String> fileModuleList = fileManagementService.getModuleList();
		Assert.assertNotNull(fileModuleList);
		Assert.assertTrue(fileModuleList.size() > 0);
	}
	
	@Test(priority = 3)
	public void testUploadConfigurationFile() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(file);
		MultipartFile multipartConfigFile = new MockMultipartFile("file", file.getName(), "text/plain",
				IOUtils.toByteArray(input));
		String message = fileManagementService.uploadConfigurationFile(multipartConfigFile, fileName, fileType, module);
		InsightsConfigFiles configFile = configFilesDAL.getConfigurationFile(fileName);
		Assert.assertEquals("File uploaded", message);
		Assert.assertNotNull(configFile);
	}
	
	@Test(priority = 4)
	public void testGetConfigurationFiles() throws InsightsCustomException {
		JsonArray jsonarray = fileManagementService.getConfigurationFilesList();
		Assert.assertNotNull(jsonarray);
	}
	
	@Test(priority = 5)
	public void testGetFileData() throws InsightsCustomException {
		byte[] fileContent = fileManagementService.getFileData(fileDetailsJson);
		Assert.assertNotNull(fileContent);
		
	}
	
	@Test(priority = 6)
	public void testUpdateConfigFile() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(file);
		MultipartFile multipartConfigFile = new MockMultipartFile("file", file.getName(), "text/plain",
				IOUtils.toByteArray(input));
		String message = fileManagementService.uploadConfigurationFile(multipartConfigFile, fileName, fileType, module);
		InsightsConfigFiles configFile = configFilesDAL.getConfigurationFile(fileName);
		Assert.assertEquals("File uploaded", message);
		Assert.assertNotNull(configFile);
	}
	
	@Test(priority = 7)
	public void testDeleteConfigurationFile() throws InsightsCustomException {
		fileManagementService.deleteConfigurationFile(fileName);
		InsightsConfigFiles configFile = configFilesDAL.getConfigurationFile(fileName);
		Assert.assertNull(configFile);
	}
	
	@Test(priority = 8, expectedExceptions = InsightsCustomException.class)
	public void testGetFileDataException() throws InsightsCustomException {
		byte[] fileContent = fileManagementService.getFileData(fileDetailsJson);
		Assert.assertNull(fileContent);
		
	}
	
	@Test(priority = 9, expectedExceptions = InsightsCustomException.class)
	public void testDeleteConfigurationFileException() throws InsightsCustomException {
		fileManagementService.deleteConfigurationFile(fileName);
	}
	
}
