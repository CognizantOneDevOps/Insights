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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import com.cognizant.devops.platformcommons.constants.MilestoneConstants;
import com.cognizant.devops.platformcommons.core.enums.MilestoneEnum;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.milestone.InsightsMileStoneOutcomeConfig;
import com.cognizant.devops.platformdal.milestone.MileStoneConfig;
import com.cognizant.devops.platformdal.milestone.MileStoneConfigDAL;
import com.cognizant.devops.platformdal.outcome.InsightsOutcomeTools;
import com.cognizant.devops.platformdal.outcome.InsightsTools;
import com.cognizant.devops.platformdal.outcome.OutComeConfigDAL;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class MilestoneOutcomeTestData  extends AbstractTestNGSpringContextTests {
	List<JsonObject> filterOutcomeList = new ArrayList<>();
	List<JsonObject> filterMilestoneList = new ArrayList<>();
	List<JsonObject> outcomeList = new ArrayList<>();
	List<JsonObject> milestoneList = new ArrayList<>();
	MileStoneConfigDAL mileStoneDAL = new MileStoneConfigDAL();
	String host = null;
	Gson gson = new Gson();
	OutComeConfigDAL outComeConfigDAL = new OutComeConfigDAL();
	int milestoneOutcomeConfigId=0;
	String toolName = "NEWRELIC";
	String outcomeNameString = "Threads_Count";
    String milestoneNameString = "Mile9";
	JsonArray arr = new JsonArray();
    JsonArray outcomeList1 = new JsonArray();
	InsightsTools insightsMilestoneTools = null;
	void prepareRequestData(){
		InsightsTools newtool = new InsightsTools();
		newtool.setCategory("APPMONITORING");
		newtool.setToolName(toolName);
		newtool.setToolConfigJson("{}");
		newtool.setIsActive(Boolean.TRUE);
		newtool.setAgentCommunicationQueue("NEWRELIC_MILESTONE_EXECUTION");
		outComeConfigDAL.saveInsightsTools(newtool);
	}

	void GetInsightsMilestoneTools() {
		this.insightsMilestoneTools = outComeConfigDAL.getOutComeByToolName(toolName);
		if(this.insightsMilestoneTools == null) {
			prepareRequestData();
			this.insightsMilestoneTools = outComeConfigDAL.getOutComeByToolName(toolName);
		}
	}
	
	public JsonObject convertStringIntoJson(String convertregisterkpi) {
		JsonObject objectJson = new JsonObject();
		objectJson = JsonUtils.parseStringAsJsonObject(convertregisterkpi);
		return objectJson;
	}
	
	public void updateMileStoneConfig(JsonObject configJson, String Status) throws InsightsCustomException {
			    MileStoneConfig existingConfig = mileStoneDAL.getMileStoneConfigById(configJson.get("id").getAsInt());
			    MileStoneConfig mileStoneConfig = new MileStoneConfig();
				mileStoneConfig.setId(configJson.get("id").getAsInt());
				mileStoneConfig.setMileStoneName(configJson.get(MilestoneConstants.MILESTONENAME).getAsString());
				mileStoneConfig.setStatus(Status);
				String startDate = configJson.get(MilestoneConstants.STARTDATE).getAsString();
				long epochStartDate = InsightsUtils.getEpochTime(startDate) / 1000;
				String endDate = configJson.get(MilestoneConstants.ENDDATE).getAsString();
				long epochEndDate = InsightsUtils.getEpochTime(endDate) / 1000;
				String milestoneReleaseID = configJson.get(MilestoneConstants.MILESTONE_RELEASEID).getAsString();
				mileStoneConfig.setStartDate(epochStartDate);
				mileStoneConfig.setEndDate(epochEndDate);
				mileStoneConfig.setMilestoneReleaseID(milestoneReleaseID);
				mileStoneConfig.setUpdatedDate(InsightsUtils.getCurrentTimeInEpochMilliSeconds());
				Set<InsightsMileStoneOutcomeConfig> outcomeList = new HashSet<>();
				JsonArray existingOutcomeList = configJson.get("existingOutcomeList").getAsJsonArray();
				JsonArray outcomeListFromUI = configJson.get("outcomeList").getAsJsonArray();
				for(JsonElement outcome: outcomeListFromUI) {
					InsightsOutcomeTools insightsOutcomeTools = outComeConfigDAL.getOutComeConfigByName(outcome.getAsJsonObject().get("outcomeName").getAsString());
					InsightsMileStoneOutcomeConfig insightsMileStoneOutcomeConfig = new InsightsMileStoneOutcomeConfig();
					insightsMileStoneOutcomeConfig.setLastUpdatedDate(InsightsUtils.getCurrentTimeInEpochMilliSeconds());
					insightsMileStoneOutcomeConfig.setMileStoneConfig(mileStoneConfig);
					insightsMileStoneOutcomeConfig.setInsightsOutcomeTools(insightsOutcomeTools);
					insightsMileStoneOutcomeConfig.setStatus(Status);
					insightsMileStoneOutcomeConfig.setStatusMessage(MilestoneEnum.OutcomeStatus.NOT_STARTED.getValue());
					outcomeList.add(insightsMileStoneOutcomeConfig);
				}
				mileStoneConfig.setListOfOutcomes(outcomeList);
				mileStoneDAL.updateMileStoneConfig(mileStoneConfig);
	}
}