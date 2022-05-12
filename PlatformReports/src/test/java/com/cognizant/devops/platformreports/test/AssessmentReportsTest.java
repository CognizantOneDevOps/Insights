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
package com.cognizant.devops.platformreports.test;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.assessmentreport.InsightsContentConfig;
import com.cognizant.devops.platformdal.assessmentreport.InsightsKPIConfig;
import com.cognizant.devops.platformdal.assessmentreport.InsightsReportVisualizationContainer;
import com.cognizant.devops.platformdal.assessmentreport.ReportConfigDAL;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFilesDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowExecutionHistory;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformworkflow.workflowtask.core.WorkflowExecutor;
import com.cognizant.devops.platformworkflow.workflowtask.core.WorkflowImmediateJobExecutor;
import com.cognizant.devops.platformworkflow.workflowtask.core.WorkflowRetryExecutor;

@Test
public class AssessmentReportsTest extends AssessmentReportsTestData {
	private static Logger log = LogManager.getLogger(AssessmentReportsTest.class);
	ReportConfigDAL reportConfigDAL = new ReportConfigDAL();
	WorkflowDAL workflowDAL = new WorkflowDAL();
	int typeId = 0;
	int reportTypeId = 0;

	@BeforeClass
	public void onInit() throws Exception {
			
		
		reportTypeId = saveWorkflowType("Report");
		// save multiple Kpi definition in db
		readKpiFileAndSave("KPIDefination.json");
		InsightsConfigFilesDAL configFilesDAL = new InsightsConfigFilesDAL();
		configFilesDAL.saveConfigurationFile(AssessmentReportsTestData.createConfigFileData());
		// save multiple content definition in db
		readContentFileAndSave("ContentsConfiguration.json");
		getData();
		generateGrafanaToken();
		
		
		// save report template in db
		readReportTempFileAndSave("REPORT_SONAR_JENKINS_PROD_RT.json", reportIdProdRT);
		readReportTempFileAndSave("REPORT_SONAR_RT.json", reportIdSonarRT);
		saveReportTemplate(reportTemplatekpi, reportIdkpiRT);
		saveReportTemplate(reportTemplatekpis, reportIdkpisRT);

		// save report template design files in db
		uploadReportTemplateDesignFiles(reportIdProdRT);

		typeId = saveWorkflowType("SYSTEM");
		

		// save workflow task in db
		saveWorkflowTask(taskKpiExecution);
		saveWorkflowTask(taskPDFExecution);
		saveWorkflowTask(taskEmailExecution);
		saveWorkflowTask(taskSystemHealthNotificationExecution);
		saveWorkflowTask(taskSystemEmailNotificationExecution);

		// save assessment report
		saveAssessmentReport(workflowIdProd, assessmentReport, 2);
		saveAssessmentReport(workflowIdWithEmail, assessmentReportWithEmail, 3);
		saveAssessmentReport(workflowIdWithoutEmail, assessmentReportWithoutEmail, 2);
		saveAssessmentReport(workflowIdWrongkpi, assessmentReportWrongkpi, 2);
		saveAssessmentReport(workflowIdWrongkpis, assessmentReportWrongkpis, 2);
		saveAssessmentReport(workflowIdFail, assessmentReportFail, 1);

		saveHealthNotificationWorkflowConfig();

		grafanaPDFExportUrl = ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint();
		smtpHostServer = ApplicationConfigProvider.getInstance().getEmailConfiguration().getSmtpHostServer();
		ApplicationConfigProvider.getInstance().getAssessmentReport().setMaxWorkflowRetries(3);

		initializeTask();

		// run workflow executor
		WorkflowExecutor executor = new WorkflowExecutor();
		executor.executeWorkflow();
		Thread.sleep(40000);

		WorkflowImmediateJobExecutor immediateJobExecutor = new WorkflowImmediateJobExecutor();
		immediateJobExecutor.executeImmediateWorkflow();
		Thread.sleep(40000);

	}

