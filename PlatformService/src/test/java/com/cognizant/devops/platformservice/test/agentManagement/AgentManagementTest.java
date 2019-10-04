/*******************************************************************************
 *  * Copyright 2017 Cognizant Technology Solutions
 *  * 
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  * use this file except in compliance with the License.  You may obtain a copy
 *  * of the License at
 *  * 
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  * 
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  * License for the specific language governing permissions and limitations under
 *  * the License.
 *******************************************************************************/
package com.cognizant.devops.platformservice.test.agentManagement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.agentConfig.AgentConfig;
import com.cognizant.devops.platformdal.agentConfig.AgentConfigDAL;
import com.cognizant.devops.platformservice.agentmanagement.service.AgentConfigTO;
import com.cognizant.devops.platformservice.agentmanagement.service.AgentManagementServiceImpl;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

@Test
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class AgentManagementTest extends AgentManagementTestData{
	
	public static final AgentManagementTestData agentManagementTestData = new AgentManagementTestData();
	public static final AgentManagementServiceImpl agentManagementServiceImpl =
												new AgentManagementServiceImpl();
	private static Logger log = LogManager.getLogger(AgentManagementTestData.class);

	private JsonObject getProperties() {
		Gson gson = new Gson();
		JsonElement jsonElement = gson.fromJson(agentManagementTestData.configDetails.trim(), JsonElement.class);
		JsonObject json = jsonElement.getAsJsonObject();
		json.addProperty("osversion", osversion);
		json.addProperty("agentVersion", agentVersion);
		json.addProperty("toolName", toolName.toUpperCase());
		return json;
	}
	
	/*Method to get the available list of agents in the system. */
	@Test(priority = 1)
	public void testGetSystemAvailableAgentList() throws InsightsCustomException {
		
		AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
		try {
			ApplicationConfigProvider.getInstance().getAgentDetails().setOnlineRegistration(true);
			Map<String, ArrayList<String>> availableAgents = agentServiceImpl.getSystemAvailableAgentList();
			Assert.assertNotNull(availableAgents);
			Assert.assertTrue(availableAgents.size() > 0);
			Assert.assertTrue(availableAgents.containsKey("v5.0"));
			Assert.assertTrue(availableAgents.containsKey("v5.2"));
			
			for (Map.Entry<String, ArrayList<String>> entry : availableAgents.entrySet()) {
				
			    if(entry.getKey().equals("v5.2") || entry.getKey().equals("v5.0")){
				    ArrayList<String> toolNameList = entry.getValue();
				    Assert.assertTrue(toolNameList.size() > 0);
				    Assert.assertTrue(toolNameList.contains("git"));
			    }
			}
		} catch (InsightsCustomException e) {
			if (e.getMessage().contains("java.net.ConnectException")) {
				log.debug("Unable to connect to docroot on internet");
			}
		}
	
	}
	
	@Test(priority = 2)
	public void testGetSystemAvailableAgentListForOfflineRegistration() throws InsightsCustomException {

		AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
		ApplicationConfigProvider.getInstance().getAgentDetails().setOnlineRegistration(false);
		Map<String, ArrayList<String>> availableAgents = agentServiceImpl.getSystemAvailableAgentList();
		
		Assert.assertNotNull(availableAgents);
		Assert.assertTrue(availableAgents.size() > 0);
		Assert.assertTrue(availableAgents.containsKey("v5.2"));
		
		for (Map.Entry<String, ArrayList<String>> entry : availableAgents.entrySet()) {
			
		    if(entry.getKey().equals("v5.2")){
			    ArrayList<String> toolNameList = entry.getValue();
			    Assert.assertTrue(toolNameList.size()>0);
			    Assert.assertTrue(toolNameList.contains("git"));
			    Assert.assertTrue(toolNameList.contains("pivotalTracker"));
		    }
		}
		
	}
	
	@Test(priority = 3)
	public void testGetToolRawConfigFile() throws InsightsCustomException {
		
		String version ="v5.2";
		String tool = "git";
		try {
			ApplicationConfigProvider.getInstance().getAgentDetails().setOnlineRegistration(true);
			AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
			String configJson = agentServiceImpl.getToolRawConfigFile(version, tool);
			
			Gson gson = new Gson();
			JsonElement jsonElement = gson.fromJson(configJson.trim(), JsonElement.class);
			JsonObject json = jsonElement.getAsJsonObject();
			Assert.assertNotNull(json);
			Assert.assertEquals(json.get("toolCategory").getAsString(), "SCM");
		} catch (InsightsCustomException e) {
			if (e.getMessage().contains("java.net.ConnectException")) {
				log.debug("Unable to connect to docroot on internet");
			}
		}
	}
	
	@Test(priority = 4)
	public void testGetToolRawConfigFileForOfflineRegistration() throws InsightsCustomException {
		
		String version ="v5.2";
		String tool = "pivotalTracker";
		ApplicationConfigProvider.getInstance().getAgentDetails().setOnlineRegistration(false);
		AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
		String configJson = agentServiceImpl.getToolRawConfigFile(version, tool);

		Gson gson = new Gson();
		JsonElement jsonElement = gson.fromJson(configJson.trim(), JsonElement.class);
		JsonObject json = jsonElement.getAsJsonObject();
		Assert.assertNotNull(json);
		Assert.assertEquals(json.get("toolCategory").getAsString(), "ALM");
	}
	
	@Test(priority = 5) 
	public void testRegisterAgent() throws InsightsCustomException {
		
		AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
		String expectedOutcome = "SUCCESS";
		
		String response = agentServiceImpl.registerAgent(agentManagementTestData.toolName, 
							agentManagementTestData.agentVersion, agentManagementTestData.osversion, 
							agentManagementTestData.configDetails, agentManagementTestData.trackingDetails);
		
		Assert.assertEquals(expectedOutcome, response);
			
	}
	
	@Test(priority = 6) 
	public void testRegisterAgentInDatabase() throws InsightsCustomException {

		AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
		
		List<AgentConfigTO>  registeredAgents = agentServiceImpl.getRegisteredAgents();
		for (AgentConfigTO agentConfig : registeredAgents) {
			Assert.assertTrue(agentConfig.getToolName().equals("git"));
		}
	}
	
	@Test(priority = 7)
	public void testGetRegisteredAgents() throws InsightsCustomException {
		
		Assert.assertFalse(agentManagementServiceImpl.getRegisteredAgents().isEmpty());		
	}
	
	@Test(priority = 8)
	public void testStartStopAgentForStartAction() throws InsightsCustomException {

		String action = "START";
		AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
		String expectedOutput = "SUCCESS";
		String response = agentServiceImpl.startStopAgent(agentManagementTestData.agentId, agentManagementTestData.toolName,
							agentManagementTestData.osversion, action);
		Assert.assertNotNull(response);
		Assert.assertEquals(response, expectedOutput);
	}
	
	@Test(priority = 9)
	public void testStartStopAgentForStopAction() throws InsightsCustomException {

		String action = "STOP";
		AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
		String expectedOutput = "SUCCESS";
		String response = agentServiceImpl.startStopAgent(agentManagementTestData.agentId, agentManagementTestData.toolName, 
																					agentManagementTestData.osversion, action);
		Assert.assertNotNull(response);
		Assert.assertEquals(response, expectedOutput);
	}
	
	@Test(priority = 10)
	public void testStartStopAgentForNoAction() throws InsightsCustomException {

		String action = "REGISTER";
		String osversion = "WINDOWS";
		String expectedOutput = "SUCCESS";
		String response = agentManagementServiceImpl.startStopAgent(agentManagementTestData.agentId, agentManagementTestData.toolName,
																									osversion, action);
		Assert.assertNotNull(response);
		Assert.assertEquals(response, expectedOutput);
	}
	
	@Test(priority = 11)
	public void testStartStopAgentForLinux() throws InsightsCustomException {

		String action = "REGISTER";
		String osversion = "LINUX";
		String expectedOutput = "SUCCESS";
		String response = agentManagementServiceImpl.startStopAgent(agentManagementTestData.agentId, agentManagementTestData.toolName,
																									osversion, action);
		Assert.assertNotNull(response);
		Assert.assertEquals(response, expectedOutput);
	}

	@Test(priority = 12, expectedExceptions = InsightsCustomException.class)
	public void testStartStopAgentForException() throws InsightsCustomException {

		String action = "REGISTER";
		String osversion = "WINDOWS";
		String expectedOutput = "SUCCESS";
		String response = agentManagementServiceImpl.startStopAgent("123456ASC", agentManagementTestData.toolName, osversion, action);
		Assert.assertNotNull(response);
		Assert.assertEquals(response, expectedOutput);
	}
	
	@Test (priority = 13)
	public void getAgentDetails() throws InsightsCustomException {
		
		AgentConfig agentConfig = new AgentConfig();
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		agentConfig = agentConfigDAL.getAgentConfigurations(agentManagementTestData.agentId);
		AgentConfigTO agentConfigDetails = new AgentConfigTO();
		agentConfigDetails = agentManagementServiceImpl.getAgentDetails(agentManagementTestData.agentId);
		Assert.assertNotNull(agentConfigDetails.getAgentId());
		Assert.assertEquals(agentConfigDetails.getToolCategory(), "SCM");
		
	}
	
	@Test (priority = 14)
	public void getAgentDetailsForException() throws InsightsCustomException {

		AgentConfig agentConfig = new AgentConfig();
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		AgentConfigTO agentConfigDetails = agentManagementServiceImpl.getAgentDetails(agentManagementTestData.agentId);
	}
	
	@Test(priority = 15)
	public void testUpdateAgent() throws InsightsCustomException {
		
		ApplicationConfigProvider.getInstance().getAgentDetails().setOnlineRegistration(true);
		AgentManagementServiceImpl agentManagementServiceImpl = new AgentManagementServiceImpl();
		agentManagementServiceImpl.updateAgent(agentManagementTestData.agentId, configDetails, agentManagementTestData.toolName, 
																agentManagementTestData.agentVersion, agentManagementTestData.osversion);
	}
	
	@Test(priority = 16)
	public void testUninstallAgent() throws InsightsCustomException{

		String expectedOutCome = "SUCCESS"; 
		String response = agentManagementServiceImpl.uninstallAgent(agentManagementTestData.agentId, agentManagementTestData.toolName, 
																							agentManagementTestData.osversion);
		Assert.assertEquals(expectedOutCome, response);
		
	}
	@Test(priority = 17, expectedExceptions = InsightsCustomException.class)
	public void testUninstallAgentForException() throws InsightsCustomException{

		String expectedOutCome =  "No entity found for query";
		String response = agentManagementServiceImpl.uninstallAgent("12345fghj", agentManagementTestData.toolName, 
																							agentManagementTestData.osversion);
		Assert.assertEquals(expectedOutCome, response);
	}
}
