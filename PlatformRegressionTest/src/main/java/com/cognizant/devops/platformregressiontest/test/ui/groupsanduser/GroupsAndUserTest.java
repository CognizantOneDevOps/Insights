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

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

/**
 * @author NivethethaS
 * 
 *         Class contains the test cases for Groups And User module
 *
 */
public class GroupsAndUserTest extends LoginAndSelectModule {

	private static final Logger log = LogManager.getLogger(GroupsAndUserTest.class);

	GroupsAndUserConfiguration clickAllActionButton;

	String line = "============================================================================================================================================================";

	/**
	 * This method will be run before any test method belonging to the classes
	 * inside the <test> tag is run.
	 * @throws InterruptedException 
	 */
	@BeforeTest
	public void setUp() throws InterruptedException {
		initialization();
		getData(ConfigOptionsTest.GROUPS_AND_USERS_DIR + File.separator + ConfigOptionsTest.GROUP_JSON_FILE);
		selectModuleUnderConfiguration(LoginAndSelectModule.testData.get("groupsAndUser"));
		clickAllActionButton = new GroupsAndUserConfiguration();
	}

	/**
	 * This method will be executed just before any function/method with @Test
	 * annotation starts.
	 */
	@BeforeMethod
	public void beforeMethod() {
		log.info(line);
	}

	/**
	 * Assert true if landing page is displayed else false
	 */
	@Test(priority = 1)
	public void navigateToGroupsAndUserLandingPage() {
		Assert.assertTrue(clickAllActionButton.navigateToGroupsAnduserLandingPage(), "Landing page is not displayed");
	}

	/**
	 * Assert true if new access group is created else false
	 * @throws InterruptedException 
	 */
	@Test(priority = 2)
	public void addAccessGroup() throws InterruptedException {
		Assert.assertTrue(clickAllActionButton.addNewAccessGroup(), "Access Group already exists");
	}

	/**
	 * Assert true if user is created successfully else false
	 * @throws InterruptedException 
	 */
	@Test(priority = 3)
	public void addUser() throws InterruptedException {
		Assert.assertTrue(clickAllActionButton.addUser(), "user has not been created");
	}

	/**
	 * Assert true if error messages for each field is displayed else false
	 * @throws InterruptedException 
	 */
	@Test(priority = 4)
	public void addIncorrectUser() throws InterruptedException {
		Assert.assertTrue(clickAllActionButton.addIncorrectUser(), "some of the field values are correct");
	}

	/**
	 * Assert true if user is added to specified access group else false
	 */
	@Test(priority = 5)
	public void assignUserToAccessGroup() throws InterruptedException {
		Assert.assertTrue(clickAllActionButton.assignUser(), "user has not been created");

	}

	/**
	 * Assert true if user is added to multiple access groups else false
	 */
	@Test(priority = 6)
	public void assignUserToMultipleAccessGroup() throws InterruptedException {
		Assert.assertTrue(clickAllActionButton.assignUserToMultipleAccessGroup(),
				"cannot assign user to multiple access group");
	}

	/**
	 * Assert true if error message is displayed when adding the same user to same
	 * access group else false
	 */
	@Test(priority = 7)
	public void assignsameuserToSameAccessGroup() throws InterruptedException {
		Assert.assertTrue(clickAllActionButton.assignSameUser(), "Access Group has not been created");
	}

	/**
	 * Assert true if created user is displayed in UI else false
	 */
	@Test(priority = 8)
	public void checkIfUserIsPresent() {
		Assert.assertTrue(clickAllActionButton.checkUserIsPresent(), "user is not present in access group");
	}

	/**
	 * Assert true if editing the same user is successful else false
	 * @throws InterruptedException 
	 */
	@Test(priority = 9)
	public void editUser() throws InterruptedException {
		Assert.assertTrue(clickAllActionButton.editUser(), "user has not been edited");
	}

	/**
	 * Assert true if deleting the same user is successful else false
	 * @throws InterruptedException 
	 */
	@Test(priority = 10)
	public void deleteUser() throws InterruptedException {
		Assert.assertTrue(clickAllActionButton.deleteUser(), "user has not been deleted");
	}

	/**
	 * This method will be executed just after any function/method with @Test
	 * annotation ends.
	 */
	@AfterMethod
	public void afterMethod() {
		log.info(line);
	}

}
