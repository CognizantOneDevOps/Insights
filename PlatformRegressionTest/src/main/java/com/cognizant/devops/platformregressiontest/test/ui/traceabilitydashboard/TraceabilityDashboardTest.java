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
package com.cognizant.devops.platformregressiontest.test.ui.traceabilitydashboard;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;
import com.cognizant.devops.platformregressiontest.test.ui.testdata.DashboardReportDataProvider;
import com.cognizant.devops.platformregressiontest.test.ui.testdata.TraceabilityDataProvider;
import com.cognizant.devops.platformregressiontest.test.ui.testdatamodel.TraceabilityDataModel;

public class TraceabilityDashboardTest extends LoginAndSelectModule {

	private static final Logger log = LogManager.getLogger(TraceabilityDashboardTest.class);

	TraceabilityDashboardConfiguration clickAllActionButton;

	String line = "============================================================================================================================================================";

	/**
	 * This method will be run before any test method belonging to the classes
	 * inside the <test> tag is run.
	 * 
	 * @throws InterruptedException
	 */
	@BeforeTest
	public void setUp() throws InterruptedException {
		initialization();
		getData(ConfigOptionsTest.TRACEABILITY_DASHBOARD_DIR + File.separator
				+ ConfigOptionsTest.TRACEABILITY_JSON_FILE);
		selectModuleOnClickingDashboardgroups(LoginAndSelectModule.testData.get("traceability"));
		clickAllActionButton = new TraceabilityDashboardConfiguration();
	}

	/**
	 * This method will be executed just before any function/method with @Test
	 * annotation starts.
	 */
	@BeforeMethod
	public void beforeMethod() {
		log.info(line);
	}

	/**
	 * Assert true if landing page is displayed else false
	 */
	@Test(priority = 1)
	public void navigateToTraceabilityLandingPage() {
		Assert.assertTrue(clickAllActionButton.navigateToTraceabilityLandingPage(), "Landing page is not displayed");
	}

	/**
	 * Assert true if search is successful and traceability matrix is displayed else
	 * false
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 2, enabled = true, dataProvider = "traceabilityjsondataprovider", dataProviderClass = TraceabilityDataProvider.class)
	public void validateTraceabilityMatrix(TraceabilityDataModel data) throws InterruptedException {
		Assert.assertTrue(clickAllActionButton.validateTraceabilityMatrix(data), "Traceability functionality successful");
	}
	
	/**
	 * Assert true if more info and issue details are displayed else
	 * false
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 3)
	public void checkCount() throws InterruptedException {
		Assert.assertTrue(clickAllActionButton.checkCount(), "Check count functionality successful");
	}
	
	/**
	 * Assert true if the page contents are cleared after clicking on clear button else false
	 * @throws InterruptedException 
	 */
	@Test(priority = 4)
	public void clearButtonFunctionality() throws InterruptedException {
		Assert.assertTrue(clickAllActionButton.clearFunctionality(), "clear functionality successful");
	}

	/**
	 * Assert true if error is displayed for incorrect search else false
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 5)
	public void incorrectSearch() throws InterruptedException {
		Assert.assertTrue(clickAllActionButton.incorrectSearch(), "Error is displayed for incorrect search");
	}
}