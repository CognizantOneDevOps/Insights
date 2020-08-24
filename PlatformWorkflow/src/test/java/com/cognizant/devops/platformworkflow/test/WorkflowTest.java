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
package com.cognizant.devops.platformworkflow.test;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.assessmentreport.ReportConfigDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowExecutionHistory;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformworkflow.workflowtask.app.PlatformWorkflowApplication;
import com.cognizant.devops.platformworkflow.workflowtask.core.WorkflowDataHandler;
import com.cognizant.devops.platformworkflow.workflowtask.core.WorkflowExecutor;
import com.cognizant.devops.platformworkflow.workflowtask.core.WorkflowRetryExecutor;
import com.cognizant.devops.platformworkflow.workflowtask.utils.PlatformWorkflowApplicationTest;
import com.cognizant.devops.platformworkflow.workflowthread.core.WorkflowThreadPool;

public class WorkflowTest extends WorkflowTestData {

	WorkflowDAL workflowDAL = new WorkflowDAL();
	ReportConfigDAL reportConfigDAL = new ReportConfigDAL();
	WorkflowDataHandler workflowHandler = new WorkflowDataHandler();
	
	
	@BeforeTest
	public void onInit() throws InsightsCustomException, InterruptedException {
		
		ApplicationConfigCache.loadConfigCache();
		
		//save kpi definition in db
		saveKpiDefinition(kpiDefinition);
		
		//save content definition in db
		saveContentDefinition(contentDefinition);
		
		//save workflow tasks in db
		saveWorkflowTask(workflowTask);
		saveWorkflowTask(failWorkflowTask);
		saveWorkflowTask(wrongWorkflowTask);
		
		//save report template in db
		saveReportTemplate(reportTemplate);
		
		//save assessment reports in db 
		saveAssessmentReport(workflowId, mqChannel, assessmentReport, null);
		saveAssessmentReport(failWorkflowId, mqChannelFail, assessmentReportFail, null);
		saveAssessmentReport(WorkflowIdWrongTask, wrongMqChannel, reportWithWrongTask, null);
		saveAssessmentReport(WorkflowIdWith2Task, mqChannel, assessmentReportWith2Task, mqChannelFail);
		saveAssessmentReport(WorkflowIdTest, mqChannel, assessmentReportTest, wrongMqChannel);

		initializeTask();
		
		WorkflowThreadPool.getInstance();

		PlatformWorkflowApplication.workflowExecutor();

		PlatformWorkflowApplicationTest.testWorkflowExecutor();

		//run workflow executor 
		WorkflowExecutor executor = new WorkflowExecutor();
		executor.executeWorkflow();
		Thread.sleep(3000);
		
	}
	
	@Test(priority = 1)
	public void testValidateStatusUpdate() {
		InsightsWorkflowConfiguration workflowConfig = workflowDAL.getWorkflowConfigByWorkflowId(workflowId);		
		Assert.assertEquals(workflowConfig.getStatus(), "COMPLETED");
		Assert.assertTrue(workflowConfig.getNextRun() > nextRunDaily);

	}
	
	
	@Test(priority = 2)
	public void testExecutionHistoryUpdate() throws InterruptedException {
		List<InsightsWorkflowExecutionHistory> executionHistory = workflowDAL.getWorkflowExecutionHistoryRecordsByWorkflowId(workflowId);
		if(executionHistory.size() > 0) {
			for(InsightsWorkflowExecutionHistory eachExecutionRecord: executionHistory) {
				Assert.assertEquals(eachExecutionRecord.getTaskStatus(), "COMPLETED");	
			}
		}		
	}
	
	@Test(priority = 3)
	public void testFailTaskStatusUpdate() {
		InsightsWorkflowConfiguration workflowConfig = workflowDAL.getWorkflowConfigByWorkflowId(failWorkflowId);		
		Assert.assertEquals(workflowConfig.getStatus(), "ERROR");
		Assert.assertTrue(workflowConfig.getNextRun() == nextRunDaily);
	}
	
