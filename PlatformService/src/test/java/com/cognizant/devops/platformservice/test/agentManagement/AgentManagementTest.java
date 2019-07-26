package com.cognizant.devops.platformservice.test.agentManagement;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import com.cognizant.devops.platformservice.agentmanagement.util.AgentManagementUtil;
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
	
	@Test(expectedExceptions = InsightsCustomException.class) 
	public void testRegisterAgent() throws InsightsCustomException {
		
		String expectedOutcome = "SUCCESS";
		String response = agentManagementServiceImpl.registerAgent(agentDummyData.toolName, 
							agentDummyData.agentVersion, agentDummyData.osversion, 
							agentDummyData.configDetails, agentDummyData.trackingDetails);
		
		/*Method method = AgentDummyData.class.getDeclaredMethod("setupAgentInstanceCreation", String.class);
		method.setAccessible(true);
		String output = (String) method.invoke(agentManagementServiceImpl,"someinput");
		*/
		Assert.assertEquals(expectedOutcome, response);
		
	}
	
	@Test(expectedExceptions = InsightsCustomException.class) 
	public void testRegisterAgentWithEmptyTrackingDetails() throws InsightsCustomException {
		
		String expectedOutcome = "SUCCESS";
		String response = agentManagementServiceImpl.registerAgent(agentDummyData.toolName, 
							agentDummyData.agentVersion, agentDummyData.osversion, 
							agentDummyData.configDetails, null);
		
		Assert.assertEquals(expectedOutcome, response);
		
	}
	
	@Test(expectedExceptions = Exception.class) 
	public void testRegisterAgentWithPath() throws InsightsCustomException, IOException {
		
		Path sourceFolderPath = Paths.get(ApplicationConfigProvider.getInstance().getAgentDetails().getUnzipPath(),
				agentDummyData.agentId);
		Path zipPath = Paths.get(ApplicationConfigProvider.getInstance().getAgentDetails().getUnzipPath(),
				agentDummyData.agentId + ".zip");
		Path agentZipPath = null;
		agentZipPath = AgentManagementUtil.getInstance().getAgentZipFolder(sourceFolderPath, zipPath);
		Assert.assertNotNull(agentZipPath);
		
	}
 	
	/*Method to get the available list of agents in the system. */
	@Test(priority = 1)
	public void testGetSystemAvailableAgentList() throws InsightsCustomException {
		Map<String, ArrayList<String>> availableAgents = agentManagementServiceImpl.getSystemAvailableAgentList();
		 
		Assert.assertNotNull(availableAgents);
		Assert.assertTrue(availableAgents.size() > 0);
		
	}
	
	@Test(priority = 2)
	public void testGetRegisteredAgents() throws InsightsCustomException {
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		List<AgentConfig> agentConfigList = agentConfigDAL.getAllDataAgentConfigurations();
		List<AgentConfigTO> agentList = new ArrayList<>(agentConfigList.size());

		Assert.assertNotNull(agentList); 
		Assert.assertEquals(agentManagementServiceImpl.getRegisteredAgents(), agentList);		
	}
	
	@Test(priority = 3)
	public void testGetRegisteredAgentsWithAgentPresent() throws InsightsCustomException {
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		List<AgentConfig> agentConfigList = agentConfigDAL.getAllAgentConfigurations();
		List<AgentConfigTO> agentList = new ArrayList<>(agentConfigList.size());

		Assert.assertNotNull(agentList); 
		Assert.assertEquals(agentManagementServiceImpl.getRegisteredAgents(), agentList);
		
	}
	
	@Test (priority = 4)
	public void testGetRegisteredAgentsWithAgentCall() throws InsightsCustomException {
		List<AgentConfigTO> agentList = agentManagementServiceImpl.getRegisteredAgents();
		
		Assert.assertNotNull(agentList); 
		Assert.assertEquals(agentManagementServiceImpl.getRegisteredAgents(), agentList);
		
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
		
		String action = AGENTACTION.START.getValue();
		
		String expectedOutput = "SUCCESS";
		String response = agentManagementServiceImpl.startStopAgent(agentDummyData.agentId, agentDummyData.toolName,
							agentDummyData.osversion, action);
		Assert.assertNotNull(response);
		Assert.assertEquals(response, expectedOutput);
	}
	
	@Test(expectedExceptions = InsightsCustomException.class)
	public void testStartStopAgentForStopAction() throws InsightsCustomException {
		
		String action = AGENTACTION.STOP.getValue();
		
		String expectedOutput = "SUCCESS";
		String response = agentManagementServiceImpl.startStopAgent(agentId, toolName, osversion, action);
		Assert.assertNotNull(response);
		Assert.assertEquals(response, expectedOutput);
	}
	
	@Test(expectedExceptions = InsightsCustomException.class)
	public void testUninstallAgent() throws InsightsCustomException{
		
		String expectedOutCome = "SUCCESS"; 

		String response = agentManagementServiceImpl.uninstallAgent(agentId, toolName, osversion);
		
		Assert.assertNull(response);
		Assert.assertEquals(expectedOutCome, response);
		
	}
	
	@Test 
	public void testDeleteAgentConfigurations() {
		
		String agentKey = "agentKey";
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		List<AgentConfig> result = agentConfigDAL.deleteAgentConfigurations(agentKey);
		System.out.println("result is " + result);
		//Assert.assertFalse(result.isEmpty());
	}
	
	@Test(expectedExceptions = InsightsCustomException.class)
	public void testGetToolRawConfigFile() throws InsightsCustomException {
		String version ="version";
		String tool = "tool";
		String expectedOutcome = "Tool_CONFIG";
		
		String configJson = agentManagementServiceImpl.getToolRawConfigFile(version, tool);
		
		Assert.assertEquals(configJson, expectedOutcome);
		
	}
	
	@Test (expectedExceptions = Exception.class)
	public void getAgentDetails() {
		
		AgentConfig agentConfig = new AgentConfig();
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		agentConfig = agentConfigDAL.getAgentConfigurations(agentDummyData.agentId);
		//BeanUtils.copyProperties(agentConfigDAL.getAgentConfigurations(agentDummyData.agentId), agentConfig);
		List<AgentConfig> expectedResult = agentConfigDAL.getAgentConfigurations(agentDummyData.toolName, 
											agentDummyData.toolCategory);
		Assert.assertEquals(agentConfig, expectedResult);
		
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
}
