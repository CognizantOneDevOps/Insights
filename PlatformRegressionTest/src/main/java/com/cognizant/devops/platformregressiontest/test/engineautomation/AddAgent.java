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

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.cognizant.devops.platformcommons.mq.core.RabbitMQConnectionProvider;
import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;


public class AddAgent extends AddAgentObjectRepository{

	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
	WebDriverWait hold = new WebDriverWait(driver, Duration.ofSeconds(1));
	Map<String, String> testData = new HashMap<>();
	private static final Logger log = LogManager.getLogger(AddAgent.class);

	public AddAgent() {
		PageFactory.initElements(driver, this);
	}

	public boolean verifyServerConfigAgentDetailBlock() {
		selectModuleUnderConfiguration("Server Configuration");
		try {
			JavascriptExecutor js = (JavascriptExecutor) driver;
			js.executeScript("arguments[0].scrollIntoView();", agentConfigurationHeading);
			if (visibilityOf(agentConfigurationHeading, 10)
					&& checkIsOnlineRegistration(LoginAndSelectModule.testData.get("isOnlineRegistration"))
					&& checkBrowseRepoUrl(LoginAndSelectModule.testData.get("browseRepoUrl"))
					&& checkDownloadRepoUrl(LoginAndSelectModule.testData.get("downloadRepoUrl"))
					&& checkOnlineRegistrationMode(LoginAndSelectModule.testData.get("onlineRegistrationMode"))
					&& checkOfflineAgentPath(LoginAndSelectModule.testData.get("offlineAgentPath"))
					&& checkUnzipPath(LoginAndSelectModule.testData.get("unzipPath"))
					&& checkAgentExchange(LoginAndSelectModule.testData.get("agentExchange"))
					&& checkAgentPkgQueue(LoginAndSelectModule.testData.get("agentPkgQueue"))) {
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

	public boolean navigateToAgentManagementLandingPage() throws InterruptedException {
		selectModuleOnClickingConfig("Agent Management");
		log.info("Landing page is displayed.");
		return landingPage.isDisplayed();
	}

	public boolean registerAgent() throws InterruptedException {
		if (verifyAgentId(LoginAndSelectModule.testData.get("agentId"))) {
			log.info("Agent id already exists.");
			return false;
		}
		visibilityOf(clickAddButton, 10);
		clickOn(clickAddButton, 10);
		Thread.sleep(5000);
		selectOs.sendKeys(LoginAndSelectModule.testData.get("osName"));
		selectAgentType.sendKeys(LoginAndSelectModule.testData.get("typeName"));
		Thread.sleep(5000);
		selectVersion.sendKeys(LoginAndSelectModule.testData.get("version"));
		selectTools.sendKeys(LoginAndSelectModule.testData.get("toolName"));
		visibilityOf(mqUser, 10);
		mqUser.clear();
		mqUser.sendKeys(LoginAndSelectModule.testData.get("mqUser"));
		visibilityOf(mqPassword, 10);
		mqPassword.clear();
		mqPassword.sendKeys(LoginAndSelectModule.testData.get("mqPassword"));
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].scrollIntoView();", agentIdValue);
		agentIdValue.sendKeys(LoginAndSelectModule.testData.get("agentId"));
		wait.until(ExpectedConditions.visibilityOf(addAgent)).click();
		success.isDisplayed();
		log.info(successMsg.getText());
		wait.until(ExpectedConditions.visibilityOf(ok)).click();
		return verifyAgentId(LoginAndSelectModule.testData.get("agentId"));
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

	public boolean checkDaemonagentService() throws Exception {
		return isProcessRunning("DaemonAgent");
	}

	public boolean checkAgentService() throws Exception {
		Thread.sleep(1000);
		return isProcessRunning(LoginAndSelectModule.testData.get("agentId"));
	}

	public boolean isProcessRunning(String processName) throws Exception {
		final Process process = Runtime.getRuntime().exec("net start");
		try (Scanner reader = new Scanner(process.getInputStream(), "UTF-8")) {
			while (reader.hasNextLine()) {
				String nextline = reader.nextLine();
				if (nextline.contains(processName))
					return true;
				else
					continue;
			}
			reader.close();
		}
		return false;
	}

	public boolean checkEngineService() throws Exception {
		return isProcessRunning("InSightsEngine");
	}

	public boolean checkTrackingFile() {
		File trackingFile = new File(LoginAndSelectModule.testData.get("trackingJson"));
		if (trackingFile.exists() && !trackingFile.isDirectory()) {
			return true;
		}
		return false;
	}

	public boolean checkQueue() throws Exception {
		if (publishMessage(LoginAndSelectModule.testData.get("healthQueueName"))
				&& publishMessage(LoginAndSelectModule.testData.get("dataQueueName"))) {
			return true;
		}
		return false;
	}

	public static boolean publishMessage(String queueName) throws Exception {
		Connection connection = null;
		Channel channel = null;
		try {
			connection = RabbitMQConnectionProvider.getConnection();
			channel = connection.createChannel();
			channel.queueDeclarePassive(queueName);
		} catch (Exception e) {
			log.info("{} queue does not exist", queueName);
			return false;
		}
		log.info("{} queue exist", queueName);
		return true;
	}

	/**
	 * wait until the visibility of list of web elements
	 * 
	 * @param element
	 * @return size of list of web elements
	 */
	public static int visibilityOfAllElements(List<WebElement> element) {
		return new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.visibilityOfAllElements(element)).size();
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
