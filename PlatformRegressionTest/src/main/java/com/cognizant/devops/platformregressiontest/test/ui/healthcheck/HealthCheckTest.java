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
package com.cognizant.devops.platformregressiontest.test.ui.healthcheck;

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

/**
 * @author Nainsi
 * 
 *         Class contains the test cases for Health Check Module
 *
 */
public class HealthCheckTest extends LoginAndSelectModule {

	private static final Logger log = LogManager.getLogger(HealthCheckTest.class);

	HealthCheckConfiguration clickAllActionButton;

	String line = "============================================================================================================================================================";

	/**
	 * This method will be run before any test method belonging to the classes
	 * inside the <test> tag is run.
	 */
	@BeforeTest
	public void setUp() {
		initialization();
		getData(ConfigOptionsTest.HEALTH_CHECK_DIR + File.separator + ConfigOptionsTest.HEALTHCHECK_JSON_FILE);
		selectMenuOption(LoginAndSelectModule.testData.get("healthCheck"));
		clickAllActionButton = new HealthCheckConfiguration();
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
	 * Assert true if landing page displayed successfully
	 */

	@Test(priority = 1)
	public void navigateToHealthCheckLandingPage() {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.navigateToHealthCheckLandingPage(), "Landing page is displayed");
	}

	/**
	 * Assert true if all registered agents are displayed on UI which are present in
	 * database
	 */

	@Test(priority = 2)
	public void checkAllRegisteredAgentDisplayed() {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.checkAllRegisteredAgentDisplayed(),
				"Agents data has been loaded successfully");
	}

	/**
	 * Assert true if Notification toggle functionality is working successfully.
	 */

	@Test(priority = 3)
	public void testNotificationToggle() {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.testNotificationToggle(), "Notification Toggle is in working mode");
	}

	/**
	 * Assert true if health check status icon is displayed.
	 */

	@Test(priority = 4)
	public void checkHealthCheckStatusIcon() {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.checkHealthCheckStatusIcon(),
				"Health check status of all agents is success.");
	}

	/**
	 * Assert true if Latest Status Details and Latest Failure Details are displayed
	 * in detail dialog box under Agents tab
	 * 
	 * @throws InterruptedException
	 */

	@Test(priority = 5)
	public void detailDialogBoxTabUnderAgentsTab() throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.detailDialogBoxTabUnderAgentsTab(),
				"Latest Status Details and Latest Failure Details are displayed in detail dialog box under Agents tab");
	}

	/**
	 * Assert true if Select tool functionality is working.
	 */
	@Test(priority = 6)
	public void testSelectToolUnderAgentsTab() {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.testSelectToolUnderAgentsTab(), "Select tool functionality is working.");
	}

	/**
	 * Assert true if all components present under Data Components Tab are having
	 * correct data.
	 */
	@Test(priority = 7)
	public void verifyDataComponentsTabData() {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.verifyDataComponentsTabData(),
				"All components present under Data Components Tab are having correct data.");
	}

	/**
	 * Assert true if for all servers under Data Components tab, health check is
	 * success.
	 */
	@Test(priority = 8)
	public void serverHealthCheckStatus() {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.serverHealthCheckStatus(),
				"For all servers under Data Components tab, health check is success.");
	}

	/**
	 * Assert true if all artifacts present under Services Tab are having correct
	 * data.
	 */
	@Test(priority = 9)
	public void verifyServicesTabData() {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.verifyServicesTabData(),
				"All artifacts present under Services Tab are having correct data.");
	}

	/**
	 * Assert true if for all services under Services tab, health check is success
	 */
	@Test(priority = 10)
	public void serviceHealthCheckStatus() {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.serviceHealthCheckStatus(),
				"For all services under Services tab, health check is success.");
	}

	/**
	 * Assert true if Latest Status Details are displayed in detail dialog box under
	 * Services tab
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 11)
	public void verifyDetailsDialogBoxUnderServicesTab() {
		Assert.assertTrue(clickAllActionButton.verifyDetailsDialogBoxUnderServicesTab(),
				"Latest Status Details and Latest Failure Details are displayed in detail dialog box under Services tab");
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
