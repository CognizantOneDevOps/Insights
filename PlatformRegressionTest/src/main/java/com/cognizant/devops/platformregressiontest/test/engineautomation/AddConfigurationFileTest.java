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
package com.cognizant.devops.platformregressiontest.test.engineautomation;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

public class AddConfigurationFileTest extends LoginAndSelectModule {

	private static final Logger log = LogManager.getLogger(AddConfigurationFileTest.class);

	AddConfigurationFile clickAllActionButton;

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
		getData(ConfigOptionsTest.ENGINE_AUTO_DIR + File.separator + ConfigOptionsTest.CONFIGURATION_JSON_FILE);
		selectModuleUnderConfiguration(LoginAndSelectModule.testData.get("configurationFile"));
		clickAllActionButton = new AddConfigurationFile();
	}

	/**
	 * This method will be executed just before any function/method with @Test
	 * annotation starts.
	 * @throws InterruptedException 
	 */
	@BeforeMethod
	public void beforeMethod() throws InterruptedException {
                  Thread.sleep(1000);
     }

	/**
	 * Assert true if landing page is displayed else false
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 1)
	public void navigateToConfigurationFileManagementLandingPage() {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.navigateToConfigurationLandingPage(), "Landing page is displayed");
	}

	/**
	 * Assert true if adding module configuration is successful else false
	 * @throws Exception 
	 */
	@Test(priority = 2)
	public void addModuleConfiguration() throws Exception {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.addNewConfiguration(), "Adding new configuration");
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
