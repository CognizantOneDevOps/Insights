/*******************************************************************************
* Copyright 2024 Cognizant Technology Solutions
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
package com.cognizant.devops.platformdal.neo4jScaling;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformdal.core.BaseDAL;

public class InsightsReplicaConfigDAL extends BaseDAL {

	private static Logger log = LogManager.getLogger(InsightsReplicaConfigDAL.class);

	/**
	 * Method to get all replica neo4j config
	 * 
	 * @return List
	 */
	public List<InsightsReplicaConfig> getAllReplicaConfig() {
		try {
			Map<String, Object> parameters = new HashMap<>();
			return getResultList("FROM InsightsReplicaConfig INRC ORDER BY INRC.id", InsightsReplicaConfig.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}
	
	/**
	 * Method to get replica config by replicaName
	 * 
	 * @return InsightsReplicaConfig
	 */
	public InsightsReplicaConfig getReplicaByName(String replicaName) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("replicaName", replicaName);
			return getUniqueResult("FROM InsightsReplicaConfig INRC where INRC.replicaName = :replicaName", InsightsReplicaConfig.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}
}
