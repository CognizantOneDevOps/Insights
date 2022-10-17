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
package com.cognizant.devops.platformregressiontest.test.ui.groupsanduser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

/**
 * @author NivethethaS
 * 
 *         Class contains the objects used for Groups And User module test
 *         cases
 *
 */
public class GroupsAndUserObjectRepository extends LoginAndSelectModule {

	Map<String, String> testData = new HashMap<>();

	@FindBy(xpath =  "//div[contains(text(),'Group & Users Management')]")
	WebElement landingPage;

	@FindBy(xpath = "//mat-select[@name ='selectedAdminOrg']")
	WebElement accessGroup;

	@FindBy(xpath = "//span[contains(@class, 'mat-option-text')]")
	List<WebElement> accessGroupList;

	@FindBy(xpath = "//span[contains(@class, 'mat-option-text')]")
	List<WebElement> accessGroupList1;

	@FindBy(xpath = "//mat-icon[@svgicon='add']")
	WebElement clickAddButton;
	
	@FindBy(xpath = "//mat-icon[@svgicon='retry']")
	WebElement refresh;

	@FindBy(xpath = "//mat-icon[@id='personAdd']")
	WebElement addUserButton;

	@FindBy(xpath = "//input[@placeholder='Please input an Access Group Name']")
	WebElement accessGroupName;

	@FindBy(xpath = "//button[contains(text(),'YES')]")
	WebElement yesButton;
	
	@FindBy(xpath = "//button[@id='yesBtn']")
	WebElement yes;
	
	@FindBy(xpath = "//span[text()= 'NO']")
	WebElement noButton;
	
	@FindBy(xpath = "//button[@id='crossClose']")
	WebElement crossClose;

	@FindBy(xpath = "//span[contains(text(),'Organization created')]")
	WebElement successMessage;
	
	@FindBy(xpath = "//span[contains(text(),'User added to organization')]")
	WebElement assignSuccessMessage;
	
	@FindBy(xpath = "//span[contains(text(),'User has been added.')]")
	WebElement userSuccessMessage;	

	@FindBy(xpath = "//span[contains(text(), 'User is already member of this organization')]")
	WebElement errorMessage;

	@FindBy(xpath = "//mat-radio-button[@id='AddUser']")
	WebElement addUserRadioButton;

	@FindBy(xpath = "//span[contains(text(), 'Assign users to various access groups')]")
	WebElement addUserDisplayPage;

	@FindBy(xpath = "//mat-radio-button[@id='Assignusers']")
	WebElement assignUserRadioButton;
	
	@FindBy(xpath = "//mat-icon[@svgicon='backButton']")
	WebElement backButton;

	@FindBy(xpath = "//div[@id='AssignUserBlock']")
	WebElement assignUserLandingPage;

	@FindBy(xpath = "//input[@placeholder=' Input a Name']")
	WebElement nameRequired;

	@FindBy(xpath = "//input[@placeholder=' Provide a valid E-mail Address']")
	WebElement emailAddress;

	@FindBy(xpath = "//input[@placeholder=' Input a Username']")
	WebElement userName;

	@FindBy(xpath = "//input[@placeholder=' Input a Password']")
	WebElement password;

	@FindBy(xpath = "//mat-select[@placeholder=' Select a Role']")
	WebElement selectRole;

	@FindBy(xpath = "//div[contains(text(),'Please enter a valid Name.')]")
	WebElement errName;

	@FindBy(xpath = "//div[contains(text(),'Please enter a valid Email.')]")
	WebElement erremailAddress;

	@FindBy(xpath = "//div[contains(text(),'Please enter a valid Username.')]")
	WebElement erruserName;

	@FindBy(xpath = "//div[contains(text(),'Please enter a valid Password.')]")
	WebElement errPassword;

	@FindBy(xpath = "//div[contains(text(),'Please select a Role.')]")
	WebElement errRole;

	@FindBy(xpath = "//span[contains(@class, 'mat-option-text')]")
	List<WebElement> selectRoleList;

	@FindBy(xpath = "//mat-icon[@svgicon='saveHook']")
	WebElement saveButton;

	@FindBy(xpath = "//input[@id='SearchId']")
	WebElement searchBox;

	@FindBy(xpath = "//span[contains(text(),'Select Access Group')]")
	WebElement accessGroup1;

	@FindBy(xpath = "//span[contains(text(),'Select Access Group')]")
	WebElement accessGroup2;

	@FindBy(xpath = "//span[contains(text(),'Select Role')]")
	WebElement selectRole1;

	@FindBy(xpath = "//span[contains(text(),'Select Role')]")
	WebElement selectRole2;

	@FindBy(xpath = "//td[contains(text(),'admin1@Cognizant.com')]")
	WebElement verifyUserSave;

	@FindBy(xpath = "//mat-icon[@title='Redirect To Landing Page']")
	WebElement redirectButton;

	@FindBy(xpath = "//mat-icon[@svgicon='edit']")
	WebElement editButton;

	@FindBy(xpath = "//mat-icon[@svgicon='trash']")
	WebElement deleteButton;

	@FindBy(xpath = "//table//tbody[@role='rowgroup']")
	WebElement userDetailsTable;

	@FindBy(xpath = "//label[contains(text(),'Current Access Group ')]")
	WebElement checkRedirectToLandingPage;

	@FindBy(xpath = "//input[@placeholder='Search by Login ID or Email Address']")
	WebElement searchBoxInLandingPage;

	@FindBy(xpath = "//div[@id='AddUserBlock']/span[@class='input-label']")
	List<WebElement> addUserFields;
	
	@FindBy(xpath = "//span[contains(text(),'Name')]")
	WebElement addUsername;
	
	@FindBy(xpath = "//span[contains(text(),'Email')]")
	WebElement addUserEmail;
	
	@FindBy(xpath = "//span[contains(text(),'Login ID')]")
	WebElement addUserLoginID;
	
}
