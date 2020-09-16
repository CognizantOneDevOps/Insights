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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformdal.assessmentreport.InsightsContentConfig;
import com.cognizant.devops.platformdal.assessmentreport.InsightsKPIConfig;
import com.cognizant.devops.platformdal.assessmentreport.InsightsReportsKPIConfig;
import com.cognizant.devops.platformdal.assessmentreport.ReportConfigDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowExecutionHistory;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformreports.assessment.content.ContentExecutor;
import com.cognizant.devops.platformreports.assessment.dal.ReportPostgresDataHandler;
import com.cognizant.devops.platformreports.assessment.datamodel.ContentConfigDefinition;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsKPIConfigDTO;
import com.cognizant.devops.platformreports.assessment.kpi.KPIExecutor;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;
import com.cognizant.devops.platformworkflow.workflowtask.message.factory.WorkflowTaskSubscriberHandler;
import com.cognizant.devops.platformworkflow.workflowtask.utils.MQMessageConstants;
import com.cognizant.devops.platformworkflow.workflowtask.utils.WorkflowUtils;
import com.cognizant.devops.platformworkflow.workflowthread.core.WorkflowThreadPool;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class ReportKPISubscriber extends WorkflowTaskSubscriberHandler {
	private static Logger log = LogManager.getLogger(ReportKPISubscriber.class.getName());

	private WorkflowDAL workflowDAL = new WorkflowDAL();
	private InsightsWorkflowConfiguration workflowConfig = new InsightsWorkflowConfiguration();
	private long executionId;

	public ReportKPISubscriber(String routingKey) throws Exception {
		super(routingKey);
	}	
	
	@Override
	public void handleTaskExecution(byte[] body) throws IOException {
		try {

			List<JsonObject> failedJobs = new ArrayList<>();
			List<InsightsKPIConfig> kpiConfigList = new ArrayList<>();
			List<Integer> contentList = new ArrayList<>();

			String message = new String(body, MQMessageConstants.MESSAGE_ENCODING);

			log.debug("Worlflow Detail ==== ReportKPISubscriber routing key  message handleDelivery ===== {} ",
					message);

			JsonObject incomingTaskMessage = new JsonParser().parse(message).getAsJsonObject();
			String workflowId = incomingTaskMessage.get("workflowId").getAsString();
			executionId = incomingTaskMessage.get("executionId").getAsLong();
			workflowConfig = workflowDAL.getWorkflowConfigByWorkflowId(workflowId);
			/* kpi and content list */

			boolean isWorkflowTaskRetry = incomingTaskMessage.get(WorkflowUtils.RETRY_JSON_PROPERTY).getAsBoolean();
			if (isWorkflowTaskRetry) {
				/* fill failed kpis and content for execution */
				extractKPIAndContentRetryList(incomingTaskMessage, kpiConfigList, contentList);
				/* execute content */
				executeContent(contentList, failedJobs);

			} else {
				Set<InsightsReportsKPIConfig> reportsKPIConfigSet = workflowConfig.getAssessmentConfig()
						.getReportTemplateEntity().getReportsKPIConfig();
				reportsKPIConfigSet.forEach(reportKpi -> kpiConfigList.add(reportKpi.getKpiConfig()));
			}
			/* Execute Kpi */
			log.debug("Worlflow Detail ==== ReportKPISubscriber before executeKPI {} ", kpiConfigList.size());
			executeKPI(kpiConfigList, failedJobs);

			if (!failedJobs.isEmpty()) {
				updateFailedTaskStatusLog(failedJobs);
			}

		} catch (InsightsJobFailedException e) {
			log.error("Worlflow Detail ==== InsightsJobFailedException ==== {} ", e);
			throw new InsightsJobFailedException("some of the Kpi's or Contents failed to execute " + e.getMessage());
		} catch (RejectedExecutionException e) {
			log.error("Worlflow Detail ==== RejectedExecutionException ==== {} ", e);
			throw new InsightsJobFailedException(
					"some of the Kpi's or Contents failed to execute RejectedExecutionException " + e.getMessage());
		} catch (Exception e) {
			log.error("Worlflow Detail ==== handleTaskExecution ==== {} ", e);
			throw new InsightsJobFailedException(
					"some of the Kpi's or Contents failed to execute Exception " + e.getMessage());
		}
		log.debug("Worlflow Detail ==== ReportKPISubscriber completed ");
	}

	private void extractKPIAndContentRetryList(JsonObject incomingTaskMessage, List<InsightsKPIConfig> kpiConfigList,
			List<Integer> contentList) {
		ReportConfigDAL reportConfigDAL = new ReportConfigDAL();
		InsightsWorkflowExecutionHistory historyConfig = workflowDAL
				.getWorkflowExecutionHistoryByHistoryId(incomingTaskMessage.get("exectionHistoryId").getAsInt());
		JsonObject statusLog = new Gson().fromJson(historyConfig.getStatusLog(), JsonObject.class);
		log.debug(" Worlflow Detail ====  extractKPIAndContentRetryList statusLog {} ", statusLog);
		if (statusLog != null) {
			Type type = new TypeToken<List<Integer>>() {
			}.getType();
			Gson gson = new Gson();
			List<Integer> kpiList = gson.fromJson(statusLog.get("kpiList"), type);
			if (!kpiList.isEmpty()) {
				kpiConfigList.addAll(reportConfigDAL.getActiveKPIConfigurationsBasedOnKPIId(kpiList));
			}
			contentList.addAll(gson.fromJson(statusLog.get("contentArray"), type));
		} else {
			log.error("Worlflow Detail ==== ReportKPISubscriber unable to parse retry message {} ", statusLog);
		}
	}

	private void executeKPI(List<InsightsKPIConfig> kpiConfigList, List<JsonObject> failedJobs)
			throws InterruptedException, ExecutionException {
		log.debug("Worlflow Detail ==== ReportKPISubscriber  inside executeKPI start ");
		int assessmentId = workflowConfig.getAssessmentConfig().getId();
		String assessmentInputDataSource = workflowConfig.getAssessmentConfig().getInputDatasource();
		int reportTemplateId = workflowConfig.getAssessmentConfig().getReportTemplateEntity().getReportId();
		List<Callable<JsonObject>> kpiListToExecute = new ArrayList<>();
		for (InsightsKPIConfig kpiConfig : kpiConfigList) {
			try {
				InsightsKPIConfigDTO kpiConfigDTO = new InsightsKPIConfigDTO();
				kpiConfigDTO.setKpiId(kpiConfig.getKpiId());
				kpiConfigDTO.setKpiName(kpiConfig.getKpiName());
				kpiConfigDTO.setToolname(kpiConfig.getToolname());
				kpiConfigDTO.setGroupName(kpiConfig.getGroupName());
				kpiConfigDTO.setCategory(kpiConfig.getCategory());
				kpiConfigDTO.setDatasource(kpiConfig.getDatasource());
				kpiConfigDTO.setdBQuery(kpiConfig.getdBQuery());
				kpiConfigDTO.setResultField(kpiConfig.getResultField());
				kpiConfigDTO.setExecutionId(executionId);
				kpiConfigDTO.setReportId(reportTemplateId);
				kpiConfigDTO.setAssessmentId(assessmentId);
				kpiConfigDTO.setWorkflowId(workflowConfig.getWorkflowId());
				kpiConfigDTO.setSchedule(WorkflowTaskEnum.WorkflowSchedule.valueOf(workflowConfig.getScheduleType()));
				kpiConfigDTO.setLastRunTime(workflowConfig.getLastRun());
				kpiConfigDTO.setNextRunTime(workflowConfig.getNextRun());
				kpiConfigDTO.setOneTimeReportStartTime(workflowConfig.getAssessmentConfig().getStartDate());
				kpiConfigDTO.setOneTimeReportEndDate(workflowConfig.getAssessmentConfig().getEndDate());
				kpiConfigDTO.setInputDatasource(assessmentInputDataSource);

				KPIExecutor kpirun = new KPIExecutor(kpiConfigDTO);
				kpiListToExecute.add(kpirun);

			} catch (Exception e) {
				log.debug("Worlflow Detail ==== ReportKPISubscriber  Exception  after  thread complete  ");
			}
		}

		/* segregate entire KPI execution list into defined chunks */

		List<List<Callable<JsonObject>>> kpiChunkList = WorkflowThreadPool.getChunk(kpiListToExecute, 3);

		/* submit each chunk to threadpool in a loop */

		executeKpiChunks(kpiChunkList, failedJobs);

		log.debug("Worlflow Detail ==== ReportKPISubscriber  executeKPI completed ");
	}

	private void executeKpiChunks(List<List<Callable<JsonObject>>> chunkList, List<JsonObject> failedJobs)
			throws InterruptedException, ExecutionException {

		for (List<Callable<JsonObject>> chunk : chunkList) {
			List<Future<JsonObject>> chunkResponse = WorkflowThreadPool.getInstance().invokeAll(chunk);
			//log.debug("Worlflow Detail ==== ReportKPISubscriber  chunk submmited to thread ");
			for (Future<JsonObject> singleChunkResponse : chunkResponse) {
				if (!singleChunkResponse.isCancelled()
						&& !singleChunkResponse.get().get("Status").getAsString().equalsIgnoreCase("Success")) {
					//log.debug("Worlflow Detail ==== ReportKPISubscriber chunk response thread ");
					failedJobs.add(singleChunkResponse.get());
				}

			}
		}

	}

	private void executeContent(List<Integer> contentList, List<JsonObject> failedJobs)
			throws InterruptedException, ExecutionException {
		List<Callable<Integer>> contentListToExecute = new ArrayList<>();
		ReportPostgresDataHandler contentProcessing = new ReportPostgresDataHandler();
		int assessmentId = workflowConfig.getAssessmentConfig().getId();
		int reportTemplateId = workflowConfig.getAssessmentConfig().getReportTemplateEntity().getReportId();
		for (int contentId : contentList) {
			InsightsContentConfig contentConfig = contentProcessing.getContentConfig(contentId);
			ContentConfigDefinition contentConfigDefinition = contentProcessing
					.convertJsonToContentConfig(contentConfig);
			if (contentConfigDefinition != null) {
				contentConfigDefinition.setExecutionId(executionId);
				contentConfigDefinition
						.setSchedule(WorkflowTaskEnum.WorkflowSchedule.valueOf(workflowConfig.getScheduleType()));
				contentConfigDefinition.setWorkflowId(workflowConfig.getWorkflowId());
				contentConfigDefinition.setReportId(reportTemplateId);
				contentConfigDefinition.setAssessmentId(assessmentId);
				contentListToExecute.add(new ContentExecutor(contentConfigDefinition));

			} else {
				throw new InsightsJobFailedException("Content execution failed");
			}
		}

		/* segregate entire content execution list into defined chunks */

		List<List<Callable<Integer>>> contentChunkList = WorkflowThreadPool.getChunk(contentListToExecute, 3);

		/* submit each chunk to threadpool in a loop */

		executeContentChunks(contentChunkList, failedJobs);

	}

	private void executeContentChunks(List<List<Callable<Integer>>> chunkList, List<JsonObject> failedJobs)
			throws InterruptedException, ExecutionException {
		JsonArray failedContentList = new JsonArray();

		for (List<Callable<Integer>> chunk : chunkList) {
			List<Future<Integer>> chunkResponse = WorkflowThreadPool.getInstance().invokeAll(chunk);
			for (Future<Integer> singleChunkResponse : chunkResponse) {
				if (!singleChunkResponse.isCancelled() && singleChunkResponse.get() != -1) {
					failedContentList.add(singleChunkResponse.get());
				}

			}
		}

		if (failedContentList.size() > 0) {
			JsonObject contentObject = new JsonObject();
			contentObject.addProperty("Status", "Failure");
			contentObject.add("contentArray", failedContentList);
			failedJobs.add(contentObject);
		}

	}

	private void updateFailedTaskStatusLog(List<JsonObject> failedJobs)
			throws InterruptedException, ExecutionException {
		JsonObject statusObject = new JsonObject();
		JsonArray kpiArray = new JsonArray();
		JsonArray contentArray = new JsonArray();

		for (JsonObject failedJob : failedJobs) {
			// JsonObject payload = failedJob
			if (failedJob.has("kpiArray")) {
				failedJob.get("kpiArray").getAsJsonArray().forEach(id -> kpiArray.add(id));

			} else if (failedJob.has("contentArray")) {
				failedJob.get("contentArray").getAsJsonArray().forEach(id -> contentArray.add(id));

			}
		}

		statusObject.addProperty("executionId", executionId);
		statusObject.addProperty("workflowId", workflowConfig.getWorkflowId());
		statusObject.add("kpiList", kpiArray);
		statusObject.add("contentArray", contentArray);
		// statusLog set here which is class variable of WorkflowTaskSubscriberHandler
		statusLog = new Gson().toJson(statusObject);
		throw new InsightsJobFailedException("some of the Kpi's or Contents failed to execute");
	}

}
