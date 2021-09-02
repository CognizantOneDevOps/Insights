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
package com.cognizant.devops.platformdal.grafana.pdf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformdal.core.BaseDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowExecutionHistory;


public class GrafanaDashboardPdfConfigDAL extends BaseDAL {

	private static Logger log = LogManager.getLogger(GrafanaDashboardPdfConfigDAL.class);
	
	public int saveGrafanaDashboardConfig(GrafanaDashboardPdfConfig config)
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
	
	public GrafanaDashboardPdfConfig fetchGrafanaDashboardDetailsByWorkflowId(String workflowId) {
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("workflowId", workflowId);
			return getUniqueResult( "FROM GrafanaDashboardPdfConfig gd where gd.workflowConfig.workflowId = :workflowId",
					GrafanaDashboardPdfConfig.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}
	
	public GrafanaDashboardPdfConfig getWorkflowById(int id)
    { 
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("id", id);
			return getUniqueResult( "FROM GrafanaDashboardPdfConfig a WHERE a.id = :id",
					GrafanaDashboardPdfConfig.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
		
    }
	
	public List<GrafanaDashboardPdfConfig> getAllGrafanaDashboardConfigs() {
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("source", "PLATFORM");
			return getResultList( "FROM GrafanaDashboardPdfConfig gd WHERE gd.source = :source",
					GrafanaDashboardPdfConfig.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	public void updateGrafanaDashboardConfig(GrafanaDashboardPdfConfig config)
    { 
		try {
			update(config);
		}
        catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
    }

	public void deleteGrafanaDashboardConfig(GrafanaDashboardPdfConfig grafanaDashboardPdfConfig) {
		try {
			
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("workflowId", grafanaDashboardPdfConfig.getWorkflowConfig().getWorkflowId());
			List<InsightsWorkflowExecutionHistory> executionRecord = getResultList( "FROM InsightsWorkflowExecutionHistory a WHERE a.workflowConfig.workflowId= :workflowId",
					InsightsWorkflowExecutionHistory.class,
					parameters);
			for(InsightsWorkflowExecutionHistory history:executionRecord){
				delete(history);
			}

			delete(grafanaDashboardPdfConfig);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}
	
	public int saveGrafanaOrgToken(GrafanaOrgToken grafanaOrgToken)
    { 
		int id = -1;
		try  {
			id = (int) save(grafanaOrgToken);
			return id;
		} catch (Exception e) {
			log.error(e);
			return id;
		}
    }
	
	public GrafanaOrgToken getTokenByOrgId(int orgId)
    { 
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("orgId", orgId);
			return getUniqueResult( "FROM GrafanaOrgToken a WHERE a.orgId = :orgId",
					GrafanaOrgToken.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
		
    }
	
}