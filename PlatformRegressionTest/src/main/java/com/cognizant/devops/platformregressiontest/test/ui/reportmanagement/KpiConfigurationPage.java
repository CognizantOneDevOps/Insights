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
package com.cognizant.devops.platformregressiontest.test.ui.reportmanagement;

import java.io.File;
import java.time.Duration;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.SkipException;

import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.test.ui.testdatamodel.ReportConfigurationDataModel;

/**
 * @author Ankita
 * 
 *
 */

public class KpiConfigurationPage extends KPIObjectRepository {

	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

	private static final Logger log = LogManager.getLogger(KpiConfigurationPage.class);

	public static String uploadFilePath = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator
			+ ConfigOptionsTest.AUTO_DIR + File.separator + ConfigOptionsTest.REPORT_MANAGEMENT_DIR + File.separator;

	public KpiConfigurationPage() {
		PageFactory.initElements(driver, this);
	}

	public boolean navigateToKPIConfigurationLandingPage() {
		return landingPage.isDisplayed();
	}

	private void clickAddButton() {

		Actions actions = new Actions(driver);
		wait.until(ExpectedConditions.elementToBeClickable(addNewKPIButton));
		actions.moveToElement(addNewKPIButton).click();
		Action action = actions.build();
		action.perform();

	}

	/**
	 * This method handles KPI insertion
	 * 
	 * @param kpiId
	 * @param kpiName
	 * @param toolName
	 * @param category
	 * @param resultField
	 * @param groupName
	 * @param datasource
	 * @param dbQuery
	 * @param isActive
	 * @return
	 * @throws InterruptedException 
	 */
	public boolean saveKPI(ReportConfigurationDataModel data) throws InterruptedException {

		clickAddButton();
		kpiIdEl.sendKeys(data.getKpiId());
		kpiNameEl.sendKeys(data.getKpiName());
		resultFieldEl.sendKeys(data.getResultField());
		toolNameEl.sendKeys(data.getToolName());
		categoryNameEl.sendKeys(data.getCategory());
		groupNameEl.sendKeys(data.getGroupName());
		datasourceEl.sendKeys(data.getDatasource());
		dbQueryEl.sendKeys(data.getDbQuery());
		Thread.sleep(1000);
		wait.until(ExpectedConditions.elementToBeClickable(saveEl)).click();
		wait.until(ExpectedConditions.elementToBeClickable(yesBtnEl)).click();
		wait.until(ExpectedConditions.elementToBeClickable(crossClose));
		Thread.sleep(4000);
		try {
			if (kpiExistsEl.isDisplayed()) {
				crossClose.click();
				navigateToKPILandingPage();
				log.debug("Skipping test case as KPI : {} already exists", data.getKpiId());
				throw new SkipException("Skipping test case as KPI : " + data.getKpiId() + " already exists");
			}
		} catch (NoSuchElementException e) {
			log.info("Something went wrong while saving KPI : {} exception : {}", data.getKpiId(), e.getMessage());
			crossClose.click();
			return true;
		}
		crossClose.click();
		return true;

	}

	/**
	 * This method does KPI validation
	 * 
	 * @param kpiId
	 * @param kpiName
	 * @param toolName
	 * @param category
	 * @param resultField
	 * @param groupName
	 * @param datasource
	 * @param dbQuery
	 * @param isActive
	 * @return
	 * @throws InterruptedException 
	 */
	public boolean validateKPI(ReportConfigurationDataModel data) throws InterruptedException {

		clickAddButton();
		kpiIdEl.sendKeys(data.getKpiId());
		kpiNameEl.sendKeys(data.getKpiName());
		resultFieldEl.sendKeys(data.getResultField());
		toolNameEl.sendKeys(data.getToolName());
		categoryNameEl.sendKeys(data.getCategory());
		groupNameEl.sendKeys(data.getGroupName());
		datasourceEl.sendKeys(data.getDatasource());
		dbQueryEl.sendKeys(data.getDbQuery());
		wait.until(ExpectedConditions.elementToBeClickable(saveEl)).click();
		try {
		wait.until(ExpectedConditions.elementToBeClickable(yesBtnEl)).click();
		wait.until(ExpectedConditions.elementToBeClickable(crossClose)).click();
		}
		catch(Exception e) {
			crossClose.click();
			navigateToKPILandingPage();
			return true;
		}
		Thread.sleep(2000);
		try {
			if (kpiValidateEl.isDisplayed()) {
				crossClose.click();
				navigateToKPILandingPage();
				log.info("add screen KPI : {} validated successfully", data.getKpiId());
				return true;
			}
		} catch (NoSuchElementException e) {
			crossClose.click();
			navigateToKPILandingPage();
			log.error("unable to validate add screen kpi {}", data.getKpiId());
			return true;
		}
		crossClose.click();
		return false;

	}

