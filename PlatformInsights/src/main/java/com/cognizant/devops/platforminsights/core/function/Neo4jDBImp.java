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
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.core.enums.ExecutionActions;
import com.cognizant.devops.platformcommons.core.enums.JobSchedule;
import com.cognizant.devops.platformcommons.core.enums.KPIJobResultAttributes;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.NodeData;
import com.cognizant.devops.platforminsights.core.BaseActionImpl;
import com.cognizant.devops.platforminsights.core.enums.KPIAttributes;
import com.cognizant.devops.platforminsights.datamodel.KPIDefinition;
import com.cognizant.devops.platforminsights.datamodel.Neo4jKPIDefinition;
import com.cognizant.devops.platforminsights.exception.InsightsJobFailedException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Neo4jDBImp extends BaseActionImpl {
	Neo4jDBHandler graphDBHandler = new Neo4jDBHandler();
	public Neo4jDBImp(Neo4jKPIDefinition neo4jKpiDefinition) {
		super(neo4jKpiDefinition);
	}

	public Neo4jDBImp(KPIDefinition kpiDefinition) {
		super(kpiDefinition);
	}

	public Neo4jDBImp() {
		super();
	}

	private static final Logger log = LogManager.getLogger(Neo4jDBImp.class);

	public List<Map<String, Object>> getNeo4jResult() {
		List<Map<String, Object>> resultList = new ArrayList<>();
		try {
			String graphQuery = neo4jKpiDefinition.getNeo4jQuery();
			//graphQuery = getNeo4jQueryWithDates(neo4jKpiDefinition.getSchedule(), graphQuery);
			log.debug("Database type found to be Neo4j and graphQuery with date is=== " + graphQuery);
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
					if (rowData.size() == 1) {
						if (!rowData.get(0).isJsonNull()) {
							resultMap = getResultMapNeo4j(rowData.get(0).getAsLong(), "");//AsString()
						} else {
							log.debug("rowData result is null " + rowData);
						}
					} else {
						int i = 0;
						for (JsonElement key : rowData) {
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
		return null;
	}

	@Override
	protected void executeNeo4jGraphQuery() {

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
		}
		return neo4jQuery;
	}

	public List<Neo4jKPIDefinition> readKPIJobsFromNeo4j() {
		List<Neo4jKPIDefinition> jobs = new ArrayList<Neo4jKPIDefinition>(0);
		try {
			String graphQuery = "MATCH (n:INFERENCE:CONFIG) RETURN n";
			log.debug("Database type found to be Neo4j  === " + graphQuery);
			GraphResponse graphResp = graphDBHandler.executeCypherQuery(graphQuery);
			log.debug(graphResp.getJson());
			List<NodeData> nodesList = graphResp.getNodes();
			for (NodeData nodeData : nodesList) {
				Neo4jKPIDefinition nodemapping = mapNodeData(nodeData);
				jobs.add(nodemapping);
			}
		} catch (Exception e) {
			log.error("Exception while running neo4j operation", e);
		}
		return jobs;
	}

	private Neo4jKPIDefinition mapNodeData(NodeData node) {
		Neo4jKPIDefinition neo4jDef = new Neo4jKPIDefinition();
		neo4jDef.setKpiID(Integer.parseInt(node.getPropertyMap().get(KPIJobResultAttributes.KPIID.toString())));
		neo4jDef.setAction(
				ExecutionActions.valueOf(node.getPropertyMap().get(KPIJobResultAttributes.ACTION.toString())));
		neo4jDef.setActive(Boolean.parseBoolean(node.getPropertyMap().get(KPIJobResultAttributes.ISACTIVE.toString())));
		neo4jDef.setAverageField(node.getPropertyMap().get(KPIJobResultAttributes.AVERAGEFIELD.toString()));
		neo4jDef.setComparisionKpi(
				Boolean.parseBoolean(node.getPropertyMap().get(KPIJobResultAttributes.ISCOMPARISIONKPI.toString())));
		neo4jDef.setEndTimeField(node.getPropertyMap().get(KPIJobResultAttributes.ENDTIMEFIELD.toString()));
		neo4jDef.setExpectedTrend(node.getPropertyMap().get(KPIJobResultAttributes.EXPECTEDTREND.toString()));
		neo4jDef.setGroupBy(
				Boolean.parseBoolean(node.getPropertyMap().get(KPIJobResultAttributes.ISGROUPBY.toString())));
		neo4jDef.setGroupByField(node.getPropertyMap().get(KPIJobResultAttributes.GROUPBYFIELD.toString()));
		neo4jDef.setGroupByFieldName(node.getPropertyMap().get(KPIJobResultAttributes.GROUPBYFIELDNAME.toString()));
		neo4jDef.setLastRunTime(
				Long.parseLong(node.getPropertyMap().get(KPIJobResultAttributes.LASTRUNTIME.toString())));
		neo4jDef.setName(node.getPropertyMap().get(KPIJobResultAttributes.NAME.toString()));
		neo4jDef.setNeo4jLabel(node.getPropertyMap().get(KPIJobResultAttributes.NEO4JLABEL.toString()));
		neo4jDef.setNeo4jQuery(node.getPropertyMap().get(KPIJobResultAttributes.NEO4JQUERY.toString()));
		neo4jDef.setNextRun(node.getPropertyMap().get(KPIJobResultAttributes.NEXTRUN.toString()));
		neo4jDef.setResultOutPutType(node.getPropertyMap().get(KPIJobResultAttributes.RESULTOUTPUTTYPE.toString()));
		neo4jDef.setSchedule(
				JobSchedule.valueOf(node.getPropertyMap().get(KPIJobResultAttributes.SCHEDULE.toString())));
		neo4jDef.setStartTimeField(node.getPropertyMap().get(KPIJobResultAttributes.STARTTIMEFIELD.toString()));
		neo4jDef.setTimeFormat(node.getPropertyMap().get(KPIJobResultAttributes.TIMEFORMAT.toString()));
		neo4jDef.setToolName(node.getPropertyMap().get(KPIJobResultAttributes.TOOLNAME.toString()));
		neo4jDef.setVector(node.getPropertyMap().get(KPIJobResultAttributes.VECTOR.toString()));
		return neo4jDef;
	}

	public void updateJobLastRun(List<Neo4jKPIDefinition> jobUpdateList) {

		try {
			Map<String, List<Neo4jKPIDefinition>> kpiInferenceResultGroupedOnScheduled = jobUpdateList.stream()
					.collect(Collectors.groupingBy(kpi -> kpi.getSchedule().toString()));
			for (Map.Entry<String, List<Neo4jKPIDefinition>> kipInferanceResult : kpiInferenceResultGroupedOnScheduled
					.entrySet()) {
				StringBuffer sbWhereClause = new StringBuffer();
				sbWhereClause.append("[");
				log.debug(" Scheduled  " + kipInferanceResult.getKey() + "  List   " + kipInferanceResult.getValue());
				for (Neo4jKPIDefinition neo4jKPIDefinition : kipInferanceResult.getValue()) {
					sbWhereClause.append("'").append(neo4jKPIDefinition.getKpiID().toString()).append("'").append(",");
				}
				sbWhereClause.setLength(sbWhereClause.length() - 1);
				sbWhereClause.append("]");
				String updateCypherQuery = prepareLastRunQuery(sbWhereClause.toString(),
						kipInferanceResult.getKey().toString());
				log.debug(" query of updated job group wise  " + updateCypherQuery);
				GraphResponse updateGraphResponse = graphDBHandler.executeCypherQuery(updateCypherQuery);
			}

		} catch (Exception e) {
			log.error(" Error while updateJobLastRun  " + e.getMessage());
		}

	}

	public String prepareLastRunQuery(String kpiIds, String schedule) {
		Long currentEpochTime = InsightsUtils.getLastRunTime(schedule);
		String query = " MATCH (n:INFERENCE:CONFIG) where n.kpiID in " + kpiIds + "  SET n.lastRunTime='"
				+ currentEpochTime + "' RETURN count(n)";
		log.debug(" last run update quey " + query);
		return query;
	}

}
