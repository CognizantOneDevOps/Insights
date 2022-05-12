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
package com.cognizant.devops.platformregressiontest.test.ui.dashboardreportdownload;

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
import org.testng.SkipException;

import com.cognizant.devops.platformregressiontest.test.ui.testdatamodel.DashboardReportDataModel;

public class DashboardReportDownloadConfiguration extends DashboardReportDownloadObjectRepository {

	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

	private static final Logger log = LogManager.getLogger(DashboardReportDownloadConfiguration.class);

	public DashboardReportDownloadConfiguration() {
		PageFactory.initElements(driver, this);
	}

	/**
	 * checks whether landing page is displayed or not by checking visibility of
	 * Dashboard Report Configuration heading
	 * 
	 * @return true if Health Check heading, Notification label, Agents tab, Data
	 *         Components tab and Services tab is displayed o/w false
	 */
	public boolean navigateToDashboardReportDownloadPage() {
		try {
			log.info("Landing page displayed : {}", visibilityOf(landingPage, 4));
			return landingPage.isDisplayed();
		} catch (Exception e) {
			throw new SkipException("Something went wrong while navigating to landing page");
		}
	}

	/**
	 * creates report with different frequencies, time range, theme, pdf type etc
	 * 
	 * @param data
	 * @return
	 */
	public boolean createReportWithMailingDetails(DashboardReportDataModel data) {
		try {
			fillData(data);
			visibilityOf(addEmail, 2);
			clickOn(addEmail, 2);
			visibilityOf(senderEmailAddress, 2);
			senderEmailAddress.sendKeys(data.getSenderEmailAddress());
			visibilityOf(receiverEmailAddress, 2);
			receiverEmailAddress.sendKeys(data.getReceiverEmailAddress());
			visibilityOf(receiverCCEmailAddress, 2);
			receiverCCEmailAddress.sendKeys(data.getReceiverCCEmailAddress());
			visibilityOf(receiverBCCEmailAddress, 2);
			receiverBCCEmailAddress.sendKeys(data.getReceiverBCCEmailAddress());
			visibilityOf(mailSubject, 2);
			mailSubject.sendKeys(data.getMailSubject());
			visibilityOf(mailBodyTemplate, 2);
			mailBodyTemplate.sendKeys(data.getMailBodyTemplate());
			visibilityOf(saveMailDetails, 2);
			clickOn(saveMailDetails, 2);
			visibilityOf(save, 2);
			clickOn(save, 2);
			visibilityOf(dashboardPreview, 2);
			visibilityOf(confirm, 2);
			clickOn(confirm, 2);
			visibilityOf(yes, 2);
			clickOn(yes, 2);
			visibilityOf(success, 2);
			clickOn(OKAY, 2);
			return verifyDashboardReport(data.getTitle());
		} catch (Exception e) {
			throw new SkipException("Something went wrong while creating report for" + data.getDashboardName());
		}
	}

	/**
	 * creates report without mailing details
	 * 
	 * @param data
	 * @return
	 */
	public boolean createReportWithoutMailingDetails(DashboardReportDataModel data) {
		try {
			fillData(data);
			visibilityOf(save, 2);
			clickOn(save, 2);
			visibilityOf(dashboardPreview, 2);
			visibilityOf(confirm, 2);
			clickOn(confirm, 2);
			visibilityOf(yes, 2);
			clickOn(yes, 2);
			visibilityOf(success, 2);
			clickOn(OKAY, 2);
			return verifyDashboardReport(data.getTitle());

		} catch (Exception e) {
			throw new SkipException("Something went wrong while creating report for" + data.getDashboardName());
		}

	}

	/**
	 * checks details functionality and verify if heading Workflow History detail
	 * with title present or not
	 * 
	 * @param data
	 * @return
	 */
	public boolean checkDetailsFunctionality(DashboardReportDataModel data) {
		selectReport(data.getTitle());
		try {
			driver.findElement(
					By.xpath("//tr/td[contains(text(), '" + data.getTitle() + "')]//following-sibling::td[6]")).click();
			visibilityOf(workfloHistoryDetail, 2);
			if (workfloHistoryDetail.getText().equals("Workflow History Detail - " + data.getTitle())) {
				closeDialog.click();
				return true;
			}
			closeDialog.click();
		} catch (Exception e) {
			closeDialog.click();
			log.info("Details functionality not found.");
		}
		return true;
	}

	public boolean deleteReport(DashboardReportDataModel data) {
		try {
			selectReport(data.getTitle());
			clickOn(deleteReport, 2);
			clickOn(yes, 2);
			clickOn(OKAY, 2);
			if (!selectReport(data.getTitle())) {
				log.info("{} deleted successfully.", data.getTitle());
				return true;
			}
			return false;
		} catch (Exception e) {
			throw new SkipException("Something went wrong while deleting report");
		}
	}

	/**
	 * checks refresh and redirect functionality
	 * 
	 * @param data
	 * @return
	 */
	public boolean checkRefreshAndRedirectFunctionality(DashboardReportDataModel data) {
		if (refreshFunctionalityCheck(data)) {
			log.info("refresh & reset functionality successful");
			redirectButton.click();
			wait.until(ExpectedConditions.elementToBeClickable(landingPage));
			try {
				if (landingPage.isDisplayed()) {
					log.info("navigate to landing page successful");
					return true;
				}
			} catch (Exception e) {
				log.info("navigate to landing page unsuccessful");
				return false;
			}
			return true;
		}
		log.info("refresh & reset functionality unsuccessful");
		return false;
	}

