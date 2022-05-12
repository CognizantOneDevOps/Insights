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

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

/**
 * @author NivethethaS
 * 
 *         Class contains the business logic for bulk upload module test cases
 *
 */
public class BulkUploadConfiguration extends BulkUploadObjectRepository {

	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

	private static final Logger log = LogManager.getLogger(BulkUploadConfiguration.class);

	public BulkUploadConfiguration() {
		PageFactory.initElements(driver, this);
	}

	String toolName = LoginAndSelectModule.testData.get("toolname");
	String timeField = LoginAndSelectModule.testData.get("timefield");
	String gitFile = LoginAndSelectModule.testData.get("correctGITfile");

	/**
	 * checks whether landing page is displayed or not
	 * 
	 * @return true if landing page is displayed o/w false
	 */
	public boolean navigateToBulkUploadLandingPage() {
		log.info("Bulk upload Landing page displayed : {}", landingPage.isDisplayed());
		return landingPage.isDisplayed();
	}

	/**
	 * Uploads file larger than 2 MB and checks whether error message is displayed
	 * or not
	 * 
	 * @return true if error message is displayed o/w false
	 * @throws InterruptedException
	 */
	public boolean addincorrectfileSize() throws InterruptedException {
		selectData(selectTool1, toolName, timeField1, timeField, file1,
				LoginAndSelectModule.testData.get("largefileSize"));
		saveButton.click();
		try {
			if (failureStatus.isDisplayed()) {
				log.info("File Size greater than 2 MB - error message is displayed");
				resetButton.click();
				return true;
			}
		} catch (Exception e) {
			resetButton.click();
			log.info("something went wrong- file size error message is not displayed {}", e.getMessage());
			return false;
		}
		return false;
	}

	/**
	 * Uploads a non csv file and checks whether error message is displayed or not
	 * 
	 * @return true if error message is displayed o/w false
	 * @throws InterruptedException
	 */
	public boolean addincorrectfileType() throws InterruptedException {
		selectData(selectTool1, toolName, timeField1, timeField, file1,
				LoginAndSelectModule.testData.get("incorrectfiletype"));
		saveButton.click();
		try {
			if (failureStatus.isDisplayed()) {
				log.info("Incorrect file format - error message is displayed");
				resetButton.click();
				return true;
			}
		} catch (Exception e) {
			resetButton.click();
			log.info("something went wrong- file format error message is not displayed {}", e.getMessage());
			return false;
		}
		return false;
	}

	/**
	 * Uploads file with null epoch time and checks whether error message is
	 * displayed or not
	 * 
	 * @return true if error message is displayed o/w false
	 * @throws InterruptedException
	 */
	public boolean uploadDataWithNullEpochTime() throws InterruptedException {
		selectData(selectTool1, toolName, timeField1, timeField, file1,
				LoginAndSelectModule.testData.get("fileWithNullEpochTime"));
		saveButton.click();
		try {
			wait.until(ExpectedConditions.elementToBeClickable(yesButton));
			if (uploadMessage.isDisplayed()) {
				yesButton.click();
				wait.until(ExpectedConditions.elementToBeClickable(okButton));
				okButton.click();
				wait.until(ExpectedConditions.visibilityOf(failureStatus));
				if (failureStatus.isDisplayed()) {
					log.info("null value in column - error message is displayed");
					resetButton.click();
					return true;
				}
			}
		} catch (Exception e) {
			resetButton.click();
			log.info("something went wrong- null value error message is not displayed {}", e.getMessage());
			return false;
		}
		return false;
	}

	/**
	 * Uploads file time format field and check whether success message is displayed
	 * 
	 * @return true if success message is displayed o/w false
	 * @throws InterruptedException
	 */
	public boolean uploadDataWithTimeZoneFormat() throws InterruptedException {
		timeFormatfield.sendKeys(LoginAndSelectModule.testData.get("correctTimezoneFormat"));
		selectData(selectTool1, toolName, timeField1, timeField, file1, gitFile);
		saveButton.click();
		wait.until(ExpectedConditions.elementToBeClickable(yesButton));
		try {
			if (uploadMessage.isDisplayed()) {
				yesButton.click();
				wait.until(ExpectedConditions.elementToBeClickable(okButton));
				if (successMessage.isDisplayed()) {
					okButton.click();
					resetButton.click();
					log.info("Uploaded one file");
					return true;
				}
			}
		} catch (Exception e) {
			resetButton.click();
			log.info("something went wrong {}", e.getMessage());
			return false;
		}
		return false;
	}

