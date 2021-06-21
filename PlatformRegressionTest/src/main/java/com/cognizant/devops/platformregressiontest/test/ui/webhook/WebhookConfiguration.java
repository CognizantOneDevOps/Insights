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
package com.cognizant.devops.platformregressiontest.test.ui.webhook;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.SkipException;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;
import com.cognizant.devops.platformregressiontest.test.ui.testdata.WebhookDataProvider;

/**
 * @author NivethethaS
 * 
 *         Class contains the business logic for Webhook Configuration module
 *         test cases
 *
 */
public class WebhookConfiguration extends WebhookObjectRepository {

	WebDriverWait wait = new WebDriverWait(driver, 20);

	private static final Logger log = LogManager.getLogger(WebhookConfiguration.class);

	WebhookDataProvider actionButton = new WebhookDataProvider();

	public WebhookConfiguration() {
		PageFactory.initElements(driver, this);
	}

	/**
	 * Creates new Webhook and adds it to the database
	 * 
	 * @return true if new Webhook is created o/w skip
	 * @throws Exception
	 */
	@SuppressWarnings("all")
	public boolean addNewWebHook() throws Exception {
		boolean isTrue = true;
		if (verifyWebhookPresent(LoginAndSelectModule.testData.get("WebHookname"))) {
			log.debug("Webhook name already exists");
			throw new SkipException("Skipping test case as webhook already exists");
		} else {
			addWebhook.click();
			driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			webhookName.sendKeys(LoginAndSelectModule.testData.get("WebHookname"));
			selectTool(LoginAndSelectModule.testData.get("ToolName"));
			selectDataFormat(LoginAndSelectModule.testData.get("DataFromat"));
			String dynamicResponse = actionButton.getDynamicTemplate();
			wait.until(ExpectedConditions.visibilityOf(dynamicTemplate));
			dynamicTemplate.sendKeys(dynamicResponse);
			timeField.sendKeys(LoginAndSelectModule.testData.get("TimeField"));
			timeFormat.sendKeys(LoginAndSelectModule.testData.get("TimeFormat"));
			wait.until(ExpectedConditions.elementToBeClickable(saveButton));
			saveButton.click();
			wait.until(ExpectedConditions.elementToBeClickable(yesButton));
			yesButton.click();
			wait.until(ExpectedConditions.elementToBeClickable(okButton));
			driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
			try {
				if (successMessage.isDisplayed()) {
					wait.until(ExpectedConditions.elementToBeClickable(okButton));
					okButton.click();
					log.info("Add new webhook successful");
					isTrue = true;
				}
			} catch (Exception e) {
				log.info("error while creating webhook");
				isTrue = false;
			}
		}
		return isTrue;
	}

	/**
	 * Selects tool from list of tools available
	 * 
	 * @param toolName
	 */
	public void selectTool(String toolName) {
		wait.until(ExpectedConditions.elementToBeClickable(selectTool));
		selectTool.click();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		for (WebElement toolValue : toolNameList) {
			if ((toolValue.getText()).equals(toolName)) {
				wait.until(ExpectedConditions.elementToBeClickable(toolValue));
				toolValue.click();
				break;
			}
		}
	}

	/**
	 * Selects Data Format from list of Data Formats available
	 * 
	 * @param formatName
	 * @throws Exception
	 */
	public void selectDataFormat(String formatName) throws InterruptedException {
		wait.until(ExpectedConditions.elementToBeClickable(dataFormat));
		dataFormat.click();
		Thread.sleep(1000);
		for (WebElement dataFormatValue : dataFormatList) {
			if ((dataFormatValue.getText()).equals(formatName)) {
				wait.until(ExpectedConditions.elementToBeClickable(dataFormatValue));
				dataFormatValue.click();
				break;
			}
		}
	}

	/**
	 * Checks whether the Webhook is present in the UI or not
	 * 
	 * @param userName
	 * @return true if Webhook present in list of Webhook in database list else
	 *         false
	 */
	public boolean verifyWebhookPresent(String userName) {
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		List<WebElement> rws = webhookDetailsTable.findElements(By.tagName("tr"));
		for (int i = 0; i < rws.size(); i++) {
			List<WebElement> cols = (rws.get(i)).findElements(By.tagName("td"));
			if ((cols.get(1).getText()).equals(userName)) {
				driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
				log.info("{} user name is present.", userName);
				return true;
			}
		}
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		log.info("{} user name is not present.", userName);
		return false;
	}

