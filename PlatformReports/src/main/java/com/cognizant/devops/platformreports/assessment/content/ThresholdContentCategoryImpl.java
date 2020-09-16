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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum.WorkflowSchedule;
import com.cognizant.devops.platformreports.assessment.datamodel.ContentConfigDefinition;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsContentDetail;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsKPIResultDetails;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineEnum;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineUtils;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;

public class ThresholdContentCategoryImpl extends BaseContentCategoryImpl {
	private static Logger log = LogManager.getLogger(ThresholdContentCategoryImpl.class);

	public ThresholdContentCategoryImpl(ContentConfigDefinition inferenceContentConfigDefinition) {
		super(inferenceContentConfigDefinition);
	}

	/**
	 * Generate Threshold content text using KPI result
	 */
	@Override
	public void generateContent() {

		List<InsightsKPIResultDetails> inferenceResults = getKPIExecutionResult();
		InsightsContentDetail contentResult = null;
		if (!inferenceResults.isEmpty()) {
			contentResult = getContentFromResult(inferenceResults);
		} else {
			contentResult = setNeutralContentDetail();
			log.debug("Worlflow Detail ====   No kpi result found for kpi Id {} ContentId {} ",
					contentConfigDefinition.getKpiId(), contentConfigDefinition.getContentId());
		}
		if (contentResult != null) {
			log.debug("Worlflow Detail ====  contentid {}  kpi {} contentResultText  + {} ",
					contentConfigDefinition.getCategory(), contentConfigDefinition.getKpiId(),
					contentResult.getInferenceText());
			saveContentResult(contentResult);
		}
	}

	/**
	 * process KPI result based on Threshold category to generate content object
	 * 
	 * @param kpiResultDetailsList
	 * @return
	 */
	private InsightsContentDetail getContentFromResult(List<InsightsKPIResultDetails> inferenceResults) {
		InsightsContentDetail insightsInferenceContentResult = null;
		if (getContentConfig().getAction() == ReportEngineEnum.ExecutionActions.COUNT
				|| getContentConfig().getAction() == ReportEngineEnum.ExecutionActions.PERCENTAGE) {
			insightsInferenceContentResult = countInferenceResult(inferenceResults);
		} else if (getContentConfig().getAction() == ReportEngineEnum.ExecutionActions.AVERAGE) {
			insightsInferenceContentResult = averageInferenceResult(inferenceResults);
		}

		return insightsInferenceContentResult;
	}

	/**
	 * Process KPI result to create content text using count and precentage method
	 * 
	 * @param inferenceResults
	 * @return
	 */
	private InsightsContentDetail countInferenceResult(List<InsightsKPIResultDetails> inferenceResults) {
		InsightsContentDetail inferenceContentResult = null;
		Map<String, Object> resultValuesMap = new HashMap<>();
		List<InsightsKPIResultDetails> listBelow = new ArrayList<>();
		List<InsightsKPIResultDetails> listAbove = new ArrayList<>();
		try {
			String result = "";
			String inferenceText = "";
			InsightsKPIResultDetails resultFirstData = inferenceResults.get(0);
			String actaulDirection = "Neutral";
			addTimeValueinResult(resultValuesMap, contentConfigDefinition.getSchedule());
			for (InsightsKPIResultDetails inferenceResultDetails : inferenceResults) {
				String comparisonField = getResultFieldFromContentDefination();
				Double currentValue = (Double) inferenceResultDetails.getResults().get(comparisonField);
				if (currentValue < getContentConfig().getThreshold()) {
					listBelow.add(inferenceResultDetails);
				} else {
					listAbove.add(inferenceResultDetails);
				}
			}

			resultValuesMap.put("belowThresholdCount", listBelow.size());
			resultValuesMap.put("aboveThresholdCount", listAbove.size());

			if (getContentConfig().getDirectionOfThreshold() == ReportEngineEnum.DirectionOfThreshold.BELOW) {
				result = String.valueOf(listBelow.size());
				actaulDirection = listBelow.size() > listAbove.size() ? "Below" : "Above";
				if (getContentConfig().getAction() == ReportEngineEnum.ExecutionActions.PERCENTAGE) {
					result = String.valueOf(listBelow.size() * 100 / inferenceResults.size());
					actaulDirection = (listBelow.size() * 100 / inferenceResults.size() > (listAbove.size() * 100)
							/ inferenceResults.size()) ? "Below" : "Above";
				}
				resultValuesMap.put("actualdirection", actaulDirection);

			} else if (getContentConfig().getDirectionOfThreshold() == ReportEngineEnum.DirectionOfThreshold.ABOVE) {
				result = String.valueOf(listAbove.size());
				actaulDirection = listAbove.size() > listBelow.size() ? "Above" : "Below";
				if (getContentConfig().getAction() == ReportEngineEnum.ExecutionActions.PERCENTAGE) {
					result = String.valueOf((listAbove.size() * 100) / inferenceResults.size());
					actaulDirection = ((listAbove.size() * 100) / inferenceResults.size() > (listBelow.size() * 100)
							/ inferenceResults.size()) ? "Above" : "Below";
				}
				resultValuesMap.put("actualdirection", actaulDirection);
			}

			ReportEngineEnum.KPISentiment sentiment = getContentConfig().getDirectionOfThreshold().name()
					.equalsIgnoreCase(actaulDirection)
					? ReportEngineEnum.KPISentiment.POSITIVE
					: ReportEngineEnum.KPISentiment.NEGATIVE;

			resultValuesMap.put("result", result);

			if (result.equalsIgnoreCase("0.0") || result.equalsIgnoreCase("0")) {
				inferenceText = getContentText(ReportEngineUtils.NEUTRAL_MESSAGE_KEY, resultValuesMap);
			} else {
				inferenceText = getContentText(ReportEngineUtils.STANDARD_MESSAGE_KEY, resultValuesMap);
			}

			if (inferenceText != null) {
				inferenceContentResult = setContentDetail(resultFirstData, resultValuesMap, sentiment, "",
						inferenceText);
			} else {
				log.debug(
						"Worlflow Detail ====   inference text is null for count and percentage threshold KPIId {} contentId {} result {} ",
						getContentConfig().getKpiId(), getContentConfig().getContentId(), resultFirstData);
			}
		} catch (Exception e) {
			log.error(e);
			log.error(" Errro while content processing for threshold KPIId {} contentId {} ",
					getContentConfig().getKpiId(), getContentConfig().getContentId());
			throw new InsightsJobFailedException("Exception while running neo4j operation {} " + e.getMessage());
		}

		return inferenceContentResult;
	}

