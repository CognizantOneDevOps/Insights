/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformservice.milestone.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.cognizant.devops.platformcommons.constants.MilestoneConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.enums.MilestoneEnum;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.milestone.InsightsMileStoneOutcomeConfig;
import com.cognizant.devops.platformdal.milestone.MileStoneConfig;
import com.cognizant.devops.platformdal.milestone.MileStoneConfigDAL;
import com.cognizant.devops.platformdal.outcome.InsightsOutcomeTools;
import com.cognizant.devops.platformdal.outcome.OutComeConfigDAL;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@Service
public class MileStoneServiceImpl implements MileStoneService{

	private static final Logger log = LogManager.getLogger(MileStoneServiceImpl.class);

	MileStoneConfigDAL mileStoneConfigDAL = new MileStoneConfigDAL();
	OutComeConfigDAL outComeConfigDAL = new OutComeConfigDAL();
	
	@Override
	public int saveMileStoneConfig(JsonObject configJson) throws InsightsCustomException {
		String milestoneName = configJson.get(MilestoneConstants.MILESTONENAME).getAsString();
        MileStoneConfig milestoneConfig = mileStoneConfigDAL.getMileStoneConfigByName(milestoneName);
        if (milestoneConfig != null) {
			throw new InsightsCustomException("Milestone with given name already exists.");
		}
		String startDate = configJson.get(MilestoneConstants.STARTDATE).getAsString();
		long epochStartDate = InsightsUtils.getEpochTime(startDate) / 1000;
		String endDate = configJson.get(MilestoneConstants.ENDDATE).getAsString();
		long epochEndDate = InsightsUtils.getEpochTime(endDate) / 1000;
		
		if (epochStartDate > epochEndDate) {
			throw new InsightsCustomException("Start Date cannot be greater than End Date");
		}
        String milestoneReleaseID = configJson.get(MilestoneConstants.MILESTONE_RELEASEID).getAsString();
		MileStoneConfig mileStoneConfig = new MileStoneConfig();
		mileStoneConfig.setMileStoneName(milestoneName);
		mileStoneConfig.setStatus(MilestoneEnum.MilestoneStatus.NOT_STARTED.name());
		mileStoneConfig.setStartDate(epochStartDate);
		mileStoneConfig.setEndDate(epochEndDate);
		mileStoneConfig.setCreatedDate(InsightsUtils.getCurrentTimeInEpochMilliSeconds());
		mileStoneConfig.setMilestoneReleaseID(milestoneReleaseID);
		Set<InsightsMileStoneOutcomeConfig> outcomeList = new HashSet<>();
		JsonArray outcomeListFromUI = configJson.get("outcomeList").getAsJsonArray();
		for(JsonElement outcomeName: outcomeListFromUI) {
			InsightsOutcomeTools insightsOutcomeTools = outComeConfigDAL.getOutComeConfigByName(outcomeName.getAsString());
			InsightsMileStoneOutcomeConfig insightsMileStoneOutcomeConfig = new InsightsMileStoneOutcomeConfig();
			insightsMileStoneOutcomeConfig.setStatus(MilestoneEnum.OutcomeStatus.NOT_STARTED.name());
			insightsMileStoneOutcomeConfig.setMileStoneConfig(mileStoneConfig);
			insightsMileStoneOutcomeConfig.setInsightsOutcomeTools(insightsOutcomeTools);
			insightsMileStoneOutcomeConfig.setStatusMessage(MilestoneEnum.OutcomeStatus.NOT_STARTED.getValue());
			insightsMileStoneOutcomeConfig.setLastUpdatedDate(InsightsUtils.getCurrentTimeInEpochMilliSeconds());
			outcomeList.add(insightsMileStoneOutcomeConfig);
		}
		mileStoneConfig.setListOfOutcomes(outcomeList);
		return mileStoneConfigDAL.saveMileStoneConfig(mileStoneConfig);
	}
	
