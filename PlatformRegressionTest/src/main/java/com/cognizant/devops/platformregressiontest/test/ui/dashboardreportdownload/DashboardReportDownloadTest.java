/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
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
package com.cognizant.devops.platformregressiontest.test.ui.dashboardreportdownload;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;
import com.cognizant.devops.platformregressiontest.test.ui.testdata.DashboardReportDataProvider;
import com.cognizant.devops.platformregressiontest.test.ui.testdatamodel.DashboardReportDataModel;

public class DashboardReportDownloadTest extends LoginAndSelectModule {

	private static final Logger log = LogManager.getLogger(DashboardReportDownloadTest.class);
	DashboardReportDownloadConfiguration dashboardReport;

	String line = "============================================================================================================================================================";

	/**
	 * This method will be run before any test method belonging to the classes
	 * inside the <test> tag is run.
	 * 
	 * @throws InterruptedException
	 */
	@BeforeTest
	public void setUp() {
		initialization();
		getData(ConfigOptionsTest.DASHBOARD_REPORT_DOWNLOAD_DIR + File.separator
				+ ConfigOptionsTest.DASHBOARDREPORTDOWNLOAD_JSON_FILE);
		selectMenuOption("Dashboard Report Download");
		dashboardReport = new DashboardReportDownloadConfiguration();
	}

	/**
	 * This method will be executed just before any function/method with @Test
	 * annotation starts.
	 */
	@BeforeMethod
	public void beforeMethod() {
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
	}

	/**
	 * Assert true if landing page is displayed else false
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 1)
	public void navigateToDashboardReportDownloadPage() {
		log.info(line);
		Assert.assertTrue(dashboardReport.navigateToDashboardReportDownloadPage(), "Landing page is displayed");
	}

	/**
	 * Assert true if report created successfully
	 * 
	 * @param data
	 */
	@Test(priority = 2, enabled = true, dataProvider = "reportWithMailingDetails", dataProviderClass = DashboardReportDataProvider.class)
	public void createReportWithMailingDetails(DashboardReportDataModel data) {
		log.info(line);
		Assert.assertTrue(dashboardReport.createReportWithMailingDetails(data), "Created report with mailing details.");
	}

	/**
	 * Assert true if report created successfully without adding mailing details
	 * 
	 * @param data
	 */
	@Test(priority = 3, enabled = true, dataProvider = "reportWithoutMailingDetails", dataProviderClass = DashboardReportDataProvider.class)
	public void createReportWithoutMailingDetails(DashboardReportDataModel data) {
		log.info(line);
		Assert.assertTrue(dashboardReport.createReportWithoutMailingDetails(data),
				"Created report without mailing details.");
	}

	/**
	 * Assert true if details functionality working successfully
	 * 
	 * @param data
	 */
	@Test(priority = 4, enabled = true, dataProvider = "reportWithMailingDetails", dataProviderClass = DashboardReportDataProvider.class)
	public void checkDetailsFunctionality(DashboardReportDataModel data) {
		log.info(line);
		Assert.assertTrue(dashboardReport.checkDetailsFunctionality(data), "Details functionality working.");
	}

	/**
	 * 
	 * Assert true if delete report functionality working successfully
	 * 
	 * @param data
	 */
	@Test(priority = 5, enabled = true, dataProvider = "reportWithMailingDetails", dataProviderClass = DashboardReportDataProvider.class)
	public void deleteReport(DashboardReportDataModel data) {
		log.info(line);
		Assert.assertTrue(dashboardReport.deleteReport(data), "Report deleted successfully.");
	}

	/**
	 * Assert true if refresh and redirect functionality working successfully
	 * 
	 * @param data
	 */
	@Test(priority = 6, enabled = true, dataProvider = "reportWithoutMailingDetails", dataProviderClass = DashboardReportDataProvider.class)
	public void checkRefreshAndRedirectFunctionality(DashboardReportDataModel data) {
		log.info(line);
		Assert.assertTrue(dashboardReport.checkRefreshAndRedirectFunctionality(data),
				"Refresh, Reset and Redirect to landing page functionalities working");
	}

	/**
	 * This method will be executed just after any function/method with @Test
	 * annotation ends.
	 */
	@AfterMethod
	public void afterMethod() {
		log.info(line);
	}

}
