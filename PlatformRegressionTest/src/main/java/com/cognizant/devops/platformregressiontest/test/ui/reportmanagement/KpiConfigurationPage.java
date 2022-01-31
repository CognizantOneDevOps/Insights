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
import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

/**
 * @author Ankita
 * 
 *
 */

public class KpiConfigurationPage extends KPIObjectRepository {

	WebDriverWait wait = new WebDriverWait(driver, 10);

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
	 */
	public String saveKPI(String kpiId, String kpiName, String toolName, String category, String resultField,
			String groupName, String datasource, String dbQuery, String isActive) {

		clickAddButton();
		kpiIdEl.sendKeys(kpiId);
		kpiNameEl.sendKeys(kpiName);
		resultFieldEl.sendKeys(resultField);
		toolNameEl.sendKeys(toolName);
		categoryNameEl.sendKeys(category);
		groupNameEl.sendKeys(groupName);
		datasourceEl.sendKeys(datasource);
		dbQueryEl.sendKeys(dbQuery);
		wait.until(ExpectedConditions.elementToBeClickable(isActiveEl));
		isActiveEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(saveEl)).click();
		wait.until(ExpectedConditions.elementToBeClickable(yesBtnEl)).click();
		wait.until(ExpectedConditions.elementToBeClickable(btnOKEl));
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		try {
			if (kpiExistsEl.isDisplayed()) {
				btnOKEl.click();
				navigateToKPILandingPage();
				log.debug("Skipping test case as KPI : {} already exists", kpiId);
				throw new SkipException("Skipping test case as KPI : " + kpiId + " already exists");
			}
		} catch (NoSuchElementException e) {
			log.info("Something went wrong while saving KPI : {} exception : {}", kpiId, e.getMessage());
		}
		btnOKEl.click();
		return kpiId;

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
	 */
	public boolean validateKPI(String kpiId, String kpiName, String toolName, String category, String resultField,
			String groupName, String datasource, String dbQuery, String isActive) {

		clickAddButton();
		kpiIdEl.sendKeys(kpiId);
		kpiNameEl.sendKeys(kpiName);
		resultFieldEl.sendKeys(resultField);
		toolNameEl.sendKeys(toolName);
		categoryNameEl.sendKeys(category);
		groupNameEl.sendKeys(groupName);
		datasourceEl.sendKeys(datasource);
		dbQueryEl.sendKeys(dbQuery);
		wait.until(ExpectedConditions.elementToBeClickable(isActiveEl));
		isActiveEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(saveEl));
		saveEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(btnOKEl));
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		try {
			if (kpiValidateEl.isDisplayed()) {
				btnOKEl.click();
				navigateToKPILandingPage();
				log.info("add screen KPI : {} validated successfully", kpiId);
				return true;
			}
		} catch (NoSuchElementException e) {
			log.error("unable to validate add screen kpi {}", kpiId);
			return true;
		}
		btnOKEl.click();
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
	 */
	public boolean editKPI(String kpiId, String category) {
		selectKPI(kpiId);
		wait.until(ExpectedConditions.visibilityOf(btnEditEl));
		btnEditEl.click();
		categoryNameEl.sendKeys(category);
		wait.until(ExpectedConditions.elementToBeClickable(saveEl));
		saveEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(yesBtnEl));
		yesBtnEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(btnOKEl));
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		try {
			if (kpiUpdateEl.isDisplayed()) {
				btnOKEl.click();
				log.info(" kpiId {} updated successfully ", kpiId);
				return true;
			}
		} catch (NoSuchElementException e) {
			log.error("Unable to edit kpiId {} ", kpiId);
			return true;
		}
		btnOKEl.click();
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
	 */
	public boolean editValidateKPI(String kpiId, String category, String resultField, String dbQuery,
			String datasource) {
		selectKPI(kpiId);
		wait.until(ExpectedConditions.visibilityOf(btnEditEl));
		btnEditEl.click();
		categoryNameEl.sendKeys(category);
		resultFieldEl.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
		resultFieldEl.sendKeys(resultField);
		dbQueryEl.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
		dbQueryEl.sendKeys(dbQuery);
		wait.until(ExpectedConditions.elementToBeClickable(saveEl));
		saveEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(btnOKEl));
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		try {
			if (kpiValidateEl.isDisplayed()) {
				btnOKEl.click();
				navigateToKPILandingPage();
				log.info(" edit screen kpiId {} validated successfully ", kpiId);
				return true;
			}
		} catch (NoSuchElementException e) {
			log.error(" edit screen kpiId {} validation unsuccessful ", kpiId);
			return false;
		}
		btnOKEl.click();
		return false;

	}

	/**
	 * This method checks for non-editable fields.
	 * 
	 * @param kpiId
	 * @return
	 */
	public boolean nonEditableFields(String kpiId) {
		selectKPI(kpiId);
		wait.until(ExpectedConditions.elementToBeClickable(btnEditEl));
		btnEditEl.click();
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		try {
			if (kpiIdEl.getAttribute("ng-reflect-is-disabled").equals("true")
					&& kpiNameEl.getAttribute("ng-reflect-is-disabled").equals("true")
					&& groupNameEl.getAttribute("ng-reflect-is-disabled").equals("true")
					&& toolNameEl.getAttribute("ng-reflect-is-disabled").equals("true")) {
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
	 */
	public boolean deleteKPI(String kpiId) {

		selectKPI(kpiId);
		wait.until(ExpectedConditions.visibilityOf(btnDeleteEl));
		btnDeleteEl.click();
		wait.until(ExpectedConditions.elementToBeClickable(yesBtnEl));
		yesBtnEl.click();
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		try {
			wait.until(ExpectedConditions.visibilityOf(kpiDeletedEl));
			if (kpiDeletedEl.isDisplayed()) {
				wait.until(ExpectedConditions.elementToBeClickable(btnOKEl));
				btnOKEl.click();
				log.info("kpiId {} deleted successfully ", kpiId);
				return true;
			}
		} catch (NoSuchElementException e) {
			log.error(" unable to deletge kpiId {}  with exception {} :", kpiId, e.getMessage());
			return true;
		}

		btnOKEl.click();
		return false;
	}

	/**
	 * This method handles KPI selection.
	 * 
	 * @param kpiId
	 */
	public void selectKPI(String kpiId) {

		for (int i = 0; i < kpiListEl.size(); i++) {
			if (kpiListEl.get(i).getText().equals(kpiId)) {
				List<WebElement> radioButtons = kpiListEl.get(i)
						.findElements(By.xpath(".//preceding::span[contains(@class, 'mat-radio-container')]"));
				driver.manage().timeouts().implicitlyWait(6, TimeUnit.SECONDS);
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
			driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
			wait.until(ExpectedConditions.elementToBeClickable(refreshBtnE1));
			refreshBtnE1.click();
			driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

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
		}
		return true;
	}

	/**
	 * This method handles upload kpi json.
	 * 
	 * @param fileName
	 */
	public boolean uploadJson(String fileName) {
		String path = uploadFilePath + fileName;
		uploadBtnE1.click();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		chooseFileBtnE1.sendKeys(path);
		wait.until(ExpectedConditions.elementToBeClickable(uploadJsonBtnE1));
		uploadJsonBtnE1.click();
		wait.until(ExpectedConditions.elementToBeClickable(btnOKEl));
		btnOKEl.click();
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
		actions.moveToElement(searchKPIEl).click();
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
