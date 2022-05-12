/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformservice.test.milestone;

import com.cognizant.devops.platformdal.outcome.InsightsTools;
import com.cognizant.devops.platformdal.outcome.OutComeConfigDAL;

public class MilestoneOutcomeTestData {
	
	OutComeConfigDAL outComeConfigDAL = new OutComeConfigDAL();
	
	String toolName = "NEWRELIC";
	
	String outcomeNameString = "Threads_Count";
	String saveOutcomeJson = "{\"outcomeName\":\""+outcomeNameString+"\",\"outcomeType\":\"Tech\",\"toolName\":\"toolNameeee1\",\"newRelicAppId\":\"\",\"splunkIndex\":\"\",\"appDynamicsAppName\":\"\",\"appDynamicsMetricPath\":\"\",\"metricName\":\"\",\"metricKey\":\"\",\"logKey\":\"\",\"isActive\":true,\"metricUrl\":\"https://abc.com/v2/applications/88908/metrics.json\",\"parameters\":[{\"key\":\"Names[]\",\"value\":\"Threads/Time/CPU/New Relic Harvest Service/UserTime\"}],\"toolConfigJson\":{\"newRelicAppId\":\"\",\"metricName\":\"\"}}";
	String editOutcomeJson = "{\"id\":\"iiddee\",\"outcomeName\":\""+outcomeNameString+"\",\"outcomeType\":\"Techtype\",\"toolName\":\"NEWRELIC\",\"newRelicAppId\":\"\",\"splunkIndex\":\"\",\"metricName\":\"\",\"metricKey\":\"\",\"logKey\":\"\",\"isActive\":\"true\",\"toolId\":\"1\",\"category\":\"APPMONITORING\",\"toolQueue\":\"NEWRELIC_MILESTONE_EXECUTION\",\"toolStatus\":\"true\",\"createdDate\":\"1631026474842\",\"metricUrl\":\"https://abc.com/v2/applications/8890812/metrics.json\",\"parameters\":[{\"key\":\"Names[]\",\"value\":\"Threads/Time/CPU/New Relic Harvest Service/UserTime\"}],\"toolConfigJson\":{\"newRelicAppId\":\"\",\"metricName\":\"\"}}";
		
			
	String deleteOutcomeJson = "{\"id\":iiddee}";
	String statusUpdate = "{\"id\":iiddee,\"isActive\":activee}";
	
	String milestoneNameString = "Mile6";
	String saveMilestoneJson = "{\"mileStoneName\":\""+milestoneNameString+"\",\"milestoneReleaseID\":\"Version_1\",\"startDate\":\"2021-09-01T00:00:00Z\",\"endDate\":\"2021-09-22T00:00:00Z\",\"outcomeList\":[\"Outcommmme\"]}";
	String statusUpdateMilstone = "{\"id\":iiddee}";
	
	void prepareRequestData(){
		InsightsTools newtool = new InsightsTools();
		newtool.setCategory("APPMONITORING");
		newtool.setToolName(toolName);
		newtool.setToolConfigJson("{}");
		newtool.setIsActive(Boolean.TRUE);
		newtool.setAgentCommunicationQueue("NEWRELIC_MILESTONE_EXECUTION");
		outComeConfigDAL.saveInsightsTools(newtool);
	}
}