	/**
	 * Uploads multiple files at same time ans checks whether success message and
	 * error message are displayed for each file
	 * 
	 * @return true if correct success & error message are displayed for each file
	 *         upload o/w false
	 * @throws InterruptedException
	 */
	public boolean uploadMultipleFiles() throws InterruptedException {
		selectData(selectTool1, toolName, timeField1, timeField, file1,
				LoginAndSelectModule.testData.get("fileWithNumericValues"));
		selectData(selectTool2, toolName, timeField2, timeField, file2, gitFile);
		selectData(selectTool3, toolName, timeField3, LoginAndSelectModule.testData.get("incorrecttimefield"), file3,
				gitFile);
		selectData(selectTool4, toolName, timeField4, LoginAndSelectModule.testData.get("timefieldforNumericData"),
				file4, LoginAndSelectModule.testData.get("fileWithNumericValues"));
		saveButton.click();
		wait.until(ExpectedConditions.elementToBeClickable(yesButton));
		yesButton.click();
		wait.until(ExpectedConditions.elementToBeClickable(okButton));
		okButton.click();
		if (successInRows(successTickrow4, successTickrow2) && incorrectTimefieldError(failureStatusRow1)
				&& incorrectTimefieldError(failureStatusRow3)) {
			resetButton.click();
			return true;
		}
		return false;
	}

	/**
	 * Checks for Insights Time Field not present error for the particular row
	 * 
	 * @param incorrectTimefieldRowNumber - error message with row number xpath
	 * @return true if the error message is displayed o/w false
	 */
	private boolean incorrectTimefieldError(WebElement incorrectTimefieldRowNumber) {
		try {
			if (incorrectTimefieldRowNumber.isDisplayed()) {
				log.info(
						"Insights Time Field not present- error is displayed at particular row while uploading multiple files");
				return true;
			}
		} catch (Exception e) {
			log.info("error message not displayed {}", e.getMessage());
			return false;
		}
		return false;
	}

	/**
	 * Checks for success tick in particular row 
	 * 
	 * @param successTickInrow4 - success tick in row 4
	 * @param successTickInrow2 - success tick in row 2
	 * @return true if success tick is displayed in row 2 & 4 o/w false
	 */
	private boolean successInRows(WebElement successTickInrow4, WebElement successTickInrow2) {
		try {
			if (successTickInrow4.isDisplayed() && successTickInrow2.isDisplayed()) {
				log.info("success tick is displayed in row 2 & 4");
				return true;
			}
		} catch (Exception e) {
			log.info("success tick is not displayed {}", e.getMessage());
			return false;
		}
		return false;
	}

	/**
	 * Input the fields - Tool,InsightsTimeField and choose file field
	 * 
	 * @param selectTool - tool field with row number xpath
	 * @param toolname - input for selectTool
	 * @param timeField - time field with row number xpath
	 * @param timeFieldname - input for timeField
	 * @param file - choose file field with row number xpath
	 * @param filepath - input for choose file
	 * @throws InterruptedException
	 */
	private void selectData(WebElement selectTool, String toolname, WebElement timeField, String timeFieldname,
			WebElement file, String filepath) throws InterruptedException {
		Thread.sleep(1000);
		selectTool.click();
		for (WebElement toolValue : toolnameList) {
			if ((toolValue.getText()).equals(toolname)) {
				wait.until(ExpectedConditions.elementToBeClickable(toolValue));
				toolValue.click();
				break;
			}
		}
		timeField.sendKeys(timeFieldname);
		file.sendKeys(filepath);
	}
}