	@Override
	public JsonArray getMileStoneConfigs() throws InsightsCustomException {
			try {
				List<MileStoneConfig> configs = mileStoneConfigDAL.getAllMileStoneConfig();
				JsonArray mileStoneArray = new JsonArray();
				for (MileStoneConfig mile : configs) {
					JsonObject mileJson = new JsonObject();
					mileJson.addProperty("id", mile.getId());
					mileJson.addProperty(MilestoneConstants.MILESTONENAME, mile.getMileStoneName());
					mileJson.addProperty(MilestoneConstants.STARTDATE, mile.getStartDate());
					mileJson.addProperty(MilestoneConstants.ENDDATE, mile.getEndDate());
					mileJson.addProperty(MilestoneConstants.MILESTONE_RELEASEID, mile.getMilestoneReleaseID());
					mileJson.addProperty("status", mile.getStatus());
					mileJson.addProperty("workflowId", ""); //mile.getWorkflowConfig().getWorkflowId()
					List<JsonObject> config = new ArrayList<>();
					for(InsightsMileStoneOutcomeConfig milestoneConfig: mile.getListOfOutcomes()) {
						JsonObject jsonobject = new JsonObject();
						jsonobject.addProperty("id",milestoneConfig.getId());
						jsonobject.addProperty("mileStoneId",milestoneConfig.getMileStoneConfig().getId());
						jsonobject.addProperty("outcomeId",milestoneConfig.getInsightsOutcomeTools().getId());
						jsonobject.addProperty("outcomeName", milestoneConfig.getInsightsOutcomeTools().getOutcomeName());
						jsonobject.addProperty("queueName", milestoneConfig.getInsightsOutcomeTools().getInsightsTools().getAgentCommunicationQueue());
						jsonobject.addProperty("toolName", milestoneConfig.getInsightsOutcomeTools().getInsightsTools().getToolName());
						jsonobject.addProperty("status",milestoneConfig.getStatus());
						jsonobject.addProperty("statusMessage",milestoneConfig.getStatusMessage());
						jsonobject.addProperty("lastUpdatedDate",milestoneConfig.getLastUpdatedDate());
						config.add(jsonobject);
					}
					mileJson.add("listOfOutcomes", new Gson().toJsonTree(config));
					mileStoneArray.add(mileJson);
				}
				return mileStoneArray;
			} catch (Exception e) {
				log.error("Error getting milestone list ..", e);
				throw new InsightsCustomException(e.getMessage());
			}
	}

	@Override
	public void updateMileStoneConfig(JsonObject configJson) throws InsightsCustomException {
		try {
			MileStoneConfig existingConfig = mileStoneConfigDAL.getMileStoneConfigById(configJson.get("id").getAsInt());
			if(existingConfig.getStatus().equals("NOT_STARTED") && existingConfig.getStartDate()>InsightsUtils.getCurrentTimeInSeconds()) {
				MileStoneConfig mileStoneConfig = new MileStoneConfig();
				mileStoneConfig.setId(configJson.get("id").getAsInt());
				mileStoneConfig.setMileStoneName(configJson.get(MilestoneConstants.MILESTONENAME).getAsString());
				mileStoneConfig.setStatus("NOT_STARTED");
				String startDate = configJson.get(MilestoneConstants.STARTDATE).getAsString();
				long epochStartDate = InsightsUtils.getEpochTime(startDate) / 1000;
				String endDate = configJson.get(MilestoneConstants.ENDDATE).getAsString();
				long epochEndDate = InsightsUtils.getEpochTime(endDate) / 1000;
				if (epochStartDate > epochEndDate) {
					throw new InsightsCustomException("Start Date cannot be greater than End Date");
				}
				String milestoneReleaseID = configJson.get(MilestoneConstants.MILESTONE_RELEASEID).getAsString();
				mileStoneConfig.setStartDate(epochStartDate);
				mileStoneConfig.setEndDate(epochEndDate);
				mileStoneConfig.setMilestoneReleaseID(milestoneReleaseID);
				mileStoneConfig.setUpdatedDate(InsightsUtils.getCurrentTimeInEpochMilliSeconds());
				Set<InsightsMileStoneOutcomeConfig> outcomeList = new HashSet<>();
				JsonArray existingOutcomeList = configJson.get("existingOutcomeList").getAsJsonArray();
				JsonArray outcomeListFromUI = configJson.get("outcomeList").getAsJsonArray();
				existingOutcomeList.forEach(x-> mileStoneConfigDAL.deleteOutcome(x.getAsInt()));
				
				for(JsonElement outcome: outcomeListFromUI) {
					InsightsOutcomeTools insightsOutcomeTools = outComeConfigDAL.getOutComeConfigByName(outcome.getAsString());
					InsightsMileStoneOutcomeConfig insightsMileStoneOutcomeConfig = new InsightsMileStoneOutcomeConfig();
					insightsMileStoneOutcomeConfig.setLastUpdatedDate(InsightsUtils.getCurrentTimeInEpochMilliSeconds());
					insightsMileStoneOutcomeConfig.setMileStoneConfig(mileStoneConfig);
					insightsMileStoneOutcomeConfig.setInsightsOutcomeTools(insightsOutcomeTools);
					insightsMileStoneOutcomeConfig.setStatus(MilestoneEnum.OutcomeStatus.NOT_STARTED.name());
					insightsMileStoneOutcomeConfig.setStatusMessage(MilestoneEnum.OutcomeStatus.NOT_STARTED.getValue());
					outcomeList.add(insightsMileStoneOutcomeConfig);
				}
				mileStoneConfig.setListOfOutcomes(outcomeList);
				mileStoneConfigDAL.updateMileStoneConfig(mileStoneConfig);
			}
			else {
				throw new InsightsCustomException("Only Milestones that have not started can be edited");
			}
		} catch(Exception e) {
			log.error("Error while updating milestone...", e);
			throw new InsightsCustomException(e.getMessage());
		}
		
	}

