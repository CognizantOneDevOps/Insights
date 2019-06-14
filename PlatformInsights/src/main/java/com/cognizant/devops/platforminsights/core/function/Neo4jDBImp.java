/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platforminsights.core.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platforminsights.core.BaseActionImpl;
import com.cognizant.devops.platforminsights.datamodel.KPIDefinition;
import com.cognizant.devops.platforminsights.datamodel.Neo4jKPIDefinition;
import com.cognizant.devops.platforminsights.exception.InsightsSparkJobFailedException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Neo4jDBImp extends BaseActionImpl {
	public Neo4jDBImp(Neo4jKPIDefinition neo4jKpiDefinition) {
		super(neo4jKpiDefinition);
	}

	public Neo4jDBImp(KPIDefinition kpiDefinition) {
		super(kpiDefinition);
	}

	private static final Logger log = LogManager.getLogger(Neo4jDBImp.class);

	public List<Map<String, Object>> getNeo4jResult() {
		Neo4jDBHandler graphDBHandler = new Neo4jDBHandler();
		List<Map<String, Object>> resultList = new ArrayList<>();
		try {
			log.debug("Database type found to be Neo4j");
			String graphQuery = neo4jKpiDefinition.getNeo4jQuery();
			log.debug(graphQuery);
			GraphResponse graphResp = graphDBHandler.executeCypherQuery(graphQuery);
			log.debug(graphResp.getJson());
			JsonArray graphJsonResult = graphResp.getJson().getAsJsonArray("results");
			Map<String, Object> resultMap = new HashMap<>();
			String groupByFieldVal = "";
			String groupByFieldValResult = "";

			for (JsonElement obj : graphJsonResult) {
				JsonObject innerJson = obj.getAsJsonObject();
				JsonArray data = innerJson.getAsJsonArray("data");
				for (JsonElement dataObj : data) {
					JsonObject row = dataObj.getAsJsonObject();
					JsonArray rowData = row.getAsJsonArray("row");
					log.debug("rowData " + rowData);
					if (rowData.size() == 1) {
						resultMap = getResultMapNeo4j(rowData.getAsString(), "");
					} else {
						int i = 0;
						for (JsonElement key : rowData) {
							log.debug(" key  " + key);
							if (i == 0 && neo4jKpiDefinition.isGroupBy()) {
								groupByFieldVal = key.getAsString();
								i++;
							} else {
								groupByFieldValResult = key.getAsString();
							}
						}
						resultMap = getResultMapNeo4j(groupByFieldValResult, groupByFieldVal);
					}
					resultList.add(resultMap);
				}
			}
		} catch (Exception e) {
			log.error("Exception while running neo4j operation", e);
		}
		return resultList;
	}

	@Override
	protected Map<String, Object> execute() throws InsightsSparkJobFailedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void executeNeo4jGraphQuery() {
		// TODO Auto-generated method stub

	}

}
