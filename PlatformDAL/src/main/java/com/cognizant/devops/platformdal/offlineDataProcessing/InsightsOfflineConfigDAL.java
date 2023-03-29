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
package com.cognizant.devops.platformdal.offlineDataProcessing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformdal.core.BaseDAL;

public class InsightsOfflineConfigDAL extends BaseDAL {
	private static Logger log = LogManager.getLogger(InsightsOfflineConfigDAL.class);

	/**
	 * Method to save Offline Data Configuration
	 * 
	 * @param config
	 * @return int
	 */
	public int saveOfflineDataConfig(InsightsOfflineConfig config) {
		try {
			return (int) save(config);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to update InsightsOfflineConfig record
	 * 
	 * @param config
	 * @return int
	 */
	public int updateOfflineConfig(InsightsOfflineConfig config) {
		try {
			update(config);
			return 0;
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to delete Offline data using queryName
	 * 
	 * @param queryName
	 * @return String
	 */
	public String deleteOfflinebyQueryName(String queryName) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("queryName", queryName);
			InsightsOfflineConfig executionRecord = getSingleResult(
					"FROM InsightsOfflineConfig IOC WHERE IOC.queryName= :queryName", InsightsOfflineConfig.class,
					parameters);
			delete(executionRecord);
			return PlatformServiceConstants.SUCCESS;
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}
	
	public List<InsightsOfflineConfig> getAllActiveOfflineDataQuery() {
		try {
			Map<String, Object> parameters = new HashMap<>();
			return getResultList("FROM InsightsOfflineConfig IOC WHERE IOC.isActive = true ",
					InsightsOfflineConfig.class, parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}


	/**
	 * Method to get Offline config using queryName
	 * 
	 * @param queryName
	 * @return InsightsOfflineConfig object
	 */
	public InsightsOfflineConfig getOfflineDataConfig(String queryName) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("queryName", queryName);
			return getUniqueResult("FROM InsightsOfflineConfig IOC WHERE IOC.queryName = :queryName ",
					InsightsOfflineConfig.class, parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method used to get all offlineConfig list
	 * 
	 * @return List<InsightsOfflineConfig>
	 */
	public List<InsightsOfflineConfig> getAllOfflineConfig() {
		try {
			Map<String, Object> parameters = new HashMap<>();
			return getResultList("FROM InsightsOfflineConfig IOC ORDER BY IOC.lastRunTime desc", InsightsOfflineConfig.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

}
