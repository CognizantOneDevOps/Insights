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
package com.cognizant.devops.platformregressiontest.test.ui.logosetting;

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;
import com.google.auto.common.Visibility;

/**
 * @author NivethethaS
 * 
 *         Class contains the logic for logo setting roles test cases
 *
 */
public class LogoSettingConfiguration extends LogoSettingObjectRepository {

	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

	private static final Logger log = LogManager.getLogger(LogoSettingConfiguration.class);

	public LogoSettingConfiguration() {
		PageFactory.initElements(driver, this);
	}

	/**
	 * checks whether landing page is displayed or not
	 * 
	 * @return true if landing page is displayed o/w false
	 */
	public boolean navigateToLogoSettingLandingPage() {
		log.info("Logo setting Landing page displayed : {}", landingPage.isDisplayed());
		return landingPage.isDisplayed();
	}
	
	public boolean addLargeFileSize() throws InterruptedException {
		chooseFileButton.sendKeys(LoginAndSelectModule.testData.get("largeFile"));
		Thread.sleep(2000);
		visibilityOf(uploadImage,2);
		clickOn(uploadImage,3);
		wait.until(ExpectedConditions.elementToBeClickable(crossClose));
		try {
			if (errorMessage.isDisplayed()) {
				log.info("Error message is displayed while trying to add image size greater than 1 MB");
				crossClose.click();
				return true;
			}
		} catch (Exception e) {
			log.info("Error message is not displayed");
			crossClose.click();
			return false;
		}
		return false;
	}

	public boolean addLogo() {
		chooseFileButton.sendKeys(LoginAndSelectModule.testData.get("insights_logo"));
		previewLogo.isDisplayed();
		uploadImage.click();
		wait.until(ExpectedConditions.elementToBeClickable(crossClose));
		try {
			if (successMessage.isDisplayed()) {
				log.info("successfully added logo");
				crossClose.click();
				return true;
			}
		} catch (Exception e) {
			log.info("Error while adding logo");
			crossClose.click();
			return false;
		}
		return false;
	}
	
	public boolean cancelUpload() {
		chooseFileButton.sendKeys(LoginAndSelectModule.testData.get("insights_logo"));
		previewLogo.isDisplayed();
		cancelUpload.click();
		try {
			if (previewLogo.isDisplayed()) {
				log.info("cancel upload functionality - fail");
				return false;
			}
		} catch (Exception e) {
			log.info("cancel upload functionality - success");
			return true;
		}
		return false;
	}

	public boolean incorrectFile() {
		chooseFileButton.sendKeys(LoginAndSelectModule.testData.get("incorrectFile"));
		uploadImage.click();
		wait.until(ExpectedConditions.elementToBeClickable(okButton));
		try {
			if (errorMessage.isDisplayed()) {
				log.info("Error message is displayed while trying to add image size greater than 1 MB");
				okButton.click();
				return true;
			}
		} catch (Exception e) {
			log.info("Error message is not displayed");
			okButton.click();
			return false;
		}
		return false;
	}
	/**
	 * wait until the visibility of web element
	 * 
	 * @param element
	 * @param timeout
	 * @return true if element is displayed else false
	 */
	public static boolean visibilityOf(WebElement element, int timeout) {
		new WebDriverWait(driver, Duration.ofSeconds(timeout)).until(ExpectedConditions.visibilityOf(element));
		return element.isDisplayed();
	}

	/**
	 * wait until the visibility of web element then click on the web element
	 * 
	 * @param element
	 * @param timeout
	 */
	public static void clickOn(WebElement element, int timeout) {
		new WebDriverWait(driver, Duration.ofSeconds(timeout)).until(ExpectedConditions.elementToBeClickable(element));
		element.click();
	}

}
