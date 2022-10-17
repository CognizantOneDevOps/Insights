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
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or 

ied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformregressiontest.test.ui.serverconfiguration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
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
	boolean isInvalid=false;

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
	 * @throws InterruptedException 
	 */
	public boolean saveConfiguration() throws InterruptedException {
		Thread.sleep(1000);
		visibilityOf(elasticSearchEndpoint,2);
		elasticSearchEndpoint.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
		clickOn(saveButton, 1);
		yesButton.click();
		try {
			if (successMessage.isDisplayed()) {
				clickOn(okClose, 2);
				log.info("save button functionality successful : true");
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
	 * @throws InterruptedException 
	 */
	public boolean redirectFunctionality() throws InterruptedException {
		redirectButton.click();
		Thread.sleep(1000);
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

	public boolean checkAllMandatoryFields() {
		try {
			if (checkGrafanaMandatoryFields() && checkNeo4jMandatoryFields() &&
					checkPostgreMandatoryFields() && checkMsgQueueMandatoryFields()) {
				log.info("Successfully validated all mandatory fields : true");
				return true;
			}
		} catch (Exception e) {
			log.info("Error while validating mandatory fields");
			return false;
		}
		return true;
	}


	/**
	 * Checks whether Error message is displayed for mandatory fields
	 * 
	 * @return true if error is displayed o/w false
	 */
	public boolean checkGrafanaMandatoryFields() {
		try{
		visibilityOf(graphendpoint,2);
		grafanaInput.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
		clickOn(saveButton, 1);
		wait.until(ExpectedConditions.visibilityOf(grafanaErrorMsg));
		isInvalid=true;
		log.info(grafanaErrorMsg.getText());
		wait.until(ExpectedConditions.visibilityOf(crossClose)).click();
		visibilityOf(grafanaDBEndpoint,2);
		grafanaDBEndpoint.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
		clickOn(saveButton, 1);
		grafanaErrorMsg.isDisplayed();
		isInvalid=true;
		log.info(grafanaErrorMsg.getText());
		wait.until(ExpectedConditions.visibilityOf(crossClose)).click();
		visibilityOf(grafanaadminUserName,2);
		grafanaadminUserName.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
		clickOn(saveButton, 1);
		wait.until(ExpectedConditions.visibilityOf(grafanaErrorMsg));
		isInvalid=true;
		log.info(grafanaErrorMsg.getText());
		wait.until(ExpectedConditions.visibilityOf(crossClose)).click();
		visibilityOf(grafanaadminUserPassword,2);
		grafanaadminUserPassword.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
		clickOn(saveButton, 1);
		wait.until(ExpectedConditions.visibilityOf(grafanaErrorMsg));
		isInvalid=true;
		log.info(grafanaErrorMsg.getText());
		wait.until(ExpectedConditions.visibilityOf(crossClose)).click();
		return isInvalid;
		} 
		catch (Exception e) {
			log.info("Exception occured");
			isInvalid=false;
			return isInvalid;
		}		
	}
	public boolean checkNeo4jMandatoryFields() throws InterruptedException {
		redirectButton.click();
		selectModuleOnClickingConfig(LoginAndSelectModule.testData.get("serverconf"));
		try{
		visibilityOf(graphendpoint,2);
		graphendpoint.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
		clickOn(saveButton, 1);
		wait.until(ExpectedConditions.visibilityOf(neo4jErrorMsg));
		isInvalid=true;
		log.info(neo4jErrorMsg.getText());
		wait.until(ExpectedConditions.visibilityOf(crossClose)).click();
		visibilityOf(graphauthToken,2);
		graphauthToken.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
		clickOn(saveButton, 1);
		neo4jErrorMsg.isDisplayed();
		isInvalid=true;
		log.info(neo4jErrorMsg.getText());
		wait.until(ExpectedConditions.visibilityOf(crossClose)).click();
		visibilityOf(graphboltEndPoint,2);
		graphboltEndPoint.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
		clickOn(saveButton, 1);
		wait.until(ExpectedConditions.visibilityOf(neo4jErrorMsg));
		isInvalid=true;
		log.info(neo4jErrorMsg.getText());
		wait.until(ExpectedConditions.visibilityOf(crossClose)).click();
		return isInvalid;
		} 
		catch (Exception e) {
			log.info("Exception occured");
			isInvalid=false;
			return isInvalid;
		}		
	}
	public boolean checkPostgreMandatoryFields() throws InterruptedException {
		redirectButton.click();
		selectModuleOnClickingConfig(LoginAndSelectModule.testData.get("serverconf"));
		try{
		visibilityOf(postgreUserName,2);
		postgreUserName.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
		clickOn(saveButton, 1);
		wait.until(ExpectedConditions.visibilityOf(postgreErrorMsg));
		isInvalid=true;
		log.info(postgreErrorMsg.getText());
		wait.until(ExpectedConditions.visibilityOf(crossClose)).click();
		visibilityOf(postgrePassword,2);
		postgrePassword.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
		clickOn(saveButton, 1);
		postgreErrorMsg.isDisplayed();
		isInvalid=true;
		log.info(postgreErrorMsg.getText());
		wait.until(ExpectedConditions.visibilityOf(crossClose)).click();
		visibilityOf(postgreInsightsDBUrl,2);
		postgreInsightsDBUrl.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
		clickOn(saveButton, 1);
		wait.until(ExpectedConditions.visibilityOf(postgreErrorMsg));
		isInvalid=true;
		log.info(postgreErrorMsg.getText());
		wait.until(ExpectedConditions.visibilityOf(crossClose)).click();
		visibilityOf(postgreGrafanaDBUrl,2);
		postgreGrafanaDBUrl.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
		clickOn(saveButton, 1);
		wait.until(ExpectedConditions.visibilityOf(postgreErrorMsg));
		isInvalid=true;
		log.info(postgreErrorMsg.getText());
		wait.until(ExpectedConditions.visibilityOf(crossClose)).click();
		return isInvalid;
		} 
		catch (Exception e) {
			log.info("Exception occured");
			isInvalid=false;
			return isInvalid;
		}		
	}
	public boolean checkMsgQueueMandatoryFields() throws InterruptedException {
		redirectButton.click();
		selectModuleOnClickingConfig(LoginAndSelectModule.testData.get("serverconf"));
		try{
		visibilityOf(messageQueueHost,2);
		messageQueueHost.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
		clickOn(saveButton, 1);
		wait.until(ExpectedConditions.visibilityOf(msgQueueErrorMsg));
		isInvalid=true;
		log.info(msgQueueErrorMsg.getText());
		wait.until(ExpectedConditions.visibilityOf(crossClose)).click();
		visibilityOf(messageQueueUser,2);
		messageQueueUser.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
		clickOn(saveButton, 1);
		msgQueueErrorMsg.isDisplayed();
		isInvalid=true;
		log.info(msgQueueErrorMsg.getText());
		wait.until(ExpectedConditions.visibilityOf(crossClose)).click();
		visibilityOf(messageQueuePassword,2);
		messageQueuePassword.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
		clickOn(saveButton, 1);
		wait.until(ExpectedConditions.visibilityOf(msgQueueErrorMsg));
		isInvalid=true;
		log.info(msgQueueErrorMsg.getText());
		wait.until(ExpectedConditions.visibilityOf(crossClose)).click();
		return isInvalid;
		} 
		catch (Exception e) {
			log.info("Exception occured");
			isInvalid=false;
			return isInvalid;
		}		
	}
	public boolean checkOtherMandatoryFields() throws InterruptedException {
		redirectButton.click();
		selectModuleOnClickingConfig(LoginAndSelectModule.testData.get("serverconf"));
		try{
			Thread.sleep(1000);
		visibilityOf(insightsServiceURL,2);
		insightsServiceURL.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
		clickOn(saveButton, 1);
		wait.until(ExpectedConditions.visibilityOf(serviceUrlErrorMsg));
		wait.until(ExpectedConditions.visibilityOf(crossClose)).click();
		isInvalid=true;
		log.info(serviceUrlErrorMsg.getText());
		return isInvalid;
		} 
		catch (Exception e) {
			log.info("Exception occured");
			isInvalid=false;
			return isInvalid;
		}		
	}

	private void checkIsOnlineRegistration(String isOnlineRegistrationData) {
		wait.until(ExpectedConditions.elementToBeClickable(isOnlineRegistrationValue));
		isOnlineRegistrationValue.clear();
		isOnlineRegistrationValue.sendKeys(isOnlineRegistrationData);
		log.info("isOnlineRegistration : {}",
				isOnlineRegistrationValue.getAttribute("value"));
	}
	/**
	 * wait until the visibility of web element
	 * 
	 * @param element
	 * @param timeout
	 * @return true if element is displayed else false
	 */
	public static boolean visibilityOf(WebElement element, int timeout) {
		return new WebDriverWait(driver, Duration.ofSeconds(timeout)).until(ExpectedConditions.visibilityOf(element)).isDisplayed();
	}
	/**
	 * wait until the visibility of web element then click on the web element
	 * 
	 * @param element
	 * @param timeout
	 */
	public static void clickOn(WebElement element, int timeout) {
		new WebDriverWait(driver, Duration.ofSeconds(timeout)).until(ExpectedConditions.elementToBeClickable(element)).click();
	}


}
