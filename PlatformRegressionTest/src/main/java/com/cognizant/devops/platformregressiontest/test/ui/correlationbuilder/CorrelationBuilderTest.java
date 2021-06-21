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
package com.cognizant.devops.platformregressiontest.test.ui.correlationbuilder;

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
 *         Class contains the test cases for Correlation Builder Module
 *
 */
public class CorrelationBuilderTest extends LoginAndSelectModule {

	private static final Logger log = LogManager.getLogger(CorrelationBuilderTest.class);

	CorrelationBuilderConfiguration clickAllActionButton;
	
	String line = "============================================================================================================================================================";

	/**
	 * This method will be run before any test method belonging to the classes
	 * inside the <test> tag is run.
	 * @throws InterruptedException 
	 */
	@BeforeTest
	public void setUp() throws InterruptedException {
		initialization();
		getData(ConfigOptionsTest.CORRELATION_BUILDER_DIR + File.separator + ConfigOptionsTest.CORRELATION_JSON_FILE);
		selectModuleUnderConfiguration(LoginAndSelectModule.testData.get("correlationBuiler"));
		clickAllActionButton = new CorrelationBuilderConfiguration();
		clickAllActionButton.getAgentWebhookLabels();
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
	 * @throws InterruptedException 
	 */
	@Test(priority = 1)
	public void navigateToCorrelationBuilderLandingPage() throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.navigateToCorrelationBuilderLandingPage(), "Landing page is displayed");
	}

	/**
	 * Assert true if existing correlations present in database is displayed else
	 * false
	 */
	@Test(priority = 2)
	public void checkIfExistingRecordsDisplayed() {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.checkIfExistingRecordsDisplayed(),
				"Existing record in database is displayed on UI");
	}

	/**
	 * Assert true if successfully able to save the correlation with the naming
	 * convention as alphanumeric with underscore only else false
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 3)
	public void correlationNameWithAlphaNumericCharacters() throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.correlationNameWithAlphaNumericCharacters(),
				"All strings containing alphanumeric characters are allowed and string containing special characters not allowed.");
	}

	/**
	 * Assert true if not able to save the correlation with the naming convention as
	 * special characters else false
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 4)
	public void correlationNameWithSpecialCharacters() throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.correlationNameWithSpecialCharacters(),
				"All strings containing alphanumeric characters are allowed");
	}

	/**
	 * Assert true if successfully able to save the correlation and it is being
	 * reflected on UI under List of correlations in database else false
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 5)
	public void createCorrelation() throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.createCorrelation(), "Relation has been created");
	}

	/**
	 * Assert true if not able to save the correlation with the same name
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 6)
	public void createCorrelationWithSameName() throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.createCorrelationWithSameName(), "Relation Name already exists.");
	}

	/**
	 * Assert true if on clicking View Correlation icon user able to see the name of
	 * the correlation, source tool name and destination tool name
	 * @throws InterruptedException 
	 */
	@Test(priority = 7)
	public void viewCorrelation() throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.viewCorrelation(), "View correlation is in working mode.");
	}

	/**
	 * Assert true if enable and disable functionality is working else false
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 8)
	public void disableAndEnableCorrelation() throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.disableAndEnableCorrelation(), "Correlation is in enable mode.");
	}

	/**
	 * Assert true if after deleting correlation, deleted correlation is not present
	 * on UI
	 * @throws InterruptedException 
	 */
	@Test(priority = 9)
	public void deleteCorrelation() throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.deleteCorrelation(), "Correlation has been deleted.");
	}

	/**
	 * Assert true if cancel functionality is working else false
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 10)
	public void cancelCorrelation() throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.cancelCorrelation(), "Cancel functionality is working.");
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