	@Test(priority = 4)
	public void testFailTaskExecutionHistoryUpdate() throws InterruptedException {
		List<InsightsWorkflowExecutionHistory> executionHistory = workflowDAL.getWorkflowExecutionHistoryRecordsByWorkflowId(failWorkflowId);
		if(executionHistory.size() > 0) {
			for(InsightsWorkflowExecutionHistory eachExecutionRecord: executionHistory) {
				Assert.assertEquals(eachExecutionRecord.getTaskStatus(), "ERROR");
			}
		}		
	}
	
	//if workflow task is not initialized or subscribed properly
	@Test(priority = 5)
	public void testWrongTaskStatusUpdate() {
		InsightsWorkflowConfiguration workflowConfig = workflowDAL.getWorkflowConfigByWorkflowId(WorkflowIdWrongTask);		
		Assert.assertEquals(workflowConfig.getStatus(), "TASK_INITIALIZE_ERROR");
		Assert.assertTrue(workflowConfig.getNextRun() == nextRunDaily);
	}
	
	@Test(priority = 6)
	public void testStatusUpdateWith2Task() {
		InsightsWorkflowConfiguration workflowConfig = workflowDAL.getWorkflowConfigByWorkflowId(WorkflowIdWith2Task);		
		Assert.assertEquals(workflowConfig.getStatus(), "ERROR");
		int task1 = getTaskId(mqChannel);
		List<InsightsWorkflowExecutionHistory> executionHistory = workflowDAL.getWorkflowExecutionHistoryRecordsByWorkflowId(WorkflowIdWith2Task);
		if(executionHistory.size() > 0) {
			for(InsightsWorkflowExecutionHistory eachExecutionRecord: executionHistory) {
				if(eachExecutionRecord.getCurrenttask() == task1) {
					Assert.assertEquals(eachExecutionRecord.getTaskStatus(), "COMPLETED");
				} else {
					Assert.assertEquals(eachExecutionRecord.getTaskStatus(), "ERROR");
				}
			}
		}
	}
	
	@Test(priority = 7)
	public void testWorkflowRetryCount() throws InterruptedException {
		WorkflowRetryExecutor retryExecutor = new WorkflowRetryExecutor();
		retryExecutor.retryWorkflows();
		Thread.sleep(1000);
		List<InsightsWorkflowExecutionHistory> executionHistory = workflowDAL.getWorkflowExecutionHistoryRecordsByWorkflowId(failWorkflowId);
		if(executionHistory.size() > 0) {
			for(InsightsWorkflowExecutionHistory eachExecutionRecord: executionHistory) {
				Assert.assertTrue(eachExecutionRecord.getRetryCount()>0);
				Assert.assertEquals(eachExecutionRecord.getTaskStatus(), "ERROR");
			}
		}
		
	}
	
	@Test(priority = 8)
	public void testAbortStatusUpdate() throws InterruptedException {
		ApplicationConfigProvider.getInstance().getAssessmentReport().setMaxWorkflowRetries(1);
		WorkflowRetryExecutor executorRetry = new WorkflowRetryExecutor();
		executorRetry.retryWorkflows();
		Thread.sleep(1000);
		List<InsightsWorkflowExecutionHistory> executionHistory = workflowDAL.getWorkflowExecutionHistoryRecordsByWorkflowId(failWorkflowId);
		if(executionHistory.size() > 0) {
			for(InsightsWorkflowExecutionHistory eachExecutionRecord: executionHistory) {
				Assert.assertEquals(eachExecutionRecord.getTaskStatus(), "ABORTED");
			}
		}		
	}
	
	@AfterTest
	public void cleanUp() {
		
		delete(workflowId);
		delete(failWorkflowId);
		delete(WorkflowIdWith2Task);
		delete(WorkflowIdWrongTask);
		delete(WorkflowIdTest);
		
		//delete workflow task
		for(int element: taskidList) {
			workflowDAL.deleteTask(element);
		}
		
		//delete report template
		reportConfigDAL.deleteReportTemplatebyReportID(reportTemplateJson.get("reportId").getAsInt());

		//delete content
		reportConfigDAL.deleteContentbyContentID(contentDefinitionJson.get("contentId").getAsInt());
		
		//delete kpi
		reportConfigDAL.deleteKPIbyKpiID(kpiDefinitionJson.get("kpiId").getAsInt());

	}
}

