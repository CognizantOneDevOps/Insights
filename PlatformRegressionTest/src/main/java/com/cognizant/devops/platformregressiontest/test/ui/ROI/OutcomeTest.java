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
package com.cognizant.devops.platformregressiontest.test.ui.ROI;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;
import com.cognizant.devops.platformregressiontest.test.ui.testdata.OutcomeConfigDataProvider;
import com.cognizant.devops.platformregressiontest.test.ui.testdatamodel.OutcomeConfigDataModel;

public class OutcomeTest extends LoginAndSelectModule {

	private static final Logger log = LogManager.getLogger(OutcomeTest.class);
	OutcomeConfiguration outcome;

	String line = "============================================================================================================================================================";

	/**
	 * This method will be run before any test method belonging to the classes
	 * inside the <test> tag is run.
	 * 
	 */
	
	@BeforeTest
	public void setUp() {
		initialization();
		getData(ConfigOptionsTest.ROI_DIR + File.separator + ConfigOptionsTest.OUTCOME_TEST_DATA);
		selectROIModule("OutCome Config");
		outcome = new OutcomeConfiguration();
	}
	
	/**
	 * This method will be executed just before any function/method with @Test
	 * annotation starts.
	 * @throws InterruptedException 
	 */
	@BeforeMethod
	public void beforeMethod() throws InterruptedException {
		Thread.sleep(4000);
	}
	
	/**
	 * Assert true if landing page is displayed else false
	 * 
	 */
	
	@Test(priority = 1)
	public void navigateToOutcomeConfig() {
		log.info(line);
		Assert.assertTrue(outcome.navigateToOutcomeConfig(), "Landing page is displayed");
	}

	/**
	 * Assert true if the outcome is configured successfully
	 * 
	 * @param data
	 */
	
	@Test(priority = 2, enabled = true, dataProvider = "configureOutcomeWithoutReqParams", dataProviderClass = OutcomeConfigDataProvider.class)
	public void configureOutcomeWithoutReqParams(OutcomeConfigDataModel data) {
		log.info(line);
		Assert.assertTrue(outcome.configureOutcomeWithoutReqParams(data), "Created report with mailing details.");
	}
	
	/**
	 * Assert true if the outcome with the given name is already available
	 * 
	 * @param data
	 */
	
	@Test(priority = 3, enabled = true, dataProvider = "configureOutcomeWithoutReqParams", dataProviderClass = OutcomeConfigDataProvider.class)
	public void validateDuplicateOutcome(OutcomeConfigDataModel data) {
		log.info(line);
		Assert.assertTrue(outcome.validateDuplicateOutcome(data), "Outcome name already exists in the outcome list.");
	}
	
	
	
	/**
	 * Assert true if the outcome is configured successfully
	 * 
	 * @param data
	 */
	
	@Test(priority = 4, enabled = true, dataProvider = "configureOutcomeWithReqParams", dataProviderClass = OutcomeConfigDataProvider.class)
	public void configureOutcomeWithReqParams(OutcomeConfigDataModel data) {
		log.info(line);
		Assert.assertTrue(outcome.configureOutcomeWithReqParams(data), "Created report with mailing details.");
	}
	
	/**
	 * Assert true if the outcome is edited successfully successfully
	 * 
	 * @param data
	 */
	
	@Test(priority = 5, enabled = true, dataProvider = "editOutcome", dataProviderClass = OutcomeConfigDataProvider.class)
	public void editOutcome(OutcomeConfigDataModel data) {
		log.info(line);
		Assert.assertTrue(outcome.editOutcome(data),
				"Successfully edited the outcome.");
	}
	
	/**
	 * Assert true if refresh functionality working successfully
	 * 
	 * @param data
	 */
	
	@Test(priority = 6, enabled = true, dataProvider = "configureOutcomeWithoutReqParams", dataProviderClass = OutcomeConfigDataProvider.class)
	public void checkRefresh(OutcomeConfigDataModel data) {
		log.info(line);
		Assert.assertTrue(outcome.checkRefresh(data),
				"Refresh to landing page functionalities working");
	}
	
	/**
	 * 
	 * Assert true if delete report functionality working successfully
	 * 
	 * @param data
	 */
	
	@Test(priority = 7, enabled = true, dataProvider = "configureOutcomeWithReqParams", dataProviderClass = OutcomeConfigDataProvider.class)
	public void deleteOutcome(OutcomeConfigDataModel data) {
		log.info(line);
		Assert.assertTrue(outcome.deleteOutcome(data), "Outcome deleted successfully.");
	}
	
	/**
	 * 
	 * Assert true if the status function is verified successfully
	 * 
	 * @param data
	 */
	
	@Test(priority = 8, enabled = true, dataProvider = "configureOutcomeWithoutReqParams", dataProviderClass = OutcomeConfigDataProvider.class)
	public void checkStatus(OutcomeConfigDataModel data) {
		log.info(line);
		Assert.assertTrue(outcome.checkStatus(data), "Outcome status verified successfully.");
	}

}
