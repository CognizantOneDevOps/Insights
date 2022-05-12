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
package com.cognizant.devops.platformregressiontest.test.ui.dataarchival;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.SkipException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

public class DataArchivalConfiguration extends DataArchivalObjectRepository{

	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

	
	private static final Logger log = LogManager.getLogger(DataArchivalConfiguration.class);

	public DataArchivalConfiguration() {
		PageFactory.initElements(driver, this);
	}

	/**
	 * checks whether landing page is displayed or not
	 * 
	 * @return true if landing page is displayed o/w false
	 * @throws InterruptedException
	 */
	public boolean navigateToArchivalLandingPage() {
		log.info("Data Archival Landing page displayed");
		return landingPage.isDisplayed();
	}

	/**
	 * Creates new data archive and adds it to the database
	 * 
	 * @return true if archive is created successfully o/w false
	 * @throws InterruptedException
	 */
	@SuppressWarnings("all")                            
	public boolean addArchiveData() throws InterruptedException {
		if (checkArchive(LoginAndSelectModule.testData.get("archiveName"))) {
			log.debug("archive name already exists");
			throw new SkipException("Skipping test case as archive already exists");
		} else {
			driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			addDetails.click();
			archivalName.sendKeys(LoginAndSelectModule.testData.get("archiveName"));
			startDate.click();
			calenderArrow.click();
			selectDate(LoginAndSelectModule.testData.get("startYear"));
			selectDate(LoginAndSelectModule.testData.get("startMonth"));
			selectDate(LoginAndSelectModule.testData.get("startDate"));
			stopDate.click();
			calenderArrow.click();
			selectDate(LoginAndSelectModule.testData.get("endYear"));
			selectDate(LoginAndSelectModule.testData.get("endMonth"));
			selectDate(LoginAndSelectModule.testData.get("endDate"));
			noofdaystoRetain.sendKeys(LoginAndSelectModule.testData.get("daysToRetain"));
			saveButton.click();
			yesButton.click();
			try {
				if (successMessage.isDisplayed()) {
					wait.until(ExpectedConditions.elementToBeClickable(okButton));
					okButton.click();
					log.info("successfully added archive date");
					return true;
				}
			} catch (Exception e) {
				log.info("error while creating archive date");
				redirectToLandingPage.click();
				return false;
			}
		}
		log.info("unexpected error");
		return false;
	}

	/**
	 * Checks if archive name is present in the UI
	 * 
	 * @param archive
	 * @return True if archive is present o/w false
	 */
	private boolean checkArchive(String archive) {
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		List<WebElement> rws = archiveDetailsTable.findElements(By.tagName("tr"));
		for (int i = 0; i < rws.size(); i++) {
			List<WebElement> cols = (rws.get(i)).findElements(By.tagName("td"));
			if ((cols.get(1).getText()).equals(archive)) {
				log.info("{} archive name is present.", archive);
				return true;
			}
		}
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		log.info("{} archive name is not present.", archive);
		return false;
	}

	/**
	 * Selects the particular date value from list of values available
	 * 
	 * @param startInput
	 * @throws InterruptedException
	 */
	public void selectDate(String dateValue) throws InterruptedException {
		Thread.sleep(5);
		for (WebElement d : listOfDate) {
			if (d.getText().equals(dateValue)) {
				d.click();
				break;
			}
		}
	}

	/**
	 * Checks if error message is popped up when we try to add data archive existing
	 * archive name
	 * 
	 * @return True if error message is popped up o/w false
	 * @throws InterruptedException
	 */
	public boolean addSameArchiveData() throws InterruptedException {
		if (!checkArchive(LoginAndSelectModule.testData.get("archiveName"))) {
			log.debug("archive name does not exist to check error");
			throw new SkipException("Skipping test case as archive does not exist");
		} else {
			driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
			addDetails.click();
			driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
			archivalName.sendKeys(LoginAndSelectModule.testData.get("archiveName"));
			startDate.click();
			calenderArrow.click();
			selectDate(LoginAndSelectModule.testData.get("startYear"));
			selectDate(LoginAndSelectModule.testData.get("startMonth"));
			selectDate(LoginAndSelectModule.testData.get("startDate"));
			stopDate.click();
			calenderArrow.click();
			selectDate(LoginAndSelectModule.testData.get("endYear"));
			selectDate(LoginAndSelectModule.testData.get("endMonth"));
			selectDate(LoginAndSelectModule.testData.get("endDate"));
			noofdaystoRetain.sendKeys(LoginAndSelectModule.testData.get("daysToRetain"));
			saveButton.click();
			yesButton.click();
			wait.until(ExpectedConditions.elementToBeClickable(okButton));
			try {
				if (errorMessage.isDisplayed()) {
					okButton.click();
					log.info("successfully got an error message while adding archive with existing name");
					redirectToLandingPage.click();
					return true;
				}
			} catch (Exception e) {
				log.info("error while creating archive with existing name");
				redirectToLandingPage.click();
				return false;
			}
		}
		log.info("unexpected error");
		return false;
	}

	/**
	 * Checks if reset and redirect functionality are successful
	 * 
	 * @return True if reset and redirect functionality are successful o/w false
	 */
	public boolean resetAndRedirectFunctionality() {
		if (resetFunctionalityCheck()) {
			redirectButton.click();
			wait.until(ExpectedConditions.elementToBeClickable(addDetails));
			try {
				if (addDetails.isDisplayed()) {
					log.info("navigate to landing page successful");
					return true;
				}
			} catch (Exception e) {
				log.info("navigate to landing page unsuccessful");
				return false;
			}
			return true;
		}
		log.debug("redirect & reset functionality unsuccessful");
		throw new SkipException("Skipping test case as redirect & reset functionality unsuccessful");
	}

	/**
	 * checks if reset functionality is successful
	 * 
	 * @return true if reset functionality is successful o/w false
	 */
	private boolean resetFunctionalityCheck() {
		addDetails.click();
		archivalName.sendKeys(LoginAndSelectModule.testData.get("archiveName"));
		resetButton.click();
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		if (archivalName.getText().length() == 0) {
			log.info("reset functionality successful");
			return true;
		}
		log.info("reset functionality unsuccessful");
		return false;
	}
}