	/**
	 * Adds already existing webhook name and checks whether error pop-up is
	 * displayed
	 * 
	 * @return true if Webhook is already present o/w skip
	 * @throws Exception
	 */
	public boolean addSameWebHook() throws Exception {
		boolean isTrue = true;
		if (!verifyWebhookPresent(LoginAndSelectModule.testData.get("WebHookname"))) {
			log.debug("Webhook name does not exists");
			throw new SkipException("Skipping test case as webhook doesnot exists");
		} else {
			addWebhook.click();
			driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			webhookName.sendKeys(LoginAndSelectModule.testData.get("WebHookname"));
			selectTool(LoginAndSelectModule.testData.get("ToolName"));
			selectDataFormat(LoginAndSelectModule.testData.get("DataFromat"));
			String dynamicResponse = actionButton.getDynamicTemplate();
			wait.until(ExpectedConditions.visibilityOf(dynamicTemplate));
			dynamicTemplate.sendKeys(dynamicResponse);
			timeField.sendKeys(LoginAndSelectModule.testData.get("TimeField"));
			timeFormat.sendKeys(LoginAndSelectModule.testData.get("TimeFormat"));
			wait.until(ExpectedConditions.elementToBeClickable(saveButton));
			saveButton.click();
			wait.until(ExpectedConditions.elementToBeClickable(yesButton));
			yesButton.click();
			wait.until(ExpectedConditions.elementToBeClickable(okButton));
			driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
			try {
				if (errorMessage.isDisplayed()) {
					wait.until(ExpectedConditions.elementToBeClickable(okButton));
					okButton.click();
					log.info("successfully added webhook");
					isTrue = true;
					wait.until(ExpectedConditions.elementToBeClickable(redirectButton));
					redirectButton.click();
				}
			} catch (Exception e) {
				log.info("error while creating webhook with same name");
				isTrue = false;
			}
		}
		return isTrue;
	}

	/**
	 * Checks whether error pop-up is displayed for incorrect label name, dynamic
	 * template and response template.
	 * 
	 * @return true if all the error messages are popped up o/w false
	 * @throws Exception
	 */
	@SuppressWarnings("all")
	public boolean errorCheck() throws InterruptedException {
		addWebhook.click();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		webhookName.sendKeys(LoginAndSelectModule.testData.get("WebHookname1"));
		selectTool(LoginAndSelectModule.testData.get("ToolName"));
		labelName.clear();
		labelName.sendKeys(LoginAndSelectModule.testData.get("IncorrectLabelName"));
		selectDataFormat(LoginAndSelectModule.testData.get("DataFromat"));
		dynamicTemplate.sendKeys(LoginAndSelectModule.testData.get("incorrectResponse"));
		timeField.sendKeys(LoginAndSelectModule.testData.get("TimeField"));
		timeFormat.sendKeys(LoginAndSelectModule.testData.get("TimeFormat"));
		wait.until(ExpectedConditions.elementToBeClickable(saveButton));
		saveButton.click();
		isErrorDisplayed();
		labelName.clear();
		labelName.sendKeys(LoginAndSelectModule.testData.get("CorrectLabelName"));
		wait.until(ExpectedConditions.elementToBeClickable(saveButton));
		saveButton.click();
		isErrorDisplayed();
		dynamicTemplate.clear();
		responseTemplate.sendKeys(LoginAndSelectModule.testData.get("incorrectResponse"));
		wait.until(ExpectedConditions.elementToBeClickable(saveButton));
		saveButton.click();
		boolean result = isErrorDisplayed();
		wait.until(ExpectedConditions.elementToBeClickable(redirectButton));
		redirectButton.click();
		if (result) {
			log.info(
					"error message is thrown for incorrect label name, non-JSON Response Template and Dynamic Template");
			return true;
		} else {
			log.info(
					"error message is not thrown for incorrect label name, non-JSON Response Template or Dynamic Template");
			return false;
		}
	}

	/**
	 * Checks whether error pop-up is displayed
	 * 
	 * @return true if error pop-up is displayed else false
	 */
	public boolean isErrorDisplayed() {
		boolean isDisp = true;
		wait.until(ExpectedConditions.elementToBeClickable(okButton));
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		try {
			if (errorMessage.isDisplayed()) {
				wait.until(ExpectedConditions.elementToBeClickable(okButton));
				okButton.click();
				log.info("error while creating webhook");
				isDisp = true;
			}
		} catch (Exception e) {
			wait.until(ExpectedConditions.elementToBeClickable(yesButton));
			yesButton.click();
			wait.until(ExpectedConditions.elementToBeClickable(okButton));
			okButton.click();
			log.info("no error while creating webhook");
			isDisp = false;
		}
		return isDisp;
	}

