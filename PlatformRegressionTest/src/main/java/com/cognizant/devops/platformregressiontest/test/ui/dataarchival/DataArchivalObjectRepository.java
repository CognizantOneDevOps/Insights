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

import java.util.List;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

/**
 * @author NivethethaS
 * 
 *         Class contains the objects used for Data Archival module
 *
 */
public class DataArchivalObjectRepository extends LoginAndSelectModule{
	
	@FindBy(xpath = "//mat-icon[@svgicon='add']")
	WebElement addDetails;

	@FindBy(xpath = "//mat-icon[@svgicon='edit']")
	WebElement editDetails;
	
	@FindBy(xpath = "//mat-icon[@svgicon='trash']")
	WebElement deleteDetails;

	@FindBy(xpath = "//mat-icon[@svgicon='view']")
	WebElement viewDetails;

	@FindBy(xpath = "//input[@name='archivalName']")
	WebElement archivalName;
	
	@FindBy(xpath = "//input[@name='DaysToRetain']")
	WebElement noofdaystoRetain;

	@FindBy(xpath = "//mat-icon[@svgicon='saveHook']")
	WebElement saveButton;

	@FindBy(xpath = "//mat-icon[@svgicon='backButton']")
	WebElement backButton;

	@FindBy(xpath = "//button[@id='yesBtn']")
	WebElement yesBtn;

	@FindBy(xpath = "//span[contains(text(),'successfully')]")
	WebElement successMessage;

	@FindBy(xpath = "//span[contains(text(), ' already exists. Please try again with a new name.')]")
	WebElement errorMessage;

	@FindBy(xpath = "//div[@class='mat-calendar-arrow']")
	WebElement calenderArrow;

	@FindBy(xpath = "//table//tbody[@role='rowgroup']")
	WebElement archiveDetailsTable;

	@FindBy(xpath = "//mat-icon[@title='Redirect To Landing Page']")
	WebElement redirectButton;

	@FindBy(xpath = "//mat-icon[@svgicon='exit']")
	WebElement resetButton;

	@FindBy(xpath = "//div[contains(text(),'Data Archival')]")
	WebElement landingPage;
	
	@FindBy(xpath = "//mat-icon[@id='stDate']")
	WebElement openStartDateCl;

	@FindBy(xpath = "//mat-icon[@id='endDate']")
	WebElement openEndDateCl;

	
	@FindBy(xpath = "//button[contains(@class,'mat-calendar-period-button')]")
	WebElement selectYearArrowButton;

	@FindBy(xpath = "//div[contains(@class, 'mat-calendar-body-cell')]")
	public List<WebElement> yearList;

	@FindBy(xpath = "//div[contains(@class, 'mat-calendar-invert')]")
	WebElement selectMonthArrowButton;

	@FindBy(xpath = "//div[contains(@class, 'mat-calendar-body-cell-content')]")
	public List<WebElement> monthList;

	@FindBy(xpath = "//div[contains(@class, 'mat-calendar-body-cell-content')]")
	public List<WebElement> dateList;

	@FindBy(xpath = "//button[@id='crossClose']")
	WebElement crossClose;


}
