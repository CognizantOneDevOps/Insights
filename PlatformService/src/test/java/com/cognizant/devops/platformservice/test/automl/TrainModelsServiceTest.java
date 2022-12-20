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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.multipart.MultipartFile;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.cognizant.devops.automl.service.TrainModelsServiceImpl;
import com.cognizant.devops.automl.controller.InsightsTrainModelController;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.assessmentreport.InsightsKPIConfig;
import com.cognizant.devops.platformdal.assessmentreport.ReportConfigDAL;
import com.cognizant.devops.platformdal.autoML.AutoMLConfig;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTask;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowType;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformservice.workflow.service.WorkflowServiceImpl;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Test
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
@SuppressWarnings("unused")
@WebAppConfiguration
public class TrainModelsServiceTest extends TrainModelsServiceTestData {
	private static Logger log = LogManager.getLogger(TrainModelsServiceTest.class);
	
	@Autowired
	InsightsTrainModelController InsightsTrainModelController;
	@Autowired
	TrainModelsServiceImpl trainModelsServiceImpl;
	@Autowired
	WorkflowServiceImpl workflowService;//= new WorkflowServiceImpl();
	WorkflowDAL workflowConfigDAL = new WorkflowDAL();
	ReportConfigDAL reportConfigDAL = new ReportConfigDAL();
	@BeforeClass
	public void onInit() throws InsightsCustomException, IOException {
		//add workflow type for automl
		InsightsWorkflowType workflowTypeObj = workflowConfigDAL.getWorkflowType(WorkflowTaskEnum.WorkflowType.AUTOML.getValue());
		if (workflowTypeObj == null) {
			InsightsWorkflowType type = new InsightsWorkflowType();
			type.setWorkflowType(WorkflowTaskEnum.WorkflowType.AUTOML.getValue());
			workflowConfigDAL.saveWorkflowType(type);
		}
		//add workflow task for automl if not present
		List<InsightsWorkflowTask> listofTasks = workflowConfigDAL.getTaskLists(WorkflowTaskEnum.WorkflowType.AUTOML.getValue());
		if(listofTasks.isEmpty()) {
			workflowService.saveWorkflowTask(workflowTaskJson);
		} else {
			isTaskExists = true;
		}
		h2oEndpoint = ApplicationConfigProvider.getInstance().getMlConfiguration().getH2oEndpoint();
	}
	
	//save automl usecase and check entry in workflow Config
	@Test(priority = 1)
	public void testSaveAutoMLConfig() throws IOException, InsightsCustomException {
		FileInputStream input = new FileInputStream(file);
		MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain",IOUtils.toByteArray(input));
		ResponseEntity<JsonObject> response = InsightsTrainModelController.saveUsecase(multipartFile, usecase, configuration, 
				trainingPercent, predictionColumn, numOfModels, getTaskList(),"Regression");
		
	    AutoMLConfig automl = autoMLConfigDAL.getMLConfigByUsecase(usecase);
		InsightsWorkflowConfiguration workflowConfig = workflowConfigDAL.getWorkflowByWorkflowId(automl.getWorkflowConfig()
				.getWorkflowId());
		Assert.assertTrue(workflowConfig.isRunImmediate());
		Assert.assertEquals(workflowConfig.getStatus(), WorkflowTaskEnum.WorkflowStatus.NOT_STARTED.toString());
		Assert.assertNotNull(automl);
	}
	
