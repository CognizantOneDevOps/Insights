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
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

public class AssessmentReportConfigPage extends AssessmentReportObjectRepository {

	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

	private static final Logger log = LogManager.getLogger(AssessmentReportConfigPage.class);

	public AssessmentReportConfigPage() {
		PageFactory.initElements(driver, this);
	}

	private void dragAndDropTask() {
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		addTaskButton.click();
		WebElement kpiExecutionTaskSource = driver.findElement(By.xpath("(//div[@id='fromAllTaskList'])[1]"));
		WebElement pdfExecutionTaskSource = driver.findElement(By.xpath("(//div[@id='fromAllTaskList'])[2]"));
		WebElement emailExecutionTaskSource = driver.findElement(By.xpath("(//div[@id='fromAllTaskList'])[3]"));
		WebElement taskExecutionDestination = driver.findElement(By.xpath("(//div[@class='container'])[2]"));
		Actions act = new Actions(driver);
		act.dragAndDrop(kpiExecutionTaskSource, taskExecutionDestination).build().perform();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		act.dragAndDrop(pdfExecutionTaskSource, taskExecutionDestination).build().perform();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		act.dragAndDrop(emailExecutionTaskSource, taskExecutionDestination).build().perform();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		taskaddButton.click();

	}

	public void addReport(String reportName, String title, String templateName, String frequency, String reoccurence,
			String day, String month, String year, String mailFrom, String mailTo, String ccMail, String bccMail,
			String subject, String bodyTemplate) {

		wait.until(ExpectedConditions.elementToBeClickable(addButton));
		addButton.click();
		reportNameEl.sendKeys(reportName);
		titleNameEl.sendKeys(title);
		reportTemplateDropDownEl.sendKeys(templateName);
		frequencyDropDownEl.sendKeys(frequency);
		dragAndDropTask();
		addMaildetails(mailFrom, mailTo, ccMail, bccMail, subject, bodyTemplate);
		driver.manage().timeouts().implicitlyWait(9, TimeUnit.SECONDS);
		wait.until(ExpectedConditions.elementToBeClickable(saveButton));
		saveButton.click();
		driver.manage().timeouts().implicitlyWait(9, TimeUnit.SECONDS);
		wait.until(ExpectedConditions.elementToBeClickable(yesButton));
		yesButton.click();
		driver.manage().timeouts().implicitlyWait(9, TimeUnit.SECONDS);
		wait.until(ExpectedConditions.elementToBeClickable(okButton));
		okButton.click();

	}

	private void addMaildetails(String mailFrom, String mailTo, String ccMail, String bccMail, String subject,
			String bodyTemplate) {

		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		clickMailingDetails.click();
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		mailFromEl.sendKeys(mailFrom);
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		mailToEl.sendKeys(mailTo);
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		ccReceiverMailAddressEl.sendKeys(ccMail);
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		bccReceiverMailAddressEl.sendKeys(bccMail);
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		mailSubjectEl.sendKeys(subject);
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		mailBodyTemplateEl.sendKeys(bodyTemplate);
		addMail.click();
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
	}
}
