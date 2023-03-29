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

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.convert.TypeConverters.CronExpressionConverter;
import org.apache.logging.log4j.core.util.CronExpression;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.cognizant.devops.engines.platformengine.message.core.EngineStatusLogger;
import com.cognizant.devops.engines.platformengine.modules.offlinedataprocessing.model.DataEnrichmentModel;
import com.cognizant.devops.platformcommons.config.ApplicationConfigInterface;
import com.cognizant.devops.platformcommons.constants.EngineConstants;
import com.cognizant.devops.platformcommons.constants.MilestoneConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.enums.FileDetailsEnum;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFiles;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFilesDAL;
import com.cognizant.devops.platformdal.offlineDataProcessing.InsightsOfflineConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

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
public class OfflineDataProcessingExecutor implements Job, ApplicationConfigInterface {
	
	private static Logger log = LogManager.getLogger(OfflineDataProcessingExecutor.class);
	private static final String DATE_TIME_FORMAT = "yyyy/MM/dd hh:mm a";
	private static final String JSON_FILE_EXTENSION = "json";
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT).withZone(InsightsUtils.zoneId);
	InsightsConfigFilesDAL configFilesDAL = new InsightsConfigFilesDAL();
	OfflineDataProcessingFromDB offlineDataProcessingDB = new OfflineDataProcessingFromDB();
	private Map<String,String> loggingInfo = new ConcurrentHashMap<>();
	String jobName="";
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		long startTime =System.currentTimeMillis();
		try {
			jobName=context.getJobDetail().getKey().getName();
			EngineStatusLogger.getInstance().createSchedularTaskStatusNode("OfflineDataProcessingExecutor execution Start ",
					PlatformServiceConstants.SUCCESS,jobName);
			ApplicationConfigInterface.loadConfiguration();
			loggingInfo.put(MilestoneConstants.EXECID, String.valueOf(System.currentTimeMillis()));
			List<InsightsOfflineConfig> offlineDataConfigList = offlineDataProcessingDB.getAllActiveOfflineDataConfigFromDB();
			if (offlineDataConfigList != null &&  !offlineDataConfigList.isEmpty()) {
				offlineDataProcessingDB.processOfflineConfigurationFromDB(offlineDataConfigList, loggingInfo);
			} else {
				executeOfflineProcessing();
			}
			log.debug(" Type=OfflineDataProcessing execId={} offlineProcessingFileName={} queryName={} ProcessingTime={} processedRecords={} Offline Data Processing completed",loggingInfo.get(MilestoneConstants.EXECID),"-","-",0,0);
		} catch (Exception e) {
			log.error("Offline Data Procesing has some issue",e);
			EngineStatusLogger.getInstance().createSchedularTaskStatusNode("Offline Data Procesing has some issue",
					PlatformServiceConstants.FAILURE,jobName);
		}
		
		long processingTime = System.currentTimeMillis() - startTime ;
		EngineStatusLogger.getInstance().createSchedularTaskStatusNode("OfflineDataProcessingExecutor execution completed ",
				PlatformServiceConstants.SUCCESS,jobName,processingTime);
	}

	public int executeOfflineProcessing() {
		List<InsightsConfigFiles> configFile = configFilesDAL
				.getAllConfigurationFilesForModule(FileDetailsEnum.FileModule.DATAENRICHMENT.name());
		int jsonFileCount = 0;		
		if (configFile==null) {
			return jsonFileCount;
		}	
		for (InsightsConfigFiles eachFile : configFile) {
				if (eachFile.getFileType().equalsIgnoreCase(FileDetailsEnum.ConfigurationFileType.JSON.name())) {
					jsonFileCount++;
					loggingInfo.put(EngineConstants.FILENAME,eachFile.getFileName());
					processOfflineConfiguration(eachFile);
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
	public boolean hasJsonFileExtension(String fileName) {
		if (fileName != null && !fileName.isEmpty()) {
			String extension = FilenameUtils.getExtension(fileName);
			if (JSON_FILE_EXTENSION.equalsIgnoreCase(extension)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Processes each  configuration file
	 * Processes each query block inside each configuration file and executes cypher query
	 * @param jsonFile
	 */
	
	public boolean processOfflineConfiguration(InsightsConfigFiles jsonFile) {
		try {
				String configFileData = new String(jsonFile.getFileData(), StandardCharsets.UTF_8);
				List<DataEnrichmentModel> dataEnrichmentModels  = Arrays.asList(new Gson().fromJson(configFileData,
						DataEnrichmentModel[].class));
				for (DataEnrichmentModel dataEnrichmentModel : dataEnrichmentModels) {
					String cypherQuery = dataEnrichmentModel.getCypherQuery();
					loggingInfo.put(EngineConstants.QUERYNAME, dataEnrichmentModel.getQueryName().trim().replace(" ", "_"));
					log.debug(" Type=OfflineDataProcessing execId={} offlineProcessingFileName={} queryName={} ProcessingTime={} processedRecords={} Cypher query : {} ",loggingInfo.get(MilestoneConstants.EXECID),loggingInfo.get(EngineConstants.FILENAME),loggingInfo.get(EngineConstants.QUERYNAME),0,0,cypherQuery);
					Long runSchedule = dataEnrichmentModel.getRunSchedule();
					if (cypherQuery == null || cypherQuery.isEmpty() || runSchedule == null )  {
						log.error(" Type=OfflineDataProcessing execId={} offlineProcessingFileName={} queryName={} {} doesn't have either cypherQuery or runSchedule attribute.",loggingInfo.get(MilestoneConstants.EXECID),loggingInfo.get(EngineConstants.FILENAME),loggingInfo.get(EngineConstants.QUERYNAME),dataEnrichmentModel.getQueryName());
						continue;
					}
					if (isQueryScheduledToRun(dataEnrichmentModel.getRunSchedule(),
							dataEnrichmentModel.getLastExecutionTime(), dataEnrichmentModel.getCronSchedule())) {
						boolean successFlag = executeCypherQuery(cypherQuery, dataEnrichmentModel);
						//Checks if query execution fails due to some exception, don't update lastExecutionTime 
						if (successFlag) {
							updateLastExecutionTime(dataEnrichmentModel);							
						}
					}
				}
				jsonFile.setFileData(new Gson().toJson(dataEnrichmentModels).getBytes());
				configFilesDAL.updateConfigurationFile(jsonFile);
		} catch (IllegalStateException | JsonSyntaxException  ex) {
			log.error(" Type=OfflineDataProcessing execId={} offlineProcessingFileName={} queryName={} {} file is not as per expected format ",loggingInfo.get(MilestoneConstants.EXECID),loggingInfo.get(EngineConstants.FILENAME),loggingInfo.get(EngineConstants.QUERYNAME),jsonFile.getFileName(), ex);
			return false;
		} catch (Exception e) {
			log.error(" Type=OfflineDataProcessing execId={} offlineProcessingFileName={} queryName={} {} error while loading file ",loggingInfo.get(MilestoneConstants.EXECID),loggingInfo.get(EngineConstants.FILENAME),loggingInfo.get(EngineConstants.QUERYNAME),jsonFile.getFileName(), e);
			return false;
		}
		return true;
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
	public boolean executeCypherQuery(String cypherQuery, DataEnrichmentModel dataEnrichmentModel) {
		GraphDBHandler dbHandler = new GraphDBHandler();
		int processedRecords = 1;
		int recordCount = 0;
		long queryExecutionStartTime = System.currentTimeMillis();
		try {
			while (processedRecords > 0) {
				GraphResponse sprintResponse = dbHandler.executeCypherQuery(cypherQuery);
				JsonObject sprintResponseJson = sprintResponse.getJson();
				processedRecords = sprintResponseJson.getAsJsonArray("results").get(0).getAsJsonObject()
						.getAsJsonArray("data").get(0).getAsJsonObject().getAsJsonArray("row").get(0).getAsInt();
				log.debug(" Type=OfflineDataProcessing execId={} offlineProcessingFileName={} queryName={} ProcessingTime={} processedRecords={} Processed Records = {}",loggingInfo.get(MilestoneConstants.EXECID),loggingInfo.get(EngineConstants.FILENAME),loggingInfo.get(EngineConstants.QUERYNAME),0, processedRecords,processedRecords);
				recordCount = recordCount + processedRecords;
			}
			long queryExecutionEndTime = System.currentTimeMillis();
			long queryProcessingTime = (queryExecutionEndTime - queryExecutionStartTime);
			if (dataEnrichmentModel != null) {
				dataEnrichmentModel.setRecordsProcessed(recordCount);
				dataEnrichmentModel.setQueryProcessingTime(queryProcessingTime);
			}
			log.debug(" Type=OfflineDataProcessing execId={} offlineProcessingFileName={} queryName={} ProcessingTime={} processedRecords={} Offline Query processed records={} ",loggingInfo.get(MilestoneConstants.EXECID),loggingInfo.get(EngineConstants.FILENAME),loggingInfo.get(EngineConstants.QUERYNAME), queryProcessingTime,processedRecords,processedRecords);
		} catch (UnsupportedOperationException | IllegalStateException | IndexOutOfBoundsException | InsightsCustomException ex) {
			log.error(" Type=OfflineDataProcessing execId={} offlineProcessingFileName={} queryName={} {} - query processing failed",loggingInfo.get(MilestoneConstants.EXECID),loggingInfo.get(EngineConstants.FILENAME),loggingInfo.get(EngineConstants.QUERYNAME),cypherQuery, ex);
			return false;
		} 
		return true;
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
	public boolean isQueryScheduledToRun(Long runSchedule, String lastRunTime, String cronSchedule) {
		// if lastExecutionTime property is not added in the json file, we'll
		// execute the query by default
		if (lastRunTime == null && (cronSchedule == null || cronSchedule.trim().length() == 0)) {
			return true;
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
					return true;
				}
			} catch (Exception e) {
				log.error(" Type=OfflineDataProcessing execId={} offlineProcessingFileName={} queryName={} Unable to parse the CRON expression:{} ",loggingInfo.get(MilestoneConstants.EXECID),loggingInfo.get(EngineConstants.FILENAME),loggingInfo.get(EngineConstants.QUERYNAME),cronSchedule, e);
			}
		}else {
		
			return isScheduled(dateTime,now,runSchedule);		

		}
		return false;
	}
	
	
	private boolean isScheduled(ZonedDateTime dateTime, ZonedDateTime now, Long runSchedule) {
		Long timeDifferenceInMinutes = null;
		
		if (dateTime != null && now != null) {
			Duration d = Duration.between(dateTime, now);
			timeDifferenceInMinutes = d.abs().toMinutes();
		}
		if (timeDifferenceInMinutes > runSchedule) {
			return true;
		}
		
		return false;
		
	}

}
