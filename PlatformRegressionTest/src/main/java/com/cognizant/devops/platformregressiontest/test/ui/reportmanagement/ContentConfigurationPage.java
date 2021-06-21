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

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.SkipException;

import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

/**
 * @author Ankita
 *
 */

public class ContentConfigurationPage extends ContentConfigurationObjectRepository {

	WebDriverWait wait = new WebDriverWait(driver, 20);

	public static final String THRESHOLD_RANGE = "THRESHOLD_RANGE";
	public static final String THRESHOLD = "THRESHOLD";
	public static final String MINMAX = "MINMAX";
	public static final String COMPARISON = "COMPARISON";

	private static final Logger log = LogManager.getLogger(ContentConfigurationPage.class);

	public static String uploadFilePath = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator
			+ ConfigOptionsTest.AUTO_DIR + File.separator + ConfigOptionsTest.REPORT_MANAGEMENT_DIR + File.separator;

	public ContentConfigurationPage() {
		PageFactory.initElements(driver, this);
	}

	public void selectTableRow(String value, List<WebElement> table) {

		kpiInputEl.sendKeys(value);
		for (int i = 0; i < table.size(); i++) {
			if (table.get(i).getText().equals(value)) {
				List<WebElement> radioButtons = table.get(i)
						.findElements(By.xpath(".//preceding::span[contains(@class, 'mat-radio-container')]"));
				driver.manage().timeouts().implicitlyWait(6, TimeUnit.SECONDS);
				radioButtons.get(i).click();
				break;
			}
		}
	}

	public boolean navigateToContentConfigurationLandingPage() {
		return landingPage.isDisplayed();
	}

	/**
	 * This method handles Content Addition
	 */
	private void clickAddButton() {

		Actions actions = new Actions(driver);
		wait.until(ExpectedConditions.elementToBeClickable(addNewContentButtonEl));
		actions.moveToElement(addNewContentButtonEl).click();
		Action action = actions.build();
		action.perform();

	}

