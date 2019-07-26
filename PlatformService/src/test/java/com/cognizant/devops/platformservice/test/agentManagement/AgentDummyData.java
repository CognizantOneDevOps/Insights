package com.cognizant.devops.platformservice.test.agentManagement;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

public class AgentDummyData {

	String toolName = " ";
	String agentVersion = "AGENT_VERSION";
	String osversion = "OS_VERSION";
	String configDetails = "{\"Id\": \"workflow\"}";
	String trackingDetails = "TRACKING_DETAILS";
	
	Date updateDate = Timestamp.valueOf(LocalDateTime.now());
	
	String agentId = "agentId";
	String toolCategory = " ";
	
}