	/**
	 * Edits the webhook with node updation
	 * 
	 * @return true if editting is successful o/w false
	 * @throws InterruptedException
	 */
	public boolean updateWebhook() throws InterruptedException {
		Thread.sleep(1000);
		boolean editDone = false;
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		for (int i = 0; i < webhooknameList.size(); i++) {
			if (webhooknameList.get(i).getText().equals(LoginAndSelectModule.testData.get("WebHookname"))) {
				List<WebElement> radioButtons = webhooknameList.get(i)
						.findElements(By.xpath(".//preceding::span[contains(@class, 'mat-radio-container')]"));
				radioButtons.get(i).click();
				wait.until(ExpectedConditions.elementToBeClickable(editButton));
				editButton.click();
				log.info("Edit button clicked successfully");
				driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
				nodeUpdation();
				wait.until(ExpectedConditions.elementToBeClickable(saveButton));
				saveButton.click();
				wait.until(ExpectedConditions.elementToBeClickable(yesButton));
				yesButton.click();
				wait.until(ExpectedConditions.elementToBeClickable(okButton));
				driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
				try {
					wait.until(ExpectedConditions.visibilityOf(successMessage));
					if (successMessage.isDisplayed()) {
						okButton.click();
						log.info("updation successful");
						editDone = true;
					}
				} catch (Exception e) {
					okButton.click();
					log.info("updation unsuccessful");
					editDone = false;
				}
				break;
			}
		}
		return editDone;
	}

	/**
	 * Enables node updation in webhook
	 * 
	 * @return true if node updation is enabled, false if node updation is not
	 *         enabled
	 */
	public boolean nodeUpdation() {
		log.info("In node updation");
		try {
			driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
			if (nodeField.isDisplayed()) {
				log.info("node updation is already enabled");
				return true;
			}
		} catch (Exception e) {
			wait.until(ExpectedConditions.elementToBeClickable(nodeToggleButton));
			nodeToggleButton.click();
			wait.until(ExpectedConditions.visibilityOf(nodeField));
			nodeField.sendKeys(LoginAndSelectModule.testData.get("NodeProperty"));
			log.info("node updation enabled");
			return false;
		}
		return false;
	}

	/**
	 * Creates new webhook with event configuration
	 * 
	 * @return true if new webhook is created o/w false
	 * @throws Exception
	 */
	public boolean addWebHookWithEvent() throws Exception {
		boolean isTrue = true;
		if (verifyWebhookPresent(LoginAndSelectModule.testData.get("WebHookname1"))) {
			log.debug("Webhook name already exists");
			throw new SkipException("Skipping test case as webhook already exists");
		} else {
			addWebhook.click();
			driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			webhookName.sendKeys(LoginAndSelectModule.testData.get("WebHookname1"));
			selectTool(LoginAndSelectModule.testData.get("ToolName"));
			eventToggleButton.click();
			if (checkUI()) {
				selectDataFormat(LoginAndSelectModule.testData.get("DataFromat"));
				responseTemplate.sendKeys(LoginAndSelectModule.testData.get("ResponseTemplate"));
				timeField.sendKeys(LoginAndSelectModule.testData.get("TimeField"));
				timeFormat.sendKeys(LoginAndSelectModule.testData.get("TimeFormat"));
				String eventConfigData = actionButton.getDynamicTemplate();
				eventConfig.sendKeys(eventConfigData);
				wait.until(ExpectedConditions.elementToBeClickable(saveButton));
				saveButton.click();
				wait.until(ExpectedConditions.elementToBeClickable(yesButton));
				yesButton.click();
				wait.until(ExpectedConditions.elementToBeClickable(okButton));
				driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
				try {
					if (successMessage.isDisplayed()) {
						wait.until(ExpectedConditions.elementToBeClickable(okButton));
						okButton.click();
						log.info("successfully added webhook");
						isTrue = true;
					}
				} catch (Exception e) {
					wait.until(ExpectedConditions.elementToBeClickable(okButton));
					okButton.click();
					log.info("webhook name already exists");
					isTrue = false;
					wait.until(ExpectedConditions.elementToBeClickable(redirectButton));
					redirectButton.click();
				}
				return isTrue;
			} else {
				log.info("Error while loading event config UI");
				return false;
			}
		}
	}

	/**
	 * Checks whether event config field is displayed and label name is disabled
	 * after enabling event processing
	 * 
	 * @return true if displayed UI is correct o/w false
	 */
	public boolean checkUI() {
		boolean isPresent = false;
		try {
			if (!labelName.isEnabled() && eventConfig.isDisplayed()) {
				log.info("label name is disabled and event config textbox is enabled");
				isPresent = true;
			}
		} catch (Exception e) {
			log.info("Error while loading event config UI");
			isPresent = false;
		}
		return isPresent;
	}
}
