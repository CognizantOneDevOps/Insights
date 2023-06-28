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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
import com.cognizant.devops.platformcommons.constants.ErrorMessage;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.constants.ReportStatusConstants;
import com.cognizant.devops.platformcommons.constants.StringExpressionConstants;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.exception.InsightsJobFailedException;
import com.cognizant.devops.platformdal.assessmentreport.InsightsReportVisualizationContainer;
import com.cognizant.devops.platformdal.assessmentreport.InsightsReportsKPIConfig;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformreports.assessment.dal.ReportPDFVisualizationHandlerFactory;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsAssessmentConfigurationDTO;
import com.cognizant.devops.platformreports.assessment.pdf.BasePDFProcessor;
import com.cognizant.devops.platformreports.assessment.pdf.PDFKPIVisualizationProcesser;
import com.cognizant.devops.platformworkflow.workflowtask.core.InsightsStatusProvider;
import com.cognizant.devops.platformworkflow.workflowtask.message.factory.WorkflowTaskSubscriberHandler;
import com.cognizant.devops.platformworkflow.workflowthread.core.WorkflowThreadPool;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class PDFExecutionSubscriber extends WorkflowTaskSubscriberHandler {
	
	private static Logger log = LogManager.getLogger(PDFExecutionSubscriber.class.getName());

	private WorkflowDAL workflowDAL = new WorkflowDAL();
	InsightsAssessmentConfigurationDTO assessmentReportDTO = null;
	
	public PDFExecutionSubscriber(String routingKey) throws Exception {
		super(routingKey);
	}

	@Override
	public void handleTaskExecution(String incomingTaskMessage) throws IOException {
		try {
			long startTime = System.nanoTime();
			log.debug(
					"Worlflow Detail ==== PDFExecutionSubscriber started ... routing key  message handleDelivery ===== {} ",
					incomingTaskMessage);
			assessmentReportDTO = new InsightsAssessmentConfigurationDTO();
			JsonObject incomingTaskMessageJson = JsonUtils.parseStringAsJsonObject(incomingTaskMessage);
			prepareVisualizationJsonBasedOnKPIResult(incomingTaskMessageJson);
			processVisualizationJson();
			setPDFDetailsInEmailHistory(incomingTaskMessageJson);
			log.debug("Worlflow Detail ==== PDFExecutionSubscriber Completed  {} ", incomingTaskMessage);
			InsightsStatusProvider.getInstance().createInsightStatusNode("PDFExecutionSubscriber completed",
					PlatformServiceConstants.SUCCESS);
			long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
			log.debug(StringExpressionConstants.STR_EXP_TASKEXECUTION_1,assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getConfigId(),"-","-","-",processingTime,
					ReportStatusConstants.REPORT_ID+assessmentReportDTO.getReportId() + ReportStatusConstants.REPORT_NAME +assessmentReportDTO.getReportName() +  ReportStatusConstants.VISUALIZATION_UTIL +
					assessmentReportDTO.getVisualizationutil() + "PDFExecutionSubscriber Completed ");				
		} catch (InsightsJobFailedException ijfe) {
			log.error("Worlflow Detail ==== PDFExecutionSubscriber Completed with error ", ijfe);
			InsightsStatusProvider.getInstance().createInsightStatusNode(ErrorMessage.PDFEXECUTION_ERROR+ijfe.getMessage(),
					PlatformServiceConstants.FAILURE);
			statusLog = ijfe.getMessage();
			log.error(StringExpressionConstants.STR_EXP_TASKEXECUTION_1,assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getConfigId(),"-","-","-",0,
					ReportStatusConstants.REPORT_ID+assessmentReportDTO.getReportId() + ReportStatusConstants.REPORT_NAME +assessmentReportDTO.getReportName() + ReportStatusConstants.VISUALIZATION_UTIL +
					assessmentReportDTO.getVisualizationutil() + ErrorMessage.PDFEXECUTION_ERROR + ijfe.getMessage());
			throw ijfe;
		} catch (Exception e) {
			log.error("Worlflow Detail ==== PDFExecutionSubscriber Completed with error ", e);
			InsightsStatusProvider.getInstance().createInsightStatusNode(ErrorMessage.PDFEXECUTION_ERROR+e.getMessage(),
					PlatformServiceConstants.FAILURE);
			log.error(StringExpressionConstants.STR_EXP_TASKEXECUTION_1,assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getConfigId(),"-","-","-",0,
					ReportStatusConstants.REPORT_ID+assessmentReportDTO.getReportId() + ReportStatusConstants.REPORT_NAME +assessmentReportDTO.getReportName() + ReportStatusConstants.VISUALIZATION_UTIL +
					assessmentReportDTO.getVisualizationutil() + ErrorMessage.PDFEXECUTION_ERROR + e.getMessage());
			throw new InsightsJobFailedException(e.getMessage());
		}
	}

	private void prepareVisualizationJsonBasedOnKPIResult(JsonObject incomingTaskMessage)
			throws InterruptedException, ExecutionException {

		InsightsWorkflowConfiguration workflowConfig;
		String workflowId = incomingTaskMessage.get(AssessmentReportAndWorkflowConstants.WORKFLOW_ID).getAsString();
		long executionId = incomingTaskMessage.get("executionId").getAsLong();
		workflowConfig = workflowDAL.getWorkflowConfigByWorkflowId(workflowId);

		assessmentReportDTO.setAsseementreportname(workflowConfig.getAssessmentConfig().getAsseementreportname());
		assessmentReportDTO.setConfigId(workflowConfig.getAssessmentConfig().getId());
		assessmentReportDTO.setExecutionId(executionId);
		assessmentReportDTO.setWorkflowId(workflowId);
		assessmentReportDTO.setReportId(workflowConfig.getAssessmentConfig().getReportTemplateEntity().getReportId());
		assessmentReportDTO
				.setReportName(workflowConfig.getAssessmentConfig().getReportTemplateEntity().getTemplateName());
		assessmentReportDTO.setReportFilePath(workflowConfig.getAssessmentConfig().getReportTemplateEntity().getFile());
		assessmentReportDTO
				.setAsseementreportdisplayname(workflowConfig.getAssessmentConfig().getAsseementReportDisplayName());
		assessmentReportDTO.setVisualizationutil(workflowConfig.getAssessmentConfig().getReportTemplateEntity().getVisualizationutil());

		Set<InsightsReportsKPIConfig> reportsKPIConfigSet = workflowConfig.getAssessmentConfig()
				.getReportTemplateEntity().getReportsKPIConfig();
		getKPIVisualizationReportObject(reportsKPIConfigSet);

	}

	private void getKPIVisualizationReportObject(Set<InsightsReportsKPIConfig> reportsKPIConfigSet)
			throws InterruptedException, ExecutionException {

		List<JsonObject> failedJobs = new ArrayList<>();
		List<Callable<JsonObject>> kpiListToExecute = new ArrayList<>();
		JsonArray resultArray = new JsonArray();
		for (InsightsReportsKPIConfig reportKpiConfig : reportsKPIConfigSet) {
			PDFKPIVisualizationProcesser pdfProcesser = new PDFKPIVisualizationProcesser(reportKpiConfig,
					assessmentReportDTO);
			kpiListToExecute.add(pdfProcesser);
		}
		/* segregate entire KPI execution list into defined chunks */

		List<List<Callable<JsonObject>>> kpiChunkList = WorkflowThreadPool.getChunk(kpiListToExecute, 3);

		/* submit each chunk to threadpool in a loop */

		executeKpiChunks(kpiChunkList, failedJobs, resultArray);

		if (!failedJobs.isEmpty()) {
			updateFailedTaskStatusLog(failedJobs, assessmentReportDTO);
		}
		assessmentReportDTO.setVisualizationResult(resultArray);

	}

	private void executeKpiChunks(List<List<Callable<JsonObject>>> chunkList, List<JsonObject> failedJobs,
			JsonArray resultArray) throws InterruptedException, ExecutionException {
		JsonArray failedKpiList = new JsonArray();
		for (List<Callable<JsonObject>> chunk : chunkList) {
			List<Future<JsonObject>> chunkResponse = WorkflowThreadPool.getInstance().invokeAll(chunk);
			for (Future<JsonObject> singleChunkResponse : chunkResponse) {
				if (!singleChunkResponse.isCancelled()
						&& !singleChunkResponse.get().get("status").getAsString().equalsIgnoreCase("success")) {

					failedKpiList.add(singleChunkResponse.get().get("kpiId"));
				} else {
					resultArray.add(singleChunkResponse.get().get("data"));
				}
			}
		}

		if (failedKpiList.size() > 0) {
			JsonObject kpiObject = new JsonObject();
			kpiObject.addProperty("status", "failure");
			kpiObject.add(AssessmentReportAndWorkflowConstants.KEYARRAY, failedKpiList);
			failedJobs.add(kpiObject);
		}

	}

	private void updateFailedTaskStatusLog(List<JsonObject> failedJobs,
			InsightsAssessmentConfigurationDTO assessmentReportDTO) {
		JsonObject statusObject = new JsonObject();
		JsonArray kpiArray = new JsonArray();

		for (JsonObject failedJob : failedJobs) {
			if (failedJob.has(AssessmentReportAndWorkflowConstants.KEYARRAY)) {
				failedJob.get(AssessmentReportAndWorkflowConstants.KEYARRAY).getAsJsonArray().forEach(id -> kpiArray.add(id));
			}
		}
		statusObject.addProperty("executionId", assessmentReportDTO.getExecutionId());
		statusObject.addProperty(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, assessmentReportDTO.getWorkflowId());
		statusObject.add("kpiList", kpiArray);
		// statusLog set here which is class variable of WorkflowTaskSubscriberHandler
		statusLog = new Gson().toJson(statusObject);
		log.error("Worlflow Detail ==== some of the Kpi's visualization is failed to execute statusLog {} ", statusLog);
		log.error(StringExpressionConstants.STR_EXP_TASKEXECUTION_1,assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getConfigId(),"-","-","-",0,
				ReportStatusConstants.REPORT_ID+assessmentReportDTO.getReportId() + ReportStatusConstants.REPORT_NAME +assessmentReportDTO.getReportName() + ReportStatusConstants.VISUALIZATION_UTIL +
				assessmentReportDTO.getVisualizationutil() + "some of the Kpi's visualization is failed to execute");		
		throw new InsightsJobFailedException(
				"Worlflow Detail ==== some of the Kpi's visualization is failed to execute");
	}

	private void processVisualizationJson() {
		BasePDFProcessor chartHandler = ReportPDFVisualizationHandlerFactory
				.getChartHandler(assessmentReportDTO.getVisualizationutil());
		chartHandler.generatePDF(assessmentReportDTO);
	}

	private void setPDFDetailsInEmailHistory(JsonObject incomingTaskMessageJson) {
		try {
			long startTime = System.nanoTime();
			String workflowId = incomingTaskMessageJson.get(AssessmentReportAndWorkflowConstants.WORKFLOW_ID)
					.getAsString();
			InsightsReportVisualizationContainer emailHistoryConfig = new InsightsReportVisualizationContainer();
			emailHistoryConfig.setExecutionId(assessmentReportDTO.getExecutionId());
			emailHistoryConfig.setMailAttachmentName(assessmentReportDTO.getAsseementreportname());
			File attachedFile = new File(assessmentReportDTO.getPdfExportedFilePath());
			if (attachedFile.exists()) {
				emailHistoryConfig.setAttachmentData(FileUtils.readFileToByteArray(attachedFile));
			}else {
				throw new InsightsJobFailedException(ErrorMessage.WORKFLOW_ERROR);
			}
			if (incomingTaskMessageJson.get("nextTaskId").getAsInt() == -1) {
				emailHistoryConfig.setStatus(WorkflowTaskEnum.WorkflowStatus.COMPLETED.name());
				emailHistoryConfig.setExecutionTime(InsightsUtils.getCurrentTimeInEpochMilliSeconds());
			} else {
				emailHistoryConfig.setStatus(WorkflowTaskEnum.EmailStatus.NOT_STARTED.name());
			}
			emailHistoryConfig.setWorkflowConfig(workflowId);
			workflowDAL.saveEmailExecutionHistory(emailHistoryConfig);
			long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
			log.debug(StringExpressionConstants.STR_EXP_TASKEXECUTION_1,assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getConfigId(),"-","-","-",processingTime,
					ReportStatusConstants.REPORT_ID+assessmentReportDTO.getReportId() + ReportStatusConstants.REPORT_NAME +assessmentReportDTO.getReportName() + ReportStatusConstants.VISUALIZATION_UTIL +
					assessmentReportDTO.getVisualizationutil() );
		} catch (Exception e) {
			log.error(ErrorMessage.WORKFLOW_ERROR);
			log.error(StringExpressionConstants.STR_EXP_TASKEXECUTION_1,assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getConfigId(),"-","-","-",0,
					ReportStatusConstants.REPORT_ID+assessmentReportDTO.getReportId() + ReportStatusConstants.REPORT_NAME +assessmentReportDTO.getReportName() + ReportStatusConstants.VISUALIZATION_UTIL +
					assessmentReportDTO.getVisualizationutil() + "Error setting PDF details in Email History table" +e.getMessage());
			throw new InsightsJobFailedException(ErrorMessage.WORKFLOW_ERROR);
		}
	}
}
