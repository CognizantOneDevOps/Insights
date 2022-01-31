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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum.WorkflowSchedule;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformreports.assessment.datamodel.ContentConfigDefinition;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsContentDetail;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsKPIResultDetails;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineEnum;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineUtils;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;
import com.google.gson.JsonObject;

public class ThresholdRangeCategoryImpl extends BaseContentCategoryImpl {
	private static Logger log = LogManager.getLogger(ThresholdRangeCategoryImpl.class);
	public ThresholdRangeCategoryImpl(ContentConfigDefinition inferenceContentConfigDefinition) {
		super(inferenceContentConfigDefinition);
	}

	/**
	 * Generate Threshold Range content text using KPI result
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
			log.debug("Type=TaskExecution  executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}",
					contentConfigDefinition.getExecutionId(),contentConfigDefinition.getWorkflowId(),contentConfigDefinition.getReportId(),"-",
					contentConfigDefinition.getKpiId(),contentConfigDefinition.getCategory(),processingTime,
					"ContentId :" + contentConfigDefinition.getContentId() + "ContentName :" +contentConfigDefinition.getContentName() +
					"action :" + contentConfigDefinition.getAction() 
					+ "ContentResult :" + contentConfigDefinition.getNoOfResult());
		}
	}

	/**
	 * process KPI result based on Threshold Range category to generate content
	 * object
	 * 
	 * @param kpiResultDetailsList
	 * @return
	 */
	private InsightsContentDetail getContentFromResult(List<InsightsKPIResultDetails> inferenceResults) {
		InsightsContentDetail insightsInferenceContentResult = null;
		if (getContentConfig().getAction() == ReportEngineEnum.ExecutionActions.COUNT
				|| getContentConfig().getAction() == ReportEngineEnum.ExecutionActions.PERCENTAGE) {
			insightsInferenceContentResult = countAndPercentageInferenceResult(inferenceResults);
		}
		return insightsInferenceContentResult;
	}

