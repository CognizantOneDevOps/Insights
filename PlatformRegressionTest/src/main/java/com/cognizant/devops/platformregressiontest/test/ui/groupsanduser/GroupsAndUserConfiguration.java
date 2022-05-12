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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.SkipException;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

/**
 * @author NivethethaS
 * 
 *         Class contains the business logic for Groups And User module test
 *         cases
 *
 */
public class GroupsAndUserConfiguration extends GroupsAndUserObjectRepository {

	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

	private static final Logger log = LogManager.getLogger(GroupsAndUserConfiguration.class);

	public GroupsAndUserConfiguration() {
		PageFactory.initElements(driver, this);
	}

	/**
	 * checks whether landing page is displayed or not
	 * 
	 * @return true if landing page is displayed o/w false
	 */
	public boolean navigateToGroupsAnduserLandingPage() {
		log.info("Landing page displayed : {}", landingPage.isDisplayed());
		return landingPage.isDisplayed();
	}

	/**
	 * Creates new Access Group and adds to the database
	 * 
	 * @return true if new Access Group is created o/w false
	 * @throws InterruptedException
	 */
	public boolean addNewAccessGroup() {
		boolean userAdded = true;
		for (int i = 0; i < 3; i++) {
			if (addAccessGroup(i)) {
				driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
				yesButton.click();
				wait.until(ExpectedConditions.elementToBeClickable(okButton));
				driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
				try {
					if (successMessage.isDisplayed()) {
						log.info("successfully added Access Group");
						userAdded = true;
						okButton.click();
					}
				} catch (Exception e) {
					log.info("something went wrong when adding access group");
					userAdded = false;
					break;
				} finally {
					log.info("finally block- Redirect to Groups & User");
					wait.until(ExpectedConditions.elementToBeClickable(redirectButton));
					redirectButton.click();
				}
			} else {
				log.debug("Access group already exists");
				throw new SkipException("Skipping test case as access group already exists");
			}
		}
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		return userAdded;
	}

