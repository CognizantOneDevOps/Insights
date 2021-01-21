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

import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
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
import com.cognizant.devops.platformservice.assessmentreport.service.AssesmentReportServiceImpl;
import com.google.gson.JsonObject;

@Test
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
@SuppressWarnings("unused")
public class ReportTemplateKPIContentServiceTest extends AssessmentReportServiceData {
	private static final Logger log = LogManager.getLogger(ReportTemplateKPIContentServiceTest.class);

	int reportId = 0;
	int uploadedTemplateId = 0;
	@BeforeClass
	public void prepareData() throws InsightsCustomException {
	}

	public static final AssesmentReportServiceImpl assessmentService = new AssesmentReportServiceImpl();
	ReportConfigDAL reportConfigDAL = new ReportConfigDAL();


	@Test(priority = 1)
	public void testVisualizationUtil() throws InsightsCustomException {
		try {
			List<String> chartHandlerList = assessmentService.getVisualizationUtil();
			Assert.assertNotNull(chartHandlerList);
			Assert.assertFalse(chartHandlerList.isEmpty());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 2)
	public void testAllChartType() throws InsightsCustomException {
		try {
			List<String> vTypeList = assessmentService.getAllChartType();
			Assert.assertNotNull(vTypeList);
			Assert.assertFalse(vTypeList.isEmpty());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 3)
	public void testContentAction() throws InsightsCustomException {
		try {
			List<String> listOfCategory = assessmentService.getContentAction();
			Assert.assertNotNull(listOfCategory);
			Assert.assertFalse(listOfCategory.isEmpty());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 4)
	public void testKpiDataSource() throws InsightsCustomException {
		try {
			List<String> listOfDatasource = assessmentService.getKpiDataSource();
			Assert.assertNotNull(listOfDatasource);
			Assert.assertFalse(listOfDatasource.isEmpty());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 5)
	public void testKpiCategory() throws InsightsCustomException {
		try {
			List<String> listOfCategory = assessmentService.getKpiCategory();
			Assert.assertNotNull(listOfCategory);
			Assert.assertFalse(listOfCategory.isEmpty());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 6)
	public void testjobSchedule() throws InsightsCustomException {
		try {
			List<String> listOfSchedule = assessmentService.getSchedule();
			Assert.assertNotNull(listOfSchedule);
			Assert.assertFalse(listOfSchedule.isEmpty());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 7)
	public void testSaveReportTemplate() throws InsightsCustomException {
		try {
			int response = assessmentService.saveKpiDefinition(convertStringIntoJson(registerkpi));
			kpiIdList.add(response);
			reportId = assessmentService.saveTemplateReport(convertStringIntoJson(reportTemplateSave));
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
			List<InsightsKPIConfig> kpiconfigList = assessmentService.getActiveKpiList();
			Assert.assertNotNull(kpiconfigList);
			Assert.assertFalse(kpiconfigList.isEmpty());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 9)
	public void testGetReportTemplate() throws InsightsCustomException {
		try {
			List<InsightsAssessmentReportTemplate> listOfReportTemplate = assessmentService.getReportTemplate();
			Assert.assertNotNull(listOfReportTemplate);
			Assert.assertFalse(listOfReportTemplate.isEmpty());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 10)
	public void testEditReportTemplate() throws InsightsCustomException {
		try {
			int response = assessmentService.saveKpiDefinition(convertStringIntoJson(registerSecondkpi));
			kpiIdList.add(response);
			editReportTemplate = editReportTemplate.replace("reportIdData", String.valueOf(reportId));
			log.debug(" editReportTemplate  {} ", editReportTemplate);
			reportId = assessmentService.editReportTemplate(convertStringIntoJson(editReportTemplate));
			InsightsAssessmentReportTemplate report = (InsightsAssessmentReportTemplate) reportConfigDAL
					.getReportTemplateByReportId(reportId);
			Assert.assertNotNull(report);
			Assert.assertTrue(report.getReportsKPIConfig().size() >= 2);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 11)
	public void testUploadReportTemplateDesignFiles() throws InsightsCustomException, IOException {
		try {
			String response = assessmentService.uploadReportTemplateDesignFiles(readReportTemplateDesignFiles(), reportId);
			List<InsightsReportTemplateConfigFiles> fileList = reportConfigDAL.getReportTemplateConfigFileByReportId(reportId);
			Assert.assertEquals(response, "File uploaded");
			Assert.assertTrue(fileList.size() > 0);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 12)
	public void testDeleteReportTemplate() throws InsightsCustomException {
		try {
			JsonObject deleteRequest = new JsonObject();
			deleteRequest.addProperty("reportId", reportId);
			String response = assessmentService.deleteReportTemplate(deleteRequest);
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
			FileInputStream input = new FileInputStream(templateJsonFile);
			MultipartFile multipartFile = new MockMultipartFile("file", configFile.getName(), "text/plain",
					IOUtils.toByteArray(input));
			String response = assessmentService.uploadReportTemplate(multipartFile);
			uploadedTemplateId = Integer.parseInt(response.replaceAll("[^0-9]", ""));
			InsightsAssessmentReportTemplate report = (InsightsAssessmentReportTemplate) reportConfigDAL
					.getReportTemplateByReportId(uploadedTemplateId);
			Assert.assertNotNull(report);
			Assert.assertTrue(report.getReportsKPIConfig().size() > 0);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 14)
	public void testDeleteUploadedReportTemplate() throws InsightsCustomException {
		try {
			JsonObject deleteRequest = new JsonObject();
			deleteRequest.addProperty("reportId", uploadedTemplateId);
			String response = assessmentService.deleteReportTemplate(deleteRequest);
			InsightsAssessmentReportTemplate report = null;
			try {
				report = (InsightsAssessmentReportTemplate) reportConfigDAL.getReportTemplateByReportId(uploadedTemplateId);
			} catch (Exception e) {
				log.error(e);
			}
			Assert.assertNull(report);
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 15, expectedExceptions = InsightsCustomException.class)
	public void testUploadedReportTemplateWithWrongFile() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(configFileTxt);
		MultipartFile multipartFile = new MockMultipartFile("file", configFileTxt.getName(), "text/plain",
				IOUtils.toByteArray(input));
		String response = assessmentService.uploadReportTemplate(multipartFile);
	}
	
	@Test(priority = 16, expectedExceptions = InsightsCustomException.class)
	public void testUploadReportTemplateDesignFilesWithWrongFile() throws InsightsCustomException, IOException {
		FileInputStream input = new FileInputStream(configFileTxt);
		MultipartFile multipartFile = new MockMultipartFile("file", configFileTxt.getName(), "text/plain",
				IOUtils.toByteArray(input));
		MultipartFile[] files = new MultipartFile[1];
		files[0] = multipartFile;
		String response = assessmentService.uploadReportTemplateDesignFiles(files, reportId);

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
