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
package com.cognizant.devops.platformregressiontest.test.ui.datadictionary;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

public class DataDictionaryobjectRepository extends LoginAndSelectModule {

	@FindBy(xpath = "//a[contains(text(),'Data Dictionary')]")
	WebElement landingPage;

	@FindBy(xpath = "//mat-select[@name='selectedSourceTool']")
	WebElement selectSourceTool;

	@FindBy(xpath = "//mat-select[@name='selectedLabels']")
	WebElement selectSourceLabel;

	@FindBy(xpath = "//span[contains(text(),'Select Destination Tool')]")
	WebElement selectDestinationTool;

	@FindBy(xpath = "//mat-select[@name='selectedDestinationLabel']")
	WebElement selectDestinationLabel;
	
	@FindBy(xpath = "//span[contains(@class, 'mat-option-text')]")
	public List<WebElement> sourceToolList;

	@FindBy(xpath = "//span[contains(@class, 'mat-option-text')]")
	public List<WebElement> destinationToolList;
	
	@FindBy(xpath = "//td[contains(@class,'cdk-column-ToolName')]")
	public List<WebElement> toolNameList;
	
	@FindBy(xpath = "//mat-select[@name='selectTool']")
	WebElement toolList;
	
	@FindBy(xpath = "//span[contains(@class, 'mat-option-text')]")
	public List<WebElement> toolListOption;

	@FindBy(xpath = "//span[contains(text(),'Click to show Correlation')]")
	WebElement showCorrelation;
	
	@FindBy(xpath = "//b[contains(text(),'Relationship Name')]")
	WebElement relationshipName;
}
