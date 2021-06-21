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

package com.cognizant.devops.platformregressiontest.test.ui.configurationfilemanagement;

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
import com.cognizant.devops.platformregressiontest.test.ui.testdata.ConfigurationFileDataProvider;
import com.cognizant.devops.platformregressiontest.test.ui.testdatamodel.ConfigurationFileManagementDataModel;

/**
 * @author NivethethaS
 * 
 *         Class contains the test cases for Configuration File Management
 *         module test cases
 *
 */
public class ConfigurationFileManagementTest extends LoginAndSelectModule {

	private static final Logger log = LogManager.getLogger(ConfigurationFileManagementTest.class);

	ConfigurationFileManagementConfiguration clickAllActionButton;

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
		getData(ConfigOptionsTest.CONFIGURATION_FILE_DIR + File.separator + ConfigOptionsTest.CONFIGURATION_JSON_FILE);
		selectModuleUnderConfiguration(LoginAndSelectModule.testData.get("configurationFile"));
		clickAllActionButton = new ConfigurationFileManagementConfiguration();
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
	public void navigateToCorrelationBuilderLandingPage() {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.navigateToConfigurationLandingPage(), "Landing page is displayed");
	}

	/**
	 * Assert true if adding module configuration is successful else false
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 2, enabled = true, dataProvider = "addConfigFilesData", dataProviderClass = ConfigurationFileDataProvider.class)
	public void addModuleConfiguration(ConfigurationFileManagementDataModel data) throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.addNewConfiguration(data), "Adding new configuration");
	}

	/**
	 * Assert true if error message is displayed when adding the same module's
	 * configuration else false
	 * 
	 * @throws InterruptedException
	 */

	@Test(priority = 3)
	public void addSameModuleConfiguration() throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.addSameConfiguration(), "Error message is displayed ");
	}

	/**
	 * Assert true if editing is successful else false
	 * 
	 * @throws InterruptedException
	 */

	@Test(priority = 4)
	public void editModuleConfiguration() {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.editConfiguration(), "Editting done successfully");
	}

	/**
	 * Assert true if refresh, reset and redirect to landing page functionalities
	 * are successful else false
	 * 
	 * @throws InterruptedException
	 */

	@Test(priority = 5)
	public void checkRefreshAndResetFunctionality() throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.refreshAndResetFunctionality(),
				"Refresh, Reset and Redirect to landing page functionalities working");
	}

	/**
	 * Assert true if deleting the existing configuration is successful else false
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 5)
	public void deleteModuleConfiguration() {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.deleteConfiguration(), "Deleting correlations successful");
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
