/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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
package com.cognizant.devops.platformservice.test.automl;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.compress.utils.IOUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.multipart.MultipartFile;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.automl.service.TrainModelsServiceImpl;
import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.autoML.AutoMLConfig;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTask;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformservice.workflow.service.WorkflowServiceImpl;
import com.google.gson.JsonObject;

@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class TrainModelsServiceTest extends TrainModelsServiceTestData {
	
	TrainModelsServiceImpl trainModelsServiceImpl;
	WorkflowServiceImpl workflowService = new WorkflowServiceImpl();
	WorkflowDAL workflowConfigDAL = new WorkflowDAL();
	
	@BeforeTest
	public void onInit() throws InsightsCustomException, IOException {
		
		ApplicationConfigCache.loadConfigCache();
		trainModelsServiceImpl = new TrainModelsServiceImpl();
		
		//add workflow task for automl if not present
		List<InsightsWorkflowTask> listofTasks = workflowConfigDAL.getTaskLists("AUTOML");
		if(listofTasks.isEmpty()) {
			workflowService.saveWorkflowTask(workflowTaskJson);
		} else {
			isTaskExists = true;
		}
		
	}
	
	//save automl usecase and check entry in workflow Config
	@Test(priority = 1)
	public void testSaveAutoMLConfig() throws IOException, InsightsCustomException {
		FileInputStream input = new FileInputStream(file);
		MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain",IOUtils.toByteArray(input));
		int id = trainModelsServiceImpl.saveAutoMLConfig(multipartFile, usecase, configuration, 
				trainingPercent, predictionColumn, numOfModels, getTaskList(),"Regression");
		AutoMLConfig automl = autoMLConfigDAL.getMLConfigByUsecase(usecase);
		InsightsWorkflowConfiguration workflowConfig = workflowConfigDAL.getWorkflowByWorkflowId(automl.getWorkflowConfig()
				.getWorkflowId());
		Assert.assertTrue(workflowConfig.isRunImmediate());
		Assert.assertEquals(workflowConfig.getStatus(), WorkflowTaskEnum.WorkflowStatus.NOT_STARTED.toString());
		Assert.assertTrue(workflowConfig.getTaskSequenceEntity().size() > 0);
		Assert.assertNotNull(automl);
		Assert.assertNotNull(id);
		Assert.assertNotEquals(id, -1);
	}
	
	//save with existing usecase name
	@Test(priority = 2, expectedExceptions = InsightsCustomException.class)
	public void testSaveAutoMlConfigWithExistingUsecase() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(file);
		MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain",IOUtils.toByteArray(input));
		int id = trainModelsServiceImpl.saveAutoMLConfig(multipartFile, usecase, configuration, 
				trainingPercent, predictionColumn, numOfModels, getTaskList(),"Regression");
	}
	
	//save with incorrect usecase name
//	@Test(priority = 2, expectedExceptions = InsightsCustomException.class)
//	public void testSaveAutoMlConfigIncorrectUsecase() throws InsightsCustomException, IOException {
//		FileInputStream input = new FileInputStream(file);
//		MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain",IOUtils.toByteArray(input));
//		int id = trainModelsServiceImpl.saveAutoMLConfig(multipartFile, incorrectUsecase, configuration, 
//				trainingPercent, predictionColumn, numOfModels, getTaskList());
//	}
	
	//get all usecases from postgres
	@Test(priority = 3)
	public void testGetUsecase() throws InsightsCustomException {
		JsonObject automl = trainModelsServiceImpl.getUsecases();
		Assert.assertNotNull(automl);
		Assert.assertTrue(automl.has("usecases"));
	}
	
	
	@Test(priority = 4)
	public void testGetLeaderBoard() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(file);
		MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain",IOUtils.toByteArray(input));
		int id = trainModelsServiceImpl.saveAutoMLConfig(multipartFile, usecase, configuration, 
				trainingPercent, predictionColumn, numOfModels, getTaskList(),"Regression");
		executeAutomlConfig(usecase);
		JsonObject leaderboard = trainModelsServiceImpl.getLeaderBoard(usecase);
		modelName = leaderboard.get("data").getAsJsonArray().get(0).getAsJsonObject().get("model_id").getAsString();
		Assert.assertTrue(leaderboard.has("data"));
		Assert.assertNotNull(leaderboard);
		Assert.assertNotNull(modelName);
	}
	
	@Test(priority = 5)
	public void testGetPrediction() throws InsightsCustomException {
		JsonObject predictedData = trainModelsServiceImpl.getPrediction(modelName, usecase);
		Assert.assertTrue(predictedData.has("Fields"));
		Assert.assertTrue(predictedData.has("Data"));
		Assert.assertNotNull(predictedData);
	}
	
	@Test(priority = 6)
	public void testDownloadMojo() throws InsightsCustomException {
		JsonObject savedMojo = trainModelsServiceImpl.downloadMojo(usecase, modelName);
		Assert.assertTrue(savedMojo.has("Message"));
		Assert.assertNotNull(savedMojo);
	}
	
	//delete usecase from postgres along with Csv file
		@Test(priority = 7)
		public void testDeleteUsecase() throws InsightsCustomException {
			JsonObject automl = trainModelsServiceImpl.deleteUsecase(usecase);
			Assert.assertNotNull(automl);
			Assert.assertEquals(automl.get("statusCode").getAsInt(), 1);
		}
		
//		//delete usecase with incorrect name
//		@Test(priority = 8, expectedExceptions = InsightsCustomException.class)
//		public void testDeleteIncorrectUsecase() throws InsightsCustomException {
//			JsonObject automl = trainModelsServiceImpl.deleteUsecase(incorrectUsecase);
//			}
	
	@AfterTest
	public void cleanUp() {
		
		//delete workflow task
		if(!isTaskExists) {
			int taskId = workflowConfigDAL.getTaskId(mqChannel);
			workflowConfigDAL.deleteTask(taskId);
		}
		
	}

}
