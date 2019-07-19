package com.cognizant.devops.platformservice.test.agentManagement;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

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
public class AgentManagementTest{
	
	private static final String UNINSTALL_AGENT = AGENTACTION.UNINSTALL.name();
	
	@Test(expectedExceptions = InsightsCustomException.class)
	public void testRegisterAgent() throws InsightsCustomException{
		
		String toolName = "TOOL_NAME";
		String agentVersion = "AGENT_VERSION";
		String osversion = "OS_VERSION";
		String configDetails = "CONFIG_DETAILS";
		String trackingDetails = "TRACKING_DETAILS";
		
		String expectedOutcome = "SUCCESS";
		
		AgentManagementServiceImpl agentManagementServiceImpl = new AgentManagementServiceImpl();
		String response = agentManagementServiceImpl.registerAgent(toolName, agentVersion, osversion, configDetails, 
																	trackingDetails);
		
		Assert.assertNotNull(response);
		Assert.assertEquals(expectedOutcome, response);
		
	}
 	
	/*Method to get the available list of agents in the system. */
	@Test(priority = 1)
	public void testGetSystemAvailableAgentList() throws InsightsCustomException {
		AgentManagementServiceImpl agentManagementServiceImpl = new AgentManagementServiceImpl();
		Map<String, ArrayList<String>> availableAgents = agentManagementServiceImpl.getSystemAvailableAgentList();
		System.out.println(availableAgents);
		 
		Assert.assertNotNull(availableAgents);
		Assert.assertTrue(availableAgents.size() > 0);
		
	}
	
	@Test(priority = 2)
	public void testGetRegisteredAgents() throws InsightsCustomException {
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		List<AgentConfig> agentConfigList = agentConfigDAL.getAllDataAgentConfigurations();
		List<AgentConfigTO> agentList = new ArrayList<>(agentConfigList.size());

		System.out.println("Get Registered  agents" + agentList.size());
		Assert.assertNotNull(agentList); 
		
	}
	
	@Test(priority = 3)
	public void testGetRegisteredAgentsWithAgentPresent() throws InsightsCustomException {
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		List<AgentConfig> agentConfigList = agentConfigDAL.getAllAgentConfigurations();
		List<AgentConfigTO> agentList = new ArrayList<>(agentConfigList.size());

		System.out.println("Get Registered  agents" + agentList.size());
		Assert.assertNotNull(agentList); 
		
	}
	
	@Test (priority = 4)
	public void testGetRegisteredAgentsWithAgentCall() throws InsightsCustomException {
		AgentManagementServiceImpl agentManagementServiceImpl = new AgentManagementServiceImpl();
		List<AgentConfigTO> agentList = agentManagementServiceImpl.getRegisteredAgents();
		System.out.println("Get Registered  agents" + agentList.size());
		Assert.assertNotNull(agentList); 
		
	}
	
	@Test
	public void testSaveAgentConfigFromUI() throws InsightsCustomException {
		
		Date updateDate = Timestamp.valueOf(LocalDateTime.now());
		
		String agentId = "agentId";
		String toolName = "toolName";
		String osversion = "osversion";
		String agentVersion = "agentVersion";
		String configDetails = "configDetails";
		
		Gson gson = new Gson();
		JsonElement jsonElement = gson.fromJson(configDetails.trim(), JsonElement.class);
		JsonObject json = jsonElement.getAsJsonObject();
		json.addProperty("osversion", osversion);
		json.addProperty("agentVersion", agentVersion);
		json.addProperty("toolName", toolName.toUpperCase());
		
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		boolean response = agentConfigDAL.saveAgentConfigFromUI(agentId, json.get("toolCategory").getAsString(), 
				toolName, json, agentVersion, osversion, updateDate);
		
		Assert.assertTrue(response);
	}
	
	@Test
	public void testStartStopAgentForStartAction() throws InsightsCustomException {
		
		String agentId = "agentId";
		String toolName = "toolName";
		String osversion = "osversion";
		String action = AGENTACTION.START.getValue();
		
		System.out.println("action " + action);
		String expectedOutput = "SUCCESS";
		AgentManagementServiceImpl agentManagementServiceImpl = new AgentManagementServiceImpl();
		String response = agentManagementServiceImpl.startStopAgent(agentId, toolName, osversion, action);
		System.out.println("response " + response);
		Assert.assertNotNull(response);
		
		Assert.assertEquals(response, expectedOutput);
	}
	
	@Test(expectedExceptions = InsightsCustomException.class)
	public void testUninstallAgent() throws InsightsCustomException{
		
		String agentId = "agentId";
		String toolName = "toolName";
		String osversion = "osversion";
		String expectedOutCome = "SUCCESS"; 

		AgentManagementServiceImpl agentManagementServiceImpl = new AgentManagementServiceImpl();
		String response = agentManagementServiceImpl.uninstallAgent(agentId, toolName, osversion);
		//AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		//agentConfigDAL.deleteAgentConfigurations(agentId);
		 
		Assert.assertNull(response);
		Assert.assertEquals(expectedOutCome, response);
		
	}
	
	@Test 
	public void deleteAgentConfigurations() {
		
		String agentKey = "agentKey";
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		List<AgentConfig> result = agentConfigDAL.deleteAgentConfigurations(agentKey);
		
		Assert.assertFalse(result.isEmpty());
	}
}
