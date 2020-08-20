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
package com.cognizant.devops.platformreports.test;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.assessmentreport.InsightsContentConfig;
import com.cognizant.devops.platformdal.assessmentreport.InsightsKPIConfig;
import com.cognizant.devops.platformdal.assessmentreport.ReportConfigDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowExecutionHistory;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformworkflow.workflowtask.core.WorkflowExecutor;
import com.cognizant.devops.platformworkflow.workflowtask.core.WorkflowRetryExecutor;

public class AssessmentReportsTest extends AssessmentReportsTestData {
	
	ReportConfigDAL reportConfigDAL = new ReportConfigDAL();
	WorkflowDAL workflowDAL = new WorkflowDAL();
	
	@BeforeTest
	public void onInit() throws Exception {
		
		ApplicationConfigCache.loadConfigCache();
		
		//save multiple Kpi definition in db
		readKpiFileAndSave("KPIDefination.json");
		
		//save multiple content definition in db
		readContentFileAndSave("ContentsConfiguration.json");
		
		//save report template in db
		readReportTempFileAndSave("REPORT_SONAR_JENKINS_PROD_RT.json");
		readReportTempFileAndSave("REPORT_SONAR_RT.json");
		saveReportTemplate(reportTemplatekpi);
		saveReportTemplate(reportTemplatekpis);
		
		//save workflow task in db
		saveWorkflowTask(taskKpiExecution);
		saveWorkflowTask(taskPDFExecution);
		saveWorkflowTask(taskEmailExecution);
		
		
		//save assessment report
		saveAssessmentReport(workflowIdProd, assessmentReport, 3);
		saveAssessmentReport(workflowIdFail, assessmentReportFail, 1);

		initializeTask();
		
		//run workflow executor 
		WorkflowExecutor executor = new WorkflowExecutor();
		executor.executeWorkflow();
		Thread.sleep(70000);
		
	
	}

	@Test(priority = 1)
	public void testExecutionHistoryUpdateProd() throws InterruptedException {
		List<InsightsWorkflowExecutionHistory> executionHistory = workflowDAL.getWorkflowExecutionHistoryRecordsByWorkflowId(workflowIdProd);
		if(executionHistory.size() > 0) {
			for(InsightsWorkflowExecutionHistory eachExecutionRecord: executionHistory) {
				Assert.assertEquals(eachExecutionRecord.getTaskStatus(), WorkflowTaskEnum.WorkflowStatus.COMPLETED.toString());	
			}
		}		
	}	
	
	@Test(priority = 2)
	public void testValidateStatusUpdateProd() {
		InsightsWorkflowConfiguration workflowConfig = workflowDAL.getWorkflowConfigByWorkflowId(workflowIdProd);		
		Assert.assertEquals(workflowConfig.getStatus(), WorkflowTaskEnum.WorkflowStatus.COMPLETED.toString());
		Assert.assertTrue(workflowConfig.getNextRun() > nextRunBiWeekly);
	}
	
	
	@Test(priority = 3)
	public void testKpiResultProd() {
		List<InsightsWorkflowExecutionHistory> executionHistory = workflowDAL.getWorkflowExecutionHistoryRecordsByWorkflowId(workflowIdProd);
		long executionId = 0;
		if(executionHistory.size() > 0) {
			for(InsightsWorkflowExecutionHistory eachExecutionRecord: executionHistory) {
				executionId = eachExecutionRecord.getExecutionId();
			}
		}
		String query = "MATCH (n:KPI:RESULTS) with distinct max(n.executionId) as latestexecutionId " +
				"Match (b:KPI:RESULTS) where b.executionId = "+executionId+" and b.kpiId in [100252, 100253, 100254, 100112, 100901, 100903]  return latestexecutionId,b  LIMIT 25";

		Assert.assertNotNull(readNeo4jData(query));
	}
	
	@Test(priority = 4)
	public void testContentResultProd() {
		List<InsightsWorkflowExecutionHistory> executionHistory = workflowDAL.getWorkflowExecutionHistoryRecordsByWorkflowId(workflowIdProd);
		long executionId = 0;
		if(executionHistory.size() > 0) {
			for(InsightsWorkflowExecutionHistory eachExecutionRecord: executionHistory) {
				executionId = eachExecutionRecord.getExecutionId();
			}
		}
		String query = "MATCH (n:KPI:CONTENT_RESULT) with distinct max(n.executionId) as latestexecutionId " +
				"Match (b:KPI:CONTENT_RESULT) where b.executionId = "+executionId+" and b.kpiId in [100252, 100253, 100254, 100112, 100901, 100903]  return latestexecutionId,b  LIMIT 25";
		Assert.assertNotNull(readNeo4jData(query));
	}
	
	
	@Test(priority = 5)
	public void testValidateFailStatusUpdate() {
		InsightsWorkflowConfiguration workflowConfig = workflowDAL.getWorkflowConfigByWorkflowId(workflowIdFail);		
		Assert.assertEquals(workflowConfig.getStatus(), WorkflowTaskEnum.WorkflowStatus.ERROR.toString());
		Assert.assertTrue(workflowConfig.getNextRun() == nextRunDaily);
	}
	