	/**
	 * Process KPI result to create content text using average method
	 * 
	 * @param inferenceResults
	 * @return
	 */
	private InsightsContentDetail averageInferenceResult(List<InsightsKPIResultDetails> inferenceResults) {

		Map<String, Object> resultValuesMap = new HashMap<>();
		InsightsContentDetail inferenceContentResult = null;
		String comparisonField = getResultFieldFromContentDefination();
		InsightsKPIResultDetails resultDetailObj = inferenceResults.get(0);
		try {
			ReportEngineEnum.KPISentiment sentiment = ReportEngineEnum.KPISentiment.NEUTRAL;
			String actaulDirection = "Neutral";
			Double avgValue = inferenceResults.stream()
					.mapToDouble((result -> (Double) result.getResults().get(comparisonField))).average().getAsDouble();
			if (getContentConfig().getDirectionOfThreshold() == ReportEngineEnum.DirectionOfThreshold.BELOW) {
				actaulDirection = (avgValue < getContentConfig().getThreshold()) ? "Below" : "Above";

			} else {
				actaulDirection = (avgValue > getContentConfig().getThreshold()) ? "Above" : "Below";
			}

			sentiment = getContentConfig().getDirectionOfThreshold().name().equalsIgnoreCase(actaulDirection)
					? ReportEngineEnum.KPISentiment.POSITIVE
					: ReportEngineEnum.KPISentiment.NEGATIVE;
			resultValuesMap.put("actualdirection", actaulDirection);
			resultValuesMap.put("result", avgValue);

			addTimeValueinResult(resultValuesMap, contentConfigDefinition.getSchedule());

			String inferenceText = null;
			if (avgValue == 0.0) {
				inferenceText = getContentText(ReportEngineUtils.NEUTRAL_MESSAGE_KEY, resultValuesMap);
			} else {
				inferenceText = getContentText(ReportEngineUtils.STANDARD_MESSAGE_KEY, resultValuesMap);
			}

			if (inferenceText != null) {
				inferenceContentResult = setContentDetail(resultDetailObj, resultValuesMap, sentiment, "",
						inferenceText);
			} else {
				log.debug(
						"Worlflow Detail ====   inference text is null for average threshold KPIId {} contentId {} result {} ",
						getContentConfig().getKpiId(), getContentConfig().getContentId(), resultDetailObj);
			}

		} catch (Exception e) {
			log.error(e);
			log.error(" Errro while content processing for average threshold KPIId {} contentId {} ",
					getContentConfig().getKpiId(), getContentConfig().getContentId());
			throw new InsightsJobFailedException("Exception while running neo4j operation {} " + e.getMessage());
		}

		return inferenceContentResult;
	}

	/**
	 * calculate and add time value in result map for content text
	 * 
	 * @param resultValuesMap
	 * @param workflowSchedule
	 */
	private void addTimeValueinResult(Map<String, Object> resultValuesMap, WorkflowSchedule workflowSchedule) {
		int year = 0;
		int week = 0;
		int day = 0;
		year = workflowSchedule.getValue() / 365;
		week = workflowSchedule.getValue() / 7;
		day = workflowSchedule.getValue();
		resultValuesMap.put("year", year);
		resultValuesMap.put("week", week);
		resultValuesMap.put("day", day);
	}

}
