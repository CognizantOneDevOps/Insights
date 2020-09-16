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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum.WorkflowSchedule;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformreports.assessment.datamodel.ContentConfigDefinition;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsContentDetail;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsKPIResultDetails;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineEnum;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineUtils;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;

public class TrendCategoryImpl extends BaseContentCategoryImpl {
	private static Logger log = LogManager.getLogger(TrendCategoryImpl.class);

	public TrendCategoryImpl(ContentConfigDefinition inferenceContentConfigDefinition) {
		super(inferenceContentConfigDefinition);
	}

	/**
	 * Generate Trend content text using KPI result
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
	 * process KPI result based on Trend category to generate content object
	 * 
	 * @param kpiResultDetailsList
	 * @return
	 */
	private InsightsContentDetail getContentFromResult(List<InsightsKPIResultDetails> inferenceResults) {
		InsightsContentDetail insightsInferenceContentResult = null;
		if (getContentConfig().getAction() == ReportEngineEnum.ExecutionActions.COUNT) { //|| getContentConfig().getAction() == ReportEngineEnum.ExecutionActions.PERCENTAGE
			insightsInferenceContentResult = countTrendResult(inferenceResults);
		} else if (getContentConfig().getAction() == ReportEngineEnum.ExecutionActions.AVERAGE) {
			insightsInferenceContentResult = averageTrendResult(inferenceResults);
		}

		return insightsInferenceContentResult;
	}

	/**
	 * Process KPI result to create content text using count and precentage method
	 * 
	 * @param inferenceResults
	 * @return
	 */
	private InsightsContentDetail countTrendResult(List<InsightsKPIResultDetails> inferenceResults) {
		InsightsContentDetail inferenceContentResult = null;
		Map<String, Object> resultValuesMap = new HashMap<>();
		double resultValue = 0.0;
		try {

			ReportEngineEnum.KPISentiment sentiment = ReportEngineEnum.KPISentiment.NEUTRAL;
			InsightsKPIResultDetails resultFirstData = inferenceResults.get(0);
			addTimeValueinResult(resultValuesMap, contentConfigDefinition.getSchedule());
			for (InsightsKPIResultDetails inferenceResultDetails : inferenceResults) {
				String comparisonField = getResultFieldFromContentDefination();
				resultValue = resultValue + ((double) inferenceResultDetails.getResults().get(comparisonField));
			}

			resultValuesMap.put("result", resultValue);

			inferenceContentResult = prepareContentMessageAndResult(inferenceContentResult, resultValuesMap,
					resultValue, sentiment, resultFirstData);
		} catch (Exception e) {
			log.error(e);
			log.error(" Errro while content processing for trend KPIId {} contentId {} ",
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
	private InsightsContentDetail averageTrendResult(List<InsightsKPIResultDetails> inferenceResults) {

		Map<String, Object> resultValuesMap = new HashMap<>();
		InsightsContentDetail inferenceContentResult = null;
		try {
			String comparisonField = getResultFieldFromContentDefination();
			InsightsKPIResultDetails resultDetailObj = inferenceResults.get(0);

			ReportEngineEnum.KPISentiment sentiment = ReportEngineEnum.KPISentiment.NEUTRAL;
			double resultValue = inferenceResults.stream()
					.mapToDouble((result -> (Double) result.getResults().get(comparisonField))).average().getAsDouble();

			resultValuesMap.put("result", resultValue);

			addTimeValueinResult(resultValuesMap, contentConfigDefinition.getSchedule());

			inferenceContentResult = prepareContentMessageAndResult(inferenceContentResult, resultValuesMap,
					resultValue, sentiment, resultDetailObj);

		} catch (Exception e) {
			log.error(e);
			log.error(" Errro while content processing for average trend KPIId {} contentId {} ",
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

	/**
	 * Prepare content message and result
	 * 
	 * @param inferenceContentResult
	 * @param resultValuesMap
	 * @param resultValue
	 * @param sentiment
	 * @param resultFirstData
	 * @return
	 * @throws InsightsCustomException
	 */
	private InsightsContentDetail prepareContentMessageAndResult(InsightsContentDetail inferenceContentResult,
			Map<String, Object> resultValuesMap, double resultValue, ReportEngineEnum.KPISentiment sentiment,
			InsightsKPIResultDetails resultFirstData) throws InsightsCustomException {
		String inferenceText = "";
		if (resultValue == 0.0) {
			inferenceText = getContentText(ReportEngineUtils.NEUTRAL_MESSAGE_KEY, resultValuesMap);
		} else {
			inferenceText = getContentText(ReportEngineUtils.STANDARD_MESSAGE_KEY, resultValuesMap);
		}

		if (inferenceText != null) {
			inferenceContentResult = setContentDetail(resultFirstData, resultValuesMap, sentiment, "", inferenceText);
		} else {
			log.debug(" inference text is null in Trend KPIId {} contentId {} result {} ",
					getContentConfig().getKpiId(), getContentConfig().getContentId(), resultFirstData);
		}
		return inferenceContentResult;
	}

}
