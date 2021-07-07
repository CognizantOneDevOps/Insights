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
package com.cognizant.devops.platformregressiontest.test.ui.dataarchival;

/**
 * @author NivethethaS
 * 
 *         Class contains the test cases for Data Archival module
 *
 */
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

public class DataArchivalTest extends LoginAndSelectModule {

	private static final Logger log = LogManager.getLogger(DataArchivalTest.class);

	DataArchivalConfiguration clickAllActionButton;

	String line = "============================================================================================================================================================";

	/**
	 * This method will be run before any test method belonging to the classes
	 * inside the <test> tag is run.
	 * 
	 * @throws InterruptedException
	 */
	@BeforeTest
	public void setUp() throws InterruptedException {
		log.info(line);
		initialization();
		getData(ConfigOptionsTest.ARCHIVAL_JSON_FILE);
		selectModuleOnClickingConfig(LoginAndSelectModule.testData.get("dataArchival"));
		clickAllActionButton = new DataArchivalConfiguration();
	}

	/**
	 * This method will be executed just before any function/method with @Test
	 * annotation starts.
	 */
	@BeforeMethod
	public void beforeMethod() {
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
	}

	@Test(priority = 1)
	public void navigateToCorrelationBuilderLandingPage() {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.navigateToArchivalLandingPage(),
				"Adding details to Archive data unsuccessful");
	}

	/**
	 * Assert true if creating archive data is successful else false
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 2)
	public void addNewArchiveData() throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.addArchiveData(), "archive data created");
	}

	/**
	 * Assert true if creating archive data with existing archive name is
	 * unsuccessful else false
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 3)
	public void addSameArchiveData() throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.addSameArchiveData(), "archive data created with existing archive name");
	}

	/**
	 * Assert true if reset and redirect functionalities are successful else false
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 4)
	public void checkResetAndRedirectFunctionality() {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.resetAndRedirectFunctionality(), "Reset and redirect functionality successful");
	}

	/**
	 * This method will be executed just after any function/method with @Test
	 * annotation ends.
	 */
	@AfterMethod
	public void afterMethod() {
		log.info(line);
	}

	/**
	 * After running all the test cases close the browser
	 */
	@AfterTest
	public void tearDown() {
		driver.quit();
	}
}
