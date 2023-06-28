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
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.constants.StringExpressionConstants;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum.WorkflowSchedule;
import com.cognizant.devops.platformcommons.exception.InsightsJobFailedException;
import com.cognizant.devops.platformreports.assessment.datamodel.ContentConfigDefinition;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsContentDetail;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsKPIResultDetails;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineEnum;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineEnum.KPISentiment;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineUtils;

public class ThresholdContentCategoryImpl extends BaseContentCategoryImpl {

	private static Logger log = LogManager.getLogger(ThresholdContentCategoryImpl.class);
	
	public static final String NEUTRAL = "Neutral";

	public ThresholdContentCategoryImpl(ContentConfigDefinition inferenceContentConfigDefinition) {
		super(inferenceContentConfigDefinition);
	}

	/**
	 * Generate Threshold content text using KPI result
	 */
	@Override
	public void generateContent() {
		long startTime = System.nanoTime();
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
			long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
			log.debug(StringExpressionConstants.STR_EXP_TASK,
					contentConfigDefinition.getExecutionId(),contentConfigDefinition.getWorkflowId(),contentConfigDefinition.getReportId(),"-",
					contentConfigDefinition.getKpiId(),contentConfigDefinition.getCategory(),processingTime,
					ConfigOptions.CONTENT_ID + contentConfigDefinition.getContentId() + ConfigOptions.CONTENT_NAME +contentConfigDefinition.getContentName() +
					ConfigOptions.ACTION + contentConfigDefinition.getAction() 
					+ contentResult + contentConfigDefinition.getNoOfResult());
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
		long startTime = System.nanoTime();
		InsightsContentDetail inferenceContentResult = null;
		Map<String, Object> resultValuesMap = new HashMap<>();
		List<InsightsKPIResultDetails> listBelow = new ArrayList<>();
		List<InsightsKPIResultDetails> listAbove = new ArrayList<>();
		try {
			String result = "";
			String inferenceText = "";
			InsightsKPIResultDetails resultFirstData = inferenceResults.get(0);
			String actaulDirection = NEUTRAL;
			addTimeValueinResult(resultValuesMap, contentConfigDefinition.getSchedule());
			
			 prepareListAboveAndBelowValue(inferenceResults,listBelow,listAbove);

			resultValuesMap.put("belowThresholdCount", listBelow.size());
			resultValuesMap.put("aboveThresholdCount", listAbove.size());

			if (getContentConfig().getDirectionOfThreshold() == ReportEngineEnum.DirectionOfThreshold.BELOW) {
				result = String.valueOf(listBelow.size());
				actaulDirection = getActualDirectionBelow(listAbove,listBelow); 
				if (getContentConfig().getAction() == ReportEngineEnum.ExecutionActions.PERCENTAGE) {
					result = String.valueOf(listBelow.size() * 100 / inferenceResults.size());
					actaulDirection = (listBelow.size() * 100 / inferenceResults.size() > (listAbove.size() * 100)
							/ inferenceResults.size()) ? AssessmentReportAndWorkflowConstants.BELOW
									: AssessmentReportAndWorkflowConstants.ABOVE;
				}
				resultValuesMap.put(AssessmentReportAndWorkflowConstants.ACTUALDIRECTION, actaulDirection);

			} else if (getContentConfig().getDirectionOfThreshold() == ReportEngineEnum.DirectionOfThreshold.ABOVE) {
				result = String.valueOf(listAbove.size());
				actaulDirection = getActualDirectionAbove(listAbove,listBelow); 
				if (getContentConfig().getAction() == ReportEngineEnum.ExecutionActions.PERCENTAGE) {
					result = String.valueOf((listAbove.size() * 100) / inferenceResults.size());
					actaulDirection = ((listAbove.size() * 100) / inferenceResults.size() > (listBelow.size() * 100)
							/ inferenceResults.size()) ? AssessmentReportAndWorkflowConstants.ABOVE
									: AssessmentReportAndWorkflowConstants.BELOW;
				}
				resultValuesMap.put(AssessmentReportAndWorkflowConstants.ACTUALDIRECTION, actaulDirection);
			}

			ReportEngineEnum.KPISentiment sentiment = getContentConfig().getDirectionOfThreshold().name()
					.equalsIgnoreCase(actaulDirection) ? ReportEngineEnum.KPISentiment.POSITIVE
							: ReportEngineEnum.KPISentiment.NEGATIVE;

			resultValuesMap.put("result", result);
			
			inferenceContentResult = processInferenceText(result,resultValuesMap,sentiment,resultFirstData);
       
			long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
			log.debug(StringExpressionConstants.STR_EXP_TASK,
					contentConfigDefinition.getExecutionId(),contentConfigDefinition.getWorkflowId(),contentConfigDefinition.getReportId(),"-",
					contentConfigDefinition.getKpiId(),contentConfigDefinition.getCategory(),processingTime,
					ConfigOptions.CONTENT_ID + contentConfigDefinition.getContentId() + ConfigOptions.CONTENT_NAME +contentConfigDefinition.getContentName() +
					ConfigOptions.ACTION + contentConfigDefinition.getAction() 
					+ ConfigOptions.CONTENT_RESULT + contentConfigDefinition.getNoOfResult());
		} catch (Exception e) {
			log.error(e);
			log.error(" Errro while content processing for threshold KPIId {} contentId {} ",
					getContentConfig().getKpiId(), getContentConfig().getContentId());
			log.error(StringExpressionConstants.STR_EXP_TASK,
					contentConfigDefinition.getExecutionId(),contentConfigDefinition.getWorkflowId(),contentConfigDefinition.getReportId(),"-",
					contentConfigDefinition.getKpiId(),contentConfigDefinition.getCategory(),0,
					ConfigOptions.CONTENT_ID + contentConfigDefinition.getContentId() + ConfigOptions.CONTENT_NAME +contentConfigDefinition.getContentName() +
					ConfigOptions.ACTION + contentConfigDefinition.getAction() 
					+ ConfigOptions.CONTENT_RESULT + contentConfigDefinition.getNoOfResult() + "Exception while running neo4j operation" + e.getMessage());
			throw new InsightsJobFailedException("Exception while running neo4j operation {} " + e.getMessage());
		}

		return inferenceContentResult;
	}
	

