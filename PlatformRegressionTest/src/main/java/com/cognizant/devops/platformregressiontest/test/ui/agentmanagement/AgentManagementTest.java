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
package com.cognizant.devops.platformregressiontest.test.ui.agentmanagement;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

public class AgentManagementTest extends LoginAndSelectModule {

	AgentManagementConfiguration clickAllActionButton;

	@BeforeTest
	public void setUp() {
		initialization();
		getData(ConfigOptionsTest.AGENT_JSON_FILE);
		selectModuleUnderConfiguration(LoginAndSelectModule.testData.get("agentManagement"));
		clickAllActionButton = new AgentManagementConfiguration();
	}

	@Test(priority = 1)
	public void registerAgent() throws InterruptedException {

		Assert.assertTrue(clickAllActionButton.registerAgent(), "AgentId has been created");
	}

	@Test(priority = 2)
	public void registerAgentWithSameAgentID() throws InterruptedException {

		Assert.assertTrue(clickAllActionButton.registerAgentWithSameAgentId(), "AgentId already exists");
	}

	@Test(priority = 3)
	public void updateAgent() throws InterruptedException {

		Assert.assertTrue(clickAllActionButton.updateAgent(), "Agent has been updated");
	}
	
	@Test(priority = 4)
	public void startAgent() throws InterruptedException {

		Assert.assertTrue(clickAllActionButton.startAgent(), "Agent has been updated");
	}
  
	@Test(priority = 5)
	public void deleteAgent() throws InterruptedException {

		Assert.assertTrue(clickAllActionButton.deleteAgent(), "Agent has been deleted");
	}

	@AfterTest
	public void tearDown() {

		driver.quit();
	}

}
