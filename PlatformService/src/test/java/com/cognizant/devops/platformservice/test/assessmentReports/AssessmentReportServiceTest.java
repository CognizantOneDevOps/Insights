/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http:www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformservice.test.assessmentReports;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.multipart.MultipartFile;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.assessmentreport.InsightsAssessmentConfiguration;
import com.cognizant.devops.platformdal.assessmentreport.InsightsAssessmentReportTemplate;
import com.cognizant.devops.platformdal.assessmentreport.InsightsContentConfig;
import com.cognizant.devops.platformdal.assessmentreport.InsightsKPIConfig;
import com.cognizant.devops.platformdal.assessmentreport.ReportConfigDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTask;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTaskSequence;
import com.cognizant.devops.platformservice.assessmentreport.service.AssesmentReportServiceImpl;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Test
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class AssessmentReportServiceTest extends AssessmentReportServiceData {
	private static final Logger log = LogManager.getLogger(AssessmentReportServiceTest.class);

	@BeforeTest
	public void prepareData() {
		ApplicationConfigCache.loadConfigCache();
		try {
			prepareAssessmentData();
		} catch (Exception e) {
			log.error("message", e);
		}

	}

	public static final AssesmentReportServiceImpl assessmentService = new AssesmentReportServiceImpl();
	ReportConfigDAL reportConfigDAL = new ReportConfigDAL();

	@Test(priority = 1)
	public void testsaveKpiDefinition() throws InsightsCustomException, IOException {
		try {
			int response = assessmentService.saveKpiDefinition(registerkpiJson);
			InsightsKPIConfig kpi = reportConfigDAL.getKPIConfig(registerkpiJson.get("kpiId").getAsInt());
			Assert.assertNotNull(kpi);
			Assert.assertNotNull(kpi.getKpiId());
			Assert.assertEquals(kpi.getKpiId().intValue(), registerkpiJson.get("kpiId").getAsInt());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 2, expectedExceptions = InsightsCustomException.class)
	public void testsaveKpiDefinitionDuplicateKpiId() throws InsightsCustomException, IOException {
		int response = assessmentService.saveKpiDefinition(registerkpiJson);
	}

	@Test(priority = 3, expectedExceptions = InsightsCustomException.class)
	public void testsaveKpiDefinitionIncompleteData() throws InsightsCustomException, IOException {
		int response = assessmentService.saveKpiDefinition(incorrectregisterkpiJson);
	}

	@Test(priority = 4)
	public void testsaveContentDefinition() throws InsightsCustomException, IOException {
		try {
			int response = assessmentService.saveContentDefinition(registerContentJson);
			InsightsContentConfig content = reportConfigDAL
					.getContentConfig(registerContentJson.get("contentId").getAsInt());
			Assert.assertNotNull(content);
			Assert.assertNotNull(content.getKpiConfig().getKpiId());
			Assert.assertEquals(content.getContentId().intValue(), registerContentJson.get("contentId").getAsInt());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 5, expectedExceptions = InsightsCustomException.class)
	public void testsaveContentDefinitionDuplicateContentId() throws InsightsCustomException, IOException {
		int response = assessmentService.saveContentDefinition(registerContentJson);
	}

	@Test(priority = 6, expectedExceptions = InsightsCustomException.class)
	public void testsaveContentDefinitionIncompleteData() throws InsightsCustomException, IOException {
		int response = assessmentService.saveContentDefinition(incorrectregisterContentJson);
	}

	@Test(priority = 7, expectedExceptions = InsightsCustomException.class)
	public void testsaveContentDefinitionWithoutKpi() throws InsightsCustomException, IOException {
		int response = assessmentService.saveContentDefinition(contentWithoutKpiJson);
	}

	@Test(priority = 8)
	public void testUploadKPIInDatabase() throws InsightsCustomException, IOException {
		try {
			FileInputStream input = new FileInputStream(kpiFile);
			MultipartFile multipartFile = new MockMultipartFile("file", kpiFile.getName(), "text/plain",
					IOUtils.toByteArray(input));
			String response = assessmentService.uploadKPIInDatabase(multipartFile);
			readFileAndgetKpiIdList(kpiFile.getName());
			Assert.assertNotNull(response);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 9)
	public void testUploadContentInDatabase() throws InsightsCustomException, IOException {
		try {
			FileInputStream input = new FileInputStream(configFile);
			MultipartFile multipartFile = new MockMultipartFile("file", configFile.getName(), "text/plain",
					IOUtils.toByteArray(input));
			String response = assessmentService.uploadContentInDatabase(multipartFile);
			readFileAndgetContentIdList(configFile.getName());
			Assert.assertNotNull(response);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}

	}

	@Test(priority = 10, expectedExceptions = InsightsCustomException.class)
	public void testUploadContentInDatabaseWithWrongFileFormat() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(configFileTxt);
		MultipartFile multipartFile = new MockMultipartFile("file", configFileTxt.getName(), "text/plain",
				IOUtils.toByteArray(input));
		String response = assessmentService.uploadContentInDatabase(multipartFile);
	}

	@Test(priority = 11, expectedExceptions = InsightsCustomException.class)
	public void testUploadKPIInDatabaseWithWrongFileFormat() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(kpiFileTxt);
		MultipartFile multipartFile = new MockMultipartFile("file", kpiFileTxt.getName(), "text/plain",
				IOUtils.toByteArray(input));
		String response = assessmentService.uploadKPIInDatabase(multipartFile);
	}

	@Test(priority = 12, expectedExceptions = InsightsCustomException.class)
	public void testUploadKPIInDatabaseWithEmptyFile() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(emptyFile);
		MultipartFile multipartFile = new MockMultipartFile("file", emptyFile.getName(), "text/plain",
				IOUtils.toByteArray(input));
		String response = assessmentService.uploadKPIInDatabase(multipartFile);
	}

	@Test(priority = 13, expectedExceptions = InsightsCustomException.class)
	public void testUploadContentInDatabaseWithEmptyFile() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(emptyFile);
		MultipartFile multipartFile = new MockMultipartFile("file", emptyFile.getName(), "text/plain",
				IOUtils.toByteArray(input));
		String response = assessmentService.uploadContentInDatabase(multipartFile);
	}

	@Test(priority = 14)
	public void testSaveReportTemplate() throws InsightsCustomException {
		try {
			int reportId = assessmentService.saveTemplateReport(reportTemplateJson);
			InsightsAssessmentReportTemplate report = (InsightsAssessmentReportTemplate) reportConfigDAL
					.getReportTemplateByReportId(reportTemplateJson.get("reportId").getAsInt());
			Assert.assertNotNull(report);
			Assert.assertEquals(reportId, reportTemplateJson.get("reportId").getAsInt());
			Assert.assertTrue(report.getReportsKPIConfig().size() > 0);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 15)
	public void testGetReportTemplate() throws InsightsCustomException {
		try {
			List<InsightsAssessmentReportTemplate> reportTemplates = assessmentService.getReportTemplate();
			Assert.assertNotNull(reportTemplates);
			Assert.assertTrue(reportTemplates.size() > 0);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 16)
	public void testGetKPIlistOfReportTemplate() throws InsightsCustomException {
		try {
			JsonArray kpiList = assessmentService.getKPIlistOfReportTemplate(reportIdForList);
			Assert.assertNotNull(kpiList);
			Assert.assertTrue(kpiList.size() > 0);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 17, expectedExceptions = InsightsCustomException.class)
	public void testSaveReportTemplateWithoutKPIDs() throws InsightsCustomException {
		int reportId = assessmentService.saveTemplateReport(reportTemplateWithoutKPIsJson);
	}

	@Test(priority = 18, expectedExceptions = InsightsCustomException.class)
	public void testSaveReportTemplateWithoutExistingKPIDs() throws InsightsCustomException {
		int reportId = assessmentService.saveTemplateReport(reportTemplateWithoutExistingKPIDsJson);
	}

	@Test(priority = 19, expectedExceptions = InsightsCustomException.class)
	public void testIncorrectReportTemplate() throws InsightsCustomException {
		int reportId = assessmentService.saveTemplateReport(incorrectReportTemplateJson);
	}

	@Test(priority = 20)
	public void testSaveDailyAssessmentReport() throws InsightsCustomException {
		try {
			int assessmentid = assessmentService.saveAssessmentReport(dailyAssessmentReportJson);
			InsightsAssessmentConfiguration assessment = reportConfigDAL.getAssessmentByConfigId(assessmentid);
			Assert.assertNotNull(assessment, "Report not found in db");
			Assert.assertEquals(assessment.getReportTemplateEntity().getReportId(),
					dailyAssessmentReportJson.get("reportTemplate").getAsInt(),
					"Report Template not present in db as expected");
			Assert.assertEquals(assessment.getStartDate(), dailyExpectedAssessmentStartDate, "Start Date not equal");
			Assert.assertEquals(assessment.getEndDate(), dailyExpectedAssessmentEndDate, "End date not equal");
			Assert.assertEquals(assessment.getWorkflowConfig().getNextRun(), dailyExpectedNextRun,
					"Next run not equal");
			Assert.assertTrue(assessment.getWorkflowConfig().getTaskSequenceEntity().size() > 0,
					"Records not present in Workflow Task Sequence table");
			Set<InsightsWorkflowTaskSequence> set = assessment.getWorkflowConfig().getTaskSequenceEntity();
			int noOfTasks = set.size();
			set.forEach(x -> {
				if (x.getSequence() == noOfTasks)
					Assert.assertEquals(x.getNextTask().intValue(), -1, "Last task's next task not assigned -1");
			});
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 21)
	public void testSaveWeeklyAssessmentReport() throws InsightsCustomException, InterruptedException {
		Thread.sleep(1000);
		try {
			int assessmentid = assessmentService.saveAssessmentReport(weeklyAssessmentReportJson);
			InsightsAssessmentConfiguration assessment = reportConfigDAL.getAssessmentByConfigId(assessmentid);
			Assert.assertNotNull(assessment, "Report not found in db");
			Assert.assertEquals(assessment.getReportTemplateEntity().getReportId(),
					dailyAssessmentReportJson.get("reportTemplate").getAsInt(),
					"Report Template not present in db as expected");
			Assert.assertEquals(assessment.getStartDate(), weeklyExpectedAssessmentStartDate, "Start Date not equal");
			Assert.assertEquals(assessment.getEndDate(), weeklyExpectedAssessmentEndDate, "End date not equal");
			Assert.assertEquals(assessment.getWorkflowConfig().getNextRun(), weeklyExpectedNextRun,
					"Next run not equal");
			Assert.assertTrue(assessment.getWorkflowConfig().getTaskSequenceEntity().size() > 0,
					"Records not present in Workflow Task Sequence table");
			Set<InsightsWorkflowTaskSequence> set = assessment.getWorkflowConfig().getTaskSequenceEntity();
			int noOfTasks = set.size();
			set.forEach(x -> {
				if (x.getSequence() == noOfTasks)
					Assert.assertEquals(x.getNextTask().intValue(), -1, "Last task's next task not assigned -1");
			});
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 22)
	public void testSaveMonthlyAssessmentReport() throws InsightsCustomException, InterruptedException {
		Thread.sleep(1000);
		try {
			int assessmentid = assessmentService.saveAssessmentReport(monthlyAssessmentReportJson);
			InsightsAssessmentConfiguration assessment = reportConfigDAL.getAssessmentByConfigId(assessmentid);
			Assert.assertNotNull(assessment, "Report not found in db");
			Assert.assertEquals(assessment.getReportTemplateEntity().getReportId(),
					dailyAssessmentReportJson.get("reportTemplate").getAsInt(),
					"Report Template not present in db as expected");
			Assert.assertEquals(assessment.getStartDate(), monthlyExpectedAssessmentStartDate, "Start Date not equal");
			Assert.assertEquals(assessment.getEndDate(), monthlyExpectedAssessmentEndDate, "End date not equal");
			Assert.assertEquals(assessment.getWorkflowConfig().getNextRun(), monthlyExpectedNextRun,
					"Next run not equal");
			Assert.assertTrue(assessment.getWorkflowConfig().getTaskSequenceEntity().size() > 0,
					"Records not present in Workflow Task Sequence table");
			Set<InsightsWorkflowTaskSequence> set = assessment.getWorkflowConfig().getTaskSequenceEntity();
			int noOfTasks = set.size();
			set.forEach(x -> {
				if (x.getSequence() == noOfTasks)
					Assert.assertEquals(x.getNextTask().intValue(), -1, "Last task's next task not assigned -1");
			});
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 23)
	public void testSaveQuarterlyAssessmentReport() throws InsightsCustomException, InterruptedException {
		Thread.sleep(1000);
		try {
			int assessmentid = assessmentService.saveAssessmentReport(quarterlyAssessmentReportJson);
			InsightsAssessmentConfiguration assessment = reportConfigDAL.getAssessmentByConfigId(assessmentid);
			Assert.assertNotNull(assessment, "Report not found in db");
			Assert.assertEquals(assessment.getReportTemplateEntity().getReportId(),
					dailyAssessmentReportJson.get("reportTemplate").getAsInt(),
					"Report Template not present in db as expected");
			Assert.assertEquals(assessment.getStartDate(), quarterlyExpectedAssessmentStartDate,
					"Start Date not equal");
			Assert.assertEquals(assessment.getEndDate(), quarterlyExpectedAssessmentEndDate, "End date not equal");
			Assert.assertEquals(assessment.getWorkflowConfig().getNextRun(), quarterlyExpectedNextRun,
					"Next run not equal");
			Assert.assertTrue(assessment.getWorkflowConfig().getTaskSequenceEntity().size() > 0,
					"Records not present in Workflow Task Sequence table");
			Set<InsightsWorkflowTaskSequence> set = assessment.getWorkflowConfig().getTaskSequenceEntity();
			int noOfTasks = set.size();
			set.forEach(x -> {
				if (x.getSequence() == noOfTasks)
					Assert.assertEquals(x.getNextTask().intValue(), -1, "Last task's next task not assigned -1");
			});
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 24)
	public void testYearlyQuarterlyAssessmentReport() throws InsightsCustomException, InterruptedException {
		Thread.sleep(1000);
		try {
			int assessmentid = assessmentService.saveAssessmentReport(yearlyAssessmentReportJson);
			InsightsAssessmentConfiguration assessment = reportConfigDAL.getAssessmentByConfigId(assessmentid);
			Assert.assertNotNull(assessment, "Report not found in db");
			Assert.assertEquals(assessment.getReportTemplateEntity().getReportId(),
					dailyAssessmentReportJson.get("reportTemplate").getAsInt(),
					"Report Template not present in db as expected");
			Assert.assertEquals(assessment.getStartDate(), yearlyExpectedAssessmentStartDate, "Start Date not equal");
			Assert.assertEquals(assessment.getEndDate(), yearlyExpectedAssessmentEndDate, "End date not equal");
			Assert.assertEquals(assessment.getWorkflowConfig().getNextRun(), yearlyExpectedNextRun,
					"Next run not equal");
			Assert.assertTrue(assessment.getWorkflowConfig().getTaskSequenceEntity().size() > 0,
					"Records not present in Workflow Task Sequence table");
			Set<InsightsWorkflowTaskSequence> set = assessment.getWorkflowConfig().getTaskSequenceEntity();
			int noOfTasks = set.size();
			set.forEach(x -> {
				if (x.getSequence() == noOfTasks)
					Assert.assertEquals(x.getNextTask().intValue(), -1, "Last task's next task not assigned -1");
			});
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 25)
	public void testSaveOneTimeAssessmentReport() throws InsightsCustomException, InterruptedException {
		Thread.sleep(1000);
		try {
			int assessmentid = assessmentService.saveAssessmentReport(oneTimeAssessmentReportJson);
			InsightsAssessmentConfiguration assessment = reportConfigDAL.getAssessmentByConfigId(assessmentid);
			Assert.assertNotNull(assessment, "Report not found in db");
			Assert.assertEquals(assessment.getReportTemplateEntity().getReportId(),
					oneTimeAssessmentReportJson.get("reportTemplate").getAsInt(),
					"Report Template not present in db as expected");
			Assert.assertEquals(assessment.getStartDate(), oneTimeExpectedAssessmentStartDate, "Start Date not equal");
			Assert.assertEquals(assessment.getEndDate(), oneTimeExpectedAssessmentEndDate, "End date not equal");
			Assert.assertEquals(assessment.getWorkflowConfig().getNextRun(), oneTimeExpectedNextRun,
					"Next run not equal");
			Assert.assertTrue(assessment.getWorkflowConfig().getTaskSequenceEntity().size() > 0,
					"Records not present in Workflow Task Sequence table");
			Set<InsightsWorkflowTaskSequence> set = assessment.getWorkflowConfig().getTaskSequenceEntity();
			int noOfTasks = set.size();
			set.forEach(x -> {
				if (x.getSequence() == noOfTasks)
					Assert.assertEquals(x.getNextTask().intValue(), -1, "Last task's next task not assigned -1");
			});
			Assert.assertFalse(assessment.getWorkflowConfig().isReoccurence());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 26)
	public void testSaveBiWeeklyAssessmentReport() throws InsightsCustomException, InterruptedException {
		Thread.sleep(1000);
		try {
			int assessmentid = assessmentService.saveAssessmentReport(biWeeklyAssessmentReportJson);
			InsightsAssessmentConfiguration assessment = reportConfigDAL.getAssessmentByConfigId(assessmentid);
			Assert.assertNotNull(assessment, "Report not found in db");
			Assert.assertEquals(assessment.getReportTemplateEntity().getReportId(),
					biWeeklyAssessmentReportJson.get("reportTemplate").getAsInt(),
					"Report Template not present in db as expected");
			Assert.assertEquals(assessment.getStartDate(), biWeeklyExpectedAssessmentStartDate, "Start Date not equal");
			Assert.assertEquals(assessment.getEndDate(), biWeeklyExpectedAssessmentEndDate, "End date not equal");
			Assert.assertEquals(assessment.getWorkflowConfig().getNextRun(), biWeeklyExpectedNextRun,
					"Next run not equal");
			Assert.assertTrue(assessment.getWorkflowConfig().getTaskSequenceEntity().size() > 0,
					"Records not present in Workflow Task Sequence table");
			Set<InsightsWorkflowTaskSequence> set = assessment.getWorkflowConfig().getTaskSequenceEntity();
			int noOfTasks = set.size();
			set.forEach(x -> {
				if (x.getSequence() == noOfTasks)
					Assert.assertEquals(x.getNextTask().intValue(), -1, "Last task's nexttask field not assigned -1");
			});
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 27)
	public void testSaveTriWeeklyAssessmentReportWithoutReoccurence()
			throws InsightsCustomException, InterruptedException {
		Thread.sleep(1000);
		try {
			int assessmentid = assessmentService.saveAssessmentReport(triWeeklyAssessmentReportJson);
			InsightsAssessmentConfiguration assessment = reportConfigDAL.getAssessmentByConfigId(assessmentid);
			Assert.assertNotNull(assessment, "Report not found in db");
			Assert.assertEquals(assessment.getReportTemplateEntity().getReportId(),
					triWeeklyAssessmentReportJson.get("reportTemplate").getAsInt(),
					"Report Template not present in db as expected");
			Assert.assertEquals(assessment.getStartDate(), triWeeklyExpectedAssessmentStartDate,
					"Start Date not equal");
			Assert.assertEquals(assessment.getEndDate(), triWeeklyExpectedAssessmentEndDate, "End date not equal");
			Assert.assertEquals(assessment.getWorkflowConfig().getNextRun(), triWeeklyExpectedNextRun,
					"Next run not equal");
			Assert.assertTrue(assessment.getWorkflowConfig().getTaskSequenceEntity().size() > 0,
					"Records not present in Workflow Task Sequence table");
			Set<InsightsWorkflowTaskSequence> set = assessment.getWorkflowConfig().getTaskSequenceEntity();
			int noOfTasks = set.size();
			set.forEach(x -> {
				if (x.getSequence() == noOfTasks)
					Assert.assertEquals(x.getNextTask().intValue(), -1, "Last task's nexttask field not assigned -1");
			});
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 28)
	public void testSaveTriWeeklyAssessmentReportWithDataSource() throws InsightsCustomException, InterruptedException {
		Thread.sleep(1000);
		try {
			int assessmentid = assessmentService.saveAssessmentReport(triWeeklyAssessmentWithDataSourceReportJson);
			InsightsAssessmentConfiguration assessment = reportConfigDAL.getAssessmentByConfigId(assessmentid);
			Assert.assertNotNull(assessment, "Report not found in db");
			Assert.assertEquals(assessment.getReportTemplateEntity().getReportId(),
					triWeeklyAssessmentReportJson.get("reportTemplate").getAsInt(),
					"Report Template not present in db as expected");
			Assert.assertEquals(assessment.getStartDate(), triWeeklyExpectedAssessmentStartDate,
					"Start Date not equal");
			Assert.assertEquals(assessment.getEndDate(), triWeeklyExpectedAssessmentEndDate, "End date not equal");
			Assert.assertEquals(assessment.getWorkflowConfig().getNextRun(), triWeeklyExpectedNextRun,
					"Next run not equal");
			Assert.assertTrue(assessment.getWorkflowConfig().getTaskSequenceEntity().size() > 0,
					"Records not present in Workflow Task Sequence table");
			Set<InsightsWorkflowTaskSequence> set = assessment.getWorkflowConfig().getTaskSequenceEntity();
			int noOfTasks = set.size();
			set.forEach(x -> {
				if (x.getSequence() == noOfTasks)
					Assert.assertEquals(x.getNextTask().intValue(), -1, "Last task's nexttask field not assigned -1");
			});
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 29, expectedExceptions = InsightsCustomException.class)
	public void testSaveAssessmentIncompleteReport() throws InsightsCustomException {
		int assessmentid = assessmentService.saveAssessmentReport(incorrectAssessmentReportJson);
	}

	@Test(priority = 30)
	public void testLoadAssessmentReportList() throws InsightsCustomException {
		try {
			JsonArray reportList = assessmentService.getAssessmentReportList();
			Assert.assertNotNull(reportList);
			Assert.assertTrue(reportList.size() > 0);
		} catch (AssertionError e) {
			log.error(e);
		}
	}

	@Test(priority = 31)
	public void testDeleteAssessmentReport() throws InsightsCustomException {
		try {
			String deleteAssessmentReportConfigId = String.valueOf(reportConfigDAL
					.getAssessmentByAssessmentName(dailyAssessmentReportJson.get("reportName").getAsString()).getId());
			String status = assessmentService.deleteAssessmentReport(deleteAssessmentReportConfigId);
			Assert.assertEquals(status, PlatformServiceConstants.SUCCESS);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 32, expectedExceptions = InsightsCustomException.class)
	public void testDeleteAssessmentReportWithWrongConfigID() throws InsightsCustomException {
		String status = assessmentService.deleteAssessmentReport(deleteAssessmentReportWrongConfigId);
	}

	@Test(priority = 34)
	public void testGetSchedule() throws InsightsCustomException {
		try {
			List<String> scheduleRecords = assessmentService.getSchedule();
			Assert.assertNotNull(scheduleRecords);
			Assert.assertTrue(scheduleRecords.size() > 0);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 35)
	public void testUpdateAssessmentReportState() throws InsightsCustomException {
		try {
			String updateAssessmentReportState = "{\"id\":" + reportConfigDAL
					.getAssessmentByAssessmentName(weeklyAssessmentReportJson.get("reportName").getAsString()).getId()
					+ ",\"isActive\":true}";
			final JsonObject updateAssessmentReportStateJson = convertStringIntoJson(updateAssessmentReportState);
			String status = assessmentService.updateAssessmentReportState(updateAssessmentReportStateJson);
			InsightsAssessmentConfiguration assessment = reportConfigDAL
					.getAssessmentByConfigId(updateAssessmentReportStateJson.get("id").getAsInt());
			Assert.assertEquals(status, PlatformServiceConstants.SUCCESS);
			Assert.assertEquals(assessment.getWorkflowConfig().isActive().booleanValue(),
					updateAssessmentReportStateJson.get("isActive").getAsBoolean());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 36, expectedExceptions = InsightsCustomException.class)
	public void testUpdateAssessmentReportIncorrectData() throws InsightsCustomException {
		int assessmentId = assessmentService.updateAssessmentReport(updateAssessmentReportIncorrectJson);
	}

	@Test(priority = 37, expectedExceptions = InsightsCustomException.class)
	public void testSaveOneTimeAssessmentReportWithStartDateGreaterThanEndDate()
			throws InsightsCustomException, InterruptedException {
		Thread.sleep(1000);
		int assessmentid = assessmentService
				.saveAssessmentReport(oneTimeAssessmentReportWithStartDateGreaterThanEndDateJson);
	}

	@Test(priority = 38)
	public void testSaveDailyEmailAssessmentReport() throws InsightsCustomException {
		try {
			int assessmentid = assessmentService.saveAssessmentReport(dailyEmailAssessmentReportJson);
			InsightsAssessmentConfiguration assessment = reportConfigDAL.getAssessmentByConfigId(assessmentid);
			Assert.assertNotNull(assessment, "Report not found in db");
			Assert.assertEquals(assessment.getReportTemplateEntity().getReportId(),
					dailyEmailAssessmentReportJson.get("reportTemplate").getAsInt(),
					"Report Template not present in db as expected");
			Assert.assertEquals(assessment.getStartDate(), dailyEmailExpectedAssessmentStartDate,
					"Start Date not equal");
			Assert.assertEquals(assessment.getEndDate(), dailyEmailExpectedAssessmentEndDate, "End date not equal");
			Assert.assertEquals(assessment.getWorkflowConfig().getNextRun(), dailyEmailExpectedNextRun,
					"Next run not equal");
			Assert.assertTrue(assessment.getWorkflowConfig().getTaskSequenceEntity().size() > 0,
					"Records not present in Workflow Task Sequence table");
			Set<InsightsWorkflowTaskSequence> set = assessment.getWorkflowConfig().getTaskSequenceEntity();
			int noOfTasks = set.size();
			set.forEach(x -> {
				if (x.getSequence() == noOfTasks)
					Assert.assertEquals(x.getNextTask().intValue(), -1, "Last task's next task not assigned -1");
			});
			Assert.assertNotNull(assessment.getWorkflowConfig().getEmailConfig(), "Email details not found in db");
			Assert.assertEquals(
					assessment.getWorkflowConfig().getEmailConfig().getSubject(), dailyEmailAssessmentReportJson
							.get("emailDetails").getAsJsonObject().get("mailSubject").getAsString(),
					"Email details not found in db");
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 39)
	public void testSaveDailyWithoutEmailAssessmentReport() throws InsightsCustomException, InterruptedException {
		Thread.sleep(1000);
		try {
			int assessmentid = assessmentService.saveAssessmentReport(dailywithoutEmailAssessmentReportJson);
			InsightsAssessmentConfiguration assessment = reportConfigDAL.getAssessmentByConfigId(assessmentid);
			Assert.assertNotNull(assessment, "Report not found in db");
			Assert.assertEquals(assessment.getReportTemplateEntity().getReportId(),
					dailywithoutEmailAssessmentReportJson.get("reportTemplate").getAsInt(),
					"Report Template not present in db as expected");
			Assert.assertEquals(assessment.getStartDate(), dailywithoutEmailExpectedAssessmentStartDate,
					"Start Date not equal");
			Assert.assertEquals(assessment.getEndDate(), dailywithoutEmailExpectedAssessmentEndDate,
					"End date not equal");
			Assert.assertEquals(assessment.getWorkflowConfig().getNextRun(), dailywithoutEmailExpectedNextRun,
					"Next run not equal");
			Assert.assertTrue(assessment.getWorkflowConfig().getTaskSequenceEntity().size() > 0,
					"Records not present in Workflow Task Sequence table");
			Set<InsightsWorkflowTaskSequence> set = assessment.getWorkflowConfig().getTaskSequenceEntity();
			int noOfTasks = set.size();
			set.forEach(x -> {
				if (x.getSequence() == noOfTasks)
					Assert.assertEquals(x.getNextTask().intValue(), -1, "Last task's next task not assigned -1");
			});
			Assert.assertNull(assessment.getWorkflowConfig().getEmailConfig(), "Email details present in db");
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 40)
	public void testSaveEmailCCAssessmentReport() throws InsightsCustomException, InterruptedException {
		Thread.sleep(1000);
		try {
			int assessmentid = assessmentService.saveAssessmentReport(dailyEmailCCAssessmentReportJson);
			InsightsAssessmentConfiguration assessment = reportConfigDAL.getAssessmentByConfigId(assessmentid);
			Assert.assertNotNull(assessment, "Report not found in db");
			Assert.assertEquals(assessment.getReportTemplateEntity().getReportId(),
					dailyEmailAssessmentReportJson.get("reportTemplate").getAsInt(),
					"Report Template not present in db as expected");
			Assert.assertEquals(assessment.getStartDate(), dailyEmailCCExpectedAssessmentStartDate,
					"Start Date not equal");
			Assert.assertEquals(assessment.getEndDate(), dailyEmailCCExpectedAssessmentEndDate, "End date not equal");
			Assert.assertEquals(assessment.getWorkflowConfig().getNextRun(), dailyEmailCCExpectedNextRun,
					"Next run not equal");
			Assert.assertTrue(assessment.getWorkflowConfig().getTaskSequenceEntity().size() > 0,
					"Records not present in Workflow Task Sequence table");
			Set<InsightsWorkflowTaskSequence> set = assessment.getWorkflowConfig().getTaskSequenceEntity();
			int noOfTasks = set.size();
			set.forEach(x -> {
				if (x.getSequence() == noOfTasks)
					Assert.assertEquals(x.getNextTask().intValue(), -1, "Last task's next task not assigned -1");
			});
			Assert.assertNotNull(assessment.getWorkflowConfig().getEmailConfig(), "Email details not found in db");
			Assert.assertEquals(
					assessment.getWorkflowConfig().getEmailConfig().getMailCC(), dailyEmailCCAssessmentReportJson
							.get("emailDetails").getAsJsonObject().get("receiverCCEmailAddress").getAsString(),
					"Email details not found in db");
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}

	}

	@Test(priority = 41, expectedExceptions = InsightsCustomException.class)
	public void testSaveEmailBCCAssessmentReport() throws InsightsCustomException, InterruptedException {
		Thread.sleep(1000);
		try {
			int assessmentid = assessmentService.saveAssessmentReport(dailyEmailBCCAssessmentReportJson);
			InsightsAssessmentConfiguration assessment = reportConfigDAL.getAssessmentByConfigId(assessmentid);
			Assert.assertNotNull(assessment, "Report not found in db");
			Assert.assertEquals(assessment.getReportTemplateEntity().getReportId(),
					dailyEmailAssessmentReportJson.get("reportTemplate").getAsInt(),
					"Report Template not present in db as expected");
			Assert.assertEquals(assessment.getStartDate(), dailyEmailBCCExpectedAssessmentStartDate,
					"Start Date not equal");
			Assert.assertEquals(assessment.getEndDate(), dailyEmailBCCExpectedAssessmentEndDate, "End date not equal");
			Assert.assertEquals(assessment.getWorkflowConfig().getNextRun(), dailyEmailBCCExpectedNextRun,
					"Next run not equal");
			Assert.assertTrue(assessment.getWorkflowConfig().getTaskSequenceEntity().size() > 0,
					"Records not present in Workflow Task Sequence table");
			Set<InsightsWorkflowTaskSequence> set = assessment.getWorkflowConfig().getTaskSequenceEntity();
			int noOfTasks = set.size();
			set.forEach(x -> {
				if (x.getSequence() == noOfTasks)
					Assert.assertEquals(x.getNextTask().intValue(), -1, "Last task's next task not assigned -1");
			});
			Assert.assertNotNull(assessment.getWorkflowConfig().getEmailConfig(), "Email details not found in db");
			Assert.assertEquals(
					assessment.getWorkflowConfig().getEmailConfig().getSubject(), dailyEmailBCCAssessmentReportJson
							.get("emailDetails").getAsJsonObject().get("receiverBCCEmailAddress").getAsString(),
					"Email details not found in db");
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 42)
	public void testUpdateAssessmentReport() throws InsightsCustomException {
		try {
			String updateAssessmentReport = "{\"isReoccuring\":true,\"emailList\":\"dasdsd\",\"id\":"
					+ reportConfigDAL.getAssessmentByAssessmentName(
							dailyEmailAssessmentReportJson.get("reportName").getAsString()).getId()
					+ ",\"tasklist\":[{\"taskId\":1,\"description\":\"KPI_Execute_service_test\"},{\"taskId\":2,\"description\":\"PDF_Execute_service_test\"},{\"taskId\":3,\"description\":\"Email_Execute_service_test\"}],\"emailDetails\":{\"senderEmailAddress\":\"onedevops@cogdevops.com\",\"receiverEmailAddress\":\"\",\"receiverCCEmailAddress\":\"dibakor.barua@cognizant.com\",\"receiverBCCEmailAddress\":\"\",\"mailSubject\":\"{ReportDisplayName} - {TimeOfReportGeneration}\",\"mailBodyTemplate\":\"Dear User,\\nPlease find attached Assessment Report  {ReportDisplayName}\\ngenerated on {TimeOfReportGeneration}.\\n\\nRegards,\\nOneDevops Team.\\n** This is an Auto Generated Mail by Insights scheduler. Please Do not reply to this mail**\"}}";
			final JsonObject updateAssessmentReportJson = convertStringIntoJson(updateAssessmentReport);
			int assessmentId = assessmentService.updateAssessmentReport(updateAssessmentReportJson);
			Assert.assertEquals(assessmentId, updateAssessmentReportJson.get("id").getAsInt());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 43)
	public void testsetRetryStatus() throws InsightsCustomException, InterruptedException {
		Thread.sleep(1000);
		try {
			int assessmentid = assessmentService.saveAssessmentReport(dailyRestartAssessmentReport);
			int configId = reportConfigDAL
					.getAssessmentByAssessmentName(dailyRestartAssessmentReport.get("reportName").getAsString())
					.getId();
			String retryJson = "{\"configId\":" + configId + ",\"status\":\"RESTART\"}";
			JsonObject reportRestartJson = parser.parse(retryJson).getAsJsonObject();
			String status = assessmentService.setReportStatus(reportRestartJson);
			InsightsAssessmentConfiguration assessment = reportConfigDAL
					.getAssessmentByConfigId(Integer.valueOf(configId));
			Assert.assertEquals(status, PlatformServiceConstants.SUCCESS);
			Assert.assertEquals(assessment.getWorkflowConfig().getStatus(), "RESTART");
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}

	}

	@Test(priority = 44)
	public void testrunImmediateStatus() throws InsightsCustomException, InterruptedException {
		Thread.sleep(1000);
		try {
			int assessmentid = assessmentService.saveAssessmentReport(dailyRunImmediateAssessmentReport);
			int configId = reportConfigDAL
					.getAssessmentByAssessmentName(dailyRunImmediateAssessmentReport.get("reportName").getAsString())
					.getId();
			String retryJson = "{\"configId\":" + configId + ",\"runimmediate\":true}";
			JsonObject reportRestartJson = parser.parse(retryJson).getAsJsonObject();
			String status = assessmentService.setReportStatus(reportRestartJson);
			InsightsAssessmentConfiguration assessment = reportConfigDAL
					.getAssessmentByConfigId(Integer.valueOf(configId));
			Assert.assertEquals(status, PlatformServiceConstants.SUCCESS);
			Assert.assertTrue(assessment.getWorkflowConfig().isRunImmediate());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}

	}

	@AfterTest
	public void cleanUp() {

		// Delete assessment reports
		List<String> assessmentReportNames = Arrays.asList(weeklyAssessmentReportJson.get("reportName").getAsString(),
				monthlyAssessmentReportJson.get("reportName").getAsString(),
				quarterlyAssessmentReportJson.get("reportName").getAsString(),
				yearlyAssessmentReportJson.get("reportName").getAsString(),
				oneTimeAssessmentReportJson.get("reportName").getAsString(),
				biWeeklyAssessmentReportJson.get("reportName").getAsString(),
				triWeeklyAssessmentReportJson.get("reportName").getAsString(),
				triWeeklyAssessmentWithDataSourceReportJson.get("reportName").getAsString(),
				dailyEmailAssessmentReportJson.get("reportName").getAsString(),
				dailyEmailCCAssessmentReportJson.get("reportName").getAsString(),
				dailyEmailBCCAssessmentReportJson.get("reportName").getAsString(),
				dailywithoutEmailAssessmentReportJson.get("reportName").getAsString(),
				dailyRestartAssessmentReport.get("reportName").getAsString(),
				dailyRunImmediateAssessmentReport.get("reportName").getAsString());
		for (String assessmentReport : assessmentReportNames) {
			try {
				InsightsAssessmentConfiguration assessment = reportConfigDAL
						.getAssessmentByAssessmentName(assessmentReport.trim());
				String workflowID = assessment.getWorkflowConfig().getWorkflowId();
				int configID = assessment.getId();
				reportConfigDAL.deleteAssessmentReport(configID);
				workflowConfigDAL.deleteWorkflowTaskSequence(workflowID);
			} catch (Exception e) {
				log.error("Error cleaning up at AssessmentReportsServiceTest Assessment Records ", e);
			}
		}

		// deleteTask
		try {
			InsightsWorkflowTask tasks = workflowConfigDAL.getTaskbyTaskDescription("KPI_Execute_service_test");
			int taskID = tasks.getTaskId();
			workflowConfigDAL.deleteTask(taskID);
		} catch (Exception e) {
			log.error("Error cleaning up at AssessmentReportsServiceTest KPIWorkflow Task", e);
		}
		try {
			InsightsWorkflowTask pdftasks = workflowConfigDAL.getTaskbyTaskDescription("PDF_Execute_service_test");
			int pdftaskID = pdftasks.getTaskId();
			workflowConfigDAL.deleteTask(pdftaskID);
		} catch (Exception e) {
			log.error("Error cleaning up at AssessmentReportsServiceTest PDFWorkflow Task", e);
		}
		try {
			InsightsWorkflowTask emailtasks = workflowConfigDAL.getTaskbyTaskDescription("Email_Execute_service_test");
			int emailtaskID = emailtasks.getTaskId();
			workflowConfigDAL.deleteTask(emailtaskID);
		} catch (Exception e) {
			log.error("Error cleaning up at AssessmentReportsServiceTest EmailWorkflow Task", e);
		}

		// Delete Report Templates
		try {
			reportConfigDAL.deleteReportTemplatebyReportID(reportTemplateJson.get("reportId").getAsInt());
		} catch (Exception e) {
			log.error("Error cleaning up at AssessmentReportsServiceTest Report template", e);
		}
		// Delete Content
		try {
			reportConfigDAL.deleteContentbyContentID(registerContentJson.get("contentId").getAsInt());
		} catch (Exception e) {
			log.error("Error cleaning up at AssessmentReportsServiceTest Content record", e);
		}

		// Delete KPI
		try {
			reportConfigDAL.deleteKPIbyKpiID(registerkpiJson.get("kpiId").getAsInt());
		} catch (Exception e) {
			log.error("Error cleaning up at AssessmentReportsServiceTest KPI record", e);
		}
		// Delete bulk Content
		for (int element : contentIdList) {
			try {
				reportConfigDAL.deleteContentbyContentID(element);
			} catch (Exception e) {
				log.error("Error cleaning up at AssessmentReportsServiceTest Bulk Content record", e);
			}
		}

		// Delete bulk kpi

		for (int element : kpiIdList) {
			try {
				reportConfigDAL.deleteKPIbyKpiID(element);
			} catch (Exception e) {
				log.error("Error cleaning up at AssessmentReportsServiceTest Bulk KPI record", e);
			}
		}

	}

}
