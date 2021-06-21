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
package com.cognizant.devops.platformregressiontest.test.ui.reportmanagement;

import java.util.concurrent.TimeUnit;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;
import com.cognizant.devops.platformregressiontest.test.ui.testdata.ReportManagementDataProvider;

public class ContentConfigurationTest extends LoginAndSelectModule {

	ContentConfigurationPage contentConfigurationPage;

	@BeforeTest
	public void setUp() {
		initialization();
		selectMenuOption("Content Configuration");
		contentConfigurationPage = new ContentConfigurationPage();
	}

	@BeforeMethod
	public void beforeEachTestCase() {
		driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
	}

	/**
	 * This method tests content creation screen
	 * 
	 * @param contentId
	 * @param contentName
	 * @param expectedTrend
	 * @param directionOfThreshold
	 * @param kpiId
	 * @param noOfResult
	 * @param threshold
	 * @param resultField
	 * @param action
	 * @param message
	 * @param isActive
	 */
	@Test(priority = 1, dataProvider = "createContentdataprovider", dataProviderClass = ReportManagementDataProvider.class, enabled = true)
	public void saveContent(String contentId, String contentName, String expectedTrend, String directionOfThreshold,
			String kpiId, String noOfResult, String threshold, String resultField, String action, String message,
			String isActive) {

		Assert.assertEquals(contentId,
				contentConfigurationPage.saveContent(contentId, expectedTrend, directionOfThreshold, contentName,
						action, kpiId, noOfResult, threshold, resultField, message, isActive));

	}

	@Test(priority = 2, dataProvider = "createContentvalidatedataprovider", dataProviderClass = ReportManagementDataProvider.class, enabled = true)
	public void saveValaidateContent(String contentId, String contentName, String expectedTrend,
			String directionOfThreshold, String kpiId, String noOfResult, String threshold, String resultField,
			String action, String message, String isActive, String category) {

		Assert.assertEquals(
				contentConfigurationPage.saveValidateContent(contentId, expectedTrend, directionOfThreshold,
						contentName, action, kpiId, noOfResult, threshold, resultField, message, isActive, category),
				true);

	}

	/**
	 * This method tests content edit screen
	 * 
	 * @param contentId
	 * @param expectedTrend
	 */
	@Test(priority = 3, dataProvider = "editContentdataprovider", dataProviderClass = ReportManagementDataProvider.class, enabled = true)
	public void editContent(String contentId, String expectedTrend) {

		Assert.assertEquals(contentConfigurationPage.editContent(contentId, expectedTrend), true);

	}

	/**
	 * This method take json file and tests the bulkupload functionality
	 * 
	 * @param fileName
	 */
	@Test(priority = 4, dataProvider = "uploadJsonContentdataprovider", dataProviderClass = ReportManagementDataProvider.class, enabled = true)
	public void uploadJsonTest(String validateFile, String invalidFile) {

		Assert.assertEquals(contentConfigurationPage.uploadJson(validateFile), true);
	}

	@Test(priority = 5, dataProvider = "uploadJsonContentdataprovider", dataProviderClass = ReportManagementDataProvider.class, enabled = true)
	public void validateUploadJsonTest(String validateFile, String invalidFile) {

		Assert.assertEquals(contentConfigurationPage.validateUploadJson(invalidFile), true);
	}

	/**
	 * This method tests content search functionality
	 * 
	 * @param contentId
	 */
	@Test(priority = 6, dataProvider = "searchContentdataprovider", dataProviderClass = ReportManagementDataProvider.class, enabled = true)
	public void searchContent(String contentId) {
		Assert.assertEquals(contentConfigurationPage.searchContent(contentId), true);

	}

	@Test(priority = 7, dataProvider = "searchContentdataprovider", dataProviderClass = ReportManagementDataProvider.class, enabled = true)
	public void checkRefreshButton(String contentId) {
		Assert.assertEquals(contentConfigurationPage.checkRefreshButton(contentId), true);

	}

	/**
	 * This method tests content delete functionality
	 * 
	 * @param contentId
	 */
	@Test(priority = 8, dataProvider = "deleteContentdataprovider", dataProviderClass = ReportManagementDataProvider.class, enabled = true)
	public void deleteContent(String contentId) {

		Assert.assertEquals(contentConfigurationPage.deleteContent(contentId), true);

	}

}