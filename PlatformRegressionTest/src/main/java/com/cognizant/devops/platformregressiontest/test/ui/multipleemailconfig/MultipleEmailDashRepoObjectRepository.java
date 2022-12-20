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
package com.cognizant.devops.platformregressiontest.test.ui.multipleemailconfig;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

public class MultipleEmailDashRepoObjectRepository extends LoginAndSelectModule {

	@FindBy(xpath = "//div[contains(text(),'Dashboard Report Configuration')]")   //
	WebElement dashRepoLandingPage;
	
	@FindBy(xpath = "//div[contains(text(),' Email Configuration')]")   //
	WebElement landingPage;
	
	@FindBy(xpath = "//mat-icon[@svgicon='email_config']")  //
	WebElement clickEmailConfigButton;
	
	@FindBy(xpath = "//mat-icon[@id='addReport']")  //
	WebElement clickAddReport;
	
	@FindBy(xpath = "//mat-icon[@svgicon='add']")  //
	WebElement clickAddButton;
	
	@FindBy(xpath = "//mat-icon[@svgicon='minus']")  //
	WebElement clickMinusButton;
	
	@FindBy(xpath = "//input[@formcontrolname='batchName']")  //
	WebElement batchName;
	
	@FindBy(xpath = "//mat-select[@formcontrolname='schedule']")  //
	WebElement selectSchedule;
	
	@FindBy(xpath = "//mat-select[@formcontrolname='report']")  //
	WebElement selectReport;
	
	@FindBy(xpath = "//input[@formcontrolname='receiverEmailAddress']")  //
	WebElement receiverEmailAddress;
	
	@FindBy(xpath = "//input[@formcontrolname='mailSubject']")  //
	WebElement mailSubject;
	
	@FindBy(xpath = "//input[@formcontrolname='receiverCCEmailAddress']")  //
	WebElement receiverCCEmailAddress;
	
	@FindBy(xpath = "//input[@formcontrolname='receiverBCCEmailAddress']")  //
	WebElement receiverBCCEmailAddress;
	
	@FindBy(xpath = "//textarea[@name='mailBodyTemplate']")
	WebElement mailBodyTemplate;
	
	@FindBy(xpath = "//div[2]/button[2]/span[1][contains(text(),' SAVE ')]")  //
	WebElement saveGroupEmailConfig;
	
	@FindBy(xpath = "//button[contains(@id,'yesBtn')]")  //
	WebElement yes;
	
	@FindBy(xpath = "//mat-icon[@svgicon='trash']")  //
	public WebElement deleteBtn;
	
	@FindBy(xpath = "//mat-icon[@svgicon='edit']")  //
	public WebElement editBtn;
	
	@FindBy(xpath = "//td[contains(@class, 'cdk-column-batchname')]")
	List<WebElement> groupEmailList;
	
	@FindBy(xpath = "//mat-icon[@svgicon='healthcheck_show_details']")
	List<WebElement> detailsList;
	
	@FindBy(xpath = "//h1[contains(text(), 'Workflow History Detail ')]")
	WebElement workfloHistoryDetail;
	
	@FindBy(xpath = "//button[@id='crossClose']")  //
	WebElement crossClose;
	
	@FindBy(xpath = "//div[contains(text(),' Update Active/Inactive State ')]")
	WebElement updateStatus;
	
	@FindBy(xpath = "//span[contains(text(),'Saved Successfully')]")
	WebElement saveSuccess;
	
	@FindBy(xpath = "//span[contains(text(),'Updated Successfully')]")
	WebElement successEdit;
	
	@FindBy(xpath = "//span[contains(text(),'GroupEmailConfiguration with the given Batch name already exists')]")
	public WebElement groupEmailValidateEl;
	
	@FindBy(xpath = "//span[contains(text(),'Deleted Successfully')]")
	public WebElement deleteGroupEmail;
	
	@FindBy(xpath = "//mat-icon[@id='backButton']")
	public WebElement backBtn;
	
	@FindBy(xpath = "//mat-icon[@svgicon='close_dialog']")
	WebElement closeDialog;
	
	
}
