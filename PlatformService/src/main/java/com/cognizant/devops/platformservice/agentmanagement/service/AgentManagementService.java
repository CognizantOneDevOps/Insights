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

package com.cognizant.devops.platformservice.agentmanagement.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;

public interface AgentManagementService {

	public String registerAgent(String toolName, String agentVersion, String osversion, String configDetails,
			String trackingDetails, boolean vault) throws InsightsCustomException;

	public String uninstallAgent(String agentId, String toolName, String osversion) throws InsightsCustomException;

	public String startStopAgent(String agentId, String toolName, String osversion, String action)
			throws InsightsCustomException;

	public String updateAgent(String agentId, String configDetails, String toolName, String agentVersion,
			String osversion, boolean vault) throws InsightsCustomException;

	// This is used during Agent registration, provide list of Agents, with version
	// for offline processing
	public Map<String, ArrayList<String>> getOfflineSystemAvailableAgentList() throws InsightsCustomException;

	// This is used during Agent registration, provide list of Agents, with version
	// for online processing,for docroot
	public Map<String, ArrayList<String>> getDocrootAvailableAgentList() throws InsightsCustomException;

	// This is used during Agent registration, provide list of Agents, with version
	// for online processing,for nexus
	public Map<String, ArrayList<String>> getRepoAvailableAgentList() throws InsightsCustomException;

	// For agent registration, gives you RAW config.json from docroot
	public String getToolRawConfigFile(String version, String tool) throws InsightsCustomException;

	// Provides currently registered Agents in DB
	public List<AgentConfigTO> getRegisteredAgents() throws InsightsCustomException;

	// Returns config.json for request AgentId from Db
	public AgentConfigTO getAgentDetails(String agentId) throws InsightsCustomException;
}
