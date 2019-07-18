package com.cognizant.devops.platformservice.test.agentManagement;

import java.util.ArrayList;
import java.util.HashMap;
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

@Test
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class AgentManagementTest{
	
	private static final String UNINSTALL_AGENT = AGENTACTION.UNINSTALL.name();
 	
	/*Method to get the available list of agents in the system. */
	@Test
	public void testGetSystemAvailableAgentList() throws InsightsCustomException {
		AgentManagementServiceImpl agentManagementServiceImpl = new AgentManagementServiceImpl();
		Map<String, ArrayList<String>> availableAgents = agentManagementServiceImpl.getSystemAvailableAgentList();
		System.out.println(availableAgents);
		 
		Assert.assertNotNull(availableAgents);
		Assert.assertTrue(availableAgents.size() > 0);
		
	}
	
	@Test
	public void testGetRegisteredAgents() throws InsightsCustomException {
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		List<AgentConfig> agentConfigList = agentConfigDAL.getAllDataAgentConfigurations();
		List<AgentConfigTO> agentList = new ArrayList<>(agentConfigList.size());

		System.out.println("Get Registered  agents" + agentList.size());
		Assert.assertNotNull(agentList); 
		
	}
	
	@Test
	public void testGetRegisteredAgentsWithAgentPresent() throws InsightsCustomException {
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		List<AgentConfig> agentConfigList = agentConfigDAL.getAllAgentConfigurations();
		List<AgentConfigTO> agentList = new ArrayList<>(agentConfigList.size());

		System.out.println("Get Registered  agents" + agentList.size());
		Assert.assertNotNull(agentList); 
		
	}
	
	@Test 
	public void testGetRegisteredAgentsWithAgentCall() throws InsightsCustomException {
		AgentManagementServiceImpl agentManagementServiceImpl = new AgentManagementServiceImpl();
		List<AgentConfigTO> agentList = agentManagementServiceImpl.getRegisteredAgents();
		System.out.println("Get Registered  agents" + agentList.size());
		Assert.assertNotNull(agentList); 
		
	}
	
	@Test
	public void testUninstallAgent() {
		AgentManagementServiceImpl agentManagementServiceImpl = new AgentManagementServiceImpl();
		String agentId = "agentId";
		String toolName = "toolName";
		String osversion = "osversion";
		//String unInstallAgent = agentManagementServiceImpl.
		//preparingUninstallAgent(UNINSTALL_AGENT, agentId, toolName, osversion);
		AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
		agentConfigDAL.deleteAgentConfigurations(agentId);
		 
		Assert.assertNull(agentConfigDAL.getAgentConfigurations(agentId));
		
	}
	
	/*private void preparingUninstallAgent(String action, String agentId, String toolName, String osversion) {
		
		Map<String, Object> headers = new HashMap<>();
		headers.put("osType", osversion);
		headers.put("agentToolName", toolName);
		headers.put("agentId", agentId);
		headers.put("action", action);
		
		//String appConfig = ApplicationConfigProvider.getInstance().getAgentDetails().getAgentPkgQueue();
	}*/
	
}
