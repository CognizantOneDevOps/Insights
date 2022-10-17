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

	@FindBy(xpath = "//div//span[contains(text(),'Traceability Dashboard')]")
	WebElement landingPage;

	@FindBy(xpath = "//div//span[contains(text(),'Select Tool')]")
	WebElement selectTool;

	@FindBy(xpath = "//div//span[contains(text(),'Select Field')]")
	WebElement selectField;

	@FindBy(xpath = "//div//span[contains(text(),'Issue Type')]")
	WebElement issueType;
	
	@FindBy(xpath = "//input[@name='toolFieldValue']")
	WebElement toolFieldValue;
	
	@FindBy(xpath = "//button[contains(text(),'Search')]")
	WebElement searchButton;
	
	@FindBy(xpath = "//button[contains(text(),'Clear')]")
	WebElement clearButton;
	
	@FindBy(xpath = "//div[@role='tab']//div[contains(text(),'Summary')]")
	WebElement summaryTab;
	
	@FindBy(xpath = "//div[@role='tab']//div[contains(text(),'Pipeline')]")
	WebElement pipelineTab;
	
	@FindBy(xpath = "//label//u[contains(text(),' more info ')]")
	WebElement moreInfo;
	
	@FindBy(xpath = "//*[@id=\"mat-tab-content-0-1\"]//div//div//div[2]//div[1]//div[1]//label//u")
	WebElement viewJiraTool;
	
	@FindBy(xpath = "//u[contains(text(),'GIT')]")
	WebElement viewGitTool;
	
	@FindBy(xpath = "//div//h1[contains(text(),' Additional Details')]")
	WebElement additionalDetails;
	
	@FindBy(xpath = "//mat-icon[@svgicon='close_dialog']")
	WebElement closeDialog;
	
	@FindBy(xpath = "//*[@id=\"cdk-overlay-10\"]/snack-bar-container/div/div/app-snackbar-message/div/div[1]/span[contains(text(),'No data found')]")
	WebElement errorMessage;
	
	@FindBy(xpath = "//span[text()= 'OKAY']")
	WebElement okButton;
	
	@FindBy(xpath = "//span[contains(@class, 'mat-option-text')]")
	List<WebElement> dropdownList;
	
	@FindBy(xpath = "//div[contains(text(), 'This search has fetched : ')]")
	WebElement checkSummaryTab;
	
	@FindBy(xpath = "//div[contains(@class, 'parentCard')]")
	WebElement checkPipelineTab;
	
	@FindBy(xpath = "//mat-icon[@svgicon='cross']") 
	WebElement crossClose;
	
	@FindBy(xpath = "//tbody//tr//td//div[contains(text(), ' root ')]")
	WebElement checkUserRoot;
	
	@FindBy(xpath = "//input[@placeholder='Search']")
	WebElement searchUser;
	
	@FindBy(xpath = "//label[contains(@class, 'totalSummaryPropCount')]")
	List<WebElement> count;

	@FindBy(xpath = "//label[contains(text(),'Builds']")
	WebElement Builds;
	
	@FindBy(xpath = "//label[contains(text(),'Code Quality Executions']")
	List<WebElement> sonarCount;
	
	@FindBy(xpath = "//u[contains(text(),'GIT']")
	WebElement gitDetails;
	
	@FindBy(xpath = "//div//div//div[2]//div[2]//div//mat-card//label//u")
	WebElement jiraInfo;
	
	@FindBy(xpath = "//div[2]//table//tbody//tr//td[1]//div")
	WebElement searchResult;
	
	@FindBy(xpath = "//img[@src='icons/svg/traceability/JIRA.svg']")
	WebElement imgJira;
	
	@FindBy(xpath = "//img[@src='icons/svg/traceability/GIT.svg']")
	WebElement imgGit;
	
	@FindBy(xpath = "//span[contains(text(), 'Milestone already in progress, you cannot delete this Milestone. ')]")
	WebElement milestoneDeleteIssue;
	
}
