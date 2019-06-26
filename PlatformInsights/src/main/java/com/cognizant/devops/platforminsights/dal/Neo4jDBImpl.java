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
package com.cognizant.devops.platforminsights.dal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.core.enums.ExecutionActions;
import com.cognizant.devops.platformcommons.core.enums.JobSchedule;
import com.cognizant.devops.platformcommons.core.enums.KPIJobResultAttributes;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.NodeData;
import com.cognizant.devops.platforminsights.datamodel.InferenceConfigDefinition;
import com.cognizant.devops.platforminsights.exception.InsightsJobFailedException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Neo4jDBImpl implements DatabaseService {
	Neo4jDBHandler graphDBHandler = new Neo4jDBHandler();
	protected InferenceConfigDefinition inferenceConfigDefinition;
	private final String TIMEZONE = "GMT";
	Gson gson = new Gson();
	JsonParser jsonParser = new JsonParser();

	public Neo4jDBImpl(InferenceConfigDefinition kpiDefinition) {
		this.inferenceConfigDefinition = kpiDefinition;
	}

	public Neo4jDBImpl() {
	}

	private static final Logger log = LogManager.getLogger(Neo4jDBImpl.class);

	@Override
	public List<Map<String, Object>> getResult() {
		List<Map<String, Object>> resultList = new ArrayList<>();
		try {
			String graphQuery = inferenceConfigDefinition.getNeo4jQuery();
			//graphQuery = getNeo4jQueryWithDates(inferenceConfigDefinition.getSchedule(), graphQuery);
			log.debug("Database type found to be Neo4j and graphQuery with date is === " + graphQuery);
			GraphResponse graphResp = graphDBHandler.executeCypherQuery(graphQuery);
			JsonArray errorMessage = graphResp.getJson().getAsJsonArray("errors");
			if (errorMessage.size() >= 1) {
				String errorMessageText = errorMessage.get(0).getAsJsonObject().get("message").getAsString();
				log.error(" Neo4j query execution error for job '" + inferenceConfigDefinition.getName() + "' and error is '"
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
							if (i == 0 && inferenceConfigDefinition.isGroupBy()) {
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
						log.debug(" No result calculated ....");
					}
				}
			}
		} catch (Exception e) {
			log.error("Exception while running neo4j operation", e);
		}
		return resultList;
	}

	protected String getNeo4jQueryWithDates(JobSchedule schedule, String neo4jQuery) {
		Long fromDate = InsightsUtils.getDataFromTime(schedule.name());
		Long toDate = InsightsUtils.getDataToTime(schedule.name());
		String whereClause = " WHERE '" + inferenceConfigDefinition.getStartTimeField() + "' > '" + fromDate + "' AND '"
				+ inferenceConfigDefinition.getStartTimeField() + "' < '" + toDate + "'";
		if (StringUtils.containsIgnoreCase(neo4jQuery, "WHERE")) {
			neo4jQuery = StringUtils.replaceIgnoreCase(neo4jQuery, "WHERE", whereClause + " AND ");
		} else if (StringUtils.containsIgnoreCase(neo4jQuery, "RETURN")
				&& !StringUtils.containsIgnoreCase(neo4jQuery, "WHERE")) {
			neo4jQuery = StringUtils.replaceIgnoreCase(neo4jQuery, "RETURN", whereClause + " RETURN  ");
		}
		return neo4jQuery;
	}

	@Override
	public List<InferenceConfigDefinition> readKPIJobs() {
		List<InferenceConfigDefinition> jobs = new ArrayList<InferenceConfigDefinition>(0);
		try {
			String graphQuery = "MATCH (n:INFERENCE:CONFIG) RETURN n";
			GraphResponse graphResp = graphDBHandler.executeCypherQuery(graphQuery);
			List<NodeData> nodesList = graphResp.getNodes();
			for (NodeData nodeData : nodesList) {
				InferenceConfigDefinition nodemapping = mapNodeDataToInferenceConfigDefinition(nodeData);
				jobs.add(nodemapping);
			}
		} catch (Exception e) {
			log.error("Exception while running neo4j operation", e);
		}
		return jobs;
	}

	@Override
	public void updateJobLastRun(List<InferenceConfigDefinition> jobUpdateList) {

		try {
			Map<String, List<InferenceConfigDefinition>> kpiInferenceResultGroupedOnScheduled = jobUpdateList.stream()
					.collect(Collectors.groupingBy(kpi -> kpi.getSchedule().toString()));
			for (Map.Entry<String, List<InferenceConfigDefinition>> kipInferanceResult : kpiInferenceResultGroupedOnScheduled
					.entrySet()) {
				StringBuffer sbWhereClause = new StringBuffer();
				sbWhereClause.append("[");
				log.debug(" Scheduled  " + kipInferanceResult.getKey() + "  List   " + kipInferanceResult.getValue());
				for (InferenceConfigDefinition kpiDefinition : kipInferanceResult.getValue()) {
					sbWhereClause.append("'").append(kpiDefinition.getKpiID().toString()).append("'").append(",");
				}
				sbWhereClause.setLength(sbWhereClause.length() - 1);
				sbWhereClause.append("]");
				String updateCypherQuery = prepareLastRunQuery(sbWhereClause.toString(),
						kipInferanceResult.getKey().toString());
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
		log.debug(" last run update quey for scheduled " + schedule + "   " + query);
		return query;
	}

	protected Map<String, Object> getResultMapNeo4j(Long result, String groupByValue) {
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(KPIJobResultAttributes.KPIID.toString(), inferenceConfigDefinition.getKpiID());
		resultMap.put(KPIJobResultAttributes.NAME.toString(), inferenceConfigDefinition.getName());
		resultMap.put(KPIJobResultAttributes.EXPECTEDTREND.toString(), inferenceConfigDefinition.getExpectedTrend());
		resultMap.put(KPIJobResultAttributes.ISGROUPBY.toString(), inferenceConfigDefinition.isGroupBy());
		resultMap.put(KPIJobResultAttributes.RESULT.toString(), result);
		resultMap.put(KPIJobResultAttributes.VECTOR.toString(), inferenceConfigDefinition.getVector());
		resultMap.put(KPIJobResultAttributes.TOOLNAME.toString(), inferenceConfigDefinition.getToolName());
		resultMap.put(KPIJobResultAttributes.SCHEDULE.toString(), inferenceConfigDefinition.getSchedule().name());
		resultMap.put(KPIJobResultAttributes.ACTION.toString(), inferenceConfigDefinition.getAction().name());
		resultMap.put(KPIJobResultAttributes.RESULTOUTPUTTYPE.toString(), inferenceConfigDefinition.getResultOutPutType());
		resultMap.put(KPIJobResultAttributes.ISCOMPARISIONKPI.toString(), inferenceConfigDefinition.isComparisionKpi());
		if (inferenceConfigDefinition.isGroupBy()) {
			resultMap.put(KPIJobResultAttributes.GROUPBYFIELDNAME.toString(), inferenceConfigDefinition.getGroupByFieldName());
			resultMap.put(KPIJobResultAttributes.GROUPBYFIELDID.toString(), inferenceConfigDefinition.getGroupByField());
			resultMap.put(KPIJobResultAttributes.GROUPBYFIELDVAL.toString(), groupByValue);
		}
		resultMap.put(KPIJobResultAttributes.RESULTTIME.toString(), InsightsUtils.getTodayTime());
		resultMap.put(KPIJobResultAttributes.RESULTTIMEX.toString(), InsightsUtils.getUtcTime(TIMEZONE));
		return resultMap;
	}
	
	@Override
	public void saveResult(List<Map<String, Object>> resultList) {
		try {
			if (!resultList.isEmpty()) {
				String cypherQuery = "UNWIND {props} AS properties CREATE (n:INFERENCE:RESULTS) set n=properties return count(n)"; //DATA
				List<JsonObject> dataList = new ArrayList<JsonObject>();
				
				for (Map<String, Object> resultMapObject : resultList) {
					String resultMapJsonObject = gson.toJson(resultMapObject);
					JsonElement jsonElement = jsonParser.parse(resultMapJsonObject);
					dataList.add(jsonElement.getAsJsonObject());
				}
				JsonObject graphResponse = graphDBHandler.executeQueryWithData(cypherQuery, dataList);
			} else {
				log.error(" No result to store in neo4j for job : " + inferenceConfigDefinition.getName());
			}
		} catch (GraphDBException | NullPointerException e) {
			log.error("Error while saving neo4j record " + e.getMessage());
		} catch (Exception e) {
			log.error("Error while saving neo4j record " + e.getMessage());
		}
	}

	private InferenceConfigDefinition mapNodeDataToInferenceConfigDefinition(NodeData node) {
		InferenceConfigDefinition neo4jDef = new InferenceConfigDefinition();
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
		neo4jDef.setGroupByField(node.getPropertyMap().get(KPIJobResultAttributes.GROUPBYFIELDID.toString()));
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

}
