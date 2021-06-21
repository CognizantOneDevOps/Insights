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

package com.cognizant.devops.platformregressiontest.test.ui.configurationfilemanagement;

import java.io.File;
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

import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;
import com.cognizant.devops.platformregressiontest.test.ui.testdata.WebhookDataProvider;
import com.cognizant.devops.platformregressiontest.test.ui.testdatamodel.ConfigurationFileManagementDataModel;

/**
 * @author NivethethaS
 * 
 *         Class contains the business logic for Configuration File Management
 *         module test cases
 *
 */
public class ConfigurationFileManagementConfiguration extends ConfigurationFileManagementObjectRepository {

	WebDriverWait wait = new WebDriverWait(driver, 20);

	private static final Logger log = LogManager.getLogger(ConfigurationFileManagementConfiguration.class);

	public static final String CONFIG_FILES_PATH = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator
			+ ConfigOptionsTest.AUTO_DIR + File.separator + ConfigOptionsTest.CONFIGURATION_FILE_DIR + File.separator;

	WebhookDataProvider actionButton = new WebhookDataProvider();

	public ConfigurationFileManagementConfiguration() {
		PageFactory.initElements(driver, this);
	}

	/**
	 * checks whether landing page is displayed or not
	 * 
	 * @return true if landing page is displayed o/w false
	 * @throws InterruptedException
	 */
	public boolean navigateToConfigurationLandingPage() {
		log.info("Configuration Landing page displayed");
		return configurationLandingPage.isDisplayed();
	}

	/**
	 * Creates new Configuration for a particular module and adds it to the database
	 * 
	 * @return true if new configuration is created o/w false
	 * @throws InterruptedException
	 */
	public boolean addNewConfiguration(ConfigurationFileManagementDataModel data) throws InterruptedException {
		if (checkIfModuleConfigurationPresent(data.getModule())) {
			log.debug("module name already exists");
			throw new SkipException("Skipping test case as module configuration already exists");
		} else {
			addButton.click();
			fileName.sendKeys(data.getFilename());
			fileType.click();
			selectFileType(data.getFiletype());
			wait.until(ExpectedConditions.visibilityOf(fileModule)).click();
			selectModuleName(data.getModule());
			uploadFile.sendKeys(CONFIG_FILES_PATH + data.getFilepath());
			saveButton.click();
			wait.until(ExpectedConditions.elementToBeClickable(yesButton));
			yesButton.click();
			wait.until(ExpectedConditions.elementToBeClickable(okButton));
			try {
				if (successMessage.isDisplayed()) {
					wait.until(ExpectedConditions.elementToBeClickable(okButton));
					okButton.click();
					log.info("successfully added module configuration");
					return true;
				}
			} catch (Exception e) {
				log.info("error while creating module configuration");
				redirectButton.click();
				return false;
			}
			log.info("unexpected error while adding new configuration");
			return false;
		}
	}

	/**
	 * Checks if Configuration for the module is already present in the UI
	 * 
	 * @param module
	 * @return true if Configuration for the module is not present o/w false
	 */
	private boolean checkIfModuleConfigurationPresent(String module) {
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		List<WebElement> rws = configurationDetailsTable.findElements(By.tagName("tr"));
		for (int i = 0; i < rws.size(); i++) {
			List<WebElement> cols = (rws.get(i)).findElements(By.tagName("td"));
			if ((cols.get(3).getText()).equals(module)) {
				List<WebElement> radioButtons = rws.get(i)
						.findElements(By.xpath(".//preceding::span[contains(@class, 'mat-radio-container')]"));
				driver.manage().timeouts().implicitlyWait(6, TimeUnit.SECONDS);
				radioButtons.get(i).click();
				driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
				log.info("{} module name is present.", module);
				return true;
			}
		}
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		log.info("{} module name is not present.", module);
		return false;
	}

	/**
	 * selects module name from the list of module names available
	 * 
	 * @param type
	 * @throws InterruptedException
	 */
	private void selectModuleName(String type) throws InterruptedException {
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		Thread.sleep(1000);
		for (WebElement toolValue : fileTypeList) {
			if ((toolValue.getText()).equals(type)) {
				wait.until(ExpectedConditions.visibilityOf(toolValue));
				toolValue.click();
				break;
			}
		}
	}

	/**
	 * selects file type from the list of file types available
	 * 
	 * @param moduleName
	 * @throws InterruptedException
	 */
	private void selectFileType(String moduleName) throws InterruptedException {
		Thread.sleep(1000);
		for (WebElement moduleValue : fileModuleList) {
			if ((moduleValue.getText()).equals(moduleName)) {
				wait.until(ExpectedConditions.elementToBeClickable(moduleValue));
				moduleValue.click();
				break;
			}
		}
	}

