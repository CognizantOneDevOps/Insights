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
package com.cognizant.devops.platformregressiontest.test.ui.traceabilitydashboard;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;
import com.cognizant.devops.platformregressiontest.test.ui.testdatamodel.TraceabilityDataModel;

public class TraceabilityDashboardConfiguration extends TraceabilityDashboardObjectRepository {

	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

	private static final Logger log = LogManager.getLogger(TraceabilityDashboardConfiguration.class);

	public TraceabilityDashboardConfiguration() {
		PageFactory.initElements(driver, this);
	}

	boolean isValidCount = false;

	/**
	 * checks whether landing page is displayed or not
	 * 
	 * @return true if landing page is displayed o/w false
	 */
	public boolean navigateToTraceabilityLandingPage() {
		log.info("Landing page displayed : {}", landingPage.isDisplayed());
		return landingPage.isDisplayed();
	}

	/**
	 * validates the functioning of Traceability Dashboard
	 * 
	 * @return true if the dashboard is working fine
	 */
	public boolean validateTraceabilityMatrix(TraceabilityDataModel data) throws InterruptedException {
		Thread.sleep(500);
		selectFromDropDown(selectTool, data.getToolName());
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
		selectFromDropDown(selectField, data.getField());
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
		selectFromDropDown(issueType, data.getIssueType());
		toolFieldValue.sendKeys(data.getFieldValue());
		searchButton.click();
		Thread.sleep(1000);
		wait.until(ExpectedConditions.elementToBeClickable(summaryTab));
		if (isSummaryAvailable() && pipelineTab.isDisplayed()) {
			log.info("Summary section working successfully");
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
			pipelineTab.click();
			if (!checkPipelineTab.isDisplayed()) {		// checking if the pipeline tab is displayed or not
				log.info("Failed to display the Pipeline Tab.");
				return false;
			}
			if(!validateImage(data.getToolName()))      //validating the image of the Tool
				return false;
			clearButton.click();
			return true;
		}
		log.info("search unsuccessful");
		return false;
	}

	/**
	 * checks whether landing page is displayed or not
	 * 
	 * @return true if landing page is displayed o/w false
	 */
	public boolean checkCount() throws InterruptedException {
		try {
			wait.until(ExpectedConditions.elementToBeClickable(summaryTab));
			Thread.sleep(500);
			summaryTab.click();
			List<WebElement> listOfTools = count;
			for (WebElement toolCount : listOfTools) {
				if (toolCount.getText().equals(LoginAndSelectModule.testData.get("gitCount"))) {
					this.isValidCount = true;
					log.info("Count Valided Successfully");
				}
			}
			return isValidCount;
		} catch (Exception e) {
			log.info("additional details are not displayed");
			return false;
		}
	}
	
	public boolean validateImage(String toolName) throws InterruptedException {
		try {
			if(toolName.equalsIgnoreCase("jira")) {
				imgJira.isDisplayed();
				return true;
			}
			else {
				imgGit.isDisplayed();
				return true;
			}
		} catch (Exception e) {
			log.info("image is not displayed");
			return false;
		}
	}

	public boolean checkMoreDetails(String toolName) throws InterruptedException {
		JavascriptExecutor executor = (JavascriptExecutor) driver;
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
		if (toolName.equalsIgnoreCase("jira"))
			jiraInfo.click();
		// executor.executeScript("arguments[0].click();", jiraInfo);
		else
			moreInfo.click();
		wait.until(ExpectedConditions.elementToBeClickable(additionalDetails));
		try {
			if (additionalDetails.isDisplayed()) {
				driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
				closeDialog.click();
				log.info("additional details are displayed");
				return true;
			}
		} catch (Exception e) {
			log.info("additional details are not displayed");
			return false;
		}
		log.info("search unsuccessful");
		return false;
	}

	public boolean checkIssues(TraceabilityDataModel data) throws InterruptedException {
		try {
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
			if (data.getToolName().equalsIgnoreCase("jira")) {
				viewJiraTool.click();
				searchUser.sendKeys(data.getIssueKey());
				if (searchResult.isDisplayed()) {
					driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
					closeDialog.click();
					return true;
				}
			} else {
				viewGitTool.click();
				searchUser.sendKeys(data.getSearchUser());
				if (searchResult.isDisplayed()) {
					driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
					closeDialog.click();
					return true;
				}
			}
			return false;
		} catch (Exception ex) {
			log.info("Failed to check Issues functionality");
			return false;
		}
	}

	public boolean clearFunctionality() throws InterruptedException {
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
		clearButton.click();
		try {
			if (checkSummaryTab.isDisplayed() || additionalDetails.isDisplayed()) {
				log.info("clear functionality failed");
				return false;
			}
		} catch (Exception ex) {
			log.info("clear functionality successful");
			return true;
		}

		return false;
	}

	public boolean incorrectSearch() throws InterruptedException {
		selectFromDropDown(selectTool, LoginAndSelectModule.testData.get("toolName"));
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
		selectFromDropDown(selectField, LoginAndSelectModule.testData.get("field"));
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
		selectFromDropDown(issueType, LoginAndSelectModule.testData.get("issueType"));
		toolFieldValue.sendKeys(LoginAndSelectModule.testData.get("incorrectFieldValue"));
		searchButton.click();
		wait.until(ExpectedConditions.visibilityOf(crossClose));
		if (crossClose.isDisplayed()) {
			log.info("error message is displayed for incorrect search");
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
			crossClose.click();
			return true;
		}
		return false;
	}
	
	public boolean isSummaryAvailable() {
		if (checkSummaryTab.isDisplayed())
			return true;
		else 
			return false;
	}

	public void selectFromDropDown(WebElement selectDropdown, String name) throws InterruptedException {
		selectDropdown.click();
		Thread.sleep(1000);
		for (WebElement role : dropdownList) {
			if ((role.getText()).equals(name)) {
				wait.until(ExpectedConditions.elementToBeClickable(role));
				role.click();
				break;
			}
		}
	}

}