	@Override
	public String deleteMileStoneDetails(int milestoneId) throws InsightsCustomException {
		try {
			MileStoneConfig mileStoneConfig = mileStoneConfigDAL.getMileStoneConfigById(milestoneId);
			if(mileStoneConfig == null) {
				throw new InsightsCustomException("Milestone Detail not found ");
			}else {
				boolean updateOutcomeStatusFlag = mileStoneConfig.getListOfOutcomes().stream().allMatch(outcome-> outcome.getStatus().equalsIgnoreCase(MilestoneEnum.OutcomeStatus.NOT_STARTED.name()));
				if(mileStoneConfig.getStatus().equalsIgnoreCase(MilestoneEnum.MilestoneStatus.NOT_STARTED.name()) && updateOutcomeStatusFlag) {
					mileStoneConfigDAL.deleteMileStoneConfig(milestoneId);
				}else {
					throw new InsightsCustomException("Milestone already in progress, you cannot delete this Milestone. ");
				}
			}
			return PlatformServiceConstants.SUCCESS;
		} catch(Exception e) {
			log.error("Error while deleting milestone...", e);
			throw new InsightsCustomException(e.getMessage());
		}
	}

	@Override
	public JsonArray fetchOutcomeConfig() throws InsightsCustomException {
		try {
			List<InsightsOutcomeTools> configs = mileStoneConfigDAL.getOutcomeConfigTools();
			Gson gson = new Gson();
			JsonElement element = gson.toJsonTree(configs, new TypeToken<List<InsightsOutcomeTools>>() {}.getType());
			if (! element.isJsonArray() ) {
				    throw new InsightsCustomException("Unable to parse Json");
			}
			return element.getAsJsonArray();
		} catch (Exception e) {
			log.error("Error getting milestone list ..", e);
			throw new InsightsCustomException(e.getMessage());
		}
	}

	@Override
	public Boolean restartMileStoneConfig(JsonObject configJson) throws InsightsCustomException {
		try {
			int milestoneId = configJson.get("id").getAsInt();
			MileStoneConfig milestoneConfig = mileStoneConfigDAL.getMileStoneConfigById(milestoneId);
			milestoneConfig.getListOfOutcomes().forEach(outcome -> {
				if(outcome.getStatus().equalsIgnoreCase(MilestoneEnum.OutcomeStatus.ERROR.name())) {
					mileStoneConfigDAL.updateMilestoneOutcomeStatus(milestoneId, outcome.getInsightsOutcomeTools().getId(), MilestoneEnum.OutcomeStatus.RESTART.name(), MilestoneEnum.OutcomeStatus.RESTART.getValue());
				}
			});
			mileStoneConfigDAL.updateMilestoneStatus(milestoneId, MilestoneEnum.MilestoneStatus.RESTART.name());
			return Boolean.TRUE;
			
		} catch (Exception e) {
			log.error("Error while restarting milestone..", e);
			throw new InsightsCustomException(e.getMessage());
		}
	}

}
