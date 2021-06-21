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
package com.cognizant.devops.platformregressiontest.test.login;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

public class LoginTest extends LoginAndSelectModule {

	private static final Logger log = LogManager.getLogger(LoginTest.class);
	LoginConfiguration clickAllActionButton;

	String line = "============================================================================================================================================================";

	@BeforeTest
	public void setUp() {
		initialization();
		getData(ConfigOptionsTest.LOGIN_DIR + File.separator + ConfigOptionsTest.LOGIN_JSON_FILE);
		clickAllActionButton = new LoginConfiguration();
	}

	/**
	 * Assert true if login was not successful
	 */
	@Test(priority = 1)
	public void loginWithInvalidCredentials() {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.loginWithInvalidCredentials(),
				"Login failed due to incorrect credentials.");
	}

	/**
	 * Assert true if login was successful
	 * @throws InterruptedException 
	 */
	@Test(priority = 2)
	public void loginWithValidCredentials() throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.loginWithValidCredentials(), "Login successful.");
	}
	
	/**
	 * Assert true if logout was successful
	 * @throws InterruptedException 
	 */
	@Test(priority = 3)
	public void checkLogoutFunctionality() throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.checkLogoutFunctionality(), "Login successful.");
	}

	/**
	 * This method will be executed just after any function/method with @Test
	 * annotation ends.
	 * @throws InterruptedException 
	 */
	@AfterClass
	public void afterClass() throws InterruptedException {
		log.info(line);
		driver.findElement(By.xpath("//input[contains(@name,'username')]")).sendKeys(LoginAndSelectModule.testData.get("username"));
		driver.findElement(By.xpath("//input[contains(@autocomplete,'new-password')]")).sendKeys(LoginAndSelectModule.testData.get("password"));
		driver.findElement(By.xpath("//button[contains(@class,'sigBtn')]")).click();
		Thread.sleep(2000);
	}

}
