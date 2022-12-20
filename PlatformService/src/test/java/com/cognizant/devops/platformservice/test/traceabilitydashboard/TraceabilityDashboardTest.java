/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
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
package com.cognizant.devops.platformservice.test.traceabilitydashboard; 

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.multipart.MultipartFile;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.enums.FileDetailsEnum;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFiles;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFilesDAL;
import com.cognizant.devops.platformservice.rest.filemanagement.service.FileManagementServiceImpl;
import com.cognizant.devops.platformservice.traceabilitydashboard.service.TraceabilityDashboardServiceImpl;
import com.cognizant.devops.platformservice.traceabilitydashboard.controller.TraceabilityDashboardController;
import com.google.gson.JsonObject;

@Test
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class TraceabilityDashboardTest extends TreceabilityTestData {

	@Autowired
	TraceabilityDashboardController TraceabilityDashboardController;
	private static final Logger log = LogManager.getLogger(TraceabilityDashboardTest.class);
	@Autowired
	TraceabilityDashboardServiceImpl service;
	List<String> testTools = new ArrayList<>();
	InsightsConfigFilesDAL configFilesDAL = new InsightsConfigFilesDAL();
	boolean isDeleteFile = false;
	@Autowired
	FileManagementServiceImpl fileManagementServiceImpl;
	String traceabilityFileName = "TraceabilityTest";
	InsightsConfigFiles config=new InsightsConfigFiles();
	@BeforeClass
	public void beforeMethod() throws InsightsCustomException {		
		graphDBHandler = new GraphDBHandler();
		deleteNodeInNeo4j();
		insertNodeInNeo4j();
		try {
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		File traceabilityFile = new File(classLoader.getResource("TraceabilityTest.json").getFile());
		FileInputStream input = new FileInputStream(traceabilityFile);
		MultipartFile multipartTraceabilityFile = new MockMultipartFile("file", traceabilityFile.getName(), "text/plain",
					IOUtils.toByteArray(input));
		String response = fileManagementServiceImpl.uploadConfigurationFile(multipartTraceabilityFile, traceabilityFileName, "JSON", "TRACEABILITY", false);
		if(response.equals("File uploaded"))
			isDeleteFile=true;
		}catch(Exception e) {
			log.error("File already exists");
		}
	}

	@Test(priority = 1)
	public void dataModelFileExists() {
		JsonObject dataModel = null;
		try {
			List<InsightsConfigFiles> configFile = configFilesDAL
					.getAllConfigurationFilesForModule(FileDetailsEnum.FileModule.TRACEABILITY.name());
			String configFileData = new String(configFile.get(0).getFileData(), StandardCharsets.UTF_8);
			dataModel = JsonUtils.parseStringAsJsonObject(configFileData);
		} catch (Exception e) {

			log.debug("There is problem while processing Traceability.json file :" + e.getMessage());

		}
		Assert.assertNotEquals(dataModel, null);
	}
	
	@Test(priority = 2)
	public void testAvailableTools() throws InsightsCustomException{
		try {
			JsonObject response = TraceabilityDashboardController.getAvailableTools();
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 3)
	public void testToolKeySet() throws InsightsCustomException{
		try {
			toolName = "JIRA";
			JsonObject response = TraceabilityDashboardController.getToolKeyset(toolName);
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 4)
	public void testPipelineResponse() throws InsightsCustomException {
			JsonObject response = TraceabilityDashboardController.getPipeline(toolName, fieldName, fieldVal);
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
			Assert.assertEquals(response.get("data").getAsJsonObject().get("pipeline").getAsJsonArray().get(0).getAsJsonObject().isJsonNull(), false);	
	}
	
	@Test(priority = 5)
	public void testSummaryResponse() throws InsightsCustomException{
		try {
			JsonObject response = TraceabilityDashboardController.getPipeline(toolName,fieldName,fieldVal);
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
			Assert.assertEquals(response.get("data").getAsJsonObject().get("summary").getAsJsonArray().isEmpty(), false);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 6)
	public void testGetToolDisplayProperties() throws InsightsCustomException{
		try {
			JsonObject response = TraceabilityDashboardController.getToolDisplayProperties();
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
			Assert.assertEquals(response.get("data").toString().isEmpty(), false);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 7)
	public void testGetToolSummary() throws InsightsCustomException{
		try {
			JsonObject response = TraceabilityDashboardController.getToolSummary(toolName, cacheKey);
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
			Assert.assertEquals(response.get("data").toString().isEmpty(), false);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 8)
	public void testGetEpicIssues() throws InsightsCustomException{
		try {
			JsonObject response = TraceabilityDashboardController.getEpicIssues(toolName, fieldName, fieldVal,type);
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
			Assert.assertEquals(response.get("data").getAsJsonObject().get("pipeline").getAsJsonArray().isJsonNull(), false);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 9)
	public void testGetPipelineForSelectedNode() throws InsightsCustomException{
		try {
			JsonObject response = TraceabilityDashboardController.getIssuePipeline(data);
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
			Assert.assertEquals(response.get("data").getAsJsonObject().get("pipeline").getAsJsonArray().isJsonNull(), false);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 10)
	public void testGetPipelineForSelectedNodeWithoutEpicIssueType() throws InsightsCustomException{
		try {
			JsonObject response = TraceabilityDashboardController.getIssuePipeline(dataWithoutEpicIssueType);
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
			Assert.assertEquals(response.get("data").getAsJsonObject().get("pipeline").getAsJsonArray().isJsonNull(), false);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 11)
	public void testOperationNameCount() throws InsightsCustomException{
		try {
			String actual="PROPERTY_COUNT";
			for (String operation : operation) 
			{ 
				getOperationData(actual, operation);
				actual = operation;
				service.clearAllCacheValue();
				toolName = "JIRA";
				JsonObject response = TraceabilityDashboardController.getPipeline(toolName, fieldName, fieldVal);
				Assert.assertEquals(response.get("status").getAsString().replace("\"", ""), PlatformServiceConstants.SUCCESS);
			}
			} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 12)
	public void testGetUptoolValueException() throws InsightsCustomException{
		try {
			getConfigData("uptool");
			service.clearAllCacheValue();
			JsonObject response = TraceabilityDashboardController.getPipeline(toolName, fieldName, fieldVal);
			Assert.assertEquals(response.get("status").getAsString().replace("\"", ""), PlatformServiceConstants.FAILURE);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 13)
	public void testGetUptoolException() throws InsightsCustomException{
		try {
			getOperationData("uptool","uptoolll");
			JsonObject response = TraceabilityDashboardController.getPipeline(toolName, fieldName, fieldVal);
			Assert.assertEquals(response.get("status").getAsString().replace("\"", ""), PlatformServiceConstants.FAILURE);
			getOperationData("uptoolll","uptool");
			getOperationData("\"EPIC\"","[\r\n"+ "         \"EPIC\"\r\n"+ "      ]");
			service.clearAllCacheValue();
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 14)
	public void testGetDowntoolValueException() throws InsightsCustomException{
		try {
			getConfigData("downtool");
			service.clearAllCacheValue();
			JsonObject response = TraceabilityDashboardController.getPipeline(toolName, fieldName, fieldVal);
			Assert.assertEquals(response.get("status").getAsString().replace("\"", ""), PlatformServiceConstants.FAILURE);	
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 15)
	public void testGetDowntoolException() throws InsightsCustomException{
		try {
			getOperationData("downtool","downtoolll");
			service.clearAllCacheValue();
			JsonObject response = TraceabilityDashboardController.getPipeline(toolName, fieldName, fieldVal);
			Assert.assertEquals(response.get("status").getAsString().replace("\"", ""), PlatformServiceConstants.FAILURE);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 16)
	public void testGetPipelineNoCategoryDataMember() throws InsightsCustomException{
		try {
		    getOperationData("category","categoryy");
		    getOperationData("uifilter","uifiltter");
			service.clearAllCacheValue();
			JsonObject response = TraceabilityDashboardController.getPipeline(toolName, fieldName, fieldVal);
			JsonObject response1 = TraceabilityDashboardController.getEpicIssues(toolName, fieldName, fieldVal, "Others");
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.FAILURE);
			String actual1 = response1.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual1, PlatformServiceConstants.FAILURE);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 17)
	public void testToolKeySetInvalidTool() throws InsightsCustomException{
		try {
			JsonObject response = TraceabilityDashboardController.getToolKeyset(toolName);
			Assert.assertEquals(response.get("data").getAsJsonArray().isEmpty(), true);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@AfterClass
	public void cleanUp() throws InsightsCustomException {
		deleteNodeInNeo4j();
		try {
			if(isDeleteFile) {
				fileManagementServiceImpl.deleteConfigurationFile(traceabilityFileName);
			}
		}catch(Exception e) {
			log.error("File already exists");
		}
	}
}