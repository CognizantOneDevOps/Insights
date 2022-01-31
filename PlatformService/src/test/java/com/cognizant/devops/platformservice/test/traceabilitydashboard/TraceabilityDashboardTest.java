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
import java.util.Arrays;
import java.util.List;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.core.enums.FileDetailsEnum;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFiles;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFilesDAL;
import com.cognizant.devops.platformservice.rest.filemanagement.service.FileManagementServiceImpl;
import com.cognizant.devops.platformservice.traceabilitydashboard.service.TraceabilityDashboardService;
import com.cognizant.devops.platformservice.traceabilitydashboard.service.TraceabilityDashboardServiceImpl;
import com.google.gson.JsonObject;

@Test
public class TraceabilityDashboardTest extends TreceabilityTestData {

	private static final Logger log = LogManager.getLogger(TraceabilityDashboardTest.class);
	TraceabilityDashboardService service = new TraceabilityDashboardServiceImpl();
	List<String> testTools = new ArrayList<>();
	InsightsConfigFilesDAL configFilesDAL = new InsightsConfigFilesDAL();
	boolean isDeleteFile = false;
	FileManagementServiceImpl fileManagementServiceImpl = new FileManagementServiceImpl();
	String traceabilityFileName = "TraceabilityTest";

	@BeforeClass
	public void beforeMethod() throws InsightsCustomException {		
		graphDBHandler = new GraphDBHandler();
		
		insertDataInNeo4j(gitCat, gitLabel, gitAgentData);
		insertDataInNeo4j(jiraCat, jiralabel, jiraAgentData);
		
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
		Assert.assertNotEquals(null, dataModel);
	}

	@Test(priority = 2)
	public void testAvailableTools() throws InsightsCustomException {
		testTools = service.getAvailableTools();
		Assert.assertNotEquals(0, service.getAvailableTools().size());
	}

	@Test(priority = 3)
	public void testToolKeySet() throws InsightsCustomException {
		Assert.assertNotEquals(0, service.getToolKeyset(TreceabilityTestData.toolName).size());
	}

	
	@Test(priority = 4)
	public void testPipelineResponse() throws InsightsCustomException {
		JsonObject resp = new JsonObject();
		try {
			  resp = service.getPipeline(TreceabilityTestData.toolName, TreceabilityTestData.fieldName,
					Arrays.asList(TreceabilityTestData.fieldVal),"Issue");
			  if(resp.get("pipeline").getAsJsonArray().size()==0)
			  {
				  throw new InsightsCustomException("data not found");
			  }
		    } catch (InsightsCustomException e) {
		    	log.debug("skipped this test case as required data not found" );
			throw new SkipException("skipped this test case as required data not found");
		}
		Assert.assertNotEquals(0, resp.get("pipeline").getAsJsonArray().size());
	}
	
	@Test(priority =5)
	public void testSummaryResponse() throws InsightsCustomException
	{
		JsonObject resp = new JsonObject();
		try {
			  resp = service.getPipeline(TreceabilityTestData.toolName, TreceabilityTestData.fieldName,
					  Arrays.asList(TreceabilityTestData.fieldVal),"Issue");
			  if(resp.get("summary").getAsJsonArray().size()==0)
			  {
				  throw new InsightsCustomException("summary data not found");
			  }
		    } catch (InsightsCustomException e) {
		    	log.debug("skipped this test case as required data not found" );
			throw new SkipException("skipped this test case as required data not found");
		}
		Assert.assertNotEquals(0, resp.get("summary").getAsJsonArray().size());
	}

	@Test(priority = 6)
	public void testPipelineResponseWithIncorrectData() throws InsightsCustomException {
		JsonObject resp = service.getPipeline(TreceabilityTestData.toolName, TreceabilityTestData.fieldName,
				Arrays.asList(TreceabilityTestData.incorrectFieldVal),"Issue");
		Assert.assertEquals(0, resp.get("pipeline").getAsJsonArray().size());
	}
	
	
	@AfterClass
	public void cleanUp() throws InsightsCustomException {
		deleteDataFromNeo4j(jiraCat);
		deleteDataFromNeo4j(gitCat);
		
		try {
			if(isDeleteFile) {
				fileManagementServiceImpl.deleteConfigurationFile(traceabilityFileName);
			}
		}catch(Exception e) {
			log.error("File already exists");
		}
	}

}
