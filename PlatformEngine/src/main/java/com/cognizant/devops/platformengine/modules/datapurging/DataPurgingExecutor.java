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

	public void execute(JobExecutionContext context) throws JobExecutionException {
		if (ApplicationConfigProvider.getInstance().isEnableOnlineBackup()) {
			performDataPurging();				
		} 
	}


	public void performDataPurging()   {
		List<String> labelList = new ArrayList<>();
		String rowLimit = null ;
		String backupFileLocation = null ;
		long backupDurationInDays = 0;
		String backupFileName = null ;

		/**
		 * To get Settings Configuration which is set by User from Insights application UI
		 * is stored into Settings_Configuration table of PostGres database
		 */
		JsonObject configJsonObj = getSettingsJsonObject(); 
		if (configJsonObj != null) {
			JsonArray array = configJsonObj.get("labels").getAsJsonArray();
			if (array != null) {
				for (int i = 0; i < array.size(); i++) {
					labelList.add(array.get(i).getAsString());
				}
			}			
			rowLimit = configJsonObj.get(ConfigOptions.ROW_LIMIT).getAsString();
			backupFileLocation =configJsonObj.get(ConfigOptions.BACKUP_FILE_LOCATION).getAsString();
			backupFileName = configJsonObj.get(ConfigOptions.BACKUP_FILE_NAME).getAsString();
			backupDurationInDays = configJsonObj.get(ConfigOptions.BACKUP_DURATION_IN_DAYS).getAsLong();
		}


		//Converts into epoch time in seconds as data inside inSightsTime property is stored in epoch seconds  
		long epochTime = InsightsUtils.getTimeBeforeDaysInSeconds(backupDurationInDays);
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		for(String label : labelList){
			int splitlength = 0;
			boolean deleteFlag = true;
			try {
				int count = getNodeCount(dbHandler, label ,epochTime);
				while(splitlength  < count){
					GraphResponse response = executeCypherQuery(label,rowLimit,splitlength,epochTime) ;
					String localDateTime = InsightsUtils.getLocalDateTime("yyyyMMddHHmmss");
					String location = backupFileLocation + File.separator + backupFileName + "_" + splitlength + "_" + localDateTime + ".csv";
					writeToCSVFile(response , location);				
					splitlength = splitlength + Integer.parseInt(rowLimit);
				}
			} 
			catch (GraphDBException | IOException e) {
				log.error("Exception occured while taking backup into csv file in DataPurgingExecutor Job: " + e);
				deleteFlag = false;
			}
			if(deleteFlag){
				String deleteQry =  "MATCH (n:"+label+")  where n.inSightsTime < "+ epochTime  +" detach delete n ";
				try {
					dbHandler.executeCypherQuery(deleteQry);
				} catch (GraphDBException e) {
					log.error("Exception occured while deleting data of " + label +" label inside DataPurgingExecutor Job: " + e);
				}
			}
		}

	}

	private int getNodeCount(Neo4jDBHandler dbHandler, String label, long epochTime) throws GraphDBException {
		String cntQry = "MATCH (n:"+label+")  where n.inSightsTime < "+ epochTime  +" return count(n) ";
		GraphResponse cntResponse = dbHandler.executeCypherQuery(cntQry);
		int count = cntResponse.getJson() .get("results").getAsJsonArray().get(0).getAsJsonObject().get("data").getAsJsonArray()
				.get(0).getAsJsonObject().get("row").getAsInt();
		return count;
	}

	private GraphResponse executeCypherQuery(String label, String limit, int splitlength, long epochTime) throws GraphDBException {
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		String query = "MATCH (n:"+label+")  where n.inSightsTime < "+ epochTime  +"   return n skip  "+ splitlength +" limit  " +limit;
		GraphResponse response = dbHandler.executeCypherQuery(query);
		return response;
	}

	private void writeToCSVFile(GraphResponse response, String location) throws IOException {
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
		FileWriter fWriter = new FileWriter(location);
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

	private JsonObject getSettingsJsonObject() {
		SettingsConfigurationDAL settingsConfigurationDAL = new SettingsConfigurationDAL();	
		String settingsJson = settingsConfigurationDAL.getSettingsJsonObject(ConfigOptions.DATAPURGING_SETTINGS_TYPE);
		if (settingsJson != null && !settingsJson.isEmpty()) {
			Gson gson = new Gson();
			JsonElement jsonElement = gson.fromJson(settingsJson.trim(),JsonElement.class);
			return jsonElement.getAsJsonObject();
		}
		return null;
	}


	/*public static void main(String[] a){
		DataPurgingExecutor dataPurgingExecutor=new DataPurgingExecutor();
		long epochTime = InsightsUtils.getTimeBeforeDays(300L);
		System.out.println("epoch time:>>"+epochTime );
		//ApplicationConfigCache.loadConfigCache();
		//dataPurgingExecutor.performDataPurging();
	}*/
}
