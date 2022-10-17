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
package com.cognizant.devops.platformregressiontest.test.ui.logosetting;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

/**
 * @author NivethethaS
 * 
 *         Class contains the logic for logo setting roles test cases
 *
 */
public class LogoSettingTest extends LoginAndSelectModule {

	private static final Logger log = LogManager.getLogger(LogoSettingTest.class);

	LogoSettingConfiguration clickAllActionButton;

	String line = "============================================================================================================================================================";

	/**
	 * This method will be run before any test method belonging to the classes
	 * inside the <test> tag is run.
	 * 
	 * @throws InterruptedException
	 */
	@BeforeTest
	public void setUp() throws InterruptedException {
		initialization();
		getData(ConfigOptionsTest.LOGOSETTING_DIR + File.separator + ConfigOptionsTest.LOGO_JSON_FILE);
		selectModuleOnClickingConfig(LoginAndSelectModule.testData.get("moduleName"));
		clickAllActionButton = new LogoSettingConfiguration();
	}

	/**
	 * This method will be executed just before any function/method with @Test
	 * annotation starts.
	 * @throws InterruptedException 
	 */
	@BeforeMethod
	public void beforeMethod() throws InterruptedException {
		Thread.sleep(1000);
	}

	/**
	 * Assert true if landing page is displayed else false
	 */
	@Test(priority = 1)
	public void navigateToLogoSettingLandingPage() {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.navigateToLogoSettingLandingPage(),
				"Logo setting Landing page is displayed");
	}
	
	/**
	 * Assert true if adding logo successful else false
	 */
	@Test(priority = 2)
	public void addLogo() {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.addLogo(), "adding logo successful");
	}

	/**
	 * Assert true if error message is displayed else false
	 * @throws InterruptedException 
	 */
	@Test(priority = 3)
	public void addLargeFileSize() throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.addLargeFileSize(), "cannot add file size greater than 1 MB");
	}
	
	/**
	 * Assert true if cancel upload functionality successful else false
	 */
	@Test(priority = 4)
	public void cancelUpload() {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.cancelUpload(),
				"cancel upload functionality successful");
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
