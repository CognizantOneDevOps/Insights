/*******************************************************************************
 * Copyright 2022 Cognizant Technology Solutions
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
package com.cognizant.devops.platformdal.groupemail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
import com.cognizant.devops.platformdal.assessmentreport.InsightsEmailTemplates;
import com.cognizant.devops.platformdal.core.BaseDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowExecutionHistory;

public class GroupEmailConfigDAL extends BaseDAL {

	private static Logger log = LogManager.getLogger(GroupEmailConfigDAL.class);

	public InsightsGroupEmailConfiguration getConfigByBatchName(String batchName) {

		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("batchName", batchName);
			return getUniqueResult("FROM InsightsGroupEmailConfiguration IGEC WHERE IGEC.batchName = :batchName",
					InsightsGroupEmailConfiguration.class, parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}

	}

	public InsightsGroupEmailConfiguration getConfigByGroupEmailTemplateId(int id) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("id", id);
			return getUniqueResult("FROM InsightsGroupEmailConfiguration IGEC WHERE IGEC.groupEmailTemplateID = :id",
					InsightsGroupEmailConfiguration.class, parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}

	}
	
	public InsightsGroupEmailConfiguration getConfigByGroupEmailWorkflowId(String workflowId) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			return getUniqueResult("FROM InsightsGroupEmailConfiguration IGEC WHERE IGEC.workflowConfig.workflowId = :workflowId ",
					InsightsGroupEmailConfiguration.class, parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	public int saveInsightsGroupEmailConfiguration(InsightsGroupEmailConfiguration config) {
		try {
			return (int) save(config);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}
	
	public void updateGroupEmailConfig(InsightsGroupEmailConfiguration config) {
		try {
			update(config);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	public List<InsightsGroupEmailConfiguration> fetchAllGroupEmailConfigs(String source) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("source", source);
			return getResultList("FROM InsightsGroupEmailConfiguration IGEC WHERE IGEC.source = :source",
					InsightsGroupEmailConfiguration.class, parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	public void deleteGroupEmailConfig(InsightsGroupEmailConfiguration groupEmailConfig) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("workflowId", groupEmailConfig.getWorkflowConfig().getWorkflowId());
			List<InsightsWorkflowExecutionHistory> executionRecord = getResultList(
					"FROM InsightsWorkflowExecutionHistory a WHERE a.workflowConfig.workflowId= :workflowId",
					InsightsWorkflowExecutionHistory.class, parameters);
			for (InsightsWorkflowExecutionHistory history : executionRecord) {
				delete(history);
			}
			delete(groupEmailConfig);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}

	}

}
