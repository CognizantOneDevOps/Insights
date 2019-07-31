package com.cognizant.devops.platformservice.test.agentManagement;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.core.enums.AGENTACTION;
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
public class AgentManagementTest extends AgentDummyData{
	
	private static final String UNINSTALL_AGENT = AGENTACTION.UNINSTALL.name();
	public static final AgentDummyData agentDummyData = new AgentDummyData();
	public static final AgentManagementServiceImpl agentManagementServiceImpl =
												new AgentManagementServiceImpl();

	private JsonObject getProperties() {
		Gson gson = new Gson();
		JsonElement jsonElement = gson.fromJson(agentDummyData.configDetails.trim(), JsonElement.class);
		JsonObject json = jsonElement.getAsJsonObject();
		json.addProperty("osversion", osversion);
		json.addProperty("agentVersion", agentVersion);
		json.addProperty("toolName", toolName.toUpperCase());
		return json;
	}
	
	@Test(expectedExceptions = InsightsCustomException.class) 
	public void testRegisterAgent() throws InsightsCustomException {
		
		JsonObject json = getProperties();
		ApplicationConfigProvider.getInstance().getAgentDetails().setUnzipPath("C://Agents//unzip");
		AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
		String expectedOutcome = "SUCCESS";
		System.out.println(agentDummyData.toolName);
		
		String response = agentServiceImpl.registerAgent(agentDummyData.toolName, 
							agentDummyData.agentVersion, agentDummyData.osversion, 
							agentDummyData.configDetails, agentDummyData.trackingDetails);
		
		/*String response1 = agentServiceImpl.registerAgent("@d#", 
				agentDummyData.agentVersion, agentDummyData.osversion, 
				agentDummyData.configDetails, agentDummyData.trackingDetails);*/
		
		Assert.assertEquals(expectedOutcome, response);
		
		List<AgentConfigTO>  registeredAgents = agentServiceImpl.getRegisteredAgents();
		for (AgentConfigTO agentConfig : registeredAgents) {
			Assert.assertTrue(agentConfig.equals("git"));
		}
		
	}
	
	/*@Test(expectedExceptions = InsightsCustomException.class) 
	public void testRegisterAgentInSystem() throws InsightsCustomException {
		
		JsonObject json = getProperties();
		ApplicationConfigProvider.getInstance().getAgentDetails().setUnzipPath("C://Agents//unzip");
		AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
		
		String response = agentServiceImpl.registerAgent(agentDummyData.toolName, 
							agentDummyData.agentVersion, agentDummyData.osversion, 
							agentDummyData.configDetails, agentDummyData.trackingDetails);
		System.out.println(agentServiceImpl.getSystemAvailableAgentList());
		
	}*/
	
	@Test(expectedExceptions = InsightsCustomException.class) 
	public void testRegisterAgentWithEmptyTrackingDetails() throws InsightsCustomException {
		
		String expectedOutcome = "SUCCESS";
		String response = agentManagementServiceImpl.registerAgent(agentDummyData.toolName, 
							agentDummyData.agentVersion, agentDummyData.osversion, 
							agentDummyData.configDetails, null);
		
		Assert.assertEquals(expectedOutcome, response);
		
	}
	
	/*Method to get the available list of agents in the system. */
	@Test
	public void testGetSystemAvailableAgentList() throws InsightsCustomException {
		AgentManagementServiceImpl agentImpl = new AgentManagementServiceImpl();
		Map<String, ArrayList<String>> availableAgents = agentImpl.getSystemAvailableAgentList();
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
	
	}
	