	/**
	 * Process KPI result to create content text using count and precentage method
	 * 
	 * @param inferenceResults
	 * @return
	 */
	private InsightsContentDetail countAndPercentageInferenceResult(
			List<InsightsKPIResultDetails> inferenceResults) {

		InsightsContentDetail inferenceContentResult = null;
		if (contentConfigDefinition.getThresholds() != null) {
			try {
				long startTime = System.nanoTime();
				InsightsKPIResultDetails resultDetailObj = inferenceResults.get(0);
				Map<String, Object> zoneWiseCountWithSentiment = getZoneWiseCountWithSentiment(inferenceResults);
				Map<String, Object> resultValuesMap = new HashMap<>();
		
			    int redZone = (int) zoneWiseCountWithSentiment.get("redZone");
				int amberZone = (int) zoneWiseCountWithSentiment.get("amberZone");
			    int greenZone = (int) zoneWiseCountWithSentiment.get("greenZone");
				ReportEngineEnum.KPISentiment sentiment = (ReportEngineEnum.KPISentiment) zoneWiseCountWithSentiment
						.get("sentiment");
				String zone=(String)zoneWiseCountWithSentiment.get("zone");
				int result = (int)zoneWiseCountWithSentiment.get(AssessmentReportAndWorkflowConstants.RESULT);
				if (getContentConfig().getAction() == ReportEngineEnum.ExecutionActions.PERCENTAGE) {
					int totalSize = inferenceResults.size();
					resultValuesMap.put("red", ((redZone * 100) / totalSize)+"%");
					resultValuesMap.put(AssessmentReportAndWorkflowConstants.AMBER,((amberZone * 100) / totalSize)+"%");
					resultValuesMap.put(AssessmentReportAndWorkflowConstants.GREEN, ((greenZone * 100) / totalSize)+"%");
					resultValuesMap.put("zone",zone);
					resultValuesMap.put(AssessmentReportAndWorkflowConstants.RESULT,result);
					addTimeValueinResult(resultValuesMap, contentConfigDefinition.getSchedule());
				} else {
					resultValuesMap.put("red", redZone);
					resultValuesMap.put(AssessmentReportAndWorkflowConstants.AMBER, amberZone);
					resultValuesMap.put(AssessmentReportAndWorkflowConstants.GREEN, greenZone);
					resultValuesMap.put("zone",zone);
					resultValuesMap.put(AssessmentReportAndWorkflowConstants.RESULT,result);
					addTimeValueinResult(resultValuesMap, contentConfigDefinition.getSchedule());
				}
				String inferenceText = getContentText(ReportEngineUtils.STANDARD_MESSAGE_KEY, resultValuesMap);
				if (inferenceText != null) {
					inferenceContentResult = setContentDetail(resultDetailObj, resultValuesMap, sentiment, "",
							inferenceText);
				} else {
					log.debug(
							" inference text is null for count And Percentage threshold-range KPIId {} contentId {} result {} ",
							getContentConfig().getKpiId(), getContentConfig().getContentId(), resultDetailObj);
				}
				long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
				log.debug("Type=TaskExecution  executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}",
						contentConfigDefinition.getExecutionId(),contentConfigDefinition.getWorkflowId(),contentConfigDefinition.getReportId(),"-",
						contentConfigDefinition.getKpiId(),contentConfigDefinition.getCategory(),processingTime,
						"ContentId :" + contentConfigDefinition.getContentId() + "ContentName :" +contentConfigDefinition.getContentName() +
						"action :" + contentConfigDefinition.getAction() 
						+ "ContentResult :" + contentConfigDefinition.getNoOfResult());
			} catch (Exception e) {
				log.error(e);
				log.error(
						" Error while content processing for threshold-range countAndPercentageInferenceResult KPIId {} contentId {}",
						getContentConfig().getKpiId(), getContentConfig().getContentId());
				log.error("Type=TaskExecution  executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}",
						contentConfigDefinition.getExecutionId(),contentConfigDefinition.getWorkflowId(),contentConfigDefinition.getReportId(),"-",
						contentConfigDefinition.getKpiId(),contentConfigDefinition.getCategory(),0,
						"ContentId :" + contentConfigDefinition.getContentId() + "ContentName :" +contentConfigDefinition.getContentName() +
						"action :" + contentConfigDefinition.getAction() 
						+ "ContentResult :" + contentConfigDefinition.getNoOfResult() + " Error while content processing for threshold-range"  + e.getMessage());
				throw new InsightsJobFailedException(" Error while content processing for threshold-range KPIId {} contentId {}");
			}

		} else {
			log.error(" Errro while content processing for threshold-range KPIId {} contentId {}",
					getContentConfig().getKpiId(), getContentConfig().getContentId());
			log.error("Type=TaskExecution  executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}",
					contentConfigDefinition.getExecutionId(),contentConfigDefinition.getWorkflowId(),contentConfigDefinition.getReportId(),"-",
					contentConfigDefinition.getKpiId(),contentConfigDefinition.getCategory(),0,
					"ContentId :" + contentConfigDefinition.getContentId() + "ContentName :" +contentConfigDefinition.getContentName() +
					"action :" + contentConfigDefinition.getAction() 
					+ "ContentResult :" + contentConfigDefinition.getNoOfResult() + " Error while content processing for threshold-range");
		}

		return inferenceContentResult;
	}