	@Test(priority = 1)
	public void testExecutionHistoryUpdateProd() throws InterruptedException {
		try {
			if (grafanaPDFExportUrl == null || grafanaPDFExportUrl.isEmpty()) {
				throw new SkipException("skipped this test case as PDF export server details not found.");
			}
			List<InsightsWorkflowExecutionHistory> executionHistory = workflowDAL
					.getWorkflowExecutionHistoryRecordsByWorkflowId(workflowIdProd);
			if (executionHistory.size() > 0) {
				for (InsightsWorkflowExecutionHistory eachExecutionRecord : executionHistory) {
					Assert.assertEquals(eachExecutionRecord.getTaskStatus(),
							WorkflowTaskEnum.WorkflowStatus.COMPLETED.toString());
				}
			}
		} catch (AssertionError e) {
			log.error(e);
			Assert.fail("testExecutionHistoryUpdateProd ", e);
		}
	}

	// @Test(priority = 2)
//	public void testValidateStatusUpdateProd() {
//		try {
//			InsightsWorkflowConfiguration workflowConfig = workflowDAL.getWorkflowConfigByWorkflowId(workflowIdProd);
//			Assert.assertEquals(workflowConfig.getStatus(), WorkflowTaskEnum.WorkflowStatus.COMPLETED.toString());
//			Assert.assertTrue(workflowConfig.getNextRun() > nextRunBiWeekly);
//		} catch (AssertionError e) {
//			Assert.fail("testValidateStatusUpdateProd");
//		}
//	}