	@Test(priority = 6)
	public void testFailExecutionHistoryUpdate() throws InterruptedException {
		List<InsightsWorkflowExecutionHistory> executionHistory = workflowDAL.getWorkflowExecutionHistoryRecordsByWorkflowId(workflowIdFail);
		if(executionHistory.size() > 0) {
			for(InsightsWorkflowExecutionHistory eachExecutionRecord: executionHistory) {
				Assert.assertEquals(eachExecutionRecord.getTaskStatus(), WorkflowTaskEnum.WorkflowStatus.ERROR.toString());	
			}
		}		
	}
	
	@Test(priority = 7)
	public void testRetryKpiExecute() throws InterruptedException {
		InsightsKPIConfig existingConfig = reportConfigDAL.getKPIConfig(100331);
		existingConfig.setCategory("COMPARISON");
		existingConfig.setdBQuery(querySonar);
		reportConfigDAL.updateKpiConfig(existingConfig);
		WorkflowRetryExecutor executorRetry = new WorkflowRetryExecutor();
		executorRetry.retryWorkflowWithFailedTask();
		Thread.sleep(40000);
	}
	
	@Test(priority = 8)
	public void testRetryContentExecute() throws InterruptedException {
		InsightsContentConfig existingContentConfig = reportConfigDAL.getContentConfig(20013131);
		existingContentConfig.setCategory("COMPARISON");
		reportConfigDAL.updateContentConfig(existingContentConfig);
		WorkflowRetryExecutor executorRetry = new WorkflowRetryExecutor();
		executorRetry.retryWorkflowWithFailedTask();
		Thread.sleep(40000);
	}
	
	@Test(priority = 9)
	public void testValidateStatusUpdateAfterRetry() throws InterruptedException {
		InsightsWorkflowConfiguration workflowConfig = workflowDAL.getWorkflowConfigByWorkflowId(workflowIdFail);		
		Assert.assertEquals(workflowConfig.getStatus(), WorkflowTaskEnum.WorkflowStatus.COMPLETED.toString());
		Assert.assertTrue(workflowConfig.getNextRun() > nextRunDaily);

	}
	
	
	//Kpi execution exception case when kpi queries are not appropriate
	@Test(priority = 10)
	public void testWithWrongKpiQueries() throws InterruptedException, InsightsCustomException {
		saveAssessmentReport(workflowIdWrongkpi, assessmentReportWrongkpi, 2);
		saveAssessmentReport(workflowIdWrongkpis, assessmentReportWrongkpis, 2);
		WorkflowExecutor executor = new WorkflowExecutor();
		executor.executeWorkflow();
		Thread.sleep(8000);
		InsightsWorkflowConfiguration workflowConfigWrongkpi = workflowDAL.getWorkflowConfigByWorkflowId(workflowIdWrongkpi);		
		InsightsWorkflowConfiguration workflowConfigWrongkpis = workflowDAL.getWorkflowConfigByWorkflowId(workflowIdWrongkpi);		
		Assert.assertEquals(workflowConfigWrongkpi.getStatus(), WorkflowTaskEnum.WorkflowStatus.ERROR.toString());
		Assert.assertEquals(workflowConfigWrongkpis.getStatus(), WorkflowTaskEnum.WorkflowStatus.ERROR.toString());

	}
	
	//correct kpi queries updation
	@Test(priority = 11)
	public void testRetryWrongKpiQueries() throws InterruptedException {
		updateCorrectKpiQuery(100127, queryJenkins);
		updateCorrectKpiQuery(100161, queryJira);
		updateCorrectKpiQuery(100153, queryJiraAvg);
		WorkflowRetryExecutor executorRetry = new WorkflowRetryExecutor();
		executorRetry.retryWorkflowWithFailedTask();
		Thread.sleep(50000);
	}
	
	//PDF execution exception case when visualizationConfigs or charts details are not appropriate
	@Test(priority = 12)
	public void testWrongKpiQueriesStatus() throws InterruptedException, InsightsCustomException {
		
		InsightsWorkflowConfiguration workflowConfigWrongkpi = workflowDAL.getWorkflowConfigByWorkflowId(workflowIdWrongkpi);		
		InsightsWorkflowConfiguration workflowConfigWrongkpis = workflowDAL.getWorkflowConfigByWorkflowId(workflowIdWrongkpis);		
		Assert.assertEquals(workflowConfigWrongkpis.getStatus(), WorkflowTaskEnum.WorkflowStatus.ERROR.toString());
		Assert.assertEquals(workflowConfigWrongkpi.getStatus(), WorkflowTaskEnum.WorkflowStatus.ERROR.toString());

	}
	
	//delete dummy data
		@AfterTest
		public void cleanUp() throws InsightsCustomException {
			
			//cleaning Postgres
			delete(workflowIdProd);
			delete(workflowIdFail);
			delete(workflowIdWrongkpi);
			delete(workflowIdWrongkpis);
			
			//delete workflow Task
			for(int element: taskidList) {
				workflowDAL.deleteTask(element);
			}
			
			//delete report template
			for(int element: reportIdList) {
				reportConfigDAL.deleteReportTemplatebyReportID(element);
			}
			reportConfigDAL.deleteReportTemplatebyReportID(reportTemplateJson.get("reportId").getAsInt());
			reportConfigDAL.deleteReportTemplatebyReportID(reportTemplateKpisJson.get("reportId").getAsInt());

			
			//delete content Config
			for(int element: contentIdList) {
				reportConfigDAL.deleteContentbyContentID(element);
			}
			
			//delete kpi definition
			for(int element: kpiIdList) {
				reportConfigDAL.deleteKPIbyKpiID(element);
			}
		
		}
}