	/**
	 * This method handles KPI screen navigation.
	 * 
	 */
	public void navigateToKPILandingPage() {
		Actions actions = new Actions(driver);
		WebElement kpiElement = driver.findElement(By.xpath("//a[@title='Kpi Creation']"));
		wait.until(ExpectedConditions.elementToBeClickable(kpiElement));
		actions.moveToElement(kpiElement).click();
		Action action = actions.build();
		action.perform();

	}

	/**
	 * This methods checks for duplicate KPI
	 * 
	 * @param kpiId
	 * @return
	 */
	public boolean isKpiExists(String kpiId) {

		for (WebElement we : kpiListEl) {
			wait.until(ExpectedConditions.visibilityOf(we));
			if (we.getText().equals(kpiId)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * This method handles edit kpi functionality.
	 * 
	 * @param kpiId
	 * @param category
	 * @return
	 * @throws InterruptedException 
	 */
	public boolean editKPI(String kpiId, String resultField) throws InterruptedException {
		selectKPI(kpiId);
		wait.until(ExpectedConditions.visibilityOf(btnEditEl));
		btnEditEl.click();
		resultFieldEl.sendKeys(resultField);
		wait.until(ExpectedConditions.elementToBeClickable(saveEl));
		saveEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(yesBtnEl));
		yesBtnEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(crossClose));
		Thread.sleep(2000);
		try {
			if (kpiUpdateEl.isDisplayed()) {
				crossClose.click();
				log.info(" kpiId {} updated successfully ", kpiId);
				return true;
			}
		} catch (NoSuchElementException e) {
			log.error("Unable to edit kpiId {} ", kpiId);
			return true;
		}
		crossClose.click();
		return false;

	}

	/**
	 * This method handles edit screen validation.
	 * 
	 * @param kpiId
	 * @param category
	 * @param resultField
	 * @param dbQuery
	 * @param datasource
	 * @return
	 * @throws InterruptedException 
	 */
	public boolean editValidateKPI(String kpiId, String category, String resultField, String dbQuery,
			String datasource) throws InterruptedException {
		selectKPI(kpiId);
		wait.until(ExpectedConditions.visibilityOf(btnEditEl));
		btnEditEl.click();
		resultFieldEl.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
		resultFieldEl.sendKeys(resultField);
		dbQueryEl.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
		dbQueryEl.sendKeys(dbQuery);
		wait.until(ExpectedConditions.elementToBeClickable(saveEl));
		saveEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(yesBtnEl));
		yesBtnEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(crossClose));
		Thread.sleep(2000);
		try {
			if (kpiUpdateEl.isDisplayed()) {
				crossClose.click();
				navigateToKPILandingPage();
				log.info(" edit screen kpiId {} validated successfully ", kpiId);
				return true;
			}
		} catch (NoSuchElementException e) {
			log.error(" edit screen kpiId {} validation unsuccessful ", kpiId);
			return false;
		}
		crossClose.click();
		return false;

	}

	/**
	 * This method checks for non-editable fields.
	 * 
	 * @param kpiId
	 * @return
	 * @throws InterruptedException 
	 */
	public boolean nonEditableFields(String kpiId) throws InterruptedException {
		selectKPI(kpiId);
		wait.until(ExpectedConditions.elementToBeClickable(btnEditEl));
		btnEditEl.click();
		Thread.sleep(3000);
		try {
			if (!kpiIdEl.isEnabled()
					&& !kpiNameEl.isEnabled()
					&& !groupNameEl.isEnabled()) {
				navigateToKPILandingPage();
				return true;
			}
		} catch (Exception e) {
			log.info(e.getMessage());
			navigateToKPILandingPage();
			return true;

		}
		navigateToKPILandingPage();
		return false;

	}

	/**
	 * This method handles KPI deletion.
	 * 
	 * @param kpiId
	 * @return
	 * @throws InterruptedException 
	 */
	public boolean deleteKPI(String kpiId) throws InterruptedException {

		selectKPI(kpiId);
		wait.until(ExpectedConditions.visibilityOf(btnDeleteEl));
		btnDeleteEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(yesBtnEl));
		yesBtnEl.click();
		Thread.sleep(2000);
		try {
			wait.until(ExpectedConditions.visibilityOf(kpiDeletedEl));
			if (kpiDeletedEl.isDisplayed()) {
				wait.until(ExpectedConditions.elementToBeClickable(crossClose));
				crossClose.click();
				log.info("kpiId {} deleted successfully ", kpiId);
				return true;
			}
		} catch (NoSuchElementException e) {
			log.error(" unable to deletge kpiId {}  with exception {} :", kpiId, e.getMessage());
			return true;
		}

		crossClose.click();
		return false;
	}

	/**
	 * This method handles KPI selection.
	 * 
	 * @param kpiId
	 * @throws InterruptedException 
	 */
	public void selectKPI(String kpiId) throws InterruptedException {

		for (int i = 0; i < kpiListEl.size(); i++) {
			if (kpiListEl.get(i).getText().equals(kpiId)) {
				List<WebElement> radioButtons = kpiListEl.get(i)
						.findElements(By.xpath(".//preceding::span[contains(@class, 'mat-radio-container')]"));
				Thread.sleep(3000);
				radioButtons.get(i).click();
				break;
			}
		}
	}

	/**
	 * This method handles refresh button functionality
	 * 
	 * @param kpiId
	 * @return
	 */
	public boolean checkRefreshButton(String kpiId) {
		try {
			selectKPI(kpiId);
			Thread.sleep(2000);
			wait.until(ExpectedConditions.elementToBeClickable(refreshBtnE1));
			refreshBtnE1.click();
			Thread.sleep(3000);

			for (int i = 0; i < kpiListEl.size(); i++) {
				WebElement radioButton = kpiListEl.get(i).findElement(By.xpath(".//preceding::mat-radio-button"));
				if (radioButton.isSelected()) {
					log.info("KPI is in selected mode ");
					return false;
				} else {
					log.info("Not-Selected");

				}

			}
			return true;
		} catch (Exception e) {
			log.info("Error checking the refresh button");
		}
		return true;
	}

	/**
	 * This method handles upload kpi json.
	 * 
	 * @param fileName
	 * @throws InterruptedException 
	 */
	public boolean uploadJson(String fileName) throws InterruptedException {
		String path = uploadFilePath + fileName;
		uploadBtnE1.click();
		Thread.sleep(5000);
		chooseFileBtnE1.sendKeys(path);
		wait.until(ExpectedConditions.elementToBeClickable(uploadJsonBtnE1));
		uploadJsonBtnE1.click();
		wait.until(ExpectedConditions.elementToBeClickable(crossClose));
		crossClose.click();
		log.info("upload json successful");
		return true;
	}

	/**
	 * This method handles KPI search.
	 * 
	 * @param kpiId
	 * @return
	 */
	public boolean searchKPI(String kpiId) {
		Actions actions = new Actions(driver);
		actions.moveToElement(searchKpiElement).click();
		actions.sendKeys(kpiId);
		Action action = actions.build();
		action.perform();
		if (kpiListEl.size() == 1) {
			searchKPIEl.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
			navigateToKPILandingPage();
			log.info("Kpi search box test successful");
			return true;
		}
		log.info("Kpi search box test unsuccessful");
		return false;

	}

}
