/*******************************************************************************
 * Copyright 2022 Cognizant Technology Solutions
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

package com.cognizant.devops.platformdal.healthutil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
import com.cognizant.devops.platformcommons.constants.CommonsAndDALConstants;
import com.cognizant.devops.platformdal.core.BaseDAL;

public class InsightsAgentHealthDetailDAL extends BaseDAL{
	
	private static Logger log = LogManager.getLogger(InsightsAgentHealthDetailDAL.class);
	
	/**
	 * Method to save Health records
	 * 
	 * @param agentHealthDetail
	 * @return integer
	 * @throws Exception
	 */
	public int saveAgentHealthDetail(InsightsAgentHealthDetails agentHealthDetail ) {
		int id = -1;
		try  {
			id = (int) save(agentHealthDetail);
			return id;
		} catch (Exception e) {
			log.error(e);
			return id;
		}
	}
	/**
	 * Method to fetch Detailed Health records
	 * 
	 * @param query
	 * @param agentId
	 * @param maxRow
	 * @return List<InsightsAgentHealthDetails>
	 * @throws Exception
	 */
	
	public List<InsightsAgentHealthDetails> fetchAgentHealthDetail(String query,String agentId,int maxRow) {
		try {
			
			Map<String, Object> parameters = new HashMap<>();
			Map<String, Object> extraParameters = new HashMap<>();
			parameters.put(AssessmentReportAndWorkflowConstants.AGENT_ID, agentId);
			extraParameters.put(CommonsAndDALConstants.MAX_RESULTS, maxRow);
			return executeQueryWithExtraParameter(query,InsightsAgentHealthDetails.class, parameters, extraParameters);

		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

}
