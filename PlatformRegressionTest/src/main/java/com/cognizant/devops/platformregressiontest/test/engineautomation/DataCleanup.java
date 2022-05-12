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

public class DataCleanup extends DataCleanupObjectrepository {

	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
	private static final Logger log = LogManager.getLogger(DataCleanup.class);

	public DataCleanup() {
		PageFactory.initElements(driver, this);
	}

	public boolean deleteAgent() throws InterruptedException {
		selectAgent(LoginAndSelectModule.testData.get("agentId"));
		wait.until(ExpectedConditions.visibilityOf(stopAgent)).click();
		wait.until(ExpectedConditions.visibilityOf(ok)).click();
		selectAgent(LoginAndSelectModule.testData.get("agentId"));
		wait.until(ExpectedConditions.visibilityOf(deleteAgent)).click();
		wait.until(ExpectedConditions.elementToBeClickable(yes)).click();
		Thread.sleep(1000);
		if (!selectAgent(LoginAndSelectModule.testData.get("agentId"))) {
			log.info("Agent deleted successfully.");
			return true;
		}
		return false;
	}

	public boolean selectAgent(String agentName) {
		boolean agentPresent = false;
		try {
			driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
			wait.until(ExpectedConditions.visibilityOfAllElements(agentsList));
			for (int i = 0; i < agentsList.size(); i++) {
				if (agentsList.get(i).getText().equals(agentName)) {
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
}
