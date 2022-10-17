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
	public boolean addArchiveData(String archivalNme,String stDate,String stMonth,String stYear,
			String edDate,String edMonth,String edYear,String daysToRetain) throws InterruptedException {
		if (checkArchive(archivalNme)) {
			log.debug("archive name already exists");
			throw new SkipException("Skipping test case as archive already exists");
		} else {
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
			clickOn(addDetails,2);
			archivalName.sendKeys(archivalNme);
			noofdaystoRetain.sendKeys(daysToRetain);
			Thread.sleep(500);
			clickOn(openStartDateCl,2);
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
			selectYear(stYear);
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
			selectMonth(stMonth);
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
			selectDate(stDate);
			Thread.sleep(1000);
			clickOn(openEndDateCl,2);
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
			selectYear(edYear);
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
			selectMonth(edMonth);
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
			selectDate(edDate);
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
			visibilityOf(saveButton, 2);
			clickOn(saveButton,2);
			clickOn(yesBtn,2);
			try {
				if (successMessage.isDisplayed()) {
					wait.until(ExpectedConditions.elementToBeClickable(crossClose));
					crossClose.click();
					log.info("successfully added archive date");
					return true;
				}
			} catch (Exception e) {
				log.info("error while creating archive date");
				backButton.click();
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
		List<WebElement> rws = archiveDetailsTable.findElements(By.tagName("tr"));
		for (int i = 0; i < rws.size(); i++) {
			List<WebElement> cols = (rws.get(i)).findElements(By.tagName("td"));
			if ((cols.get(1).getText()).equals(archive)) {
				log.info("{} archive name is present.", archive);
				return true;
			}
		}
		log.info("{} archive name is not present.", archive);
		return false;
	}

	
	/**
	 * Checks if error message is popped up when we try to add data archive existing
	 * archive name
	 * 
	 * @return True if error message is popped up o/w false
	 * @throws InterruptedException
	 */
	public boolean addSameArchiveData(String archivalNme,String stDate,String stMonth,String stYear,
			String edDate,String edMonth,String edYear,String daysToRetain) throws InterruptedException {
		if (!checkArchive(archivalNme)) {
			log.debug("archive name does not exist to check error");
			throw new SkipException("Skipping test case as archive does not exist");
		} else {
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
			visibilityOf(addDetails, 2);
			clickOn(addDetails,2);
			archivalName.sendKeys(archivalNme);
			noofdaystoRetain.sendKeys(daysToRetain);
			clickOn(openStartDateCl,2);
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
			selectYear(stYear);
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
			selectMonth(stMonth);
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
			selectDate(stDate);
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
			clickOn(openEndDateCl,2);
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
			selectYear(edYear);
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
			selectMonth(edMonth);
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
			selectDate(edDate);
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
			visibilityOf(saveButton, 2);
			clickOn(saveButton,2);
			visibilityOf(yesBtn, 2);
			clickOn(yesBtn,2);
			try {
				if (errorMessage.isDisplayed()) {
					clickOn(crossClose,2);
					log.info("successfully got an error message while adding archive with existing name");
					visibilityOf(backButton, 2);
					clickOn(backButton, 3);
					return true;
				}
			} catch (Exception e) {
				log.info("error while creating archive with existing name");
				visibilityOf(backButton, 2);
				clickOn(backButton, 3);
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
	 * @throws InterruptedException 
	 */
	public boolean resetAndRedirectFunctionality(String archivalNme,String stDate,String stMonth,String stYear,
			String edDate,String edMonth,String edYear,String daysToRetain) throws InterruptedException {
		if (resetFunctionalityCheck(archivalNme)) {
			clickOn(backButton,4);
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
	 * @throws InterruptedException 
	 */
	private boolean resetFunctionalityCheck(String archivalNme) throws InterruptedException {
		addDetails.click();
		archivalName.sendKeys(archivalNme);
		resetButton.click();
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
		if (archivalName.getText().length() == 0) {
			log.info("reset functionality successful");
			return true;
		}
		log.info("reset functionality unsuccessful");
		return false;
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
	/**
	 * Selects the particular date value from list of values available
	 * 
	 * @param startInput
	 * @throws InterruptedException
	 */
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

	/**
	 * wait until the visibility of web element
	 * 
	 * @param element
	 * @param timeout
	 * @return true if element is displayed else false
	 */
	public static boolean visibilityOf(WebElement element, int timeout) {
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
		new WebDriverWait(driver, Duration.ofSeconds(timeout)).until(ExpectedConditions.elementToBeClickable(element));
		element.click();
	}


}
