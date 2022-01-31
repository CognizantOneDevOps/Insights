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

	@FindBy(xpath = "//div[contains(text(),'Dashboard Report Configuration')]")
	WebElement landingPage;

	@FindBy(xpath = "//mat-icon[@title='Configure a dashboard']")
	WebElement clickAddButton;

	@FindBy(xpath = "//span[@class='mainHeaderText']")
	WebElement mainHeader;

	@FindBy(xpath = "//mat-select[@placeholder='Select Organisation']")
	WebElement selectOrg;

	@FindBy(xpath = "//mat-select[@placeholder='Select Dashboard']")
	WebElement selectDashboard;

	@FindBy(xpath = "//input[@placeholder='Enter Title']")
	WebElement title;

	@FindBy(xpath = "//mat-select[@placeholder='Select Dashboard Theme']")
	WebElement selectTheme;

	@FindBy(xpath = "//mat-select[@placeholder='Select PdfType']")
	WebElement selectPdfType;

	@FindBy(xpath = "//mat-select[@name='schedule']")
	WebElement selectFrequency;

	@FindBy(xpath = "//mat-select[@placeholder='Select loadTime']")
	WebElement selectLoadTime;

	@FindBy(xpath = "//mat-select[@id='toolName']")
	WebElement toolName;

	@FindBy(xpath = "//mat-select[@id='AgentId']")
	WebElement agentId;

	@FindBy(xpath = "//mat-radio-button[@value='absolute']")
	WebElement dataTimeRangeAbsolute;

	@FindBy(xpath = "//mat-radio-button[@value='relative']")
	WebElement dataTimeRangeRelative;

	@FindBy(xpath = "//mat-radio-button[@value='other']")
	WebElement dataTimeRangeOther;

	@FindBy(xpath = "//mat-icon[@title='Add Email']")
	WebElement addEmail;

	@FindBy(xpath = "//input[@name='senderEmailAddress']")
	WebElement senderEmailAddress;

	@FindBy(xpath = "//textarea[@name='receiverEmailAddress']")
	WebElement receiverEmailAddress;

	@FindBy(xpath = "//textarea[@name='receiverCCEmailAddress']")
	WebElement receiverCCEmailAddress;

	@FindBy(xpath = "//textarea[@name='receiverBCCEmailAddress']")
	WebElement receiverBCCEmailAddress;

	@FindBy(xpath = "//input[@name='mailSubject']")
	WebElement mailSubject;

	@FindBy(xpath = "//textarea[@name='mailBodyTemplate']")
	WebElement mailBodyTemplate;

	@FindBy(xpath = "//div/button[2][contains(@class,'configureBut')]")
	WebElement saveMailDetails;

	@FindBy(xpath = "//mat-icon[@title='Preview And Save ']")
	WebElement save;

	@FindBy(xpath = "//mat-select[@id='relative']")
	WebElement selectRelativeTimeRange;

	@FindBy(xpath = "//mat-select[@id='other']")
	WebElement selectOtherTimeRange;

	@FindBy(xpath = "//input[contains(@class,'mat-start-date')]")
	WebElement selectAbsoluteStartDate;

	@FindBy(xpath = "//input[contains(@class,'mat-end-date')]")
	WebElement selectAbsoluteEndDate;

	@FindBy(xpath = "//span[@class='showDialogHeadingCss']")
	WebElement dashboardPreview;

	@FindBy(xpath = "//span[contains(text(),'CONFIRM')]")
	WebElement confirm;

	@FindBy(xpath = "//div[contains(text(),'Save Dashboard Configuration'])")
	WebElement saveDashboardConfiguration;

	@FindBy(xpath = "//span[contains(text(),'YES')]")
	WebElement yes;

	@FindBy(xpath = "//div[contains(text(),'Success')]")
	WebElement success;

	@FindBy(xpath = "//span[contains(text(),'OKAY')]")
	WebElement OKAY;

	@FindBy(xpath = "//td[contains(@class, 'cdk-column-Title')]")
	List<WebElement> reportList;

	@FindBy(xpath = "//mat-icon[@title='Delete']")
	WebElement deleteReport;

	@FindBy(xpath = "//mat-icon[@svgicon='healthcheck_show_details']")
	List<WebElement> detailsList;

	@FindBy(xpath = "//li[contains(text(),'Workflow History Detail')]")
	WebElement workfloHistoryDetail;

	@FindBy(xpath = "//mat-icon[@svgicon='close_dialog']")
	WebElement closeDialog;

	@FindBy(xpath = "//mat-icon[@title='Redirect To Landing Page']")
	WebElement redirectButton;

	@FindBy(xpath = "//mat-icon[@title='Refresh']")
	WebElement resetButton;
}
