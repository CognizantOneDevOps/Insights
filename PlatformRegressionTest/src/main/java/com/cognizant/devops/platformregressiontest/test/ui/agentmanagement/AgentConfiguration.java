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
package com.cognizant.devops.platformregressiontest.test.ui.agentmanagement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.cognizant.devops.platformregressiontest.test.ui.testdatamodel.AgentManagementDataModel;

public class AgentConfiguration extends AgentObjectRepository {

	WebDriverWait wait = new WebDriverWait(driver, 10);
	WebDriverWait hold = new WebDriverWait(driver, 1);
	Map<String, String> testData = new HashMap<>();
	private static final Logger log = LogManager.getLogger(AgentConfiguration.class);

	public AgentConfiguration() {
		PageFactory.initElements(driver, this);
	}

	public boolean verifyServerConfigAgentDetailBlock(AgentManagementDataModel data) {
		selectModuleUnderConfiguration("Server Configuration");
		try {
			JavascriptExecutor js = (JavascriptExecutor) driver;
			js.executeScript("arguments[0].scrollIntoView();", agentConfigurationHeading);
			if (visibilityOf(agentConfigurationHeading, 10) && checkIsOnlineRegistration(data.getIsOnlineRegistration())
					&& checkBrowseRepoUrl(data.getBrowseRepoUrl()) && checkDownloadRepoUrl(data.getDownloadRepoUrl())
					&& checkOnlineRegistrationMode(data.getOnlineRegistrationMode())
					&& checkOfflineAgentPath(data.getOfflineAgentPath()) && checkUnzipPath(data.getUnzipPath())
					&& checkAgentExchange(data.getAgentExchange()) && checkAgentPkgQueue(data.getAgentPkgQueue())) {
				clickOn(save, 10);
				clickOn(yes, 10);
				visibilityOf(successHeading, 10);
				log.info(successMsg.getText());
				clickOn(ok, 10);
				return true;
			}
		} catch (Exception e) {
			log.info("Something went wrong while checking agent details in server config.");
		}
		return false;
	}

	public boolean navigateToAgentManagementLandingPage(AgentManagementDataModel data) throws InterruptedException {
		selectModuleOnClickingConfig("Agent Management");
		log.info("Landing page is displayed.");
		return landingPage.isDisplayed();
	}

	public boolean checkRegisteredAgentInHealthTab(AgentManagementDataModel data) {
		selectMenuOption("Health Check");

		return false;
	}

