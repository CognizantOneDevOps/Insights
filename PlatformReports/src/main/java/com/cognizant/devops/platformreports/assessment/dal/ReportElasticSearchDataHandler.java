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
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.core.enums.JobSchedule;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformdal.dal.ElasticSearchNativeHandler;
import com.cognizant.devops.platformreports.assessment.datamodel.ContentConfigDefinition;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsKPIConfigDTO;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsKPIResultDetails;
import com.cognizant.devops.platformreports.assessment.datamodel.QueryModel;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineUtils;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ReportElasticSearchDataHandler implements ReportDataHandler {
	private static Logger log = LogManager.getLogger(ReportElasticSearchDataHandler.class);

	ElasticSearchNativeHandler deoES = new ElasticSearchNativeHandler();
	Gson gson = new Gson();

	public List<InsightsKPIResultDetails> getInferenceResult(ContentConfigDefinition inferenceContentConfigDefinition) {
		List<InsightsKPIResultDetails> inferenceDetailList = new ArrayList<>(0);
		return inferenceDetailList;
	}

	protected String getEsQueryWithDates(JobSchedule schedule, String esQuery) {

		Long fromDate = InsightsUtils.getDataFromTime(schedule.name());
		esQuery = esQuery.replace("__dataFromTime__", fromDate.toString());
		Long toDate = InsightsUtils.getDataToTime(schedule.name());
		esQuery = esQuery.replace("__dataToTime__", toDate.toString());
		return esQuery;
	}

	
	@Override
	public void saveData(List<JsonObject> resultList) {
		try {

			String indexName = ReportEngineUtils.ES_KPI_RESULT_INDEX;
			deoES.saveESResult(indexName.toLowerCase(), resultList);

		} catch (Exception e) {
			log.error("Error while saving saving KPI record {}", e.getMessage());
			throw new InsightsJobFailedException("Error while saving neo4j record {} " + e.getMessage());
		}
	}

	@Override
	public List<JsonObject> fetchKPIData(String query, InsightsKPIConfigDTO kpiDefinition , QueryModel model) {
		return null;
	}

	@Override
	public List<InsightsKPIResultDetails> fetchKPIResultData(ContentConfigDefinition contentConfigDefinition) {
		List<InsightsKPIResultDetails> kpiResultDetailList = new ArrayList<>(0);
		List<JsonObject> kpiResponce = new ArrayList<>();
		try {
			String query_type = "ES_" + contentConfigDefinition.getCategory().toString();
			String esQuery = QueryEnum.valueOf(query_type).toString();
			esQuery = esQuery.replaceAll("%kpiId%", String.valueOf(contentConfigDefinition.getKpiId()))
					.replaceAll("%executionId%", String.valueOf(contentConfigDefinition.getExecutionId()));
			log.debug("Worlflow Detail ==== In ES, esQuery {} ", esQuery);

			kpiResponce = deoES.getESResult(esQuery, ReportEngineUtils.ES_KPI_RESULT_INDEX);
			creatingResultDetailFromESResponce(kpiResultDetailList, contentConfigDefinition, kpiResponce);
			log.debug("Worlflow Detail ==== In ES, Number of KPI result record return  {} ",
					kpiResultDetailList.size());
		} catch (Exception e) {
			log.error(" Error while parsing and fetchKPIResultData for ES {} ", e);
		}
		return kpiResultDetailList;
	}

	@Override
	public void saveContentResult(JsonObject contentResult) {
		try {
			log.debug("Worlflow Detail ==== In ES,  saveContentResult started ");
			List<JsonObject> rows = new ArrayList<>();
			rows.add(contentResult);
			String indexName = ReportEngineUtils.ES_CONTENT_RESULT_INDEX;
			deoES.saveESResult(indexName.toLowerCase(), rows);
			log.debug("Worlflow Detail ==== In ES,  saveContentResult completed ");
		} catch (Exception e) {
			log.error("Error while saving Content Result record {}", e.getMessage());
			throw new InsightsJobFailedException("Error while saving neo4j record {} " + e.getMessage());
		}
	}

	public void creatingResultDetailFromESResponce(List<InsightsKPIResultDetails> kpiResultDetailList,
			ContentConfigDefinition contentConfigDefinition, List<JsonObject> kpiResponce) {

		log.debug("Worlflow Detail ==== In ES, KPI Id {} number of record return by kpi query ==== {} ",
				contentConfigDefinition.getKpiId(), kpiResponce.size());

		for (JsonObject jsonObject : kpiResponce) {
			try {
				InsightsKPIResultDetails resultMapping = gson.fromJson(jsonObject, InsightsKPIResultDetails.class);
				JsonArray columnProperty = jsonObject.get(ReportEngineUtils.COLUMN_PROPERTY).getAsJsonArray();
				Map<String, Object> results = new HashMap<>();
				for (int i = 0; i < columnProperty.size(); i++) {
					String columnName = columnProperty.get(i).getAsString();
					if (jsonObject.get(columnName) != null) {
						Object result = ReportEngineUtils.getJsonValue(jsonObject.get(columnName));
						results.put(columnName, result);
					} else {
						log.error(" null value found for {} ", columnName);
					}
				}
				resultMapping.setResults(results);
				kpiResultDetailList.add(resultMapping);
			} catch (Exception e) {
				log.error(" Error while parsing inference result for ES {} ", e);
				//throw new InsightsJobFailedException(" Error while parsing inference result {} " + e);
			}
		}

	}

	@Override
	public JsonArray fetchVisualizationResults(String query) {

		List<JsonObject> queryResponce = deoES.getESResult(query, ReportEngineUtils.ES_KPI_RESULT_INDEX);
		return creatingVisualizationJsontFromGraphResponce(queryResponce);
	}

	@Override
	public JsonArray fetchVisualizationResults(long executionId, int kpiId, int assessmentId) {
		String vQuery = "";
		vQuery = QueryEnum.valueOf(QueryEnum.ES_VCONTENTQUERY.name()).toString();
		vQuery = vQuery.replaceAll("%kpiId%", String.valueOf(kpiId))
				.replaceAll("%executionId%", String.valueOf(executionId));
		vQuery = vQuery.replace(":kpiId", String.valueOf(kpiId)).replace(":executionId", String.valueOf(executionId));
		List<JsonObject> queryResponce = deoES.getESResult(vQuery, ReportEngineUtils.ES_CONTENT_RESULT_INDEX);
		return creatingVisualizationJsontFromGraphResponce(queryResponce);
	}

	// Check time field and convert to date
	public JsonArray creatingVisualizationJsontFromGraphResponce(List<JsonObject> queryResponce) {
		JsonArray listOfResultJson = new JsonArray();
		JsonArray resultArray = new JsonArray();
		JsonArray columns = new JsonArray();
		if (!queryResponce.isEmpty()) {
			for (JsonObject jsonObject : queryResponce) {
				resultArray.add(jsonObject);
			}
			//get column array 

			JsonObject firstRecord = queryResponce.get(0).getAsJsonObject();
			Set<String> columnSet = firstRecord.keySet();
			for (String string : columnSet) {
				columns.add(string);
			}
			JsonObject kpiResultObject = new JsonObject();
			kpiResultObject.add("columns", columns);
			kpiResultObject.add("data", resultArray);
			listOfResultJson.add(kpiResultObject);
		}

		return listOfResultJson;
	}

}
