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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
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
	GraphDBHandler dbHandler = new GraphDBHandler();
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

	public abstract void generateContent();

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
		log.debug("Worlflow Detail ==== In Content, kpi result return for kpi {} is {}",
				contentConfigDefinition.getKpiId(), inferenceDetailList.size());
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
				log.error("Worlflow Detail ==== In Content, no result foundg {} and data json is null {} ",
						contentResult.getResultValuesMap(),
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
		detail.setKpiName(inferenceResult.getKpiName());
		detail.setNoOfResult(contentConfigDefinition.getNoOfResult());
		detail.setRanking(null);//ranking
		detail.setResultField(contentConfigDefinition.getResultField());
		detail.setResultTime(inferenceResult.getResultTime());
		detail.setResultTimeX(inferenceResult.getResultTimeX());
		detail.setResultValuesMap(resultValuesMap);
		detail.setSchedule(inferenceResult.getSchedule());
		detail.setSentiment(sentiment);
		detail.setThreshold(contentConfigDefinition.getThreshold());
		detail.setToolName(inferenceResult.getToolname());
		detail.setTrendline(null);//trendline
		detail.setGroup(inferenceResult.getGroupName());
		detail.setExecutionId(contentConfigDefinition.getExecutionId());
		detail.setReportId(contentConfigDefinition.getReportId());
		detail.setAssessmentId(contentConfigDefinition.getAssessmentId());
		return detail;
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
	public InsightsContentDetail setNeutralContentDetail() {
		InsightsContentDetail detail = new InsightsContentDetail();
		try {
			Map<String, Object> resultValuesMap = new HashMap<>();
			String inferenceContentText = getContentText(ReportEngineUtils.NEUTRAL_MESSAGE_KEY, resultValuesMap);
			detail.setCategory(contentConfigDefinition.getCategory());
			detail.setActualTrend(ReportEngineEnum.KPITrends.NORESULT.getValue());
			detail.setExpectedTrend(contentConfigDefinition.getExpectedTrend());
			detail.setContentId(contentConfigDefinition.getContentId());
			detail.setInferenceText(inferenceContentText);
			detail.setKpiId(contentConfigDefinition.getKpiId().longValue());
			//detail.setKpiName(inferenceResult.getKpiName());
			detail.setNoOfResult(contentConfigDefinition.getNoOfResult());
			detail.setRanking(null);//ranking
			detail.setResultField(contentConfigDefinition.getResultField());
			//detail.setResultTime(inferenceResult.getResultTime());
			//detail.setResultTimeX(inferenceResult.getResultTimeX());
			detail.setResultValuesMap(resultValuesMap);
			//detail.setSchedule(inferenceResult.getSchedule());
			detail.setSentiment(ReportEngineEnum.KPISentiment.NEUTRAL);
			detail.setThreshold(contentConfigDefinition.getThreshold());
			//detail.setToolName(inferenceResult.getToolname());
			detail.setTrendline(null);//trendline
			//detail.setGroup(inferenceResult.getGroupName());
			detail.setExecutionId(contentConfigDefinition.getExecutionId());
			detail.setReportId(contentConfigDefinition.getReportId());
			detail.setAssessmentId(contentConfigDefinition.getAssessmentId());
		} catch (Exception e) {//InsightsCustom
			log.error(" Error while setNeutralContentDetail {} ", e);
		}
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
	public String getContentText(String key, Map<String, Object> resultValuesMap) {
		String contentText = null;
		try {
			String messageConfigText = "";
			resultValuesMap = resultValuesMap.entrySet().stream()
					.collect(Collectors.toMap(Map.Entry::getKey, e -> getResultValueForDisplay(e.getValue())));
			StringSubstitutor sub = new StringSubstitutor(resultValuesMap, "{", "}");
			JsonObject valueMessageObject = jsonParser
					.parse(String.valueOf(contentConfigDefinition.getMessage()))
					.getAsJsonObject();
			JsonElement messageInference = valueMessageObject.get(key);
			if (messageInference != null) {
				messageConfigText = messageInference.getAsString();
			} else if (key.equalsIgnoreCase(ReportEngineUtils.NEUTRAL_MESSAGE_KEY)) {
				log.error("Worlflow Detail ==== In Content,No data found for content - {} ",
						contentConfigDefinition.getContentName());
				messageConfigText = "No data found for content  - " + contentConfigDefinition.getContentName();
			} else {
				log.error("Worlflow Detail ==== In Content, Error while getting message text for key {} ", key);
				messageConfigText = "No message found for content  - " + contentConfigDefinition.getContentName();
			}
			contentText = sub.replace(messageConfigText);
		} catch (Exception e) {
			log.error("Worlflow Detail ==== In Content, Error while getting contentText {} ", e);
		}
		return contentText;

	}
	
	public String getResultFieldFromContentDefination() {
		return contentConfigDefinition.getResultField();
	}

	boolean isInteger(double number) {
		return number % 1 == 0;// if the modulus(remainder of the division) of the argument(number) with 1 is 0 then return true otherwise false.
	}

	public boolean isNumeric(String strNum) {
		if (strNum == null) {
			return false;
		}
		try {
			double d = Double.parseDouble(strNum);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public String getResultValueForDisplay(Object value){
		String returnValue;
		try {
			//log.debug("value   {} ", value);
			if (!isNumeric(String.valueOf(value))) {
				returnValue= String.valueOf(value);
			} else {
				Double newData = new Double(String.valueOf(value));
				if (isInteger(newData.doubleValue())) {
					returnValue = String.valueOf(newData.intValue());
				} else {
					returnValue = String.valueOf(newData.doubleValue());
				}
			}
			//log.debug("returnValue   {} ", returnValue);
		} catch (Exception e) {
			log.error(
					"Worlflow Detail ==== In Content, Exception in getResultValueForDisplay for content {} === value === {}  ",
					contentConfigDefinition.getContentId(), value);
			returnValue = String.valueOf(value);
		}
		return returnValue;
	}

}