	public boolean registerAgent(AgentManagementDataModel data) throws InterruptedException {
		if (verifyAgentId(data.getAgentId())) {
			log.info("Agent id already exists.");
			return false;
		}
		visibilityOf(clickAddButton, 10);
		clickOn(clickAddButton, 10);
		Thread.sleep(5000);
		selectOs.sendKeys(data.getOsName());
		selectAgentType.sendKeys(data.getTypeName());
		Thread.sleep(2000);
		selectVersion.sendKeys(data.getVersion());
		selectTools.sendKeys(data.getToolName());
		visibilityOf(mqUser, 10);
		mqUser.clear();
		mqUser.sendKeys(data.getMqUser());
		visibilityOf(mqPassword, 10);
		mqPassword.clear();
		mqPassword.sendKeys(data.getMqPassword());
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].scrollIntoView();", agentIdValue);
		agentIdValue.sendKeys(data.getAgentId());
		accessToken.clear();
		accessToken.sendKeys(data.getAccessToken());
		getRepos.clear();
		getRepos.sendKeys(data.getGetRepos());
		commitsBaseEndPoint.clear();
		commitsBaseEndPoint.sendKeys(data.getCommitsBaseEndPoint());
		wait.until(ExpectedConditions.visibilityOf(addAgent)).click();
		success.isDisplayed();
		log.info(successMsg.getText());
		wait.until(ExpectedConditions.visibilityOf(ok)).click();
		return verifyAgentId(data.getAgentId());
	}
	
	public boolean registerAgentWithSameAgentId(AgentManagementDataModel data) throws InterruptedException {
		wait.until(ExpectedConditions.visibilityOf(clickAddButton)).click();
		Thread.sleep(5000);
		selectOs.sendKeys(data.getOsName());
		selectAgentType.sendKeys(data.getTypeName());
		Thread.sleep(2000);
		selectVersion.sendKeys(data.getVersion());
		selectTools.sendKeys(data.getToolName());
		wait.until(ExpectedConditions.visibilityOf(mqUser)).clear();
		Thread.sleep(2000);
		wait.until(ExpectedConditions.visibilityOf(mqUser)).clear();
		mqUser.sendKeys(data.getMqUser());
		mqPassword.clear();
		mqPassword.sendKeys(data.getMqPassword());
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].scrollIntoView();", agentIdValue);
		agentIdValue.sendKeys(data.getAgentId());
		accessToken.clear();
		accessToken.sendKeys(data.getAccessToken());
		getRepos.clear();
		getRepos.sendKeys(data.getGetRepos());
		commitsBaseEndPoint.clear();
		commitsBaseEndPoint.sendKeys(data.getCommitsBaseEndPoint());
		wait.until(ExpectedConditions.visibilityOf(addAgent)).click();
		duplicateError.isDisplayed();
		wait.until(ExpectedConditions.visibilityOf(errorMsg));
		log.info(errorMsg.getText());
		wait.until(ExpectedConditions.visibilityOf(ok)).click();
		wait.until(ExpectedConditions.visibilityOf(cancelButton)).click();
		cancelAgentMessage.isDisplayed();
		wait.until(ExpectedConditions.visibilityOf(yes)).click();
		return verifyAgentId(data.getAgentId());

	}

	public boolean verifyAgentId(String agentId) {
		driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
		boolean isInstanceId = false;
		try {
			hold.until(ExpectedConditions.visibilityOfAllElements(agentsList));
			for (WebElement agent : agentsList) {
				if (agent.getText().equals(agentId)) {
					log.info("{} present in the agents list.", agentId);
					isInstanceId = true;
					return isInstanceId;
				}
			}
			log.info("{} not found in the agents list.", agentId);
		} catch (Exception e) {
			log.info("Agent List is empty.");
		}
		driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
		return isInstanceId;
	}

	public boolean startAgent(AgentManagementDataModel data) throws InterruptedException {
		selectAgent(data);
		try {
			driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
			if (visibilityOf(stopAgent, 3)) {
				driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
				log.info("Agent is in start mode");
				clickOn(stopAgent, 10);
				clickOn(ok, 10);
				log.info("Agent stopped successfully.");
				selectAgent(data);
				if (startAgent.isDisplayed()) {
					log.info("Agent is in stop mode.");
					clickOn(startAgent, 10);
					clickOn(ok, 10);
					log.info("Agent started successfully.");
				}
				selectAgent(data);
				return stopAgent.isDisplayed();
			}
		} catch (Exception ex) {
			driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			if (startAgent.isDisplayed()) {
				log.info("Agent is in stop mode.");
				clickOn(startAgent, 10);
				clickOn(ok, 10);
				log.info("Agent started successfully.");
				selectAgent(data);
				if (stopAgent.isDisplayed()) {
					log.info("Agent is in start mode");
					clickOn(stopAgent, 10);
					clickOn(ok, 10);
					log.info("Agent stopped successfully.");
					selectAgent(data);
					return startAgent.isDisplayed();
				}
			}
		}
		return false;
	}

	public boolean updateAgent(AgentManagementDataModel data) throws InterruptedException {
		searchToolName(data.getToolName());
		selectAgent(data);
		wait.until(ExpectedConditions.visibilityOf(editAgent)).click();
		wait.until(ExpectedConditions.visibilityOf(dataLabelName)).clear();
		dataLabelName.sendKeys(data.getDataLabelName());
		wait.until(ExpectedConditions.visibilityOf(healthLabelName)).clear();
		healthLabelName.sendKeys(data.getHealthLabelName());
		wait.until(ExpectedConditions.visibilityOf(update)).click();
		wait.until(ExpectedConditions.visibilityOf(success));
		success.isDisplayed();
		wait.until(ExpectedConditions.visibilityOf(ok)).click();
		log.info("{} updated successfully.", data.getAgentId());
		return landingPage.isDisplayed();
	}

	public boolean deleteAgent(AgentManagementDataModel data) throws InterruptedException {
		selectAgent(data);
		wait.until(ExpectedConditions.visibilityOf(stopAgent)).click();
		wait.until(ExpectedConditions.visibilityOf(ok)).click();
		selectAgent(data);
		wait.until(ExpectedConditions.visibilityOf(deleteAgent)).click();
		wait.until(ExpectedConditions.elementToBeClickable(yes)).click();
		Thread.sleep(1000);
		if (!selectAgent(data)) {
			log.info("{} deleted successfully.", data.getAgentId());
			return true;
		}
		return false;
	}

	private String searchToolName(String tools) {
		driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
		visibilityOf(selectToolName, 10);
		clickOn(selectToolName, 10);
		visibilityOfAllElements(toolNameList);
		for (WebElement toolName : toolNameList) {
			if ((toolName.getText()).equals(tools)) {
				visibilityOf(toolName, 10);
				clickOn(toolName, 10);
				break;
			}
		}
		return tools;
	}

	public boolean selectAgent(AgentManagementDataModel data) {
		boolean agentPresent = false;
		try {
			driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
			hold.until(ExpectedConditions.visibilityOfAllElements(agentsList));
			for (int i = 0; i < agentsList.size(); i++) {
				if (agentsList.get(i).getText().equals(data.getAgentId())) {
					driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
					agentPresent = true;
					List<WebElement> radioButtons = agentsList.get(i)
							.findElements(By.xpath(".//preceding::span[contains(@class, 'mat-radio-container')]"));
					driver.manage().timeouts().implicitlyWait(3600, TimeUnit.SECONDS);
					radioButtons.get(i).click();
					break;
				}
			}
		} catch (Exception e) {
			log.info("Agents list is empty!!");
		}
		driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
		return agentPresent;
	}

	private boolean checkDownloadRepoUrl(String downloadRepoUrlData) {
		if (visibilityOf(downloadRepoUrl, 10)) {
			downloadRepoUrlValue.clear();
			downloadRepoUrlValue.sendKeys(downloadRepoUrlData);
			log.info("downloadRepoUrl : {}", downloadRepoUrlValue.getAttribute("value"));
			if (downloadRepoUrlValue.getAttribute("value").equals(downloadRepoUrlData))
				return true;
		}
		return false;
	}

	private boolean checkBrowseRepoUrl(String browseRepoUrlData) {
		if (visibilityOf(browseRepoUrl, 10)) {
			browseRepoUrlValue.clear();
			browseRepoUrlValue.sendKeys(browseRepoUrlData);
			log.info("browseRepoUrl : {}", browseRepoUrlValue.getAttribute("value"));
			if (browseRepoUrlValue.getAttribute("value").equals(browseRepoUrlData))
				return true;
		}
		return false;
	}

	private boolean checkIsOnlineRegistration(String isOnlineRegistrationData) {
		if (visibilityOf(isOnlineRegistration, 10)) {
			isOnlineRegistrationValue.clear();
			isOnlineRegistrationValue.sendKeys(isOnlineRegistrationData);
			log.info("isOnlineRegistration : {}", isOnlineRegistrationValue.getAttribute("value"));
			if (isOnlineRegistrationValue.getAttribute("value").equals(isOnlineRegistrationData))
				return true;
		}
		return false;
	}

	private boolean checkOnlineRegistrationMode(String onlineRegistrationModeData) {
		if (visibilityOf(onlineRegistrationMode, 10)) {
			onlineRegistrationModeValue.clear();
			onlineRegistrationModeValue.sendKeys(onlineRegistrationModeData);
			log.info("onlineRegistrationMode : {}", onlineRegistrationModeValue.getAttribute("value"));
			if (onlineRegistrationModeValue.getAttribute("value").contains(onlineRegistrationModeData))
				return true;
		}
		return false;
	}

	private boolean checkOfflineAgentPath(String offlineAgentPathData) {
		if (visibilityOf(offlineAgentPath, 10)) {
			offlineAgentPathValue.clear();
			offlineAgentPathValue.sendKeys(offlineAgentPathData);
			log.info("offlineAgentPath : {}", offlineAgentPathValue.getAttribute("value"));
			if (offlineAgentPathValue.getAttribute("value").contains(offlineAgentPathData))
				return true;
		}
		return false;
	}

	private boolean checkUnzipPath(String unzipPathData) {
		if (visibilityOf(unzipPath, 10)) {
			unzipPathValue.clear();
			unzipPathValue.sendKeys(unzipPathData);
			log.info("unzipPath : {}", unzipPathValue.getAttribute("value"));
			if (unzipPathValue.getAttribute("value").contains(unzipPathData))
				return true;
		}
		return false;
	}

	private boolean checkAgentExchange(String agentExchangeData) {
		if (visibilityOf(agentExchange, 10)) {
			agentExchangeValue.clear();
			agentExchangeValue.sendKeys(agentExchangeData);
			log.info("agentExchange : {}", agentExchangeValue.getAttribute("value"));
			if (agentExchangeValue.getAttribute("value").contains(agentExchangeData))
				return true;
		}
		return false;
	}

	private boolean checkAgentPkgQueue(String agentPkgQueueData) {
		if (visibilityOf(agentPkgQueue, 10)) {
			agentPkgQueueValue.clear();
			agentPkgQueueValue.sendKeys(agentPkgQueueData);
			log.info("agentPkgQueue : {}", agentPkgQueueValue.getAttribute("value"));
			if (agentPkgQueueValue.getAttribute("value").contains(agentPkgQueueData))
				return true;
		}
		return false;
	}

	/**
	 * wait until the visibility of list of web elements
	 * 
	 * @param element
	 * @return size of list of web elements
	 */
	public static int visibilityOfAllElements(List<WebElement> element) {
		return new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfAllElements(element)).size();
	}

	/**
	 * wait until the visibility of web element
	 * 
	 * @param element
	 * @param timeout
	 * @return true if element is displayed else false
	 */
	public static boolean visibilityOf(WebElement element, int timeout) {
		return new WebDriverWait(driver, timeout).until(ExpectedConditions.visibilityOf(element)).isDisplayed();
	}

	/**
	 * wait until the visibility of web element then click on the web element
	 * 
	 * @param element
	 * @param timeout
	 */
	public static void clickOn(WebElement element, int timeout) {
		new WebDriverWait(driver, timeout).until(ExpectedConditions.elementToBeClickable(element)).click();
	}

}