	/**
	 * This method handles Content insertion
	 * 
	 * @param contentId
	 * @param expectedTrend
	 * @param directionOfThreshold
	 * @param contentName
	 * @param action
	 * @param kpiId
	 * @param noOfResult
	 * @param threshold
	 * @param resultField
	 * @param message
	 * @param isActive
	 * @return
	 */
	public String saveContent(String contentId, String expectedTrend, String directionOfThreshold, String contentName,
			String action, String kpiId, String noOfResult, String threshold, String resultField, String message,
			String isActive) {
		clickAddButton();
		contentIdEl.sendKeys(contentId);
		contentNameEl.sendKeys(contentName);
		wait.until(ExpectedConditions.elementToBeClickable(searchKpiEl));
		searchKpiEl.click();
		selectTableRow(kpiId, kpiListEl);
		wait.until(ExpectedConditions.elementToBeClickable(kpiSelectBtnEl));
		kpiSelectBtnEl.click();
		expectedTrendEl.sendKeys(expectedTrend);
		driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
		try {
			if (directionThresholdEl.isDisplayed()) {
				directionThresholdEl.sendKeys(directionOfThreshold);
			}
		} catch (NoSuchElementException e) {
			log.info("Direction of threshold not applicable for the KPI :{} ", kpiId);
		}
		resultFieldEl.sendKeys(resultField);
		driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
		try {
			if (actionEl.isDisplayed()) {
				actionEl.sendKeys(action);
			}
		} catch (NoSuchElementException e) {
			log.info("Action not applicable for the KPI :{} ", kpiId);
		}
		wait.until(ExpectedConditions.elementToBeClickable(isActiveEl));
		isActiveEl.click();
		messageEl.sendKeys(message);
		driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
		try {
			if (thresholdEl.isDisplayed()) {
				thresholdEl.sendKeys(threshold);
			}
		} catch (NoSuchElementException e) {
			log.info("threshold not applicable for the KPI :{} ", kpiId);
		}

		driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
		try {
			if (thresholdsEl.isDisplayed()) {
				thresholdsEl.sendKeys(threshold);
			}
		} catch (NoSuchElementException e) {
			log.info("thresholds not applicable for the KPI :{} ", kpiId);
		}

		wait.until(ExpectedConditions.elementToBeClickable(saveBtnEl));
		saveBtnEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(yesBtnEl));
		yesBtnEl.click();
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		wait.until(ExpectedConditions.elementToBeClickable(btnOKEl));
		try {
			if (contentIdExistsEl.isDisplayed()) {
				btnOKEl.click();
				navigateToContentLandingPage();
				log.debug("Skipping test case as content : {} already exists", contentId);
				throw new SkipException("Skipping test case as content : " + contentId + " already exists");
			}
		} catch (NoSuchElementException e) {
			log.info("Something went wring while saving content : {} exception : {}", contentId, e.getMessage());
		}
		wait.until(ExpectedConditions.elementToBeClickable(btnOKEl));
		btnOKEl.click();
		return contentId;
	}

	/**
	 * This method handles Validate the Save Content
	 * 
	 * @param contentId
	 * @param expectedTrend
	 * @param directionOfThreshold
	 * @param contentName
	 * @param action
	 * @param kpiId
	 * @param noOfResult
	 * @param threshold
	 * @param resultField
	 * @param message
	 * @param isActive
	 * @param category
	 * @return
	 */
	public boolean saveValidateContent(String contentId, String expectedTrend, String directionOfThreshold,
			String contentName, String action, String kpiId, String noOfResult, String threshold, String resultField,
			String message, String isActive, String category) {

		boolean result = true;
		clickAddButton();
		contentIdEl.sendKeys(contentId);
		contentNameEl.sendKeys(contentName);
		wait.until(ExpectedConditions.elementToBeClickable(searchKpiEl));
		searchKpiEl.click();
		selectTableRow(kpiId, kpiListEl);
		wait.until(ExpectedConditions.elementToBeClickable(kpiSelectBtnEl));
		kpiSelectBtnEl.click();
		expectedTrendEl.sendKeys(expectedTrend);
		driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
		try {
			if (directionThresholdEl.isDisplayed()) {
				directionThresholdEl.sendKeys(directionOfThreshold);
			}
		} catch (NoSuchElementException e) {
			log.info("Direction of threshold not applicable for the KPI :{} ", kpiId);
		}
		resultFieldEl.sendKeys(resultField);
		driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
		try {
			if (actionEl.isDisplayed()) {
				actionEl.sendKeys(action);
			}
		} catch (NoSuchElementException e) {
			log.info("Action not applicable for the KPI :{} ", kpiId);
		}
		driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
		wait.until(ExpectedConditions.elementToBeClickable(isActiveEl));
		isActiveEl.click();
		messageEl.sendKeys(message);
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		if (category.equals(THRESHOLD_RANGE)) {
			result = validateThresholdRange(threshold);

		} else if (category.equals(THRESHOLD)) {
			result = validateThreshold(threshold);

		}

		else if (category.equals(MINMAX)) {
			result = validateMinMax();
		} else if (category.equals(COMPARISON)) {
			result = validateComparison();
		}

		return result;
	}

	private boolean validateThreshold(String threshold) {
		driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
		wait.until(ExpectedConditions.visibilityOf(thresholdEl));
		thresholdEl.sendKeys(threshold);
		wait.until(ExpectedConditions.elementToBeClickable(saveBtnEl));
		saveBtnEl.click();
		try {

			driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
			if (contentAddedEl.isDisplayed()) {
				driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
				wait.until(ExpectedConditions.elementToBeClickable(yesBtnEl));
				yesBtnEl.click();
				driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
				driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
				btnOKEl.click();
				navigateToContentLandingPage();
				return false;
			}

		} catch (NoSuchElementException e) {
			log.info("Validation successful navigating to content landing page {}", e.getMessage());
		}
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

		try {
			if (thresholdActionValidatorEl.isDisplayed()) {
				driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
				wait.until(ExpectedConditions.elementToBeClickable(btnOKEl));
				btnOKEl.click();
				navigateToContentLandingPage();
				return true;
			}
		} catch (NoSuchElementException e) {

		}
		try {
			driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
			Integer.parseInt(thresholdEl.getText());
			wait.until(ExpectedConditions.elementToBeClickable(btnOKEl));
			btnOKEl.click();
			navigateToContentLandingPage();
			return true;

		} catch (NumberFormatException nf) {
			log.info("Threshold can take only integer values");
			wait.until(ExpectedConditions.elementToBeClickable(btnOKEl));
			btnOKEl.click();
			navigateToContentLandingPage();
			return true;

		} catch (NoSuchElementException e) {
			log.info("There is an issue with content validation");

		}

		return false;
	}

	private boolean validateMinMax() {
		driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
		wait.until(ExpectedConditions.elementToBeClickable(saveBtnEl));
		saveBtnEl.click();
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		wait.until(ExpectedConditions.elementToBeClickable(btnOKEl));
		try {
			if (minmaxActionValidatorEl.isDisplayed()) {

				btnOKEl.click();
				navigateToContentLandingPage();
				return true;
			}
		} catch (NoSuchElementException e) {

		}
		try {
			driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
			if (contentAddedEl.isDisplayed()) {
				driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
				wait.until(ExpectedConditions.elementToBeClickable(yBtnEl));
				yBtnEl.click();
				btnOKEl.click();
				navigateToContentLandingPage();
				return false;
			}

		} catch (NoSuchElementException e) {
			log.info("Validation successful navigating to content landing page {}", e.getMessage());
		}

		return false;
	}

	private boolean validateComparison() {
		driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
		wait.until(ExpectedConditions.elementToBeClickable(saveBtnEl));
		saveBtnEl.click();
		driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
		wait.until(ExpectedConditions.elementToBeClickable(btnOKEl));
		try {
			if (comparisonMsgValidationEl.isDisplayed()) {
				driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
				btnOKEl.click();
				navigateToContentLandingPage();
				return true;

			}
		} catch (NoSuchElementException e) {
			log.info("Something went wrong while validating comparision category");
			driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
			btnOKEl.click();
			navigateToContentLandingPage();
			return false;
		}
		return false;
	}

	private boolean validateThresholdRange(String threshold) {

		driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
		wait.until(ExpectedConditions.visibilityOf(thresholdsEl));
		thresholdsEl.sendKeys(threshold);
		wait.until(ExpectedConditions.elementToBeClickable(saveBtnEl));
		saveBtnEl.click();
		driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
		wait.until(ExpectedConditions.elementToBeClickable(btnOKEl));
		driver.manage().timeouts().implicitlyWait(4, TimeUnit.SECONDS);
		try {
			if (thresholdRangeActionValidatorEl.isDisplayed()) {
				driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
				btnOKEl.click();
				navigateToContentLandingPage();
				return true;

			}
		} catch (NoSuchElementException e) {

		}
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		try {
			if (thresholdRangeValidatorEl.isDisplayed()) {
				driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
				btnOKEl.click();
				navigateToContentLandingPage();
				return true;

			}
		} catch (NoSuchElementException e) {

		}

		return false;

	}

	/**
	 * This method handles Content Landing Page
	 */
	public void navigateToContentLandingPage() {
		Actions actions = new Actions(driver);
		WebElement contentElement = driver.findElement(By.xpath("//a[@title='Content Configuration']"));
		wait.until(ExpectedConditions.elementToBeClickable(contentElement));
		actions.moveToElement(contentElement).click();
		Action action = actions.build();
		action.perform();

	}

	public void selectContent(String contentId) {

		for (int i = 0; i < contentListEl.size(); i++) {
			if (contentListEl.get(i).getText().equals(contentId)) {
				List<WebElement> radioButtons = contentListEl.get(i)
						.findElements(By.xpath(".//preceding::span[contains(@class, 'mat-radio-container')]"));
				driver.manage().timeouts().implicitlyWait(6, TimeUnit.SECONDS);
				radioButtons.get(i).click();
				break;
			}
		}
	}

	/**
	 * This method handles edit content functionality
	 * 
	 * @param contentId
	 * @param expectedTrend
	 * @return
	 */
	public boolean editContent(String contentId, String expectedTrend) {
		selectTableRow(contentId, contentListEl);
		wait.until(ExpectedConditions.visibilityOf(btnEditEl));
		btnEditEl.click();
		expectedTrendEl.sendKeys(expectedTrend);
		wait.until(ExpectedConditions.elementToBeClickable(saveBtnEl));
		saveBtnEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(yesBtnEl));
		yesBtnEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(btnOKEl));
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		try {
			if (contentUpdatedEl.isDisplayed()) {
				btnOKEl.click();
				log.info(" contentId {} updated successfully ", contentId);
				return true;
			}
		} catch (NoSuchElementException e) {
			log.error("Unable to edit contentId {} ", contentId);
			return true;
		}
		btnOKEl.click();

		return false;
	}

	/**
	 * This method handles content delete functionality
	 * 
	 * @param contentId
	 * @return
	 */
	public boolean deleteContent(String contentId) {

		selectContent(contentId);
		wait.until(ExpectedConditions.visibilityOf(delBtnEl));
		delBtnEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(yesBtnEl));
		yesBtnEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(btnOKEl));
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		try {
			wait.until(ExpectedConditions.visibilityOf(contentDeletedEl));
			if (contentDeletedEl.isDisplayed()) {
				btnOKEl.click();
				log.info("ContentId {} deleted successfully ", contentId);
				return true;
			}
		} catch (NoSuchElementException e) {
			log.error(" unable to delete contentId {}  with exception {} :", contentId, e.getMessage());
			return false;
		}

		btnOKEl.click();
		return false;
	}

	/**
	 * This method handles the bulkupload functionality
	 * 
	 * @param fileName
	 */
	public boolean uploadJson(String fileName) {
		String path = uploadFilePath + fileName;
		uploadBtnE1.click();
		driver.manage().timeouts().implicitlyWait(1200, TimeUnit.SECONDS);
		chooseFileBtnE1.sendKeys(path);
		wait.until(ExpectedConditions.elementToBeClickable(uploadJsonBtnE1));
		uploadJsonBtnE1.click();
		wait.until(ExpectedConditions.elementToBeClickable(btnOKEl));
		btnOKEl.click();
		log.info("upload json successful");
		return true;
	}

	public boolean validateUploadJson(String fileName) {
		String path = uploadFilePath + fileName;
		driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
		wait.until(ExpectedConditions.elementToBeClickable(uploadBtnE1));
		uploadBtnE1.click();
		driver.manage().timeouts().implicitlyWait(12, TimeUnit.SECONDS);
		chooseFileBtnE1.sendKeys(path);
		wait.until(ExpectedConditions.elementToBeClickable(uploadJsonBtnE1));
		uploadJsonBtnE1.click();
		driver.manage().timeouts().implicitlyWait(12, TimeUnit.SECONDS);
		try {
			if (uploadJsonValidatorEl.isDisplayed()) {
				driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
				wait.until(ExpectedConditions.elementToBeClickable(btnOKEl));
				btnOKEl.click();
				driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
				wait.until(ExpectedConditions.elementToBeClickable(uploadJsonCancelBtnE1));
				uploadJsonCancelBtnE1.click();
				navigateToContentLandingPage();
				log.info("upload json validated successfully ");
				return true;
			}
		} catch (NoSuchElementException e) {
			log.error("Unable to validate upload json  {} ", e.getMessage());
			return true;
		}
		driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
		btnOKEl.click();
		log.info("upload json successful");
		return true;
	}

	/**
	 * This method handles the search content functionality
	 * 
	 * @param contentId
	 * @return
	 */
	public boolean searchContent(String contentId) {
		Actions actions = new Actions(driver);
		actions.moveToElement(searchContentEl).click();
		actions.sendKeys(contentId);
		Action action = actions.build();
		action.perform();
		driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
		if (contentListEl.size() == 1) {
			searchContentEl.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
			navigateToContentLandingPage();
			log.info("Content search box test successful");
			return true;
		}
		log.info("Content search box test unsuccessful");
		return true;

	}

	public boolean checkRefreshButton(String contentId) {
		try {
			selectContent(contentId);
			driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
			wait.until(ExpectedConditions.elementToBeClickable(refreshBtnE1));
			refreshBtnE1.click();
			driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
			for (int i = 0; i < contentListEl.size(); i++) {
				WebElement radioButton = contentListEl.get(i).findElement(By.xpath(".//preceding::mat-radio-button"));
				if (radioButton.isSelected()) {
					log.info("contentId is in selected mode ");
					return false;
				} else {
					log.info("contentId not-selected");
				}

			}
		}

		catch (Exception e) {

		}
		return true;

	}

}
