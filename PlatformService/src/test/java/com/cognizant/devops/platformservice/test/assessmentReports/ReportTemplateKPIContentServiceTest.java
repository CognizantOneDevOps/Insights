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
import java.util.List;

import javax.json.Json;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.multipart.MultipartFile;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.assessmentreport.InsightsAssessmentReportTemplate;
import com.cognizant.devops.platformdal.assessmentreport.InsightsKPIConfig;
import com.cognizant.devops.platformdal.assessmentreport.InsightsReportTemplateConfigFiles;
import com.cognizant.devops.platformdal.assessmentreport.ReportConfigDAL;
import com.cognizant.devops.platformservice.assessmentreport.controller.InsightsAssessmentReportController;
import com.cognizant.devops.platformservice.assessmentreport.service.AssesmentReportServiceImpl;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Test
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
@SuppressWarnings("unused")
@WebAppConfiguration
public class ReportTemplateKPIContentServiceTest extends AssessmentReportServiceData {
	private static final Logger log = LogManager.getLogger(ReportTemplateKPIContentServiceTest.class);

	static int reportId = 0;
	static int uploadedTemplateId = 0;

	@BeforeClass
	public void prepareData() throws InsightsCustomException {
	}

	@Autowired
	InsightsAssessmentReportController insightsAssessmentReportController;
	@Autowired
	AssesmentReportServiceImpl assessmentService;

	ReportConfigDAL reportConfigDAL = new ReportConfigDAL();

