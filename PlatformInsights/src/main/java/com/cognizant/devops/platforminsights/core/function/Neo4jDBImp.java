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

import com.cognizant.devops.platformcommons.core.enums.JobSchedule;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platforminsights.core.BaseActionImpl;
import com.cognizant.devops.platforminsights.datamodel.KPIDefinition;
import com.cognizant.devops.platforminsights.datamodel.Neo4jKPIDefinition;
import com.cognizant.devops.platforminsights.exception.InsightsJobFailedException;
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
			String graphQuery = neo4jKpiDefinition.getNeo4jQuery();
			log.debug("Database type found to be Neo4j  === " + graphQuery);
			graphQuery = getNeo4jQueryWithDates(neo4jKpiDefinition.getSchedule(), graphQuery);
			log.debug("graphQuery with date === " + graphQuery);
			GraphResponse graphResp = graphDBHandler.executeCypherQuery(graphQuery);
			log.debug(graphResp.getJson());
			JsonArray errorMessage = graphResp.getJson().getAsJsonArray("errors");
			if (errorMessage.size() >= 1) {
				String errorMessageText = errorMessage.get(0).getAsJsonObject().get("message").getAsString();
				log.error(" Neo4j query execution error for job '" + neo4jKpiDefinition.getName() + "' and error is '"
						+ errorMessageText + " '");
				throw new InsightsJobFailedException(errorMessageText);
			}
			JsonArray graphJsonResult = graphResp.getJson().getAsJsonArray("results");
			Map<String, Object> resultMap = new HashMap<>();
			String groupByFieldVal = "";
			Long groupByFieldValResult = 0L;
			log.debug("Number of record return by query ==== " + graphJsonResult.size());
			for (JsonElement obj : graphJsonResult) {
				JsonObject innerJson = obj.getAsJsonObject();
				JsonArray data = innerJson.getAsJsonArray("data");
				for (JsonElement dataObj : data) {
					JsonObject row = dataObj.getAsJsonObject();
					JsonArray rowData = row.getAsJsonArray("row");
					log.debug("rowData " + rowData + "  rowData.get(0)  " + rowData.get(0) + " "
							+ rowData.get(0).isJsonNull());
					if (rowData.size() == 1) {
						if (!rowData.get(0).isJsonNull()) {
							resultMap = getResultMapNeo4j(rowData.get(0).getAsLong(), "");//AsString()
						} else {
							log.debug("rowData result is null " + rowData);
						}
					} else {
						int i = 0;
						for (JsonElement key : rowData) {
							log.debug(" key  " + key);
							if (i == 0 && neo4jKpiDefinition.isGroupBy()) {
								groupByFieldVal = key.getAsString();
								i++;
							} else {
								groupByFieldValResult = key.getAsLong();
							}
						}
						resultMap = getResultMapNeo4j(groupByFieldValResult, groupByFieldVal);
					}
					if (!resultMap.isEmpty()) {
						resultList.add(resultMap);
					} else {
						log.debug(" No result calulated ....");
					}
				}
			}
		} catch (Exception e) {
			log.error("Exception while running neo4j operation", e);
		}
		return resultList;
	}

	@Override
	protected Map<String, Object> execute() throws InsightsJobFailedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void executeNeo4jGraphQuery() {
		// TODO Auto-generated method stub

	}

	protected String getNeo4jQueryWithDates(JobSchedule schedule, String neo4jQuery) {
		Long fromDate = InsightsUtils.getDataFromTime(schedule.name());
		Long toDate = InsightsUtils.getDataToTime(schedule.name());
		String whereClause = " WHERE '" + neo4jKpiDefinition.getStartTimeField() + "' > '" + fromDate + "' AND '"
				+ neo4jKpiDefinition.getStartTimeField() + "' < '" + toDate + "'";
		if (neo4jQuery.contains("WHERE")) {
			neo4jQuery = neo4jQuery.replace("WHERE", whereClause + " AND ");
		} else if (neo4jQuery.contains("RETURN") && !neo4jQuery.equalsIgnoreCase("where")) {
			neo4jQuery = neo4jQuery.replace("RETURN", whereClause + " RETURN  ");
		} /*else if (neo4jQuery.equals("return") && !neo4jQuery.equalsIgnoreCase("where")) {
			neo4jQuery = neo4jQuery.replace("return", whereClause + " return  ");
			} else if (neo4jQuery.equals("where")) {
			neo4jQuery = neo4jQuery.replace("where", " WHERE '" + whereClause + " and ");
			} */
		/*
		Neo4jQuery = Neo4jQuery.replace("__dataFromTime__", fromDate.toString());
		
		Neo4jQuery = Neo4jQuery.replace("__dataToTime__", toDate.toString());*/
		return neo4jQuery;
	}

}
