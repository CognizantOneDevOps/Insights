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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.convert.TypeConverters.CronExpressionConverter;
import org.apache.logging.log4j.core.util.CronExpression;

import com.cognizant.devops.engines.platformengine.message.core.EngineStatusLogger;
import com.cognizant.devops.engines.platformengine.modules.offlinedataprocessing.model.DataEnrichmentModel;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonWriter;

/**
 * This is the executor class for Offline Data Processing. This class has the
 * ability to execute a sequence of Cypher queries. These cypher queries should
 * be stored inside a JSON configuration file. This code has the capability of
 * reading multiple json files which reside inside "data-enrichment" folder of
 * INSIGHTS_HOME path.
 * Config JSON files should have following predefined format
 * "queryName": "Some description on the query", 
 * "cypherQuery": "Actual cypher query", 
 * "runSchedule": "Query execution interval in minutes",
 * "lastExecutionTime": "Last execution time will be updated by code",
 * 
 * @author 368419
 *
 */
public class OfflineDataProcessingExecutor extends TimerTask {
	private static Logger log = LogManager.getLogger(OfflineDataProcessingExecutor.class);
	private static final String DATE_TIME_FORMAT = "yyyy/MM/dd hh:mm a";
	private static final String JSON_FILE_EXTENSION = "json";
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT).withZone(InsightsUtils.zoneId);

	@Override
	public void run() {
		try {
			executeOfflineProcessing();
			EngineStatusLogger.getInstance().createEngineStatusNode("Offline Data Procesing completed",PlatformServiceConstants.SUCCESS);
		} catch (Exception e) {
			EngineStatusLogger.getInstance().createEngineStatusNode("Offline Data Procesing has some issue",
					PlatformServiceConstants.FAILURE);
		}
	}

	public int executeOfflineProcessing() {
		File queryFolderPath = new File(ConfigOptions.OFFLINE_DATA_PROCESSING_RESOLVED_PATH);
		File[] files = queryFolderPath.listFiles();
		int jsonFileCount = 0;		
		if (files == null) {
			return jsonFileCount;
		}	
		for (File eachFile : files) {
			if (eachFile.isFile()) { // this line removes other directories/folders
				String fileName = eachFile.getName();
				if (hasJsonFileExtension(fileName)) {
					jsonFileCount++;
					processOfflineConfiguration(eachFile);
				}
			}
		}				
		return jsonFileCount;
	}

	/**
	 * Checks whether file has .json/.JSON extension
	 * 
	 * @param fileName
	 * @return
	 */
	public Boolean hasJsonFileExtension(String fileName) {
		if (fileName != null && !fileName.isEmpty()) {
			String extension = FilenameUtils.getExtension(fileName);
			if (JSON_FILE_EXTENSION.equalsIgnoreCase(extension)) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	/**
	 * Processes each offline configuration file
	 * Processes each query block inside each configuration file and executes cypher query
	 * @param jsonFile
	 */
	public Boolean processOfflineConfiguration(File jsonFile) {
		try {
			try (BufferedReader reader = new BufferedReader(new FileReader(jsonFile))) {
				DataEnrichmentModel[] dataEnrichmentModelArray = new Gson().fromJson(reader,
						DataEnrichmentModel[].class);
				List<DataEnrichmentModel> dataEnrichmentModels = Arrays.asList(dataEnrichmentModelArray);
				for (DataEnrichmentModel dataEnrichmentModel : dataEnrichmentModels) {
					String cypherQuery = dataEnrichmentModel.getCypherQuery();
					Long runSchedule = dataEnrichmentModel.getRunSchedule();
					if (cypherQuery == null || cypherQuery.isEmpty() || runSchedule == null )  {
						log.error(dataEnrichmentModel.getQueryName() + " doesn't have either cypherQuery or runSchedule attribute.");
						continue;
					}
					if (isQueryScheduledToRun(dataEnrichmentModel.getRunSchedule(),
							dataEnrichmentModel.getLastExecutionTime(), dataEnrichmentModel.getCronSchedule())) {
						Boolean successFlag = executeCypherQuery(cypherQuery, dataEnrichmentModel);
						//Checks if query execution fails due to some exception, don't update lastExecutionTime 
						if (successFlag) {
							updateLastExecutionTime(dataEnrichmentModel);							
						}
					}
				}
				// Write into the file
				try (JsonWriter writer = new JsonWriter(new FileWriter(jsonFile))) {
					writer.setIndent("  ");
					new GsonBuilder().disableHtmlEscaping().create().toJson(dataEnrichmentModels.toArray(),
							DataEnrichmentModel[].class, writer);
				} catch (IOException e) {
					log.error("Unable to update offline configuration file.", e);
				}
			} catch (FileNotFoundException e) {
				log.error("offline configuration file not found.", e);
			} catch (IOException e) {
				log.error("Unable to read offline configuration file.", e);
			}
		} catch (IllegalStateException | JsonSyntaxException ex) {
			log.error(jsonFile.getName() + " file is not as per expected format", ex);
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	/**
	 * Updates lastRunTime in the offline vector file after processing cypher
	 * query
	 * 
	 */
	public DataEnrichmentModel updateLastExecutionTime(DataEnrichmentModel dataEnrichmentModel) {
		String lastRunTime = InsightsUtils.getLocalDateTime(DATE_TIME_FORMAT);
		if (dataEnrichmentModel != null) {
			dataEnrichmentModel.setLastExecutionTime(lastRunTime);
		}
		return dataEnrichmentModel;
	}

	/**
	 * Executes cypherQuery and adds/updates two attributes "recordsProcessed"
	 * and "processingTime"
	 * 
	 * @param cypherQuery
	 * @param jsonObject
	 */
	public Boolean executeCypherQuery(String cypherQuery, DataEnrichmentModel dataEnrichmentModel) {
		GraphDBHandler dbHandler = new GraphDBHandler();
		int processedRecords = 1;
		int recordCount = 0;
		long queryExecutionStartTime = System.currentTimeMillis();
		try {
			while (processedRecords > 0) {
				GraphResponse sprintResponse = dbHandler.executeCypherQuery(cypherQuery);
				JsonObject sprintResponseJson = sprintResponse.getJson();
				try {
					processedRecords = sprintResponseJson.getAsJsonArray("results").get(0).getAsJsonObject()
							.getAsJsonArray("data").get(0).getAsJsonObject().getAsJsonArray("row").get(0).getAsInt();
				} catch (UnsupportedOperationException | IllegalStateException | IndexOutOfBoundsException ex) {
					log.error(cypherQuery + "  - query processing failed", ex);
					return Boolean.FALSE; 
				}
				log.debug(" Processed " + processedRecords);
				recordCount = recordCount + processedRecords;
			}
			long queryExecutionEndTime = System.currentTimeMillis();
			long queryProcessingTime = (queryExecutionEndTime - queryExecutionStartTime);
			if (dataEnrichmentModel != null) {
				dataEnrichmentModel.setRecordsProcessed(recordCount);
				dataEnrichmentModel.setQueryProcessingTime(queryProcessingTime);
			}
		} catch (InsightsCustomException e) {
			log.error(cypherQuery + " - query processing failed", e);
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	/**
	 * 
	 * Checks whether query is scheduled to run or not depending on runSchedule
	 * and lastRunTime
	 * 
	 * @param runSchedule
	 * @param lastRunTime
	 * @return
	 */
	public Boolean isQueryScheduledToRun(Long runSchedule, String lastRunTime, String cronSchedule) {
		// if lastExecutionTime property is not added in the json file, we'll
		// execute the query by default
		if (lastRunTime == null && (cronSchedule == null || cronSchedule.trim().length() == 0)) {
			return Boolean.TRUE;
		}
		ZonedDateTime dateTime = null;
		ZonedDateTime now = ZonedDateTime.now(InsightsUtils.zoneId);
		Long timeDifferenceInMinutes = null;
		if (lastRunTime != null && !lastRunTime.isEmpty()) {
			dateTime = ZonedDateTime.parse(lastRunTime, formatter);
		}
		if(cronSchedule != null && cronSchedule.trim().length() > 0) {
			try {
				//"0 0 0 1 * ?"
				CronExpression convert = new CronExpressionConverter().convert(cronSchedule);
				if(dateTime == null) {
					dateTime = now.minusDays(1); //If the last run time not present, compare the cron time against last 24 hours.
				}
				Date cronDate = convert.getNextValidTimeAfter(Date.from(dateTime.toInstant()));
				if(cronDate.before(new Date())) {
					return Boolean.TRUE;
				}
			} catch (Exception e) {
				log.error("Unable to parse the CRON expression: "+cronSchedule, e);
			}
		}else {
			if (dateTime != null && now != null) {
				Duration d = Duration.between(dateTime, now);
				timeDifferenceInMinutes = d.abs().toMinutes();
			}
			if (timeDifferenceInMinutes > runSchedule) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}
}
