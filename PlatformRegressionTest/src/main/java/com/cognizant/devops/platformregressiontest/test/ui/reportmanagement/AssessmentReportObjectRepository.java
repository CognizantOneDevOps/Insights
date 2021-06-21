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

public class AssessmentReportObjectRepository extends LoginAndSelectModule {

	@FindBy(xpath = "//mat-icon[@title='Add']")
	WebElement addButton;

	@FindBy(xpath = "//div[contains(text(),'Add Report')]")
	WebElement addReport;

	@FindBy(xpath = "//input[@placeholder='Enter a report name']")
	WebElement reportNameEl;;

	@FindBy(xpath = "//input[@placeholder='Enter a Report Title']")
	WebElement titleNameEl;

	@FindBy(xpath = "//mat-select[@placeholder='Select report template']")
	static WebElement reportTemplateDropDownEl;

	@FindBy(xpath = "//span[contains(@class,'mat-option-text')]")
	public List<WebElement> reportTemplateList;

	@FindBy(xpath = "//mat-select[@name='schedule']")
	WebElement frequencyDropDownEl;

	@FindBy(xpath = "//span[contains(@class, 'mat-option-text')]")
	public List<WebElement> frequencyNameList;

	@FindBy(xpath = "//div[@class='mat-calendar-arrow']")
	WebElement selectYearArrowButton;

	@FindBy(xpath = "//div[contains(@class, 'mat-calendar-body-cell')]")
	public List<WebElement> yearList;

	@FindBy(xpath = "//div[contains(@class, 'mat-calendar-invert')]")
	WebElement selectMonthArrowButton;

	@FindBy(xpath = "//div[contains(@class, 'mat-calendar-body-cell-content')]")
	public List<WebElement> monthList;

	@FindBy(xpath = "//div[contains(@class, 'mat-calendar-body-cell-content')]")
	public List<WebElement> dateList;

	@FindBy(xpath = "//mat-icon[@title= 'Add Tasks']")
	WebElement addTaskButton;

	@FindBy(xpath = "(//div[@id='fromAllTaskList'])[1]")
	WebElement kpiTask;

	@FindBy(xpath = "//span[text()=' ADD ']")
	WebElement taskaddButton;

	@FindBy(xpath = "(//div[@id='fromAllTaskList'])[2]")
	WebElement pdfTask;

	@FindBy(xpath = "//mat-icon[@title='Add Email']")
	WebElement clickMailingDetails;

	@FindBy(xpath = "//input[@name='senderEmailAddress']")
	WebElement mailFromEl;

	@FindBy(xpath = "//textarea[@name='receiverEmailAddress']")
	WebElement mailToEl;

	@FindBy(xpath = "//textarea[@name='receiverCCEmailAddress']")
	WebElement ccReceiverMailAddressEl;

	@FindBy(xpath = "//textarea[@name='receiverBCCEmailAddress']")
	WebElement bccReceiverMailAddressEl;

	@FindBy(xpath = "//input[@name='mailSubject']")
	WebElement mailSubjectEl;

	@FindBy(xpath = "//textarea[@name='mailBodyTemplate']")
	WebElement mailBodyTemplateEl;

	@FindBy(xpath = "//input[@placeholder='Enter a report name']")
	WebElement reportName;

	@FindBy(xpath = "//input[@placeholder='Enter a Report Title']")
	WebElement titleName;

	@FindBy(xpath = "//mat-select[@placeholder='Select report template']")
	public WebElement reportTemplateDropDown;

	@FindBy(xpath = "//mat-select[@name='schedule']")
	WebElement frequencyDropDown;

	@FindBy(xpath = "//input[@name='senderEmailAddress']")
	WebElement mailFrom;

	@FindBy(xpath = "//textarea[@name='receiverEmailAddress']")
	WebElement mailTo;

	@FindBy(xpath = "//textarea[@name='receiverCCEmailAddress']")
	WebElement ccReceiverMailAddress;

	@FindBy(xpath = "//textarea[@name='receiverBCCEmailAddress']")
	WebElement bccReceiverMailAddress;

	@FindBy(xpath = "//input[@name='mailSubject']")
	WebElement mailSubject;

	@FindBy(xpath = "//textarea[@name='mailBodyTemplate']")
	WebElement mailBodyTemplate;

	@FindBy(xpath = "(//button[contains(@class,'configureBut mat-raised-button')])[2]")
	WebElement addMail;

	@FindBy(xpath = "//mat-icon[@title='Save']")
	WebElement saveButton;

	@FindBy(xpath = "//div[contains(@class, 'gridheadercenter')]")
	WebElement saveReportConfirmationMessage;

	@FindBy(xpath = "//span[text()= 'YES']")
	WebElement yesButton;

	@FindBy(xpath = "//div[text()= ' Success ']")
	WebElement successMessage;

	@FindBy(xpath = "//button[contains(@class,'mat-raised-button')]")
	WebElement okButton;

	@FindBy(xpath = "//div[@class = 'sectionHeadingStyle']")
	WebElement reportsLandingPage;

	@FindBy(xpath = "//td[contains(@class, 'reportName')]")
	List<WebElement> reportsNameList;

	@FindBy(xpath = "//div[contains(@class, 'mat-radio-ripple mat-ripple')]")
	public List<WebElement> radioButtonsList;

	@FindBy(xpath = "//mat-icon[@title='Edit ']")
	WebElement clickEditButton;

	@FindBy(xpath = "//mat-icon[@title='Activate report and run immediately']")
	WebElement immiediateRunButton;

	@FindBy(xpath = "//div[text()=' Start Report Execution ']")
	WebElement immediateRunConfirmMessage;

	@FindBy(xpath = "//span[text() ='YES']")
	WebElement immediateRunClickYes;

	@FindBy(xpath = "//div[text() =' Success ']")
	WebElement immediateRunSuccess;

	@FindBy(xpath = "//span[text() ='OK']")
	WebElement immediateRunOk;

	@FindBy(xpath = "//div[text()=' Update Active/Inactive State ']")
	WebElement confirmationMessage;

	@FindBy(xpath = "//span[text()='YES']")
	WebElement clickYes;

	@FindBy(xpath = "//mat-icon[@title='Delete ']")
	WebElement clickDelete;

	@FindBy(xpath = "//span[text()='OK']")
	WebElement clickOk;

}
