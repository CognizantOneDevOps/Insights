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

public class MileStoneObjectRepository extends LoginAndSelectModule {
	@FindBy(xpath = "//mat-icon[@svgicon='add']")
	WebElement clickAddButton;
	
	@FindBy(xpath = "//input[@formcontrolname='mileStoneName']") 
	WebElement mileStoneName;
	
	@FindBy(xpath = "//input[@formcontrolname='milestoneReleaseID']") 
	WebElement milestoneReleaseID;
	
	@FindBy(xpath = "//mat-icon[@id='startDate']")  // As it is
	WebElement startDateCalendar;
	
	@FindBy(xpath = "//button[contains(@class,'mat-calendar-period-button')]")
	WebElement selectYearArrowButton;
	
	@FindBy(xpath = "//mat-checkbox[contains(@class,'mat-checkbox')]")
	WebElement selectOutcome;
	
	@FindBy(xpath = "//mat-icon[@id='endDate']")  // As it is
	WebElement endDateCalendar;
	
	@FindBy(xpath = "//div[contains(@class, 'mat-calendar-body-cell')]")
	public List<WebElement> yearList;
	
	@FindBy(xpath = "//div[contains(@class, 'mat-calendar-body-cell-content')]")
	public List<WebElement> monthList;

	@FindBy(xpath = "//div[contains(@class, 'mat-calendar-body-cell-content')]")
	public List<WebElement> dateList;
	
	@FindBy(xpath = "//div[3]/mat-form-field/div/div[1]/div[2]/mat-datepicker-toggle/button/span[1]/mat-icon")
	WebElement selectStartDate1;
	
	@FindBy(xpath = "//div[4]/mat-form-field/div/div[1]/div[2]/mat-datepicker-toggle/button/span[1]/mat-icon")
	WebElement selectEndDate;
	
	@FindBy(xpath = "//div[contains(text(),'MileStone Configuration')]")
	WebElement landingPage;
	
	@FindBy(xpath = "//mat-icon[@svgicon='search']")
	WebElement chooseOutcome;
	
	@FindBy(xpath = "//button[@id = 'yesBtn']")  
	WebElement yesButton;
	
	@FindBy(xpath = "//input[@placeholder='Search']") 
	WebElement searchOutcome;
	
	@FindBy(xpath = "//span[contains(text(),'OK')]")
	WebElement okButton;
	
	@FindBy(xpath = "//mat-icon[@svgicon='save']")
	WebElement save;
	
	@FindBy(xpath = "//td[contains(@class, 'mileStoneName')]")
	List<WebElement> mileStoneNameList;
	
	@FindBy(xpath = "//td[contains(@class, 'outcomeName')]")
	List<WebElement> outcomeList;
	
	@FindBy(xpath = "//div[contains(text(),' Save MileStone Configuration  ')]")
	WebElement saveMilestone;
	
	@FindBy(xpath = "//button[contains(@id,'yesBtn')]")
	WebElement yes;
	
	@FindBy(xpath = "//button[contains(@id,'savebtn')]")
	WebElement saveBtn;
	
	@FindBy(xpath = "//span[contains(text(),'Saved Successfully')]")
	WebElement successMessage;
	
	@FindBy(xpath = "//span[contains(text(),'Start Date cannot be greater than End Date')]")
	WebElement errorMessage;
	
	@FindBy(xpath = "//span[contains(text(),'Updated Successfully')]")
	WebElement updateSuccessMessage;
	
	@FindBy(xpath = "//b[contains(text(),'Deleted Successfully')]")
	WebElement deletedMessage;
	
	@FindBy(xpath = "//mat-icon[@svgicon='cross']")
	WebElement crossClose;
	
	@FindBy(xpath = "//mat-icon[@svgicon='close_dialog']")
	WebElement dialogClose;
	
	@FindBy(xpath = "//mat-icon[@svgicon='trash']")
	WebElement clickDelete;
	
	@FindBy(xpath = "//mat-icon[@svgicon='retry']")
	WebElement checkRefresh;
	
	@FindBy(xpath = "//mat-icon[@svgicon='healthcheck_show_details']")
	WebElement checkHeathDetails;
	
	@FindBy(xpath = "//table[@id='outcomeTable']")
	WebElement outcomeTable;
	
	@FindBy(xpath = "//mat-icon[@svgicon='exit']")
	WebElement resetButton;
	
	@FindBy(xpath = "//mat-icon[@svgicon='backButton']")
	WebElement backButton;
	
	@FindBy(xpath = "//mat-icon[@svgicon='edit']")
	WebElement clickEditButton;
	
	@FindBy(xpath = "//span[contains(text(), 'Milestone already in progress, you cannot delete this Milestone. ')]")
	WebElement milestoneDeleteIssue;
	
}