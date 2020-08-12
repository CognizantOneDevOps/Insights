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

		if (!inferenceResults.isEmpty()) {
			InsightsContentDetail contentResult = getContentFromResult(inferenceResults);
			log.debug(" contentResultList  + {} ", contentResult);
			if (contentResult != null) {
				saveContentResult(contentResult);
			}

		} else {
			log.debug(" No inference result found ");
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
		String comparisonField = inferenceResults.get(0).getResultField();

		try {

			InsightsKPIResultDetails minResultObject = Collections.min(inferenceResults,
					Comparator.comparing(e -> (double) e.getResults().get(comparisonField)));
			double minValue = (double) minResultObject.getResults().get(comparisonField);
			String dateOfMinValue = minResultObject.getResultTimeX();
			String inferenceText = "";
			ReportEngineEnum.KPISentiment sentiment = ReportEngineEnum.KPISentiment.NEUTRAL;
			InsightsKPIResultDetails resultFirstData = inferenceResults.get(0);
			addTimeValueinResult(resultValuesMap, contentConfigDefinition.getSchedule());
			resultValuesMap.put("result", minValue);
			resultValuesMap.put("minDate", dateOfMinValue);

			inferenceText = getContentText(ReportEngineUtils.STANDARD_MESSAGE_KEY, resultValuesMap);

			if (inferenceText != null) {
				inferenceContentResult = setContentDetail(resultFirstData, resultValuesMap, sentiment, "",
						inferenceText);

			} else {
				log.debug(" inference text is null in category Min-Max KPIId {} contentId {} result {} ",
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
		String comparisonField = inferenceResults.get(0).getResultField();

		try {

			InsightsKPIResultDetails maxResultObject = Collections.max(inferenceResults,
					Comparator.comparing(e -> (double) e.getResults().get(comparisonField)));
			double maxValue = (double) maxResultObject.getResults().get(comparisonField);

			String dateOfMaxValue = maxResultObject.getResultTimeX();
			String inferenceText = "";
			ReportEngineEnum.KPISentiment sentiment = ReportEngineEnum.KPISentiment.NEUTRAL;
			InsightsKPIResultDetails resultFirstData = inferenceResults.get(0);
			addTimeValueinResult(resultValuesMap, contentConfigDefinition.getSchedule());
			resultValuesMap.put("result", maxValue);
			resultValuesMap.put("maxDate", dateOfMaxValue);

			inferenceText = getContentText(ReportEngineUtils.STANDARD_MESSAGE_KEY, resultValuesMap);

			if (inferenceText != null) {
				inferenceContentResult = setContentDetail(resultFirstData, resultValuesMap, sentiment, "",
						inferenceText);

			} else {
				log.debug(" inference text is null in category Min-Max KPIId {} contentId {} result {} ",
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
