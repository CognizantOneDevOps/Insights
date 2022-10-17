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
package com.cognizant.devops.platformregressiontest.test.ui.dashboardreportdownload;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

public class DashboardReportDownloadObjectRepository extends LoginAndSelectModule {

	@FindBy(xpath = "//div[contains(text(),'Dashboard Report Configuration')]")   //
	WebElement landingPage;

	@FindBy(xpath = "//mat-icon[@svgicon='add']")  //
	WebElement clickAddButton;
	
	@FindBy(xpath = "//mat-icon[@svgicon='edit']")  //
	WebElement clickEditButton;

	@FindBy(xpath = "//span[@class='mainHeaderText']")  //
	WebElement mainHeader;

	@FindBy(xpath = "//mat-select[@id='organisation']")  //
	WebElement selectOrg;

	@FindBy(xpath = "//mat-select[@id='dashboard']")  //
	WebElement selectDashboard;

	@FindBy(xpath = "//input[@id='Enter Title']")  //
	WebElement title;

	@FindBy(xpath = "//mat-select[@placeholder='Select Dashboard Theme']")  //
	WebElement selectTheme;

	@FindBy(xpath = "//mat-select[@placeholder='Select PdfType']")  //
	WebElement selectPdfType;

	@FindBy(xpath = "//mat-select[@id='Select frequency']")  //
	WebElement selectFrequency;

	@FindBy(xpath = "//mat-select[@id='Select loadTime']")  //
	WebElement selectLoadTime;

	@FindBy(xpath = "//mat-select[@id='toolName']")
	WebElement toolName;

	@FindBy(xpath = "//mat-select[@id='AgentId']")
	WebElement agentId;

	@FindBy(xpath = "//mat-radio-button[@value='absolute']")  //As it is
	WebElement dataTimeRangeAbsolute;

	@FindBy(xpath = "//mat-radio-button[@value='relative']")  //As it is
	WebElement dataTimeRangeRelative;

	@FindBy(xpath = "//mat-radio-button[@value='other']")  //As it is
	WebElement dataTimeRangeOther;

	@FindBy(xpath = "//mat-icon[@id='Add Email']")  //
	WebElement addEmail;

	@FindBy(xpath = "//input[@name='senderEmailAddress']")  //
	WebElement senderEmailAddress;

	@FindBy(xpath = "//input[@name='receiverEmailAddress']")  //
	WebElement receiverEmailAddress;

	@FindBy(xpath = "//input[@name='receiverCCEmailAddress']")  //
	WebElement receiverCCEmailAddress;

	@FindBy(xpath = "//input[@name='receiverBCCEmailAddress']")  // 
	WebElement receiverBCCEmailAddress;

	@FindBy(xpath = "//input[@name='mailSubject']")  //As it is
	WebElement mailSubject;

	@FindBy(xpath = "//textarea[@name='mailBodyTemplate']")  //As it is
	WebElement mailBodyTemplate;

	@FindBy(xpath = "//div/button[2][contains(@id,'Save Email Config')]")  //
	WebElement saveMailDetails;

	@FindBy(xpath = "//mat-icon[@svgicon='save']")  //
	WebElement save;

	@FindBy(xpath = "//mat-select[@id='relative']")  //As it is
	WebElement selectRelativeTimeRange;

	@FindBy(xpath = "//mat-select[@id='other']")  //AS it is
	WebElement selectOtherTimeRange;

	@FindBy(xpath = "//input[contains(@class,'mat-start-date')]")  // As it is
	WebElement selectAbsoluteStartDate;

	@FindBy(xpath = "//input[contains(@class,'mat-end-date')]")  //As it is
	WebElement selectAbsoluteEndDate;

	@FindBy(xpath = "//span[@id='dashboardPreview']")  //
	WebElement dashboardPreview;

	@FindBy(xpath = "//button[contains(@id,'CONFIRM')]")  //
	WebElement confirm;

	@FindBy(xpath = "//div[contains(text(),'Save Dashboard Configuration'])")
	WebElement saveDashboardConfiguration;

	@FindBy(xpath = "//button[contains(@id,'yesBtn')]")  //
	WebElement yes;

	@FindBy(xpath = "//div[contains(text(),'Success')]")   //to be replaced
	WebElement success;
	
	@FindBy(xpath = "//button[@id='crossClose']")  //newly added
	WebElement crossClose;

	@FindBy(xpath = "//span[@id='ok']")  //
	WebElement OKAY;

	@FindBy(xpath = "//td[contains(@class, 'cdk-column-Title')]")
	List<WebElement> reportList;

	@FindBy(xpath = "//mat-icon[@id='Delete']")
	WebElement deleteReport;

	@FindBy(xpath = "//mat-icon[@svgicon='healthcheck_show_details']")
	List<WebElement> detailsList;

	@FindBy(xpath = "//h1[contains(text(),'Workflow History Detail')]")
	WebElement workfloHistoryDetail;

	@FindBy(xpath = "//mat-icon[@svgicon='close_dialog']")
	WebElement closeDialog;

	@FindBy(xpath = "//mat-icon[@svgicon='backButton']")
	WebElement redirectButton;

	@FindBy(xpath = "//mat-icon[@id='Reset']")
	WebElement resetButton;
}
