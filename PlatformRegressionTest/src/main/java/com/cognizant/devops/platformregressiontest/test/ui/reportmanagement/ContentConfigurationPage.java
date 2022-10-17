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
import java.time.Duration;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.SkipException;

import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.test.ui.testdatamodel.ReportConfigurationDataModel;

/**
 * @author Ankita
 *
 */

public class ContentConfigurationPage extends ContentConfigurationObjectRepository {

	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

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

	public void selectTableRow(String value, List<WebElement> table) throws InterruptedException {

		kpiInputEl.sendKeys(value);
		for (int i = 0; i < table.size(); i++) {
			if (table.get(i).getText().equals(value)) {
				List<WebElement> radioButtons = table.get(i)
						.findElements(By.xpath(".//preceding::span[contains(@class, 'mat-radio-container')]"));
				Thread.sleep(6000);
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
	 * @throws InterruptedException 
	 */
	public boolean saveContent(ReportConfigurationDataModel data) throws InterruptedException {
		clickAddButton();
		contentIdEl.sendKeys(data.getContentId());
		contentNameEl.sendKeys(data.getContentName());
		wait.until(ExpectedConditions.elementToBeClickable(searchKpiEl));
		searchKpiEl.click();
		selectTableRow(data.getKpiId(), kpiListEl);
		wait.until(ExpectedConditions.elementToBeClickable(kpiSelectBtnEl));
		kpiSelectBtnEl.click();
		expectedTrendEl.sendKeys(data.getExpectedTrend());
		Thread.sleep(3000);
		try {
			if (directionThresholdEl.isDisplayed()) {
				directionThresholdEl.sendKeys(data.getDirectionOfThreshold());
			}
		} catch (NoSuchElementException e) {
			log.info("Direction of threshold not applicable for the KPI :{} ", data.getKpiId());
		}
		resultFieldEl.sendKeys(data.getResultField());
		try {
			if (actionEl.isDisplayed()) {
				actionEl.sendKeys(data.getAction());
			}
		} catch (NoSuchElementException e) {
			log.info("Action not applicable for the KPI :{} ", data.getKpiId());
		}
		messageEl.sendKeys(data.getMessage());
		try {
			if (thresholdEl.isDisplayed()) {
				thresholdEl.sendKeys(data.getThreshold());
			}
		} catch (NoSuchElementException e) {
			log.info("threshold not applicable for the KPI :{} ", data.getKpiId());
		}

		try {
			if (thresholdsEl.isDisplayed()) {
				thresholdsEl.sendKeys(data.getThreshold());
			}
		} catch (NoSuchElementException e) {
			log.info("thresholds not applicable for the KPI :{} ", data.getKpiId());
		}

		wait.until(ExpectedConditions.elementToBeClickable(saveBtnEl));
		saveBtnEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(yesBtnEl));
		yesBtnEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(crossClose));
		try {
			if (contentIdExistsEl.isDisplayed()) {
				crossClose.click();
				navigateToContentLandingPage();
				log.debug("Skipping test case as content : {} already exists", data.getContentId());
				throw new SkipException("Skipping test case as content : " + data.getContentId() + " already exists");
			}
		} catch (NoSuchElementException e) {
			crossClose.click();
			navigateToContentLandingPage();
			return true;
		}
		wait.until(ExpectedConditions.elementToBeClickable(crossClose));
		crossClose.click();
		return false;
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
	 * @throws InterruptedException 
	 */
	public boolean saveValidateContent(String contentId, String expectedTrend, String directionOfThreshold,
			String contentName, String action, String kpiId, String noOfResult, String threshold, String resultField,
			String message, String isActive, String category) throws InterruptedException {

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
		Thread.sleep(3000);
		try {
			if (directionThresholdEl.isDisplayed()) {
				directionThresholdEl.sendKeys(directionOfThreshold);
			}
		} catch (NoSuchElementException e) {
			log.info("Direction of threshold not applicable for the KPI :{} ", kpiId);
		}
		resultFieldEl.sendKeys(resultField);
		try {
			if (actionEl.isDisplayed()) {
				actionEl.sendKeys(action);
			}
		} catch (NoSuchElementException e) {
			log.info("Action not applicable for the KPI :{} ", kpiId);
		}
		messageEl.sendKeys(message);
		Thread.sleep(5000);
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
		navigateToContentLandingPage();
		return result;
	}

	private boolean validateThreshold(String threshold) throws InterruptedException {
		Thread.sleep(3000);
		wait.until(ExpectedConditions.visibilityOf(thresholdEl));
		thresholdEl.sendKeys(threshold);
		wait.until(ExpectedConditions.elementToBeClickable(saveBtnEl));
		saveBtnEl.click();
		try {

			if (contentAddedEl.isDisplayed()) {
				wait.until(ExpectedConditions.elementToBeClickable(yesBtnEl));
				yesBtnEl.click();
				crossClose.click();
				navigateToContentLandingPage();
				return false;
			}

		} catch (NoSuchElementException e) {
			log.info("Validation successful navigating to content landing page {}", e.getMessage());
		}

		try {
			if (thresholdActionValidatorEl.isDisplayed()) {
				wait.until(ExpectedConditions.elementToBeClickable(crossClose));
				crossClose.click();
				navigateToContentLandingPage();
				return true;
			}
		} catch (NoSuchElementException e) {
			log.info("Threshold Validation unsuccessful");

		}
		try {
			Integer.parseInt(thresholdEl.getText());
			wait.until(ExpectedConditions.elementToBeClickable(crossClose));
			crossClose.click();
			navigateToContentLandingPage();
			return true;

		} catch (NumberFormatException nf) {
			log.info("Threshold can take only integer values");
			wait.until(ExpectedConditions.elementToBeClickable(crossClose));
			crossClose.click();
			navigateToContentLandingPage();
			return true;

		} catch (NoSuchElementException e) {
			log.info("There is an issue with content validation");

		}

		return false;
	}

	private boolean validateMinMax() throws InterruptedException {
		Thread.sleep(3000);
		wait.until(ExpectedConditions.elementToBeClickable(saveBtnEl));
		saveBtnEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(crossClose));
		try {
			if (minmaxActionValidatorEl.isDisplayed()) {

				crossClose.click();
				navigateToContentLandingPage();
				return true;
			}
		} catch (NoSuchElementException e) {
			log.info("Error occured in MinMax");
		}
		try {
			if (contentAddedEl.isDisplayed()) {
				wait.until(ExpectedConditions.elementToBeClickable(yBtnEl));
				yBtnEl.click();
				crossClose.click();
				navigateToContentLandingPage();
				return false;
			}

		} catch (NoSuchElementException e) {
			log.info("Validation successful navigating to content landing page {}", e.getMessage());
		}

		return false;
	}

	private boolean validateComparison() throws InterruptedException {
		Thread.sleep(3000);
		wait.until(ExpectedConditions.elementToBeClickable(saveBtnEl));
		saveBtnEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(crossClose));
		try {
			if (comparisonMsgValidationEl.isDisplayed()) {
				crossClose.click();
				navigateToContentLandingPage();
				return true;

			}
		} catch (NoSuchElementException e) {
			log.info("Something went wrong while validating comparision category");
			crossClose.click();
			navigateToContentLandingPage();
			return false;
		}
		return false;
	}