	private void prepareListAboveAndBelowValue(List<InsightsKPIResultDetails> inferenceResults,
			List<InsightsKPIResultDetails> listBelow, List<InsightsKPIResultDetails> listAbove) {
		
		for (InsightsKPIResultDetails inferenceResultDetails : inferenceResults) {
			String comparisonField = getResultFieldFromContentDefination();
			Double currentValue = (Double) inferenceResultDetails.getResults().get(comparisonField);
			if (currentValue < getContentConfig().getThreshold()) {
				listBelow.add(inferenceResultDetails);
			} else {
				listAbove.add(inferenceResultDetails);
			}
		}

	}

	private String getActualDirectionBelow(List<InsightsKPIResultDetails> listAbove, List<InsightsKPIResultDetails> listBelow) {
		String actaulDirection = NEUTRAL;
		actaulDirection = listBelow.size() > listAbove.size() ? AssessmentReportAndWorkflowConstants.BELOW
				: AssessmentReportAndWorkflowConstants.ABOVE;
		
		return actaulDirection;
	}
	
	
	private String getActualDirectionAbove(List<InsightsKPIResultDetails> listAbove, List<InsightsKPIResultDetails> listBelow) {
		String actaulDirection = NEUTRAL;
		
		actaulDirection = listAbove.size() > listBelow.size() ? AssessmentReportAndWorkflowConstants.ABOVE
				: AssessmentReportAndWorkflowConstants.BELOW;
		
		return actaulDirection;
		
	}
	
	
	private InsightsContentDetail processInferenceText(String result, Map<String, Object> resultValuesMap, KPISentiment sentiment, InsightsKPIResultDetails resultFirstData) {
		
		String inferenceText = "";
		InsightsContentDetail processInferenceContentResult = null;
		
		if (result.equalsIgnoreCase("0.0") || result.equalsIgnoreCase("0")) {
			inferenceText = getContentText(ReportEngineUtils.NEUTRAL_MESSAGE_KEY, resultValuesMap);
		} else {
			inferenceText = getContentText(ReportEngineUtils.STANDARD_MESSAGE_KEY, resultValuesMap);
		}

		if (inferenceText != null) {
			processInferenceContentResult = setContentDetail(resultFirstData, resultValuesMap, sentiment, "",
					inferenceText);
		} else {
			log.debug(
					"Worlflow Detail ====   inference text is null for count and percentage threshold KPIId {} contentId {} result {} ",
					getContentConfig().getKpiId(), getContentConfig().getContentId(), resultFirstData);
		}
		return processInferenceContentResult;
	}

