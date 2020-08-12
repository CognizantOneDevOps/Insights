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
import java.util.concurrent.Callable;

import com.cognizant.devops.platformdal.assessmentreport.InsightsContentConfig;
import com.cognizant.devops.platformreports.assessment.dal.ReportPostgresDataHandler;
import com.cognizant.devops.platformreports.assessment.datamodel.ContentConfigDefinition;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsKPIConfigDTO;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;
import com.google.gson.JsonArray;

public class ContentExecutor implements Callable<Integer> {

	private ContentConfigDefinition _contentConfigDTO;
	
	public ContentExecutor(ContentConfigDefinition contentConfigDTO) {
		this._contentConfigDTO = contentConfigDTO;

	}
	
	@Override
	public Integer call() throws Exception {		
		
		try
		{
			AssessmentReportContentProcesser contentProcessor = new AssessmentReportContentProcesser();
			contentProcessor.executeContentData(_contentConfigDTO);
		}
		catch (InsightsJobFailedException e) {			
			return _contentConfigDTO.getContentId();
		}
		
		
		return -1;
	}
	
	public static JsonArray executeContentJob(List<InsightsContentConfig> contentConfigList, InsightsKPIConfigDTO kpiConfigDTO) {
		ReportPostgresDataHandler contentProcessing = new ReportPostgresDataHandler();
		AssessmentReportContentProcesser contentProcessor = new AssessmentReportContentProcesser();
		JsonArray failedContentJobs = new JsonArray();

		for (InsightsContentConfig contentConfig : contentConfigList) {
			try {
				ContentConfigDefinition contentConfigDefinition = contentProcessing
						.convertJsonToContentConfig(contentConfig);
				if (contentConfigDefinition != null) {
					contentConfigDefinition.setExecutionId(kpiConfigDTO.getExecutionId());
					contentConfigDefinition.setSchedule(kpiConfigDTO.getSchedule());
					contentConfigDefinition.setWorkflowId(kpiConfigDTO.getWorkflowId());
					contentConfigDefinition.setReportId(kpiConfigDTO.getReportId());
					contentConfigDefinition.setAssessmentId(kpiConfigDTO.getAssessmentId());
					contentProcessor.executeContentData(contentConfigDefinition);
				} else {
					throw new InsightsJobFailedException("Content execution failed");
				}
			} catch (InsightsJobFailedException e) {		
				
				failedContentJobs.add(contentConfig.getContentId());
			}
		}
		return failedContentJobs;

	}

}
