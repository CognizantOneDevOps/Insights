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
package com.cognizant.devops.platformdal.milestone;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformdal.core.BaseDAL;
import com.cognizant.devops.platformdal.outcome.InsightsOutcomeTools;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;


public class MileStoneConfigDAL extends BaseDAL {

	private static Logger log = LogManager.getLogger(MileStoneConfigDAL.class);
	
	public int saveMileStoneConfig(MileStoneConfig config)
    { 
		int id = -1;
		try  {
			id = (int) save(config);
			return id;
		} catch (Exception e) {
			log.error(e);
			return id;
		}
    }

	public List<MileStoneConfig> getAllMileStoneConfig() {
		try  {
			Map<String,Object> parameters = new HashMap<>();
			return getResultList( "FROM MileStoneConfig mc order by mc.mileStoneName ",
					MileStoneConfig.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	public void updateMileStoneConfig(MileStoneConfig config)
    { 
		try {
			update(config);
		}
        catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
    }

	public String deleteMileStoneConfig(int id) {
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("id", id);
			MileStoneConfig mileStoneRecord = getSingleResult(
					"FROM MileStoneConfig mc WHERE mc.id = :id",
					MileStoneConfig.class,
					parameters);
			delete(mileStoneRecord);
			return PlatformServiceConstants.SUCCESS;
		}
        catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public MileStoneConfig fetchMileStoneByWorkflowId(String workflowId) {
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("workflowId", workflowId);
			return getUniqueResult( "FROM MileStoneConfig mc where mc.workflowConfig.workflowId = :workflowId",
					MileStoneConfig.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}
	
	public List<MileStoneConfig> fetchMileStoneByStatus() {
		try {
			Map<String,Object> parameters = new HashMap<>();
			return getResultList( "FROM MileStoneConfig mc where mc.status = 'NOT_STARTED'",
					MileStoneConfig.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	public void deleteOutcome(int id) {
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("id", id);
			InsightsMileStoneOutcomeConfig insightsMileStoneOutcomeConfig = getSingleResult(
					"FROM InsightsMileStoneOutcomeConfig mc WHERE mc.id = :id",
					InsightsMileStoneOutcomeConfig.class,
					parameters);
			delete(insightsMileStoneOutcomeConfig);
		}
        catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public List<InsightsOutcomeTools> getOutcomeConfigTools() {
		try  {
			Map<String,Object> parameters = new HashMap<>();
			return getResultList( "FROM InsightsOutcomeTools mc",
					InsightsOutcomeTools.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	public Boolean updateMilestoneOutcomeStatus(int milestoneId, int outcomeId, String status, String message) {
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("mileStoneId", milestoneId);
			parameters.put("outcomeId", outcomeId);
			InsightsMileStoneOutcomeConfig config = getSingleResult(
					"FROM InsightsMileStoneOutcomeConfig mc WHERE mc.mileStoneConfig.id = :mileStoneId and mc.insightsOutcomeTools.id = :outcomeId",
					InsightsMileStoneOutcomeConfig.class,
					parameters);
			config.setStatus(status);
			config.setStatusMessage(message);
			config.setLastUpdatedDate(InsightsUtils.getCurrentTimeInEpochMilliSeconds());
			update(config);
			return Boolean.TRUE;
		}
        catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}
	
	public MileStoneConfig getMileStoneConfigById(int milestoneId) {
		try  {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("milestoneId", milestoneId);
			return getSingleResult( "FROM MileStoneConfig mc where mc.id=:milestoneId",
					MileStoneConfig.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	public void updateMilestoneStatus(int milestoneId, String status) {
		try  {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("milestoneId", milestoneId);
			MileStoneConfig config = getSingleResult( "FROM MileStoneConfig mc where mc.id=:milestoneId",
					MileStoneConfig.class,
					parameters);
			config.setStatus(status);
			update(config);
			
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
		
	}

	public Boolean updateMileStoneConfigByWorkflowId(String workflowId) {
		try  {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			InsightsWorkflowConfiguration updateStatus =  getUniqueResult(
					"FROM InsightsWorkflowConfiguration WC WHERE WC.workflowId = :workflowId ",
					InsightsWorkflowConfiguration.class,
					parameters);
			updateStatus.setStatus("NOT_STARTED");
			update(updateStatus);
			return Boolean.TRUE;
			
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}
	
	public InsightsMileStoneOutcomeConfig getMileStoneByOutcomeId(int outcomeId) {
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("outcomeId", outcomeId);
			return getUniqueResult( "FROM InsightsMileStoneOutcomeConfig mc where mc.insightsOutcomeTools.id = :outcomeId",
					InsightsMileStoneOutcomeConfig.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}
	
	public MileStoneConfig getMileStoneConfigByName(String milestoneName) {
		try  {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("milestoneName", milestoneName);
			return getUniqueResult( "FROM MileStoneConfig mc where mc.mileStoneName=:milestoneName",
					MileStoneConfig.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}
	
	
}