	//save with existing usecase name
	@Test(priority = 2)
	public void testSaveAutoMlConfigWithExistingUsecase() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(file);
		MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain",IOUtils.toByteArray(input));
		ResponseEntity<JsonObject> response = InsightsTrainModelController.saveUsecase(multipartFile, usecase, configuration, 
				trainingPercent, predictionColumn, numOfModels, getTaskList(),"Regression");
		Assert.assertEquals(response.getStatusCodeValue(), 200);
		Assert.assertEquals(response.getBody().get("status").getAsString().replace("\"", ""), PlatformServiceConstants.FAILURE);
	}
	
	@Test(priority = 3)
	public void validateUsecaseName() throws InsightsCustomException, IOException {
		JsonObject response = InsightsTrainModelController.validateUsecaseName(usecase);
		Assert.assertEquals(response.get("data").getAsJsonObject().get("UniqueUsecase").getAsBoolean(), true);
	}
	
	@Test(priority = 4)
	public void testGetUsecase() throws InsightsCustomException {
		ResponseEntity<JsonObject> response = InsightsTrainModelController.getUsecases();
		Assert.assertEquals(response.getBody().get("data").getAsJsonObject().get("usecases").isJsonNull(), false);
	}
	
	@Test(priority = 5)
	public void testGetLeaderBoard() throws InsightsCustomException, IOException {
		if(h2oEndpoint == null || h2oEndpoint.isEmpty()) {
			throw new SkipException("skipped this test case as H2O server details not found.");
		}
		FileInputStream input = new FileInputStream(file);
		MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain",IOUtils.toByteArray(input));
		executeAutomlConfig(usecase);
		JsonObject leaderboard = trainModelsServiceImpl.getLeaderBoard(usecase);
		modelName = leaderboard.get("data").getAsJsonArray().get(0).getAsJsonObject().get("model_id").getAsString();
		Assert.assertTrue(leaderboard.has("data"));
		Assert.assertNotNull(leaderboard);
		Assert.assertNotNull(modelName);
	}
	
	@Test(priority = 6)
	public void testGetPrediction() throws InsightsCustomException {
		if(h2oEndpoint == null || h2oEndpoint.isEmpty()) {
			throw new SkipException("skipped this test case as H2O server details not found.");
		}
		JsonObject predictedData = trainModelsServiceImpl.getPrediction(modelName, usecase);
		Assert.assertTrue(predictedData.has("Fields"));
		Assert.assertTrue(predictedData.has("Data"));
		Assert.assertNotNull(predictedData);
	}
	
	@Test(priority = 7)
	public void testDownloadMojo() throws InsightsCustomException {
		if(h2oEndpoint == null || h2oEndpoint.isEmpty()) {
			throw new SkipException("skipped this test case as H2O server details not found.");
		}
		JsonObject savedMojo = trainModelsServiceImpl.downloadMojo(usecase, modelName);
		Assert.assertTrue(savedMojo.has("Message"));
		Assert.assertNotNull(savedMojo);
	}
	
	@Test(priority = 8)
	public void testGetMojoDeployedUsecases() throws InsightsCustomException {
		if(h2oEndpoint == null || h2oEndpoint.isEmpty()) {
			throw new SkipException("skipped this test case as H2O server details not found.");
		}
		JsonArray response = trainModelsServiceImpl.getMojoDeployedUsecases();
		Assert.assertNotNull(response);
		Assert.assertTrue(response.get(0).getAsJsonObject().has("usecaseName"));
		Assert.assertTrue(response.get(0).getAsJsonObject().has("predictionColumn"));
	}
	
	@Test(priority = 9)
	public void testUpdateUsecaseStateToInactive() throws InsightsCustomException {
		JsonObject usecaseJson = new JsonObject();
		usecaseJson.addProperty("usecaseName", usecase);
		usecaseJson.addProperty("isActive", false);
		JsonObject response = InsightsTrainModelController.updateUsecaseState(usecaseJson.toString());
		Assert.assertEquals(response.get("status").getAsString().replace("\"", ""), PlatformServiceConstants.SUCCESS);
		String expectedResponse = usecase + " state updated successfully.";
		Assert.assertEquals(response.get("data").getAsString().replace("\"", ""), expectedResponse);
	}
	
	@Test(priority = 10)
	public void testUpdateUsecaseStatewithInvalidUseCase() throws InsightsCustomException {
		JsonObject usecaseJson = new JsonObject();
		usecaseJson.addProperty("usecaseName", usecase+"321");
		usecaseJson.addProperty("isActive", false);
		JsonObject response = InsightsTrainModelController.updateUsecaseState(usecaseJson.toString());
		Assert.assertEquals(response.get("status").getAsString().replace("\"", ""), PlatformServiceConstants.FAILURE);
	}
	
	@Test(priority = 11)
	public void testUpdateUsecaseStatewithKPIHasUseCase() throws InsightsCustomException {
		insightsAssessmentReportController.saveKpiDefinition(registerkpiwithUsecase);
		List<InsightsKPIConfig> list = reportConfigDAL.getKpiConfigByUsecase(usecase);
		JsonObject usecaseJson = new JsonObject();
		usecaseJson.addProperty("usecaseName", usecase);
		usecaseJson.addProperty("isActive", false);
		JsonObject response = InsightsTrainModelController.updateUsecaseState(usecaseJson.toString());
		Assert.assertEquals(response.get("status").getAsString().replace("\"", ""), PlatformServiceConstants.FAILURE);
		JsonObject response1 = insightsAssessmentReportController.deleteKpiDefinition(deleteKpiString);
	}
	
	@Test(priority = 12)
	public void testUpdateUsecaseStatewithKPIHasUseCaseAndActiveState() throws InsightsCustomException {
		insightsAssessmentReportController.saveKpiDefinition(registerkpiwithUsecaseActiveState);
		List<InsightsKPIConfig> list = reportConfigDAL.getKpiConfigByUsecase(usecase);
		JsonObject usecaseJson = new JsonObject();
		usecaseJson.addProperty("usecaseName", usecase);
		usecaseJson.addProperty("isActive", true);
		JsonObject response = InsightsTrainModelController.updateUsecaseState(usecaseJson.toString());
		Assert.assertEquals(response.get("status").getAsString().replace("\"", ""), PlatformServiceConstants.SUCCESS);
		JsonObject response1 = insightsAssessmentReportController.deleteKpiDefinition(deleteKpiString);	
	}
	
	//delete usecase from postgres along with Csv file
	@Test(priority = 13)
	public void testDeleteUsecase() throws InsightsCustomException {
		ResponseEntity<JsonObject> response = InsightsTrainModelController.deleteUsecase(usecase);
	    Assert.assertEquals(response.getBody().get("status").getAsString().replace("\"", ""), PlatformServiceConstants.SUCCESS);
	    Assert.assertEquals(response.getBody().get("data").getAsJsonObject().get("statusCode").getAsInt(), 1);
	}
	
	@Test(priority = 14)
	public void testDeleteWithInValidUsecase() throws InsightsCustomException {
		try {
			ResponseEntity<JsonObject> response = InsightsTrainModelController.deleteUsecase("&amp;{<"+usecase);
			}catch (Exception e) {
			Assert.assertEquals(true, e.toString().contains("Invalid request"));
		}
	}
	
	@AfterClass
	public void cleanUp() {
		//delete workflow task
		if(!isTaskExists) {
			int taskId = workflowConfigDAL.getTaskId(mqChannel);
			workflowConfigDAL.deleteTask(taskId);
		}
	}
}