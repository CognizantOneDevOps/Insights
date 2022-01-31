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
package com.cognizant.devops.platformregressiontest.test.ui.datadictionary;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

public class DataDictionaryConfiguration extends DataDictionaryobjectRepository {
	WebDriverWait wait = new WebDriverWait(driver, 20);

	private Set<String> uniqueToolNames = new HashSet<>();
	private static final Logger log = LogManager.getLogger(DataDictionaryConfiguration.class);

	public DataDictionaryConfiguration() {
		PageFactory.initElements(driver, this);
	}

	/**
	 * checks whether landing page is displayed or not
	 * 
	 * @return true if landing page is displayed o/w false
	 * @throws InterruptedException
	 */
	public boolean navigateToDataDictionaryLandingPage() {
		log.info("Landing page displayed : {}", landingPage.isDisplayed());
		return landingPage.isDisplayed();
	}

	/**
	 * Checks whether relationship name is displayed for particular source and
	 * destination tool
	 * 
	 * @return true if relationship name is displayed o/w false
	 * @throws InterruptedException
	 */
	public boolean checkDataDictionaryFunctionality() throws InterruptedException {
		fillData();
		Thread.sleep(1000);
		showCorrelation.click();
		try {
			Thread.sleep(1000);
			if (driver.getPageSource().contains("Relationship Name")) {
				log.info("Relationship between source and destination tool is displayed");
				return true;
			}
		} catch (Exception e) {
			log.info("Relationship between source and destination tool is not displayed");
			throw new SkipException("Skipping test case as no relationship found between source and destination tool");
		}
		return false;
	}

	/**
	 * to fill the required field to save correlation
	 * 
	 * @return true if all fields were filled successfully o/w false
	 * @throws InterruptedException
	 */
	private boolean fillData() throws InterruptedException {
		if (!selectSourceTool(LoginAndSelectModule.testData.get("sourceTool")))
			return false;
		selectSourceLabel.sendKeys(LoginAndSelectModule.testData.get("sourceLabel"));
		Thread.sleep(1000);
		if (!selectDestinationTool(LoginAndSelectModule.testData.get("destinationTool")))
			return false;
		selectDestinationLabel.sendKeys(LoginAndSelectModule.testData.get("destinationLabel"));
		return true;
	}

	/**
	 * Fetch unique tool names which are registered under AgentManagement and
	 * WebhookConfiguration module, click on source tool and compare the fetched
	 * unique tool with the list of source tools, if matching then click that tool
	 * name
	 * 
	 * @param sourceTool
	 * @return true if source tool clicked successfully o/w false
	 * @throws InterruptedException
	 */
	private boolean selectSourceTool(String sourceTool) throws InterruptedException {
		clickOn(selectSourceTool, 2);
		visibilityOfAllElements(sourceToolList, 2);
		log.info("UniqueToolSize : {}, sourceToolListSize : {}", uniqueToolNames.size(), sourceToolList.size());
		if (sourceToolList.size() == uniqueToolNames.size()) {
			for (WebElement sourceToolName : sourceToolList) {
				visibilityOf(sourceToolName, 2);
				if ((sourceToolName.getText()).equals(sourceTool)) {
					clickOn(sourceToolName, 2);
					log.info(
							"All the tools which are registered by using Agent Management and Webhook Configuration module are displayed and clicked the source tool successfully.");
					break;
				}
			}
			return true;
		} else {
			log.info("Source Tool didn't load properly");
			return false;
		}
	}

	/**
	 * select the destination tool on UI
	 * 
	 * @param destinationTool
	 * @return true if destination tool clicked successfully o/w false
	 * @throws InterruptedException
	 */
	private boolean selectDestinationTool(String destinationTool) throws InterruptedException {
		clickOn(selectDestinationTool, 2);
		visibilityOfAllElements(destinationToolList, 2);
		if (destinationToolList.size() == uniqueToolNames.size()) {
			for (WebElement destinationToolName : destinationToolList) {
				visibilityOf(destinationToolName, 2);
				if ((destinationToolName.getText()).equals(destinationTool)) {
					Thread.sleep(1000);
					clickOn(destinationToolName, 2);
					log.info(
							"All the tools which are registered by using Agent Management and Webhook Configuration module are displayed and clicked the destination tool successfully.");
					break;
				}
			}
			return true;
		} else {
			log.info("Destination Tool didn't load properly");
			return false;
		}
	}

	/**
	 * select the module name that are present in the left side list of UI
	 * 
	 * @param moduleName
	 * @throws InterruptedException
	 */
	private void selectModule(String moduleName) throws InterruptedException {
		List<WebElement> menuList = driver.findElements(By.xpath("//p[contains(@class,'line-child')]"));
		for (WebElement requiredOption : menuList) {
			if (requiredOption.getText().equals(moduleName)) {
				requiredOption.click();
				break;
			}
		}
		Thread.sleep(100);

	}

	public void getAgentWebhookLabels() throws InterruptedException {
		selectModule(LoginAndSelectModule.testData.get("agentManagement"));
		driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
		try {
			visibilityOfAllElements(toolNameList, 1);
			for (WebElement toolName : toolNameList) {
				visibilityOf(toolName, 1);
				uniqueToolNames.add(toolName.getText().toUpperCase());
			}
		} catch (Exception ex) {
			log.info("No agents found under Agent Management module.");
		}
		selectModule(LoginAndSelectModule.testData.get("webhookConfiguration"));
		try {
			visibilityOfAllElements(toolNameList, 1);
			for (WebElement toolName : toolNameList) {
				visibilityOf(toolName, 1);
				uniqueToolNames.add(toolName.getText().toUpperCase());
			}
		} catch (Exception ex) {
			log.info("No webhook found under Webhook Configuration module.");
		}
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		selectModule(LoginAndSelectModule.testData.get("dataDictionary"));
	}

	/**
	 * wait until the visibility of list of web elements
	 * 
	 * @param element
	 * @return size of list of web elements
	 */
	public static int visibilityOfAllElements(List<WebElement> element, int timeout) {
		new WebDriverWait(driver, timeout).until(ExpectedConditions.visibilityOfAllElements(element));
		return element.size();
	}

	/**
	 * wait until the visibility of web element
	 * 
	 * @param element
	 * @param timeout
	 * @return true if element is displayed else false
	 */
	private boolean visibilityOf(WebElement element, int timeout) {
		new WebDriverWait(driver, timeout).until(ExpectedConditions.visibilityOf(element));
		return element.isDisplayed();
	}

	/**
	 * wait until the visibility of web element then click on the web element
	 * 
	 * @param element
	 * @param timeout
	 */
	public static void clickOn(WebElement element, int timeout) {
		new WebDriverWait(driver, timeout).until(ExpectedConditions.visibilityOf(element)).click();
	}
}
