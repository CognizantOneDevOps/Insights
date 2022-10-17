/*******************************************************************************
 * Copyright 2022 Cognizant Technology Solutions
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
package com.cognizant.devops.platformregressiontest.test.ui.ROI;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

public class OutcomeObjectRepository extends LoginAndSelectModule {
	
	@FindBy(xpath = "//span//b[contains(text(),'Outcome Configuration')]")
	WebElement landingPage;
	
	@FindBy(xpath = "//mat-icon[@svgicon='add']")
	WebElement clickAddButton;
	
	@FindBy(xpath = "//span[contains(text(),'Add details to configure an Outcome')]")
	WebElement mainHeader;
	
	@FindBy(xpath = "//input[@formcontrolname='outcomeName']") 
	WebElement outcomeName;
	
	@FindBy(xpath = "//mat-select[@formcontrolname='outcomeType']")
	WebElement outcomeType;
	
	@FindBy(xpath = "//mat-select[@formcontrolname='toolName']")
	WebElement toolName;
	
	@FindBy(xpath = "//input[@placeholder='Enter Summary Index']") 
	WebElement indexName;
	
	@FindBy(xpath = "//textarea[@formcontrolname='metricUrl']")
	WebElement metricUrl;
	
	@FindBy(xpath = "//input[@formcontrolname='key']") 
	WebElement nameRequestParam;
	
	@FindBy(xpath = "//input[@formcontrolname='value']") 
	WebElement valueRequestParam;
	
	@FindBy(xpath = "//td[contains(@class, 'cdk-column-outcomeName')]")
	List<WebElement> outcomeList;
	
	@FindBy(xpath = "//mat-icon[@svgicon='save']")
	WebElement save;
	
	@FindBy(xpath = "//div[contains(text(),' Save Outcome Configuration  ')]")
	WebElement saveOutcome;
	
	@FindBy(xpath = "//div[contains(text(),' Update Active/Inactive State ')]")
	WebElement updateStatus;
	
	@FindBy(xpath = "//button[contains(@id,'yesBtn')]")
	WebElement yes;
	
	@FindBy(xpath = "//span[contains(text(),'Saved Successfully')]")
	WebElement successMessage;
	
	@FindBy(xpath = "//span[contains(text(),'Updated Successfully')]")
	WebElement successEdit;
	
	@FindBy(xpath = "//span[contains(text(),'Deleted Successfully')]")
	WebElement successDelete;
	
	@FindBy(xpath = "//mat-icon[@svgicon='cross']")
	WebElement crossClose;
	
	@FindBy(xpath = "//mat-icon[@svgicon='edit']")
	WebElement clickEditButton;
	
	@FindBy(xpath = "//mat-icon[@svgicon='trash']")
	WebElement clickDelete;
	
	@FindBy(xpath = "//mat-icon[@svgicon='retry']")
	WebElement checkRefresh;

	@FindBy(xpath = "//span[contains(text(),'Outcome with given name already exists.')]")
	WebElement duplicateOutcome;
	
	@FindBy(xpath = "//mat-icon[@svgicon='backButton']")
	WebElement backButton;
	
	@FindBy(xpath = "//div[contains(text(),'of 3')]")
	WebElement getTotalPages;
	
	@FindBy(xpath = "//mat-icon[@svgicon='next-page']")
	WebElement nextPage;
	
}
