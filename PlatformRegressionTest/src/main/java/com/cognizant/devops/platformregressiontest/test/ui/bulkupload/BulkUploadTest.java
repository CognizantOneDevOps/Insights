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
package com.cognizant.devops.platformregressiontest.test.ui.bulkupload;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

/**
 * @author NivethethaS
 * 
 *         Class contains the test cases for bulk upload module
 *
 */
public class BulkUploadTest extends LoginAndSelectModule {

	private static final Logger log = LogManager.getLogger(BulkUploadTest.class);

	BulkUploadConfiguration clickAllActionButton;

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
		getData(ConfigOptionsTest.BULKUPLOAD_DIR + File.separator + ConfigOptionsTest.BULKUPLOAD_JSON_FILE);
		selectModuleUnderConfiguration(LoginAndSelectModule.testData.get("bulkupload"));
		clickAllActionButton = new BulkUploadConfiguration();
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
	 */
	@Test(priority = 1)
	public void navigateToBulkUploadLandingPage() {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.navigateToBulkUploadLandingPage(),
				"Bulk upload Landing page is displayed");
	}

	/**
	 * Assert true if error message for large file size is displayed else false
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 2)
	public void addLargeSizefile() throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.addincorrectfileSize(),
				"Error message for large file size is displayed");
	}

	/**
	 * Assert true if error message for non csv file type is displayed else false
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 3)
	public void addincorrectfileType() throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.addincorrectfileType(),
				"error message for non csv file type is displayed");
	}

	/**
	 * Assert true if error message null values in time field is displayed else
	 * false
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 4)
	public void uploadDataWithNullEpochTimesInTimeField() throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.uploadDataWithNullEpochTime(),
				"error message null values in time field is displayed");
	}

	/**
	 * Assert true if file is uploaded and success message is displayed else false
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 5)
	public void uploadDataWithCorrectTimeZoneFormat() throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.uploadDataWithTimeZoneFormat(),
				"Upload file with time format field successful");
	}

	/**
	 * Assert true if success and error message are displayed for respective files
	 * when adding multiple files else false
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 6)
	public void uploadMultipleFilesWithOneIncorrectData() throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.uploadMultipleFiles(),
				"success and error message are displayed for respective files");
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
