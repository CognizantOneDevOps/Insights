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

	@FindBy(xpath = "//div[contains(text(),'Add Access Group / Edit User Role / Add User / Assign User to Various Access Group ')]")
	WebElement landingPage;

	@FindBy(xpath = "//mat-select[@name ='selectedAdminOrg']")
	WebElement accessGroup;

	@FindBy(xpath = "//span[contains(@class, 'mat-option-text')]")
	List<WebElement> accessGroupList;

	@FindBy(xpath = "//mat-icon[@title='Add New Access Group']")
	WebElement clickAddButton;

	@FindBy(xpath = "//mat-icon[@title='Add User to Current/Selected Access Group']")
	WebElement addUserButton;

	@FindBy(xpath = "//input[@placeholder='Please input an Access Group Name']")
	WebElement accessGroupName;

	@FindBy(xpath = "//span[text()= 'YES']")
	WebElement yesButton;

	@FindBy(xpath = "//span[text()= 'NO']")
	WebElement noButton;

	@FindBy(xpath = "//span[text()= 'OKAY']")
	WebElement okButton;

	@FindBy(xpath = "//div[contains(text(), ' Success')]")
	WebElement successMessage;

	@FindBy(xpath = "//div[contains(text(), ' Error')]")
	WebElement errorMessage;

	@FindBy(xpath = "//div[@class='adduserCol']//span[@class='mat-radio-container']")
	WebElement addUserRadioButton;

	@FindBy(xpath = "//div[contains(text(),'Add User (Please note: You are adding the user to Main Org.')]")
	WebElement addUserDisplayPage;

	@FindBy(xpath = "//div[@class='assignUserCol']//span[@class='mat-radio-container']")
	WebElement assignUserRadioButton;

	@FindBy(xpath = "//div[contains(text(),'Assign users to various access groups ')]")
	WebElement assignUserLandingPage;

	@FindBy(xpath = "//td/input[@placeholder=' Input a Name']")
	WebElement nameRequired;

	@FindBy(xpath = "//td/input[@placeholder=' Provide a valid E-mail Address']")
	WebElement emailAddress;

	@FindBy(xpath = "//td/input[@placeholder=' Input a Username']")
	WebElement userName;

	@FindBy(xpath = "//td/input[@placeholder=' Input a Password']")
	WebElement password;

	@FindBy(xpath = "//mat-select[@placeholder=' Select a Role']")
	WebElement selectRole;

	@FindBy(xpath = "//td[contains(text(),'Please enter a valid Name.')]")
	WebElement errName;

	@FindBy(xpath = "//td[contains(text(),'Please enter a valid Email.')]")
	WebElement erremailAddress;

	@FindBy(xpath = "//td[contains(text(),'Please enter a valid Username.')]")
	WebElement erruserName;

	@FindBy(xpath = "//td[contains(text(),'Please enter a valid Password.')]")
	WebElement errPassword;

	@FindBy(xpath = "//td[contains(text(),'Please select a Role.')]")
	WebElement errRole;

	@FindBy(xpath = "//span[contains(@class, 'mat-option-text')]")
	List<WebElement> selectRoleList;

	@FindBy(xpath = "//mat-icon[@title='Save ']")
	WebElement saveButton;

	@FindBy(xpath = "//table[@class='tableMainCss']//input[@placeholder=' Search by Login ID']")
	WebElement searchBox;

	@FindBy(xpath = "//table[@class='tableMainCss']//tr[1]//span[contains(text(),'Select Access Group')]")
	WebElement accessGroup1;

	@FindBy(xpath = "//table[@class='tableMainCss']//tr[2]//span[contains(text(),'Select Access Group')]")
	WebElement accessGroup2;

	@FindBy(xpath = "//table[@class='tableMainCss']//tr[1]//span[contains(text(),'Select Role')]")
	WebElement selectRole1;

	@FindBy(xpath = "//table[@class='tableMainCss']//tr[2]//span[contains(text(),'Select Role')]")
	WebElement selectRole2;

	@FindBy(xpath = "//td[contains(text(),'admin1@Cognizant.com')]")
	WebElement verifyUserSave;

	@FindBy(xpath = "//mat-icon[@title='Redirect To Landing Page']")
	WebElement redirectButton;

	@FindBy(xpath = "//mat-icon[@title='Edit ']")
	WebElement editButton;

	@FindBy(xpath = "//mat-icon[@title='Delete Organization User ']")
	WebElement deleteButton;

	@FindBy(xpath = "//table//tbody[@role='rowgroup']")
	WebElement userDetailsTable;

	@FindBy(xpath = "//label[contains(text(),'Current Access Group ')]")
	WebElement checkRedirectToLandingPage;

	@FindBy(xpath = "//input[@placeholder='Search by Login ID or Email Address']")
	WebElement searchBoxInLandingPage;

	@FindBy(xpath = "//div[@class='adduserCol']//td[1]")
	List<WebElement> addUserFields;
	
}
