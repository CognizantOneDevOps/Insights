/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
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
package com.cognizant.devops.platformreports.assessment.pdf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.exception.InsightsJobFailedException;
import com.cognizant.devops.platformdal.assessmentreport.InsightsReportVisualizationContainer;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsAssessmentConfigurationDTO;
import com.google.gson.JsonObject;

public class PDFExecutionUtils {
	
	private static Logger log = LogManager.getLogger(PDFExecutionUtils.class);
	WorkflowDAL workflowDAL = new WorkflowDAL();
	
	/**
	 * Method use to update PDF details in Email execution table in database.
	 * 
	 * @param assessmentReportDTO
	 * @param pdf
	 */
	public void saveToVisualizationContainer(InsightsAssessmentConfigurationDTO assessmentReportDTO, byte[] pdf) {
		try {
			InsightsReportVisualizationContainer emailHistoryConfig = new InsightsReportVisualizationContainer();
			emailHistoryConfig.setExecutionId(assessmentReportDTO.getExecutionId());
			emailHistoryConfig.setAttachmentData(pdf);
			JsonObject incomingTaskMessageJson = JsonUtils.parseStringAsJsonObject(assessmentReportDTO.getIncomingTaskMessageJson());
			if (incomingTaskMessageJson.get("nextTaskId").getAsInt() == -1) {
				emailHistoryConfig.setStatus(WorkflowTaskEnum.WorkflowStatus.COMPLETED.name());
				emailHistoryConfig.setExecutionTime(InsightsUtils.getCurrentTimeInEpochMilliSeconds());
			} else {
				emailHistoryConfig.setStatus(WorkflowTaskEnum.EmailStatus.NOT_STARTED.name());
			}
			emailHistoryConfig.setMailAttachmentName(assessmentReportDTO.getAsseementreportname());
			emailHistoryConfig.setWorkflowConfig(assessmentReportDTO.getWorkflowId());
			workflowDAL.saveEmailExecutionHistory(emailHistoryConfig);
		} catch (Exception e) {
			log.error("Worlflow Detail ==== Error setting PDF details in Email History table");
			throw new InsightsJobFailedException(
					"Worlflow Detail ==== Error setting PDF details in Email History table");
		}
	}
}
