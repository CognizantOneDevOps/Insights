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
package com.cognizant.devops.platformengine.modules.datapurging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.util.DataPurgingUtils;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.dal.elasticsearch.ElasticSearchDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.settingsconfig.SettingsConfigurationDAL;
import com.cognizant.devops.platformengine.message.core.EngineStatusLogger;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class DataPurgingExecutor implements Job {

	private static Logger log = LogManager.getLogger(DataPurgingExecutor.class.getName());
	private static final String DATE_TIME_FORMAT = "yyyy/MM/dd hh:mm a";
	private static final double MAXIMUM_BACKUP_FILE_SIZE = 5.0000d;

	public void execute(JobExecutionContext context) throws JobExecutionException {
		if (ApplicationConfigProvider.getInstance().isEnableOnlineBackup() && checkDataPurgingJobSchedule()) {
			performDataPurging();
			EngineStatusLogger.getInstance().createEngineStatusNode("Data Purginig completed",
					PlatformServiceConstants.SUCCESS);
		}
		if (ApplicationConfigProvider.getInstance().getQueryCache().getPurgeEsResultsBefore() > 0) {
			purgeElasticSearchQueryCaching();
			EngineStatusLogger.getInstance().createEngineStatusNode("Query Cache - ES Data Purginig completed",
					PlatformServiceConstants.SUCCESS);
		}

	}

	public void performDataPurging() {
		String rowLimit = null;
		String backupFileLocation = null;
		long backupDurationInDays = 0;
		// Internally defined backup file name prefix
		String backupFilePrefix = "neo4jDataArchive";

		/**
		 * To get Settings Configuration which is set by User from Insights application
		 * UI is stored into Settings_Configuration table of PostGres database
		 */
		JsonObject configJsonObj = getSettingsJsonObject();
		if (configJsonObj != null) {
			rowLimit = configJsonObj.get(ConfigOptions.ROW_LIMIT).getAsString();
			backupFileLocation = configJsonObj.get(ConfigOptions.BACKUP_FILE_LOCATION).getAsString();
			backupDurationInDays = configJsonObj.get(ConfigOptions.BACKUP_DURATION_IN_DAYS).getAsLong();
		}
		boolean deleteFlag = true;
		List<GraphResponse> responseList = new ArrayList<>();
		// Converts into epoch time in seconds as data inside inSightsTime property is
		// stored in epoch seconds
		long epochTime = InsightsUtils.getTimeBeforeDaysInSeconds(backupDurationInDays);
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		try {
			responseList = getAllOrphanNodesInfo(responseList, rowLimit, epochTime, dbHandler);
			responseList = getNodesRelationshipsInfo(responseList, rowLimit, epochTime, dbHandler);

			String localDateTime = InsightsUtils.getLocalDateTime("yyyyMMddHHmmss");
			String fileLocation = backupFileLocation + File.separator + backupFilePrefix + "_" + localDateTime;
			writeToJsonFile(responseList, fileLocation);

		} catch (GraphDBException e) {
			log.error("Exception occured while selecting matching records for a specific data rentention period:" + e);
			deleteFlag = false;
		} catch (IOException e) {
			log.error("Exception occured while taking backup of data in DataPurgingExecutor Job: " + e);
			EngineStatusLogger.getInstance().createEngineStatusNode(
					" Error occured while executing DataPurgingExecutor " + e.getMessage(),
					PlatformServiceConstants.FAILURE);
			deleteFlag = false;
		}
		// delete all nodes along with its relationships for which data backup is
		// already taken
		if (deleteFlag) {
			String deleteQry = "MATCH (n:DATA) WHERE n.inSightsTime <" + epochTime + " detach delete n ";
			try {
				dbHandler.executeCypherQuery(deleteQry);

			} catch (GraphDBException e) {
				log.error(
						"Exception occured while deleting DATA nodes of Neo4j database inside DataPurgingExecutor Job: "
								+ e);
				EngineStatusLogger.getInstance().createEngineStatusNode(
						" Error occured while executing DataPurgingExecutor " + e.getMessage(),
						PlatformServiceConstants.FAILURE);
			}
		}

		try {
			// Update lastRunTime and nextRunTine into the database as per
			// dataArchivalFrequency
			updateRunTimeIntoDatabase();
		} catch (InsightsCustomException e) {
			log.error("Exception occured while updating lastRunTime and nextRunTime in DataPurgingExecutor Job: " + e);
			EngineStatusLogger.getInstance().createEngineStatusNode(
					" Error occured while executing DataPurgingExecutor " + e.getMessage(),
					PlatformServiceConstants.FAILURE);
		}
	}

	public void purgeElasticSearchQueryCaching() {
		try {
			String query = getEsPurgeQuery();
			ElasticSearchDBHandler esDbHandler = new ElasticSearchDBHandler();

			Long time = InsightsUtils.getTimeBeforeDaysInSeconds(
					new Long(ApplicationConfigProvider.getInstance().getQueryCache().getPurgeEsResultsBefore()));
			String esCacheIndex = ApplicationConfigProvider.getInstance().getQueryCache().getEsCacheIndex();
			if (esCacheIndex == null)
				esCacheIndex = "neo4j-cached-results/querycacheresults";
			else
				esCacheIndex = esCacheIndex + "/querycacheresults";
			String sourceESCacheUrl = ApplicationConfigProvider.getInstance().getEndpointData()
					.getElasticSearchEndpoint() + "/" + esCacheIndex + "/_delete_by_query";
			query = replaceTimeInEsQuery(query, time);
			JsonObject data = esDbHandler.queryES(sourceESCacheUrl, query);
			log.debug("Number of Query Cache results deleted: " + data.get("deleted").getAsString());
		} catch (Exception e) {
			log.error("Exception occured while clearing Query Cache data from Elasticsearch: " + e);
			EngineStatusLogger.getInstance().createEngineStatusNode(
					"Query Cache - Error occured while executing ES purging " + e.getMessage(),
					PlatformServiceConstants.FAILURE);
		}
	}

	public String replaceTimeInEsQuery(String query, Long time) {
		query = query.replace("__timeBeforeDays__", time.toString());
		return query;
	}

	private String getEsPurgeQuery() {
		return "{\"query\":{\"bool\":{\"must\":[{\"bool\":"
				+ "{\"must\":[{\"range\":{\"creationTime\":{\"lte\":\"__timeBeforeDays__\","
				+ "\"format\":\"epoch_millis\"}}}]}}]}}}";
	}

	/**
	 * Fetches all orphan nodes info along with its labels, properties
	 * 
	 * @param resultList
	 * @param rowLimit
	 * @param epochTime
	 * @param dbHandler
	 * @return
	 * @throws GraphDBException
	 */
	private List<GraphResponse> getAllOrphanNodesInfo(List<GraphResponse> resultList, String rowLimit, long epochTime,
			Neo4jDBHandler dbHandler) throws GraphDBException {
		GraphResponse response = null;
		int splitlength = 0;
		try {
			int count = getOrphanNodeCount(dbHandler, epochTime);
			while (splitlength < count) {
				response = getOrphanNodesInfo(rowLimit, splitlength, epochTime);
				resultList.add(response);
				splitlength = splitlength + Integer.parseInt(rowLimit);

			}
		} catch (GraphDBException e) {
			log.error(
					"Exception occured while getting information of all orphan data nodes in DataPurgingExecutor Job: "
							+ e);
		}
		return resultList;
	}

	/**
	 * Fetches all nodes along with its relationships, start node, end node and
	 * properties info
	 * 
	 * @param resultList
	 * @param rowLimit
	 * @param epochTime
	 * @param dbHandler
	 * @return
	 * @throws GraphDBException
	 */
	private List<GraphResponse> getNodesRelationshipsInfo(List<GraphResponse> resultList, String rowLimit,
			long epochTime, Neo4jDBHandler dbHandler) throws GraphDBException {
		GraphResponse response = null;
		int splitlength = 0;
		try {
			int count = getNodesWithRelationshipsCount(dbHandler, epochTime);
			while (splitlength < count) {
				response = getNodesWithRelationshipInfo(rowLimit, splitlength, epochTime);
				resultList.add(response);
				splitlength = splitlength + Integer.parseInt(rowLimit);

			}
		} catch (GraphDBException e) {
			log.error(
					"Exception occured while getting information of all nodes with relationships in DataPurgingExecutor Job:"
							+ e);
			throw e;
		}
		return resultList;
	}

	private int getOrphanNodeCount(Neo4jDBHandler dbHandler, long epochTime) throws GraphDBException {
		String cntQry = "MATCH (n:DATA) WHERE not (n)-[]-() and n.inSightsTime<" + epochTime + " return count(*)";
		GraphResponse cntResponse = dbHandler.executeCypherQuery(cntQry);
		return cntResponse.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
				.getAsJsonArray().get(0).getAsJsonObject().get("row").getAsInt();
	}

	private int getNodesWithRelationshipsCount(Neo4jDBHandler dbHandler, long epochTime) throws GraphDBException {
		String cntQry = "MATCH (n:DATA)-[r]->(m:DATA) where n.inSightsTime<" + epochTime + " return count(*)";
		GraphResponse cntResponse = dbHandler.executeCypherQuery(cntQry);
		return cntResponse.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
				.getAsJsonArray().get(0).getAsJsonObject().get("row").getAsInt();
	}

	private GraphResponse getOrphanNodesInfo(String limit, int splitlength, long epochTime) throws GraphDBException {
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		String query = "MATCH (n:DATA) WHERE not(n)-[]-() and n.inSightsTime<" + epochTime + " return n skip "
				+ splitlength + " limit " + limit;
		return dbHandler.executeCypherQuery(query);
	}

	private GraphResponse getNodesWithRelationshipInfo(String limit, int splitlength, long epochTime)
			throws GraphDBException {
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		String query = "MATCH (n:DATA)-[r]->(m:DATA) where n.inSightsTime<" + epochTime + " return distinct n,r,m skip "
				+ splitlength + " limit " + limit;
		return dbHandler.executeCypherQuery(query);
	}

	/**
	 * 
	 * Reads data from GraphResponse and writes into a json file in the file system
	 * as per desired output
	 * 
	 * @param response
	 * @param location
	 * @throws IOException
	 */

	private void writeToJsonFile(List<GraphResponse> responseList, String fileLocation) throws IOException {
		JsonObject graphObj = new JsonObject();
		JsonArray nodeArray = new JsonArray();
		JsonArray relArray = new JsonArray();

		JsonObject resultObj = new JsonObject();
		resultObj.add("graph", graphObj);
		graphObj.add("nodes", nodeArray);
		graphObj.add("relationships", relArray);
		int noOfFiles = 1;
		StringBuilder sb = new StringBuilder();
		for (GraphResponse response : responseList) {
			JsonArray array = response.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
					.getAsJsonArray();
			for (JsonElement element : array) {
				JsonObject jsonObject = element.getAsJsonObject().get("graph").getAsJsonObject();
				JsonArray nodes = jsonObject.get("nodes").getAsJsonArray();
				for (JsonElement node : nodes) {
					nodeArray.add(node);
				}
				JsonArray rel = jsonObject.get("relationships").getAsJsonArray();
				if (rel.size() > 0) {
					relArray.add(rel.get(0));
				}
			}
			sb = new StringBuilder();
			sb.append(resultObj.toString());
			String outputString = sb.toString();
			double dataSizeinMB = getOutputDataSize(outputString);
			if (dataSizeinMB > MAXIMUM_BACKUP_FILE_SIZE) {
				String jsonFileLocation = fileLocation + "_" + noOfFiles + ".json";
				try (FileWriter fileWriter = new FileWriter(jsonFileLocation);) {
					fileWriter.write(outputString);
					fileWriter.flush();
				} catch (IOException e) {
					log.error("Error in writeToJsonFile method" + e);
					throw e;
				}
				noOfFiles++;
				graphObj = new JsonObject();
				nodeArray = new JsonArray();
				relArray = new JsonArray();
				resultObj = new JsonObject();
				resultObj.add("graph", graphObj);
				graphObj.add("nodes", nodeArray);
				graphObj.add("relationships", relArray);
			}
		}
		String outputString = sb.toString();
		double dataSizeinMB = getOutputDataSize(outputString);
		if (!outputString.isEmpty() && dataSizeinMB < MAXIMUM_BACKUP_FILE_SIZE) {
			String jsonFileLocation = fileLocation + "_" + noOfFiles + ".json";
			try (FileWriter fileWriter = new FileWriter(jsonFileLocation);) {
				fileWriter.write(outputString);
				fileWriter.flush();
			} catch (IOException e) {
				log.error("Error in writeToJsonFile method" + e);
				throw e;
			}
		}
	}

	/**
	 * Return size of output data in MB
	 * 
	 * @param outputData
	 * @return
	 */
	private double getOutputDataSize(String outputData) {
		double dataSizeinKB = (double) outputData.getBytes().length / 1024;
		return (dataSizeinKB / 1024);
	}

	/**
	 * Reads data from GraphResponse and writes into a csv file in the file system
	 * as per desired output
	 * 
	 * @param response
	 * @param location
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	private void writeToCSVFile(GraphResponse response, String location) throws IOException {
		String csvFileLocation = location + ".csv";
		JsonArray array = response.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
				.getAsJsonArray();
		Map<String, Integer> headerMap = new HashMap<>();
		List<ArrayList<String>> valueStore = new ArrayList<>();
		ArrayList<String> headerList = new ArrayList<>();
		int rowCount = 0;
		for (JsonElement element : array) {
			int counter = 0;
			ArrayListAnySize<String> valueList = new ArrayListAnySize<>();
			JsonElement jsonElement = element.getAsJsonObject().get("row").getAsJsonArray().get(0);
			Set<Map.Entry<String, JsonElement>> entries = jsonElement.getAsJsonObject().entrySet();// will return
																									// members of your
																									// object
			for (Map.Entry<String, JsonElement> entry : entries) {
				String key = entry.getKey();
				if (rowCount > 0) {
					if (!headerMap.containsKey(key)) {
						headerMap.put(key, headerMap.size());
						counter++;
						headerList.add(key);
					}
				} else {
					headerMap.put(key, counter++);
				}

				int columnIndex = headerMap.get(key);
				String value = entry.getValue().getAsString();
				if (columnIndex < valueList.size() && valueList.get(columnIndex) == null) {
					valueList.set(columnIndex, value);
				} else {
					valueList.add(columnIndex, value);
				}

				//
				if (rowCount == 0) {
					headerList.add(key);
				}
			}
			valueStore.add(rowCount, valueList);
			rowCount++;
		}
		valueStore.add(0, headerList);
		CSVFormat csvFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
		FileWriter fWriter = new FileWriter(csvFileLocation);
		try (CSVPrinter csvPrinter = new CSVPrinter(fWriter, csvFormat);) {
			for (ArrayList<String> values : valueStore) {
				while (headerList.size() > values.size()) {
					values.add(null);
				}
				csvPrinter.printRecord(values);
			}
		} catch (IOException e) {
			log.error("Error in writeToCSV method" + e);
			throw e;
		}
	}

	/**
	 * Loads SettingConfiguration detail from database for DataPurging setting type
	 * and returns settingJson string and converts into jsonobject
	 * 
	 * @return JsonObject
	 */
	private JsonObject getSettingsJsonObject() {
		SettingsConfigurationDAL settingsConfigurationDAL = new SettingsConfigurationDAL();
		String settingsJson = settingsConfigurationDAL.getSettingsJsonObject(ConfigOptions.DATAPURGING_SETTINGS_TYPE);
		if (settingsJson != null && !settingsJson.isEmpty()) {
			Gson gson = new Gson();
			JsonElement jsonElement = gson.fromJson(settingsJson.trim(), JsonElement.class);
			return jsonElement.getAsJsonObject();
		}
		return null;
	}

	/**
	 * Checks whether DataPurging job should be run or not as per nextRunTime
	 * 
	 * @return
	 */
	private Boolean checkDataPurgingJobSchedule() {
		JsonObject settingsJsonObject = getSettingsJsonObject();
		String lastRunTimeStr = DataPurgingUtils.getLastRunTime(settingsJsonObject);
		String nextRunTimeStr = DataPurgingUtils.getNextRunTime(settingsJsonObject);
		Long lastRunTime = parseDateIntoEpochSeconds(lastRunTimeStr);
		Long nextRunTime = parseDateIntoEpochSeconds(nextRunTimeStr);
		Long x = InsightsUtils.getDifferenceFromLastRunTime(lastRunTime);
		Long y = InsightsUtils.getDifferenceFromNextRunTime(lastRunTime, nextRunTime);
		if (x > y) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	/**
	 * This method parses a date represented in String into ZonedDateTime and
	 * converts it into EpochSecond
	 * 
	 * @param inputDate
	 * @return
	 */
	private long parseDateIntoEpochSeconds(String inputDate) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT).withZone(InsightsUtils.zoneId);
		ZonedDateTime dateTime = null;
		if (inputDate != null && !inputDate.isEmpty()) {
			dateTime = ZonedDateTime.parse(inputDate, formatter);
		}
		if (dateTime != null) {
			return dateTime.toEpochSecond();
		}
		return 0L;
	}

	/**
	 * Updates lastRunTime in db with current date time, Calculates nextRunTime as
	 * per dataArchivalFrequency, Updates nextRunTime into the database
	 * 
	 * @throws InsightsCustomException
	 */
	private void updateRunTimeIntoDatabase() throws InsightsCustomException {
		JsonObject settingsJsonObject = getSettingsJsonObject();
		String dataArchivalFrequency = DataPurgingUtils.getDataArchivalFrequency(settingsJsonObject);
		// Captures current date time to update lastRunTime
		String lastRunTime = InsightsUtils.getLocalDateTime(DATE_TIME_FORMAT);
		String nextRunTime = DataPurgingUtils.calculateNextRunTime(dataArchivalFrequency);
		settingsJsonObject = DataPurgingUtils.updateLastRunTime(settingsJsonObject, lastRunTime);
		settingsJsonObject = DataPurgingUtils.updateNextRunTime(settingsJsonObject, nextRunTime);
		String modifiedSettingsJson = null;
		if (settingsJsonObject != null) {
			modifiedSettingsJson = settingsJsonObject.toString();
		}
		if (modifiedSettingsJson != null && !modifiedSettingsJson.isEmpty()) {
			SettingsConfigurationDAL settingsConfigurationDAL = new SettingsConfigurationDAL();
			settingsConfigurationDAL.updateSettingJson(modifiedSettingsJson);
		}
	}

}
