/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
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
package com.cognizant.devops.engines.platformroi.aggregator.test.engine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.CommonsAndDALConstants;
import com.cognizant.devops.platformcommons.constants.MilestoneConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.constants.ReportStatusConstants;
import com.cognizant.devops.platformcommons.core.enums.MilestoneEnum;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
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
import com.google.gson.reflect.TypeToken;

public class MileStoneExecutionAggregatorModuleTestData {
	private static final Logger log = LogManager.getLogger(MileStoneExecutionAggregatorModuleTestData.class.getName());

	OutComeConfigDAL outComeConfigDAL = new OutComeConfigDAL();
	MileStoneConfigDAL mileStoneConfigDAL = new MileStoneConfigDAL();

	public int saveOutcomeConfig(JsonObject configJson) throws InsightsCustomException {
		String outcomeName = configJson.get("outcomeName").getAsString();
		InsightsOutcomeTools outcomeConfig = outComeConfigDAL.getOutComeConfigByName(outcomeName);
		if (outcomeConfig != null) {
			throw new InsightsCustomException("Outcome with given name already exists.");
		}
		InsightsOutcomeTools insightsOutcomeTools = new InsightsOutcomeTools();
		insightsOutcomeTools.setOutcomeName(outcomeName);
		insightsOutcomeTools.setOutcomeType(configJson.get("outcomeType").getAsString());
		if (configJson.has(ReportStatusConstants.TOOL_CONFIG_JSON)) {
			insightsOutcomeTools
					.setConfigJson(configJson.get(ReportStatusConstants.TOOL_CONFIG_JSON).getAsJsonObject().toString());
		} else {
			insightsOutcomeTools.setConfigJson("{}");
		}
		insightsOutcomeTools.setIsActive(configJson.get(CommonsAndDALConstants.ISACTIVE).getAsBoolean());
		insightsOutcomeTools.setMetricUrl(configJson.get("metricUrl").getAsString());
		InsightsTools insightsMilestoneTools = outComeConfigDAL
				.getOutComeByToolId(configJson.get("toolName").getAsInt());
		insightsOutcomeTools.setInsightsTools(insightsMilestoneTools);
		insightsOutcomeTools.setCreatedDate(InsightsUtils.getCurrentTimeInEpochMilliSeconds());
		insightsOutcomeTools.setRequestParameters(configJson.get("parameters").getAsJsonArray().toString());
		return outComeConfigDAL.saveOutcomeConfig(insightsOutcomeTools);
	}

	public JsonArray getAllActiveOutcome() throws InsightsCustomException {
		try {
			List<InsightsOutcomeTools> configs = outComeConfigDAL.getAllActiveOutcome();
			Gson gson = new Gson();
			JsonElement element = gson.toJsonTree(configs, new TypeToken<List<InsightsOutcomeTools>>() {
			}.getType());
			if (!element.isJsonArray()) {
				throw new InsightsCustomException("Unable to parse Json");
			}
			return element.getAsJsonArray();
		} catch (Exception e) {
			log.error("Error getting outcome list ..", e);
			throw new InsightsCustomException(e.getMessage());
		}
	}

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
		for (JsonElement outcomeName : outcomeListFromUI) {
			InsightsOutcomeTools insightsOutcomeTools = outComeConfigDAL
					.getOutComeConfigByName(outcomeName.getAsString());
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
				mileJson.addProperty("workflowId", ""); // mile.getWorkflowConfig().getWorkflowId()
				List<JsonObject> config = new ArrayList<>();
				for (InsightsMileStoneOutcomeConfig milestoneConfig : mile.getListOfOutcomes()) {
					JsonObject jsonobject = new JsonObject();
					jsonobject.addProperty("id", milestoneConfig.getId());
					jsonobject.addProperty("mileStoneId", milestoneConfig.getMileStoneConfig().getId());
					jsonobject.addProperty("outcomeId", milestoneConfig.getInsightsOutcomeTools().getId());
					jsonobject.addProperty("outcomeName", milestoneConfig.getInsightsOutcomeTools().getOutcomeName());
					jsonobject.addProperty("queueName",
							milestoneConfig.getInsightsOutcomeTools().getInsightsTools().getAgentCommunicationQueue());
					jsonobject.addProperty("toolName",
							milestoneConfig.getInsightsOutcomeTools().getInsightsTools().getToolName());
					jsonobject.addProperty("status", milestoneConfig.getStatus());
					jsonobject.addProperty("statusMessage", milestoneConfig.getStatusMessage());
					jsonobject.addProperty("lastUpdatedDate", milestoneConfig.getLastUpdatedDate());
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

	public JsonObject buildSuccessResponseWithData(Object data) {

		JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty(PlatformServiceConstants.STATUS, PlatformServiceConstants.SUCCESS);
		jsonResponse.add(PlatformServiceConstants.DATA, new Gson().toJsonTree(data));
		JsonObject validatedData = ValidationUtils.replaceHTMLContentFormString(jsonResponse);
		if (validatedData == null) {
			validatedData = buildFailureResponse(PlatformServiceConstants.INVALID_RESPONSE_DATA);
		}
		return validatedData;
	}

	public static JsonObject buildFailureResponse(String message) {
		log.error("Error while running API message {} ", message);
		JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty(PlatformServiceConstants.STATUS, PlatformServiceConstants.FAILURE);
		jsonResponse.addProperty(PlatformServiceConstants.MESSAGE, message);
		return jsonResponse;
	}
}


