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
package com.cognizant.devops.engines.platformengine.modules.offlinedataprocessing;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.convert.TypeConverters.CronExpressionConverter;
import org.apache.logging.log4j.core.util.CronExpression;

import com.cognizant.devops.platformcommons.constants.EngineConstants;
import com.cognizant.devops.platformcommons.constants.MilestoneConstants;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFilesDAL;
import com.cognizant.devops.platformdal.offlineDataProcessing.InsightsOfflineConfig;
import com.cognizant.devops.platformdal.offlineDataProcessing.InsightsOfflineConfigDAL;
import com.google.gson.JsonObject;

public class OfflineDataProcessingFromDB {

	private static Logger log = LogManager.getLogger(OfflineDataProcessingFromDB.class);
	InsightsConfigFilesDAL configFilesDAL = new InsightsConfigFilesDAL();
	InsightsOfflineConfigDAL configOfflineDAL = new InsightsOfflineConfigDAL();
	private Map<String,String> loggingInfo = new ConcurrentHashMap<>();
	GraphDBHandler dbHandler = new GraphDBHandler();
	public static final int MAX_RETRY_COUNT = 2;
	
	public List<InsightsOfflineConfig> getAllActiveOfflineDataConfigFromDB() {
		List<InsightsOfflineConfig> offlineDataConfig = null;
		try {
			offlineDataConfig = configOfflineDAL.getAllActiveOfflineDataQuery();
		} catch (Exception e) {
			log.error(e);
		}
		return offlineDataConfig;
	}

	public void processOfflineConfigurationFromDB(List<InsightsOfflineConfig> offlineDataConfig,
			Map<String, String> loggingInfolocal) throws InsightsCustomException {
		loggingInfo.putAll(loggingInfolocal);
		try {
			for (InsightsOfflineConfig dataEnrichmentModel : offlineDataConfig) {
				processEnrichmentRecord(dataEnrichmentModel);
			}

		} catch (Exception e) {
			log.error(" Type=OfflineDataProcessing execId={} offlineProcessingFileName={} queryName={} ",
					loggingInfo.get(MilestoneConstants.EXECID), loggingInfo.get(EngineConstants.FILENAME),
					loggingInfo.get(EngineConstants.QUERYNAME), e);
			throw new InsightsCustomException(e.getMessage());
		}
	}

	private void processEnrichmentRecord(InsightsOfflineConfig dataEnrichmentModel) throws Exception {
		String cypherQuery = dataEnrichmentModel.getCypherQuery();
		String cronSchedule = dataEnrichmentModel.getCronSchedule();
		int retryCount = dataEnrichmentModel.getRetryCount();
		try {
			loggingInfo.put(EngineConstants.QUERYNAME, dataEnrichmentModel.getQueryName());
			log.debug(" Type=OfflineDataProcessing execId={} queryName={} ProcessingTime={} processedRecords={} Cypher query : {} ",
					loggingInfo.get(MilestoneConstants.EXECID), loggingInfo.get(EngineConstants.QUERYNAME), 0,
					0, cypherQuery);

			if (cypherQuery == null || cypherQuery.isEmpty() || (cronSchedule == null && cronSchedule.isEmpty())) {
				throw new InsightsCustomException("queryName : " + dataEnrichmentModel.getQueryName()
						+ " doesn't have either cypherQuery or cronSchedule attribute.");
			}

			if (isQueryScheduledToRunFromDB(dataEnrichmentModel.getLastRunTime(),
					dataEnrichmentModel.getCronSchedule())) {
				executeCypherQueryFromDB(cypherQuery, dataEnrichmentModel);
			}
			
		} catch (UnsupportedOperationException | IllegalStateException | IndexOutOfBoundsException
				| InsightsCustomException ex) {
			updateRetryCount(retryCount, dataEnrichmentModel, ex.getMessage());
			log.error(
					" Type=OfflineDataProcessing execId={} queryName={} query processing failed with message : {}",
					loggingInfo.get(MilestoneConstants.EXECID), loggingInfo.get(EngineConstants.QUERYNAME), ex);
		}
		configOfflineDAL.updateOfflineConfig(dataEnrichmentModel);
	}

