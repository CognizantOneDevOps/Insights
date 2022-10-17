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
package com.cognizant.devops.platformregressiontest.test.ui.reportmanagement;


import java.time.Duration;
import java.util.List;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import org.openqa.selenium.support.PageFactory;
import org.testng.SkipException;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;


public class AssessmentReportConfiguration extends AssessmentReportObjectRepository {

	private static final Logger log = LogManager.getLogger(AssessmentReportConfiguration.class);

	static String reportTemplateName = LoginAndSelectModule.testData.get("sprintScoreCardTemplate");
	String frequencyName = LoginAndSelectModule.testData.get("frequencyName");
	String new_frequency = LoginAndSelectModule.testData.get("frequencyName2");
	String year = LoginAndSelectModule.testData.get("year");
	String month = LoginAndSelectModule.testData.get("month");
	String date = LoginAndSelectModule.testData.get("date");

	public AssessmentReportConfiguration() {
		PageFactory.initElements(driver, this);
	}

	public void clickAddButton() {

		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
		addButton.click();
	}

	public boolean navigateToaddReportConfirmation() {
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
		return saveReportConfirmationMessage.isDisplayed();

	}

	public boolean navigateToaddReportSuccess() {

		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
		return successMessage.isDisplayed();
	}

	public boolean navigateToReportsLandingPage() {

		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
		return reportsLandingPage.isDisplayed();
	}

	public boolean addNewReport() {
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
		clickAddButton();
		reportName.sendKeys(LoginAndSelectModule.testData.get("reportName"));
		titleName.sendKeys(LoginAndSelectModule.testData.get("title"));
		try {
			selectReportTemplate(reportTemplateName);
			Thread.sleep(2000);
			if (((frequencyName).equals(LoginAndSelectModule.testData.get("oneTimeFrequency")))
					|| ((frequencyName).equals(LoginAndSelectModule.testData.get("BiWeeklyFrequency")))
					|| ((frequencyName).equals(LoginAndSelectModule.testData.get("triWeeklyFrequency")))) {
				selectFrequency(frequencyName);
				Thread.sleep(2000);
				clickCalender();
				Thread.sleep(2000);
				selectYear(year);
				Thread.sleep(2000);
				selectMonth(month);
				Thread.sleep(2000);
				selectDate(date);
				Thread.sleep(2000);
			} else {
				selectFrequency(frequencyName);
			}
			Thread.sleep(2000);
			dragAndDropTask();
			addMaildetails();
			Thread.sleep(2000);
			saveButton.click();
			navigateToaddReportConfirmation();
			clickYesButton();
			Thread.sleep(100);
			navigateToaddReportSuccess();
			crossClose.click();

		} catch (Exception e) {

			log.error("Please fill the mendatory fields", e);
		}

		return verifyReportName(LoginAndSelectModule.testData.get("reportName"));
	}
	
	public boolean createDuplicateReport() {
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
		clickAddButton();
		reportName.sendKeys(LoginAndSelectModule.testData.get("reportName"));
		titleName.sendKeys(LoginAndSelectModule.testData.get("title2"));
		try {
			selectReportTemplate(reportTemplateName);
			Thread.sleep(2000);
			if (((new_frequency).equals(LoginAndSelectModule.testData.get("oneTimeFrequency")))
					|| ((new_frequency).equals(LoginAndSelectModule.testData.get("BiWeeklyFrequency")))
					|| ((new_frequency).equals(LoginAndSelectModule.testData.get("triWeeklyFrequency")))) {
				selectFrequency(new_frequency);
				Thread.sleep(2000);
				clickCalender();
				Thread.sleep(2000);
				selectYear(year);
				Thread.sleep(2000);
				selectMonth(month);
				Thread.sleep(2000);
				selectDate(date);
				Thread.sleep(2000);
			} else {
				selectFrequency(frequencyName);
			}
			Thread.sleep(2000);
			dragAndDropTask();
			addMaildetails();
			Thread.sleep(2000);
			saveButton.click();
			navigateToaddReportConfirmation();
			clickYesButton();
			if (alreadyExists.isDisplayed()) {
				log.info("Report with similar name already exists.");
				Thread.sleep(2000);
				crossClose.click();
				backButton.click();
				return true;
			}
			Thread.sleep(100);
			navigateToaddReportSuccess();
			crossClose.click();

		} catch (Exception e) {

			log.error("Please fill the mendatory fields", e);
		}

		return verifyReportName(LoginAndSelectModule.testData.get("reportName"));
	}

	public boolean immediateReportRun() throws InterruptedException {

		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
		selectReport();
		immiediateRunButton.click();
		immediateRunConfirmMessage.isDisplayed();
		immediateRunClickYes.click();
		immediateRunSuccess.isDisplayed();
		immediateRunOk.click();
		return reportsLandingPage.isDisplayed();
	}

	public boolean inActiveReport() throws InterruptedException {

		selectReport();
		Thread.sleep(2000);
		clickToggleButton();
		confirmationMessage.click();
		Thread.sleep(2000);
		clickYes.click();
		return reportsLandingPage.isDisplayed();

	}

