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

import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.exception.InsightsJobFailedException;
import com.cognizant.devops.platformreports.assessment.datamodel.ContentConfigDefinition;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineEnum;

public class AssessmentReportContentProcesser {

	private static final Logger log = LogManager.getLogger(AssessmentReportContentProcesser.class);

	/**
	 * Used to process content based on KPI Result, It either fetch record from DB
	 * or from file Make a method to directly process content in case any kpi is
	 * successful but their any content is failed so we in such cases we need to run
	 * content only.
	 */
	public void executeContentData(ContentConfigDefinition inferenceContentConfigDefinition) {
		try {
			long startTime = System.nanoTime();
			BaseContentCategoryImpl baseContentAction;
			if (inferenceContentConfigDefinition.getCategory() == ReportEngineEnum.ContentCategory.COMPARISON
					|| inferenceContentConfigDefinition.getCategory() == ReportEngineEnum.ContentCategory.STANDARD) {
				baseContentAction = new ComparisonContentCategoryImpl(inferenceContentConfigDefinition);
			} else if (inferenceContentConfigDefinition.getCategory() == ReportEngineEnum.ContentCategory.THRESHOLD) {
				baseContentAction = new ThresholdContentCategoryImpl(inferenceContentConfigDefinition);
			} else if (inferenceContentConfigDefinition
					.getCategory() == ReportEngineEnum.ContentCategory.THRESHOLD_RANGE) {
				baseContentAction = new ThresholdRangeCategoryImpl(inferenceContentConfigDefinition);
			} else if (inferenceContentConfigDefinition.getCategory() == ReportEngineEnum.ContentCategory.MINMAX) {
				baseContentAction = new MinMaxCategoryImpl(inferenceContentConfigDefinition);
			} else if (inferenceContentConfigDefinition.getCategory() == ReportEngineEnum.ContentCategory.TREND) {
				baseContentAction = new TrendCategoryImpl(inferenceContentConfigDefinition);
			} else {
				log.error("Worlflow Detail ====  No content category executer defined for contentId {}",
						inferenceContentConfigDefinition.getContentId());
				throw new InsightsJobFailedException("Worlflow Detail ====  No content category executer defined");
			}
			baseContentAction.generateContent();
			long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
			log.debug("Type=TaskExecution  executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}",
					inferenceContentConfigDefinition.getExecutionId(),inferenceContentConfigDefinition.getWorkflowId(),inferenceContentConfigDefinition.getReportId()
					,"-",
					inferenceContentConfigDefinition.getKpiId(),inferenceContentConfigDefinition.getCategory(),processingTime,
					" ContentId :" + inferenceContentConfigDefinition.getContentId() +
					" ContentName :" +inferenceContentConfigDefinition.getContentName() +
					" action :" + inferenceContentConfigDefinition.getAction() +  
					" ContentResult :" + inferenceContentConfigDefinition.getNoOfResult());			
		} catch (InsightsJobFailedException e) {
			log.error("Worlflow Detail ====  Exception while execution content Id %d KPI Id {} exception {} ",
					inferenceContentConfigDefinition.getContentId(), inferenceContentConfigDefinition.getKpiId(), e);
			log.error("Type=TaskExecution  executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}",
					inferenceContentConfigDefinition.getExecutionId(),inferenceContentConfigDefinition.getWorkflowId(),inferenceContentConfigDefinition.getReportId()
					,"-",
					inferenceContentConfigDefinition.getKpiId(),inferenceContentConfigDefinition.getCategory(),0,
					"ContentId :" + inferenceContentConfigDefinition.getContentId() +
					"ContentName :" +inferenceContentConfigDefinition.getContentName() +
					"action :" + inferenceContentConfigDefinition.getAction() +  
					"ContentResult :" + inferenceContentConfigDefinition.getNoOfResult() + "Exception while execution content Id " + e.getMessage() );			
			throw new InsightsJobFailedException("Exception while execution content Id");
		}
	}
}