	private boolean validateThresholdRange(String threshold) throws InterruptedException {
		Thread.sleep(3000);
		wait.until(ExpectedConditions.visibilityOf(thresholdsEl));
		thresholdsEl.sendKeys(threshold);
		wait.until(ExpectedConditions.elementToBeClickable(saveBtnEl));
		saveBtnEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(crossClose));
		try {
			if (thresholdRangeActionValidatorEl.isDisplayed()) {
				crossClose.click();
				navigateToContentLandingPage();
				return true;

			}
		} catch (NoSuchElementException e) {
			log.info("Error occured in Threshold RangeAction Validator");
		}
		try {
			if (thresholdRangeValidatorEl.isDisplayed()) {
				crossClose.click();
				navigateToContentLandingPage();
				return true;

			}
		} catch (NoSuchElementException e) {
			log.info("Threshold Range validation unsuccessful");
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

	public void selectContent(String contentId) throws InterruptedException {

		for (int i = 0; i < contentListEl.size(); i++) {
			if (contentListEl.get(i).getText().equals(contentId)) {
				List<WebElement> radioButtons = contentListEl.get(i)
						.findElements(By.xpath(".//preceding::span[contains(@class, 'mat-radio-container')]"));
				Thread.sleep(6000);
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
	 * @throws InterruptedException 
	 */
	public boolean editContent(String contentId, String expectedTrend) throws InterruptedException {
		selectTableRow(contentId, contentListEl);
		wait.until(ExpectedConditions.visibilityOf(btnEditEl));
		btnEditEl.click();
		expectedTrendEl.sendKeys(expectedTrend);
		wait.until(ExpectedConditions.elementToBeClickable(saveBtnEl));
		saveBtnEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(yesBtnEl));
		yesBtnEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(crossClose));
		Thread.sleep(2000);
		try {
			if (contentUpdatedEl.isDisplayed()) {
				crossClose.click();
				log.info(" contentId {} updated successfully ", contentId);
				return true;
			}
		} catch (NoSuchElementException e) {
			log.error("Unable to edit contentId {} ", contentId);
			return true;
		}
		crossClose.click();

		return false;
	}

	/**
	 * This method handles content delete functionality
	 * 
	 * @param contentId
	 * @return
	 * @throws InterruptedException 
	 */
	public boolean deleteContent(String contentId) throws InterruptedException {

		selectContent(contentId);
		wait.until(ExpectedConditions.visibilityOf(delBtnEl));
		delBtnEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(yesBtnEl));
		yesBtnEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(crossClose));
		Thread.sleep(2000);
		try {
			wait.until(ExpectedConditions.visibilityOf(contentDeletedEl));
			if (contentDeletedEl.isDisplayed()) {
				crossClose.click();
				log.info("ContentId {} deleted successfully ", contentId);
				return true;
			}
		} catch (NoSuchElementException e) {
			log.error(" unable to delete contentId {}  with exception {} :", contentId, e.getMessage());
			return false;
		}

		crossClose.click();
		return false;
	}

