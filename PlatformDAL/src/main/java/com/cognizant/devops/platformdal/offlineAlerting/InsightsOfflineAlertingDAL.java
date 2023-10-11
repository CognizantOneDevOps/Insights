/*******************************************************************************
 * Copyright 2023 Cognizant Technology Solutions
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
package com.cognizant.devops.platformdal.offlineAlerting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformdal.core.BaseDAL;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaDashboardPdfConfig;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowExecutionHistory;

public class InsightsOfflineAlertingDAL extends BaseDAL {

	private static Logger log = LogManager.getLogger(InsightsOfflineAlertingDAL.class);

	public int saveOfflineAlertingConfig(InsightsOfflineAlerting config) {
		int id = -1;
		try {
			id = (int) save(config);
			return id;
		} catch (Exception e) {
			log.error(e);
			return id;
		}
	}

	public List<InsightsOfflineAlerting> getAllOfflineAlertingConfig() {
		try {
			Map<String, Object> parameters = new HashMap<>();
			return getResultList("FROM InsightsOfflineAlerting IOA ORDER BY IOA.alertName ",
					InsightsOfflineAlerting.class, parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	public InsightsOfflineAlerting getAlertConfigByAlertName(String alertName) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("alertName", alertName);
			return getUniqueResult("FROM InsightsOfflineAlerting IOA WHERE IOA.alertName = :alertName ",
					InsightsOfflineAlerting.class, parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}
	
	public InsightsOfflineAlerting getAlertConfigByWorkflowId(String workflowId)
    { 
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("workflowId", workflowId);
			return getUniqueResult( "FROM InsightsOfflineAlerting IOA WHERE IOA.workflowConfig.workflowId = :workflowId",
					InsightsOfflineAlerting.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
		
    }

	public List<InsightsOfflineAlerting> getActiveOfflineAlerting() {
		try {
			Map<String, Object> parameters = new HashMap<>();
			return getResultList("FROM InsightsOfflineAlerting IOA WHERE IOA.isActive = true ",
					InsightsOfflineAlerting.class, parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	public void updateOfflineAlertingConfig(InsightsOfflineAlerting alertConfig) {
		try {
			update(alertConfig);
			
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}
	
	public void deleteOfflineAlerting(InsightsOfflineAlerting alertConfig) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("workflowId", alertConfig.getWorkflowConfig().getWorkflowId());
			InsightsOfflineAlerting executionRecord = getUniqueResult(
					"FROM InsightsOfflineAlerting IOA WHERE IOA.workflowConfig.workflowId= :workflowId",
					InsightsOfflineAlerting.class, parameters);
			delete(executionRecord);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}


}