	@Test(priority = 1)
	public void testVisualizationUtil() throws InsightsCustomException {
		try {
			JsonObject response = insightsAssessmentReportController.getVisualizationUtil();
			JsonArray chartHandlerList = response.getAsJsonArray("data");
			Assert.assertNotNull(chartHandlerList);
			Assert.assertFalse(chartHandlerList.isEmpty());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 2)
	public void testAllChartType() throws InsightsCustomException {
		try {
			JsonObject response = insightsAssessmentReportController.getChartType();
			JsonObject vTypeList = response.getAsJsonObject("data");
			Assert.assertTrue(vTypeList.getAsJsonArray("vTypes").size() > 0);
			Assert.assertFalse(vTypeList.keySet().isEmpty());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 3)
	public void testContentAction() throws InsightsCustomException {
		try {
			JsonObject response = insightsAssessmentReportController.getContentAction();
			JsonArray listOfCategory = response.getAsJsonArray("data");
			Assert.assertNotNull(listOfCategory);
			Assert.assertFalse(listOfCategory.isEmpty());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 4)
	public void testKpiDataSource() throws InsightsCustomException {
		try {
			JsonObject response = insightsAssessmentReportController.getKpiDataSourcelist();
			JsonArray listOfDatasource = response.getAsJsonArray("data");
			Assert.assertNotNull(listOfDatasource);
			Assert.assertFalse(listOfDatasource.isEmpty());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 5)
	public void testKpiCategory() throws InsightsCustomException {
		try {
			JsonObject response = insightsAssessmentReportController.getKpiCategorylist();
			JsonArray listOfCategory = response.getAsJsonArray("data");
			Assert.assertNotNull(listOfCategory);
			Assert.assertFalse(listOfCategory.isEmpty());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 6)
	public void testjobSchedule() throws InsightsCustomException {
		try {
			JsonObject response = insightsAssessmentReportController.getScheduleList();
			JsonArray listOfSchedule = response.getAsJsonArray("data");
			Assert.assertNotNull(listOfSchedule);
			Assert.assertFalse(listOfSchedule.isEmpty());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 7)
	public void testSaveReportTemplate() throws InsightsCustomException {
		try {
			try {
	            Thread.sleep(10000);
	        } catch (InterruptedException e) {
	            log.debug(e.getMessage());
	        }
			JsonObject response = insightsAssessmentReportController.saveReportTemplate(convertStringIntoJson(reportTemplateSave).toString());
			log.debug(response);
			reportId = Integer.parseInt(response.get("data").getAsString().replace("\"", "").replaceAll("[^0-9]", ""));
			InsightsAssessmentReportTemplate report = (InsightsAssessmentReportTemplate) reportConfigDAL
					.getReportTemplateByReportId(reportId);
			Assert.assertNotNull(report);
			Assert.assertTrue(report.getReportsKPIConfig().size() > 0);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 8)
	public void testActiveKpiList() throws InsightsCustomException {
		try {
			JsonObject response = insightsAssessmentReportController.getAllActiveKpiList();
			JsonArray kpiconfigList = response.getAsJsonArray("data");
			Assert.assertNotNull(kpiconfigList);
			Assert.assertFalse(kpiconfigList.isEmpty());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 9)
	public void testGetReportTemplate() throws InsightsCustomException {
		try {
			JsonObject response = insightsAssessmentReportController.getReportTemplateList();
			JsonArray listOfReportTemplate = response.getAsJsonArray("data");

			Assert.assertNotNull(listOfReportTemplate);
			Assert.assertFalse(listOfReportTemplate.isEmpty());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 10)
	public void testGetAllActiveContentList() {
		try {

			JsonObject response = insightsAssessmentReportController.getAllActiveContentList();
			String expectedStatus = "success";
			String actualStatus = response.get("status").getAsString().replace("\"", "");
			JsonArray contentArray = response.getAsJsonArray("data");

			Assert.assertEquals(actualStatus, expectedStatus);
			Assert.assertTrue(contentArray.size() > 0);
			Assert.assertNotNull(contentArray);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 11)
	public void testEditReportTemplate() throws InsightsCustomException {
		try {
			JsonObject responseFirst = insightsAssessmentReportController.saveKpiDefinition(registerkpi);
			JsonObject responseSecond = insightsAssessmentReportController.saveKpiDefinition(registerSecondkpi);

			String editReportTemplate = "{\"reportName\":\"report_template_save\",\"reportId\":\"reportIdData\",\"description\":\"Testing\",\"isActive\":true,\"visualizationutil\":\"FUSION\",\"kpiConfigs\":[{\"kpiId\":100201,\"visualizationConfigs\":[{\"vId\":\"100\",\"vQuery\":\"Query\"}]},{\"kpiId\":100144,\"visualizationConfigs\":[{\"vId\":\"100\",\"vQuery\":\"Query\"}]}]}";

			log.debug(" editReportTemplate  {} ", editReportTemplate);
			editReportTemplate = editReportTemplate.replace("reportIdData", String.valueOf(reportId));

			JsonObject responseJson = insightsAssessmentReportController.editReportTemplate(editReportTemplate);
			InsightsAssessmentReportTemplate report = (InsightsAssessmentReportTemplate) reportConfigDAL
					.getReportTemplateByReportId(reportId);
			Assert.assertNotNull(report);
			Assert.assertTrue(report.getReportsKPIConfig().size() >= 2);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 12)
	public void testDeleteReportTemplate() throws InsightsCustomException {
		try {
			String templateToDelete = "{\"reportId\":\"reportIdData\"}";
			templateToDelete = templateToDelete.replace("reportIdData", String.valueOf(reportId));

			JsonObject response = insightsAssessmentReportController.deleteReportTemplate(templateToDelete);
			InsightsAssessmentReportTemplate report = null;
			try {
				report = (InsightsAssessmentReportTemplate) reportConfigDAL.getReportTemplateByReportId(reportId);
			} catch (Exception e) {
				log.error(e);
			}
			Assert.assertNull(report);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 13)
	public void testUploadReportTemplate() throws InsightsCustomException, IOException {
		try {
			FileInputStream input = new FileInputStream(file);
			MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain",
					IOUtils.toByteArray(input));

			JsonObject response = insightsAssessmentReportController.uploadReportTemplate(multipartFile);
			String expectedStatus = "success";
			String actualStatus = response.get("status").getAsString().replace("\"", "");
			uploadedTemplateId = Integer
					.parseInt(response.get("data").getAsString().replace("\"", "").replaceAll("[^0-9]", ""));

			Assert.assertEquals(actualStatus, expectedStatus);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 14)
	public void testDeleteUploadedReportTemplate() throws InsightsCustomException {
		try {
			String templateToDelete = "{\"reportId\":\"reportIdData\"}";
			templateToDelete = templateToDelete.replace("reportIdData", String.valueOf(uploadedTemplateId));

			JsonObject response = insightsAssessmentReportController.deleteReportTemplate(templateToDelete);
			InsightsAssessmentReportTemplate report = null;
			try {
				report = (InsightsAssessmentReportTemplate) reportConfigDAL
						.getReportTemplateByReportId(uploadedTemplateId);
			} catch (Exception e) {
				log.error(e);
			}
			Assert.assertNull(report);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 15)
	public void testUploadedReportTemplateWithWrongFile() throws InsightsCustomException, IOException {
		try {
			FileInputStream input = new FileInputStream(configFileTxt);
			MultipartFile multipartFile = new MockMultipartFile("file", configFileTxt.getName(), "text/plain",
					IOUtils.toByteArray(input));
			JsonObject response = insightsAssessmentReportController.uploadReportTemplate(multipartFile);
			String expectedStatus = "failure";
			String expectedOutcome = "Invalid Report Template file format.";

			String actualStatus = response.get("status").getAsString().replace("\"", "");
			String actualOutcome = response.get("message").getAsString().replace("\"", "");

			Assert.assertEquals(actualStatus, expectedStatus);
			Assert.assertEquals(actualOutcome, expectedOutcome);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 16)
	public void testUploadReportTemplateDesignFilesWithWrongFile() throws InsightsCustomException, IOException {
		try {
			FileInputStream input = new FileInputStream(configFileTxt);
			MultipartFile multipartFile = new MockMultipartFile("file", configFileTxt.getName(), "text/plain",
					IOUtils.toByteArray(input));
			MultipartFile[] files = new MultipartFile[1];
			files[0] = multipartFile;
			JsonObject response = insightsAssessmentReportController.uploadReportTemplateDesignFiles(files, reportId);
			String expectedStatus = "failure";
			String id = response.get("message").getAsString().replace("\"", "").replaceAll("[^0-9]", "");

			String expectedOutcome = " Report template not exists in database " + id;

			String actualStatus = response.get("status").getAsString().replace("\"", "");
			String actualOutcome = response.get("message").getAsString().replace("\"", "");

			Assert.assertEquals(actualStatus, expectedStatus);
			Assert.assertEquals(actualOutcome, expectedOutcome);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}

	}

	@Test(priority = 17)
	public void testGetTemplateType() throws InsightsCustomException {
		JsonObject response = insightsAssessmentReportController.getTemplateType();
		JsonArray remplateTypeList = response.getAsJsonArray("data");
		String expectedStatus = "success";

		String actualStatus = response.get("status").getAsString().replace("\"", "");

		Assert.assertEquals(actualStatus, expectedStatus);
		Assert.assertNotNull(remplateTypeList);
		Assert.assertFalse(remplateTypeList.isEmpty());
	}

	@AfterClass
	public void cleanUp() {
		for (int element : kpiIdList) {
			try {
				reportConfigDAL.deleteKPIbyKpiID(element);
			} catch (Exception e) {
				log.error("Error cleaning up at AssessmentReportsServiceTest Bulk KPI record", e);
			}
		}

		reportConfigDAL.deleteTemplateDesignFilesByReportTemplateID(reportId);

	}

}
