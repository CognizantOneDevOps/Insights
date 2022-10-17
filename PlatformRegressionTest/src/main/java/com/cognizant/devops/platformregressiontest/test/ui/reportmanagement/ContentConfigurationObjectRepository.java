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

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

public class ContentConfigurationObjectRepository extends LoginAndSelectModule {

	@FindBy(xpath = "//div[contains(text(),'  Content Configuration ')]")
	WebElement landingPage;

	@FindBy(xpath = "//td[contains(@class, 'mat-column-ContentId')]")
	List<WebElement> contentListEl;

	@FindBy(xpath = "//mat-icon[@svgicon='add']")
	WebElement addNewContentButtonEl;

	// mat-icon[@title="Add New KPI"]

	@FindBy(xpath = "//div[contains(text(),'Message should contain')]")
	WebElement contentMessageValidatorEl;

	@FindBy(xpath = "//div[contains(text(),'Count or Percentage')]")
	WebElement thresholdRangeActionValidatorEl;

	@FindBy(xpath = "//div[contains(text(),'Count,Percentage or Average')]")
	WebElement thresholdActionValidatorEl;

	@FindBy(xpath = "//div[contains(text(),'Min or Max')]")
	WebElement minmaxActionValidatorEl;

	@FindBy(xpath = "//div[contains(text(),'red,amber and green values')]")
	WebElement thresholdRangeValidatorEl;

	@FindBy(xpath = "//span[contains(text(),'Message should contain Positive,Negative and Neutral Messages')]")
	WebElement comparisonMsgValidationEl;

	@FindBy(xpath = "//input[@name='contentId']")
	WebElement contentIdEl;

	@FindBy(xpath = "//b[contains(text(),'Content Definition already exists')]")
	WebElement contentIdExistsEl;
	
	@FindBy(xpath = "//button[@id='crossClose']")
	WebElement crossClose;

	@FindBy(xpath = "//input[@name='contentName']")
	WebElement contentNameEl;

	@FindBy(xpath = "//b[contains(text(),'Content definition updated')]")
	WebElement contentUpdatedEl;

	@FindBy(xpath = "//mat-icon[@svgicon='search']")
	WebElement searchKpiEl;

	@FindBy(xpath = "//td[contains(@class, 'mat-column-kpiId ')]")
	List<WebElement> kpiListEl;

	@FindBy(xpath = "//input[@placeholder='Search']")
	WebElement kpiInputEl;

	@FindBy(xpath = "//span[contains(text(),' OK ')]")
	WebElement kpiSelectBtnEl;

	@FindBy(xpath = "//mat-select[@name='expectedTrend']")
	WebElement expectedTrendEl;

	@FindBy(xpath = "//span[contains(@class, 'mat-option-text')]")
	public List<WebElement> expectedTrendListEl;

	@FindBy(xpath = "//input[@name='resultField']")
	WebElement resultFieldEl;

	@FindBy(xpath = "//mat-select[@name='action']")
	WebElement actionEl;

	@FindBy(xpath = "//span[contains(@class, 'mat-option-text')]")
	public List<WebElement> actionListEl;

	@FindBy(xpath = "//mat-select[@name='directionThreshold']")
	WebElement directionThresholdEl;

	@FindBy(xpath = "//span[contains(@class, 'mat-option-text')]")
	public List<WebElement> directionThresholdListEl;

	@FindBy(xpath = "//textarea[@name='threshold']")
	WebElement thresholdEl;

	@FindBy(xpath = "//textarea[@name='thresholds']")
	WebElement thresholdsEl;

	@FindBy(xpath = "//mat-slide-toggle[contains(@class,'mat-slide-toggle')]")
	WebElement isActiveEl;

	@FindBy(xpath = "//textarea[@name='message']")
	WebElement messageEl;

	@FindBy(xpath = "//mat-icon[@svgicon='save']")
	WebElement saveBtnEl;

	@FindBy(xpath = "//button[@id='yesBtn']")
	WebElement yesBtnEl;

	@FindBy(xpath = "//span[contains(text(),'file is not a valid JSON')]")
	WebElement uploadJsonValidatorEl;

	@FindBy(xpath = "//span[text()='YES']/parent::button")
	WebElement yBtnEl;

	@FindBy(xpath = "//span[text()= 'OKAY']")
	WebElement btnOKEl;

	@FindBy(xpath = "//mat-icon[@svgicon='trash']")
	WebElement delBtnEl;

	@FindBy(xpath = "//mat-icon[@svgicon='edit']")
	WebElement btnEditEl;

	@FindBy(xpath = "//mat-icon[@svgicon='upload']")
	WebElement uploadBtnE1;

	@FindBy(xpath = "//input[@type='file']")
	WebElement chooseFileBtnE1;

	@FindBy(xpath = "//mat-icon[@title='Upload ']")
	WebElement uploadJsonBtnE1;

	@FindBy(xpath = "//mat-icon[@svgicon='close_dialog']")
	WebElement uploadJsonCancelBtnE1;

	@FindBy(xpath = "//span[text()='OK']")
	WebElement okBtnE1;

	//// mat-icon[@title="Refresh"]
	@FindBy(xpath = "//mat-icon[@svgicon='retry']")
	WebElement refreshBtnE1;

	@FindBy(xpath = "//input[@placeholder='Search']")
	public WebElement searchContentEl;

	// ="//b[contains(text(),'deleted')]")
	@FindBy(xpath = "//b[contains(text(),'deleted')]")
	WebElement contentDeletedEl;

	@FindBy(xpath = "//div[contains(text(),'You have created ')]")
	WebElement contentAddedEl;
}
