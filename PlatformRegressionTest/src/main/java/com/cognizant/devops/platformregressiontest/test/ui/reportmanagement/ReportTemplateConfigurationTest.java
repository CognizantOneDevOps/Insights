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
package com.cognizant.devops.platformregressiontest.test.ui.reportmanagement;


import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;
import com.cognizant.devops.platformregressiontest.test.ui.testdata.ReportManagementDataProvider;

/**
 * @author Ankita
 *
 */
public class ReportTemplateConfigurationTest extends LoginAndSelectModule {

	ReportTemplateConfigurationPage reportTemplateConfigurationPage;

	@BeforeTest
	public void setUp() {
		initialization();
		selectMenuOption("Report Template Configuration");
		reportTemplateConfigurationPage = new ReportTemplateConfigurationPage();
	}

	// Add Report Template

	/**
	 * This method tests create report template page
	 * 
	 * @param reportName
	 * @param description
	 * @param visualizationutil
	 * @param kpiId
	 * @param vType
	 * @param vQuery
	 * @throws InterruptedException 
	 */
	@Test(priority = 1, dataProvider = "createReportTemplatedataprovider", dataProviderClass = ReportManagementDataProvider.class, enabled = true)
	public void createReportTemplate(String reportName, String description, String visualizationutil,String templateType, String kpiId,
			String vType, String vQuery) throws InterruptedException {

		Assert.assertEquals(reportTemplateConfigurationPage.createReportTemplate(reportName, description,
				visualizationutil,templateType, kpiId, vType, vQuery), true);

	}

	/**
	 * This method tests validation on create report template screen
	 * 
	 * @param reportName
	 * @param description
	 * @param visualizationutil
	 * @param kpiId
	 * @param vType
	 * @param vQuery
	 * @throws InterruptedException
	 */
	@Test(priority = 2, dataProvider = "createValidateReportTemplatedataprovider", dataProviderClass = ReportManagementDataProvider.class, enabled = true)
	public void createValidateReportTemplate(String reportName, String description, String visualizationutil,String templateType,
			String kpiId, String vType, String vQuery) throws InterruptedException {

		Assert.assertEquals(reportTemplateConfigurationPage.createValidateReportTemplate(reportName, description,
				visualizationutil,templateType, kpiId, vType, vQuery), true);

	}

	/**
	 * This method tests upload json functionality
	 * 
	 * @param fileName
	 * @throws InterruptedException 
	 */
	@Test(priority = 3, dataProvider = "uploadJsonReportTemplatedataprovider", dataProviderClass = ReportManagementDataProvider.class, enabled = true)
	public void uploadJsonTest(String fileName) throws InterruptedException {

		Assert.assertEquals(reportTemplateConfigurationPage.uploadJson(fileName), true);
	}

	/**
	 * This method tests config files attachment functionality
	 * 
	 * @param reportTemplateName
	 * @param configFileName
	 * @throws InterruptedException 
	 */
	@Test(priority = 4, dependsOnMethods = {
			"uploadJsonTest" }, dataProvider = "reportConfigFilesdataprovider", dataProviderClass = ReportManagementDataProvider.class, enabled = true)
	public void attachReportConfigFiles(String reportTemplateName, String configFileName) throws InterruptedException {

		Assert.assertEquals(reportTemplateConfigurationPage.attachReportConfigFiles(reportTemplateName, configFileName),
				true);

	}

	/**
	 * This method tests edit screen functionality along with delete KPI
	 * 
	 * @param reportName
	 * @param kpiId
	 * @param description
	 * @param vType
	 * @param vQuery
	 * @throws InterruptedException 
	 */

	@Test(priority = 5, dataProvider = "editReportTemplatedataprovider", dataProviderClass = ReportManagementDataProvider.class, enabled = true)
	public void editReportTemplate(String reportName, String kpiId, String description, String vType, String vQuery) throws InterruptedException {

		reportTemplateConfigurationPage.editReportTemplate(reportName, kpiId, description, vType, vQuery);
	}

	/**
	 * This method tests refresh button functionality
	 * 
	 * @param reportName
	 * @throws InterruptedException 
	 */
	@Test(priority = 6, dataProvider = "deleteReportTemplatedataprovider", dataProviderClass = ReportManagementDataProvider.class, enabled = true)
	public void refreshReportTemplate(String reportName) throws InterruptedException {

		Assert.assertEquals(reportTemplateConfigurationPage.checkRefreshButton(reportName), true);
	}

	/**
	 * This method tests report template delete functionality
	 * 
	 * @param reportName
	 * @throws InterruptedException 
	 */
	@Test(priority = 7, dataProvider = "deleteReportTemplatedataprovider", dataProviderClass = ReportManagementDataProvider.class, enabled = true)
	public void deleteReportTemplate(String reportName) throws InterruptedException {

		reportTemplateConfigurationPage.deleteReportTemplate(reportName);
	}

	public void checkReportDetails(String reportName) throws InterruptedException {
		
		reportTemplateConfigurationPage.checkReportDetails(reportName);
	}
}
