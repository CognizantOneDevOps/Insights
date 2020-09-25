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
package com.cognizant.devops.engines.platformengine.modules.dataenrichment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
/**
 * 
 * @author Vishal Ganjare (vganjare)
 * 
 * Entry point for data enrichment module.
 *
 */
public class DataEnrichmentModule extends TimerTask {
	private static Logger log = LogManager.getLogger(DataEnrichmentModule.class);
	
	@Override
	public void run() {
		handleDataCleaning();
	}

	private void handleDataCleaning() {
		Map<String, String> dataEnrichmentCypherQueryMap = loadDataEnrichmentJson();
		if(dataEnrichmentCypherQueryMap == null) {
			return;
		}
		GraphDBHandler dbHandler = new GraphDBHandler();
		for(Map.Entry<String, String> entry: dataEnrichmentCypherQueryMap.entrySet()) {
			try {
				int processedRecords = 1;
				while(processedRecords > 0) {
					GraphResponse sprintResponse = dbHandler.executeCypherQuery(entry.getValue());
					JsonObject sprintResponseJson = sprintResponse.getJson();
					processedRecords = sprintResponseJson.getAsJsonArray("results").get(0).getAsJsonObject()
							.getAsJsonArray("data").get(0).getAsJsonObject()
							.getAsJsonArray("row").get(0).getAsInt();
					log.debug(entry.getKey()+" Processed "+processedRecords);
				}

			} catch (InsightsCustomException e) {
				log.error(entry.getKey() + " Processing Failed", e);
			} catch (Exception e) {
				log.error(entry.getKey() + " Processing Failed", e);
			}
		}
	}
	
	private Map<String, String> loadDataEnrichmentJson() {
		BufferedReader reader = null;
		Map<String, String> dataEnrichmentMap = null;
		File correlationTemplate = new File(ConfigOptions.DATA_ENRICHMENT_FILE_RESOLVED_PATH);
		try {
			if (correlationTemplate.exists()) {
				reader = new BufferedReader(new FileReader(correlationTemplate));
				dataEnrichmentMap = new Gson().fromJson(reader, Map.class);
			} 
		} catch (FileNotFoundException e) {
			log.error("data-enrichment.json file not found.", e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				log.error("Unable to read the correlation.json file.", e);
			}
		}
		return dataEnrichmentMap;
	}
}
