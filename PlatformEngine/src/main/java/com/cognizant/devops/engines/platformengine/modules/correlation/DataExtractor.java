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
package com.cognizant.devops.engines.platformengine.modules.correlation;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.engines.platformengine.modules.correlation.model.Correlation;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.CorrelationConfig;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.core.enums.FileDetailsEnum;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFiles;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFilesDAL;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * 
 * @author Vishal Ganjare (vganjare)
 * 
 *         Entry point for data extraction module.
 *
 */
@Deprecated
public class DataExtractor {
	private static Logger log = LogManager.getLogger(DataExtractor.class);
	private  boolean isDataExtractionInProgress = false;
	private static Pattern p = Pattern.compile("((?<!([A-Z]{1,10})-?)(#|)+[A-Z]+(-|.)\\d+)");
	private int dataBatchSize = 2000;
	private String execId = null;
	InsightsConfigFilesDAL configFilesDAL = new InsightsConfigFilesDAL();

	public void setExecId(String execId) {
		this.execId = execId;
	}

	public void execute() {
		CorrelationConfig correlationConfig = ApplicationConfigProvider.getInstance().getCorrelations();
		if (correlationConfig != null) {
			List<Correlation> corelations = loadCorrelations();
			dataExtraction(corelations);		
		}
	}

	private void dataExtraction(List<Correlation> corelations) {

		String divider = null;
		String sourceTool = null;

		for (Correlation correlation : corelations) {
			sourceTool = correlation.getSource().getToolName();
			divider = correlation.getSource().getAlmkeyPattern();
			if (!isDataExtractionInProgress && correlation.getSource().getToolCategory().equals("ALM")
					&& correlation.getDestination().getToolCategory().equals("SCM")) {
					log.info("DataExtractor - ALM key extraction is in progress from SCM message field.");
					String almKeyProcessedIndex = correlation.getSource().getAlmKeyProcessedIndex();
					String almKeysIndex = correlation.getSource().getAlmKeysIndex();
					isDataExtractionInProgress = true;
					updateSCMNodesWithAlmKey(divider, sourceTool, almKeyProcessedIndex, almKeysIndex);
					cleanSCMNodes(sourceTool, almKeyProcessedIndex);
					if (correlation.getSource().isEnrichAlmData()) {
						enrichAlmData(sourceTool);
					}
					isDataExtractionInProgress = false;
			}
		}

	}
	
