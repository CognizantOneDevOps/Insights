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
package com.cognizant.devops.platformregressiontest.test.ui.configurationfilemanagement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

/**
 * @author NivethethaS
 * 
 *         Class contains the objects used for Configuration File Management
 *         module test cases
 *
 */
public class ConfigurationFileManagementObjectRepository extends LoginAndSelectModule {

	Map<String, String> testData = new HashMap<>();

	@FindBy(xpath = "//span[contains(text(),'Configuration ')]")
	WebElement configurationLandingPage;

	@FindBy(xpath = "//mat-icon[@svgicon='add']")
	WebElement addButton;

	@FindBy(xpath = "//input[@placeholder='Enter file name']")
	WebElement fileName;

	@FindBy(xpath = "//mat-select[@placeholder='Select file type']")
	WebElement fileType;

	@FindBy(xpath = "//span[contains(@class, 'mat-option-text')]")
	List<WebElement> fileTypeList;
	
	@FindBy(xpath = "//td[contains(@class, 'mat-column-fileName')]")
	List<WebElement> fileNameList;
	
	@FindBy(xpath = "//mat-select[@placeholder='Select file module']")
	WebElement fileModule;

	@FindBy(xpath = "//span[contains(@class, 'mat-option-text')]")
	List<WebElement> fileModuleList;

	@FindBy(xpath = "//table//tbody[@role='rowgroup']")
	WebElement configurationDetailsTable;

	@FindBy(xpath = "//input[@id='file']")
	WebElement uploadFile;

	@FindBy(xpath = "//mat-icon[@svgicon='edit']")
	WebElement editButton;

	@FindBy(xpath = "//mat-icon[@svgicon='trash']")
	WebElement deleteButton;

	@FindBy(xpath ="//mat-icon[@svgicon='retry']")
	WebElement refreshButton;

	@FindBy(xpath ="//mat-icon[@svgicon='exit']")
	WebElement resetButton;

	@FindBy(xpath = "//button[@id='yesBtn']")
	WebElement yesButton;

	@FindBy(xpath = "//button[@id='crossClose']")
	WebElement crossClose;

	@FindBy(xpath = "//mat-icon[@svgicon='save']")
	WebElement saveButton;
	
	@FindBy(xpath = "//span[contains(text(),'successfully.')]")
	WebElement successMessage;
	
	@FindBy(xpath = "//span[text()='File already exists in database.']")
	WebElement errorMessage;
	
	@FindBy(xpath = "//mat-icon[@svgicon='backButton']")
	WebElement redirectButton;

	@FindBy(xpath = "//div[contains(text(),'File Management')]")
	WebElement landingPage;
}