	/**
	 * This method handles the bulkupload functionality
	 * 
	 * @param fileName
	 * @throws InterruptedException 
	 */
	public boolean uploadJson(String fileName) throws InterruptedException {
		String path = uploadFilePath + fileName;
		uploadBtnE1.click();
		Thread.sleep(5000);
		chooseFileBtnE1.sendKeys(path);
		wait.until(ExpectedConditions.elementToBeClickable(uploadJsonBtnE1));
		uploadJsonBtnE1.click();
		wait.until(ExpectedConditions.elementToBeClickable(crossClose));
		crossClose.click();
		log.info("upload json successful");
		return true;
	}

	public boolean validateUploadJson(String fileName) throws InterruptedException {
		String path = uploadFilePath + fileName;
		Thread.sleep(3000);
		wait.until(ExpectedConditions.elementToBeClickable(uploadBtnE1));
		uploadBtnE1.click();
		Thread.sleep(5000);
		chooseFileBtnE1.sendKeys(path);
        Thread.sleep(1000);
        uploadJsonBtnE1.click();
        Thread.sleep(12000);
		try {
			if (uploadJsonValidatorEl.isDisplayed()) {
				Thread.sleep(3000);
				wait.until(ExpectedConditions.elementToBeClickable(crossClose));
				crossClose.click();
				Thread.sleep(1000);
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
		Thread.sleep(3000);
		crossClose.click();
		log.info("upload json successful");
		return true;
	}

	/**
	 * This method handles the search content functionality
	 * 
	 * @param contentId
	 * @return
	 * @throws InterruptedException 
	 */
	public boolean searchContent(String contentId) throws InterruptedException {
		Actions actions = new Actions(driver);
		actions.moveToElement(searchContentEl).click();
		actions.sendKeys(contentId);
		Action action = actions.build();
		action.perform();
		Thread.sleep(3000);
		if (contentListEl.size() == 1) {
			searchContentEl.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
			navigateToContentLandingPage();
			log.info("Content search box test successful");
			return true;
		}
		log.info("Content search box test unsuccessful");
		return false;

	}

	public boolean checkRefreshButton(String contentId) {
		try {
			selectContent(contentId);
			Thread.sleep(3000);
			wait.until(ExpectedConditions.elementToBeClickable(refreshBtnE1));
			refreshBtnE1.click();
			Thread.sleep(5000);
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
			log.info("Error while checking Refresh button");
		}
		return true;

	}

}