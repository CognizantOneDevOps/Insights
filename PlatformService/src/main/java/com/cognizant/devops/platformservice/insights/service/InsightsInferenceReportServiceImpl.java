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
package com.cognizant.devops.platformservice.insights.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.assessmentreport.InsightsAssessmentConfiguration;
import com.cognizant.devops.platformdal.assessmentreport.InsightsReportsKPIConfig;
import com.cognizant.devops.platformdal.assessmentreport.ReportConfigDAL;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Service("insightsInferenceReportService")
public class InsightsInferenceReportServiceImpl implements InsightsInferenceService {

	private static final Logger log = LogManager.getLogger(InsightsInferenceReportServiceImpl.class);
	private static final String ASSESSMENT_REPORT_NAME = "Report_Grafana_Inference";
	Gson gson = new Gson();
	ReportConfigDAL reportConfigDAL = new ReportConfigDAL();
	GraphDBHandler graphDBHandler = new GraphDBHandler();

	@Override
	public List<InsightsInference> getInferenceDetails(String schedule) {
		return getInferences("");
	}

	@Override
	public List<InsightsInference> getInferenceDetailsVectorWise(String schedule, String group) {
		return getInferences(group);
	}

	/**
	 * Method to get Inferences
	 * 
	 * @param group
	 * @return List<InsightsInference>
	 */
	private List<InsightsInference> getInferences(String group) {
		List<InsightsInference> inferences = new ArrayList<>(0);
		InsightsInference insightsInference = new InsightsInference();
		List<InsightsInferenceDetail> contentResults = new ArrayList<>();
		try {
			InsightsAssessmentConfiguration assessmentReport = reportConfigDAL
					.getAssessmentByAssessmentName(ASSESSMENT_REPORT_NAME);
			int assessmentId = assessmentReport.getId();
			contentResults = getContentResults(assessmentId, group);
			log.debug(" Content result return for assessmentId {} and group {} is {} ", assessmentId, group,
					contentResults.size());
			if (!contentResults.isEmpty()) {
				List<InsightsKPIResultDetails> kpiResultList = getKPIDetails(assessmentReport, group);
				Map<Long, List<InsightsKPIResultDetails>> kpiResultsMap = getMapOfKpiResults(kpiResultList);
				List<InsightsInferenceDetail> inferenceDetails = getInferenceDetailsFromKpiAndContent(contentResults,
						kpiResultsMap);
				insightsInference.setHeading(group);
				insightsInference.setInferenceDetails(inferenceDetails);
				insightsInference.setRanking(1);
				inferences.add(insightsInference);
			}
		} catch (Exception e) {
			log.error("Problem getting content or kpi results in inference report", e);
		}
		return inferences;
	}

	/**
	 * Method to get Content Results
	 * 
	 * @param assessmentId
	 * @param group
	 * @return List<InsightsInferenceDetail>
	 */
	private List<InsightsInferenceDetail> getContentResults(int assessmentId, String group) {
		List<InsightsInferenceDetail> contentResults = new ArrayList<>();
		try {
			String graphQuery = getQueryForContentResult(assessmentId, group);
			log.debug(" graphQuery {} ", graphQuery);
			GraphResponse graphResp = graphDBHandler.executeCypherQuery(graphQuery);
			JsonArray errorMessage = graphResp.getJson().getAsJsonArray("errors");
			if (errorMessage.size() >= 1) {
				String errorMessageText = errorMessage.get(0).getAsJsonObject().get("message").getAsString();
				log.error(" error while executing query and error is {} ", errorMessageText);
				throw new InsightsCustomException(errorMessageText);
			}
			JsonArray graphJsonResult = graphResp.getJson().getAsJsonArray("results");
			contentResults = parseContentResultResponse(graphJsonResult);
			log.debug("Created content result list");
		} catch (Exception e) {
			log.error(" error while executing query in getContentResults {} {} ", e.getCause(), e.getMessage());
		}

		return contentResults;
	}

	/**
	 * Method to get Query for fetching Content Results
	 * 
	 * @param assessmentId
	 * @param group
	 * @return String
	 */
	private String getQueryForContentResult(int assessmentId, String group) {
		String cypherQuery = "MATCH (n:CONTENT_RESULT)";
		cypherQuery = cypherQuery + " where n.assessmentId=" + assessmentId + " ";
		cypherQuery = cypherQuery + " with distinct max(n.executionId) as latestexecutionId "
				+ "	Match (b:CONTENT_RESULT) where b.executionId =latestexecutionId";
		cypherQuery = cypherQuery + " and b.group='" + group + "' ";
		cypherQuery = cypherQuery + " RETURN b  order by b.executionId,b.kpiId desc ";
		return cypherQuery;
	}

