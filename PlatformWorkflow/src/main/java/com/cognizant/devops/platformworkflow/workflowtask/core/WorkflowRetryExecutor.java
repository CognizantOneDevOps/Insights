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
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.cognizant.devops.platformcommons.config.ApplicationConfigInterface;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.constants.StringExpressionConstants;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowExecutionHistory;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTask;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTaskSequence;
import com.cognizant.devops.platformworkflow.workflowtask.exception.WorkflowTaskInitializationException;
import com.cognizant.devops.platformworkflow.workflowtask.utils.WorkflowUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * This class mainly used to handle all retry workflow scenario
 */
public class WorkflowRetryExecutor implements Job, ApplicationConfigInterface {

	private static final Logger log = LogManager.getLogger(WorkflowRetryExecutor.class);
	private static final long serialVersionUID = -282836461086726715L;
	final int maxWorkflowsRetries = ApplicationConfigProvider.getInstance().getAssessmentReport()
			.getMaxWorkflowRetries();
	private WorkflowDataHandler workflowProcessing = new WorkflowDataHandler();
	

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.debug(" Worlflow Detail ====  Schedular Inside WorkflowRetryExecutor ");
		try {
			ApplicationConfigInterface.loadConfiguration();
			initilizeWorkflowTasks();
			retryWorkflows();
		} catch (Exception e) {
			log.error("Worlflow Detail Error ====  {}",e.getMessage());
		}	
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
		log.debug(" Worlflow Detail ====  Inside WorkflowRetryExecutor retryWorkflowWithFailedTask");
		long startTime = System.nanoTime();
		List<InsightsWorkflowExecutionHistory> readyToRunWorkflowHistory = workflowProcessing.getFailedTasksForRetry();
		for (InsightsWorkflowExecutionHistory workflowHistory : readyToRunWorkflowHistory) {
			if (workflowHistory.getRetryCount() < maxWorkflowsRetries || workflowHistory.getWorkflowConfig().getWorkflowType()
					.equalsIgnoreCase(WorkflowTaskEnum.WorkflowType.SYSTEM.name())) {
				InsightsWorkflowTask firstworkflowTask = workflowProcessing
						.getWorkflowTaskByTaskId(workflowHistory.getCurrenttask());
				JsonObject mqRetryJsonObject = JsonUtils.parseStringAsJsonObject(workflowHistory.getRequestMessage());
				mqRetryJsonObject.addProperty(WorkflowUtils.RETRY_JSON_PROPERTY, true);
				mqRetryJsonObject.addProperty(WorkflowUtils.EXECUTION_HISTORY_JOSN_PROPERTY, workflowHistory.getId());
				long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
				log.debug(" Worlflow Detail  ====  Retry flow workflowHistory {}  ", workflowHistory);
				try {
					log.debug(" Worlflow Detail ==== before publish message retryWorkflowWithFailedTask {} ",
							mqRetryJsonObject);
					workflowProcessing.publishMessageInMQ(firstworkflowTask.getMqChannel(), mqRetryJsonObject);
					log.debug(StringExpressionConstants.STR_EXP_WORKFLOW_4+ StringExpressionConstants.STR_EXP_TASKRETRYCOUNT,workflowHistory.getExecutionId(),workflowHistory.getWorkflowConfig().getWorkflowId(),
							workflowHistory.getWorkflowConfig().getLastRun()
							,workflowHistory.getWorkflowConfig().getNextRun(),workflowHistory.getWorkflowConfig().getScheduleType()
							,mqRetryJsonObject.get(WorkflowUtils.RETRY_JSON_PROPERTY).getAsString(),workflowHistory.getRetryCount(),
							"-","-",workflowHistory.getWorkflowConfig().getWorkflowType(),processingTime,workflowHistory.getTaskStatus(),"-");					
				} catch (WorkflowTaskInitializationException e) {
					log.debug(" Worlflow Detail ====  workflow failed to execute due to MQ exception {}  ",
							workflowHistory.getWorkflowConfig().getWorkflowId());
					InsightsStatusProvider.getInstance().createInsightStatusNode("In WorkflowRetryExecutor,retryWorkflowWithFailedTask failed due to exception. WorkflowId: "+workflowHistory.getWorkflowConfig().getWorkflowId(),
							PlatformServiceConstants.FAILURE);
					log.error(StringExpressionConstants.STR_EXP_WORKFLOW_4+ StringExpressionConstants.STR_EXP_TASKRETRYCOUNT,workflowHistory.getExecutionId(),workflowHistory.getWorkflowConfig().getWorkflowId(),
							workflowHistory.getWorkflowConfig().getLastRun()
							,workflowHistory.getWorkflowConfig().getNextRun(),workflowHistory.getWorkflowConfig().getScheduleType()
							,mqRetryJsonObject.get(WorkflowUtils.RETRY_JSON_PROPERTY).getAsString(),workflowHistory.getRetryCount(),
							"-","-",workflowHistory.getWorkflowConfig().getWorkflowType(),processingTime,workflowHistory.getTaskStatus(),e.getMessage());
				
				}
			} else {
					log.debug(" Worlflow Detail  ==== Retry flow max retries overflow  workflowHistory {}  ",
							workflowHistory);
					workflowProcessing.updateRetryWorkflowExecutionHistory(workflowHistory.getId(),
							workflowHistory.getWorkflowConfig().getWorkflowId(),
							WorkflowTaskEnum.WorkflowStatus.ABORTED.toString(), "");
					log.error(StringExpressionConstants.STR_EXP_WORKFLOW_4+ StringExpressionConstants.STR_EXP_TASKRETRYCOUNT,workflowHistory.getExecutionId(),workflowHistory.getWorkflowConfig().getWorkflowId(),
							workflowHistory.getWorkflowConfig().getLastRun()
							,workflowHistory.getWorkflowConfig().getNextRun(),workflowHistory.getWorkflowConfig().getScheduleType()
							,"-",workflowHistory.getRetryCount(),
							"-","-",workflowHistory.getWorkflowConfig().getWorkflowType(),0,workflowHistory.getTaskStatus(),"Retry flow max retries overflow  workflowHistory");					
			}
		}
	}

	/**
	 * Gell all rerty record if Workflow in error state and atleast one of the tasks
	 * is completed and none is in error state
	 */
	private void retryWorkflowWithCompletedTask() {
		log.debug(" Worlflow Detail ====  Inside WorkflowRetryExecutor retryWorkflowWithCompletedTask ");
		List<InsightsWorkflowExecutionHistory> readyToRunWorkflowHistory = workflowProcessing.getNextTasksForRetry();
		for (InsightsWorkflowExecutionHistory lastCompletedTaskExecution : readyToRunWorkflowHistory) {
			long startTime = System.nanoTime();
			JsonObject mqRetryJsonObject = JsonUtils.parseStringAsJsonObject(lastCompletedTaskExecution.getRequestMessage());
			mqRetryJsonObject.addProperty("exectionHistoryId", lastCompletedTaskExecution.getId());
			String message = new Gson().toJson(mqRetryJsonObject);
			Map<String, Object> requestMessage = WorkflowUtils.convertJsonObjectToMap(message);
			log.debug(
					" Worlflow Detail  ==== Inside WorkflowRetryExecutor retryWorkflowWithCompletedTask Retry flow workflowHistory {}  ",
					lastCompletedTaskExecution);
			
			try {
				workflowProcessing.publishMessageToNextInMQ(requestMessage);
				long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
				log.debug("Type=WorkFlow ExecutionId={}  WorkflowId={}  WorkflowType={} TaskDescription={} TaskMQChannel={} status ={} LastRunTime ={} NextRunTime ={} Schedule ={} TaskRetryCount={} RetryType ={} isTaskRetry={} processingTime={} message={}"
						,lastCompletedTaskExecution.getExecutionId(),lastCompletedTaskExecution.getWorkflowConfig().getWorkflowId(),lastCompletedTaskExecution.getWorkflowConfig().getWorkflowType(),"-","-",lastCompletedTaskExecution.getTaskStatus(),lastCompletedTaskExecution.getWorkflowConfig().getLastRun(),"-",lastCompletedTaskExecution.getWorkflowConfig().getScheduleType(),lastCompletedTaskExecution.getRetryCount(),"-","WorkflowWithCompletedTask",processingTime,"-");
				
				log.debug(StringExpressionConstants.STR_EXP_WORKFLOW_4+ StringExpressionConstants.STR_EXP_TASKRETRYCOUNT,lastCompletedTaskExecution.getExecutionId(),lastCompletedTaskExecution.getWorkflowConfig().getWorkflowId(),
						lastCompletedTaskExecution.getWorkflowConfig().getLastRun()
						,lastCompletedTaskExecution.getWorkflowConfig().getNextRun(),lastCompletedTaskExecution.getWorkflowConfig().getScheduleType()
						,mqRetryJsonObject.get(WorkflowUtils.RETRY_JSON_PROPERTY).getAsString(),lastCompletedTaskExecution.getRetryCount(),
						"-","-",lastCompletedTaskExecution.getWorkflowConfig().getWorkflowType(),processingTime,lastCompletedTaskExecution.getTaskStatus(),
						"Inside WorkflowRetryExecutor retryWorkflowWithCompletedTask Retry flow workflowHistory");
			} catch (WorkflowTaskInitializationException e) {
				log.error(
						" Worlflow Detail  ====  workflow failed to retry and will be picked up in next retry schedule  {} ",
						lastCompletedTaskExecution);
				log.error(StringExpressionConstants.STR_EXP_WORKFLOW_4+ StringExpressionConstants.STR_EXP_TASKRETRYCOUNT,lastCompletedTaskExecution.getExecutionId(),lastCompletedTaskExecution.getWorkflowConfig().getWorkflowId(),
						lastCompletedTaskExecution.getWorkflowConfig().getLastRun()
						,lastCompletedTaskExecution.getWorkflowConfig().getNextRun(),lastCompletedTaskExecution.getWorkflowConfig().getScheduleType()
						,mqRetryJsonObject.get(WorkflowUtils.RETRY_JSON_PROPERTY).getAsString(),lastCompletedTaskExecution.getRetryCount(),
						"-","-",lastCompletedTaskExecution.getWorkflowConfig().getWorkflowType(),0,lastCompletedTaskExecution.getTaskStatus(),
						"In WorkflowRetryExecutor,retryWorkflowWithCompletedTask failed due to exception.");
				InsightsStatusProvider.getInstance().createInsightStatusNode("In WorkflowRetryExecutor,retryWorkflowWithCompletedTask failed due to exception. Last completed execution: "+lastCompletedTaskExecution,
						PlatformServiceConstants.FAILURE);				
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
				long startTime = System.nanoTime();
				log.debug(" Worlflow Detail ==== retryWorkflowWithoutHistory executionId {}  ", executionId);
				InsightsWorkflowTaskSequence firstworkflowTask = workflowProcessing
						.getWorkflowTaskSequenceByWorkflowId(workflowConfig.getWorkflowId());
				JsonObject mqRequestJson = new JsonObject();
				workflowProcessing.createTaskRequestJson(executionId, workflowConfig.getWorkflowId(),
						firstworkflowTask.getWorkflowTaskEntity().getTaskId(), firstworkflowTask.getNextTask(),
						firstworkflowTask.getSequence(), mqRequestJson);
				try {
					Thread.sleep(1);
					log.debug(" Worlflow Detail ==== before publish message retryWorkflowWithoutHistory {} ",
							mqRequestJson);
					workflowProcessing.publishMessageInMQ(firstworkflowTask.getWorkflowTaskEntity().getMqChannel(),
							mqRequestJson);
					long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
					
					log.debug("Type=WorkFlow ExecutionId={}  WorkflowId={}  WorkflowType={} status ={}"
							+ " LastRunTime ={} NextRunTime ={} Schedule ={}   TaskDescription={}  TaskMQChannel={} "
							+ "TaskRetryCount={} RetryType ={}  processingTime={} message={}"
							,executionId,workflowConfig.getWorkflowId(),workflowConfig.getWorkflowType(),"-",workflowConfig.getLastRun(),workflowConfig.getNextRun(),workflowConfig.getScheduleType(),firstworkflowTask.getWorkflowTaskEntity().getDescription(),firstworkflowTask.getWorkflowTaskEntity().getMqChannel(),"-",AssessmentReportAndWorkflowConstants.WORK_FLOW_WITHOUT_HISTORY,processingTime,"-");
				} catch (WorkflowTaskInitializationException | InterruptedException e) {
					log.debug("Type=WorkFlow ExecutionId={}  WorkflowId={}  WorkflowType={} "
							+ " status ={} LastRunTime ={} NextRunTime ={} Schedule ={}   TaskDescription={} "
							+ " TaskMQChannel={} TaskRetryCount={} RetryType ={}  processingTime={} message={}"
							,executionId,workflowConfig.getWorkflowId(),workflowConfig.getWorkflowType(),"-",
							workflowConfig.getLastRun(),workflowConfig.getNextRun(),workflowConfig.getScheduleType(),
							firstworkflowTask.getWorkflowTaskEntity().getDescription(),firstworkflowTask.getWorkflowTaskEntity().getMqChannel()
							,"-",AssessmentReportAndWorkflowConstants.WORK_FLOW_WITHOUT_HISTORY,0,e.getMessage());
					log.debug(
							" Worlflow Detail ==== retryWorkflowWithoutHistory workflow failed to execute due to MQ exception {}  ",
							workflowConfig.getWorkflowId());
					
					InsightsStatusProvider.getInstance().createInsightStatusNode("In WorkflowRetryExecutor,retryWorkflowWithoutHistory failed due to exception. WorkflowId: "+workflowConfig.getWorkflowId(),
							PlatformServiceConstants.FAILURE);
					
					log.debug("Type=WorkFlow ExecutionId={}  WorkflowId={}  WorkflowType={} "
							+ " status ={} LastRunTime ={} NextRunTime ={} Schedule ={}   TaskDescription={}  TaskMQChannel={} "
							+ "TaskRetryCount={} RetryType ={}  processingTime={} message={}"
							,executionId,workflowConfig.getWorkflowId(),workflowConfig.getWorkflowType(),
							"Failure",workflowConfig.getLastRun(),workflowConfig.getNextRun(),workflowConfig.getScheduleType()
							,firstworkflowTask.getWorkflowTaskEntity().getDescription()
							,firstworkflowTask.getWorkflowTaskEntity().getMqChannel(),"-",AssessmentReportAndWorkflowConstants.WORK_FLOW_WITHOUT_HISTORY,0,e.getMessage());
					
				}
			}
		} else {
			log.debug("Worlflow Detail ====  WorkflowRetryExecutor No retry workflows are currently on due to run");
		}
	}
	
	private void initilizeWorkflowTasks() {
		WorkflowTaskInitializer taskSubscriber = new WorkflowTaskInitializer();
		try {
			taskSubscriber.registerTaskSubscriber();
		} catch (Exception e) {
			log.error(e);
		}
	}
}
