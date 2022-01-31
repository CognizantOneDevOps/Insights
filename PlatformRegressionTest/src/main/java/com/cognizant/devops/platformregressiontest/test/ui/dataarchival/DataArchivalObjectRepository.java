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
	
	@FindBy(xpath = "//mat-icon[@title='Add']")
	WebElement addDetails;

	@FindBy(xpath = "//tbody//tr[1]//td[2]//input")
	WebElement archivalName;

	@FindBy(xpath = "//tbody//tr[2]//td[2]//span/*[name()='svg']")
	WebElement startDate;

	@FindBy(xpath = "//tbody//tr[3]//td[2]//span/*[name()='svg']")
	WebElement stopDate;

	@FindBy(xpath = "//tbody//tr[4]//td[2]//input")
	WebElement noofdaystoRetain;

	@FindBy(xpath = "//mat-icon[@title='Save']")
	WebElement saveButton;

	@FindBy(xpath = "//mat-icon[@title='Redirect To Landing Page']")
	WebElement redirectToLandingPage;

	@FindBy(xpath = "//mat-icon[@title='Delete']")
	WebElement deleteButton;

	@FindBy(xpath = "//mat-icon[@title='View Details']")
	WebElement viewDetailsButton;

	@FindBy(xpath = "//table[@role='presentation']//tbody//tr//td")
	List<WebElement> listOfDate;

	@FindBy(xpath = "//span[text()= 'OKAY']")
	WebElement okButton;

	@FindBy(xpath = "//span[text()= 'YES']")
	WebElement yesButton;

	@FindBy(xpath = "//div[contains(text(), ' Success')]")
	WebElement successMessage;

	@FindBy(xpath = "//div[contains(text(), ' Error')]")
	WebElement errorMessage;

	@FindBy(xpath = "//div[@class='mat-calendar-arrow']")
	WebElement calenderArrow;

	@FindBy(xpath = "//table//tbody[@role='rowgroup']")
	WebElement archiveDetailsTable;

	@FindBy(xpath = "//mat-icon[@title='Redirect To Landing Page']")
	WebElement redirectButton;

	@FindBy(xpath = "//mat-icon[@title='Reset']")
	WebElement resetButton;

	@FindBy(xpath = "//div[contains(text(),'Data Archival')]")
	WebElement landingPage;

}
