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
package com.cognizant.devops.platformservice.outcome.service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.cognizant.devops.platformcommons.constants.CommonsAndDALConstants;
import com.cognizant.devops.platformcommons.constants.ReportStatusConstants;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.milestone.InsightsMileStoneOutcomeConfig;
import com.cognizant.devops.platformdal.milestone.MileStoneConfigDAL;
import com.cognizant.devops.platformdal.outcome.InsightsOutcomeTools;
import com.cognizant.devops.platformdal.outcome.InsightsTools;
import com.cognizant.devops.platformdal.outcome.OutComeConfigDAL;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@Service
public class OutComeServiceImpl implements OutComeService{

	private static final Logger log = LogManager.getLogger(OutComeServiceImpl.class);

	OutComeConfigDAL outComeConfigDAL = new OutComeConfigDAL();
	MileStoneConfigDAL mileStoneConfigDAL = new MileStoneConfigDAL();
	
	@Override
	public int saveOutcomeConfig(JsonObject configJson) throws InsightsCustomException {
		String outcomeName = configJson.get("outcomeName").getAsString();
        InsightsOutcomeTools outcomeConfig = outComeConfigDAL.getOutComeConfigByName(outcomeName);
        if (outcomeConfig != null) {
			throw new InsightsCustomException("Outcome with given name already exists.");
		}
		InsightsOutcomeTools insightsOutcomeTools = new InsightsOutcomeTools();
		insightsOutcomeTools.setOutcomeName(outcomeName);
		insightsOutcomeTools.setOutcomeType(configJson.get("outcomeType").getAsString());
		if(configJson.has(ReportStatusConstants.TOOL_CONFIG_JSON)) {
			insightsOutcomeTools.setConfigJson(configJson.get(ReportStatusConstants.TOOL_CONFIG_JSON).getAsJsonObject().toString());
		} else {
			insightsOutcomeTools.setConfigJson("{}");
		}
		insightsOutcomeTools.setIsActive(configJson.get(CommonsAndDALConstants.ISACTIVE).getAsBoolean());
		insightsOutcomeTools.setMetricUrl(configJson.get("metricUrl").getAsString());
		InsightsTools insightsMilestoneTools = outComeConfigDAL.getOutComeByToolId(configJson.get("toolName").getAsInt());
		insightsOutcomeTools.setInsightsTools(insightsMilestoneTools);
		insightsOutcomeTools.setCreatedDate(InsightsUtils.getCurrentTimeInEpochMilliSeconds());
		insightsOutcomeTools.setRequestParameters(configJson.get("parameters").getAsJsonArray().toString());
		return outComeConfigDAL.saveOutcomeConfig(insightsOutcomeTools);
	}

	@Override
	public JsonArray getMileStoneTools() throws InsightsCustomException {
		try {
			List<InsightsTools> configs = outComeConfigDAL.getMileStoneTools();
			Gson gson = new Gson();
			JsonElement element = gson.toJsonTree(configs, new TypeToken<List<InsightsTools>>() {}.getType());
			if (!element.isJsonArray() ) {
				    throw new InsightsCustomException("Unable to parse Json");
			}
			return element.getAsJsonArray();
		} catch (Exception e) {
			log.error("Error getting milestone list ..", e);
			throw new InsightsCustomException(e.getMessage());
		}
	}

	@Override
	public void updateOutcomeConfig(JsonObject configJson) throws InsightsCustomException {
		try {
			int outcomeId = configJson.get("id").getAsInt();
			InsightsOutcomeTools insightsOutcomeTools = outComeConfigDAL.getOutcomeConfig(outcomeId);
			insightsOutcomeTools.setOutcomeName(configJson.get("outcomeName").getAsString());
			insightsOutcomeTools.setOutcomeType(configJson.get("outcomeType").getAsString());
			if(configJson.has(ReportStatusConstants.TOOL_CONFIG_JSON)) {
				insightsOutcomeTools.setConfigJson(configJson.get(ReportStatusConstants.TOOL_CONFIG_JSON).getAsJsonObject().toString());
			}
			insightsOutcomeTools.setIsActive(configJson.get("isActive").getAsBoolean());
			insightsOutcomeTools.setMetricUrl(configJson.get("metricUrl").getAsString());
			insightsOutcomeTools.setUpdatedDate(InsightsUtils.getCurrentTimeInEpochMilliSeconds());
			insightsOutcomeTools.setRequestParameters(configJson.get("parameters").toString());
			outComeConfigDAL.updateOutcomeConfig(insightsOutcomeTools);
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(" Error while updating record "+e.getMessage());
		}
	}
	
	@Override
	public void updateOutcomeConfigStatus(JsonObject configJson) throws InsightsCustomException {
		try {
			int outcomeId = configJson.get("id").getAsInt();
			InsightsOutcomeTools insightsOutcomeTools = outComeConfigDAL.getOutcomeConfig(outcomeId);
			insightsOutcomeTools.setIsActive(configJson.get("isActive").getAsBoolean());
			outComeConfigDAL.updateOutcomeConfig(insightsOutcomeTools);
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(" Error while updating outcome status "+e.getMessage());
		}
	}

	@Override
	public String deleteOutcomeDetails(int id) throws InsightsCustomException {
		InsightsMileStoneOutcomeConfig milestoneConfig = mileStoneConfigDAL.getMileStoneByOutcomeId(id);
		if (milestoneConfig != null) {
			throw new InsightsCustomException(
					"Outcome cannot be deleted as it is attached to Milestone Config.");
		} else {
			return outComeConfigDAL.deleteOutcomeConfig(id);
			
		}
	}

	@Override
	public JsonArray getAllActiveOutcome() throws InsightsCustomException {
		try {
			List<InsightsOutcomeTools> configs = outComeConfigDAL.getAllActiveOutcome();
			Gson gson = new Gson();
			JsonElement element = gson.toJsonTree(configs, new TypeToken<List<InsightsOutcomeTools>>() {}.getType());
			if (!element.isJsonArray() ) {
				    throw new InsightsCustomException("Unable to parse Json");
			}
			return element.getAsJsonArray();
		} catch (Exception e) {
			log.error("Error getting outcome list ..", e);
			throw new InsightsCustomException(e.getMessage());
		}
	}
}
