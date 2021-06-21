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
package com.cognizant.devops.platformregressiontest.test.ui.agentmanagement;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;
import com.cognizant.devops.platformregressiontest.test.ui.testdata.AgentDataProvider;
import com.cognizant.devops.platformregressiontest.test.ui.testdatamodel.AgentManagementDataModel;

/**
 * @author Nainsi
 * 
 *         Class contains the test cases for Agent Management Module
 *
 */
public class OnDemandTest extends LoginAndSelectModule {

	private static final Logger log = LogManager.getLogger(OnDemandTest.class);
	AgentConfiguration clickAllActionButton;
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
		getData(ConfigOptionsTest.AGENT_DIR + File.separator + ConfigOptionsTest.AGENT_ONDEMAND_JSON_FILE);
		selectModuleOnClickingConfig("Agent Management");
		clickAllActionButton = new AgentConfiguration();
	}

	/**
	 * 
	 * Replace agent block fields in server config with on demand related details
	 * 
	 * @param data
	 */
	@Test(priority = 1, enabled = true, dataProvider = "agentondemanddataprovider", dataProviderClass = AgentDataProvider.class)
	public void verifyServerConfigAgentDetailBlock(AgentManagementDataModel data) {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.verifyServerConfigAgentDetailBlock(data),
				"Server config having on demand details.");
	}

	/**
	 * Check if agent management module landing page is displayed
	 * 
	 * @param data
	 * @throws InterruptedException
	 */
	@Test(priority = 2, enabled = true, dataProvider = "agentondemanddataprovider", dataProviderClass = AgentDataProvider.class)
	public void navigateToAgentManagementLandingPage(AgentManagementDataModel data) throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.navigateToAgentManagementLandingPage(data),
				"Landing page is displayed.");
	}

	/**
	 * Register agent from the on demand url
	 * 
	 * @param data
	 * @throws InterruptedException
	 */
	@Test(priority = 3, enabled = true, dataProvider = "agentondemanddataprovider", dataProviderClass = AgentDataProvider.class)
	public void registerAgent(AgentManagementDataModel data) throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.registerAgent(data), "AgentId has been created");
	}

	/**
	 * register agent with same id. If already exists message is visible then return
	 * true.
	 * 
	 * @param data
	 * @throws InterruptedException
	 */
	@Test(priority = 4, enabled = true, dataProvider = "agentondemanddataprovider", dataProviderClass = AgentDataProvider.class)
	public void registerAgentWithSameAgentID(AgentManagementDataModel data) throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.registerAgentWithSameAgentId(data), "AgentId already exists");
	}

	/**
	 * Update agent with changing some fields in config.json on UI
	 * 
	 * @param data
	 * @throws InterruptedException
	 */
	@Test(priority = 5, enabled = true, dataProvider = "agentondemanddataprovider", dataProviderClass = AgentDataProvider.class)
	public void updateAgent(AgentManagementDataModel data) throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.updateAgent(data), "Agent has been updated");
	}

	/**
	 * 
	 * Check start and stop functionality
	 * 
	 * @param data
	 * @throws InterruptedException
	 */
	@Test(priority = 6, enabled = true, dataProvider = "agentondemanddataprovider", dataProviderClass = AgentDataProvider.class)
	public void stopAndStartAgent(AgentManagementDataModel data) throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.startAgent(data), "Agent has been stopped and started successfully.");
	}

	/**
	 * Delete the agent and check if not present in the list then return true else
	 * false
	 * 
	 * @param data
	 * @throws InterruptedException
	 */
	@Test(priority = 7, enabled = true, dataProvider = "agentondemanddataprovider", dataProviderClass = AgentDataProvider.class)
	public void deleteAgent(AgentManagementDataModel data) throws InterruptedException {
		log.info(line);
		Assert.assertTrue(clickAllActionButton.deleteAgent(data), "Agent has been deleted");
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