	/**
	 * Checks whether error message is popped out if we add existing configuration
	 * 
	 * @return true if error message is popped up o/w false
	 * @throws InterruptedException
	 */
	public boolean addSameConfiguration() throws InterruptedException {
		if (!checkIfModuleConfigurationPresent(LoginAndSelectModule.testData.get("module"))) {
			log.debug("module name already exists");
			throw new SkipException("Skipping test case as module configuration already exists");
		} else {
			addButton.click();
			fileName.sendKeys(LoginAndSelectModule.testData.get("filename"));
			fileType.click();
			selectFileType(LoginAndSelectModule.testData.get("filetype"));
			fileModule.click();
			selectModuleName(LoginAndSelectModule.testData.get("module"));
			uploadFile.sendKeys(CONFIG_FILES_PATH + LoginAndSelectModule.testData.get("filepath"));
			saveButton.click();
			wait.until(ExpectedConditions.elementToBeClickable(yesButton));
			yesButton.click();
			wait.until(ExpectedConditions.elementToBeClickable(okButton));
			try {
				if (errorMessage.isDisplayed()) {
					wait.until(ExpectedConditions.elementToBeClickable(okButton));
					okButton.click();
					log.info("error message is displayed when we try to add same module configuration");
					redirectButton.click();
					return true;
				}
			} catch (Exception e) {
				log.info("error while creating same module configuration");
				redirectButton.click();
				return false;
			}
			log.info("unexpected error while creating same module configuration");
			return false;
		}
	}

	/**
	 * Edits the configuration with updated configuration file
	 * 
	 * @return true if editing is successful o/w false
	 */
	public boolean editConfiguration() {
		if (!checkIfModuleConfigurationPresent(LoginAndSelectModule.testData.get("module"))) {
			log.debug("module cofiguration is not present");
			throw new SkipException("Skipping test case as module configuration is not available to edit");
		} else {
			wait.until(ExpectedConditions.elementToBeClickable(deleteButton));
			editButton.click();
			uploadFile.sendKeys(CONFIG_FILES_PATH + LoginAndSelectModule.testData.get("updatedfilepath"));
			saveButton.click();
			wait.until(ExpectedConditions.elementToBeClickable(yesButton));
			yesButton.click();
			wait.until(ExpectedConditions.elementToBeClickable(okButton));
			try {
				if (successMessage.isDisplayed()) {
					wait.until(ExpectedConditions.elementToBeClickable(okButton));
					okButton.click();
					log.info("Module configuration is editted successfully");
					return true;
				}
			} catch (Exception e) {
				log.info("error while editting module configuration");
				redirectButton.click();
				return false;
			}
			log.info("something went wrong");
			return false;
		}
	}

	/**
	 * checks if refresh, reset and redirect to landing page functionalities are
	 * successful
	 * 
	 * @return true if all the functionalities are successful o/w false
	 * @throws InterruptedException
	 */
	public boolean refreshAndResetFunctionality() throws InterruptedException {
		if (refreshFunctionalityCheck(LoginAndSelectModule.testData.get("filename")) && resetFunctionalityCheck()) {
			log.info("refresh & reset functionality successful");
			redirectButton.click();
			wait.until(ExpectedConditions.elementToBeClickable(landingPage));
			try {
				if (landingPage.isDisplayed()) {
					log.info("navigate to landing page successful");
					return true;
				}
			} catch (Exception e) {
				log.info("navigate to landing page unsuccessful");
				return false;
			}
			return true;
		}
		log.info("refresh & reset functionality unsuccessful");
		return false;
	}

	/**
	 * checks if reset functionality is successful
	 * 
	 * @return true if reset functionality is successful o/w false
	 */
	private boolean resetFunctionalityCheck() {
		addButton.click();
		fileName.sendKeys(LoginAndSelectModule.testData.get("filename"));
		resetButton.click();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		if (fileName.getText().length() == 0) {
			log.info("reset functionality successful");
			return true;
		}
		log.info("reset functionality unsuccessful");
		return false;
	}

	/**
	 * checks if refresh functionality is successful
	 * 
	 * @return true if refresh functionality is successful o/w false
	 * @throws InterruptedException
	 */
	private boolean refreshFunctionalityCheck(String fileName) {
		try {
			for (int i = 0; i < fileNameList.size(); i++) {
				if (fileNameList.get(i).getText().equals(fileName)) {
					List<WebElement> radioButtons = fileNameList.get(i)
							.findElements(By.xpath(".//preceding::span[contains(@class, 'mat-radio-container')]"));
					radioButtons.get(i).click();
					log.info("Radio button clicked successfully.");
					refreshButton.click();
					log.info("Refresh button clicked successfully.");
					Thread.sleep(2000);
					if (!radioButtons.get(i).isSelected()) {
						log.info("radio button is unselected after clicking on refresh icon");
						return true;
					}
				}
			}
		} catch (Exception ex) {
			log.warn(ex.getMessage());
			throw new SkipException("Skipping test case as something went wrong.");
		}
		return false;
	}

	/**
	 * Deletes the configuration created
	 * 
	 * @return true if deletion is successful o/w false
	 */
	public boolean deleteConfiguration() {
		if (!checkIfModuleConfigurationPresent(LoginAndSelectModule.testData.get("module"))) {
			log.debug("module cofiguration is not present or already deleted");
			throw new SkipException("Skipping test case as module configuration is already deleted");
		} else {
			wait.until(ExpectedConditions.elementToBeClickable(deleteButton));
			deleteButton.click();
			wait.until(ExpectedConditions.elementToBeClickable(yesButton));
			yesButton.click();
			wait.until(ExpectedConditions.elementToBeClickable(okButton));
			try {
				if (successMessage.isDisplayed()) {
					wait.until(ExpectedConditions.elementToBeClickable(okButton));
					okButton.click();
					log.info("successfully deleted module configuration");
					return true;
				}
			} catch (Exception e) {
				log.info("error while deleting module configuration");
				redirectButton.click();
				return false;
			}
			log.info("something went wrong - unexpected error while deleting configuration");
			return false;
		}
	}
}
