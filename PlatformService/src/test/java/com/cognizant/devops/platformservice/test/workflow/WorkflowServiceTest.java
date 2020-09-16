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

	@BeforeClass
	public void prepareData() throws InsightsCustomException {
		// save kpi
		try {
			int response = assessmentService.saveKpiDefinition(registerkpiWorkflowJson);

			// save report template
			int reportId = assessmentService.saveTemplateReport(reportTemplateWorkflowJson);
		} catch (Exception e) {
			log.error("Error preparing data at WorkflowServiceTest ", e);
		}

	}

	@Test(priority = 1)
	public void testsaveWorkflowTask() throws InsightsCustomException {
		int response = workflowService.saveWorkflowTask(workflowTaskAsJson);
		int responseEmail = workflowService.saveWorkflowTask(workflowEmailTaskAsJson);
		InsightsWorkflowTask task = workflowConfigDAL.getTaskByTaskId(response);
		InsightsWorkflowTask emailtask = workflowConfigDAL.getTaskByTaskId(responseEmail);
		Assert.assertNotNull(task);
		Assert.assertNotNull(emailtask);
		taskID = task.getTaskId();
		emailTaskID = emailtask.getTaskId();
		String dailyAssessmentReportWorkflow = "{\"reportName\":\"Daily_Deployment_Workflow\",\"reportTemplate\":123456,\"emailList\":\"fdfsfsdfs\",\"schedule\":\"DAILY\",\"startdate\":null,\"isReoccuring\":true,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"
				+ taskID + ",\"sequence\":0}],\"asseementreportdisplayname\":\"Report_test\",\"emailDetails\":null}";
		dailyAssessmentReportWorkflowJson = convertStringIntoJson(dailyAssessmentReportWorkflow);
	}

	@Test(priority = 2, expectedExceptions = InsightsCustomException.class)
	public void testsaveIncorrectWorkflowTask() throws InsightsCustomException, IOException {
		int response = workflowService.saveWorkflowTask(incorrectWorkflowTaskJson);
	}

	@Test(priority = 3)
	public void testgetWorkflowTaskList() throws InsightsCustomException {
		JsonArray taskList = workflowService.getTaskList(workflowType);
		Assert.assertNotNull(taskList);
		Assert.assertTrue(taskList.size() > 0);
	}

	@Test(priority = 4)
	public void testgetWorkflowExecutionRecords() throws InsightsCustomException {
		int assessmentid = assessmentService.saveAssessmentReport(dailyAssessmentReportWorkflowJson);
		InsightsAssessmentConfiguration assessmentObj = reportConfigDAL
				.getAssessmentByAssessmentName(dailyAssessmentReportWorkflowJson.get("reportName").getAsString());
		historyid = updateWorkflowExecutionHistory(assessmentObj.getWorkflowConfig().getWorkflowId(), taskID);
		JsonObject configIdJson = new JsonObject();
		configIdJson.addProperty("configid", assessmentObj.getId());
		JsonArray records = workflowService.getWorkFlowExecutionRecords(configIdJson).getAsJsonArray("records");
		String expected = workflowTaskAsJson.get("description").getAsString();
		Assert.assertNotNull(records);
		Assert.assertTrue(records.size() > 0);
		Assert.assertEquals(records.get(0).getAsJsonObject().get("currentTask").getAsString(), expected);
	}



	@AfterClass
	public void cleanUp() {

		// Delete execution history
		try {
			workflowConfigDAL.deleteExecutionHistory(historyid);

			// Delete assessment reports
			List<String> assessmentReportNames = Arrays
					.asList(dailyAssessmentReportWorkflowJson.get("reportName").getAsString());
			for (String assessmentReport : assessmentReportNames) {
				InsightsAssessmentConfiguration assessment = reportConfigDAL
						.getAssessmentByAssessmentName(assessmentReport.trim());
				String workflowID = assessment.getWorkflowConfig().getWorkflowId();
				int configID = assessment.getId();
				reportConfigDAL.deleteAssessmentReport(configID);
				workflowConfigDAL.deleteWorkflowTaskSequence(workflowID);
			}

			// Delete tasks
			String status = workflowConfigDAL.deleteTask(taskID);
			String statusEmail = workflowConfigDAL.deleteTask(emailTaskID);

			// Delete Report Templates
			reportConfigDAL.deleteReportTemplatebyReportID(reportTemplateWorkflowJson.get("reportId").getAsInt());

			// Delete KPI
			reportConfigDAL.deleteKPIbyKpiID(registerkpiWorkflowJson.get("kpiId").getAsInt());
		} catch (Exception e) {
			log.error("Error cleaning data up at WorkflowServiceTest ", e);
		}

	}

}
