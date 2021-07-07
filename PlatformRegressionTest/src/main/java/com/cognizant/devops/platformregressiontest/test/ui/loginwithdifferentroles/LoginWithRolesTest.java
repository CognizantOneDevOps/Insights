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
package com.cognizant.devops.platformregressiontest.test.ui.loginwithdifferentroles;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

/**
 * @author NivethethaS
 * 
 *         Class contains the test cases for login with different roles test
 *         cases
 *
 */
public class LoginWithRolesTest extends LoginAndSelectModule {

	private static final Logger log = LogManager.getLogger(LoginWithRolesTest.class);

	LoginWithRolesConfiguration clickAllActionButton;

	String line = "============================================================================================================================================================";

	/**
	 * This method will be run before any test method belonging to the classes
	 * inside the <test> tag is run.
	 * 
	 * @throws InterruptedException
	 */
	@BeforeTest
	public void setUp() {
		initialization();
		getData(ConfigOptionsTest.GROUPS_AND_USERS_DIR + File.separator + ConfigOptionsTest.GROUP_JSON_FILE);
		clickAllActionButton = new LoginWithRolesConfiguration();
	}

	/**
	 * Assert true if Login successful else false
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 1)
	public void loginwithRoles() {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.loginWithCreatedUserID(), "Login with created user successful");
	}

	/**
	 * Assert true if admin role functionality is successful else false
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 2)
	public void checkAdminFunctionality() throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.checkAdminrole(), "Role- Admin Functionality successful");
	}

	/**
	 * Assert true if editor role functionality is successful else false
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 3)
	public void checkEditorFunctionality() throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.checkEditorRole(), "Role- Editor Functionality successful");
	}

	/**
	 * Assert true if Viewers role functionality is successful else false
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 4)
	public void checkViewerFunctionality() throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.checkViewerRole(), "Role- Viewer Functionality successful");
	}

	/**
	 * logs out as created user and logs in using default credentials
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 5)
	public void loginAsDefaultUser() {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.loginasdefaultuser(), "login with default user credentials successful");
	}
	
	/**
	 * deletes created user from all the access group
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 6)
	public void deleteCreatedUser() throws InterruptedException {
		log.info(line);
		selectModuleOnClickingConfig("Group & Users");
		Assert.assertTrue(clickAllActionButton.deleteUserFromAllAccessGroup(), "login with default user credentials successful");
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
