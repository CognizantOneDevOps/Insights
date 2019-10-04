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
package com.cognizant.devops.platformengine.modules.correlation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.CorrelationConfig;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformengine.modules.correlation.model.Correlation;

import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jFieldIndexRegistry;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
/**
 * 
 * @author Vishal Ganjare (vganjare)
 * 
 * Entry point for data extraction module.
 *
 */
public class DataExtractor{
	private static Logger log = LogManager.getLogger(DataExtractor.class);
	
	private static boolean isDataExtractionInProgress = false;
	private static Pattern p = Pattern.compile("((?<!([A-Z]{1,10})-?)(#|)+[A-Z]+(-|.)\\d+)");
	private static String sourceTool = null;
	private static String destinationTool = null;
	private int dataBatchSize = 2000;
	
	public void execute() {
		CorrelationConfig correlationConfig = ApplicationConfigProvider.getInstance().getCorrelations();
		String divider=null;
		if(correlationConfig != null) {
			List<Correlation> corelations = loadCorrelations();
			for(Correlation correlation: corelations) {
				sourceTool=correlation.getSource().getToolName();
				destinationTool=correlation.getDestination().getToolName();
				divider=correlation.getSource().getAlmkeyPattern();
				if(!isDataExtractionInProgress) {
					if(correlation.getSource().getToolCategory().equals("ALM") && correlation.getDestination().getToolCategory().equals("SCM")) {
						log.info("DataExtractor - ALM key extraction is in progress from SCM message field.");
						String almKeyProcessedIndex = correlation.getSource().getAlmKeyProcessedIndex();
						String almKeysIndex = correlation.getSource().getAlmKeysIndex();
						isDataExtractionInProgress = true;
						updateSCMNodesWithAlmKey(divider, sourceTool, almKeyProcessedIndex, almKeysIndex);
						cleanSCMNodes(sourceTool, almKeyProcessedIndex, almKeysIndex);
						if(correlation.getSource().isEnrichAlmData()) {
							enrichAlmData(sourceTool);
						}
						isDataExtractionInProgress = false;
					}
				}
			}
		}
	}
	
	private void updateSCMNodesWithAlmKey(String divider, String sourceTool, String almKeyProcessedIndex, String almKeysIndex) {
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		try {
			Neo4jFieldIndexRegistry.getInstance().syncFieldIndex("SCM", almKeyProcessedIndex);
			Neo4jFieldIndexRegistry.getInstance().syncFieldIndex("SCM", almKeysIndex);
			String paginationCypher = "MATCH (n:SCM:DATA:RAW) where not exists(n."+almKeyProcessedIndex+") and exists(n.commitId) return count(n) as count";
			GraphResponse paginationResponse = dbHandler.executeCypherQuery(paginationCypher);
			int resultCount = paginationResponse.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
					.getAsJsonArray().get(0).getAsJsonObject().get("row").getAsJsonArray().get(0).getAsInt();
			while(resultCount > 0) {
				long st = System.currentTimeMillis();
				String scmDataFetchCypher = "MATCH (source:SCM:DATA:RAW) where not exists(source."+almKeyProcessedIndex+") and exists(source.commitId) "
						+ "WITH { uuid: source.uuid, commitId: source.commitId, message: source.message} "
						+ "as data limit "+dataBatchSize+" return collect(data)";
				GraphResponse response = dbHandler.executeCypherQuery(scmDataFetchCypher);
				JsonArray rows = response.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
						.getAsJsonArray().get(0).getAsJsonObject().get("row").getAsJsonArray();
				if(rows.isJsonNull() || rows.size() == 0) {
					log.info("DataExtractor - updateSCMNodesWithAlmKey returns 0 rows.");
					break;
				}
				JsonArray dataList = rows.get(0).getAsJsonArray();
				int processedRecords = dataList.size();
				String addAlmKeysCypher = "UNWIND {props} as properties MATCH (source:SCM:DATA:RAW {uuid : properties.uuid, commitId: properties.commitId}) "
						+ "set source."+almKeysIndex+" = properties."+almKeysIndex+", source."+almKeyProcessedIndex+" = true return count(source)";
				String updateRawLabelCypher = "UNWIND {props} as properties MATCH (source:SCM:DATA:RAW {uuid : properties.uuid, commitId: properties.commitId}) "
						+ "set source."+almKeyProcessedIndex+" = true return count(source)";
				List<JsonObject> almKeysCypherProps = new ArrayList<JsonObject>();
				List<JsonObject> updateRawLabelCypherProps = new ArrayList<JsonObject>();
				JsonObject data = null;
				for(JsonElement dataElem : dataList) {
					JsonObject dataJson = dataElem.getAsJsonObject();
					JsonElement messageElem = dataJson.get("message");
					if(messageElem.isJsonPrimitive()) {
						String message = messageElem.getAsString();
						JsonArray almKeys = new JsonArray();
						while(message.contains(divider)) {
							Matcher m = p.matcher(message);
							if(m.find()) {
								almKeys.add(m.group());
								message = message.replaceAll(m.group(), "");
							}else {
								break;
							}
						}
						data = new JsonObject();
						data.addProperty("uuid", dataJson.get("uuid").getAsString());
						data.addProperty("commitId", dataJson.get("commitId").getAsString());
						if(almKeys.size() > 0) {
							data.add(almKeysIndex, almKeys);
							almKeysCypherProps.add(data);
						}else {
							updateRawLabelCypherProps.add(data);
						}
					}
				}
				if(updateRawLabelCypherProps.size() > 0) {
					JsonObject bulkCreateNodes = dbHandler.bulkCreateNodes(updateRawLabelCypherProps, null, updateRawLabelCypher);
					log.debug(bulkCreateNodes);
				}
				if(almKeysCypherProps.size() > 0) {
					JsonObject bulkCreateNodes = dbHandler.bulkCreateNodes(almKeysCypherProps, null, addAlmKeysCypher);
					log.debug(bulkCreateNodes);
				}
				resultCount = resultCount - dataBatchSize;
				log.debug("Processed "+processedRecords+" SCM records, time taken: "+(System.currentTimeMillis() - st) + " ms");
			}
		} catch (GraphDBException e) {
			log.error("Unable to extract "+sourceTool+" keys from SCM Commit messages", e);
		}
	}
	
