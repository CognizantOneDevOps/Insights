/*******************************************************************************
 * Copyright 2022 Cognizant Technology Solutions
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
package com.cognizant.devops.platformregressiontest.test.ui.scheduletask;

import java.time.Duration;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

public class ScheduleTaskConfiguration extends ScheduleTaskObjectRepository {

	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

	private static final Logger log = LogManager.getLogger(ScheduleTaskConfiguration.class);

	public ScheduleTaskConfiguration() {
		PageFactory.initElements(driver, this);
	}

	public boolean navigateToScheduleTaskManagement() {
		return landingPage.isDisplayed();
	}

	public boolean addScheduleTask() {
		try {
			wait.until(ExpectedConditions.elementToBeClickable(clickAddButton));
			clickAddButton.click();
			taskName.isDisplayed();
			taskName.sendKeys(LoginAndSelectModule.testData.get("taskName"));
			description.sendKeys(LoginAndSelectModule.testData.get("description"));
			schedule.sendKeys(LoginAndSelectModule.testData.get("schedule"));
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
			wait.until(ExpectedConditions.elementToBeClickable(save));
			save.click();
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
			if (successMessage.isDisplayed()) {
				log.info("saved successfully");
				driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
				crossClose.click();
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean selectTask() throws InterruptedException {
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
		for (int i = 0; i < taskList.size(); i++) {
			if (taskList.get(i).getText().equals(LoginAndSelectModule.testData.get("taskName"))) {
				List<WebElement> deleteButtons = taskList.get(i)
						.findElements(By.xpath(".//preceding::span[contains(@class, 'mat-radio-container')]"));
				deleteButtons.get(i).click();
				driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
				return true;
			}
		}
		if (nextPage.isEnabled()) {
			nextPage.click();
			selectTask();
		}
		return true;
	}

	public boolean editScheduleTask() {
		try {
			selectTask();
			wait.until(ExpectedConditions.elementToBeClickable(clickEditButton));
			clickEditButton.click();
			schedule.clear();
			schedule.sendKeys(LoginAndSelectModule.testData.get("updatedSchedule"));
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
			wait.until(ExpectedConditions.elementToBeClickable(save));
			save.click();
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
			if (successMessage.isDisplayed()) {
				log.info("updated successfully");
				driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
				crossClose.click();
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean validateStop() {
		try {
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
			selectTask();
			wait.until(ExpectedConditions.elementToBeClickable(clickStop));
			clickStop.click();
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
			if (stopMessage.isDisplayed()) {
				log.info("stoped successfully");
				driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
				crossClose.click();
				return true;
			}
			return false;
		} catch (Exception e) {
			log.info(e.getMessage());
			return false;
		}
	}

	public boolean validateStart() {
		try {
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
			selectTask();
			wait.until(ExpectedConditions.elementToBeClickable(clickStart));
			clickStart.click();
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
			if (startMessage.isDisplayed()) {
				log.info("started successfully");
				driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
				crossClose.click();
				return true;
			}
			return false;
		} catch (Exception e) {
			log.info(e.getMessage());
			return false;
		}
	}

	public boolean deleteTask() {
		try {
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
			selectTask();
			wait.until(ExpectedConditions.elementToBeClickable(clickStop));
			clickStop.click();
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
			crossClose.click();
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
			selectTask();
			wait.until(ExpectedConditions.elementToBeClickable(clickDelete));
			clickDelete.click();
			wait.until(ExpectedConditions.elementToBeClickable(yesButton));
			yesButton.click();
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
			if (deletedMessage.isDisplayed()) {
				log.info("deleted successfully");
				driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
				crossClose.click();
				return true;
			}
			return false;
		} catch (InterruptedException e) {
			return false;
		}
	}

	public boolean validateExecutionHistory() {	
		try {
			selectTask();
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
			Thread.sleep(3000);
			driver.findElement(
					By.xpath("//tr/td[contains(text(), '" +LoginAndSelectModule.testData.get("taskName")+ "')]//following-sibling::td[5]//mat-icon[@svgicon='healthcheck_show_details']")).click();

			historyDialogBox.isDisplayed();
			wait.until(ExpectedConditions.elementToBeClickable(dialogClose));
			dialogClose.click();
			return true;
		} catch (Exception e) {
			log.info(e.getMessage());
			return false;
		}
	}

	public boolean addInvalideScheduleTask() {
		try {
			wait.until(ExpectedConditions.elementToBeClickable(clickAddButton));
			clickAddButton.click();
			taskName.isDisplayed();
			taskName.sendKeys(LoginAndSelectModule.testData.get("invalideTaskName"));
			description.sendKeys(LoginAndSelectModule.testData.get("description"));
			schedule.sendKeys(LoginAndSelectModule.testData.get("schedule"));
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
			wait.until(ExpectedConditions.elementToBeClickable(save));
			save.click();
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
			if (invalideMessage.isDisplayed()) {
				log.info("saved invalideMessage");
				Thread.sleep(1000);
				crossClose.click();
				backButton.click();
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

}