	/**
	 * Process KPI result to create content text using average method
	 * 
	 * @param inferenceResults
	 * @return
	 */
	private InsightsContentDetail averageInferenceResult(List<InsightsKPIResultDetails> inferenceResults) {
		long startTime = System.nanoTime();
		Map<String, Object> resultValuesMap = new HashMap<>();
		InsightsContentDetail inferenceContentResult = null;
		String comparisonField = getResultFieldFromContentDefination();
		InsightsKPIResultDetails resultDetailObj = inferenceResults.get(0);
		try {
			ReportEngineEnum.KPISentiment sentiment = ReportEngineEnum.KPISentiment.NEUTRAL;
			String actaulDirection = NEUTRAL;
			Double avgValue = inferenceResults.stream()
					.mapToDouble((result -> (Double) result.getResults().get(comparisonField))).average().getAsDouble();
			if (getContentConfig().getDirectionOfThreshold() == ReportEngineEnum.DirectionOfThreshold.BELOW) {
				actaulDirection = (avgValue < getContentConfig().getThreshold())
						? AssessmentReportAndWorkflowConstants.BELOW
						: AssessmentReportAndWorkflowConstants.ABOVE;

			} else {
				actaulDirection = (avgValue > getContentConfig().getThreshold())
						? AssessmentReportAndWorkflowConstants.ABOVE
						: AssessmentReportAndWorkflowConstants.BELOW;
			}

			sentiment = getContentConfig().getDirectionOfThreshold().name().equalsIgnoreCase(actaulDirection)
					? ReportEngineEnum.KPISentiment.POSITIVE
					: ReportEngineEnum.KPISentiment.NEGATIVE;
			resultValuesMap.put(AssessmentReportAndWorkflowConstants.ACTUALDIRECTION, actaulDirection);
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

			long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
			log.debug(StringExpressionConstants.STR_EXP_TASK,
					contentConfigDefinition.getExecutionId(),contentConfigDefinition.getWorkflowId(),contentConfigDefinition.getReportId(),"-",
					contentConfigDefinition.getKpiId(),contentConfigDefinition.getCategory(),processingTime,
					ConfigOptions.CONTENT_ID + contentConfigDefinition.getContentId() + ConfigOptions.CONTENT_NAME  +contentConfigDefinition.getContentName() +
					ConfigOptions.ACTION + contentConfigDefinition.getAction() 
					+ ConfigOptions.CONTENT_RESULT + contentConfigDefinition.getNoOfResult());
			
		} catch (Exception e) {
			log.error(e);
			log.error(" Error while content processing for average threshold KPIId {} contentId {} ",
					getContentConfig().getKpiId(), getContentConfig().getContentId());
			log.error(StringExpressionConstants.STR_EXP_TASK,
					contentConfigDefinition.getExecutionId(),contentConfigDefinition.getWorkflowId(),contentConfigDefinition.getReportId(),"-",
					contentConfigDefinition.getKpiId(),contentConfigDefinition.getCategory(),0,
					ConfigOptions.CONTENT_ID + contentConfigDefinition.getContentId() + ConfigOptions.CONTENT_NAME +contentConfigDefinition.getContentName() +
					ConfigOptions.ACTION + contentConfigDefinition.getAction() 
					+ ConfigOptions.CONTENT_RESULT + contentConfigDefinition.getNoOfResult() + "Exception while running neo4j operation" + e.getMessage());
			
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
