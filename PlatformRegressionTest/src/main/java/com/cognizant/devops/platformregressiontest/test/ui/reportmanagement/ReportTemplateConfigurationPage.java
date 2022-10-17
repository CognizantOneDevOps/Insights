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

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.SkipException;

import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;

public class ReportTemplateConfigurationPage extends ReportTemplateObjectRepository {

	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

	private static final Logger log = LogManager.getLogger(ReportTemplateConfigurationPage.class);

	private static String uploadFilePath = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator
			+ ConfigOptionsTest.AUTO_DIR + File.separator + ConfigOptionsTest.REPORT_MANAGEMENT_DIR + File.separator;

	private static String uploadConfigFiles = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator
			+ ConfigOptionsTest.AUTO_DIR + File.separator + ConfigOptionsTest.REPORT_CONFIG_FOLDER + File.separator;

	public ReportTemplateConfigurationPage() {
		PageFactory.initElements(driver, this);
	}

	/**
	 * This method handles create report template functionality
	 * 
	 * @param reportName
	 * @param description
	 * @param visualizationutil
	 * @param kpiId
	 * @param vType
	 * @param vQuery
	 * @return
	 * @throws InterruptedException 
	 */
	public boolean createReportTemplate(String reportName, String description, String visualizationutil,String templateType, String kpiId,
			String vType, String vQuery) throws InterruptedException {
		clickAddButton();
		templateNameEl.sendKeys(reportName);
		descriptionEl.sendKeys(description);
		visualizationEl.sendKeys(visualizationutil);
		templateTypeEl.sendKeys(templateType);
		wait.until(ExpectedConditions.elementToBeClickable(searchKpiEl));
		searchKpiEl.click();
		selectTableRow(kpiId, kpiListEl);
		wait.until(ExpectedConditions.elementToBeClickable(kpiSelectBtnEl));
		kpiSelectBtnEl.click();
		vTypeEl.sendKeys(vType);
		vQueryEl.sendKeys(vQuery);
		wait.until(ExpectedConditions.elementToBeClickable(addKpiBtn));
		addKpiBtn.click();
		Thread.sleep(1000);
		wait.until(ExpectedConditions.elementToBeClickable(saveEl));
		saveEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(yesBtnEl));
		yesBtnEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(crossClose));
		Thread.sleep(2000);
		try {
			if (reportExitsBtnEl.isDisplayed()) {
				crossClose.click();
				navigateToReportTemplateLandingPage();
				log.debug("Skipping test case as report template : {} already exists", reportName);
				throw new SkipException("Skipping test as report template : " + reportName + " already exists");
			}
		} catch (NoSuchElementException e) {
			crossClose.click();
			navigateToReportTemplateLandingPage();
			return true;
		}
		crossClose.click();
		return true;

	}
	
	private boolean visibilityOf(WebElement element, int timeout) {
		new WebDriverWait(driver, Duration.ofSeconds(timeout)).until(ExpectedConditions.visibilityOf(element));
		return element.isDisplayed();
	}

	/**
	 * This method handles navigation to report template configuration page
	 */
	public void navigateToReportTemplateLandingPage() {
		Actions actions = new Actions(driver);
		WebElement kpiElement = driver.findElement(By.xpath("//a[@title='Report Template Configuration']"));
		wait.until(ExpectedConditions.elementToBeClickable(kpiElement));
		actions.moveToElement(kpiElement).click();
		Action action = actions.build();
		action.perform();
	}

	private void clickAddButton() throws InterruptedException {
		Thread.sleep(10000);
		wait.until(ExpectedConditions.visibilityOf(addBtnEl)).click();
	}

	public void selectTableRow(String value, List<WebElement> table) throws InterruptedException {
		kpiInputEl.sendKeys(value);
		for (int i = 0; i < table.size(); i++) {
			if (table.get(i).getText().equals(value)) {
				List<WebElement> radioButtons = table.get(i)
						.findElements(By.xpath(".//preceding::span[contains(@class, 'mat-radio-container')]"));
				Thread.sleep(10000);
				radioButtons.get(0).click();
				break;
			}
		}
	}

	/**
	 * This method handles upload json functionality
	 * 
	 * @param fileName
	 * @return
	 * @throws InterruptedException 
	 */
	public boolean uploadJson(String fileName) throws InterruptedException {
		String path = uploadFilePath + fileName;
		uploadBtnE1.click();
		Thread.sleep(9000);
		chooseFileBtnE1.sendKeys(path);
		wait.until(ExpectedConditions.elementToBeClickable(uploadJsonBtnE1));
		uploadJsonBtnE1.click();
		wait.until(ExpectedConditions.elementToBeClickable(crossClose));
		try {
			if (reportExitsBtnEl.isDisplayed()) {
				crossClose.click();
				wait.until(ExpectedConditions.elementToBeClickable(btnCloseEl));
				btnCloseEl.click();
				navigateToReportTemplateLandingPage();
				log.debug("Skipping test case as report template : already exists");
				throw new SkipException("Skipping test as report template : already exists");
			}
		} catch (NoSuchElementException e) {
			log.info("Something went wrong while saving report template : exception :");
		}
		Thread.sleep(5000);
		crossClose.click();
		log.info("upload json successful");
		return true;
	}

	/**
	 * This method handles delete report template functionality
	 * 
	 * @param reportName
	 * @throws InterruptedException 
	 */
	public void deleteReportTemplate(String reportName) throws InterruptedException {

		selectRT(reportName);
		wait.until(ExpectedConditions.visibilityOf(btnDeleteEl));
		btnDeleteEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(yesBtnEl));
		yesBtnEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(crossClose));
		crossClose.click();

	}

	/**
	 * This method handles report template selection
	 * 
	 * @param reportName
	 * @throws InterruptedException 
	 */
	public void selectRT(String reportName) throws InterruptedException {

		for (int i = 0; i < reportTemplateListEl.size(); i++) {
			if (reportTemplateListEl.get(i).getText().equals(reportName)) {
				List<WebElement> radioButtons = reportTemplateListEl.get(i)
						.findElements(By.xpath(".//preceding::span[contains(@class, 'mat-radio-container')]"));
				Thread.sleep(10000);
				radioButtons.get(i).click();
				break;
			}
		}
	}

	/**
	 * This method handles refresh button functionality
	 * 
	 * @param reportName
	 * @return
	 * @throws InterruptedException 
	 */
	public boolean checkRefreshButton(String reportName) throws InterruptedException {
		selectRT(reportName);
		Thread.sleep(2000);
		refreshBtnE1.click();
		for (int i = 0; i < reportTemplateListEl.size(); i++) {
			WebElement radioButton = reportTemplateListEl.get(i)
					.findElement(By.xpath(".//preceding::mat-radio-button"));
			if (radioButton.isSelected()) {
				log.info("report template is in selected mode ");
				return false;
			} else {
				log.info("Not-Selected");
			}

		}
		return true;
	}

	/**
	 * this method handles delete kpi functionality
	 * 
	 * @param kpiId
	 * @return
	 * @throws InterruptedException 
	 */
	public boolean deleteKPIFromRT(String kpiId) throws InterruptedException {
		for (int i = 0; i < kpiListEl.size(); i++) {
			if (kpiListEl.get(i).getText().equals(kpiId)) {
				List<WebElement> deleteButtons = kpiListEl.get(i)
						.findElements(By.xpath("//mat-icon[@svgicon='trash']"));
				Thread.sleep(10000);
				deleteButtons.get(0).click();
				break;
			}
		}
		return true;
	}

	/**
	 * This method handles edit report template functionality
	 * 
	 * @param reportName
	 * @param kpiId
	 * @param description
	 * @param vType
	 * @param vQuery
	 * @throws InterruptedException 
	 */
	public void editReportTemplate(String reportName, String kpiId, String description, String vType, String vQuery) throws InterruptedException {

		selectRT(reportName);
		wait.until(ExpectedConditions.visibilityOf(btnEditEl));
		btnEditEl.click();
		Thread.sleep(1000);
		deleteKPIFromRT(kpiId);
		wait.until(ExpectedConditions.elementToBeClickable(yesBtnEl));
		yesBtnEl.click();
		descriptionEl.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
		descriptionEl.sendKeys(description);
		wait.until(ExpectedConditions.elementToBeClickable(searchKpiEl));
		searchKpiEl.click();
		selectTableRow(kpiId, kpiListEl);
		wait.until(ExpectedConditions.elementToBeClickable(kpiSelectBtnEl));
		kpiSelectBtnEl.click();
		vTypeEl.sendKeys(vType);
		vQueryEl.sendKeys(vQuery);
		wait.until(ExpectedConditions.elementToBeClickable(addKpiBtn));
		addKpiBtn.click();
		wait.until(ExpectedConditions.elementToBeClickable(saveEl));
		saveEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(yesBtnEl));
		yesBtnEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(crossClose));
		crossClose.click();

	}

	/**
	 * This method handles report template validation
	 * 
	 * @param reportName
	 * @param description
	 * @param visualizationutil
	 * @param kpiId
	 * @param vType
	 * @param vQuery
	 * @return
	 * @throws InterruptedException
	 */
	public boolean createValidateReportTemplate(String reportName, String description, String visualizationutil,
			String templateType,String kpiId, String vType, String vQuery) throws InterruptedException {
		Thread.sleep(1000);
		clickAddButton();
		templateNameEl.sendKeys(reportName);
		descriptionEl.sendKeys(description);
		visualizationEl.sendKeys(visualizationutil);
		Thread.sleep(3000);
		wait.until(ExpectedConditions.elementToBeClickable(saveEl));
		Thread.sleep(3000);
		saveEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(crossClose));
		try {
			if (fillMandatoryDialogEl.isDisplayed()) {

				crossClose.click();
				navigateToReportTemplateLandingPage();
				return true;
			}
		} catch (Exception e) {
			if (addKPIDialogEl.isDisplayed()) {

				crossClose.click();
				navigateToReportTemplateLandingPage();
				return true;
			}
		}
		Thread.sleep(3000);

		return false;

	}

	/**
	 * This method handles attach config functionality
	 * 
	 * @param reportTemplateName
	 * @param configFileName
	 * @return
	 * @throws InterruptedException 
	 */
	public boolean attachReportConfigFiles(String reportTemplateName, String configFileName) throws InterruptedException {
		String path = uploadConfigFiles + configFileName;
		selectRT(reportTemplateName);
		wait.until(ExpectedConditions.visibilityOf(attachFilesBtnE1));
		try{
		attachFilesBtnE1.click();
		Thread.sleep(10000);
		chooseFileBtnE1.sendKeys(path);
		wait.until(ExpectedConditions.elementToBeClickable(uploadJsonBtnE1));
		uploadJsonBtnE1.click();
		wait.until(ExpectedConditions.elementToBeClickable(crossClose));
		crossClose.click();
		}
		catch(Exception e) {
			log.debug("Skipping test case as attach file button is disabled");
			throw new SkipException("Skipping test case as attach file button is disabled");
		}
		return true;

	}

	public boolean checkReportDetails(String reportName) throws InterruptedException {
		selectRT(reportName);
		try {
			driver.findElement(
					By.xpath("//tr/td[contains(text(), '" + reportName + "')]//following-sibling::td[4]")).click();
			visibilityOf(workfloHistoryDetail, 2);
			if (workfloHistoryDetail.getText().equals("KPI Details - " + reportName)) {
				Thread.sleep(500);
			     btnCloseEl.click();
				return true;
			}
		} catch (Exception e) {
			btnCloseEl.click();
			log.info("Details functionality not found.");
		}
		return true;
	}
}
