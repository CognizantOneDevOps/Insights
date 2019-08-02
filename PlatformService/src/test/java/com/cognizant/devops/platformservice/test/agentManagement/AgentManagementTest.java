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
	
	/*Method to get the available list of agents in the system. */
	@Test(priority = 1)
	public void testGetSystemAvailableAgentList() throws InsightsCustomException {
		System.out.println("1.System available list");
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
	
	@Test(priority = 2)
	public void testGetSystemAvailableAgentListForOfflineRegistration() throws InsightsCustomException {
		System.out.println("1.1System available list");
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
	
	@Test(priority = 3)
	public void testGetToolRawConfigFile() throws InsightsCustomException {
		System.out.println("2. toolRaw Config");
			String version ="v5.2";
			String tool = "git";
			ApplicationConfigProvider.getInstance().getAgentDetails().setOnlineRegistration(true);
			//ApplicationConfigProvider.getInstance().getAgentDetails().setUnzipPath("C://Users//668059//Documents//Agents//unzip");
			//ApplicationConfigProvider.getInstance().getAgentDetails().setOfflineAgentPath("C://Users//668059//Documents//Agents//offline");
			AgentManagementServiceImpl impl = new AgentManagementServiceImpl();
			String configJson = impl.getToolRawConfigFile(version, tool);
			
			
			Gson gson = new Gson();
			JsonElement jsonElement = gson.fromJson(configJson.trim(), JsonElement.class);
			JsonObject json = jsonElement.getAsJsonObject();
			Assert.assertNotNull(json);
			Assert.assertEquals(json.get("toolCategory").getAsString(), "SCM");
	}
	
	@Test(priority = 4)
	public void testGetToolRawConfigFileForOfflineRegistration() throws InsightsCustomException {
		System.out.println("2.1 toolRaw Config");
			String version ="v5.2";
			String tool = "pivotalTracker";
			//String expectedOutcome = "{\"mqConfig\":{\"user\":\"iSight\",\"password\":\"iSight\",\"host\":\"127.0.0.1\",\"exchange\":\"iSight\",\"agentControlXchg\":\"iAgent\"},\"subscribe\":{\"config\":\"ALM.PIVOTALTRACKER.config\",\"agentCtrlQueue\":\"pivotal_agent\"},\"publish\":{\"data\":\"ALM.PIVOTALTRACKER.DATA\",\"health\":\"ALM.PIVOTALTRACKER.HEALTH\"},\"communication\":{\"type\":\"REST\",\"sslVerify\":false,\"responseType\":\"JSON\"},\"dynamicTemplate\":{\"timeFieldMapping\":{\"startDate\":\"%Y-%m-%d\"},\"responseTemplate\":{\"id\":\"storyId\",\"created_at\":\"createdAt\",\"story_type\":\"storyType\",\"name\":\"storyName\",\"current_state\":\"currentStoryState\"},\"relationMetadata\":{\"labels\":[\"LATEST\"],\"relation\":{\"properties\":[\"iterationNumber\",\"projectId\",\"storyId\",\"backLog\",\"cycleTime\",\"rejectionRate\"],\"name\":\"ITERATION_HAS_ISSUES\",\"source\":{\"constraints\":[\"projectId\",\"storyId\"]},\"destination\":{\"constraints\":[\"iterationNumber\"]}}},\"storyMetadata\":{\"labels\":[\"STORY\"],\"dataUpdateSupported\":true,\"uniqueKey\":[\"projectId\",\"storyId\"]}},\"agentId\":\"pivotal_agent\",\"auth\":\"base64\",\"runSchedule\":30,\"toolCategory\":\"ALM\",\"enableValueArray\":false,\"enableDataValidation\":true,\"useResponseTemplate\":true,\"userid\":\"dsfd\",\"passwd\":\"fedfvdv\",\"token\":\"vdvdv\",\"baseEndPoint\":\"https://www.pivotaltracker.com\",\"startFrom\":\"2015-11-29 12:17:45\",\"toolsTimeZone\":\"Asia/Kolkata\",\"timeStampField\":\"createdAt\",\"timeStampFormat\":\"%Y-%m-%dT%H:%M:%SZ\",\"isEpochTimeFormat\":false,\"isDebugAllowed\":true,\"loggingSetting\":{\"logLevel\":\"WARN\",\"maxBytes\":5000000,\"backupCount\":1000},\"osversion\":\"windows\",\"agentVersion\":\"v5.2\",\"toolName\":\"PIVOTALTRACKER\"}";
			ApplicationConfigProvider.getInstance().getAgentDetails().setOnlineRegistration(false);
			//ApplicationConfigProvider.getInstance().getAgentDetails().setUnzipPath("C://Users//668059//Documents//Agents//unzip");
			//ApplicationConfigProvider.getInstance().getAgentDetails().setOfflineAgentPath("C://Users//668059//Documents//Agents//offline");
			AgentManagementServiceImpl impl = new AgentManagementServiceImpl();
			String configJson = impl.getToolRawConfigFile(version, tool);
			
			//Assert.assertEquals(configJson, expectedOutcome);
			Gson gson = new Gson();
			JsonElement jsonElement = gson.fromJson(configJson.trim(), JsonElement.class);
			JsonObject json = jsonElement.getAsJsonObject();
			Assert.assertNotNull(json);
			Assert.assertEquals(json.get("toolCategory").getAsString(), "ALM");
			
	}
	
	@Test(priority = 5) 
	public void testRegisterAgent() throws InsightsCustomException {
		System.out.println("3.Register Agent");
			JsonObject json = getProperties();
			//ApplicationConfigProvider.getInstance().getAgentDetails().setUnzipPath("C://Users//668059//Documents//Agents//unzip");
			AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
			String expectedOutcome = "SUCCESS";
			
			String response = agentServiceImpl.registerAgent(agentDummyData.toolName, 
								agentDummyData.agentVersion, agentDummyData.osversion, 
								agentDummyData.configDetails, agentDummyData.trackingDetails);
			
			/*String response1 = agentServiceImpl.registerAgent("@d#", 
					agentDummyData.agentVersion, agentDummyData.osversion, 
					agentDummyData.configDetails, agentDummyData.trackingDetails);*/
			
			Assert.assertEquals(expectedOutcome, response);
			
	}
	
	@Test(priority = 6) 
	public void testRegisterAgentInDatabase() throws InsightsCustomException {
		System.out.println("3.1 Register Agent");
		JsonObject json = getProperties();
		//ApplicationConfigProvider.getInstance().getAgentDetails().setUnzipPath("C://Users//668059//Documents//Agents//unzip");
		AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
		
		List<AgentConfigTO>  registeredAgents = agentServiceImpl.getRegisteredAgents();
		for (AgentConfigTO agentConfig : registeredAgents) {
			Assert.assertTrue(agentConfig.getToolName().equals("git"));
		}
	}
	
	/*@Test
	public void testRegisterAgentWithEmptyTrackingDetails() throws InsightsCustomException {
		System.out.println("3.2 Register Agent");
		String expectedOutcome = "SUCCESS";
		String response = agentManagementServiceImpl.registerAgent(agentDummyData.toolName, 
							agentDummyData.agentVersion, agentDummyData.osversion, 
							agentDummyData.configDetails, null);
		
		Assert.assertEquals(expectedOutcome, response);
		
	}*/
	
	@Test(priority = 7)
	public void testGetRegisteredAgents() throws InsightsCustomException {
		System.out.println("4. Get Registered Agent");
		Assert.assertFalse(agentManagementServiceImpl.getRegisteredAgents().isEmpty());		
	}
	
	
	
	/*@Test(expectedExceptions = NullPointerException.class)
	public void testSaveAgentConfigFromUI() throws InsightsCustomException {
		System.out.println("5. Save from UI");
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
	}*/
	 
	@Test(priority = 8)
	public void testStartStopAgentForStartAction() throws InsightsCustomException {
		System.out.println("6.Start ");
		String action = "START";
		AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
		String expectedOutput = "SUCCESS";
		String response = agentServiceImpl.startStopAgent(agentDummyData.agentId, agentDummyData.toolName,
							agentDummyData.osversion, action);
		Assert.assertNotNull(response);
		Assert.assertEquals(response, expectedOutput);
	}
	
	@Test(priority = 9)
	public void testStartStopAgentForStopAction() throws InsightsCustomException {
		System.out.println("7.Stop ");
		String action = "STOP";
		AgentManagementServiceImpl agentServiceImpl = new AgentManagementServiceImpl();
		String expectedOutput = "SUCCESS";
		String response = agentManagementServiceImpl.startStopAgent(agentDummyData.agentId, agentDummyData.toolName, 
																					agentDummyData.osversion, action);
		Assert.assertNotNull(response);
		Assert.assertEquals(response, expectedOutput);
	}
	
	@Test(priority = 10)
	public void testStartStopAgentForNoAction() throws InsightsCustomException {
		System.out.println("8. No Action ");
		String action = "REGISTER";
		String osversion = "WINDOWS";
		String expectedOutput = "SUCCESS";
		String response = agentManagementServiceImpl.startStopAgent(agentDummyData.agentId, agentDummyData.toolName,
																									osversion, action);
		Assert.assertNotNull(response);
		Assert.assertEquals(response, expectedOutput);
	}
	

	@Test(priority = 11, expectedExceptions = InsightsCustomException.class)
	public void testStartStopAgentForException() throws InsightsCustomException {
		System.out.println("9. Exception ");
		String action = "REGISTER";
		String osversion = "WINDOWS";
		String expectedOutput = "SUCCESS";
		String response = agentManagementServiceImpl.startStopAgent("123456ASC", agentDummyData.toolName, osversion, action);
		Assert.assertNotNull(response);
		Assert.assertEquals(response, expectedOutput);
	}
	
	
	private String getAgentkey(String toolName) {
		return toolName + "_" + Instant.now().toEpochMilli();
	}
	
	/*@Test
	public void testDeleteAgentConfigurations() {
		System.out.println("10. delete");
		String agentKey = "GIT_1234567890";
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		List<AgentConfig> result = agentConfigDAL.deleteAgentConfigurations(agentKey);
		Assert.assertTrue(result.size() > 0);
	}*/
	
	
	
	@Test (priority = 12)
	public void getAgentDetails() throws InsightsCustomException {
		System.out.println("11. Get Agent details");
		AgentConfig agentConfig = new AgentConfig();
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		agentConfig = agentConfigDAL.getAgentConfigurations(agentDummyData.agentId);
		AgentConfigTO agentConfigDetails = new AgentConfigTO();
		agentConfigDetails = agentManagementServiceImpl.getAgentDetails(agentDummyData.agentId);
		Assert.assertNotNull(agentConfigDetails.getAgentId());
		Assert.assertEquals(agentConfigDetails.getToolCategory(), "SCM");
		
	}
	
	@Test (priority = 13)
	public void getAgentDetailsForException() throws InsightsCustomException {
		System.out.println("11.1 Get Agent details");
		AgentConfig agentConfig = new AgentConfig();
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		AgentConfigTO agentConfigDetails = agentManagementServiceImpl.getAgentDetails(agentDummyData.agentId);
	}
	
	@Test(priority = 14)
	public void testUpdateAgent() throws InsightsCustomException {
		System.out.println("12. UPdate");
		//ApplicationConfigProvider.getInstance().getAgentDetails().setUnzipPath("C://Users//668059//Documents//Agents//unzip");
		ApplicationConfigProvider.getInstance().getAgentDetails().setOnlineRegistration(true);
		AgentManagementServiceImpl agentManagementServiceImpl = new AgentManagementServiceImpl();
		agentManagementServiceImpl.updateAgent(agentDummyData.agentId, configDetails, agentDummyData.toolName, 
																agentDummyData.agentVersion, agentDummyData.osversion);
	}
	
	@Test(priority = 15)
	public void testUninstallAgent() throws InsightsCustomException{
		System.out.println("13. Uninstall");
		String expectedOutCome = "SUCCESS"; 
		String response = agentManagementServiceImpl.uninstallAgent(agentDummyData.agentId, agentDummyData.toolName, 
																							agentDummyData.osversion);
		Assert.assertEquals(expectedOutCome, response);
		
	}
	@Test(priority = 16, expectedExceptions = InsightsCustomException.class)
	public void testUninstallAgentForException() throws InsightsCustomException{
		System.out.println("13.1 Uninstall");
		String expectedOutCome =  "No entity found for query";
		String response = agentManagementServiceImpl.uninstallAgent("12345fghj", agentDummyData.toolName, 
																							agentDummyData.osversion);
		Assert.assertEquals(expectedOutCome, response);
	}
}
