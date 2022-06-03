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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.agentConfig.AgentConfig;
import com.cognizant.devops.platformdal.agentConfig.AgentConfigDAL;
import com.cognizant.devops.platformservice.agentmanagement.service.AgentConfigTO;
import com.cognizant.devops.platformservice.agentmanagement.service.AgentManagementServiceImpl;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Test
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class AgentManagementTest extends AgentManagementTestData{
	
	public static final AgentManagementTestData agentManagementTestData = new AgentManagementTestData();
	public static final AgentManagementServiceImpl agentManagementServiceImpl =
												new AgentManagementServiceImpl();
	private static Logger log = LogManager.getLogger(AgentManagementTestData.class);
	
	@BeforeClass
	public void prepareData() throws InsightsCustomException {
		try {
			ApplicationConfigCache.loadConfigCache();
			prepareOfflineAgent(version);			
		} catch (Exception e) {
			log.error("message", e);
		}

	}
	
	/*Method to get the available list of agents in the system. */
//	@Test(priority = 1)
//	public void testGetSystemAvailableAgentList() throws InsightsCustomException {
//		
//		AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
//		try {
//			ApplicationConfigProvider.getInstance().getAgentDetails().setOnlineRegistration(true);
//			Map<String, ArrayList<String>> availableAgents = agentServiceImpl.getDocrootAvailableAgentList();
//			Assert.assertNotNull(availableAgents);
//			Assert.assertTrue(availableAgents.size() > 0);
//			Assert.assertTrue(availableAgents.containsKey("v5.0"));
//			Assert.assertTrue(availableAgents.containsKey("v5.2"));
//			
//			for (Map.Entry<String, ArrayList<String>> entry : availableAgents.entrySet()) {
//				
//			    if(entry.getKey().equals("v5.2") || entry.getKey().equals("v5.0")){
//				    ArrayList<String> toolNameList = entry.getValue();
//				    Assert.assertTrue(toolNameList.size() > 0);
//				    Assert.assertTrue(toolNameList.contains("git"));
//			    }
//			}
//		} catch (InsightsCustomException e) {
//			if (e.getMessage().contains("java.net.ConnectException")) {
//				log.debug("Unable to connect to docroot on internet");
//			}
//		}
//	
//	}
	
	@Test(priority = 2)
	public void testGetSystemAvailableAgentListForOfflineRegistration() throws InsightsCustomException {

		AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
		ApplicationConfigProvider.getInstance().getAgentDetails().setOnlineRegistration(false);
		Map<String, ArrayList<String>> availableAgents = agentServiceImpl.getOfflineSystemAvailableAgentList();
		
		Assert.assertNotNull(availableAgents);
		Assert.assertTrue(availableAgents.size() > 0);
		Assert.assertTrue(availableAgents.containsKey(version));
		
		for (Map.Entry<String, ArrayList<String>> entry : availableAgents.entrySet()) {
			
		    if(entry.getKey().equals(version)){
			    ArrayList<String> toolNameList = entry.getValue();
			    Assert.assertTrue(toolNameList.size()>0);
			    Assert.assertTrue(toolNameList.contains("git"));
		    }
		}
		
	}
	
//	@Test(priority = 3)
//	public void testGetToolRawConfigFile() throws InsightsCustomException {
//		
//		try {
//			ApplicationConfigProvider.getInstance().getAgentDetails().setOnlineRegistration(true);
//			AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
//			String configJson = agentServiceImpl.getToolRawConfigFile(version, gitTool,false);
//			
//			Gson gson = new Gson();
//			JsonElement jsonElement = gson.fromJson(configJson.trim(), JsonElement.class);
//			JsonObject json = jsonElement.getAsJsonObject();
//			Assert.assertNotNull(json);
//			Assert.assertEquals(json.get("toolCategory").getAsString(), "SCM");
//		} catch (InsightsCustomException e) {
//			if (e.getMessage().contains("java.net.ConnectException")) {
//				log.debug("Unable to connect to docroot on internet");
//			}
//		}
//	}
	
	@Test(priority = 4)
	public void testGetToolRawConfigFileForOfflineRegistration() throws InsightsCustomException {
		
		ApplicationConfigProvider.getInstance().getAgentDetails().setOnlineRegistration(false);
		AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
		String configJson = agentServiceImpl.getToolRawConfigFile(version, gitTool,false);

		Gson gson = new Gson();
		JsonElement jsonElement = gson.fromJson(configJson.trim(), JsonElement.class);
		JsonObject json = jsonElement.getAsJsonObject();
		Assert.assertNotNull(json);
		Assert.assertEquals(json.get("toolCategory").getAsString(), "SCM");
	}
	
	@Test(priority = 5) 
	public void testRegisterAgent() throws InsightsCustomException {
		
		AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
		String expectedOutcome = "SUCCESS";
		String oldVersion = "v9.1";
		String response = agentServiceImpl.registerAgent(gitTool, oldVersion, osversion, 
				configDetails, trackingDetails, false,false, "Agent");
		
		Assert.assertEquals(expectedOutcome, response);
			
	}
	
	@Test(priority = 6, expectedExceptions = InsightsCustomException.class) 
	public void testRegisterAgentWithDuplicateId() throws InsightsCustomException {
		
		AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
		String expectedOutcome = "Agent Id already exsits.";
		
		String response = agentServiceImpl.registerAgent(gitTool, 
							version, osversion, configDetails, trackingDetails, false,false, "Agent");
		
		Assert.assertEquals(expectedOutcome, response);
			
	}
	
	@Test(priority = 7, expectedExceptions = InsightsCustomException.class) 
	public void testAgentIDandToolNamenotequal() throws InsightsCustomException {
		
		AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
		String expectedOutcome = "Agent Id and Tool name cannot be the same.";
		
		String response = agentServiceImpl.registerAgent(gitTool, 
							version, osversion, configDetailsWithSameIDs, trackingDetails, false,false, "Agent");
		
		Assert.assertEquals(expectedOutcome, response);
			
	}
	
	@Test(priority = 8, expectedExceptions = InsightsCustomException.class) 
	public void testRegisterAgentWithInvalidDataLabelName() throws InsightsCustomException {
		
		AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
		String expectedOutcome = "Invalid data label Name, it should contain only alphanumeric character,underscore & dot";
		
		String response = agentServiceImpl.registerAgent(gitTool, 
							version, osversion, configDetailsWithInvalidDataLabel, trackingDetails, false,false, "Agent");
		
		Assert.assertEquals(expectedOutcome, response);
			
	}
	
	@Test(priority = 9, expectedExceptions = InsightsCustomException.class) 
	public void testRegisterAgentWithInvalidHealthLabelName() throws InsightsCustomException {
		
		AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
		String expectedOutcome = "Invalid health label Name, it should contain only alphanumeric character,underscore & dot";
		
		String response = agentServiceImpl.registerAgent(gitTool, 
							version, osversion, 
							configDetailsWithInvalidHealthLabel, trackingDetails, false, false, "Agent");
		
		Assert.assertEquals(expectedOutcome, response);
			
	}
	
	@Test(priority = 10) 
	public void testRegisterAgentInDatabase() throws InsightsCustomException {

		AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
		
		List<AgentConfigTO>  registeredAgents = agentServiceImpl.getRegisteredAgentsAndHealth();
		 /*for (AgentConfigTO agentConfig : registeredAgents) {
			Assert.assertTrue(agentConfig.getToolName().equals("git"));
		} */
        Assert.assertTrue(registeredAgents.size() > 0);
	}
	
	@Test(priority = 11)
	public void testGetRegisteredAgents() throws InsightsCustomException {
		
		Assert.assertFalse(agentManagementServiceImpl.getRegisteredAgentsAndHealth().isEmpty());		
	}
	
	@Test(priority = 12)
	public void testStartStopAgentForStartAction() throws InsightsCustomException {

		String action = "START";
		AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
		String expectedOutput = "SUCCESS";
		String response = agentServiceImpl.startStopAgent(agentId, gitTool,osversion, action);
		Assert.assertNotNull(response);
		Assert.assertEquals(response, expectedOutput);
	}
	
	@Test(priority = 13)
	public void testStartStopAgentForStopAction() throws InsightsCustomException {

		String action = "STOP";
		AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
		String expectedOutput = "SUCCESS";
		String response = agentServiceImpl.startStopAgent(agentId, gitTool, osversion, action);
		Assert.assertNotNull(response);
		Assert.assertEquals(response, expectedOutput);
	}
	
	@Test(priority = 14)
	public void testStartStopAgentForNoAction() throws InsightsCustomException {

		String action = "REGISTER";
		String osversion = "WINDOWS";
		String expectedOutput = "SUCCESS";
		String response = agentManagementServiceImpl.startStopAgent(agentId, gitTool, osversion, action);
		Assert.assertNotNull(response);
		Assert.assertEquals(response, expectedOutput);
	}
	
	@Test(priority = 15)
	public void testStartStopAgentForLinux() throws InsightsCustomException {

		String action = "REGISTER";
		String osversion = "LINUX";
		String expectedOutput = "SUCCESS";
		String response = agentManagementServiceImpl.startStopAgent(agentId, gitTool,osversion, action);
		Assert.assertNotNull(response);
		Assert.assertEquals(response, expectedOutput);
	}

	@Test(priority = 16, expectedExceptions = InsightsCustomException.class)
	public void testStartStopAgentForException() throws InsightsCustomException {

		String action = "REGISTER";
		String osversion = "WINDOWS";
		String expectedOutput = "SUCCESS";
		String response = agentManagementServiceImpl.startStopAgent("123456ASC", gitTool, osversion, action);
		Assert.assertNotNull(response);
		Assert.assertEquals(response, expectedOutput);
	}
	
	@Test (priority = 17)
	public void getAgentDetails() throws InsightsCustomException {
		
		AgentConfig agentConfig = new AgentConfig();
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		agentConfig = agentConfigDAL.getAgentConfigurations(agentId);
		AgentConfigTO agentConfigDetails = new AgentConfigTO();
		agentConfigDetails = agentManagementServiceImpl.getAgentDetails(agentId);
		Assert.assertNotNull(agentConfigDetails.getAgentId());
		Assert.assertEquals(agentConfigDetails.getToolCategory(), "SCM");
		
	}
	
	@Test (priority = 18)
	public void getAgentDetailsForException() throws InsightsCustomException {

		AgentConfig agentConfig = new AgentConfig();
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		AgentConfigTO agentConfigDetails = agentManagementServiceImpl.getAgentDetails(agentId);
	}
	
	@Test(priority = 19)
	public void testUpdateAgent() throws InsightsCustomException {
		
		ApplicationConfigProvider.getInstance().getAgentDetails().setOnlineRegistration(true);
		AgentManagementServiceImpl agentManagementServiceImpl = new AgentManagementServiceImpl();
		agentManagementServiceImpl.updateAgent(agentId, configDetails, gitTool, 
																version, osversion, false,false);
	}
	
	@Test(priority = 20)
	public void testUpdateAgentVersion() throws InsightsCustomException {
		String expectedOutcome = "SUCCESS";
		//String version = "v8.0";
		ApplicationConfigProvider.getInstance().getAgentDetails().setOnlineRegistration(true);
		AgentManagementServiceImpl agentManagementServiceImpl = new AgentManagementServiceImpl();
		String response = agentManagementServiceImpl.updateAgent(agentId, configDetails, gitTool, version, osversion, false,false);
		Assert.assertEquals(expectedOutcome, response);
	}
	
	@Test(priority = 21)
	public void testUninstallAgent() throws InsightsCustomException{

		String expectedOutCome = "SUCCESS"; 
		String response = agentManagementServiceImpl.uninstallAgent(agentId, gitTool, osversion);
		Assert.assertEquals(expectedOutCome, response);
		
	}
	
	@Test(priority = 22, expectedExceptions = InsightsCustomException.class)
	public void testUninstallAgentForException() throws InsightsCustomException{

		String expectedOutCome =  "No entity found for query";
		String response = agentManagementServiceImpl.uninstallAgent("12345fghj", gitTool, osversion);
		Assert.assertEquals(expectedOutCome, response);
	}
	
	@Test(priority = 23) 
	public void testRegisterWebhookAgent() throws InsightsCustomException {
		AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
		String expectedOutcome = "SUCCESS";
		String configJson = agentServiceImpl.getToolRawConfigFile(version, gitTool,true);
		String response = agentServiceImpl.registerAgent(gitTool, version, osversion, 
				webhookConfigDetails, tracking, false,true,"Webhook");
		
		Assert.assertEquals(expectedOutcome, response);
		
			
	}
	
	@Test(priority = 24, expectedExceptions = InsightsCustomException.class) 
	public void testRegisterIncorrectWebhookAgent() throws InsightsCustomException {
		AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
		String toolname = "neo4jarchival";
		String configJson = agentServiceImpl.getToolRawConfigFile(version, toolname,true);
			
	}
	
	@Test(priority = 25, expectedExceptions = InsightsCustomException.class) 
	public void testRegisterWithInvalidAgentId() throws InsightsCustomException {
		String expectedOutcome = "Agent Id has to be Alpha numeric with '_' as special character";
		String response = agentManagementServiceImpl.registerAgent(gitTool, version, osversion, 
				ConfigDetailsWithInvalidAgentId, trackingDetails, false,true,"Agent");
		
		Assert.assertEquals(expectedOutcome, response);
			
	}
	
	@Test(priority = 26)
	public void testUninstallWebhookAgent() throws InsightsCustomException{
		String expectedOutCome = "SUCCESS"; 
		String response = agentManagementServiceImpl.uninstallAgent(webhookAgentId, gitTool, osversion);
		Assert.assertEquals(expectedOutCome, response);
		
	}
	
	@Test (priority = 27)
	public void testgetRepoAvailableAgentList() throws InsightsCustomException {
		
		Map<String, ArrayList<String>> agentList = agentManagementServiceImpl.getRepoAvailableAgentList();
		Assert.assertTrue(agentList.size() > 0);
	}
	
	@Test(priority = 28, expectedExceptions = InsightsCustomException.class)
	public void testUpdateAgentWithEmptyAgentId() throws InsightsCustomException {
		
		ApplicationConfigProvider.getInstance().getAgentDetails().setOnlineRegistration(true);
		AgentManagementServiceImpl agentManagementServiceImpl = new AgentManagementServiceImpl();
		agentManagementServiceImpl.updateAgent("", configDetails, gitTool, version, osversion, false,false);
	}
	
	
	@Test(priority = 29)
	public void testGetAvailableAgentTags() throws InsightsCustomException {
		
		AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
		try {
			Map<String, String> availableAgentTags = agentServiceImpl.getAvailableAgentTags();
			log.debug(availableAgentTags);
			Assert.assertNotNull(availableAgentTags);
			Assert.assertTrue(availableAgentTags.size() > 0);
			Assert.assertTrue(availableAgentTags.containsValue("v9.1"));
			Assert.assertTrue(availableAgentTags.containsValue("v9.2"));
			
		} catch (InsightsCustomException e) {
			log.error("Unable to fetch agents tags");
		}
	
	}
	
	@Test(priority = 30)
	public void testDownloadAgentPackage() throws InsightsCustomException {
		
		AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
		
		String version = "v9.1";
		String dirPath = ApplicationConfigProvider.getInstance().getAgentDetails().getOfflineAgentPath() + File.separator + version;
		try {
			FileUtils.deleteDirectory(new File(dirPath));
			ApplicationConfigProvider.getInstance().getAgentDetails().setOnlineRegistration(false);
			String response = agentServiceImpl.downloadAgentPackageFromGithub(version);
			log.debug(response);
			File targetDirPath = new File(dirPath);
			 Assert.assertTrue(targetDirPath.exists());
		} catch (InsightsCustomException | IOException e) {
			log.error("Unable to download agent package");
		}
	
	}
	
	@Test(priority = 31, expectedExceptions = InsightsCustomException.class)
	public void testDownloadAgentPackageException() throws InsightsCustomException {
		
		AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
		String version = "v9.1";
		String dirPath = ApplicationConfigProvider.getInstance().getAgentDetails().getOfflineAgentPath() + File.separator + version;
		ApplicationConfigProvider.getInstance().getAgentDetails().setOnlineRegistration(false);
		String response = agentServiceImpl.downloadAgentPackageFromGithub(version);
		String expectedReponse = version + " - Package already exist!";
		Assert.assertTrue(response.equalsIgnoreCase(expectedReponse));
	}
	
	@AfterClass
	public void cleanUp() throws InsightsCustomException, IOException {
		//FileUtils.deleteDirectory(new File(ApplicationConfigProvider.getInstance().getAgentDetails().getOfflineAgentPath()));
	}
	
}