	/**
	 * Process KPI result and divide it based on Zone or Range
	 * 
	 * @param inferenceResults
	 * @return
	 */
	private Map<String, Object> getZoneWiseCountWithSentiment(List<InsightsKPIResultDetails> inferenceResults) {
		int redZone = 0;
		int amberZone = 0;
		int greenZone = 0;
		String zone=AssessmentReportAndWorkflowConstants.GREEN;
		ReportEngineEnum.KPISentiment sentiment = ReportEngineEnum.KPISentiment.NEUTRAL;
		HashMap<String,Integer> zonesMap = new HashMap<>();
		String comparisonField = getResultFieldFromContentDefination();
		JsonObject thresholdObjs = JsonUtils.parseStringAsJsonObject(String.valueOf(contentConfigDefinition.getThresholds()));
		double amberThreshold = thresholdObjs.get(AssessmentReportAndWorkflowConstants.AMBER).getAsDouble();
		double greenThreshold = thresholdObjs.get(AssessmentReportAndWorkflowConstants.GREEN).getAsDouble();

		if (contentConfigDefinition
				.getDirectionOfThreshold() == ReportEngineEnum.DirectionOfThreshold.BELOW) {
			for (InsightsKPIResultDetails resultObj : inferenceResults) {
				double val = (double) resultObj.getResults().get(comparisonField);
				if (val <= greenThreshold) {
					greenZone++;
				} else if (val <= amberThreshold) {
					amberZone++;
				} else {
					redZone++;
				}
			}			
			zonesMap.put(AssessmentReportAndWorkflowConstants.GREEN, greenZone);
			zonesMap.put(AssessmentReportAndWorkflowConstants.AMBER,amberZone);
			zonesMap.put("red",redZone);			
			zone=  Collections.min(zonesMap.keySet());

			sentiment = getThresholdRangeSentiment(redZone, amberZone, greenZone,
					contentConfigDefinition.getDirectionOfThreshold());
		}else {
			for (InsightsKPIResultDetails resultObj : inferenceResults) {
				double val = (double) resultObj.getResults().get(comparisonField);
				if (val >= greenThreshold) {
					greenZone++;
				} else if (val >= amberThreshold) {
					amberZone++;
				} else {
					redZone++;
				}
			}			
			zonesMap.put(AssessmentReportAndWorkflowConstants.GREEN, greenZone);
			zonesMap.put(AssessmentReportAndWorkflowConstants.AMBER,amberZone);
			zonesMap.put("red",redZone);			
			zone=  Collections.max(zonesMap.keySet()); 
			sentiment = getThresholdRangeSentiment(redZone, amberZone, greenZone,
					contentConfigDefinition.getDirectionOfThreshold());
		}

		Map<String, Object> zoneWiseCount = new HashMap<>();
		zoneWiseCount.put("greenZone", zonesMap.get(AssessmentReportAndWorkflowConstants.GREEN));
		zoneWiseCount.put("amberZone", zonesMap.get(AssessmentReportAndWorkflowConstants.AMBER));
		zoneWiseCount.put("redZone", zonesMap.get("red"));
		zoneWiseCount.put("sentiment", sentiment);
		zoneWiseCount.put("zone", zone);
		zoneWiseCount.put(AssessmentReportAndWorkflowConstants.RESULT, getZoneResultValue(zone));

		return zoneWiseCount;

	}

	/**
	 * method use to get get Threshold Range Sentiment
	 * 
	 * @param redZone
	 * @param amberZone
	 * @param greenZone
	 * @param contentThresholdValue
	 * @return
	 */
	private ReportEngineEnum.KPISentiment getThresholdRangeSentiment(int redZone, int amberZone, int greenZone,
			ReportEngineEnum.DirectionOfThreshold contentThresholdValue) {
		ReportEngineEnum.KPISentiment sentiment;
		if (contentThresholdValue == ReportEngineEnum.DirectionOfThreshold.BELOW) {
			if (greenZone < (amberZone + redZone)) {
				sentiment = ReportEngineEnum.KPISentiment.POSITIVE;
			} else {
				sentiment = ReportEngineEnum.KPISentiment.NEGATIVE;
			}
		} else {
			if (greenZone > (amberZone + redZone)) {
				sentiment = ReportEngineEnum.KPISentiment.POSITIVE;
			} else {
				sentiment = ReportEngineEnum.KPISentiment.NEGATIVE;
			}
		}
		return sentiment;
	}
	
	/**
	 * Get final zone/range integer value
	 * 
	 * @param zone
	 * @return
	 */
	private int getZoneResultValue(String zone)
	{
		if(zone.equals(AssessmentReportAndWorkflowConstants.GREEN))
		{
			return 1;
		}
		else if(zone.equals(AssessmentReportAndWorkflowConstants.AMBER))
		{
			return 0;
		}
		else
		{
			return -1;
		}
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