	@Test
	public void testGetSystemAvailableAgentListForOfflineRegistration() throws InsightsCustomException {
		AgentManagementServiceImpl agentImpl = new AgentManagementServiceImpl();
		ApplicationConfigProvider.getInstance().getAgentDetails().setOnlineRegistration(false);
		Map<String, ArrayList<String>> availableAgents = agentImpl.getSystemAvailableAgentList();
		
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
	
	@Test
	public void testGetRegisteredAgents() throws InsightsCustomException {
//		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
//		List<AgentConfig> agentConfigList = agentConfigDAL.getAllDataAgentConfigurations();
//		List<AgentConfigTO> agentList = new ArrayList<>(agentConfigList.size());
//		Assert.assertNotNull(agentList); 
		Assert.assertFalse(agentManagementServiceImpl.getRegisteredAgents().isEmpty());		
	}
	
	
	
	@Test(expectedExceptions = NullPointerException.class)
	public void testSaveAgentConfigFromUI() throws InsightsCustomException {
		
		Gson gson = new Gson();
		JsonElement jsonElement = gson.fromJson(agentDummyData.configDetails.trim(), JsonElement.class);
		JsonObject json = jsonElement.getAsJsonObject();
		json.addProperty("osversion", osversion);
		json.addProperty("agentVersion", agentVersion);
		json.addProperty("toolName", toolName.toUpperCase());
		 
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL(); 
		boolean response = agentConfigDAL.saveAgentConfigFromUI(agentDummyData.agentId, 
							json.get("toolCategory").getAsString(), agentDummyData.toolName, json, 
							agentDummyData.agentVersion, agentDummyData.osversion, agentDummyData.updateDate);
		
		Assert.assertTrue(response);
	}
	 
	@Test(expectedExceptions = InsightsCustomException.class)
	public void testStartStopAgentForStartAction() throws InsightsCustomException {
		
		String action = "START";
		
		String expectedOutput = "SUCCESS";
		String response = agentManagementServiceImpl.startStopAgent("JENKINS_123", agentDummyData.toolName,
							agentDummyData.osversion, action);
		Assert.assertNotNull(response);
		Assert.assertEquals(response, expectedOutput);
	}
	
	@Test(expectedExceptions = InsightsCustomException.class)
	public void testStartStopAgentForStopAction() throws InsightsCustomException {
		
		String action = "STOP";
		String osversion = "WINDOWS";
		
		String expectedOutput = "SUCCESS";
		String response = agentManagementServiceImpl.startStopAgent("JENKINS_123", toolName, osversion, action);
		Assert.assertNotNull(response);
		Assert.assertEquals(response, expectedOutput);
	}
	
	@Test(expectedExceptions = InsightsCustomException.class)
	public void testStartStopAgentForNoAction() throws InsightsCustomException {
		
		String action = "REGISTER";
		String osversion = "WINDOWS";
		String expectedOutput = "SUCCESS";
		String response = agentManagementServiceImpl.startStopAgent("JENKINS_123", toolName, osversion, action);
		Assert.assertNotNull(response);
		Assert.assertEquals(response, expectedOutput);
	}
	

	@Test(expectedExceptions = InsightsCustomException.class)
	public void testStartStopAgentForException() throws InsightsCustomException {
		
		String action = "REGISTER";
		String osversion = "WINDOWS";
		String expectedOutput = "SUCCESS";
		String response = agentManagementServiceImpl.startStopAgent("123456ASC", toolName, osversion, action);
		Assert.assertNotNull(response);
		Assert.assertEquals(response, expectedOutput);
	}
	
	
	private String getAgentkey(String toolName) {
		return toolName + "_" + Instant.now().toEpochMilli();
	}
	
	@Test 
	public void testDeleteAgentConfigurations() {
		
		String agentKey = "GIT_1234567890";
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		List<AgentConfig> result = agentConfigDAL.deleteAgentConfigurations(agentKey);
		Assert.assertTrue(result.size() > 0);
	}
	
	@Test(expectedExceptions = InsightsCustomException.class)
	public void testGetToolRawConfigFile() throws InsightsCustomException {
		String version ="v5.2";
		String tool = "pivotalTracker";
		ApplicationConfigProvider.getInstance().getAgentDetails().setOnlineRegistration(true);
		ApplicationConfigProvider.getInstance().getAgentDetails().setUnzipPath("C://Agents//unzip");
		ApplicationConfigProvider.getInstance().getAgentDetails().setOfflineAgentPath("C://Agents//offline");
		AgentManagementServiceImpl impl = new AgentManagementServiceImpl();
		String configJson = impl.getToolRawConfigFile(version, tool);
		
		Assert.assertNotNull(configJson);
		
		Gson gson = new Gson();
		JsonElement jsonElement = gson.fromJson(configJson.trim(), JsonElement.class);
		JsonObject json = jsonElement.getAsJsonObject();
		Assert.assertEquals(json.get("toolCategory"), "ALM");
	}
	
	@Test(expectedExceptions = InsightsCustomException.class)
	public void testGetToolRawConfigFileForOfflineRegistration() throws InsightsCustomException {
		String version ="v5.2";
		String tool = "pivotalTracker";
		String expectedOutcome = "{\"mqConfig\":{\"user\":\"iSight\",\"password\":\"iSight\",\"host\":\"127.0.0.1\",\"exchange\":\"iSight\",\"agentControlXchg\":\"iAgent\"},\"subscribe\":{\"config\":\"ALM.PIVOTALTRACKER.config\",\"agentCtrlQueue\":\"pivotal_agent\"},\"publish\":{\"data\":\"ALM.PIVOTALTRACKER.DATA\",\"health\":\"ALM.PIVOTALTRACKER.HEALTH\"},\"communication\":{\"type\":\"REST\",\"sslVerify\":false,\"responseType\":\"JSON\"},\"dynamicTemplate\":{\"timeFieldMapping\":{\"startDate\":\"%Y-%m-%d\"},\"responseTemplate\":{\"id\":\"storyId\",\"created_at\":\"createdAt\",\"story_type\":\"storyType\",\"name\":\"storyName\",\"current_state\":\"currentStoryState\"},\"relationMetadata\":{\"labels\":[\"LATEST\"],\"relation\":{\"properties\":[\"iterationNumber\",\"projectId\",\"storyId\",\"backLog\",\"cycleTime\",\"rejectionRate\"],\"name\":\"ITERATION_HAS_ISSUES\",\"source\":{\"constraints\":[\"projectId\",\"storyId\"]},\"destination\":{\"constraints\":[\"iterationNumber\"]}}},\"storyMetadata\":{\"labels\":[\"STORY\"],\"dataUpdateSupported\":true,\"uniqueKey\":[\"projectId\",\"storyId\"]}},\"agentId\":\"pivotal_agent\",\"auth\":\"base64\",\"runSchedule\":30,\"toolCategory\":\"ALM\",\"enableValueArray\":false,\"enableDataValidation\":true,\"useResponseTemplate\":true,\"userid\":\"dsfd\",\"passwd\":\"fedfvdv\",\"token\":\"vdvdv\",\"baseEndPoint\":\"https://www.pivotaltracker.com\",\"startFrom\":\"2015-11-29 12:17:45\",\"toolsTimeZone\":\"Asia/Kolkata\",\"timeStampField\":\"createdAt\",\"timeStampFormat\":\"%Y-%m-%dT%H:%M:%SZ\",\"isEpochTimeFormat\":false,\"isDebugAllowed\":true,\"loggingSetting\":{\"logLevel\":\"WARN\",\"maxBytes\":5000000,\"backupCount\":1000},\"osversion\":\"windows\",\"agentVersion\":\"v5.2\",\"toolName\":\"PIVOTALTRACKER\"}";
		ApplicationConfigProvider.getInstance().getAgentDetails().setOnlineRegistration(false);
		ApplicationConfigProvider.getInstance().getAgentDetails().setUnzipPath("C://Agents//unzip");
		ApplicationConfigProvider.getInstance().getAgentDetails().setOfflineAgentPath("C://Agents//offline");
		AgentManagementServiceImpl impl = new AgentManagementServiceImpl();
		String configJson = impl.getToolRawConfigFile(version, tool);
		
		Assert.assertEquals(configJson, expectedOutcome);
		Gson gson = new Gson();
		JsonElement jsonElement = gson.fromJson(configJson.trim(), JsonElement.class);
		JsonObject json = jsonElement.getAsJsonObject();
		Assert.assertEquals(json.get("toolCategory"), "ALM");
		
	}
	
	@Test (expectedExceptions = InsightsCustomException.class)
	public void getAgentDetails() throws InsightsCustomException {
		
		AgentConfig agentConfig = new AgentConfig();
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		agentConfig = agentConfigDAL.getAgentConfigurations("JENKINS_123");
		AgentConfigTO agentConfigDetails = new AgentConfigTO();
		agentConfigDetails = agentManagementServiceImpl.getAgentDetails("JENKINS_123");
		Assert.assertNotNull(agentConfigDetails);
		
	}
	
	@Test (expectedExceptions = InsightsCustomException.class)
	public void getAgentDetailsForException() throws InsightsCustomException {
		
		AgentConfig agentConfig = new AgentConfig();
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		AgentConfigTO agentConfigDetails = agentManagementServiceImpl.getAgentDetails("JENKINS");	
	}
	
	@Test(expectedExceptions = NullPointerException.class)
	public void tesUpdateAgent() {
		
		String expectedOutcome = "SUCCESS";
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		Gson gson = new Gson();
		JsonElement jsonElement = gson.fromJson(agentDummyData.configDetails.trim(), JsonElement.class);
		JsonObject json = jsonElement.getAsJsonObject();
		boolean actual = agentConfigDAL.saveAgentConfigFromUI(agentDummyData.agentId, 
						json.get("toolCategory").getAsString(), agentDummyData.toolName, json,agentDummyData.agentVersion,
						agentDummyData.osversion, agentDummyData.updateDate);
		
		Assert.assertEquals(actual, expectedOutcome);
		
	}
	
	@Test(expectedExceptions = InsightsCustomException.class)
	public void testUpdateAgent() throws InsightsCustomException {
		
		ApplicationConfigProvider.getInstance().getAgentDetails().setUnzipPath("C://Agents//unzip");
		ApplicationConfigProvider.getInstance().getAgentDetails().setOnlineRegistration(true);;
		agentManagementServiceImpl.updateAgent("JENKINS_123", configDetails, toolName, agentVersion, osversion);
	}
	
	@Test(expectedExceptions = InsightsCustomException.class)
	public void testUninstallAgent() throws InsightsCustomException{
		
		String expectedOutCome = "SUCCESS"; 
		String response = agentManagementServiceImpl.uninstallAgent("JIRA_1234567890", toolName, osversion);
		Assert.assertEquals(expectedOutCome, response);
		
	}
	@Test(expectedExceptions = InsightsCustomException.class)
	public void testUninstallAgentForException() throws InsightsCustomException{
		String expectedOutCome =  "No entity found for query";
		String response = agentManagementServiceImpl.uninstallAgent("12345fghj", toolName, osversion);
		Assert.assertEquals(expectedOutCome, response);
	}
}