	/**
	 * checks if reset functionality is successful
	 * 
	 * @param data
	 * 
	 * @return true if reset functionality is successful o/w false
	 */
	private boolean refreshFunctionalityCheck(DashboardReportDataModel data) {
		clickAddButton.click();
		selectOrg.sendKeys(data.getOrganization());
		resetButton.click();
		if (selectOrg.getText().equals("Select Organisation")) {
			log.info("reset functionality successful");
			return true;
		}
		log.info("reset functionality unsuccessful");
		return false;
	}

	/**
	 * select report with the given title
	 * 
	 * @param reportTitle
	 * @return
	 */
	public boolean selectReport(String reportTitle) {
		boolean reportPresent = false;
		try {
			driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
			wait.until(ExpectedConditions.visibilityOfAllElements(reportList));
			for (int i = 0; i < reportList.size(); i++) {
				if (reportList.get(i).getText().equals(reportTitle)) {
					driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
					reportPresent = true;
					List<WebElement> radioButtons = reportList.get(i)
							.findElements(By.xpath(".//preceding::span[contains(@class, 'mat-radio-container')]"));
					driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
					radioButtons.get(i).click();
					break;
				}
			}
		} catch (Exception e) {
			log.info("Report list is empty!!");
		}
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		return reportPresent;
	}

	/**
	 * Common data to fill for test cases
	 * 
	 * @param data
	 */
	private void fillData(DashboardReportDataModel data) {
		visibilityOf(clickAddButton, 2);
		clickOn(clickAddButton, 2);
		visibilityOf(mainHeader, 2);
		visibilityOf(selectOrg, 2);
		selectOrg.sendKeys(data.getOrganization());
		visibilityOf(selectDashboard, 2);
		selectDashboard.sendKeys(data.getDashboardName());
		visibilityOf(title, 2);
		title.sendKeys(data.getTitle());
		visibilityOf(selectTheme, 2);
		selectTheme.sendKeys(data.getTheme());
		visibilityOf(selectPdfType, 2);
		selectPdfType.sendKeys(data.getPdfType());
		visibilityOf(selectLoadTime, 2);
		selectLoadTime.sendKeys(data.getLoadTime());
		if (data.getDashboardName().equals("PlatformAgent")) {
			toolName.sendKeys(data.getToolName());
			visibilityOf(agentId, 2);
			agentId.sendKeys(data.getAgentId());
		}
		if (data.getDashboardName().equals("PlatformEngine")) {
			toolName.sendKeys(data.getAgentId());
		}
		selectFrequency.sendKeys(data.getFrequency());
		switch (data.getDataTimeRange()) {

		case "Absolute":
			visibilityOf(dataTimeRangeAbsolute, 2);
			clickOn(dataTimeRangeAbsolute, 2);
			selectAbsoluteStartDate.sendKeys(data.getAbsoluteStartDate());
			selectAbsoluteEndDate.sendKeys(data.getAbsoluteEndDate());
			break;

		case "Relative":
			visibilityOf(dataTimeRangeRelative, 2);
			clickOn(dataTimeRangeRelative, 2);
			visibilityOf(selectRelativeTimeRange, 2);
			selectRelativeTimeRange.sendKeys(data.getTimeRange());
			break;

		case "Other":
			visibilityOf(dataTimeRangeOther, 2);
			clickOn(dataTimeRangeOther, 2);
			visibilityOf(selectOtherTimeRange, 2);
			selectOtherTimeRange.sendKeys(data.getTimeRange());
			break;

		default:
			throw new SkipException("No data time range selected.");
		}

	}

	/**
	 * Verifies whether report present or not
	 * 
	 * @param reportTitle
	 * @return
	 */
	private boolean verifyDashboardReport(String reportTitle) {
		boolean isReportPresent = false;
		try {
			driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
			visibilityOfAllElements(reportList, 1);
			for (WebElement reportName : reportList) {
				if (reportName.getText().equals(reportTitle)) {
					isReportPresent = true;
					log.info("Report saved successfully.");
					return isReportPresent;
				}
			}
			log.info("Report is not present in the view list.");
		} catch (Exception e) {
			log.info("No reports found.");
		}
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		return isReportPresent;
	}

	/**
	 * wait until the visibility of web element
	 * 
	 * @param element
	 * @param timeout
	 * @return true if element is displayed else false
	 */
	private boolean visibilityOf(WebElement element, int timeout) {
		new WebDriverWait(driver, Duration.ofSeconds(timeout)).until(ExpectedConditions.visibilityOf(element));
		return element.isDisplayed();
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

	/**
	 * wait until the visibility of list of web elements
	 * 
	 * @param element
	 * @return size of list of web elements
	 */
	public static int visibilityOfAllElements(List<WebElement> element, int timeout) {
		new WebDriverWait(driver, Duration.ofSeconds(timeout)).until(ExpectedConditions.visibilityOfAllElements(element));
		return element.size();
	}

}
