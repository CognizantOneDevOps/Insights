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

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

public class TraceabilityDashboardConfiguration extends TraceabilityDashboardObjectRepository {

	WebDriverWait wait = new WebDriverWait(driver, 20);

	private static final Logger log = LogManager.getLogger(TraceabilityDashboardConfiguration.class);

	public TraceabilityDashboardConfiguration() {
		PageFactory.initElements(driver, this);
	}

	/**
	 * checks whether landing page is displayed or not
	 * 
	 * @return true if landing page is displayed o/w false
	 */
	public boolean navigateToTraceabilityLandingPage() {
		log.info("Landing page displayed : {}", landingPage.isDisplayed());
		return landingPage.isDisplayed();
	}

	public boolean validateTraceabilityMatrix() throws InterruptedException {
		selectFromDropDown(selectTool, LoginAndSelectModule.testData.get("toolName"));
		selectFromDropDown(selectField, LoginAndSelectModule.testData.get("field"));
		selectFromDropDown(issueType, LoginAndSelectModule.testData.get("issueType"));
		toolFieldValue.sendKeys(LoginAndSelectModule.testData.get("fieldValue"));
		searchButton.click();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		wait.until(ExpectedConditions.elementToBeClickable(summaryTab));
		if (summaryTab.isDisplayed() && pipelineTab.isDisplayed()) {
			log.info("search successful");
			pipelineTab.click();
			JavascriptExecutor executor = (JavascriptExecutor)driver;
			executor.executeScript("arguments[0].click();", moreInfo);
			wait.until(ExpectedConditions.elementToBeClickable(additionalDetails));
			try {
				if (additionalDetails.isDisplayed()) {
					closeDialog.click();
					log.info("additional details are displayed");
					return true;
				}
			} catch (Exception e) {
				log.info("additional details are not displayed");
				return false;
			}
		}
		log.info("search unsuccessful");
		return false;
	}

	public boolean clearFunctionality() {
		clearButton.click();
		try {
			if (summaryTab.isDisplayed() || pipelineTab.isDisplayed()) {
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
		selectFromDropDown(selectField, LoginAndSelectModule.testData.get("field"));
		selectFromDropDown(issueType, LoginAndSelectModule.testData.get("issueType"));
		toolFieldValue.sendKeys(LoginAndSelectModule.testData.get("incorrectFieldValue"));
		searchButton.click();
		wait.until(ExpectedConditions.visibilityOf(errorMessage));
		if (errorMessage.isDisplayed()) {
			log.info("error message is displayed for incorrect search");
			okButton.click();
			return true;
		}
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
