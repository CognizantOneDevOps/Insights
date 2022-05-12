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
package com.cognizant.devops.platformregressiontest.test.engineautomation;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

public class AddConfigurationFile extends AddConfigurationFileObjectRepository {
	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
	private static final Logger log = LogManager.getLogger(AddConfigurationFile.class);

	public AddConfigurationFile() {
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
	 * @throws Exception
	 */
	public boolean addNewConfiguration() throws Exception {
		if (addFileDetails(LoginAndSelectModule.testData.get("correlationfilename"),
				LoginAndSelectModule.testData.get("filetype"), LoginAndSelectModule.testData.get("correlationmodule"),
				LoginAndSelectModule.testData.get("correlationfilepath"))
				&& addFileDetails(LoginAndSelectModule.testData.get("dataenrichmentfilename"),
						LoginAndSelectModule.testData.get("filetype"),
						LoginAndSelectModule.testData.get("dataenrichmentmodule"),
						LoginAndSelectModule.testData.get("dataenrichmentfilepath"))
				&& addFileDetails(LoginAndSelectModule.testData.get("traceabilityfilename"),
						LoginAndSelectModule.testData.get("filetype"),
						LoginAndSelectModule.testData.get("traceabilitymodule"),
						LoginAndSelectModule.testData.get("traceabilityfilepath"))) {
			return true;
		}
		return false;
	}

	private boolean addFileDetails(String filename, String filetype, String modulename, String modulepath)
			throws InterruptedException {
		if (checkIfModuleConfigurationPresent(filename)) {
			return false;
		} else {
			addButton.click();
			fileName.sendKeys(filename);
			fileType.click();
			selectFileType(filetype);
			wait.until(ExpectedConditions.visibilityOf(fileModule)).click();
			selectModuleName(modulename);
			uploadFile.sendKeys(modulepath);
			saveButton.click();
			wait.until(ExpectedConditions.elementToBeClickable(yesButton));
			yesButton.click();
			wait.until(ExpectedConditions.elementToBeClickable(okButton));
			try {
				if (successMessage.isDisplayed()) {
					wait.until(ExpectedConditions.elementToBeClickable(okButton));
					okButton.click();
					log.info("successfully added {} module configuration", modulename);
					return true;
				}
			} catch (Exception e) {
				log.info("error while creating {} module configuration", modulename);
				redirectButton.click();
				return false;
			}
			log.info("unexpected error while adding new {} configuration", modulename);
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
}
