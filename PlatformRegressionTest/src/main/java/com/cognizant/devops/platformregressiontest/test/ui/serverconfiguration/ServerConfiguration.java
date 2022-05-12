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
package com.cognizant.devops.platformregressiontest.test.ui.serverconfiguration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

/**
 * @author NivethethaS
 * 
 *         Class contains the logic for server configuration roles test cases
 *
 */
public class ServerConfiguration extends ServerConfigurationObjectRepository {

	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

	private static final Logger log = LogManager.getLogger(ServerConfiguration.class);

	public ServerConfiguration() {
		PageFactory.initElements(driver, this);
	}

	/**
	 * checks whether landing page is displayed or not
	 * 
	 * @return true if landing page is displayed o/w false
	 */
	public boolean navigateToServerConfigurationLandingPage() {
		log.info("Server Configuration Landing page displayed : {}", landingPage.isDisplayed());
		return landingPage.isDisplayed();
	}

	/**
	 * Checks whether success message is displayed for an empty save
	 * 
	 * @return true if save button works properly o/w false
	 */
	public boolean saveConfiguration() {
		checkIsOnlineRegistration("true");
		offlineAgentPathValue.clear();
		saveButton.click();
		wait.until(ExpectedConditions.elementToBeClickable(yesButton));
		yesButton.click();
		try {
			wait.until(ExpectedConditions.elementToBeClickable(okButton));
			if (successMessage.isDisplayed()) {
				log.info("save button functionality successful : true");
				okButton.click();
				return true;
			}
		} catch (Exception e) {
			log.info("save button functionality successful : false");
			return false;
		}
		return false;
	}

	/**
	 * Checks whether server configuration page is not displayed when redirect
	 * button is clicked
	 * 
	 * @return true if server configuration page is not displayed o/w false
	 */
	public boolean redirectFunctionality() {
		redirectButton.click();
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		try {
			if (landingPage.isDisplayed()) {
				log.info("Redirect functionality successful : false");
				return false;
			}
		} catch (Exception e) {
			log.info("Redirect functionality successful : true");
			return true;
		}
		return false;
	}

	private void checkIsOnlineRegistration(String isOnlineRegistrationData) {
		wait.until(ExpectedConditions.elementToBeClickable(isOnlineRegistrationValue));
		isOnlineRegistrationValue.clear();
		isOnlineRegistrationValue.sendKeys(isOnlineRegistrationData);
		log.info("isOnlineRegistration : {}", isOnlineRegistrationValue.getAttribute("value"));
	}

}
