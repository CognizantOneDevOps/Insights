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
package com.cognizant.devops.platformregressiontest.test.ui.scheduletask;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

public class ScheduleTaskObjectRepository extends LoginAndSelectModule {
	
	@FindBy(xpath = "//div[contains(text(),' Schedule Task Management ')]")
	WebElement landingPage;
	
	@FindBy(xpath = "//mat-icon[@svgicon='add']")
	WebElement clickAddButton;
	
	@FindBy(xpath = "//input[@placeholder='Enter Task name']") 
	WebElement taskName;
	
	@FindBy(xpath = "//textarea[@formcontrolname='componentClassDetail']") 
	WebElement description;
	
	@FindBy(xpath = "//input[@formcontrolname='schedule']") 
	WebElement schedule;
	
	@FindBy(xpath = "//button[@id = 'yesBtn']")  
	WebElement yesButton;
	
	@FindBy(xpath = "//span[contains(text(),'OK')]")
	WebElement okButton;
	
	@FindBy(xpath = "//mat-icon[@svgicon='save']")
	WebElement save;
	
	@FindBy(xpath = "//mat-icon[@svgicon='cross']")
	WebElement crossClose;
	
	@FindBy(xpath = "//mat-icon[@svgicon='trash']")
	WebElement clickDelete;
	
	@FindBy(xpath = "//mat-icon[@svgicon='backButton']")
	WebElement backButton;
	
	@FindBy(xpath = "//mat-icon[@svgicon='edit']")
	WebElement clickEditButton;
	
	@FindBy(xpath = "//mat-icon[@svgicon='stop']")
	WebElement clickStop;
	
	@FindBy(xpath = "//mat-icon[@svgicon='play']")
	WebElement clickStart;
	
	@FindBy(xpath = "//span[contains(text(),'successfully')]")
	WebElement successMessage;
	
	@FindBy(xpath = "//span[contains(text(),'Task creation/Edit has issue, Please check service log for more detail.')]")
	WebElement invalideMessage;
	
	@FindBy(xpath = "//span[contains(text(),'Task deleted successfull')]")
	WebElement deletedMessage;
	
	@FindBy(xpath = "//h1[contains(text(),'Task History Detail ')]")
	WebElement historyDialogBox;
	
	@FindBy(xpath = "//b[contains(text(),'STOP')]")
	WebElement stopMessage;
	
	@FindBy(xpath = "//b[contains(text(),'START')]")
	WebElement startMessage;
	
	@FindBy(xpath = "//span[contains(text(),'')]")
	WebElement errorMessage;
	
	@FindBy(xpath = "//mat-icon[@svgicon='next-page']")
	WebElement nextPage;
	
	@FindBy(xpath = "//mat-icon[@svgicon='close_dialog']")
	WebElement dialogClose;
	
	@FindBy(xpath = "//td[contains(@class, 'componentName')]")
	List<WebElement> taskList;
	

}
