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
package com.cognizant.devops.platformworkflow.workflowtask.core;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowExecutionHistory;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTask;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTaskSequence;
import com.cognizant.devops.platformworkflow.workflowtask.exception.WorkflowTaskInitializationException;
import com.cognizant.devops.platformworkflow.workflowtask.utils.WorkflowUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * This class mainly used to handle all retry workflow scenario
 */
public class WorkflowRetryExecutor implements Job {

	private static final Logger log = LogManager.getLogger(WorkflowRetryExecutor.class);
	private static final long serialVersionUID = -282836461086726715L;
	final int maxWorkflowsRetries = ApplicationConfigProvider.getInstance().getAssessmentReport()
			.getMaxWorkflowRetries();
	private WorkflowDataHandler workflowProcessing = new WorkflowDataHandler();
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.debug(" Worlflow Detail ====  Schedular Inside WorkflowRetryExecutor ");
		retryWorkflows();	
	}
	
	
	
	public void retryWorkflows() {
		
		retryWorkflowWithFailedTask();
		retryWorkflowWithCompletedTask();
		retryWorkflowWithoutHistory();
	}

	/**
	 * Get all record with workflow in error state and one of the tasks is in error
	 * state
	 */
	public void retryWorkflowWithFailedTask() {
		JsonParser parser = new JsonParser();
		log.debug(" Worlflow Detail ====  Inside WorkflowRetryExecutor retryWorkflowWithFailedTask");
		List<InsightsWorkflowExecutionHistory> readyToRunWorkflowHistory = workflowProcessing.getFailedTasksForRetry();
		for (InsightsWorkflowExecutionHistory workflowHistory : readyToRunWorkflowHistory) {
			if (workflowHistory.getRetryCount() < maxWorkflowsRetries) {
				InsightsWorkflowTask firstworkflowTask = workflowProcessing
						.getWorkflowTaskByTaskId(workflowHistory.getCurrenttask());
				JsonObject mqRetryJsonObject = parser.parse(workflowHistory.getRequestMessage()).getAsJsonObject();
				mqRetryJsonObject.addProperty(WorkflowUtils.RETRY_JSON_PROPERTY, true);
				mqRetryJsonObject.addProperty(WorkflowUtils.EXECUTION_HISTORY_JOSN_PROPERTY, workflowHistory.getId());
				log.debug(" Worlflow Detail  ====  Retry flow workflowHistory {}  ", workflowHistory);
				try {
					log.debug(" Worlflow Detail ==== before publish message retryWorkflowWithFailedTask {} ",
							mqRetryJsonObject);
					workflowProcessing.publishMessageInMQ(firstworkflowTask.getMqChannel(), mqRetryJsonObject);
				} catch (WorkflowTaskInitializationException e) {
					log.debug(" Worlflow Detail ====  workflow failed to execute due to MQ exception {}  ",
							workflowHistory.getWorkflowConfig().getWorkflowId());
				}
			} else {
				log.debug(" Worlflow Detail  ==== Retry flow max retries overflow  workflowHistory {}  ",
						workflowHistory);
				workflowProcessing.updateRetryWorkflowExecutionHistory(workflowHistory.getId(),
						workflowHistory.getWorkflowConfig().getWorkflowId(),
						WorkflowTaskEnum.WorkflowStatus.ABORTED.toString(), "");
			}
		}
	}

	/**
	 * Gell all rerty record if Workflow in error state and atleast one of the tasks
	 * is completed and none is in error state
	 */
	private void retryWorkflowWithCompletedTask() {
		log.debug(" Worlflow Detail ====  Inside WorkflowRetryExecutor retryWorkflowWithCompletedTask ");
		JsonParser parser = new JsonParser();
		List<InsightsWorkflowExecutionHistory> readyToRunWorkflowHistory = workflowProcessing.getNextTasksForRetry();
		for (InsightsWorkflowExecutionHistory lastCompletedTaskExecution : readyToRunWorkflowHistory) {
			JsonObject mqRetryJsonObject = parser.parse(lastCompletedTaskExecution.getRequestMessage())
					.getAsJsonObject();
			mqRetryJsonObject.addProperty("exectionHistoryId", lastCompletedTaskExecution.getId());
			String message = new Gson().toJson(mqRetryJsonObject);
			Map<String, Object> requestMessage = WorkflowUtils.convertJsonObjectToMap(message);
			log.debug(
					" Worlflow Detail  ==== Inside WorkflowRetryExecutor retryWorkflowWithCompletedTask Retry flow workflowHistory {}  ",
					lastCompletedTaskExecution);
			try {
				workflowProcessing.publishMessageToNextInMQ(requestMessage);
			} catch (WorkflowTaskInitializationException e) {
				log.debug(
						" Worlflow Detail  ====  workflow failed to retry and will be picked up in next retry schedule  {} ",
						lastCompletedTaskExecution);
			}
		}

	}

	/**
	 * Get all retry record, if Workflow in error state and has none of the tasks
	 * completed or failed
	 */
	private void retryWorkflowWithoutHistory() {
		log.debug(" Worlflow Detail ====  Inside WorkflowRetryExecutor retryWorkflowWithoutHistory ");
		List<InsightsWorkflowConfiguration> readyToRetryWorkflow = workflowProcessing.getReadyToRetryWorkflows();
		
		log.debug(" Worlflow Detail ==== retryWorkflowWithoutHistory {} ", readyToRetryWorkflow.size());

		if (!readyToRetryWorkflow.isEmpty()) {
			for (InsightsWorkflowConfiguration workflowConfig : readyToRetryWorkflow) {
				long executionId = System.currentTimeMillis();
				log.debug(" Worlflow Detail ==== retryWorkflowWithoutHistory executionId {}  ", executionId);
				InsightsWorkflowTaskSequence firstworkflowTask = workflowProcessing
						.getWorkflowTaskSequenceByWorkflowId(workflowConfig.getWorkflowId());
				JsonObject mqRequestJson = new JsonObject();
				workflowProcessing.createTaskRequestJson(executionId, workflowConfig.getWorkflowId(),
						firstworkflowTask.getWorkflowTaskEntity().getTaskId(), firstworkflowTask.getNextTask(),
						firstworkflowTask.getSequence(), mqRequestJson);
				try {
					log.debug(" Worlflow Detail ==== before publish message retryWorkflowWithoutHistory {} ",
							mqRequestJson);
					workflowProcessing.publishMessageInMQ(firstworkflowTask.getWorkflowTaskEntity().getMqChannel(),
							mqRequestJson);
				} catch (WorkflowTaskInitializationException e) {
					log.debug(
							" Worlflow Detail ==== retryWorkflowWithoutHistory workflow failed to execute due to MQ exception {}  ",
							workflowConfig.getWorkflowId());
				}
			}
		} else {
			log.debug("Worlflow Detail ====  WorkflowRetryExecutor No retry workflows are currently on due to run");
		}
	}
}
