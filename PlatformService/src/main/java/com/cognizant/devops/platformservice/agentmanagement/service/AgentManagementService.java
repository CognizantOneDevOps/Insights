package com.cognizant.devops.platformservice.agentmanagement.service;

public interface AgentManagementService {

	public String registerAgent(String configDetails);
	public String installAgent(String agentId,String toolName);
	public String startStopAgent(String agentId,String action);
	public String updateAgent(String agentId, String configDetails);
}