	private void updateSCMNodesWithAlmKey(String divider, String sourceTool, String almKeyProcessedIndex,
			String almKeysIndex) {
		GraphDBHandler dbHandler = new GraphDBHandler();
		try {
			String paginationCypher = "MATCH (n:SCM:DATA:RAW) where n." + almKeyProcessedIndex
					+ "IS NULL and n.commitId IS NOT NULL return count(n) as count";
			GraphResponse paginationResponse = dbHandler.executeCypherQuery(paginationCypher);
			int resultCount = paginationResponse.getJson().get(ConfigOptions.RESULTS).getAsJsonArray().get(0)
					.getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("row").getAsJsonArray()
					.get(0).getAsInt();
			while (resultCount > 0) {
				long st = System.currentTimeMillis();
				String scmDataFetchCypher = "MATCH (source:SCM:DATA:RAW) where source." + almKeyProcessedIndex
						+ "IS NULL and source.commitId IS NOT NULL "
						+ "WITH { uuid: source.uuid, commitId: source.commitId, message: source.message} "
						+ "as data limit " + dataBatchSize + " return collect(data)";
				GraphResponse response = dbHandler.executeCypherQuery(scmDataFetchCypher);
				JsonArray rows = response.getJson().get(ConfigOptions.RESULTS).getAsJsonArray().get(0).getAsJsonObject()
						.get("data").getAsJsonArray().get(0).getAsJsonObject().get("row").getAsJsonArray();
				if (rows.isJsonNull() || rows.size() == 0) {
					log.info("DataExtractor - updateSCMNodesWithAlmKey returns 0 rows.");
					break;
				}
				JsonArray dataList = rows.get(0).getAsJsonArray();
				int processedRecords = dataList.size();
				String addAlmKeysCypher = "UNWIND $props as properties MATCH (source:SCM:DATA:RAW {uuid : properties.uuid, commitId: properties.commitId}) "
						+ "set source." + almKeysIndex + " = properties." + almKeysIndex + ", source."
						+ almKeyProcessedIndex + " = true return count(source)";
				String updateRawLabelCypher = "UNWIND $props as properties MATCH (source:SCM:DATA:RAW {uuid : properties.uuid, commitId: properties.commitId}) "
						+ "set source." + almKeyProcessedIndex + " = true return count(source)";
				List<JsonObject> almKeysCypherProps = new ArrayList<>();
				List<JsonObject> updateRawLabelCypherProps = new ArrayList<>();
				JsonObject data = null;

				for (JsonElement dataElem : dataList) {
					JsonObject dataJson = dataElem.getAsJsonObject();
					JsonElement messageElem = dataJson.get("message");
					if (messageElem.isJsonPrimitive()) {
						String message = messageElem.getAsString();
						JsonArray almKeys = prepareAlmKeys(message, divider);
						data = new JsonObject();
						data.addProperty("uuid", dataJson.get("uuid").getAsString());
						data.addProperty("commitId", dataJson.get("commitId").getAsString());
						if (almKeys.size() > 0) {
							data.add(almKeysIndex, almKeys);
							almKeysCypherProps.add(data);
						} else {
							updateRawLabelCypherProps.add(data);
						}
					}
				}

				bulkCreateNodes(updateRawLabelCypherProps, almKeysCypherProps, updateRawLabelCypher, addAlmKeysCypher);

				resultCount = resultCount - dataBatchSize;
				log.debug(" Type=Correlator execId={} Processed {} SCM records, time taken: {} ms", execId,
						processedRecords, (System.currentTimeMillis() - st));
			}
		} catch (InsightsCustomException e) {
			log.error("Unable to extract " + sourceTool + " keys from SCM Commit messages", e);
		}
	}
	
	private void bulkCreateNodes(List<JsonObject> updateRawLabelCypherProps, List<JsonObject> almKeysCypherProps,
			String updateRawLabelCypher, String addAlmKeysCypher) throws InsightsCustomException{
		GraphDBHandler dbHandler = new GraphDBHandler();
		
		if (!updateRawLabelCypherProps.isEmpty()) {
			JsonObject bulkCreateNodes = dbHandler.bulkCreateNodes(updateRawLabelCypherProps,
					updateRawLabelCypher);
			log.debug(bulkCreateNodes);
		}
		if (!almKeysCypherProps.isEmpty()) {
			JsonObject bulkCreateNodes = dbHandler.bulkCreateNodes(almKeysCypherProps,addAlmKeysCypher);
			log.debug(bulkCreateNodes);
		}
	
	}
	
	private JsonArray prepareAlmKeys(String message, String divider){
		JsonArray prepareAlmKeys = new JsonArray();
		
		while (message.contains(divider)) {
			Matcher m = p.matcher(message);
			if (m.find()) {
				prepareAlmKeys.add(m.group());
				message = message.replaceAll(m.group(), "");
			} else {
				break;
			}
		}
		return prepareAlmKeys;
	}

	private void cleanSCMNodes(String sourceTool, String almKeyProcessedIndex) {
		GraphDBHandler dbHandler = new GraphDBHandler();
		try {
			int processedRecords = 1;
			while (processedRecords > 0) {
				long st = System.currentTimeMillis();
				String scmCleanUpCypher = "MATCH (n:SCM:DATA) where not n:RAW and n." + almKeyProcessedIndex
						+ "IS NOT NULL " + "WITH distinct n limit " + dataBatchSize + " remove n." + almKeyProcessedIndex
						+ " return count(n)";
				GraphResponse response = dbHandler.executeCypherQuery(scmCleanUpCypher);
				processedRecords = response.getJson().get(ConfigOptions.RESULTS).getAsJsonArray().get(0).getAsJsonObject()
						.get("data").getAsJsonArray().get(0).getAsJsonObject().get("row").getAsJsonArray().get(0)
						.getAsInt();
				log.debug(" Type=Correlator execId={} Processed {} SCM records, time taken: {} ms",execId,processedRecords,(System.currentTimeMillis() - st));
			}
		} catch (InsightsCustomException e) {
			log.error("Unable to extract " + sourceTool + " keys from SCM Commit messages", e);
		}
	}