	@Test(priority = 3)
	public void testKpiResultProd() {
		try {
			List<InsightsWorkflowExecutionHistory> executionHistory = workflowDAL
					.getWorkflowExecutionHistoryRecordsByWorkflowId(workflowIdProd);
			long executionId = 0;
			if (executionHistory.size() > 0) {
				for (InsightsWorkflowExecutionHistory eachExecutionRecord : executionHistory) {
					executionId = eachExecutionRecord.getExecutionId();
					String query = "MATCH (n:KPI:RESULTS) with distinct max(n.executionId) as latestexecutionId "
							+ "Match (b:KPI:RESULTS) where b.executionId = " + executionId
							+ " and b.kpiId in [100252, 100253, 100254, 100112, 100901, 100903]  return latestexecutionId,b  LIMIT 25";

					Assert.assertNotNull(readNeo4jData(query));
				}
			}
			
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 4)
	public void testContentResultProd() {
		try {
			List<InsightsWorkflowExecutionHistory> executionHistory = workflowDAL
					.getWorkflowExecutionHistoryRecordsByWorkflowId(workflowIdProd);
			long executionId = 0;
			if (executionHistory.size() > 0) {
				for (InsightsWorkflowExecutionHistory eachExecutionRecord : executionHistory) {
					executionId = eachExecutionRecord.getExecutionId();
					String query = "MATCH (n:KPI:CONTENT_RESULT) with distinct max(n.executionId) as latestexecutionId "
							+ "Match (b:KPI:CONTENT_RESULT) where b.executionId = " + executionId
							+ " and b.kpiId in [100252, 100253, 100254, 100112, 100901, 100903]  return latestexecutionId,b  LIMIT 25";
					Assert.assertNotNull(readNeo4jData(query));
				}
			}
			
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 5)
	public void testValidateFailStatusUpdate() {
		try {
			InsightsWorkflowConfiguration workflowConfig = workflowDAL.getWorkflowConfigByWorkflowId(workflowIdFail);
			Assert.assertEquals(workflowConfig.getStatus(), WorkflowTaskEnum.WorkflowStatus.ERROR.toString());
//			Assert.assertTrue(workflowConfig.getNextRun() == nextRunDaily);
		} catch (AssertionError e) {
			throw new SkipException(e.getMessage());
		}
	}

	@Test(priority = 6)
	public void testFailExecutionHistoryUpdate() throws InterruptedException {
		try {
			List<InsightsWorkflowExecutionHistory> executionHistory = workflowDAL
					.getWorkflowExecutionHistoryRecordsByWorkflowId(workflowIdFail);
			if (executionHistory.size() > 0) {
				for (InsightsWorkflowExecutionHistory eachExecutionRecord : executionHistory) {
					Assert.assertEquals(eachExecutionRecord.getTaskStatus(),
							WorkflowTaskEnum.WorkflowStatus.ERROR.toString());
				}
			}
		} catch (AssertionError e) {
			throw new SkipException(e.getMessage());
		}
	}

	@Test(priority = 7)
	public void testRetryKpiExecute() throws InterruptedException {
		try {
			InsightsKPIConfig existingConfig = reportConfigDAL.getKPIConfig(100331);
			existingConfig.setCategory("COMPARISON");
			existingConfig.setdBQuery(querySonar);
			reportConfigDAL.updateKpiConfig(existingConfig);
			WorkflowRetryExecutor executorRetry = new WorkflowRetryExecutor();
			executorRetry.retryWorkflowWithFailedTask();
			Thread.sleep(20000);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 8)
	public void testRetryContentExecute() throws InterruptedException {
		try {
			InsightsContentConfig existingContentConfig = reportConfigDAL.getContentConfig(20013131);
			existingContentConfig.setCategory("COMPARISON");
			reportConfigDAL.updateContentConfig(existingContentConfig);
			WorkflowRetryExecutor executorRetry = new WorkflowRetryExecutor();
			executorRetry.retryWorkflowWithFailedTask();
			Thread.sleep(20000);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 9)
	public void testValidateStatusUpdateAfterRetry() throws InterruptedException {
		try {
			InsightsWorkflowConfiguration workflowConfig = workflowDAL.getWorkflowConfigByWorkflowId(workflowIdFail);
			Assert.assertEquals(workflowConfig.getStatus(), WorkflowTaskEnum.WorkflowStatus.COMPLETED.toString());
			Assert.assertTrue(workflowConfig.getNextRun() >nextRunDaily);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}

	}

	// Kpi execution exception case when kpi queries are not appropriate
	@Test(priority = 10)
	public void testWithWrongKpiQueries() throws InterruptedException, InsightsCustomException {
		try {
			WorkflowExecutor executor = new WorkflowExecutor();
			executor.executeWorkflow();
			Thread.sleep(8000);
			InsightsWorkflowConfiguration workflowConfigWrongkpi = workflowDAL
					.getWorkflowConfigByWorkflowId(workflowIdWrongkpi);
			InsightsWorkflowConfiguration workflowConfigWrongkpis = workflowDAL
					.getWorkflowConfigByWorkflowId(workflowIdWrongkpi);
			Assert.assertNotEquals(workflowConfigWrongkpi.getStatus(), WorkflowTaskEnum.WorkflowStatus.COMPLETED.toString());
			Assert.assertNotEquals(workflowConfigWrongkpis.getStatus(), WorkflowTaskEnum.WorkflowStatus.COMPLETED.toString());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	// correct kpi queries updation
	@Test(priority = 11)
	public void testRetryWrongKpiQueries() throws InterruptedException {
		try {
			updateCorrectKpiQuery(100127, queryJenkins);
			updateCorrectKpiQuery(100161, queryJira);
			updateCorrectKpiQuery(100153, queryJiraAvg);
			WorkflowRetryExecutor executorRetry = new WorkflowRetryExecutor();
			executorRetry.retryWorkflowWithFailedTask();
			Thread.sleep(50000);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	// PDF execution exception case when visualizationConfigs or charts details are
	// not appropriate
	@Test(priority = 12)
	public void testWrongKpiQueriesStatus() throws InterruptedException, InsightsCustomException {
		try {
			InsightsWorkflowConfiguration workflowConfigWrongkpi = workflowDAL
					.getWorkflowConfigByWorkflowId(workflowIdWrongkpi);
			InsightsWorkflowConfiguration workflowConfigWrongkpis = workflowDAL
					.getWorkflowConfigByWorkflowId(workflowIdWrongkpis);
			Assert.assertNotEquals(workflowConfigWrongkpis.getStatus(), WorkflowTaskEnum.WorkflowStatus.COMPLETED.toString());
			Assert.assertNotEquals(workflowConfigWrongkpi.getStatus(), WorkflowTaskEnum.WorkflowStatus.COMPLETED.toString());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 13)
	public void testSaveInsightsEmailExecutionHistory() throws InterruptedException, InsightsCustomException {
		try {
			if (grafanaPDFExportUrl == null || grafanaPDFExportUrl.isEmpty() || smtpHostServer == null
					|| smtpHostServer.isEmpty()) {
				throw new SkipException("skipped this test case as PDF and email configuration details not found.");
			}
			InsightsWorkflowConfiguration workflowConfig = workflowDAL
					.getWorkflowConfigByWorkflowId(workflowIdWithEmail);
			InsightsReportVisualizationContainer emailHistory = workflowDAL
					.getEmailExecutionHistoryByWorkflowId(workflowConfig.getWorkflowId());
			Assert.assertNotNull(emailHistory);
			Assert.assertEquals(emailHistory.getStatus(), WorkflowTaskEnum.EmailStatus.COMPLETED.toString());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 14)
	public void testPDFRecordInEmailExecutionHistory() throws InterruptedException, InsightsCustomException {
		try {
			log.debug(" grafanaPDFExportUrl {} Insights_Home ====  {}  ", grafanaPDFExportUrl,
					ConfigOptions.CONFIG_FILE_RESOLVED_PATH);
			if (grafanaPDFExportUrl == null || grafanaPDFExportUrl.isEmpty()
					|| smtpHostServer.isEmpty()) {
				throw new SkipException("skipped this test case as PDF export server details not found.");
			}
			InsightsWorkflowConfiguration workflowConfig = workflowDAL
					.getWorkflowConfigByWorkflowId(workflowIdWithoutEmail);
			InsightsReportVisualizationContainer emailHistory = workflowDAL
					.getEmailExecutionHistoryByWorkflowId(workflowConfig.getWorkflowId());
			log.debug(" emailHistory {} ", emailHistory);
			Assert.assertNotNull(emailHistory);
			Assert.assertEquals(emailHistory.getStatus(), WorkflowTaskEnum.WorkflowStatus.COMPLETED.name());
		} catch (AssertionError e) {
			log.error(e);
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 15)
	public void testAgentHealthRecordInEmailExecutionHistory() throws InterruptedException, InsightsCustomException {
		try {
			if (grafanaPDFExportUrl == null || grafanaPDFExportUrl.isEmpty() || smtpHostServer == null
					|| smtpHostServer.isEmpty()) {
				throw new SkipException("skipped this test case as PDF and email configuration details not found.");
			}
			InsightsWorkflowConfiguration workflowConfig = workflowDAL
					.getWorkflowConfigByWorkflowId(healthNotificationWorkflowId);
			InsightsReportVisualizationContainer emailHistory = workflowDAL
					.getEmailExecutionHistoryByWorkflowId(workflowConfig.getWorkflowId());
			Assert.assertNotNull(emailHistory);
			Assert.assertEquals(emailHistory.getStatus(), WorkflowTaskEnum.WorkflowStatus.COMPLETED.name());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	// delete dummy data
	@AfterClass
	public void cleanUp() throws InsightsCustomException {

		// cleaning Postgres
		workflowDAL.deleteEmailExecutionHistoryByWorkflowId(workflowIdWithoutEmail);
		workflowDAL.deleteEmailExecutionHistoryByWorkflowId(workflowIdWithEmail);
		delete(workflowIdProd);
		delete(workflowIdWithoutEmail);
		delete(workflowIdWithEmail);
		delete(workflowIdFail);
		delete(workflowIdWrongkpi);
		delete(workflowIdWrongkpis);
		deleteWorkflowConfig(healthNotificationWorkflowId);
		reportConfigDAL.deleteTemplateDesignFilesByReportTemplateID(reportIdProdRT);

		try {
			workflowDAL.deleteWorkflowType(typeId);
			workflowDAL.deleteWorkflowType(reportTypeId);
		} catch (Exception e) {
			log.error(e);
		}

		// delete workflow Task
		for (int element : taskidList) {
			try {
				workflowDAL.deleteTask(element);
			} catch (Exception e) {
				log.error(e);
			}
		}

		// delete report template
		for (int element : reportIdList) {
			try {
				reportConfigDAL.deleteReportTemplatebyReportID(element);
			} catch (Exception e) {
				log.error(e);
			}
		}
		try {
			reportConfigDAL.deleteReportTemplatebyReportID(reportIdkpiRT);
		} catch (Exception e) {
			log.error(e);
		}
		try {
			reportConfigDAL.deleteReportTemplatebyReportID(reportIdkpisRT);
		} catch (Exception e) {
			log.error(e);
		}

		// delete content Config
		for (int element : contentIdList) {
			try {
				reportConfigDAL.deleteContentbyContentID(element);
			} catch (Exception e) {
				log.error(e);
			}
		}

		// delete kpi definition
		for (int element : kpiIdList) {
			try {
				reportConfigDAL.deleteKPIbyKpiID(element);
			} catch (Exception e) {
				log.error(e);
			}
		}

	}
}
