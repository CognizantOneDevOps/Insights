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
package com.cognizant.devops.engines.platformengine.modules.offlinedataprocessing;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.offlineDataProcessing.InsightsOfflineConfig;
import com.cognizant.devops.platformdal.offlineDataProcessing.InsightsOfflineConfigDAL;
import com.google.gson.JsonObject;

public class OfflineProcessingTestData {
	private static final Logger log = LogManager.getLogger(OfflineProcessingTestData.class);
	InsightsOfflineConfigDAL insightsOfflineConfigDAL = new InsightsOfflineConfigDAL();
    public static int MAX_RETRY_COUNT = 2;
    public static long lastRunTime = 0;
    public static String cronSchedule = "0 */5 * ? * *";
    public static String wrongCronSchedule = "0 */8 * ? # *";
    
	public String saveOfflineDefinition(JsonObject registerOfflineJson) throws InsightsCustomException {
		String queryName = "-1";
		try {
			InsightsOfflineConfig offlineConfig = new InsightsOfflineConfig();
			queryName = registerOfflineJson.get("queryName").getAsString();
			InsightsOfflineConfig offlineConfigData = insightsOfflineConfigDAL.getOfflineDataConfig(queryName);
			if (offlineConfigData != null) {
				throw new InsightsCustomException("Offline Data already exists");
			}
			String cypherQuery = registerOfflineJson.get("cypherQuery").getAsString();
			String toolName = registerOfflineJson.get("toolName").getAsString();
			Long lastRunTime = registerOfflineJson.get("lastruntime").getAsLong();
			long queryProcess = registerOfflineJson.get("queryProcessingTime").getAsLong();
			String status = registerOfflineJson.get("status").getAsString();
			String message = registerOfflineJson.get("message").getAsString();
			String cronSchedule = registerOfflineJson.get("cronSchedule").getAsString();
			if (!cronSchedule.isEmpty() && !org.quartz.CronExpression.isValidExpression(cronSchedule)) {
				throw new InsightsCustomException("Cron Expression is invalid");
			}

			offlineConfig.setIsActive(true);
			offlineConfig.setQueryName(queryName);
			offlineConfig.setCypherQuery(cypherQuery);
			offlineConfig.setToolName(toolName);
			offlineConfig.setCronSchedule(cronSchedule);
			offlineConfig.setLastRunTime(lastRunTime);
			offlineConfig.setRetryCount(0);
			offlineConfig.setQueryProcessingTime(queryProcess);
			offlineConfig.setStatus(status);
			offlineConfig.setMessage(message);

			insightsOfflineConfigDAL.saveOfflineDataConfig(offlineConfig);
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
		return queryName;

	}
	
	public List<InsightsOfflineConfig> getAllActiveOfflineConfig(){
		List<InsightsOfflineConfig> list = null;
		try {
			list = insightsOfflineConfigDAL.getAllActiveOfflineDataQuery();
		} catch (Exception e) {
			log.error(e);
		}
		return list;
	}

	public boolean updateRetryCount(int retryCount, InsightsOfflineConfig dataEnrichmentModel, String message) {
		dataEnrichmentModel.setRetryCount(++retryCount);
		dataEnrichmentModel.setStatus("Failure");
		dataEnrichmentModel.setMessage(message);
		if (retryCount > MAX_RETRY_COUNT) {
			dataEnrichmentModel.setIsActive(false);
		}
		return true;
	}
	
	public boolean deleteOfflineData(String queryName){
		boolean isRecordDeleted = Boolean.FALSE;
		try {
			InsightsOfflineConfig offlineExistingConfig = insightsOfflineConfigDAL.getOfflineDataConfig(queryName);
			if (offlineExistingConfig != null) {
				insightsOfflineConfigDAL.deleteOfflinebyQueryName(queryName);
				isRecordDeleted = Boolean.TRUE;
			} else {
				throw new InsightsCustomException("Offline definition not exists");
			}
		} catch (Exception e) {
			log.error(e);
		}
		return isRecordDeleted;
	}
}
