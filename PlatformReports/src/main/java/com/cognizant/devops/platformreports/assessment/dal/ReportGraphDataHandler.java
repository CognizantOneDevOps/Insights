/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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
package com.cognizant.devops.platformreports.assessment.dal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.enums.KPIJobResultAttributes;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.assessmentreport.ReportConfigDAL;
import com.cognizant.devops.platformreports.assessment.datamodel.ContentConfigDefinition;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsKPIConfigDTO;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsKPIResultDetails;
import com.cognizant.devops.platformreports.assessment.datamodel.QueryModel;
import com.cognizant.devops.platformreports.assessment.kpi.InsightsStatusProvider;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineUtils;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ReportGraphDataHandler implements ReportDataHandler {

	private static Logger log = LogManager.getLogger(ReportGraphDataHandler.class);

	GraphDBHandler graphDBHandler = new GraphDBHandler();
	ReportConfigDAL reportConfigDAL = new ReportConfigDAL();

	Gson gson = new Gson();

	public List<JsonObject> fetchData(String graphQuery) {

		List<JsonObject> graphResp = new ArrayList<>(0);
		try {
			JsonObject response = graphDBHandler.executeCypherQueryForJsonResponse(graphQuery);
			graphResp.add(response);
			parseGraphResponseForError(graphResp.get(0));
		} catch (InsightsCustomException | InsightsJobFailedException e1) {
			throw new InsightsJobFailedException(e1.toString());
		} catch (Exception e) {
			throw new InsightsJobFailedException(e.toString());
		}

		return graphResp;
	}

	@Override
	public void saveData(List<JsonObject> resultList) {
		try {
			String query = "UNWIND {props} AS properties " + "CREATE (n:" + ReportEngineUtils.NEO4J_RESULT_LABEL + ") "
					+ "SET n = properties";
			JsonObject graphResponse = graphDBHandler.bulkCreateNodes(resultList, null, query);
			parseGraphResponseForError(graphResponse);
		} catch (InsightsCustomException e) {
			log.error("Error while saving neo4j record {} ", e.getMessage());
			throw new InsightsJobFailedException("Error while saving neo4j record {} " + e.getMessage());
		} catch (Exception e) {
			log.error("Error while saving neo4j record {}", e.getMessage());
			throw new InsightsJobFailedException("Error while saving neo4j record {} " + e.getMessage());
		}
	}

	@Override
	public List<JsonObject> fetchKPIData(String graphQuery, InsightsKPIConfigDTO kpiDefinition, QueryModel model) {
		List<JsonObject> listOfResultJson = new ArrayList<>();
		try {

			log.debug("Worlflow Detail ==== graphQuery with date for KPI {} ==== is === {} ", kpiDefinition.getKpiId(),
					graphQuery);
			if (!kpiDefinition.getInputDatasource().isEmpty()) {
				graphDBHandler = new GraphDBHandler(kpiDefinition.getInputDatasource());
			}
			List<JsonObject> graphResp = fetchData(graphQuery);
			JsonArray graphJsonResult = graphResp.get(0).getAsJsonArray("results");

			JsonArray data = graphJsonResult.get(0).getAsJsonObject().getAsJsonArray("data");
			JsonArray columns = graphJsonResult.get(0).getAsJsonObject().getAsJsonArray("columns");
			log.debug(" Worlflow Detail ==== KPI Id {}  record return by query ==== {} ", kpiDefinition.getKpiId(),
					data.size());
			if (data.size() > 0) {
				listOfResultJson.addAll(creatingResulJsontFromGraphResponce(data, columns, kpiDefinition, model));
			} else {
				log.error("Worlflow Detail ==== No Result Neo4j query returned invalid result for the KPIID {} ",
						kpiDefinition.getKpiId());
				/*InsightsStatusProvider.getInstance().createInsightStatusNode(
						"Neo4j query returned invalid result for the KPIID " + kpiDefinition.getKpiId(),
						PlatformServiceConstants.FAILURE);*/
			}
		} catch (Exception e) {
			log.error("Exception while running neo4j operation {} ", e);
			throw new InsightsJobFailedException("Exception while running neo4j operation {} " + e.getMessage());
		}
		return listOfResultJson;
	}

	@Override
	public List<InsightsKPIResultDetails> fetchKPIResultData(ContentConfigDefinition contentConfigDefinition) {
		List<InsightsKPIResultDetails> kpiDetailList = new ArrayList<>(0);
		ReportGraphDataHandler neo4jExecutor = new ReportGraphDataHandler();
		String query_type = "NEO4J_" + contentConfigDefinition.getCategory().toString();
		String graphQuery = QueryEnum.valueOf(query_type).toString();
		graphQuery = graphQuery.replaceAll(":kpiId", String.valueOf(contentConfigDefinition.getKpiId()))
				.replaceAll(":executionId", String.valueOf(contentConfigDefinition.getExecutionId()))
				.replaceAll(":assessmentId", String.valueOf(contentConfigDefinition.getAssessmentId()));
		log.debug("Worlflow Detail ====  for kpi {} contentId {} graphQuery   {} ", contentConfigDefinition.getKpiId(),
				contentConfigDefinition.getContentId(), graphQuery);
		List<JsonObject> graphResponse = neo4jExecutor.fetchData(graphQuery);
		creatingResultDetailFromGraphResponce(kpiDetailList, contentConfigDefinition, graphResponse.get(0));
		return kpiDetailList;
	}

	@Override
	public void saveContentResult(JsonObject contentResult) {
		try {
			String query = "UNWIND {props} AS properties " + "CREATE (n:" + ReportEngineUtils.NEO4J_CONTENT_RESULT_LABEL
					+ ") " + "SET n = properties";
			JsonObject graphResponse = graphDBHandler.createNodesWithSingleData(contentResult, query);
			parseGraphResponseForError(graphResponse);

		} catch (InsightsCustomException | NullPointerException e) {
			log.error(e);
			log.error("Error while saving neo4j content result  record {} ", e.getMessage());
			throw new InsightsJobFailedException(
					"Error while saving neo4j content result  record {} " + e.getMessage());
		} catch (Exception e) {
			log.error(e);
			log.error("Error while saving neo4j content result record {} ", e.getMessage());
			throw new InsightsJobFailedException("Error while saving neo4j content result record {} " + e.getMessage());
		}
	}

	/**
	 * used to save content result
	 * 
	 * @param resultMap
	 */

	
	void parseGraphResponseForError(JsonObject graphResponse) {
		JsonArray errorMessage = graphResponse.getAsJsonArray("errors");
		if (errorMessage != null && errorMessage.size() >= 1) {
			String errorMessageText = errorMessage.get(0).getAsJsonObject().get("message").getAsString();
			throw new InsightsJobFailedException(errorMessageText);
		}
	}

	/**
	 * Used to parse KPI query Neo4j responce and create result Json Object
	 * 
	 * @param data
	 * @param columns
	 * @return
	 */
	public List<JsonObject> creatingResulJsontFromGraphResponce(JsonArray data, JsonArray columns,
			InsightsKPIConfigDTO kpiDefinition, QueryModel model) {
		List<JsonObject> listOfResultJson = new ArrayList<>();
		JsonObject propertyJson = ReportEngineUtils.getInferencePropertyJson(kpiDefinition);
		for (int dataIndex = 0; dataIndex < data.size(); dataIndex++) {
			JsonObject dataJson = new JsonObject();
			JsonArray rowData = data.get(dataIndex).getAsJsonObject().getAsJsonArray("row");
			for (int rowDataIndex = 0; rowDataIndex < rowData.size(); rowDataIndex++) {
				// Checking if row or column data is null or not
				if (!rowData.get(rowDataIndex).isJsonNull() && !columns.get(rowDataIndex).isJsonNull()) {
					dataJson.add(columns.get(rowDataIndex).getAsString(), rowData.get(rowDataIndex));
				} else {
					log.error("Either row or column data of graph response is not available for the KPI ID {} ",
							kpiDefinition.getKpiId());

				}
			}
			//String resultValue = String.valueOf(dataJson.get(kpiDefinition.getResultField()));
			if (!dataJson.entrySet().isEmpty()) { //&& validateJson(dataJson)
				dataJson.addProperty(KPIJobResultAttributes.RESULTTIME.getValue(), InsightsUtils.getTodayTime());
				dataJson.addProperty(KPIJobResultAttributes.RESULTTIMEX.getValue(),
						InsightsUtils.getUtcTime(ReportEngineUtils.TIMEZONE));
				dataJson.add(ReportEngineUtils.COLUMN_PROPERTY, columns);
				dataJson.addProperty("recordDate", model.getRecordDate());
				dataJson.addProperty("recordDateX", InsightsUtils.insightsTimeXFormat(model.getRecordDate()));

				// merge two json, merge result column with column value and inference Config
				// Property Json
				dataJson = ReportEngineUtils.mergeTwoJson(dataJson, propertyJson);
				listOfResultJson.add(dataJson);
			} else {
				log.error(
						" No result calculated  or  ResultField row value field is null or zero for the KPI ID {}....",
						kpiDefinition.getKpiId());

			}
		}
		return listOfResultJson;
	}

	private boolean validateJson(JsonObject dataJson) {
		boolean retunValue = Boolean.TRUE;
		for (Entry<String, JsonElement> elemant : dataJson.entrySet()) {
			String resultValue = String.valueOf(elemant.getValue());
			//log.debug("Worlflow Detail ====  for kpi resultValue   {} ", resultValue);
			if (resultValue != null && (!resultValue.equalsIgnoreCase("0") && !resultValue.equalsIgnoreCase("0.0")
					&& !resultValue.equalsIgnoreCase(""))) {
				retunValue = Boolean.TRUE;
			} else {
				retunValue = Boolean.FALSE;
				break;
			}
		}

		return retunValue;
	}

	public void creatingResultDetailFromGraphResponce(List<InsightsKPIResultDetails> kpiDetailList,
			ContentConfigDefinition contentConfigDefinition, JsonObject graphResp) {
		JsonArray graphJsonResult = graphResp.getAsJsonArray("results");
		log.debug("Worlflow Detail ====  KPI Id {}  record return by key query ==== {} ",
				contentConfigDefinition.getKpiId(),
				graphResp.getAsJsonArray("results").size());
		JsonArray data = graphJsonResult.get(0).getAsJsonObject().getAsJsonArray("data");
		for (int dataIndex = 0; dataIndex < data.size(); dataIndex++) {
			JsonArray rowData = data.get(dataIndex).getAsJsonObject().getAsJsonArray("row");
			for (int rowDataIndex = 0; rowDataIndex < rowData.size(); rowDataIndex++) {
				try {
					JsonObject row = rowData.get(rowDataIndex).getAsJsonObject();
					InsightsKPIResultDetails resultMapping = gson.fromJson(row, InsightsKPIResultDetails.class);
					JsonArray columnProperty = row.get(ReportEngineUtils.COLUMN_PROPERTY).getAsJsonArray();
					Map<String, Object> results = new HashMap<>();
					for (int i = 0; i < columnProperty.size(); i++) {
						String columnName = columnProperty.get(i).getAsString();
						if (row.get(columnName) != null) {
							Object result = ReportEngineUtils.getJsonValue(row.get(columnName));
							results.put(columnName, result);
						} else {
							log.error(" null value found for {} ", columnName);
						}
					}
					resultMapping.setResults(results);
					kpiDetailList.add(resultMapping);
				} catch (Exception e) {
					log.error(" Error while parsing kpi result {} ", e);
					throw new InsightsJobFailedException(" Error while parsing kpi result {} " + e.getMessage());
				}
			}
		}
	}

	@Override
	public JsonArray fetchVisualizationResults(String query) {

		List<JsonObject> graphResponse = fetchData(query);
		JsonArray graphJsonResult = graphResponse.get(0).getAsJsonArray("results");
		JsonArray data = graphJsonResult.get(0).getAsJsonObject().getAsJsonArray("data");
		JsonArray columns = graphJsonResult.get(0).getAsJsonObject().getAsJsonArray("columns");
		return creatingVisualizationJsontFromGraphResponce(data, columns);

	}

	@Override
	public JsonArray fetchVisualizationResults(long executionId, int kpiId, int assessmentId) {
		String vQuery = "";
		vQuery = QueryEnum.valueOf(QueryEnum.NEO4J_VCONTENTQUERY.name()).toString();
		vQuery = vQuery.replace(":kpiId", String.valueOf(kpiId)).replace(":executionId", String.valueOf(executionId))
				.replace(":assessmentId", String.valueOf(assessmentId));
		log.debug("Worlflow Detail ==== content Visualization query {}   ", vQuery);
		return fetchVisualizationResults(vQuery);
	}

	// Check time field and convert to date
	public JsonArray creatingVisualizationJsontFromGraphResponce(JsonArray data, JsonArray columns) {
		JsonArray listOfResultJson = new JsonArray();
		JsonArray resultArray = new JsonArray();
		for (int dataIndex = 0; dataIndex < data.size(); dataIndex++) {
			JsonObject rowObject = new JsonObject();
			JsonArray rowData = data.get(dataIndex).getAsJsonObject().getAsJsonArray("row");
			rowObject.add("row", rowData);
			resultArray.add(rowObject);
		}
		if (resultArray.size() > 0) {
			JsonObject kpiResultObject = new JsonObject();
			kpiResultObject.add("columns", columns);
			kpiResultObject.add("data", resultArray);
			listOfResultJson.add(kpiResultObject);
		}
		return listOfResultJson;
	}
}
