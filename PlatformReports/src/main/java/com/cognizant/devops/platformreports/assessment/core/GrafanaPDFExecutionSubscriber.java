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

package com.cognizant.devops.platformreports.assessment.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformreports.assessment.dal.ReportPDFVisualizationHandlerFactory;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsAssessmentConfigurationDTO;
import com.cognizant.devops.platformreports.assessment.pdf.BasePDFProcessor;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;
import com.cognizant.devops.platformworkflow.workflowtask.core.InsightsStatusProvider;
import com.cognizant.devops.platformworkflow.workflowtask.message.factory.WorkflowTaskSubscriberHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GrafanaPDFExecutionSubscriber extends WorkflowTaskSubscriberHandler{

	private static Logger log = LogManager.getLogger(GrafanaPDFExecutionSubscriber.class.getName());

	private WorkflowDAL workflowDAL = new WorkflowDAL();
	InsightsWorkflowConfiguration workflowConfig = new InsightsWorkflowConfiguration();
	InsightsAssessmentConfigurationDTO assessmentReportDTO = null;

	public GrafanaPDFExecutionSubscriber(String routingKey) throws IOException, TimeoutException, InsightsCustomException {
		super(routingKey);
	}
 
	@Override
	public void handleTaskExecution(byte[] body) throws IOException {
		try {			
			long startTime = System.nanoTime();
			String incomingTaskMessage = new String(body, StandardCharsets.UTF_8);
			log.debug("Worlflow Detail ==== GrafanaPDFExecutionSubscriber started ... "
					+ "routing key  message handleDelivery ===== {} ",incomingTaskMessage);
			
			JsonObject incomingTaskMessageJson = new JsonParser().parse(incomingTaskMessage).getAsJsonObject();
			String workflowId = incomingTaskMessageJson.get("workflowId").getAsString();
			long executionId = incomingTaskMessageJson.get("executionId").getAsLong();
			workflowConfig = workflowDAL.getWorkflowConfigByWorkflowId(workflowId);
			log.debug("Worlflow Detail ==== scheduleType {} ", workflowConfig.getScheduleType());

			assessmentReportDTO = new InsightsAssessmentConfigurationDTO();
			assessmentReportDTO.setIncomingTaskMessageJson(incomingTaskMessage);
			assessmentReportDTO.setExecutionId(executionId);
			assessmentReportDTO.setWorkflowId(workflowId);
			BasePDFProcessor chartHandler = ReportPDFVisualizationHandlerFactory
					.getChartHandler("GrafanaPDF");
			chartHandler.generatePDF(assessmentReportDTO);
			long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
			log.debug("Type = Grafana PDF ");
			log.debug("Type=TaskExecution  executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}",
					assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getConfigId(),workflowConfig.getWorkflowType(),"-","-",
					processingTime," ReportName :" +
					assessmentReportDTO.getReportName() + " Visualizationutil :" +assessmentReportDTO.getVisualizationutil());			
		} catch (InsightsJobFailedException ie) {
			log.error("Worlflow Detail ==== GrafanaPDFExecutionSubscriber Completed with error ", ie);
			log.error("Type = Grafana PDF");
			log.error("Type=TaskExecution  executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}",
					assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getConfigId(),workflowConfig.getWorkflowType(),"-","-",
					0," ReportName :" +
					assessmentReportDTO.getReportName() + " Visualizationutil :" +assessmentReportDTO.getVisualizationutil() 
					+"GrafanaPDFExecutionSubscriber Completed with error"  + ie.getMessage());
			statusLog = ie.getMessage();
			throw ie;
		} catch (Exception e) {
			log.error("Worlflow Detail ==== GrafanaPDFExecutionSubscriber Completed with error ", e);
			log.error("Type = Grafana PDF");
			log.error("Type=TaskExecution  executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}",
					assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getConfigId(),workflowConfig.getWorkflowType(),"-","-",
					0," ReportName :" +
					assessmentReportDTO.getReportName() + " Visualizationutil :" +assessmentReportDTO.getVisualizationutil() 
					+"GrafanaPDFExecutionSubscriber Completed with error"  + e.getMessage());
			throw new InsightsJobFailedException(e.getMessage());			
		}
	}
	

}