	private void cleanSCMNodes(String sourceTool, String almKeyProcessedIndex, String almKeysIndex) {
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		try {
			int processedRecords = 1;
			while(processedRecords > 0) {
				long st = System.currentTimeMillis();
				String scmCleanUpCypher = "MATCH (n:SCM:DATA) where not n:RAW and exists(n."+almKeyProcessedIndex+") "
						+ "WITH distinct n limit "+dataBatchSize+" remove n."+almKeyProcessedIndex+" return count(n)";
				GraphResponse response = dbHandler.executeCypherQuery(scmCleanUpCypher);
				processedRecords = response.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
						.getAsJsonArray().get(0).getAsJsonObject().get("row").getAsJsonArray().get(0).getAsInt();
				log.debug("Processed "+processedRecords+" SCM records, time taken: "+(System.currentTimeMillis() - st) + " ms");
			}
		} catch (GraphDBException e) {
			log.error("Unable to extract "+sourceTool+" keys from SCM Commit messages", e);
		}
	}
	
	private void enrichAlmData(String sourceTool) {
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		try {
			Neo4jFieldIndexRegistry.getInstance().syncFieldIndex(sourceTool, "_PORTFOLIO_");
			Neo4jFieldIndexRegistry.getInstance().syncFieldIndex(sourceTool, "_PRODUCT_");
			String almProjectCypher = "match (n:"+sourceTool+":DATA) where not exists(n._PORTFOLIO_) WITH distinct n.projectKey as projectKey, count(n) as count " + 
					"OPTIONAL MATCH(m:"+sourceTool+":METADATA) where m.pkey=projectKey "
					+ "WITH {projectKey: projectKey , count: count, portfolio: m.PORTFOLIO, product: m.PRODUCT} as data "
					+ "return collect(data) as data";
			GraphResponse paginationResponse = dbHandler.executeCypherQuery(almProjectCypher);
			JsonArray data = paginationResponse.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
					.getAsJsonArray().get(0).getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonArray();
			String almDataEnrichmentCypher = "UNWIND {props} as properties MATCH(n:"+sourceTool+" {projectKey: properties.projectKey}) where not exists(n._PORTFOLIO_) "
					+ "set n._PORTFOLIO_ = properties.portfolio, n._PRODUCT_ = properties.product return count(n)";
			List<JsonObject> projectList = new ArrayList<JsonObject>();
			List<List<JsonObject>> projectProcessingBatches = new ArrayList<List<JsonObject>>();
			int projectIssueSize = 0;
			for(JsonElement projectDataElement : data) {
				JsonObject projectData = projectDataElement.getAsJsonObject();
				JsonElement portfolio = projectData.get("portfolio");
				JsonElement product = projectData.get("product");
				if(!portfolio.isJsonNull() || !product.isJsonNull()) {
					projectList.add(projectData);
					projectIssueSize += projectData.get("count").getAsInt();
				}
				if(!projectProcessingBatches.contains(projectList) && projectList.size() > 0) {
					projectProcessingBatches.add(projectList);
				}
				if(projectIssueSize >= dataBatchSize) {
					projectList = new ArrayList<JsonObject>();
					projectIssueSize = 0;
				}
			}
			if(projectProcessingBatches.size() > 0) {
				for(List<JsonObject> batch : projectProcessingBatches) {
					long st = System.currentTimeMillis();
					int processedRecords = 0;
					for(JsonObject obj : batch) {
						processedRecords += obj.get("count").getAsInt();
					}
					JsonObject almEnrichmentResponse = dbHandler.bulkCreateNodes(batch, null, almDataEnrichmentCypher);
					log.debug("Processed/Enriched "+processedRecords+" "+sourceTool+" records, time taken: "+(System.currentTimeMillis() - st) + " ms");
					log.debug(almEnrichmentResponse);
				}
			}
		} catch (GraphDBException e) {
			log.error(e);
		}
	}
	private List<Correlation> loadCorrelations() {
		BufferedReader reader = null;
		InputStream in = null;
		List<Correlation> correlations = null;
		File correlationTemplate = new File(ConfigOptions.CORRELATION_FILE_RESOLVED_PATH);
		try {
			if (correlationTemplate.exists()) {
				reader = new BufferedReader(new FileReader(correlationTemplate));
			} else {
				in = getClass().getResourceAsStream("/" + ConfigOptions.CORRELATION_TEMPLATE);
				reader = new BufferedReader(new InputStreamReader(in));
			}
			Correlation[] correlationArray = new Gson().fromJson(reader, Correlation[].class);
			correlations = Arrays.asList(correlationArray);
		} catch (FileNotFoundException e) {
			log.error("Correlations.json file not found.", e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				log.error("Unable to read the correlation.json file.", e);
			}
		}
		return correlations;
	}
}