	public void executeCypherQueryFromDB(String cypherQuery, InsightsOfflineConfig dataEnrichmentModel) throws InsightsCustomException {
		
		int processedRecords = 1;
		int recordCount = 0;
		long queryExecutionStartTime = System.currentTimeMillis();
		
			while (processedRecords > 0) {
				JsonObject cypherQueryResponseJson = dbHandler.executeCypherQuery(cypherQuery).getJson();
				
				if (cypherQueryResponseJson.getAsJsonObject().get("errors").getAsJsonArray().size() > 0) {
					throw new InsightsCustomException(cypherQueryResponseJson.get("errors").getAsJsonArray().get(0)
							.getAsJsonObject().get("error").getAsString());
				}

				processedRecords = cypherQueryResponseJson.getAsJsonArray("results").get(0).getAsJsonObject()
						.getAsJsonArray("data").get(0).getAsJsonObject().getAsJsonArray("row").get(0).getAsInt();
				
				log.debug(" Type=OfflineDataProcessing execId={} queryName={} ProcessingTime={} processedRecords={}",
						loggingInfo.get(MilestoneConstants.EXECID), loggingInfo.get(EngineConstants.QUERYNAME), 0,
						processedRecords);
				recordCount = recordCount + processedRecords;
			}
			
			long queryProcessingTime = (System.currentTimeMillis() - queryExecutionStartTime)/1000;
			
			dataEnrichmentModel.setRetryCount(0);
			dataEnrichmentModel.setRecordsProcessed(recordCount);
			dataEnrichmentModel.setQueryProcessingTime(queryProcessingTime);
			dataEnrichmentModel.setLastRunTime(System.currentTimeMillis()/1000);
			dataEnrichmentModel.setStatus("Success");
			dataEnrichmentModel.setMessage("Query executed successfully!");

			log.debug(" Type=OfflineDataProcessing execId={} queryName={} queryProcessingTime={} processedRecords={} Offline Query processed records={} ",
					loggingInfo.get(MilestoneConstants.EXECID), loggingInfo.get(EngineConstants.QUERYNAME),
					queryProcessingTime, processedRecords, processedRecords);
	}

	public boolean isQueryScheduledToRunFromDB(Long lastRunTime, String cronSchedule) throws Exception {
		// if lastExecutionTime property is not added in the json file, we'll execute
		// the query by default
		if (lastRunTime == 0) {
			return true;
		}
		return cronScheduleValidate(lastRunTime, cronSchedule);
	}

	private boolean checkRunScheduled(Long lastRunTime, Long runSchedule) {
		Long timeDifferenceInMinutes = InsightsUtils.getDifferenceFromLastRunTimeInMinutes(lastRunTime);
		return (timeDifferenceInMinutes > runSchedule);
	}

	private boolean cronScheduleValidate(Long lastRunTime, String cronSchedule) throws Exception {

		try {

			if ((new CronExpressionConverter().convert(cronSchedule))
					.getNextInvalidTimeAfter(new Date(lastRunTime * 1000)).before(new Date())) {
				return true;
			}
		} catch (Exception e) {
			throw new InsightsCustomException("Unable to parse the CRON expression: " + e.getMessage());
		}
		return false;
	}

	private void updateRetryCount(int retryCount, InsightsOfflineConfig dataEnrichmentModel, String exception) {
		
		dataEnrichmentModel.setRetryCount(++retryCount);
		dataEnrichmentModel.setStatus("Failure");
		dataEnrichmentModel.setMessage(exception);
		if (retryCount > MAX_RETRY_COUNT) {
			dataEnrichmentModel.setIsActive(false);
		}

	}
}
