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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.assessmentreport.InsightsAssessmentConfiguration;
import com.cognizant.devops.platformdal.assessmentreport.ReportConfigDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTask;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowType;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformservice.assessmentreport.service.AssesmentReportServiceImpl;
import com.cognizant.devops.platformservice.workflow.service.WorkflowServiceImpl;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class WorkflowServiceTest extends WorkflowServiceTestData {
	private static final Logger log = LogManager.getLogger(WorkflowServiceTest.class);
	public static final AssesmentReportServiceImpl assessmentService = new AssesmentReportServiceImpl();
	WorkflowServiceImpl workflowService = new WorkflowServiceImpl();
	WorkflowDAL workflowConfigDAL = new WorkflowDAL();
	ReportConfigDAL reportConfigDAL = new ReportConfigDAL();
	public JsonObject dailyAssessmentReportWorkflowJson = null;
	int taskID = 0;
	int emailTaskID = 0;
	int assessmentId = 0;
	int systemHealthTaskId = 0;
	int systemEmailTaskId = 0;
	int workflowTypeId = 0;
	String workflowId = null;
	String system_workflowId = "SYSTEM_HealthNotification";

	@BeforeClass
	public void prepareData() throws InsightsCustomException {
		// save kpi
		try {
			int response = assessmentService.saveKpiDefinition(registerkpiWorkflowJson);
		} catch (Exception e) {
			log.error("Error preparing data at WorkflowServiceTest KPI record ", e);
		}

		try {
			reportId = assessmentService.saveTemplateReport(reportTemplateWorkflowJson);
		} catch (Exception e) {
			log.error("Error preparing data at WorkflowServiceTest Report template record ", e);
		}

		try {
			InsightsWorkflowType type = new InsightsWorkflowType();
			type.setWorkflowType(WorkflowTaskEnum.WorkflowType.REPORT.getValue());
			workflowConfigDAL.saveWorkflowType(type);
		} catch (Exception e) {
			log.error("Error preparing data at WorkflowServiceTest workflowtype record ", e);
		}

	}

	@Test(priority = 1)
	public void testsaveWorkflowTask() throws InsightsCustomException {
		try {
			int response = workflowService.saveWorkflowTask(workflowTaskAsJson);
			int responseEmail = workflowService.saveWorkflowTask(workflowEmailTaskAsJson);
			InsightsWorkflowTask task = workflowConfigDAL.getTaskByTaskId(response);
			InsightsWorkflowTask emailtask = workflowConfigDAL.getTaskByTaskId(responseEmail);
			Assert.assertNotNull(task);
			Assert.assertNotNull(emailtask);
			taskID = task.getTaskId();
			emailTaskID = emailtask.getTaskId();
			String dailyAssessmentReportWorkflow = "{\"reportName\":\"Daily_Deployment_Workflow\",\"reportTemplate\":"
					+ reportId
					+ ",\"emailList\":\"fdfsfsdfs\",\"schedule\":\"DAILY\",\"startdate\":null,\"isReoccuring\":true,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
					+ taskID
					+ ",\"sequence\":0}],\"asseementreportdisplayname\":\"Report_test\",\"emailDetails\":null,\"orgName\":\"Test Org\",\"userName\":\"Test_User\"}";
			dailyAssessmentReportWorkflowJson = convertStringIntoJson(dailyAssessmentReportWorkflow);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 2, expectedExceptions = InsightsCustomException.class)
	public void testsaveIncorrectWorkflowTask() throws InsightsCustomException, IOException {
		int response = workflowService.saveWorkflowTask(incorrectWorkflowTaskJson);
	}

	@Test(priority = 3)
	public void testgetWorkflowTaskList() throws InsightsCustomException {
		try {
			JsonArray taskList = workflowService.getTaskList(workflowType);
			Assert.assertNotNull(taskList);
			Assert.assertTrue(taskList.size() > 0);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 4)
	public void testgetWorkflowExecutionRecords() throws InsightsCustomException, InterruptedException {
		Thread.sleep(1000);
		InsightsAssessmentConfiguration assessmentObj = null;
		try {
			JsonObject assessmentResponse = assessmentService.saveAssessmentReport(dailyAssessmentReportWorkflowJson);
			assessmentId = assessmentResponse.get("assessmentReportId").getAsInt();
			assessmentObj = reportConfigDAL
					.getAssessmentByAssessmentName(dailyAssessmentReportWorkflowJson.get("reportName").getAsString());
			workflowId = assessmentObj.getWorkflowConfig().getWorkflowId();
			historyid = updateWorkflowExecutionHistory(assessmentObj.getWorkflowConfig().getWorkflowId(), taskID);
			JsonObject configIdJson = new JsonObject();
			configIdJson.addProperty("configid", assessmentObj.getId());
			JsonArray records = workflowService.getWorkFlowExecutionRecords(configIdJson).getAsJsonArray("records");
			String expected = workflowTaskAsJson.get("description").getAsString();
			Assert.assertNotNull(records);
			Assert.assertTrue(records.size() > 0);
			Assert.assertEquals(records.get(0).getAsJsonObject().get("currentTask").getAsString(), expected);
		} catch (AssertionError e) {
			Assert.fail("testgetWorkflowExecutionRecords");
		}
	}

	@Test(priority = 5)
	public void testgetMaximumIds() throws InsightsCustomException {
		try {
			JsonObject ConfigIdJson = new JsonObject();
			ConfigIdJson.addProperty("configid", assessmentId);
			JsonObject records = workflowService.getMaximumExecutionIDs(ConfigIdJson);
			Assert.assertNotNull(records);
			Assert.assertEquals(records.get("status").getAsBoolean(), false);
			Assert.assertEquals(records.get("executionId").getAsInt(), -1);
		} catch (AssertionError e) {
			Assert.fail("testgetWorkflowExecutionRecords");
		}
	}

	@Test(priority = 6, expectedExceptions = InsightsCustomException.class)
	public void testDownloadPDF() throws InsightsCustomException {
		try {
			JsonObject pdfDetailsJson = new JsonObject();
			pdfDetailsJson.addProperty("workflowId", workflowId);
			pdfDetailsJson.addProperty("executionId", 1600167977000l);
			byte[] fileContent = workflowService.getReportPDF(pdfDetailsJson);
		} catch (AssertionError e) {
			Assert.fail("testgetWorkflowExecutionRecords");
		}
	}

	@Test(priority = 7)
	public void testEnableHealthStatus() throws InsightsCustomException {
		try {
			JsonObject statusJson = new JsonObject();
			statusJson.addProperty("status", true);
			String response = workflowService.updateHealthNotification(statusJson);
			Assert.assertEquals(response, PlatformServiceConstants.SUCCESS);
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

	@Test(priority = 8)
	public void testDisableHealthStatus() throws InsightsCustomException {
		try {
			JsonObject statusJson = new JsonObject();
			statusJson.addProperty("status", false);
			String response = workflowService.updateHealthNotification(statusJson);
			Assert.assertEquals(response, PlatformServiceConstants.SUCCESS);
			InsightsWorkflowConfiguration workflowConfig = workflowConfigDAL
					.getWorkflowConfigByWorkflowId(system_workflowId);
			Assert.assertNotNull(workflowConfig);
			Assert.assertFalse(workflowConfig.isActive());
		} catch (AssertionError e) {
			Assert.fail("testEnableHealthStatus");
		}
	}

	@Test(priority = 9)
	public void testgetWorkflowtaskDetails() throws InsightsCustomException {
		try {
			JsonArray taskList = workflowService.getTaskDetail();
			Assert.assertNotNull(taskList);
			Assert.assertTrue(taskList.size() > 0);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 10)
	public void testgetAllWorkflowType() throws InsightsCustomException {
		try {
			List<String> taskList = workflowService.getAllWorkflowTypes();
			Assert.assertNotNull(taskList);
			Assert.assertTrue(taskList.size() > 0);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 11)
	public void testdeleteTaskDetails() throws InsightsCustomException {
		try {

			int response = workflowService.saveWorkflowTask(workflowTaskAsJson);
			InsightsWorkflowTask task = workflowConfigDAL.getTaskByTaskId(response);
			taskID = task.getTaskId();
			boolean status = workflowService.deleteTaskDetail(taskID);
			Assert.assertTrue(status);

		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 12)
	public void testUpdateTaskDetails() throws InsightsCustomException {
		try {
			int taskId = workflowService.saveWorkflowTask(workflowTaskAsJson);

			String workflowTaskData = "{\"taskId\":" + taskId
					+ ",\"description\": \"demo\",\"mqChannel\": \"WORKFLOW.WORKFLOWSERVICE_TEST.TASK.KPI.EXCECUTION\",\"componentName\": \"com.cognizant.devops.platformreports.assessment.core.ReportKPISubscriber\",\"dependency\": \"100\",\"workflowType\": \"Report\"}";
			log.debug(workflowTaskData);
			JsonObject workflowTaskAsJson = convertStringIntoJson(workflowTaskData);
			int status = workflowService.updateWorkflowTask(workflowTaskAsJson);
			Assert.assertEquals(status, 0);

		} catch (AssertionError e) {

			Assert.fail(e.getMessage());
		}
	}



	@Test(priority = 13)
	public void testgetHealthNotificationStatus() throws InsightsCustomException {
		try {
			JsonObject taskList = workflowService.getHealthNotificationStatus();
			Assert.assertNotNull(taskList);
			Assert.assertTrue(taskList.size() > 0);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 14)
	public void testgetLatestExecutionId() throws InsightsCustomException {
		try {
			JsonObject taskList = workflowService.getLatestExecutionId(workflowId);
			Assert.assertNotNull(taskList);
			Assert.assertTrue(taskList.size() > 0);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 15)
	public void testconvertStringIntoJson() throws InsightsCustomException {
		try {
			JsonObject taskList = workflowService.convertStringIntoJson(registerkpiWorkflow);
			Assert.assertNotNull(taskList);
			Assert.assertTrue(taskList.size() > 0);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 16)
	public void testgetWorkFlowExecutionRecordsByWorkflowId() throws InsightsCustomException {
		try {
			String workflowIdData = "{\"workflowId\":" + workflowId + "}";
			JsonObject workflowTaskAsJson = convertStringIntoJson(workflowIdData);
			JsonObject taskList = workflowService.getWorkFlowExecutionRecordsByWorkflowId(workflowTaskAsJson);
			Assert.assertNotNull(taskList);
			Assert.assertTrue(taskList.size() > 0);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	// , expectedExceptions = InsightsCustomException.class
	@Test(priority = 17, expectedExceptions = InsightsCustomException.class)
	public void testsaveWorkflowConfig() throws InsightsCustomException {
		try {
			InsightsWorkflowConfiguration taskList = workflowService.saveWorkflowConfig(workflowId, true, true, null,
					null, null, null, 0, null, false);
			Assert.assertNotNull(taskList);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 18, expectedExceptions = InsightsCustomException.class)
	public void testEnableHealthStatusForException() throws InsightsCustomException {
		JsonObject statusJson = new JsonObject();
		workflowService.updateHealthNotification(statusJson);
	}

	@Test(priority = 19, expectedExceptions = InsightsCustomException.class)
	public void testgetWorkFlowExecutionRecordsByWorkflowIdForException() throws InsightsCustomException {
		JsonObject statusJson = new JsonObject();
		workflowService.getWorkFlowExecutionRecordsByWorkflowId(statusJson);
	}

	@Test(priority = 20, expectedExceptions = InsightsCustomException.class)
	public void testsaveWorkflowTaskForException() throws InsightsCustomException {
		JsonObject statusJson = new JsonObject();
		workflowService.saveWorkflowTask(statusJson);
	}

	@Test(priority = 21, expectedExceptions = InsightsCustomException.class)
	public void testUpdateTaskDetailsForException() throws InsightsCustomException {
		JsonObject statusJson = new JsonObject();
		workflowService.updateWorkflowTask(statusJson);
	}

	@Test(priority = 22, expectedExceptions = InsightsCustomException.class)
	public void testgetWorkflowExecutionRecordsForException() throws InsightsCustomException {
		JsonObject statusJson = new JsonObject();
		workflowService.getWorkFlowExecutionRecords(statusJson);
	}

	@Test(priority = 23, expectedExceptions = InsightsCustomException.class)
	public void testgetMaximumExecutionIDsForException() throws InsightsCustomException {
		JsonObject statusJson = new JsonObject();
		workflowService.getMaximumExecutionIDs(statusJson);
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
			reportConfigDAL.deleteKPIbyKpiID(registerkpiWorkflowJson.get("kpiId").getAsInt());
		} catch (Exception e) {
			log.error("Error cleaning data up at WorkflowServiceTest KPI ID ", e);
		}

	}

}