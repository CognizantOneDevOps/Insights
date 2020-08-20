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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformreports.assessment.datamodel.ContentConfigDefinition;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsContentDetail;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsKPIResultDetails;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineEnum;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineUtils;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;

public class ComparisonContentCategoryImpl extends BaseContentCategoryImpl {
	private static Logger log = LogManager.getLogger(ComparisonContentCategoryImpl.class);

	public ComparisonContentCategoryImpl(ContentConfigDefinition inferenceContentConfigDefinition) {
		super(inferenceContentConfigDefinition);
	}

	/**
	 * Generate comparision content text using KPI result
	 */
	@Override
	public void generateContent() {
		List<InsightsKPIResultDetails> kpiResults = getKPIExecutionResult();

		if (!kpiResults.isEmpty()) {
			InsightsContentDetail contentResult = getContentFromResult(kpiResults);
			log.debug(" contentResultList  + {} ", contentResult);
			if (contentResult != null) {
				saveContentResult(contentResult);
			}
		} else {
			log.debug(" No kpi result found ");
		}
	}

	/**
	 * process KPI result based on comparison category to generate content object
	 * 
	 * @param kpiResultDetailsList
	 * @return
	 */
	public InsightsContentDetail getContentFromResult(
			List<InsightsKPIResultDetails> kpiResultDetailsList) {
		InsightsContentDetail inferenceContentResult = null;

		try {

			InsightsKPIResultDetails resultFirstData = kpiResultDetailsList.get(0);
			String actualTrend = ReportEngineEnum.KPITrends.NOCHANGE.getValue();
			ReportEngineEnum.KPISentiment sentiment = ReportEngineEnum.KPISentiment.NEUTRAL;
			Map<String, Object> resultValuesMap = resultFirstData.getResults();

			String contentText = "";

			// This condition will be executed if KPI comparison is FALSE
			if (getContentConfig().getCategory() == ReportEngineEnum.ContentCategory.COMPARISON) {
				if (kpiResultDetailsList.size() < 2) {
					return inferenceContentResult;
				}
				String comparisonField = getResultFieldFromContentDefination();
				InsightsKPIResultDetails previousDateData = kpiResultDetailsList.get(1);
				Object currentValue = resultFirstData.getResults().get(comparisonField);
				Object previousValue = previousDateData.getResults().get(comparisonField);

				sentiment = getSentiment(previousValue, currentValue, getContentConfig().getExpectedTrend());
				actualTrend = String.valueOf(getActualTrend(getContentConfig().getExpectedTrend(), sentiment));

				resultValuesMap.put("current:" + comparisonField, currentValue);
				resultValuesMap.put("previous:" + comparisonField, previousValue);

				contentText = getContentText(sentiment.getValue(), resultValuesMap);

			} else if (getContentConfig().getCategory() == ReportEngineEnum.ContentCategory.STANDARD) {
				contentText = getContentText(ReportEngineUtils.STANDARD_MESSAGE_KEY, resultValuesMap);

			}
			log.debug("  contentText  {} ", contentText);

			if (contentText != null) {
				inferenceContentResult = setContentDetail(resultFirstData, resultValuesMap, sentiment, actualTrend,
						contentText);

			} else {
				log.debug(" content text is null in comparison KPI KPIId {} contentId {} result {} ",
						contentConfigDefinition.getKpiId(), contentConfigDefinition.getContentId(), resultFirstData);
			}
		} catch (Exception e) {
			log.error(e);
			log.error(" Error while content processing comparison  KPIId {} contentId {} ",
					contentConfigDefinition.getKpiId(), contentConfigDefinition.getContentId());
			throw new InsightsJobFailedException("Error while content processing comparison  KPIId {} contentId {} " + e.getMessage());
		}

		return inferenceContentResult;

	}

	

	/**
	 * Compare previous and current value to get sentiment
	 * 
	 * @param previousValue
	 * @param currentValue
	 * @param expectedTrend
	 * @return
	 */
	public ReportEngineEnum.KPISentiment getSentiment(Object previousValue, Object currentValue, String expectedTrend) {
		Double previousVal = null;
		Double currentVal = null;
		if (previousValue instanceof String) {
			previousVal = Double.parseDouble(String.valueOf(previousValue));
			currentVal = Double.parseDouble(String.valueOf(currentValue));
		} else if (previousValue instanceof Double) {
			previousVal = (Double) previousValue;
			currentVal = (Double) currentValue;
		}
		if (expectedTrend.equalsIgnoreCase(ReportEngineEnum.KPITrends.DOWNWARDS.getValue())) {
			if (previousVal > currentVal) {
				return ReportEngineEnum.KPISentiment.POSITIVE;
			} else if (previousVal < currentVal) {
				return ReportEngineEnum.KPISentiment.NEGATIVE;
			} else {
				return ReportEngineEnum.KPISentiment.NEUTRAL;
			}
		} else if (expectedTrend.equalsIgnoreCase(ReportEngineEnum.KPITrends.UPWARDS.getValue())) {
			if (previousVal < currentVal) {
				return ReportEngineEnum.KPISentiment.POSITIVE;
			} else if (previousVal > currentVal) {
				return ReportEngineEnum.KPISentiment.NEGATIVE;
			} else {
				return ReportEngineEnum.KPISentiment.NEUTRAL;
			}
		} else {
			return ReportEngineEnum.KPISentiment.NEUTRAL;
		}
	}

	/**
	 * Generate Actual trend based on sentiment and excepted trend
	 * 
	 * @param expectedTrend
	 * @param sentiment
	 * @return
	 */
	private ReportEngineEnum.KPITrends getActualTrend(String expectedTrend, ReportEngineEnum.KPISentiment sentiment) {
		ReportEngineEnum.KPITrends returnTrend = ReportEngineEnum.KPITrends.NOCHANGE;
		if (expectedTrend.equalsIgnoreCase(ReportEngineEnum.KPITrends.DOWNWARDS.getValue())) {
			if (sentiment == ReportEngineEnum.KPISentiment.POSITIVE) {
				returnTrend = ReportEngineEnum.KPITrends.DOWNWARDS;
			} else if (sentiment == ReportEngineEnum.KPISentiment.NEGATIVE) {
				returnTrend = ReportEngineEnum.KPITrends.UPWARDS;
			} else if (sentiment.equals(ReportEngineEnum.KPISentiment.NEUTRAL)) {
				returnTrend = ReportEngineEnum.KPITrends.NOCHANGE;
			}
		} else if (expectedTrend.equalsIgnoreCase(ReportEngineEnum.KPITrends.UPWARDS.getValue())) {
			if (sentiment == ReportEngineEnum.KPISentiment.POSITIVE) {
				returnTrend = ReportEngineEnum.KPITrends.UPWARDS;
			} else if (sentiment == ReportEngineEnum.KPISentiment.NEGATIVE) {
				returnTrend = ReportEngineEnum.KPITrends.DOWNWARDS;
			} else if (sentiment.equals(ReportEngineEnum.KPISentiment.NEUTRAL)) {
				returnTrend = ReportEngineEnum.KPITrends.NOCHANGE;
			}
		} else {
			returnTrend = ReportEngineEnum.KPITrends.NOCHANGE;
		}
		return returnTrend;
	}
}