	/**
	 * Method to parse Content Result Response
	 * 
	 * @param graphJsonResult
	 * @return List<InsightsInferenceDetail>
	 * @throws InsightsCustomException
	 */
	private List<InsightsInferenceDetail> parseContentResultResponse(JsonArray graphJsonResult)
			throws InsightsCustomException {

		List<InsightsInferenceDetail> contentResultDetails = new ArrayList<>();
		JsonArray data = graphJsonResult.get(0).getAsJsonObject().getAsJsonArray("data");
		for (int dataIndex = 0; dataIndex < data.size(); dataIndex++) {
			JsonArray rowData = data.get(dataIndex).getAsJsonObject().getAsJsonArray("row");
			for (int rowDataIndex = 0; rowDataIndex < rowData.size(); rowDataIndex++) {
				try {
					JsonObject row = rowData.get(rowDataIndex).getAsJsonObject();
					InsightsInferenceDetail resultMapping = gson.fromJson(row, InsightsInferenceDetail.class);
					contentResultDetails.add(resultMapping);
				} catch (Exception e) {
					log.error(" Error while parsing Content result ", e);
					throw new InsightsCustomException(" Error while parsing Content result {} " + e.getMessage());
				}
			}
		}
		return contentResultDetails;
	}

	/**
	 * Method to get KPI Details
	 * 
	 * @param assessmentReport
	 * @param group
	 * @return List<InsightsKPIResultDetails>
	 * @throws InsightsCustomException
	 */
	private List<InsightsKPIResultDetails> getKPIDetails(InsightsAssessmentConfiguration assessmentReport, String group)
			throws InsightsCustomException {
		List<Integer> kpiIds = new ArrayList<>();

		Set<InsightsReportsKPIConfig> reportsKPIConfigSet = assessmentReport.getReportTemplateEntity()
				.getReportsKPIConfig();
		reportsKPIConfigSet.forEach(reportKpi -> kpiIds.add(reportKpi.getKpiConfig().getKpiId()));

		String graphQuery = getQueryForKPIResults(kpiIds, group, assessmentReport.getId());
		log.debug(" graphQuery for KPI in InsightsInferenceReport {} ", graphQuery);
		JsonObject kpiResultsJson = getKpiQueryResults(graphQuery);
		return creatingKpiResultDetailList(kpiResultsJson);

	}

	/**
	 * Method to get Query for fetching KPI Results
	 * 
	 * @param kpiIds
	 * @param group
	 * @param assessmentId
	 * @return String
	 */
	private String getQueryForKPIResults(List<Integer> kpiIds, String group, int assessmentId) {
		String cypherQuery = "MATCH (n:KPI:RESULTS) where n.kpiId in " + kpiIds;
		cypherQuery = cypherQuery + " and n.groupName='" + group + "'";
		cypherQuery = cypherQuery + " and  n.assessmentId=" + assessmentId + " ";
		cypherQuery = cypherQuery + " RETURN n order by n.executionId desc ";
		return cypherQuery;
	}

	/**
	 * Method to get KPI Results 
	 * 
	 * @param graphQuery
	 * @return JsonObject
	 * @throws InsightsCustomException
	 */
	private JsonObject getKpiQueryResults(String graphQuery) throws InsightsCustomException {
		JsonObject response = null;
		try {
			response = graphDBHandler.executeCypherQueryForJsonResponse(graphQuery);
			parseGraphResponseForError(response);
		} catch (InsightsCustomException e1) {
			log.error(e1);
			throw new InsightsCustomException(e1.toString());
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(e.toString());
		}

		return response;
	}

