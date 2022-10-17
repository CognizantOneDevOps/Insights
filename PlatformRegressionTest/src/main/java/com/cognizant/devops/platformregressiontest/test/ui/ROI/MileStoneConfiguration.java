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
package com.cognizant.devops.platformregressiontest.test.ui.ROI;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.SkipException;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

public class MileStoneConfiguration extends MileStoneObjectRepository {

	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

	private static final Logger log = LogManager.getLogger(MileStoneConfiguration.class);
	
	Date date = new Date();
	
	LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

	public MileStoneConfiguration() {
		PageFactory.initElements(driver, this);
	}

	public boolean navigateToMilestoneConfig() {
		return landingPage.isDisplayed();
	}

	public boolean saveMileStone() {
		try {
			wait.until(ExpectedConditions.elementToBeClickable(clickAddButton));
			clickAddButton.click();
			fillData(LoginAndSelectModule.testData.get("mileStoneName"),LoginAndSelectModule.testData.get("releaseID")
					,LoginAndSelectModule.testData.get("outcome"));
			Thread.sleep(2000);
			wait.until(ExpectedConditions.elementToBeClickable(save));
			save.click();
			wait.until(ExpectedConditions.elementToBeClickable(yesButton));
			yesButton.click();
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
			if (successMessage.isDisplayed()) {
				log.info("saved successfully");
				crossClose.click();
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}
	

	private void fillData(String milestoneName,String releaseID,String outcome) {
		try {
			mileStoneName.sendKeys(milestoneName);
			milestoneReleaseID.sendKeys(releaseID);
			startDateCalendar.click();
			Thread.sleep(1000);
			selectYear(String.valueOf(localDate.getYear()));
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
			selectMonth(new SimpleDateFormat("MMM").format(new Date()));
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
			selectDate(String.valueOf(localDate.getDayOfMonth() + 1));
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
			endDateCalendar.click();
			selectYear(String.valueOf(localDate.getYear()+1));
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
			selectMonth(new SimpleDateFormat("MMM").format(new Date()));
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
			selectDate(String.valueOf(localDate.getDayOfMonth()));
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
			chooseOutcome.click();
			searchOutcome.sendKeys(outcome);
			driver.findElement(
					By.xpath("//tr/td[contains(text(), '" +outcome+ "')]//preceding-sibling::td[2]//span[contains(@class, 'mat-checkbox')]")).click();
			wait.until(ExpectedConditions.elementToBeClickable(okButton));
			okButton.click();
		}
			catch (Exception e) {
				log.info(e.getMessage());
			}
		
		
	}
	
	public boolean validateMilestoneDetail() {
		try {
			selectMilestone();
			driver.findElement(
					By.xpath("//tr/td[contains(text(), '" +LoginAndSelectModule.testData.get("mileStoneName")+ "')]//following-sibling::td[5]//mat-icon[@svgicon='healthcheck_show_details']")).click();
			outcomeTable.isDisplayed();
			wait.until(ExpectedConditions.elementToBeClickable(dialogClose));
			dialogClose.click();
			return true;
			
		} catch (Exception e) {
			dialogClose.click();
			return false;
		}
	}

	public boolean deleteMileStone() {
		try {
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
			selectMilestone();
			wait.until(ExpectedConditions.elementToBeClickable(clickDelete));
			clickDelete.click();
			wait.until(ExpectedConditions.elementToBeClickable(yesButton));
			yesButton.click();
			if (deletedMessage.isDisplayed()) {
				log.info("deleted successfully");
				crossClose.click();
				return true;
			}
			if(milestoneDeleteIssue.isDisplayed()) {
				Thread.sleep(2000);
				crossClose.click();
				throw new SkipException("Milestone is either COMPLETED or is in IN_PROGRESS state.");
			}
			return false;
		} catch (InterruptedException e) {
			
			return false;
		}
	}

	public boolean editMileStone() {
		try {
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
			selectMilestone();
			wait.until(ExpectedConditions.elementToBeClickable(clickEditButton));
			clickEditButton.click();
			milestoneReleaseID.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
			milestoneReleaseID.sendKeys(LoginAndSelectModule.testData.get("updateReleaseID"));
			wait.until(ExpectedConditions.elementToBeClickable(save));
			save.click();
			wait.until(ExpectedConditions.elementToBeClickable(yesButton));
			yesButton.click();
			if (updateSuccessMessage.isDisplayed()) {
				log.info("updates successfully");
				wait.until(ExpectedConditions.elementToBeClickable(crossClose));
				crossClose.click();
				return true;
			}
			return false;
		} catch (InterruptedException e) {
			return false;
		}
	}
	
	public boolean saveWithInvalideData() {
		try {
			wait.until(ExpectedConditions.elementToBeClickable(clickAddButton));
			clickAddButton.click();
			fillData(LoginAndSelectModule.testData.get("invalideMileStoneName"),LoginAndSelectModule.testData.get("invalideReleaseID")
					,LoginAndSelectModule.testData.get("outcome"));
			if(saveBtn.isEnabled())
			{
				backButton.click();
				return false;
			}
			else {
				milestoneReleaseID.sendKeys(LoginAndSelectModule.testData.get("releaseID"));
				endDateCalendar.click();
				selectYear(String.valueOf(localDate.getYear()-1));
				driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
				selectMonth(new SimpleDateFormat("MMM").format(new Date()));
				driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
				selectDate(String.valueOf(localDate.getDayOfMonth()));
				Thread.sleep(2000);
				wait.until(ExpectedConditions.elementToBeClickable(save));
				save.click();
				wait.until(ExpectedConditions.elementToBeClickable(yesButton));
				yesButton.click();
				driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
				if (errorMessage.isDisplayed()) {
					crossClose.click();
					backButton.click();
					return true;
				}
				backButton.click();
				return false;
			}
		}
		  catch (Exception e) {
			  crossClose.click();
			  backButton.click();
			  return true;
	}
	}

	public void selectMilestone() throws InterruptedException {
		Thread.sleep(1000);
		for (int i = 0; i < mileStoneNameList.size(); i++) {
			if (mileStoneNameList.get(i).getText().equals(LoginAndSelectModule.testData.get("mileStoneName"))) {
				List<WebElement> deleteButtons = mileStoneNameList.get(i)
						.findElements(By.xpath(".//preceding::span[contains(@class, 'mat-radio-container')]"));
				driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
				deleteButtons.get(i).click();
				break;
			}
		}

	}

	public void selectYear(String year) {
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
		selectYearArrowButton.click();
		List<WebElement> requiredYear = yearList;
		for (WebElement yearSelected : requiredYear) {
			if (yearSelected.getText().equals(year)) {
				yearSelected.click();
				break;
			}
		}

	}

	public void selectMonth(String month) {
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
		List<WebElement> requiredMonth = monthList;
		for (WebElement selectedMonth : requiredMonth) {
			if (selectedMonth.getText().equalsIgnoreCase(month)) {
				selectedMonth.click();
				break;
			}
		}

	}

	public void selectDate(String date) {
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
		List<WebElement> requiredDate = dateList;
		for (WebElement selectedDate : requiredDate) {
			if (selectedDate.getText().equals(date)) {
				selectedDate.click();
				break;
			}
		}
	}



	

}
