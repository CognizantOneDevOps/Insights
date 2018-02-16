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

import org.apache.log4j.Logger;

import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jFieldIndexRegistry;
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
	private static Logger log = Logger.getLogger(DataExtractor.class);
	
	private static boolean isDataExtractionInProgress = false;
	private static final Pattern p = Pattern.compile("((?<!([A-Z]{1,10})-?)[A-Z]+-\\d+)");
	private int dataBatchSize = 2000;
	
	public void execute() {
		if(!isDataExtractionInProgress) {
			isDataExtractionInProgress = true;
			updateSCMNodesWithJiraKey();
			cleanSCMNodes();
			enrichJiraData();
			isDataExtractionInProgress = false;
		}
	}
	
	private void updateSCMNodesWithJiraKey() {
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		try {
			Neo4jFieldIndexRegistry.getInstance().syncFieldIndex("SCM", "jiraKeyProcessed");
			Neo4jFieldIndexRegistry.getInstance().syncFieldIndex("SCM", "jiraKeys");
			String paginationCypher = "MATCH (n:SCM:DATA:RAW) where not exists(n.jiraKeyProcessed) and exists(n.commitId) return count(n) as count";
			GraphResponse paginationResponse = dbHandler.executeCypherQuery(paginationCypher);
			int resultCount = paginationResponse.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
					.getAsJsonArray().get(0).getAsJsonObject().get("row").getAsJsonArray().get(0).getAsInt();
			while(resultCount > 0) {
				long st = System.currentTimeMillis();
				String scmDataFetchCypher = "MATCH (source:SCM:DATA:RAW) where not exists(source.jiraKeyProcessed) and exists(source.commitId) "
						+ "WITH { uuid: source.uuid, commitId: source.commitId, message: source.message} "
						+ "as data limit "+dataBatchSize+" return collect(data)";
				GraphResponse response = dbHandler.executeCypherQuery(scmDataFetchCypher);
				JsonArray rows = response.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
						.getAsJsonArray().get(0).getAsJsonObject().get("row").getAsJsonArray();
				if(rows.isJsonNull() || rows.size() == 0) {
					break;
				}
				JsonArray dataList = rows.get(0).getAsJsonArray();
				int processedRecords = dataList.size();
				String addJiraKeysCypher = "UNWIND {props} as properties MATCH (source:SCM:DATA:RAW {uuid : properties.uuid, commitId: properties.commitId}) "
						+ "set source.jiraKeys = properties.jiraKeys, source.jiraKeyProcessed = true return count(source)";
				String updateRawLabelCypher = "UNWIND {props} as properties MATCH (source:SCM:DATA:RAW {uuid : properties.uuid, commitId: properties.commitId}) "
						+ "set source.jiraKeyProcessed = true return count(source)";
				List<JsonObject> jiraKeysCypherProps = new ArrayList<JsonObject>();
				List<JsonObject> updateRawLabelCypherProps = new ArrayList<JsonObject>();
				JsonObject data = null;
				for(JsonElement dataElem : dataList) {
					JsonObject dataJson = dataElem.getAsJsonObject();
					JsonElement messageElem = dataJson.get("message");
					if(messageElem.isJsonPrimitive()) {
						String message = messageElem.getAsString();
						JsonArray jiraKeys = new JsonArray();
						while(message.contains("-")) {
							Matcher m = p.matcher(message);
							if(m.find()) {
								jiraKeys.add(m.group());
								message = message.replaceAll(m.group(), "");
							}else {
								break;
							}
						}
						data = new JsonObject();
						data.addProperty("uuid", dataJson.get("uuid").getAsString());
						data.addProperty("commitId", dataJson.get("commitId").getAsString());
						if(jiraKeys.size() > 0) {
							data.add("jiraKeys", jiraKeys);
							jiraKeysCypherProps.add(data);
						}else {
							updateRawLabelCypherProps.add(data);
						}
					}
				}
				if(updateRawLabelCypherProps.size() > 0) {
					JsonObject bulkCreateNodes = dbHandler.bulkCreateNodes(updateRawLabelCypherProps, null, updateRawLabelCypher);
					log.debug(bulkCreateNodes);
				}
				if(jiraKeysCypherProps.size() > 0) {
					JsonObject bulkCreateNodes = dbHandler.bulkCreateNodes(jiraKeysCypherProps, null, addJiraKeysCypher);
					log.debug(bulkCreateNodes);
				}
				resultCount = resultCount - dataBatchSize;
				log.debug("Processed "+processedRecords+" SCM records, time taken: "+(System.currentTimeMillis() - st) + " ms");
			}
		} catch (GraphDBException e) {
			log.error("Unable to extract JIRA keys from SCM Commit messages", e);
		}
	}
	
	private void cleanSCMNodes() {
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		try {
			int processedRecords = 1;
			while(processedRecords > 0) {
				long st = System.currentTimeMillis();
				String scmCleanUpCypher = "MATCH (n:SCM:DATA) where not n:RAW and exists(n.jiraKeyProcessed) "
						+ "WITH distinct n limit "+dataBatchSize+" remove n.jiraKeyProcessed return count(n)";
				GraphResponse response = dbHandler.executeCypherQuery(scmCleanUpCypher);
				processedRecords = response.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
						.getAsJsonArray().get(0).getAsJsonObject().get("row").getAsJsonArray().get(0).getAsInt();
				log.debug("Processed "+processedRecords+" SCM records, time taken: "+(System.currentTimeMillis() - st) + " ms");
			}
		} catch (GraphDBException e) {
			log.error("Unable to extract JIRA keys from SCM Commit messages", e);
		}
	}
	
	private void enrichJiraData() {
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		try {
			Neo4jFieldIndexRegistry.getInstance().syncFieldIndex("JIRA", "_PORTFOLIO_");
			Neo4jFieldIndexRegistry.getInstance().syncFieldIndex("JIRA", "_PRODUCT_");
			String jiraProjectCypher = "match (n:JIRA:DATA) where not exists(n._PORTFOLIO_) WITH distinct n.projectKey as projectKey, count(n) as count " + 
						"OPTIONAL MATCH(m:JIRA:METADATA) where m.pkey=projectKey "
						+ "WITH {projectKey: projectKey , count: count, portfolio: m.PORTFOLIO, product: m.PRODUCT} as data "
						+ "return collect(data) as data";
			GraphResponse paginationResponse = dbHandler.executeCypherQuery(jiraProjectCypher);
			JsonArray data = paginationResponse.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
					.getAsJsonArray().get(0).getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonArray();
			String jiraDataEnrichmentCypher = "UNWIND {props} as properties MATCH(n:JIRA {projectKey: properties.projectKey}) where not exists(n._PORTFOLIO_) "
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
					JsonObject jiraEnrichmentResponse = dbHandler.bulkCreateNodes(batch, null, jiraDataEnrichmentCypher);
					log.debug("Processed/Enriched "+processedRecords+" JIRA records, time taken: "+(System.currentTimeMillis() - st) + " ms");
					log.debug(jiraEnrichmentResponse);
				}
			}
		} catch (GraphDBException e) {
			log.error(e);
		}
	}
}
