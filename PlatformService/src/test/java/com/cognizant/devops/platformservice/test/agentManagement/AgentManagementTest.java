package com.cognizant.devops.platformservice.test.agentManagement;

import java.util.ArrayList;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.agentmanagement.service.AgentConfigTO;
import com.cognizant.devops.platformservice.agentmanagement.service.AgentManagementServiceImpl;

@Test
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
@WebAppConfiguration
public class AgentManagementTest extends AbstractTestNGSpringContextTests{
	
	static Logger log = LogManager.getLogger(AgentManagementTest.class);
	
	@Autowired
	AgentManagementServiceImpl agentManagementService;
	
	/*Method to get the available list of agents in the system. */
	@Test(priority=1)
	public void testGetSystemAvailableAgentList() throws InsightsCustomException {
		
		Map<String, ArrayList<String>> availableAgents = agentManagementService.getSystemAvailableAgentList();
		System.out.println(availableAgents);
		
		Assert.assertNotNull(availableAgents);
		Assert.assertTrue(availableAgents.size() > 0);
		
	}
	
	/*@Test(priority=2)
	public void testGetAgentDetails() throws InsightsCustomException {
		
		AgentConfigTO agentDetails = agentManagementService.getAgentDetails("daemon-1523257126");
		System.out.println("Agent Details " + agentDetails);
		
		Assert.assertNotNull(agentDetails);
		
	}*/
}
