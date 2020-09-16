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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum.WorkflowSchedule;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformreports.assessment.datamodel.ContentConfigDefinition;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsContentDetail;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsKPIResultDetails;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineEnum;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineUtils;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;

public class MinMaxCategoryImpl extends BaseContentCategoryImpl {

	private static Logger log = LogManager.getLogger(MinMaxCategoryImpl.class);

	public MinMaxCategoryImpl(ContentConfigDefinition inferenceContentConfigDefinition) {
		super(inferenceContentConfigDefinition);
	}

	/**
	 * Generate MinMax content text using KPI result
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
	 * process KPI result based on MinMax category to generate content object
	 * 
	 * @param kpiResultDetailsList
	 * @return
	 */
	private InsightsContentDetail getContentFromResult(List<InsightsKPIResultDetails> inferenceResults) {
		InsightsContentDetail insightsInferenceContentResult = null;
		if (getContentConfig().getAction() == ReportEngineEnum.ExecutionActions.MIN) {
			insightsInferenceContentResult = minInferenceResult(inferenceResults);
		} else if (getContentConfig().getAction() == ReportEngineEnum.ExecutionActions.MAX) {
			insightsInferenceContentResult = maxInferenceResult(inferenceResults);
		}

		return insightsInferenceContentResult;
	}

	/**
	 * Method to get min content result from kpi result
	 * 
	 * @param inferenceResults
	 * @return
	 */
	private InsightsContentDetail minInferenceResult(List<InsightsKPIResultDetails> inferenceResults) {

		InsightsContentDetail inferenceContentResult = null;
		Map<String, Object> resultValuesMap = new HashMap<>();
		String comparisonField = getResultFieldFromContentDefination();

		try {

			InsightsKPIResultDetails minResultObject = Collections.min(inferenceResults,
					Comparator.comparing(e -> (double) e.getResults().get(comparisonField)));
			//double minValue = (double) minResultObject.getResults().get(comparisonField);
			String dateOfMinValue = InsightsUtils.insightsTimeXFormat(minResultObject.getRecordDate());
			String inferenceText = "";
			ReportEngineEnum.KPISentiment sentiment = ReportEngineEnum.KPISentiment.NEUTRAL;
			InsightsKPIResultDetails resultFirstData = inferenceResults.get(0);
			addTimeValueinResult(resultValuesMap, contentConfigDefinition.getSchedule());
			String result = getResultValueForDisplay(minResultObject.getResults().get(comparisonField));
			resultValuesMap.put("result", result);
			resultValuesMap.put("minDate", dateOfMinValue);

			if (result.equalsIgnoreCase("0.0") || result.equalsIgnoreCase("0")) {
				inferenceText = getContentText(ReportEngineUtils.NEUTRAL_MESSAGE_KEY, resultValuesMap);
			} else {
				inferenceText = getContentText(ReportEngineUtils.STANDARD_MESSAGE_KEY, resultValuesMap);
			}

			if (inferenceText != null) {
				inferenceContentResult = setContentDetail(resultFirstData, resultValuesMap, sentiment, "",
						inferenceText);

			} else {
				log.debug(" inference text is null in category Min KPIId {} contentId {} result {} ",
						getContentConfig().getKpiId(), getContentConfig().getContentId(), resultFirstData);
			}
		} catch (Exception e) {
			log.error(e);
			log.error(" Errro while content processing for Min-Max KPIId {} contentId {} ",
					getContentConfig().getKpiId(), getContentConfig().getContentId());
			throw new InsightsJobFailedException("Errro while content processing for Min-Max KPIId {} contentId {} " + e.getMessage());
		}
		

		return inferenceContentResult;
	}

	/**
	 * Method to get max Content result based on KPI result
	 * 
	 * @param inferenceResults
	 * @return
	 */
	private InsightsContentDetail maxInferenceResult(List<InsightsKPIResultDetails> inferenceResults) {

		InsightsContentDetail inferenceContentResult = null;
		Map<String, Object> resultValuesMap = new HashMap<>();
		String comparisonField = getResultFieldFromContentDefination();

		try {

			InsightsKPIResultDetails maxResultObject = Collections.max(inferenceResults,
					Comparator.comparing(e -> (double) e.getResults().get(comparisonField)));
			//double maxValue = (double) maxResultObject.getResults().get(comparisonField);

			String dateOfMaxValue = InsightsUtils.insightsTimeXFormat(maxResultObject.getRecordDate());
			String inferenceText = "";
			ReportEngineEnum.KPISentiment sentiment = ReportEngineEnum.KPISentiment.NEUTRAL;
			InsightsKPIResultDetails resultFirstData = inferenceResults.get(0);
			addTimeValueinResult(resultValuesMap, contentConfigDefinition.getSchedule());
			String result = getResultValueForDisplay(maxResultObject.getResults().get(comparisonField));
			resultValuesMap.put("result", result);
			resultValuesMap.put("maxDate", dateOfMaxValue);

			if (result.equalsIgnoreCase("0.0") || result.equalsIgnoreCase("0")) {
				inferenceText = getContentText(ReportEngineUtils.NEUTRAL_MESSAGE_KEY, resultValuesMap);
			} else {
				inferenceText = getContentText(ReportEngineUtils.STANDARD_MESSAGE_KEY, resultValuesMap);
			}

			if (inferenceText != null) {
				inferenceContentResult = setContentDetail(resultFirstData, resultValuesMap, sentiment, "",
						inferenceText);

			} else {
				log.debug(" inference text is null in category Max KPIId {} contentId {} result {} ",
						getContentConfig().getKpiId(), getContentConfig().getContentId(), resultFirstData);
			}
		} catch (Exception e) {
			log.error(e);
			log.error(" Errro while content processing for category Min-Max KPIId {} contentId {} ",
					getContentConfig().getKpiId(), getContentConfig().getContentId());
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
