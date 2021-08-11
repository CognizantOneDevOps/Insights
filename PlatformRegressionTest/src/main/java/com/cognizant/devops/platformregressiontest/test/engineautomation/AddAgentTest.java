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
package com.cognizant.devops.platformregressiontest.test.engineautomation;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;


public class AddAgentTest extends LoginAndSelectModule{
	private static final Logger log = LogManager.getLogger(AddAgentTest.class);
	AddAgent clickAllActionButton;
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
		getData(ConfigOptionsTest.ENGINE_AUTO_DIR + File.separator + ConfigOptionsTest.AGENT_DUMMY_DATA_JSON_FILE);
		selectModuleOnClickingConfig("Agent Management");
		clickAllActionButton = new AddAgent();
	}

	/**
	 * 
	 * returns true if daemon agent service is running else false
	 * 
	 * @throws Exception
	 */
	@Test(priority = 1)
	public void verifyDamonagentServiceStatus() throws Exception {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.checkDaemonagentService(), "daemon agent service is running");
	}

	/**
	 * @param data
	 *             Replace agent block fields in server config with release related
	 *             details
	 */
	@Test(priority = 2)
	public void verifyServerConfigAgentDetailBlock() {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.verifyServerConfigAgentDetailBlock(),
				"Server config having on demand details.");
	}

	/**
	 * @param data
	 * @throws InterruptedException
	 * 
	 *                              Check if agent management module landing page is
	 *                              displayed
	 */
	@Test(priority = 3)
	public void navigateToAgentManagementLandingPage() throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.navigateToAgentManagementLandingPage(), "Landing page is displayed.");
	}

	/**
	 * @param data
	 * @throws InterruptedException
	 * 
	 *                              Register agent from the ondemand url
	 */
	@Test(priority = 4)
	public void registerAgent() throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.registerAgent(), "AgentId has been created");
	}
	
	/**
	 * returns true if Health and data queue are present else false
	 * @throws Exception
	 */
	@Test(priority = 5)
	public void verifyMessageQueue() throws Exception {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.checkQueue(), "Health and data queue are present");
	}
	
	/**
	 * returns true if InsightsEngine service is running else false
	 * @throws Exception
	 */
	@Test(priority = 6)
	public void verifyInsightsEngineServiceStatus() throws Exception {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.checkEngineService(), "Insights Engine service is running");
	}
	
	/**
	 * returns true if registered agent service is running else false
	 * @throws Exception
	 */
	@Test(priority = 7)
	public void verifyRegisteredagentServiceStatus() throws Exception {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.checkAgentService(), "Registered agent service is running");
	}
	
	/**
	 * returns true if tracking.json file is present for created agent else false
	 * @throws Exception
	 */
	@Test(priority = 8)
	public void checkTrackingFile() throws Exception {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.checkTrackingFile(), "tracking.json file is present for created agent");
	}
}