	public boolean editReport() throws InterruptedException {
        Thread.sleep(2000);
		selectReport();
		clickEditButton.click();
		addTaskButton.click();
		Thread.sleep(2000);
		WebElement emailExecutionTaskSource = driver.findElement(By.xpath("(//div[@id='fromSelectedTaskList'])[3]"));
		WebElement taskExecutionDestination = driver.findElement(By.xpath("(//div[@class='containerCls'])[1]"));
		Thread.sleep(2000);
		Actions act = new Actions(driver);
		act.dragAndDrop(emailExecutionTaskSource, taskExecutionDestination).build().perform();
		Thread.sleep(2000);
		for (int i = 0; i <= 2; i++) {
			try {
				taskaddButton.click();
				break;
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}

		Thread.sleep(2000);
		saveButton.click();
		navigateToaddReportConfirmation();
		clickYesButton();
		navigateToaddReportSuccess();
		crossClose.click();
		return reportsLandingPage.isDisplayed();
	}

	public boolean deleteReport() throws InterruptedException {
	    try {
	    Thread.sleep(1000);
		selectReport();
		clickDelete.click();
		clickYesButton();
		crossClose.click();
		return reportsLandingPage.isDisplayed();
	    }
	    catch (Exception e) {
			throw new SkipException("Unable to delete please check logs");
	    }
	}

	private void clickYesButton() {
		for (int i = 0; i <= 3; i++) {
			try {
				yesButton.click();
				driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
				break;
			} catch (Exception e) {
			log.error(e.getMessage());
			}
		}
	}

	public void selectReportTemplate(String reportTemplateName) throws InterruptedException {
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
		reportTemplateDropDown.click();
		Thread.sleep(2000);
		List<WebElement> templateList = reportTemplateList;
		for (WebElement temaplateName : templateList) {
			if (temaplateName.getText().equals(reportTemplateName)) {
				temaplateName.click();
				break;
			}
		}

	}

	private String selectFrequency(String frequencyName) throws InterruptedException {
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
		frequencyDropDown.click();
		Thread.sleep(2000);
		List<WebElement> frequencyList = frequencyNameList;
		for (WebElement frequency : frequencyList) {
			if (frequency.getText().equals(frequencyName)) {
				frequency.click();
				break;
			}
		}
		return frequencyName;

	}

	public void clickCalender() {
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
		driver.findElement(By.xpath("//button[contains(@aria-label,'Open calendar')]")).click();

	}

	public void selectYear(String year) {
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
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
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
		List<WebElement> requiredMonth = monthList;
		for (WebElement selectedMonth : requiredMonth) {
			if (selectedMonth.getText().equals(month)) {
				selectedMonth.click();
				break;
			}
		}

	}

	public void selectDate(String date) {
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
		List<WebElement> requiredDate = dateList;
		for (WebElement selectedDate : requiredDate) {
			if (selectedDate.getText().equals(date)) {
				selectedDate.click();
				break;
			}
		}
	}

	private void dragAndDropTask() throws InterruptedException {
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
		addTaskButton.click();
		WebElement kpiExecutionTaskSource = driver.findElement(By.xpath("(//div[@id='fromAllTaskList'])[1]"));
		WebElement pdfExecutionTaskSource = driver.findElement(By.xpath("(//div[@id='fromAllTaskList'])[2]"));
		WebElement emailExecutionTaskSource = driver.findElement(By.xpath("(//div[@id='fromAllTaskList'])[3]"));
		WebElement taskExecutionDestination = driver.findElement(By.xpath("(//div[@class='containerCls'])[2]"));

		Actions act = new Actions(driver);

		act.dragAndDrop(kpiExecutionTaskSource, taskExecutionDestination).build().perform();
		act.dragAndDrop(pdfExecutionTaskSource, taskExecutionDestination).build().perform();
		act.dragAndDrop(emailExecutionTaskSource, taskExecutionDestination).build().perform();
		Thread.sleep(2000);
		taskaddButton.click();

	}

	private void addMaildetails() throws InterruptedException {

		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
		clickMailingDetails.click();
		mailFrom.sendKeys(LoginAndSelectModule.testData.get("mailFrom"));
		mailTo.sendKeys(LoginAndSelectModule.testData.get("mailTo"));
		ccReceiverMailAddress.sendKeys(LoginAndSelectModule.testData.get("ccMail"));
		bccReceiverMailAddress.sendKeys(LoginAndSelectModule.testData.get("bccMail"));
		mailSubject.sendKeys(LoginAndSelectModule.testData.get("subject"));
		mailBodyTemplate.sendKeys(LoginAndSelectModule.testData.get("bodyTemplate"));
		Thread.sleep(2000);
		addMail.click();
	}

	public boolean verifyReportName(String report) {
		boolean isReportName = false;
		for (int i = 0; i < reportsNameList.size(); i++) {
			if (reportsNameList.get(i).getText().equals(report)) {
				isReportName = true;
				break;
			}

		}
		return isReportName;
	}

	public void selectReport() throws InterruptedException {
		Thread.sleep(1000);
		for (int i = 0; i < reportsNameList.size(); i++) {
			if (reportsNameList.get(i).getText().equals(LoginAndSelectModule.testData.get("reportName"))) {
				List<WebElement> deleteButtons = reportsNameList.get(i)
						.findElements(By.xpath(".//preceding::span[contains(@class, 'mat-radio-container')]"));
				driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
				deleteButtons.get(i).click();
				break;
			}
		}

	}

	public void clickToggleButton() {
		for (WebElement element : reportsNameList) {
			if (element.getText().equals(LoginAndSelectModule.testData.get("reportName"))) {
				List<WebElement> toggleButtons = element
						.findElements(By.xpath(".//following::span[contains(@class, 'mat-slide-toggle-bar')]"));
				driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
				toggleButtons.get(0).click();
				break;
			}

		}
	}
}
