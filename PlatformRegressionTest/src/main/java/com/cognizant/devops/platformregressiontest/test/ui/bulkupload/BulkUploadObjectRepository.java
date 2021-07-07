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
package com.cognizant.devops.platformregressiontest.test.ui.bulkupload;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

/**
 * @author NivethethaS
 * 
 *         Class contains the objects used for bulk upload module test cases
 *
 */
public class BulkUploadObjectRepository extends LoginAndSelectModule{

	Map<String, String> testData = new HashMap<>();

	@FindBy(xpath = "//div//a[contains(text(),'Bulk Upload')]")
	WebElement landingPage;

	@FindBy(xpath = "//span[contains(@class, 'mat-option-text')]")
	List<WebElement> toolnameList;

	@FindBy(xpath = "//tr[1]//div//span[contains(text(),'Select Tool')]")
	WebElement selectTool1;
	
	@FindBy(xpath = "//tr[2]//div//span[contains(text(),'Select Tool')]")
	WebElement selectTool2;
	
	@FindBy(xpath = "//tr[3]//div//span[contains(text(),'Select Tool')]")
	WebElement selectTool3;
	
	@FindBy(xpath = "//tr[4]//div//span[contains(text(),'Select Tool')]")
	WebElement selectTool4;
	
	@FindBy(xpath = "//tr[5]//div//span[contains(text(),'Select Tool')]")
	WebElement selectTool5;
	
	@FindBy(xpath = "//tr[1]//div//input[contains(@placeholder,'InsightsTimeField')]")
	WebElement timeField1;
	
	@FindBy(xpath = "//tr[2]//div//input[contains(@placeholder,'InsightsTimeField')]")
	WebElement timeField2;
	
	@FindBy(xpath = "//tr[3]//div//input[contains(@placeholder,'InsightsTimeField')]")
	WebElement timeField3;
	
	@FindBy(xpath = "//tr[4]//div//input[contains(@placeholder,'InsightsTimeField')]")
	WebElement timeField4;
	
	@FindBy(xpath = "//tr[5]//div//input[contains(@placeholder,'InsightsTimeField')]")
	WebElement timeField5;
	
	@FindBy(xpath = "//tr[1]//div//input[contains(@type,'file')]")
	WebElement file1;
	
	@FindBy(xpath = "//tr[2]//div//input[contains(@type,'file')]")
	WebElement file2;
	
	@FindBy(xpath = "//tr[3]//div//input[contains(@type,'file')]")
	WebElement file3;
	
	@FindBy(xpath = "//tr[4]//div//input[contains(@type,'file')]")
	WebElement file4;
	
	@FindBy(xpath = "//tr[5]//div//input[contains(@type,'file')]")
	WebElement file5;
	
	@FindBy(xpath = "//tr[1]//input[@name='InsightsTimeFormat']")
	WebElement timeFormatfield;
	
	@FindBy(xpath = "//tr[4]//mat-icon[@svgicon='healthcheck_success_status']")
	WebElement successTickrow4;
	
	@FindBy(xpath = "//tr[2]//mat-icon[@svgicon='healthcheck_success_status']")
	WebElement successTickrow2;
	
	@FindBy(xpath = "//mat-icon[@svgicon='healthcheck_failure_status']")
	WebElement failureStatus;
	
	@FindBy(xpath = "//tr[1]//mat-icon[@svgicon='healthcheck_failure_status']")
	WebElement failureStatusRow1;
	
	@FindBy(xpath = "//tr[3]//mat-icon[@svgicon='healthcheck_failure_status']")
	WebElement failureStatusRow3;
	
	@FindBy(xpath = "//mat-icon[@title='Save ']")
	WebElement saveButton;
	
	@FindBy(xpath = "//mat-icon[@title='Cancel ']")
	WebElement resetButton;
	
	@FindBy(xpath = "//div[contains(text(),'Upload the Data')]")
	WebElement uploadMessage;
	
	@FindBy(xpath = "//span[text()= 'YES']")
	WebElement yesButton;
	
	@FindBy(xpath = "//div[contains(text(), ' Success')]")
	WebElement successMessage;
	
	@FindBy(xpath = "//span[text()= 'OK']")
	WebElement okButton;
	
}
