/*******************************************************************************
 * Copyright 2022 Cognizant Technology Solutions
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
package com.cognizant.devops.platformregressiontest.test.ui.multipleemailconfig;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;
import com.cognizant.devops.platformregressiontest.test.ui.dashboardreportdownload.DashboardReportDownloadConfiguration;
import com.cognizant.devops.platformregressiontest.test.ui.testdata.DashboardReportDataProvider;
import com.cognizant.devops.platformregressiontest.test.ui.testdata.GroupEmailDashRepoDataProvider;
import com.cognizant.devops.platformregressiontest.test.ui.testdatamodel.DashboardReportDataModel;
import com.cognizant.devops.platformregressiontest.test.ui.testdatamodel.GroupEmailDashRepoDataModel;

public class MultipleEmailDashRepoTest extends LoginAndSelectModule{

	private static final Logger log = LogManager.getLogger(MultipleEmailDashRepoTest.class);
	MultipleEmailDashRepoConfiguration multiEmailDash;
	
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
		getData(ConfigOptionsTest.GROUP_EMAIL_CONFIG_DASH + File.separator
				+ ConfigOptionsTest.GROUP_EMAIL_DASH_REPO);
		selectMenuOption("Dashboard Report Download");
		multiEmailDash = new MultipleEmailDashRepoConfiguration();
	}
	
	/**
	 * This method will be executed just before any function/method with @Test
	 * annotation starts.
	 * @throws InterruptedException 
	 */
	@BeforeMethod
	public void beforeMethod() throws InterruptedException {
		Thread.sleep(5000);
	}
	
	/**
	 * Assert true if landing page is displayed else false
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 1)
	public void navigateToGroupEmailConfigPage() {
		log.info(line);
		Assert.assertTrue(multiEmailDash.navigateToGroupEmailConfigPage(), "Landing page is displayed");
	}
	
	/**
	 * Assert true if  group email report is created successfully
	 * 
	 * @param data
	 */
	@Test(priority = 2, enabled = true, dataProvider = "createGroupEmailReport", dataProviderClass = GroupEmailDashRepoDataProvider.class)
	public void createGroupEmailReport(GroupEmailDashRepoDataModel data) {
		log.info(line);
		Assert.assertTrue(multiEmailDash.createGroupEmailReport(data), "Successfully created the Group Email report.");
	}
	
	/**
	 * Assert true if  group email report is edited successfully
	 * 
	 * @param data
	 */
	@Test(priority = 3, enabled = true, dataProvider = "editGroupEmailReport", dataProviderClass = GroupEmailDashRepoDataProvider.class)
	public void editGroupEmailReport(GroupEmailDashRepoDataModel data) {
		log.info(line);
		Assert.assertTrue(multiEmailDash.editGroupEmailReport(data),
				"Successfully edited the Group Email report.");
	}
	
	/**
	 * Assert true if details functionality working successfully
	 * 
	 * @param data
	 */
	@Test(priority = 4, enabled = true, dataProvider = "createGroupEmailReport", dataProviderClass = GroupEmailDashRepoDataProvider.class)
	public void checkDetailsFunctionality(GroupEmailDashRepoDataModel data) {
		log.info(line);
		Assert.assertTrue(multiEmailDash.checkDetailsFunctionality(data), "Details functionality working.");
	}
	
	/**
	 * Assert true if active status is changed successfully
	 * 
	 * @param data
	 */
	@Test(priority = 5, enabled = true, dataProvider = "createGroupEmailReport", dataProviderClass = GroupEmailDashRepoDataProvider.class)
	public void checkStatus(GroupEmailDashRepoDataModel data) {
		log.info(line);
		Assert.assertTrue(multiEmailDash.checkStatus(data), "Group Email report status updated successfully.");
	}
	
	/**
	 * Asserts true if the Group Email Report is deleted successfully
	 * 
	 * @param data
	 */
	@Test(priority = 6, enabled = true, dataProvider = "createGroupEmailReport", dataProviderClass = GroupEmailDashRepoDataProvider.class)
	public void deleteGroupEmailReport(GroupEmailDashRepoDataModel data) {
		log.info(line);
		Assert.assertTrue(multiEmailDash.deleteGroupEmailReport(data), "Multiple Email Report deleted successfully.");
	}
	
}
