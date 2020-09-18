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
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.assessmentreport.InsightsAssessmentConfiguration;
import com.cognizant.devops.platformdal.assessmentreport.ReportConfigDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTask;
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
	String workflowId = null;

	@BeforeTest
	public void prepareData() throws InsightsCustomException {
		// save kpi
		try {
			int response = assessmentService.saveKpiDefinition(registerkpiWorkflowJson);
		} catch (Exception e) {
			log.error("Error preparing data at WorkflowServiceTest KPI record ", e);
		}

		try {
			int reportId = assessmentService.saveTemplateReport(reportTemplateWorkflowJson);
		} catch (Exception e) {
			log.error("Error preparing data at WorkflowServiceTest Report template record ", e);
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
			String dailyAssessmentReportWorkflow = "{\"reportName\":\"Daily_Deployment_Workflow\",\"reportTemplate\":123456,\"emailList\":\"fdfsfsdfs\",\"schedule\":\"DAILY\",\"startdate\":null,\"isReoccuring\":true,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
					+ taskID
					+ ",\"sequence\":0}],\"asseementreportdisplayname\":\"Report_test\",\"emailDetails\":null}";
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
			assessmentId = assessmentService.saveAssessmentReport(dailyAssessmentReportWorkflowJson);
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

	@AfterTest
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

		// Delete Report Templates
		try {
			reportConfigDAL.deleteReportTemplatebyReportID(reportTemplateWorkflowJson.get("reportId").getAsInt());
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