	public List<InsightsKPIResultDetails> creatingKpiResultDetailList(JsonObject kpiResultsJson) {
		List<InsightsKPIResultDetails> kpiDetailList = new ArrayList<>();
		JsonArray graphJsonResult = kpiResultsJson.getAsJsonArray("results");
		JsonArray data = graphJsonResult.get(0).getAsJsonObject().getAsJsonArray("data");
		for (int dataIndex = 0; dataIndex < data.size(); dataIndex++) {
			JsonArray rowData = data.get(dataIndex).getAsJsonObject().getAsJsonArray("row");
			for (int rowDataIndex = 0; rowDataIndex < rowData.size(); rowDataIndex++) {
				try {
					JsonObject row = rowData.get(rowDataIndex).getAsJsonObject();
					InsightsKPIResultDetails resultMapping = gson.fromJson(row, InsightsKPIResultDetails.class);
					JsonArray columnProperty = row.get("columnProperty").getAsJsonArray();
					Map<String, Object> results = new HashMap<>();
					for (int i = 0; i < columnProperty.size(); i++) {
						String columnName = columnProperty.get(i).getAsString();
						if (row.get(columnName) != null) {
							Object result = getJsonValue(row.get(columnName));
							results.put(columnName, result);
						}
					}
					resultMapping.setResults(results);
					kpiDetailList.add(resultMapping);
				} catch (Exception e) {
					log.error(" Error while parsing kpi result ", e);
				}
			}
		}
		log.debug(" KPI record return for infrence graph is {} ", kpiDetailList.size());
		return kpiDetailList;
	}

	private Map<Long, List<InsightsKPIResultDetails>> getMapOfKpiResults(List<InsightsKPIResultDetails> kpiResultList) {
		return kpiResultList.stream().collect(Collectors.groupingBy(InsightsKPIResultDetails::getKpiId));
	}

	void parseGraphResponseForError(JsonObject graphResponse) throws InsightsCustomException {
		JsonArray errorMessage = graphResponse.getAsJsonArray("errors");
		if (errorMessage != null && errorMessage.size() >= 1) {
			String errorMessageText = errorMessage.get(0).getAsJsonObject().get("message").getAsString();
			throw new InsightsCustomException(errorMessageText);
		}
	}

	public static Object getJsonValue(JsonElement jsonElementResult) {
		Object result = null;
		if (jsonElementResult.isJsonPrimitive()) {
			if (jsonElementResult.getAsJsonPrimitive().isBoolean())
				return jsonElementResult.getAsBoolean();
			if (jsonElementResult.getAsJsonPrimitive().isString())
				return jsonElementResult.getAsString();
			if (jsonElementResult.getAsJsonPrimitive().isNumber()) {
				return jsonElementResult.getAsLong();
			}
		} else {
			result = jsonElementResult.getAsString();
		}
		return result;
	}

	private List<InsightsInferenceDetail> getInferenceDetailsFromKpiAndContent(
			List<InsightsInferenceDetail> contentResults, Map<Long, List<InsightsKPIResultDetails>> kpiResultsMap) {
		for (InsightsInferenceDetail contentResult : contentResults) {
			String resultField = contentResult.getResultField();
			List<InsightsKPIResultDetails> kpiResultListForContent = kpiResultsMap.get(contentResult.getKpiId());
			List<ResultSetModel> resultSetValues = getResultSet(resultField, kpiResultListForContent);
			Collections.reverse(resultSetValues);
			contentResult.setTrendline(getTrend(contentResult.getActualTrend()));
			contentResult.setInference(contentResult.getInferenceText());
			contentResult.setResultSet(resultSetValues);
		}
		return contentResults;
	}

	private List<ResultSetModel> getResultSet(String resultField,
			List<InsightsKPIResultDetails> kpiResultListForContent) {
		List<ResultSetModel> resultValues = new ArrayList<>();
		int counter = 0;
		for (InsightsKPIResultDetails kpiresult : kpiResultListForContent) {
			if (counter < 5) {
				ResultSetModel model = new ResultSetModel();
				Map<String, Object> resultMap = kpiresult.getResults();
				model.setValue(Long.valueOf(resultMap.get(resultField).toString()));
				model.setResultDate(new Date(kpiresult.getRecordDate() * 1000));
				resultValues.add(model);
				counter++;
			} else {
				break;
			}
		}
		return resultValues;
	}

	private String getTrend(String actualTrend) {

		if ("POSITIVE".equalsIgnoreCase(actualTrend) || "UPWARDS".equalsIgnoreCase(actualTrend)) {
			return "Low to High";
		} else if ("NEGATIVE".equalsIgnoreCase(actualTrend) || "DOWNWARDS".equalsIgnoreCase(actualTrend)) {
			return "High to Low";
		} else {
			return "No Change";
		}

	}

}
