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
package com.cognizant.devops.platformreports.assessment.content;

import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformreports.assessment.dal.ReportDataHandler;
import com.cognizant.devops.platformreports.assessment.dal.ReportDataHandlerFactory;
import com.cognizant.devops.platformreports.assessment.datamodel.ContentConfigDefinition;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsContentDetail;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsKPIResultDetails;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineEnum;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineUtils;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public abstract class BaseContentCategoryImpl {

	private static Logger log = LogManager.getLogger(BaseContentCategoryImpl.class);

	protected ContentConfigDefinition contentConfigDefinition;
	Neo4jDBHandler dbHandler = new Neo4jDBHandler();
	JsonParser jsonParser = new JsonParser();
	String datasource = ApplicationConfigProvider.getInstance().getAssessmentReport().getOutputDatasource();
	ReportDataHandler datasourceDataHandler = ReportDataHandlerFactory.getDataSource(datasource);
	Gson gson = new Gson();
	ObjectMapper oMapper = new ObjectMapper();

	public BaseContentCategoryImpl() {

	}

	public BaseContentCategoryImpl(ContentConfigDefinition inferenceContentConfigDefinition) {
		this.contentConfigDefinition = inferenceContentConfigDefinition;
	}

	public abstract void generateContent() throws InsightsJobFailedException;

	public ContentConfigDefinition getContentConfig() {
		return this.contentConfigDefinition;
	}


	/**
	 * Fetch KPI result for content execution and convert DB record into
	 * InsightsKPIResultDetails model
	 * 
	 * @return
	 */
	public List<InsightsKPIResultDetails> getKPIExecutionResult() {
		List<InsightsKPIResultDetails> inferenceDetailList = null;
		log.debug("Worlflow Detail ==== In Content, fetch record for contentid {}  kpi {} content category {}  ",
				contentConfigDefinition.getCategory(), contentConfigDefinition.getKpiId(),
				contentConfigDefinition.getContentId());
		inferenceDetailList = datasourceDataHandler.fetchKPIResultData(contentConfigDefinition);
		return inferenceDetailList;
	}


	/**
	 * Merge content json and result JSON. Prepare content json object and store it
	 * in Graph or ES DB
	 * 
	 * @param contentResult
	 */
	public void saveContentResult(InsightsContentDetail contentResult) {
		try {
			String json = oMapper.writeValueAsString(contentResult);
			JsonObject contentDataJson = jsonParser.parse(json).getAsJsonObject();
			if (!contentResult.getResultValuesMap().isEmpty() && contentDataJson != null) {
				JsonObject resultValueJson = jsonParser
						.parse(oMapper.writeValueAsString(contentResult.getResultValuesMap())).getAsJsonObject();
				contentDataJson = ReportEngineUtils.mergeTwoJson(contentDataJson, resultValueJson);
			} else {
				log.error(" no result foundg {} and data json is null {} ", contentResult.getResultValuesMap(),
						contentDataJson);
			}
			datasourceDataHandler.saveContentResult(contentDataJson);
		} catch (Exception e) {
			log.error(" Error while saveContentResult {} ", e);
			throw new InsightsJobFailedException(
					" Error while saveContentResult for content Id {} " + contentResult.getContentId());
		}
	}

	/**
	 * Method is convert content model to entity model, further this entity model
	 * store in database
	 * 
	 * @param inferenceResult
	 * @param resultValuesMap
	 * @param sentiment
	 * @param actualTrend
	 * @param inferenceText
	 * @return
	 */
	public InsightsContentDetail setContentDetail(InsightsKPIResultDetails inferenceResult,
			Map<String, Object> resultValuesMap, ReportEngineEnum.KPISentiment sentiment, String actualTrend,
			String inferenceText) {

		InsightsContentDetail detail = new InsightsContentDetail();
		detail.setCategory(contentConfigDefinition.getCategory());
		detail.setActualTrend(actualTrend);
		detail.setExpectedTrend(contentConfigDefinition.getExpectedTrend());
		detail.setContentId(contentConfigDefinition.getContentId());		
		detail.setInferenceText(inferenceText);
		detail.setKpiId(inferenceResult.getKpiId());
		detail.setKpiName(inferenceResult.getName());
		detail.setNoOfResult(contentConfigDefinition.getNoOfResult());
		detail.setRanking(null);//ranking
		detail.setResultField(contentConfigDefinition.getResultField());
		detail.setResultTime(inferenceResult.getResultTime());
		detail.setResultTimeX(inferenceResult.getResultTimeX());
		detail.setResultValuesMap(resultValuesMap);
		detail.setSchedule(inferenceResult.getSchedule());
		detail.setSentiment(sentiment);
		detail.setThreshold(contentConfigDefinition.getThreshold());
		detail.setToolName(inferenceResult.getToolName());
		detail.setTrendline(null);//trendline
		detail.setGroup(inferenceResult.getGroup());
		detail.setExecutionId(contentConfigDefinition.getExecutionId());
		return detail;
	}

	/**
	 * Prepare content text based on kpi result property
	 * 
	 * @param key
	 * @param resultValuesMap
	 * @return
	 * @throws InsightsCustomException
	 */
	public String getContentText(String key, Map<String, Object> resultValuesMap) throws InsightsCustomException {
		String inferenceText = null;
		try {
			StringSubstitutor sub = new StringSubstitutor(resultValuesMap, "{", "}");
			JsonObject valueMessageObject = jsonParser
					.parse(String.valueOf(contentConfigDefinition.getMessage()))
					.getAsJsonObject();
			JsonElement messageInference = valueMessageObject.get(key);
			if (messageInference != null) {
				inferenceText = sub.replace(messageInference.getAsString());
			} else {
				log.error(" Error while getting message text for key {} ", key);
				throw new InsightsCustomException(" Content message not configured ");
			}
		} catch (Exception e) {
			log.error(" Error while getting contentText {} ", e);
		}
		return inferenceText;

	}
	
	public String getResultFieldFromContentDefination() {
		return contentConfigDefinition.getResultField();
	}

}
