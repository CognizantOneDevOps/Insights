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
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.core.util.DataPurgingUtils;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformdal.settingsconfig.SettingsConfigurationDAL;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class DataPurgingExecutor implements Job {


	private static Logger log = Logger.getLogger(DataPurgingExecutor.class.getName());
	private static final String DATE_TIME_FORMAT = "yyyy/MM/dd hh:mm a";

	public void execute(JobExecutionContext context) throws JobExecutionException {
		if (ApplicationConfigProvider.getInstance().isEnableOnlineBackup()) {
			if(checkDataPurgingJobSchedule()) {
				performDataPurging();		
			}
		} 
	}


	public void performDataPurging()   {
		String rowLimit = null ;
		String backupFileLocation = null ;
		long backupDurationInDays = 0;
		String backupFileFormat = null;
		//Internally defined backup file name prefix
		String backupFilePrefix = "neo4jDataArchive";
		boolean isCsvFormat = false;

		/**
		 * To get Settings Configuration which is set by User from Insights application UI
		 * is stored into Settings_Configuration table of PostGres database
		 */
		JsonObject configJsonObj = getSettingsJsonObject(); 
		if (configJsonObj != null) {
			rowLimit = configJsonObj.get(ConfigOptions.ROW_LIMIT).getAsString();
			backupFileLocation = configJsonObj.get(ConfigOptions.BACKUP_FILE_LOCATION).getAsString();	
			backupFileFormat = configJsonObj.get(ConfigOptions.BACKUP_FILE_FORMAT).getAsString();
			backupDurationInDays = configJsonObj.get(ConfigOptions.BACKUP_DURATION_IN_DAYS).getAsLong();
		}

		if (ConfigOptions.CSV_FORMAT.equals(backupFileFormat)) {
			isCsvFormat = true;
		} else if (ConfigOptions.JSON_FORMAT.equals(backupFileFormat)) {
			isCsvFormat = false;	
		}

		//Converts into epoch time in seconds as data inside inSightsTime property is stored in epoch seconds  
		long epochTime = InsightsUtils.getTimeBeforeDaysInSeconds(backupDurationInDays);
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		GraphResponse response = null;
		int splitlength = 0;
		boolean successFlag = false;
		try {
			int count = getNodeCount(dbHandler,epochTime);
			while(splitlength  < count){
				boolean deleteFlag = true;
				try{
					response = executeCypherQuery(rowLimit,splitlength,epochTime) ;
				}
				catch (GraphDBException e){
					log.error("Exception occured while selecting matching records for a specific data rentention period:" + e);
					deleteFlag = false;
				}
				String localDateTime = InsightsUtils.getLocalDateTime("yyyyMMddHHmmss");
				String location = backupFileLocation + File.separator + backupFilePrefix + "_" + splitlength + "_" + localDateTime; 
				try{
					if (isCsvFormat) {
						writeToCSVFile(response,location);
					} else {
						writeToJsonFile(response,location);	
					}
				}
				catch (IOException e) {
					log.error("Exception occured while taking backup of data in DataPurgingExecutor Job: " + e);
					deleteFlag = false;
				}

				//delete call with modified query
				if(deleteFlag){
					String deleteQry = "MATCH (n:DATA) where n.inSightsTime < "+ epochTime  +" with n skip "+ splitlength
												+" limit " + rowLimit + " detach delete n ";
					try {
						dbHandler.executeCypherQuery(deleteQry);

					} catch (GraphDBException e) {
						log.error("Exception occured while deleting DATA nodes of Neo4j database inside DataPurgingExecutor Job: " + e);
					}
				}
				splitlength = splitlength + Integer.parseInt(rowLimit);
				successFlag = successFlag || deleteFlag;
			}
		} 
		catch (GraphDBException e) {
			log.error("Exception occured while getting total node count of all data nodes in DataPurgingExecutor Job: " + e);
		}
		if(successFlag){
			// Update lastRunTime and nextRunTine into the database as per dataArchivalFrequency
			updateRunTimeIntoDatabase();
		}
	}

	private int getNodeCount(Neo4jDBHandler dbHandler,long epochTime) throws GraphDBException {
		String cntQry = "MATCH (n:DATA)  where n.inSightsTime < "+ epochTime  +" return count(n) ";
		GraphResponse cntResponse = dbHandler.executeCypherQuery(cntQry);
		int count = cntResponse.getJson() .get("results").getAsJsonArray().get(0).getAsJsonObject().get("data").getAsJsonArray()
				.get(0).getAsJsonObject().get("row").getAsInt();
		return count;
	}

	private GraphResponse executeCypherQuery(String limit, int splitlength, long epochTime) throws GraphDBException {
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		String query = "MATCH (n:DATA)  where n.inSightsTime < "+ epochTime  +"   return n skip  "+ splitlength +" limit  " +limit;
		GraphResponse response = dbHandler.executeCypherQuery(query);
		return response;
	}

	/**
	 * 
	 * Reads data from GraphResponse and
	 * writes into a json file in the file system as per desired output
	 * @param response
	 * @param location
	 * @throws IOException
	 */
	private void writeToJsonFile(GraphResponse response, String location) throws IOException {
		String jsonFileLocation = location + ".json";
		JsonArray array = response.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data").getAsJsonArray();
		StringBuilder sb = new StringBuilder();
		for(JsonElement element : array) {
			JsonElement jsonElement = element.getAsJsonObject().get("row").getAsJsonArray().get(0);
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			sb.append(jsonObject.toString());
			sb.append(",");
		}
		String outputString = sb.toString();
		//Removes last appended ',' from output string
		outputString = outputString.substring(0,outputString.length()-1);
		//Adds entire output string inside [] bracket
		outputString = "[" + outputString +"]";
		FileWriter fileWriter = new FileWriter(jsonFileLocation);
		fileWriter.write(outputString);
		fileWriter.flush();
		fileWriter.close();
	}


	/**
	 * Reads data from GraphResponse and
	 * writes into a csv file in the file system as per desired output
	 * @param response
	 * @param location
	 * @throws IOException
	 */
	private void writeToCSVFile(GraphResponse response, String location) throws IOException {
		String csvFileLocation = location + ".csv";
		JsonArray array = response.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data").getAsJsonArray();
		Map<String,Integer> headerMap = new HashMap<>();
		List<ArrayList<String>> valueStore = new ArrayList<>();
		ArrayList<String> headerList = new ArrayList<>();
		int rowCount = 0;
		for(JsonElement element : array) {
			int counter = 0;
			ArrayListAnySize<String> valueList = new ArrayListAnySize<>();
			JsonElement jsonElement = element.getAsJsonObject().get("row").getAsJsonArray().get(0);
			Set<Map.Entry<String, JsonElement>> entries = jsonElement.getAsJsonObject().entrySet();//will return members of your object
			for (Map.Entry<String, JsonElement> entry: entries) {
				String key = entry.getKey();
				if (rowCount > 0){
					if(!headerMap.containsKey(key)) {
						headerMap.put(key, headerMap.size());
						counter++;
						headerList.add(key);
					}					
				} else {					
					headerMap.put(key, counter++);
				}

				int columnIndex = headerMap.get(key);
				String value = entry.getValue().getAsString();
				if (columnIndex < valueList.size() && valueList.get(columnIndex)== null) {
					valueList.set(columnIndex,value); 
				} else {
					valueList.add(columnIndex, value);				   
				}

				//
				if (rowCount == 0) {
					headerList.add(key);
				}			    
			}			
			valueStore.add(rowCount , valueList);
			rowCount++;
		}
		valueStore.add(0 , headerList);		
		CSVFormat csvFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
		FileWriter fWriter = new FileWriter(csvFileLocation);
		try(CSVPrinter csvPrinter = new CSVPrinter(fWriter, csvFormat);) {			
			for (ArrayList<String> values: valueStore) {
				while (headerList.size()> values.size()) {
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
	 * @return JsonObject
	 */
	public JsonObject getSettingsJsonObject() {
		SettingsConfigurationDAL settingsConfigurationDAL = new SettingsConfigurationDAL();	
		String settingsJson = settingsConfigurationDAL.getSettingsJsonObject(ConfigOptions.DATAPURGING_SETTINGS_TYPE);
		if (settingsJson != null && !settingsJson.isEmpty()) {
			Gson gson = new Gson();
			JsonElement jsonElement = gson.fromJson(settingsJson.trim(),JsonElement.class);
			return jsonElement.getAsJsonObject();
		}
		return null;
	}

	/**
	 * Checks whether DataPurging job should be run or not as per nextRunTime
	 * @return
	 */
	private Boolean checkDataPurgingJobSchedule() {
		JsonObject settingsJsonObject = getSettingsJsonObject();
		String lastRunTimeStr = DataPurgingUtils.getLastRunTime(settingsJsonObject);
		String nextRunTimeStr = DataPurgingUtils.getNextRunTime(settingsJsonObject);
		Long lastRunTime = parseDateIntoEpochSeconds(lastRunTimeStr);
		Long nextRunTime = parseDateIntoEpochSeconds(nextRunTimeStr);
		Long x = getDifferenceFromLastRunTime(lastRunTime);
		Long y = getDifferenceFromNextRunTime(lastRunTime, nextRunTime);
		if (x > y) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	/**
	 * This method parses a date represented in String into ZonedDateTime
	 * and converts it into EpochSecond
	 * @param inputDate
	 * @return
	 */
	public long parseDateIntoEpochSeconds(String inputDate) {
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
	 * 
	 * Calculates difference between currentTime and lastRunTime
	 * (now - lastRunTime)
	 * @param lastRunTime
	 * @return
	 */ 
	public long getDifferenceFromLastRunTime(long lastRunTime){
		ZonedDateTime now = ZonedDateTime.now(InsightsUtils.zoneId);
		ZonedDateTime lastRunTimeInput = ZonedDateTime.ofInstant(Instant.ofEpochSecond(lastRunTime), InsightsUtils.zoneId);
		Duration d = Duration.between(lastRunTimeInput,now);		
		return d.abs().toMillis();
	}

	/**
	 * Calculates difference between nextRunTime and lastRunTime
	 * (nextRunTime - lastRunTime )
	 * @param lastRunTime
	 * @param nextRunTime
	 * @return
	 */
	public long getDifferenceFromNextRunTime(Long lastRunTime, Long nextRunTime){
		ZonedDateTime lastRunTimeInput = ZonedDateTime.ofInstant(Instant.ofEpochSecond(lastRunTime), InsightsUtils.zoneId);
		ZonedDateTime nextRunTimeInput = ZonedDateTime.ofInstant(Instant.ofEpochSecond(nextRunTime), InsightsUtils.zoneId);
		Duration d = Duration.between(lastRunTimeInput, nextRunTimeInput);
		return d.abs().toMillis();
	}



	/**
	 * Updates lastRunTime in db with current date time,
	 * Calculates nextRunTime as per dataArchivalFrequency,
	 * Updates nextRunTime into the database
	 */
	private void updateRunTimeIntoDatabase() {
		JsonObject settingsJsonObject = getSettingsJsonObject();
		String dataArchivalFrequency = DataPurgingUtils.getDataArchivalFrequency(settingsJsonObject);
		//Captures current date time to update lastRunTime
		String lastRunTime = InsightsUtils.getLocalDateTime(DATE_TIME_FORMAT);
		String nextRunTime = DataPurgingUtils.calculateNextRunTime(dataArchivalFrequency);
		settingsJsonObject = DataPurgingUtils.updateLastRunTime(settingsJsonObject,lastRunTime);
		settingsJsonObject = DataPurgingUtils.updateNextRunTime(settingsJsonObject,nextRunTime);
		String modifiedSettingsJson = null;
		if (settingsJsonObject != null) {
			modifiedSettingsJson = settingsJsonObject.toString();			
		}
		if (modifiedSettingsJson != null && !modifiedSettingsJson.isEmpty()){
			SettingsConfigurationDAL settingsConfigurationDAL = new SettingsConfigurationDAL();
			settingsConfigurationDAL.updateSettingJson(modifiedSettingsJson);
		}
	}

}