	/**
	 * Sends keys to access group name according to the value passed
	 * 
	 * @param count
	 * @return true if sending keys successful o/w false
	 */
	@SuppressWarnings("all")
	public boolean addAccessGroup(int count) {
		if (count == 0) {
			if (!verifyAccessGroupName(LoginAndSelectModule.testData.get("accessGroup1"))) {
				wait.until(ExpectedConditions.elementToBeClickable(clickAddButton));
				clickAddButton.click();
				accessGroupName.sendKeys(LoginAndSelectModule.testData.get("accessGroup1"));
				return true;
			} else {
				return false;
			}
		}
		if (count == 1) {
			if (!verifyAccessGroupName(LoginAndSelectModule.testData.get("accessGroup2"))) {
				wait.until(ExpectedConditions.elementToBeClickable(clickAddButton));
				clickAddButton.click();
				accessGroupName.sendKeys(LoginAndSelectModule.testData.get("accessGroup2"));
				return true;
			} else {
				return false;
			}
		}
		if (count == 2) {
			if (!verifyAccessGroupName(LoginAndSelectModule.testData.get("accessGroup3"))) {
				wait.until(ExpectedConditions.elementToBeClickable(clickAddButton));
				clickAddButton.click();
				accessGroupName.sendKeys(LoginAndSelectModule.testData.get("accessGroup3"));
				return true;
			} else {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks whether Groups & users landing page is displayed
	 * 
	 * @return true if Groups & users landing page is displayed o/w false
	 */
	public boolean redirectToGroupsAnduserLandingPage() {
		log.info("redirect to Group & user landing page successful");
		redirectButton.click();
		return checkRedirectToLandingPage.isDisplayed();
	}

	/**
	 * Checks whether the access group name is present in the UI or not
	 * 
	 * @param accessName
	 * @return true if access group name present in list of access group in database
	 *         list else false
	 */
	public boolean verifyAccessGroupName(String accessName) {
		if (accessName != null) {
			accessGroup.click();
			for (WebElement access : accessGroupList) {
				if (access.getText().equals(accessName)) {
					log.info("{} Access Group is clicked successfully.", accessName);
					wait.until(ExpectedConditions.elementToBeClickable(access));
					access.click();
					return true;
				}
			}
			accessGroup.sendKeys(Keys.ESCAPE);
		}
		return false;
	}

	/**
	 * Checks whether the user name is present in the UI or not
	 * 
	 * @param userName
	 * @return true if user name present in list of access group in database list
	 *         else false
	 */
	public boolean verifyUserPresent(String userName) {
		List<WebElement> rws = userDetailsTable.findElements(By.tagName("tr"));
		for (int i = 0; i < rws.size(); i++) {
			List<WebElement> cols = (rws.get(i)).findElements(By.tagName("td"));
			if ((cols.get(1).getText()).equals(userName)) {
				log.info("{} user name is present.", userName);
				return true;
			}
		}
		log.info("{} user name is not present.", userName);
		return false;
	}

	/**
	 * checks whether Add user form,Assign user form and Add User form fields is
	 * displayed correctly
	 * 
	 * @return true if Add user form,Assign user form and Add User form fields is
	 *         displayed o/w false
	 * @throws InterruptedException
	 */
	public boolean verifyUI() {
		wait.until(ExpectedConditions.elementToBeClickable(addUserButton));
		addUserButton.click();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		try {
			if (addUserDisplayPage.isDisplayed() && assignUserLandingPage.isDisplayed() && verifyAddUserForm()) {
				log.info("Add user form,Assign user form and Add User form fields is displayed correctly");
				return true;
			}
		} catch (NoSuchElementException e) {
			log.info("Add user form or Assign user form or Add User form fields is not loaded");
			return false;
		}
		return false;
	}

	/**
	 * Checks whether Add User form fields are displayed correctly
	 * 
	 * @return true if Add User form fields are displayed correctly o/w false
	 */
	private boolean verifyAddUserForm() {
		List<String> fieldsList = new ArrayList<>();
		addUserFields.stream().map(WebElement::getText).forEach(fieldsList::add);
		List<String> listA = new ArrayList<>();
		listA.add("Name");
		listA.add("Email");
		listA.add("Login ID");
		listA.add("Password");
		listA.add("Role");
		if (fieldsList.containsAll(listA)) {
			log.info("Add User form fields are loaded successfully");
			return true;
		} else {
			log.info("error while loading Add User form fields");
			return false;
		}
	}

	/**
	 * Verifies UI & Creates new user with all the mandatory details and saves it in
	 * the DB
	 * 
	 * @return true if correct UI is displayed & user is created successfully o/w
	 *         false
	 * @throws InterruptedException
	 */
	@SuppressWarnings("all")
	public boolean addUser() throws InterruptedException {
		boolean userAdded = false;
		redirectButton.click();
		if (verifyAccessGroupName("Main Org.") && !verifyUserPresent(LoginAndSelectModule.testData.get("userName"))) {
			if (!verifyUI()) {
				log.info("Elements of add user are not loaded properly");
				return false;
			}
			wait.until(ExpectedConditions.elementToBeClickable(addUserRadioButton));
			addUserRadioButton.click();
			nameRequired.sendKeys(LoginAndSelectModule.testData.get("name"));
			emailAddress.sendKeys(LoginAndSelectModule.testData.get("email"));
			userName.sendKeys(LoginAndSelectModule.testData.get("userName"));
			password.sendKeys(LoginAndSelectModule.testData.get("password"));
			selectRole.click();
			Thread.sleep(1000);
			selectRole(LoginAndSelectModule.testData.get("roleAdmin"));
			wait.until(ExpectedConditions.elementToBeClickable(saveButton));
			driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			saveButton.click();
			wait.until(ExpectedConditions.elementToBeClickable(okButton));
			driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
			try {
				if (successMessage.isDisplayed()) {
					log.info("successfully added user");
					userAdded = true;
					wait.until(ExpectedConditions.elementToBeClickable(okButton));
					okButton.click();
				}
			} catch (Exception e) {
				log.info("Error while adding user");
				userAdded = false;
				wait.until(ExpectedConditions.elementToBeClickable(okButton));
				okButton.click();
			}
		} else {
			redirectButton.click();
			log.debug("user already exists in Main Org.");
			throw new SkipException("Skipping test case as already exists in Main Org.");
		}
		return userAdded;
	}

	/**
	 * checks whether error message for each field in Add User form is displayed
	 * correctly
	 * 
	 * @return true if all the error message is displayed correctly o/w false
	 * @throws InterruptedException
	 */
	public boolean addIncorrectUser() {
		boolean userAdded = false;
		redirectButton.click();
		wait.until(ExpectedConditions.elementToBeClickable(addUserButton));
		addUserButton.click();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		addUserRadioButton.click();
		nameRequired.sendKeys(LoginAndSelectModule.testData.get("err_name"));
		emailAddress.sendKeys(LoginAndSelectModule.testData.get("err_email"));
		userName.sendKeys(LoginAndSelectModule.testData.get("err_userName"));
		password.sendKeys(LoginAndSelectModule.testData.get("err_password"));
		wait.until(ExpectedConditions.elementToBeClickable(saveButton));
		saveButton.click();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		try {
			if (errName.isDisplayed() && erremailAddress.isDisplayed() && erruserName.isDisplayed()
					&& errPassword.isDisplayed() && errRole.isDisplayed()) {
				log.info("Error message for each field is displayed correctly");
				userAdded = true;
			}
		} catch (NoSuchElementException e) {
			log.info("1 or more field validation has failed");
			userAdded = false;
		}
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		return userAdded;
	}

	/**
	 * Checks whether the search box to search user is enabled after clicking on Add
	 * user radio button and Assigns specified user to specified access group &
	 * role.
	 * 
	 * @return true if search box is enabled and user is added to specified access
	 *         group o/w false
	 * @throws InterruptedException
	 */
	public boolean assignUser() throws InterruptedException {
		boolean userAssigned = false;
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		redirectButton.click();
		if (verifyAccessGroupName(LoginAndSelectModule.testData.get("accessGroup3"))
				&& !verifyUserPresent(LoginAndSelectModule.testData.get("userName"))) {
			wait.until(ExpectedConditions.elementToBeClickable(addUserButton));
			addUserButton.click();
			wait.until(ExpectedConditions.elementToBeClickable(assignUserRadioButton));
			assignUserRadioButton.click();
			if (!isSearchBoxEnabled()) {
				log.info("Search box is not enabled after clicking on add user radio button");
				return false;
			}
			searchBox.sendKeys(LoginAndSelectModule.testData.get("userName"));
			wait.until(ExpectedConditions.elementToBeClickable(accessGroup1));
			accessGroup1.click();
			selectAccessGroup(LoginAndSelectModule.testData.get("accessGroup3"));
			selectRole1.click();
			Thread.sleep(1000);
			selectRole(LoginAndSelectModule.testData.get("roleUpdate"));
			wait.until(ExpectedConditions.elementToBeClickable(saveButton));
			saveButton.click();
			wait.until(ExpectedConditions.elementToBeClickable(okButton));
			driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
			try {
				if (successMessage.isDisplayed()) {
					log.info("User is added successfully to access group 1");
					userAssigned = true;
					okButton.click();
				}
			} catch (Exception e) {
				log.info("error while adding user to access group 1");
				userAssigned = false;
				okButton.click();
			}
		} else {
			log.debug("user already exists in the access group");
			throw new SkipException("Skipping test case as user already exists in the access group");
		}
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		return userAssigned;
	}
	
	/**
	 * selects access group name from the list of access group names available
	 * 
	 * @param accessName
	 */
	public void selectAccessGroup(String accessName) {
		for (WebElement group : accessGroupList) {
			if ((group.getText()).equals(accessName)) {
				log.info("{} access group clicked", accessName);
				group.click();
				break;
			}
		}
	}
	
	/**
	 * selects role name from the list of role names available
	 * 
	 * @param roleName
	 */
	public void selectRole(String roleName) {
		for (WebElement role : selectRoleList) {
			log.info("{} role is seen successfully.", role.getText());
			if ((role.getText()).equals(roleName)) {
				log.info("{} role clicked", roleName);
				wait.until(ExpectedConditions.elementToBeClickable(role));
				role.click();
				break;
			}
		}
	}

	/**
	 * Adds one specific user to multiple access groups
	 * 
	 * @return true if user is added successfully o/w false
	 * @throws InterruptedException
	 */
	public boolean assignUserToMultipleAccessGroup() throws InterruptedException {
		boolean userAssign = true;
		redirectButton.click();
		if (verifyAccessGroupName(LoginAndSelectModule.testData.get("accessGroup2"))
				&& !verifyUserPresent(LoginAndSelectModule.testData.get("userName"))) {
			wait.until(ExpectedConditions.elementToBeClickable(addUserButton));
			addUserButton.click();
			wait.until(ExpectedConditions.elementToBeClickable(assignUserRadioButton));
			assignUserRadioButton.click();
			searchBox.sendKeys(LoginAndSelectModule.testData.get("userName"));
			wait.until(ExpectedConditions.elementToBeClickable(accessGroup1));
			accessGroup1.click();
			selectAccessGroup(LoginAndSelectModule.testData.get("accessGroup2"));
			selectRole1.click();
			Thread.sleep(2000);
			selectRole(LoginAndSelectModule.testData.get("roleUpdate"));
			wait.until(ExpectedConditions.elementToBeClickable(accessGroup2));
			accessGroup2.click();
			Thread.sleep(2000);
			selectAccessGroup(LoginAndSelectModule.testData.get("accessGroup1"));
			selectRole2.click();
			Thread.sleep(2000);
			selectRole(LoginAndSelectModule.testData.get("role1"));
			wait.until(ExpectedConditions.elementToBeClickable(saveButton));
			saveButton.click();
			wait.until(ExpectedConditions.elementToBeClickable(okButton));
			driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
			try {
				if (successMessage.isDisplayed()) {
					log.info("User is added to multiple access groups");
					userAssign = true;
					okButton.click();
				}
			} catch (Exception e) {
				log.info("error while adding user to access group");
				userAssign = false;
				okButton.click();
			}
		} else {
			log.debug("user already exists in the access group");
			throw new SkipException("Skipping test case as user already exists in the access group");
		}
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		return userAssign;
	}

	/**
	 * Checks whether error message is popped out if we add existing user to
	 * existing access group
	 * 
	 * @return true if error message is displayed o/w false
	 * @throws InterruptedException
	 */
	public boolean assignSameUser() throws InterruptedException {
		boolean userAssigned = true;
		redirectButton.click();
		wait.until(ExpectedConditions.elementToBeClickable(saveButton));
		addUserButton.click();
		wait.until(ExpectedConditions.elementToBeClickable(saveButton));
		assignUserRadioButton.click();
		if (!isSearchBoxEnabled()) {
			return false;
		}
		searchBox.sendKeys(LoginAndSelectModule.testData.get("userName"));
		wait.until(ExpectedConditions.elementToBeClickable(saveButton));
		accessGroup1.click();
		for (WebElement group : accessGroupList) {
			if ((group.getText()).equals(LoginAndSelectModule.testData.get("accessGroup2"))) {
				wait.until(ExpectedConditions.elementToBeClickable(saveButton));
				group.click();
				driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
				break;
			}
		}
		selectRole1.click();
		Thread.sleep(2000);
		for (WebElement role : selectRoleList) {
			if ((role.getText()).equals(LoginAndSelectModule.testData.get("roleUpdate"))) {
				role.click();
				break;
			}
		}
		wait.until(ExpectedConditions.elementToBeClickable(saveButton));
		saveButton.click();
		wait.until(ExpectedConditions.elementToBeClickable(okButton));
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		try {
			if (errorMessage.isDisplayed()) {
				log.info("error message is displayed when we try to add existing user to the access group");
				userAssigned = true;
				wait.until(ExpectedConditions.elementToBeClickable(okButton));
				okButton.click();
			}
		} catch (Exception e) {
			log.info("error message is not displayed");
			userAssigned = false;
			wait.until(ExpectedConditions.elementToBeClickable(okButton));
			okButton.click();
		}
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		redirectButton.click();
		return userAssigned;
	}

	/**
	 * Checks whether the search box is enabled
	 * 
	 * @return true if search box is enabled o/w false
	 */
	public boolean isSearchBoxEnabled() {
		if (searchBox.isEnabled()) {
			log.info("Search box is enabled");
			return true;
		}
		log.info("Search box is not enabled");
		return false;
	}

	/**
	 * checks if created user is displayed in the UI by searching the specific user
	 * in the DB
	 * 
	 * @return true if created user is displayed in the UI o/w false
	 * @throws InterruptedException
	 */
	public boolean checkUserIsPresent() {
		redirectButton.click();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		if (searchLoginID(LoginAndSelectModule.testData.get("userName"))) {
			log.info("Created user is present in the users list");
			searchBoxInLandingPage.clear();
			return true;
		}
		log.info("Created user is not present in the users list");
		return false;
	}

	/**
	 * Edits the user & Checks whether editing the same user is successful
	 * 
	 * @return true if editing is successful o/w false
	 * @throws InterruptedException
	 */
	public boolean editUser() {
		boolean editDone = false;
		redirectButton.click();
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		verifyAccessGroupName(LoginAndSelectModule.testData.get("accessGroup2"));
		if (searchLoginID(LoginAndSelectModule.testData.get("userName"))) {
			WebElement rws = userDetailsTable.findElement(By.tagName("tr"));
			List<WebElement> cols = rws.findElements(By.tagName("td"));
			if ((cols.get(1).getText()).equals(LoginAndSelectModule.testData.get("name"))) {
				cols.get(0).findElement(By.xpath("//span[@class='mat-radio-container']")).click();
				wait.until(ExpectedConditions.elementToBeClickable(editButton));
				editButton.click();
				driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
				userDetailsTable.findElement(By.xpath("//mat-select[@placeholder='Select number of records ']"))
						.click();
				driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
				for (WebElement role : selectRoleList) {
					if ((role.getText()).equals(LoginAndSelectModule.testData.get("roleChange"))) {
						role.click();
						break;
					}
				}
			}
		} else {
			log.debug("user name is not present in the database");
			throw new SkipException("Skipping test case as user does not exist");
		}
		wait.until(ExpectedConditions.elementToBeClickable(saveButton));
		saveButton.click();
		wait.until(ExpectedConditions.elementToBeClickable(okButton));
		okButton.click();
		if ((userDetailsTable.findElement(By.xpath("//mat-select[@placeholder='Select number of records ']")).getText())
				.equals(LoginAndSelectModule.testData.get("roleChange"))) {
			log.info("Editing done successfully");
			editDone = true;
		} else {
			log.info("Editing is not successfully");
			editDone = false;
		}
		searchBoxInLandingPage.sendKeys(Keys.CONTROL + "a");
		searchBoxInLandingPage.sendKeys(Keys.DELETE);
		redirectButton.click();
		return editDone;
	}

	/**
	 * Deletes the user from DB & checks whether deletion is successful
	 * 
	 * @return true if deletion is successful o/w false
	 * @throws InterruptedException
	 */
	public boolean deleteUser() {
		boolean deleteDone = true;
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		verifyAccessGroupName(LoginAndSelectModule.testData.get("accessGroup3"));
		if (searchLoginID(LoginAndSelectModule.testData.get("userName"))) {
			WebElement rws = userDetailsTable.findElement(By.tagName("tr"));
			List<WebElement> cols = rws.findElements(By.tagName("td"));
			if ((cols.get(1).getText()).equals(LoginAndSelectModule.testData.get("name"))) {
				cols.get(0).findElement(By.xpath("//span[@class='mat-radio-container']")).click();
				wait.until(ExpectedConditions.elementToBeClickable(deleteButton));
				deleteButton.click();
				driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
			}
			wait.until(ExpectedConditions.elementToBeClickable(yesButton));
			yesButton.click();
			wait.until(ExpectedConditions.elementToBeClickable(okButton));
			okButton.click();
			driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		} else {
			log.debug("user does not exist to be deleted");
			throw new SkipException("Skipping test case as user does not exist");
		}
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		if (!(driver.findElement(By.xpath("//tr//td[2]")).getText())
				.contains(LoginAndSelectModule.testData.get("name"))) {
			log.info("User is deleted");
			deleteDone = true;
		} else {
			log.info("Error while deleting user");
			deleteDone = false;
		}
		return deleteDone;
	}

	/**
	 * Searches the user name in User details table
	 * 
	 * @param userName
	 * @return true if user is displayed o/w false
	 * @throws InterruptedException
	 */
	public boolean searchLoginID(String userName) {
		searchBoxInLandingPage.clear();
		searchBoxInLandingPage.sendKeys(userName);
		try {
			if (userDetailsTable.isDisplayed()) {
				log.info("{} is present in the User Table.", userName);
				return true;
			}
		} catch (Exception e) {
			log.info("{} is not present in the User Table.", userName);
			return false;
		}
		return false;
	}

}