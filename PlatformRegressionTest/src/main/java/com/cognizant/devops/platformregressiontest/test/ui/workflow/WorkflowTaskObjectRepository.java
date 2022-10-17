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
package com.cognizant.devops.platformregressiontest.test.ui.workflow;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

public class WorkflowTaskObjectRepository extends LoginAndSelectModule {
	
	@FindBy(xpath = "//span[contains(text(),'Workflow Task Management')]")
	WebElement landingPage;

	@FindBy(xpath = "//mat-icon[@svgicon='add']")
	WebElement addWorkflow;
	
	@FindBy(xpath = "//mat-icon[@svgicon='edit']")
	WebElement editWorkflow;

	@FindBy(xpath = "//mat-icon[@svgicon='trash']")
	WebElement trashWorkflow;
	
	@FindBy(xpath = "//mat-icon[@svgicon='refresh']")
	WebElement refresh;

	@FindBy(xpath = "//mat-icon[@svgicon='backButton']")
	WebElement backButton;
	
	@FindBy(xpath = "//mat-icon[@svgicon='save']")
	WebElement save;
	
	@FindBy(xpath = "//mat-icon[@svgicon='exit']")
	WebElement exit;

	@FindBy(xpath = "//span[contains(text(),'Add  workflow task details')]")
	WebElement addWorkflowHeader;

	@FindBy(xpath = "//label[contains(text(),' Workflow Type')]")
	WebElement lblType;
	
	@FindBy(xpath = "//mat-select[@id='workflowType']")
	WebElement workflowType;

	@FindBy(xpath = "//label[contains(text(),'MQ Channel Name')]")
	WebElement lblQueueName;
	
	@FindBy(xpath = "//input[@id='mqChannel']")
	WebElement mqChannel;

	@FindBy(xpath = "//label[contains(text(),'Description')]")
	WebElement lblDesc;
	
	@FindBy(xpath = "//input[@id='description']")
	WebElement description;

	@FindBy(xpath = "//label[contains(text(),'Dependency')]")
	WebElement lblDependency;
	
	@FindBy(xpath = "//input[@id='dependency']")
	WebElement dependency;

	@FindBy(xpath = "//label[contains(text(),'Component Class Detail')]")
	WebElement lblDetail;
	
	@FindBy(xpath = "//textarea[@id='classDetail']")
	WebElement classDetail;
	
	@FindBy(xpath = "//mat-option[@role='option']")
	List<WebElement> typeList;
	
	@FindBy(xpath = "//span[contains(text(),'successfully')]")
	WebElement success;
	
	@FindBy(xpath = "//button[@id='crossClose']")
	WebElement crossClose;
	
	@FindBy(xpath = "//span[contains(text(),'Task already exist')]")
	WebElement existTask;
	
	@FindBy(xpath = "//button[@id='yesBtn']")
	WebElement yesBtn;
	
	@FindBy(xpath = "//td[contains(@class, 'mat-column-TaskName')]")
	List<WebElement> workflowList;
	
	@FindBy(xpath = "//span[contains(text(),'deleted successfully')]")
	WebElement workflowDelete;
	
	@FindBy(xpath = "//span[contains(text(),'Please fill mandatory fields')]")
	WebElement invalidMsg;

	

}
