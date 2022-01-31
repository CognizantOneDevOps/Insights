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
package com.cognizant.devops.platformregressiontest.test.ui.traceabilitydashboard;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

public class TraceabilityDashboardObjectRepository extends LoginAndSelectModule{

	@FindBy(xpath = "//div//a[contains(text(),'Traceability Dashboard')]")
	WebElement landingPage;

	@FindBy(xpath = "//div//span[contains(text(),'Select Tool')]")
	WebElement selectTool;

	@FindBy(xpath = "//div//span[contains(text(),'Select Field')]")
	WebElement selectField;

	@FindBy(xpath = "//div//span[contains(text(),'Issue Type')]")
	WebElement issueType;
	
	@FindBy(xpath = "//input[@name='toolFieldValue']")
	WebElement toolFieldValue;
	
	@FindBy(xpath = "//button//span[contains(text(),'Search')]")
	WebElement searchButton;
	
	@FindBy(xpath = "//button//span[contains(text(),'Clear')]")
	WebElement clearButton;
	
	@FindBy(xpath = "//div[@role='tab']//div[contains(text(),'Summary')]")
	WebElement summaryTab;
	
	@FindBy(xpath = "//div[@role='tab']//div[contains(text(),'Pipeline')]")
	WebElement pipelineTab;
	
	@FindBy(xpath = "//div[@class='column2 ng-star-inserted'][1]//span[contains(text(),'more info')]")
	WebElement moreInfo;
	
	@FindBy(xpath = "//div[contains(text(),'Additional Details')]")
	WebElement additionalDetails;
	
	@FindBy(xpath = "//mat-icon[@svgicon='close_dialog']")
	WebElement closeDialog;
	
	@FindBy(xpath = "//div[contains(text(), ' Error')]")
	WebElement errorMessage;
	
	@FindBy(xpath = "//span[text()= 'OKAY']")
	WebElement okButton;
	
	@FindBy(xpath = "//span[contains(@class, 'mat-option-text')]")
	List<WebElement> dropdownList;
}
