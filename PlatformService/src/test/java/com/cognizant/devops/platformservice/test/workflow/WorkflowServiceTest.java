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
package com.cognizant.devops.platformservice.test.workflow;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.assessmentreport.InsightsAssessmentConfiguration;
import com.cognizant.devops.platformdal.assessmentreport.InsightsReportVisualizationContainer;
import com.cognizant.devops.platformdal.assessmentreport.ReportConfigDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowExecutionHistory;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTask;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowType;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformservice.assessmentreport.service.AssesmentReportServiceImpl;
import com.cognizant.devops.platformservice.test.testngInitializer.TestngInitializerTest;
import com.cognizant.devops.platformservice.workflow.controller.InsightsWorkflowController;
import com.cognizant.devops.platformservice.workflow.service.WorkflowServiceImpl;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Test
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class WorkflowServiceTest extends WorkflowServiceTestData {
	private static final Logger log = LogManager.getLogger(WorkflowServiceTest.class);
	public static final AssesmentReportServiceImpl assessmentService = new AssesmentReportServiceImpl();
	
	@Autowired
	WorkflowServiceImpl workflowService;
	
	@Autowired
	InsightsWorkflowController workflowController;
	WorkflowDAL workflowConfigDAL = new WorkflowDAL();
	ReportConfigDAL reportConfigDAL = new ReportConfigDAL();

	JsonObject testData = new JsonObject();
	
	@BeforeClass
	public void prepareData() throws InsightsCustomException {
		try {
		    String path = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator + TestngInitializerTest.TESTNG_TESTDATA + File.separator
                    + TestngInitializerTest.TESTNG_PLATFORMSERVICE + File.separator + "WorkflowService.json";
			testData = JsonUtils.getJsonData(path).getAsJsonObject();
			int response = assessmentService.saveKpiDefinition(testData.get("registerkpiWorkflow").getAsJsonObject());
			int reportId = assessmentService.saveTemplateReport(testData.get("reportTemplateWorkflow").getAsJsonObject());
			setReportId(reportId);
		    InsightsWorkflowType type = new InsightsWorkflowType();
			type.setWorkflowType(WorkflowTaskEnum.WorkflowType.REPORT.getValue());
			workflowConfigDAL.saveWorkflowType(type);
		} catch (Exception e) {
			log.error("Error preparing data at WorkflowServiceTest workflowtype record ", e);
		}
	}
	
	@Test(priority = 1)
	public void testSaveWorkflowTask() throws InsightsCustomException {
		try {
			JsonObject response = workflowController.saveAssessmentTask(testData.get("workflowTaskData").toString());
			String responseId = response.get("data").getAsString().replace("\"", "").replaceAll("[^0-9]", "");
			
			JsonObject responseEmail = workflowController.saveAssessmentTask(testData.get("workflowEmailTaskData").toString());
			String responseEmailId = responseEmail.get("data").getAsString().replace("\"", "").replaceAll("[^0-9]", "");
			
			InsightsWorkflowTask task = workflowConfigDAL.getTaskByTaskId(Integer.parseInt(responseId));
			InsightsWorkflowTask emailtask = workflowConfigDAL.getTaskByTaskId(Integer.parseInt(responseEmailId));
			Assert.assertNotNull(task);
			Assert.assertNotNull(emailtask);
			taskID = task.getTaskId();
			emailTaskID = emailtask.getTaskId();
			SetInfo(taskID, emailTaskID);
			String actualResponse = response.get("status").getAsString().replace("\"", "");
			String actualResponseEmail = responseEmail.get("status").getAsString().replace("\"", "");
			
			Assert.assertEquals(actualResponse, PlatformServiceConstants.SUCCESS);
			Assert.assertEquals(actualResponseEmail, PlatformServiceConstants.SUCCESS);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 2)
	public void testSaveIncorrectWorkflowTask() throws InsightsCustomException{
		try {
			JsonObject response = workflowController.saveAssessmentTask(testData.get("incorrectWorkflowTask").toString());
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.FAILURE);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
		}
	
	@Test(priority = 3)
	public void testSaveWorkflowTaskException() throws InsightsCustomException {
		try {
			JsonObject response = workflowController.saveAssessmentTask(testData.get("workflowTaskDataException").toString());
			String actual = response.get("status").getAsString();
			Assert.assertEquals(actual, PlatformServiceConstants.FAILURE);
			} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 4)
	public void testGetWorkflowTaskList() throws InsightsCustomException {
		try {
			JsonObject response = workflowController.getTaskList(workflowType);
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
	} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 5)
	public void testGetWorkflowExecutionRecords() throws InsightsCustomException, InterruptedException {
		Thread.sleep(1000);
		InsightsAssessmentConfiguration assessmentObj = null;
		try {
			JsonObject assessmentResponse = assessmentService.saveAssessmentReport(dailyAssessmentReportWorkflowJson);
			assessmentId = assessmentResponse.get("assessmentReportId").getAsInt();
			assessmentObj = reportConfigDAL
					.getAssessmentByAssessmentName(dailyAssessmentReportWorkflowJson.get("reportName").getAsString());
			workflowId = assessmentObj.getWorkflowConfig().getWorkflowId();
			historyid = updateWorkflowExecutionHistory(workflowId, taskID, assessmentId);
			List<InsightsWorkflowExecutionHistory> executionId = workflowConfigDAL.getWorkflowExecutionHistoryRecordsByWorkflowId(workflowId);
			long i = executionId.get(0).getExecutionId();
			
			JsonObject pdfDetailsJson = new JsonObject();
			pdfDetailsJson.addProperty("workflowId", workflowId);
			pdfDetailsJson.addProperty("executionId", i);
			String input= pdfDetailsJson.toString().replace("\n", "").replace("\r", "");
			String encodeString = new String(Base64.getEncoder().encodeToString(input.getBytes()));
			ResponseEntity<byte[]> response1 = workflowController.getReportPDF(encodeString);
			
			JsonObject configIdJson = new JsonObject();
			configIdJson.addProperty("configid", assessmentObj.getId());
			
			JsonObject response = workflowController.getWorkflowExecutionRecords(configIdJson.toString());
			String actual = response.get("status").getAsString().replace("\"", "");
			
			JsonArray records = workflowService.getWorkFlowExecutionRecords(configIdJson).getAsJsonArray("records");
			String expected = testData.get("workflowTaskData").getAsJsonObject().get("description").getAsString();
			
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
			Assert.assertNotNull(records);
			Assert.assertTrue(records.size() > 0);
			Assert.assertEquals(records.get(0).getAsJsonObject().get("currentTask").getAsString(), expected);
		} catch (AssertionError e) {
			Assert.fail("testgetWorkflowExecutionRecords");
		}
	}

	@Test(priority = 6)
	public void testGetWorkflowExecutionRecordsException() throws InsightsCustomException, InterruptedException {
		Thread.sleep(1000);
		try {
			JsonObject configIdJson = new JsonObject();
			JsonObject response = workflowController.getWorkflowExecutionRecords(configIdJson.toString());
			String actual = response.get("status").getAsString();
			Assert.assertEquals(actual, PlatformServiceConstants.FAILURE);
				} 
		catch (AssertionError e) {
			Assert.fail("testgetWorkflowExecutionRecords");
		}
	}

	@Test(priority = 7)
	public void testGetMaximumIds() throws InsightsCustomException {
		try {
			JsonObject ConfigIdJson = new JsonObject();
			ConfigIdJson.addProperty("configid", assessmentId);
			
			JsonObject response = workflowController.getMaximumExecutionIds(ConfigIdJson.toString());
			String actual = response.get("status").getAsString().replace("\"", "");
			
			JsonObject records = workflowService.getMaximumExecutionIDs(ConfigIdJson);
			
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
			Assert.assertNotNull(records);
			Assert.assertEquals(records.get("status").getAsBoolean(), false);
			Assert.assertEquals(records.get("executionId").getAsInt(), -1);
		} catch (AssertionError e) {
			Assert.fail("testgetWorkflowExecutionRecords");
		}
	}
	
	@Test(priority = 8)
	public void testGetMaximumIdsException() throws InsightsCustomException {
		try {
			JsonObject ConfigIdJson = new JsonObject();
			JsonObject response = workflowController.getMaximumExecutionIds(ConfigIdJson.toString());
			String actual = response.get("status").getAsString().replace("\"", "");
			
			Assert.assertEquals(actual, PlatformServiceConstants.FAILURE);
			} catch (AssertionError e) {
			Assert.fail("testgetWorkflowExecutionRecords");
		}
	}
	
	@Test(priority = 9)
	public void testDownloadPDF() throws InsightsCustomException, InterruptedException, IOException {
		Thread.sleep(1000);
		try {
			InsightsReportVisualizationContainer emailHistoryConfig = new InsightsReportVisualizationContainer();
			emailHistoryConfig.setMailId(1);
			emailHistoryConfig.setExecutionId(123456789L);
			emailHistoryConfig.setMailCC("demo@abc.com");
			emailHistoryConfig.setMailFrom("demo1@abc.com");
			emailHistoryConfig.setMailTo("demo2@abc.com");
			emailHistoryConfig.setWorkflowConfig(workflowId);
			emailHistoryConfig.setAttachmentData(emailHistoryConfig.toString().getBytes());
			workflowConfigDAL.saveEmailExecutionHistory(emailHistoryConfig);
			
			InsightsReportVisualizationContainer record = workflowConfigDAL.getEmailExecutionHistoryByWorkflowId(workflowId.toString());
			long executionId = record.getExecutionId();
			
			JsonObject pdfDetailsJson = new JsonObject();
			pdfDetailsJson.addProperty("workflowId", workflowId);
			pdfDetailsJson.addProperty("executionId", executionId);
			String input= pdfDetailsJson.toString().replace("\n", "").replace("\r", "");
			String encodeString = new String(Base64.getEncoder().encodeToString(input.getBytes()));
			ResponseEntity<byte[]> response = workflowController.getReportPDF(encodeString);
			} 
		catch (AssertionError e) {
			Assert.fail("testgetWorkflowExecutionRecords");
		}
	}

	@Test(priority = 10)
	public void testEnableHealthStatus() throws InsightsCustomException {
		try {
			JsonObject statusJson = new JsonObject();
			statusJson.addProperty("status", true);
			
			JsonObject response = workflowController.enableHealthNotification(statusJson.toString());
			String actual = response.get("status").getAsString().replace("\"", "");
			
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
			InsightsWorkflowConfiguration workflowConfig = workflowConfigDAL
					.getWorkflowConfigByWorkflowId(system_workflowId);
			Assert.assertNotNull(workflowConfig);
			Assert.assertTrue(workflowConfig.isActive());
			Assert.assertNotNull(workflowConfig.getEmailConfig());
			InsightsWorkflowType workflowTypeObj = workflowConfigDAL
					.getWorkflowType(WorkflowTaskEnum.WorkflowType.SYSTEM.getValue());
			Assert.assertNotNull(workflowTypeObj);
			workflowTypeId = workflowTypeObj.getId();
			InsightsWorkflowTask systemNotificationTask = workflowConfigDAL
					.getTaskByChannel("WORKFLOW.SYSTEM_TASK.SYSTEMNOTIFICATION.EXECUTION");
			Assert.assertNotNull(systemNotificationTask);
			systemHealthTaskId = systemNotificationTask.getTaskId();
			InsightsWorkflowTask emailNotificationTask = workflowConfigDAL
					.getTaskByChannel("WORKFLOW.SYSTEM_TASK.EMAIL.EXCECUTION");
			Assert.assertNotNull(emailNotificationTask);
			systemEmailTaskId = emailNotificationTask.getTaskId();
		} catch (AssertionError e) {
			Assert.fail("testEnableHealthStatus");
		}
	}
	
	@Test(priority = 11)
	public void testEnableHealthStatusException() throws InsightsCustomException {
		try {
			JsonObject statusJson = new JsonObject();
			statusJson.addProperty("statusss", true);
			
			JsonObject response = workflowController.enableHealthNotification(statusJson.toString());
			String actual = response.get("status").getAsString().replace("\"", "");
			
			Assert.assertEquals(actual, PlatformServiceConstants.FAILURE);
			} catch (AssertionError e) {
			Assert.fail("testEnableHealthStatus");
		}
	}

	@Test(priority = 12)
	public void testDisableHealthStatus() throws InsightsCustomException {
		try {
			JsonObject statusJson = new JsonObject();
			statusJson.addProperty("status", false);
			
			JsonObject response = workflowController.enableHealthNotification(statusJson.toString());
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
			InsightsWorkflowConfiguration workflowConfig = workflowConfigDAL
					.getWorkflowConfigByWorkflowId(system_workflowId);
			Assert.assertNotNull(workflowConfig);
			Assert.assertFalse(workflowConfig.isActive());
		} catch (AssertionError e) {
			Assert.fail("testEnableHealthStatus");
		}
	}

	@Test(priority = 13)
	public void testGetWorkflowtaskDetails() throws InsightsCustomException {
		try {
			JsonObject response = workflowController.getTaskDetail();
			String actual = response.get("status").getAsString().replace("\"", "");
			
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
			
			JsonArray taskList = workflowService.getTaskDetail();
			Assert.assertNotNull(taskList);
			Assert.assertTrue(taskList.size() > 0);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 14)
	public void testGetAllWorkflowType() throws InsightsCustomException {
		try {
			List<String> taskList = workflowController.getWorkflowType();
			Assert.assertNotNull(taskList);
			Assert.assertTrue(taskList.size() > 0);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 15)
	public void testDeleteTaskDetails() throws InsightsCustomException {
		try {
			int response = workflowService.saveWorkflowTask(testData.get("workflowTaskData").getAsJsonObject());
			InsightsWorkflowTask task = workflowConfigDAL.getTaskByTaskId(response);
			taskID = task.getTaskId();
			JsonObject responseJson = workflowController.deleteWorkflowtask(taskID);
			String actual = responseJson.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 16)
	public void testDeleteTaskDetailsException() throws InsightsCustomException {
			JsonObject responseJson = workflowController.deleteWorkflowtask(taskID+123);
			String actual = responseJson.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.FAILURE);
	}
	

	@Test(priority = 17)
	public void testUpdateTaskDetails() throws InsightsCustomException {
		try {
			int taskId = workflowService.saveWorkflowTask(testData.get("workflowTaskData").getAsJsonObject());
			getWorkflowTaskData(taskId);
			JsonObject responseJson = workflowController.updateWorkflowTask(updateWorkflowTaskData);
			String actual = responseJson.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
			
		} catch (AssertionError e) {

			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 18)
	public void testUpdateTaskDetailsException() throws InsightsCustomException {
		try {
			JsonObject response = workflowController.updateWorkflowTask(workflowTaskDataUpdateException);
			String actual = response.get("status").getAsString();
			Assert.assertEquals(actual, PlatformServiceConstants.FAILURE);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 19)
	public void testUpdateTaskDetailsValidation() throws InsightsCustomException {
		try {
			JsonObject response = workflowController.updateWorkflowTask(updateWorkflowTaskDataValidation);
			String actual = response.get("status").getAsString();
			Assert.assertEquals(actual, PlatformServiceConstants.FAILURE);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 20)
	public void testGetHealthNotificationStatus() throws InsightsCustomException {
		try {
			JsonObject response = workflowController.getTaskList();
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
			JsonObject taskList = workflowService.getHealthNotificationStatus();
			Assert.assertNotNull(taskList);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 21)
	public void testGetLatestExecutionId() throws InsightsCustomException {
		try {
			JsonObject response = workflowController.getLatestExecutionId(workflowId);
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertNotNull(response);
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
		} 
		catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 22)
	public void testconvertStringIntoJson() throws InsightsCustomException {
		try {
			JsonObject taskList = workflowService.convertStringIntoJson(testData.get("registerkpiWorkflow").toString());
			Assert.assertNotNull(taskList);
			} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 23)
	public void testGetWorkFlowExecutionRecordsByWorkflowId() throws InsightsCustomException {
		try {
			String workflowIdData = "{\"workflowId\":" + workflowId + "}";
			JsonObject workflowTaskAsJson = convertStringIntoJson(workflowIdData);
			
			JsonObject response = workflowController.getWorkflowExecutionRecordsByWorkflowId(workflowIdData);
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.SUCCESS);
			
			JsonObject taskList = workflowService.getWorkFlowExecutionRecordsByWorkflowId(workflowTaskAsJson);
			Assert.assertNotNull(taskList);
			Assert.assertTrue(taskList.size() > 0);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 24)
	public void testGetWorkFlowExecutionRecordsByWorkflowIdException() throws InsightsCustomException {
		try {
			String workflowIdData = "{\"workflowIdd\":" + workflowId + "}";
			JsonObject response = workflowController.getWorkflowExecutionRecordsByWorkflowId(workflowIdData);
			String actual = response.get("status").getAsString().replace("\"", "");
			Assert.assertEquals(actual, PlatformServiceConstants.FAILURE);
			
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 25, expectedExceptions = InsightsCustomException.class)
	public void testSaveWorkflowConfig() throws InsightsCustomException {
		try {
			InsightsWorkflowConfiguration taskList = workflowService.saveWorkflowConfig(workflowId, true, true, null,
					null, null, null, 0, null, false);
			Assert.assertNotNull(taskList);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 26)
	public void testSaveWorkflowTaskValidationError() throws InsightsCustomException {
			JsonObject response = workflowController.saveAssessmentTask("&amp;"+testData.get("workflowTaskDataExceptionValidation").toString());
			String actual = response.get("status").getAsString();
			Assert.assertEquals(actual, PlatformServiceConstants.FAILURE);
	}
	
	@Test(priority = 27)
	public void testEnableHealthStatusValidationError() throws InsightsCustomException {
			JsonObject statusJson = new JsonObject();
			statusJson.addProperty("&amp;{<status", true);
			
			JsonObject response = workflowController.enableHealthNotification(statusJson.toString());
			String actual = response.get("status").getAsString().replace("\"", "");
			
			Assert.assertEquals(actual, PlatformServiceConstants.FAILURE);
	}
	
	@AfterClass
	public void cleanUp() {

		// Delete execution history
		try {
			workflowConfigDAL.deleteExecutionHistory(historyid);
		} catch (Exception e) {
			log.error("Error cleaning data up at WorkflowServiceTest Execution History Record ", e);
		}

		// Delete assessment reports

		List<String> assessmentReportNames = Arrays
				.asList(dailyAssessmentReportWorkflowJson.get("reportName").getAsString());
		for (String assessmentReport : assessmentReportNames) {
			try {
				InsightsAssessmentConfiguration assessment = reportConfigDAL
						.getAssessmentByAssessmentName(assessmentReport.trim());
				String workflowID = assessment.getWorkflowConfig().getWorkflowId();
				int configID = assessment.getId();
				reportConfigDAL.deleteAssessmentReport(configID);
				workflowConfigDAL.deleteWorkflowTaskSequence(workflowID);
			} catch (Exception e) {
				log.error("Error cleaning data up at WorkflowServiceTest Assessment Record ", e);
			}
		}

		try {
			workflowConfigDAL.deleteWorkflowTaskSequence(system_workflowId);
		} catch (Exception e) {
			log.error("Error cleaning data up at System Task sequence", e);
		}

		try {
			String status = workflowConfigDAL.deleteEmailTemplateByWorkflowId(system_workflowId);
		} catch (Exception e) {
			log.error("Error cleaning data up at System Email task ", e);
		}

		try {
			workflowConfigDAL.deleteWorkflowConfig(system_workflowId);
		} catch (Exception e) {
			log.error("Error cleaning data up at System Task sequence", e);
		}

		// Delete tasks
		try {
			String status = workflowConfigDAL.deleteTask(taskID);
		} catch (Exception e) {
			log.error("Error cleaning data up at WorkflowServiceTest KPI task ", e);
		}
		try {
			String statusEmail = workflowConfigDAL.deleteTask(emailTaskID);
		} catch (Exception e) {
			log.error("Error cleaning data up at WorkflowServiceTest Email task ", e);
		}

		try {
			String status = workflowConfigDAL.deleteTask(systemHealthTaskId);
		} catch (Exception e) {
			log.error("Error cleaning data up at System Notification task ", e);
		}

		try {
			String status = workflowConfigDAL.deleteTask(systemEmailTaskId);
		} catch (Exception e) {
			log.error("Error cleaning data up at System Email task ", e);
		}

		try {
			String status = workflowConfigDAL.deleteWorkflowType(workflowTypeId);
		} catch (Exception e) {
			log.error("Error cleaning data up at WorkflowType", e);
		}

		// Delete Report Templates
		try {
			reportConfigDAL.deleteReportTemplatebyReportID(reportId);
		} catch (Exception e) {
			log.error("Error cleaning data up at WorkflowServiceTest Report template ", e);
		}

		// Delete KPI
		try {
			reportConfigDAL.deleteKPIbyKpiID(testData.get("registerkpiWorkflowJson").getAsJsonObject().get("kpiId").getAsInt());
		} catch (Exception e) {
			log.error("Error cleaning data up at WorkflowServiceTest KPI ID ", e);
		}

	}

}