	private void enrichAlmData(String sourceTool) {
		GraphDBHandler dbHandler = new GraphDBHandler();
		try {
			String almProjectCypher = "match (n:" + sourceTool
					+ ":DATA) where n._PORTFOLIO_ IS NULL WITH distinct n.projectKey as projectKey, count(n) as count "
					+ "OPTIONAL MATCH(m:" + sourceTool + ":METADATA) where m.pkey=projectKey "
					+ "WITH {projectKey: projectKey , count: count, portfolio: m.PORTFOLIO, product: m.PRODUCT} as data "
					+ "return collect(data) as data";
			GraphResponse paginationResponse = dbHandler.executeCypherQuery(almProjectCypher);
			JsonArray data = paginationResponse.getJson().get(ConfigOptions.RESULTS).getAsJsonArray().get(0).getAsJsonObject()
					.get("data").getAsJsonArray().get(0).getAsJsonObject().get("row").getAsJsonArray().get(0)
					.getAsJsonArray();
			String almDataEnrichmentCypher = "UNWIND $props as properties MATCH(n:" + sourceTool
					+ " {projectKey: properties.projectKey}) where n._PORTFOLIO_ IS NULL "
					+ "set n._PORTFOLIO_ = properties.portfolio, n._PRODUCT_ = properties.product return count(n)";
			
			List<List<JsonObject>> projectProcessingBatches = prepareProjectProcessingBatches(data);
		
			if (!projectProcessingBatches.isEmpty()) {
				for (List<JsonObject> batch : projectProcessingBatches) {
					long st = System.currentTimeMillis();
					int processedRecords = 0;
					for (JsonObject obj : batch) {
						processedRecords += obj.get("count").getAsInt();
					}
					JsonObject almEnrichmentResponse = dbHandler.bulkCreateNodes(batch,almDataEnrichmentCypher);
					log.debug("Processed/Enriched {} {} records, time taken: {} ms " , processedRecords, sourceTool ,(System.currentTimeMillis() - st));
					log.debug(almEnrichmentResponse);
				}
			}
		} catch (InsightsCustomException e) {
			log.error(e);
		}
	}
	
	private List<List<JsonObject>> prepareProjectProcessingBatches(JsonArray data) {
		
		List<JsonObject> projectList = new ArrayList<>();
		List<List<JsonObject>> processingBatches = new ArrayList<>();
		int projectIssueSize = 0;
		
		for (JsonElement projectDataElement : data) {
			JsonObject projectData = projectDataElement.getAsJsonObject();
			JsonElement portfolio = projectData.get("portfolio");
			JsonElement product = projectData.get("product");
			if (!portfolio.isJsonNull() || !product.isJsonNull()) {
				projectList.add(projectData);
				projectIssueSize += projectData.get("count").getAsInt();
			}
			if (!processingBatches.contains(projectList) && !projectList.isEmpty()) {
				processingBatches.add(projectList);
			}
			if (projectIssueSize >= dataBatchSize) {
				projectList = new ArrayList<>();
				projectIssueSize = 0;
			}
		}
		
		return processingBatches;
	}

	private List<Correlation> loadCorrelations() {
		List<Correlation> correlations = null;
		try {
			List<InsightsConfigFiles> configFile = configFilesDAL
					.getAllConfigurationFilesForModule(FileDetailsEnum.FileModule.CORRELATION.name());
			if (configFile != null && !configFile.isEmpty()) {
				String configFileData = new String(configFile.get(0).getFileData(), StandardCharsets.UTF_8);
				Correlation[] correlationArray = new Gson().fromJson(configFileData, Correlation[].class);
				correlations = Arrays.asList(correlationArray);
				log.debug("Correlation.json is successfully loaded.");
			} else {
				log.error("Correlation.json not found in DB.");
				
			}
		} catch (JsonSyntaxException e) {
			
			log.error("Correlation.json is not formatted");
		} catch (Exception e) {
			
			log.error("Exception while loading Correlation.json");
		}
		return correlations;
	}
}
