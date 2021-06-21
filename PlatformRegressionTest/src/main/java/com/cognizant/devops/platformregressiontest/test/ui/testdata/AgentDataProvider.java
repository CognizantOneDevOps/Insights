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
package com.cognizant.devops.platformregressiontest.test.ui.testdata;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.DataProvider;

import com.cognizant.devops.platformregressiontest.test.common.ConfigOptionsTest;
import com.cognizant.devops.platformregressiontest.test.ui.utility.ReadJsonData;

public class AgentDataProvider {
	
	ReadJsonData readJsonData = ReadJsonData.getInstance();

	public static final String AGENT_PATH = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME) + File.separator
			+ ConfigOptionsTest.AUTO_DIR + File.separator + ConfigOptionsTest.AGENT_DIR + File.separator + ConfigOptionsTest.AGENT_JSON_FILE;
	
	public static final String AGENT_ONDEMAND_PATH = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME)
			+ File.separator + ConfigOptionsTest.AUTO_DIR + File.separator + ConfigOptionsTest.AGENT_DIR + File.separator + ConfigOptionsTest.AGENT_ONDEMAND_JSON_FILE;

	public static final String AGENT_RELEASE_PATH = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME)
			+ File.separator + ConfigOptionsTest.AUTO_DIR + File.separator + ConfigOptionsTest.AGENT_DIR + File.separator + ConfigOptionsTest.AGENT_RELEASE_JSON_FILE;

	public static final String AGENT_OFFLINE_PATH = System.getenv().get(ConfigOptionsTest.INSIGHTS_HOME)
			+ File.separator + ConfigOptionsTest.AUTO_DIR + File.separator + ConfigOptionsTest.AGENT_DIR + File.separator + ConfigOptionsTest.AGENT_OFFLINE_JSON_FILE;

	@DataProvider(name = "agentdataprovider")
	Object[][] getAgentData() throws IOException {
		return (ReadJsonData.readAgentJsonData(AGENT_PATH));
	}

	@DataProvider(name = "agentondemanddataprovider")
	Object[][] getAgentOnDemandData() throws IOException {
		return (ReadJsonData.readAgentJsonData(AGENT_ONDEMAND_PATH));
	}

	@DataProvider(name = "agentreleasedataprovider")
	Object[][] getAgentReleaseData() throws IOException {
		return (ReadJsonData.readAgentJsonData(AGENT_RELEASE_PATH));
	}

	@DataProvider(name = "agentofflinedataprovider")
	Object[][] getAgentOfflineData() throws IOException {
		return (ReadJsonData.readAgentJsonData(AGENT_OFFLINE_PATH));
	}

}