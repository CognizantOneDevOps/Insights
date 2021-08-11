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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.persistence.PersistenceException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowExecutionHistory;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTask;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTaskSequence;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformworkflow.workflowtask.exception.WorkflowFailedTaskException;
import com.cognizant.devops.platformworkflow.workflowtask.exception.WorkflowTaskInitializationException;
import com.cognizant.devops.platformworkflow.workflowtask.message.factory.WorkflowTaskPublisherFactory;
import com.cognizant.devops.platformworkflow.workflowtask.message.factory.WorkflowTaskSubscriberHandler;
import com.cognizant.devops.platformworkflow.workflowtask.utils.WorkflowUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class WorkflowDataHandler {
	private static final Logger log = LogManager.getLogger(WorkflowDataHandler.class);

	protected static Map<Integer, WorkflowTaskSubscriberHandler> registry = new HashMap<>(0);
	WorkflowDAL workflowDAL = new WorkflowDAL();

	/**
	 * Get list of all ready to run Active workflow. NOT_STARTED and RESTART are
	 * awalys eligible for run unreceptive of date.
	 * If workflow is COMPLETED and marked as reoccurence then workflow pick up
	 * based on specified schedule item due to run .
	 * 
	 * @return
	 */
	public List<InsightsWorkflowConfiguration> getReadyToRunWorkFlowConfigs() {

		List<InsightsWorkflowConfiguration> readyToRunReports = new ArrayList<>();
		try {
			List<InsightsWorkflowConfiguration> workflowConfigs = workflowDAL
					.getAllScheduledAndActiveWorkflowConfiguration();
			for (InsightsWorkflowConfiguration worflowConfig : workflowConfigs) {

				if (worflowConfig.getStatus().equalsIgnoreCase(WorkflowTaskEnum.WorkflowStatus.COMPLETED.name())&& worflowConfig.isReoccurence()) {
					if (isWorkflowScheduledToRun(worflowConfig.getNextRun())) {
						readyToRunReports.add(worflowConfig);
						log.debug("Type=WorkFlow executionId={} workflowId={} LastRunTime={} NextRunTime={} schedule={} isTaskRetry={} TaskRetryCount={} TaskDescription={} "
								+ "TaskMQChannel={} WorkflowType={} processingTime={} status ={} message={}","-"
								,worflowConfig.getWorkflowId(),worflowConfig.getLastRun(),worflowConfig.getNextRun(),worflowConfig.getScheduleType(),"-","-","-","-",
								worflowConfig.getWorkflowType(),0 ,"Completed","-");
					}
				} else if (worflowConfig.getStatus()
						.equalsIgnoreCase(WorkflowTaskEnum.WorkflowStatus.NOT_STARTED.name())
						|| worflowConfig.getStatus()						
								.equalsIgnoreCase(WorkflowTaskEnum.WorkflowStatus.RESTART.toString())) {
					log.debug("Type=WorkFlow executionId={} WorkflowId={} LastRunTime={} NextRunTime={} schedule={} isTaskRetry={} TaskRetryCount={} TaskDescription={} "
							+ "TaskMQChannel={} WorkflowType={} processingTime={} status ={} message={}","-"
							,worflowConfig.getWorkflowId(),worflowConfig.getLastRun(),worflowConfig.getNextRun(),worflowConfig.getScheduleType(),"-","-","-","-",
							worflowConfig.getWorkflowType(),0 ,"Restart","-");
					readyToRunReports.add(worflowConfig);
				}
			}
		} catch (Exception e) {
			log.error("Error while preparing workflows for execution ", e);
			log.error("Type=WorkFlow executionId={} WorkflowId={} LastRunTime={} NextRunTime={} schedule={} isTaskRetry={} TaskRetryCount={} TaskDescription={}"
					+ "TaskMQChannel={} WorkflowType={} processingTime={} status ={} message={}"
					,"-","-","-","-","-","-","-","-","-","-",0 ,"-",e.getMessage());
		}
		return readyToRunReports;
	}

	/**
	 * Method to get all Immediate workflow config
	 * 
	 * @return
	 */
	public List<InsightsWorkflowConfiguration> getImmediateWorkFlowConfigs() {

		
		List<InsightsWorkflowConfiguration> readyToRunReports = new ArrayList<>();		
		String WorkflowId ="-" ;		
		String WorkflowType = "-";
		String status= "-";	
		Long lastruntime = null;
		Long nextruntime =null;
		String schedule="-";
		
		try {
			long startTime = System.nanoTime();
			List<InsightsWorkflowConfiguration> workflowImmediateConfigs = workflowDAL
					.getImmediateWorkflowConfiguration();
			for (InsightsWorkflowConfiguration worflowImmediateConfig : workflowImmediateConfigs) {
				if (worflowImmediateConfig.getStatus()
						.equalsIgnoreCase(WorkflowTaskEnum.WorkflowStatus.NOT_STARTED.name())
						|| worflowImmediateConfig.getStatus()
								.equalsIgnoreCase(WorkflowTaskEnum.WorkflowStatus.RESTART.toString())
						|| worflowImmediateConfig.getStatus()
								.equalsIgnoreCase(WorkflowTaskEnum.WorkflowStatus.TASK_INITIALIZE_ERROR.toString())) {
					readyToRunReports.add(worflowImmediateConfig);
					WorkflowId =worflowImmediateConfig.getWorkflowId();					
					WorkflowType = worflowImmediateConfig.getWorkflowType();
					status = worflowImmediateConfig.getStatus();
					lastruntime = worflowImmediateConfig.getLastRun();
					nextruntime=worflowImmediateConfig.getNextRun();
					schedule = worflowImmediateConfig.getScheduleType();
					long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
					log.debug("Type=WorkFlow executionId={} WorkflowId={} LastRunTime={} NextRunTime={} schedule={} isTaskRetry={} TaskRetryCount={} TaskDescription={} "
							+ "TaskMQChannel={} WorkflowType={} processingTime={} status ={} message={}","-"
							,WorkflowId,lastruntime,nextruntime,schedule,"-","-","-","-",
							WorkflowType,processingTime ,status,"-");
				}
			}
		} catch (Exception e) {
			log.error("Error while preparing getImmediateWorkFlowConfigs for execution ", e);
			log.error("Type=WorkFlow executionId={} WorkflowId={} LastRunTime={} NextRunTime={} schedule={} isTaskRetry={} TaskRetryCount={} TaskDescription={} "
					+ "TaskMQChannel={} WorkflowType={} processingTime={} status ={} message={}","-"
					,WorkflowId,lastruntime,nextruntime,schedule,"-","-","-",WorkflowType,0,status,e.getMessage());
			
		}
		return readyToRunReports;
	}

	/**
	 * Get all workflow retry task which has task ERROR entry Execution History
	 * 
	 * @return
	 */
	public List<InsightsWorkflowExecutionHistory> getFailedTasksForRetry() {

		List<InsightsWorkflowExecutionHistory> workflowHistoryIds = new ArrayList<>();
		try {
			workflowHistoryIds = workflowDAL.getErrorExecutionHistoryBasedOnWorflow();
		} catch (Exception e) {
			log.error("Error while executing workflow retry  ", e);
		}
		return workflowHistoryIds;
	}

	/**
	 * Get all workflow which are in RESTART state
	 * 
	 * @return
	 */
	public List<InsightsWorkflowConfiguration> getReadyToRetryWorkflows() {
		return workflowDAL.getAllRestartWorkflows();
	}

	/**
	 * Get all retry workflow execution history based on previous its previos state,
	 * It searches history only for workflow configutation who has in ERROR state .
	 * 
	 * @return
	 */
	public List<InsightsWorkflowExecutionHistory> getNextTasksForRetry() {
		List<InsightsWorkflowConfiguration> workflowConfigs = workflowDAL.getCompletedTaskRetryWorkflows();
		List<InsightsWorkflowExecutionHistory> workflowHistoryIds = new ArrayList<>();
		try {
			for (InsightsWorkflowConfiguration failedWorkflow : workflowConfigs) {
				// get latest workflow execution id for each failed workflow
				long latestExecutionId = workflowDAL
						.getLastestExecutionIdForFailedWorkflow(failedWorkflow.getWorkflowId());
				if (latestExecutionId != 0) // If zero means no history execution found for the given execution id
				{
					InsightsWorkflowExecutionHistory nextTask = workflowDAL
							.getLastestTaskByEndTime(failedWorkflow.getWorkflowId(), latestExecutionId);
					if (nextTask != null) {
						workflowHistoryIds.add(nextTask);
					}
				}
			}

		} catch (Exception e) {
			log.error("Error while executing workflow retry ", e);
		}
		return workflowHistoryIds;
	}

	/**
	 * Get first workflow task sequence based on workflow id
	 * 
	 * @param workflowId
	 * @return
	 */
	public InsightsWorkflowTaskSequence getWorkflowTaskSequenceByWorkflowId(String workflowId) {
		try {
			return workflowDAL.getWorkflowTaskSequenceByWorkflowId(workflowId);
		} catch (PersistenceException e) {
			log.error("Error while executing workflow retry ", e);
			throw e;
		}
	}

	/**
	 * Get Workflow Task based on taskid
	 * 
	 * @param taskId
	 * @return
	 */
	public InsightsWorkflowTask getWorkflowTaskByTaskId(int taskId) {

		return workflowDAL.getTaskByTaskId(taskId);
	}

	/**
	 * method check if workflow is due to run
	 * 
	 * @param mextRun
	 * @return true if workflow is due to run
	 */
	private static boolean isWorkflowScheduledToRun(long mextRun) {
		long now = InsightsUtils.getCurrentTimeInSeconds();
		return (now >= mextRun);
	}

	/**
	 * Publish message in RabbitMq based on raouting key and message body
	 * 
	 * @param routingKey
	 * @param mqRequestJson
	 * @throws WorkflowFailedTaskException
	 */
	public void publishMessageInMQ(String routingKey, JsonObject mqRequestJson)
			throws WorkflowTaskInitializationException {
		long startTime = System.nanoTime();
		try {
			int subscribedTaskId = mqRequestJson.get(AssessmentReportAndWorkflowConstants.CURRENTTASKID).getAsInt();
			if (registry.containsKey(subscribedTaskId)) {
				long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
				WorkflowTaskPublisherFactory.publish(routingKey, mqRequestJson.toString());				
				log.debug("Type=WorkFlow executionId={} WorkflowId={} LastRunTime={} NextRunTime={} schedule={} isTaskRetry={} TaskRetryCount={} TaskDescription={} "
						+ "TaskMQChannel={} WorkflowType={} processingTime={} status ={} message={}"
						,mqRequestJson.get("executionId"),mqRequestJson.get(AssessmentReportAndWorkflowConstants.WORKFLOW_ID).getAsString()
						,"-","-","-",mqRequestJson.get("isWorkflowTaskRetry"),"-"
						,"-",routingKey,"-",processingTime ,"Success","Message Published Successfully!");
				
			} else {
				throw new WorkflowTaskInitializationException("Worlflow Detail ====  Queue is not subscribed yet");				
			}
		} catch (Exception e) {
			log.error(" Error while publishing message in  routingKey {} queue ", routingKey, e);
			log.error("Type=WorkFlow executionId={} WorkflowId={} LastRunTime={} NextRunTime={} schedule={}  isTaskRetry={} TaskRetryCount={} TaskDescription={} "
					+ "TaskMQChannel={} WorkflowType={} processingTime={} status ={} message={}",
					mqRequestJson.get("executionId"),mqRequestJson.get(AssessmentReportAndWorkflowConstants.WORKFLOW_ID).getAsString(),"-","-","-",
					mqRequestJson.get("isWorkflowTaskRetry"),"-","-",routingKey,"-",0,"-",e.getMessage());
			throw new WorkflowTaskInitializationException("Worlflow Detail ====  Queue is not subscribed yet");			
		}
	}

	/**
	 * Method save initial Execution History record in RDBMS database.
	 * Also if it is first task sequence then update Workflow Configuration status
	 * to IN_PROGRESS
	 * 
	 * @param requestMessage
	 * @return
	 */	
	public int saveWorkflowExecutionHistory(Map<String, Object> requestMessage) {
		long startTime = System.nanoTime();
		String workflowId = String.valueOf(requestMessage.get(AssessmentReportAndWorkflowConstants.WORKFLOW_ID));
		InsightsWorkflowConfiguration workflowConfig = workflowDAL
				.getWorkflowConfigByWorkflowId(String.valueOf(requestMessage.get(AssessmentReportAndWorkflowConstants.WORKFLOW_ID)));
		Gson gsonObj = new Gson();
		InsightsWorkflowExecutionHistory historyConfig = new InsightsWorkflowExecutionHistory();
		historyConfig.setExecutionId((long) requestMessage.get(AssessmentReportAndWorkflowConstants.EXECUTIONID));
		historyConfig.setWorkflowConfig(workflowConfig);
		historyConfig.setStartTime(System.currentTimeMillis());
		historyConfig.setCurrenttask((int) requestMessage.get(AssessmentReportAndWorkflowConstants.CURRENTTASKID));
		historyConfig.setRequestMessage(gsonObj.toJson(requestMessage));
		historyConfig.setTaskStatus(WorkflowTaskEnum.WorkflowTaskStatus.IN_PROGRESS.toString());
		int historyId = workflowDAL.saveTaskworkflowExecutionHistory(historyConfig);
		if ((int) requestMessage.get("sequence") == 1) {
			updateWorkflowDetails(workflowId, WorkflowTaskEnum.WorkflowTaskStatus.IN_PROGRESS.toString(), false);
		}
		long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
		log.debug(" Worlflow Detail ==== saveWorkflowExecutionHistory completed  ");
		log.debug("Type=WorkFlow executionId={} WorkflowId={} LastRunTime={} NextRunTime={} schedule={} isTaskRetry={} TaskRetryCount={} TaskDescription={} "
				+ "TaskMQChannel={} WorkflowType={} processingTime={} status ={} message={}",requestMessage.get("executionId"),workflowId,workflowConfig.getLastRun(),
				workflowConfig.getNextRun(),workflowConfig.getScheduleType(),requestMessage.get("isWorkflowTaskRetry"),historyConfig.getRetryCount(),"-","-","-",processingTime,"InProgress","-");		
		return historyId;
	}

	/**
	 * Update End time and task status in Workflow Execution History
	 * 
	 * @param historyId
	 * @param status
	 * @param statusLog
	 */
	public void updateWorkflowExecutionHistory(int historyId, String status, String statusLog) {

		InsightsWorkflowExecutionHistory historyConfig = workflowDAL.getWorkflowExecutionHistoryByHistoryId(historyId);
		historyConfig.setEndTime(System.currentTimeMillis());
		historyConfig.setStatusLog(statusLog);
		historyConfig.setTaskStatus(status);
		workflowDAL.updateTaskworkflowExecutionHistory(historyConfig);
		log.debug(" Worlflow Detail ====  updateWorkflowExecutionHistory completed ");
	}

	/**
	 * Prepare and publish next task message in RabbitMq
	 * 
	 * @param requestMessage
	 * @throws WorkflowFailedTaskException
	 */
	public void publishMessageToNextInMQ(Map<String, Object> requestMessage)
			throws WorkflowTaskInitializationException {
		long startTime = System.nanoTime();
		InsightsWorkflowTask insightsWorkflowTaskEntity = new InsightsWorkflowTask();
		String workflowId =String.valueOf(requestMessage.get(AssessmentReportAndWorkflowConstants.WORKFLOW_ID));
		try {
			log.debug(" Worlflow Detail ==== publishMessageToNextInMQ start {}", requestMessage);			
			insightsWorkflowTaskEntity = workflowDAL
					.getTaskByTaskId((int) requestMessage.get(AssessmentReportAndWorkflowConstants.NEXT_TASK_ID));
			InsightsWorkflowTaskSequence currentTaskSequence = workflowDAL
					.getWorkflowTaskSequenceByWorkflowAndTaskId(workflowId, (int) requestMessage.get(AssessmentReportAndWorkflowConstants.CURRENTTASKID));
			InsightsWorkflowTaskSequence nextTaskSequence = workflowDAL
					.getWorkflowTaskSequenceByWorkflowAndTaskId(workflowId, (int) requestMessage.get(AssessmentReportAndWorkflowConstants.NEXT_TASK_ID));
			if (currentTaskSequence.getNextTask() == -1) {
				log.debug("Worlflow Detail ==== This is last task update workflow config ");
				updateWorkflowDetails(workflowId, WorkflowTaskEnum.WorkflowTaskStatus.COMPLETED.toString(), true);
				
			} else {
				JsonObject mqRequestJson = new JsonObject();
				createTaskRequestJson((long) requestMessage.get("executionId"), workflowId,
						(int) requestMessage.get("nextTaskId"), nextTaskSequence.getNextTask(),
						nextTaskSequence.getSequence(), mqRequestJson);
				log.debug(" Worlflow Detail ====  publish message for nexttask  {}", mqRequestJson);
				publishMessageInMQ(insightsWorkflowTaskEntity.getMqChannel(), mqRequestJson);
				long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
				log.debug("Type=WorkFlow  executionId={} WorkflowId={}  LastRunTime={} NextRunTime={} schedule={} isTaskRetry={} TaskRetryCount={} TaskDescription={} "
						+ "TaskMQChannel={} WorkflowType={} processingTime={} status ={} message={}"
						,requestMessage.get("executionId"),workflowId,"-","-","-",
						requestMessage.get("isWorkflowTaskRetry"),"-",insightsWorkflowTaskEntity.getDescription(),insightsWorkflowTaskEntity.getMqChannel(),"-",processingTime,"Completed","-");
				
			}
			
			log.debug(" Worlflow Detail ====  publishMessageToNextInMQ completed ");			
			
		} catch (WorkflowTaskInitializationException we) {
			log.error("Worlflow Detail ==== Error while publishMessageToNextInMQ  ", we);
			log.error("Type=WorkFlow executionId={} WorkflowId={} LastRunTime={} NextRunTime={} schedule={} isTaskRetry={} TaskRetryCount={} TaskDescription={} "
					+ "TaskMQChannel={} WorkflowType={} processingTime={} status ={} message={}",requestMessage.get("executionId"),workflowId,"-","-","-",
					requestMessage.get("isWorkflowTaskRetry"),"-",insightsWorkflowTaskEntity.getDescription(),insightsWorkflowTaskEntity.getMqChannel(),"-",0,"Error","-");
			throw new WorkflowTaskInitializationException(
					"Worlflow Detail ====  unable to next task publish message in MQ");			
		} catch (Exception e) {
			log.error("Worlflow Detail ==== Error while publishMessageToNextInMQ  ", e);
			log.error("Type=WorkFlow executionId={}  WorkflowId={} LastRunTime={} NextRunTime={} schedule={} isTaskRetry={} TaskRetryCount={} TaskDescription={} "
					+ "TaskMQChannel={} WorkflowType={} processingTime={} status ={} message={}",requestMessage.get("executionId"),workflowId,"-","-","-",
					requestMessage.get("isWorkflowTaskRetry"),"-",insightsWorkflowTaskEntity.getDescription(),insightsWorkflowTaskEntity.getMqChannel(),"-",0,"Error","-");
			throw new WorkflowTaskInitializationException("Worlflow Detail ====  unable to publish message in MQ");
		}
	}

	/**
	 * Update next run time in workflow configuration
	 * 
	 * @param workflowId
	 * @param status
	 * @param isUpdateLastRunTime
	 */	
	public void updateWorkflowDetails(String workflowId, String status, boolean isUpdateLastRunTime) {

		InsightsWorkflowConfiguration workflowConfig = workflowDAL.getWorkflowConfigByWorkflowId(workflowId);
		boolean runImmediate = workflowConfig.isRunImmediate();
		workflowConfig.setRunImmediate(Boolean.FALSE);
		if (isUpdateLastRunTime) {
			long workflowlastRunTime = InsightsUtils.getCurrentTimeInSeconds();
			log.debug(" Worlflow Detail ==== Last nextruntime: {} ",workflowConfig.getNextRun());
			long nextRunTime = InsightsUtils.getNextRunTime(workflowConfig.getNextRun(),
					workflowConfig.getScheduleType(), false);
			log.debug(" Worlflow Detail ==== Next nextruntime: {} ",nextRunTime);
			workflowConfig.setNextRun(nextRunTime);
			workflowConfig.setLastRun(workflowlastRunTime);
		}
		if (status.equalsIgnoreCase("ERROR") || status.equalsIgnoreCase("ABORTED")) {
			workflowConfig.setLastRun(InsightsUtils.getCurrentTimeInSeconds());
		} else if (status.equalsIgnoreCase("TASK_INITIALIZE_ERROR") && runImmediate) {
			workflowConfig.setRunImmediate(Boolean.TRUE);
		}
		workflowConfig.setStatus(status);
		workflowDAL.updateWorkflowConfig(workflowConfig);
		log.debug(" Worlflow Detail ====  updateWorkflowStatus completed ");
	}

	/**
	 * Update WorkflowExecutionHistory for retry flow.
	 * If retry count exceeded beyound maxWorkflowRetries then change task status
	 * and workflow status to ABORTED
	 * 
	 * @param historyId
	 * @param workflowId
	 * @param status
	 * @param statusLog
	 */
	public void updateRetryWorkflowExecutionHistory(int historyId, String workflowId, String status, String statusLog) {
		long startTime = System.nanoTime();
		InsightsWorkflowExecutionHistory historyConfig = workflowDAL.getWorkflowExecutionHistoryByHistoryId(historyId);
		if (status.equalsIgnoreCase(WorkflowTaskEnum.WorkflowStatus.IN_PROGRESS.toString())) {
			historyConfig.setStartTime(System.currentTimeMillis());
			historyConfig.setRetryCount(historyConfig.getRetryCount() + 1);
			updateWorkflowDetails(workflowId, WorkflowTaskEnum.WorkflowTaskStatus.IN_PROGRESS.toString(), false);
		} else if (status.equalsIgnoreCase(WorkflowTaskEnum.WorkflowStatus.ABORTED.toString())) {
			updateWorkflowDetails(workflowId, WorkflowTaskEnum.WorkflowTaskStatus.ABORTED.toString(), false);
		}
		historyConfig.setTaskStatus(status);
		workflowDAL.updateTaskworkflowExecutionHistory(historyConfig);
		long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
		log.debug(" Worlflow Detail ====  updateWorkflowExecutionHistory completed ");
		log.debug("Type=WorkFlow  WorkflowId={} executionId={} LastRunTime={} NextRunTime={} schedule={} isTaskRetry={} TaskRetryCount={} TaskDescription={} "
				+ "TaskMQChannel={} WorkflowType={} processingTime={} status ={} message={}"
				,historyConfig.getExecutionId(),"-","-","-","-"
				,"-",historyConfig.getRetryCount(),"-","-","-",processingTime,historyConfig.getTaskStatus(),"-");
	}

	/**
	 * Create task request json
	 * 
	 * @param executionId
	 * @param WorkflowId
	 * @param currentTaskId
	 * @param nextTaskId
	 * @param sequence
	 * @param mqRequestJson
	 */
	public void createTaskRequestJson(long executionId, String workflowId, int currentTaskId, int nextTaskId,
			int sequence, JsonObject mqRequestJson) {
		mqRequestJson.addProperty(AssessmentReportAndWorkflowConstants.EXECUTIONID, executionId);
		mqRequestJson.addProperty(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
		mqRequestJson.addProperty(AssessmentReportAndWorkflowConstants.CURRENTTASKID, currentTaskId);
		mqRequestJson.addProperty(AssessmentReportAndWorkflowConstants.NEXT_TASK_ID, nextTaskId);
		mqRequestJson.addProperty("sequence", sequence);
		mqRequestJson.addProperty(WorkflowUtils.RETRY_JSON_PROPERTY, false);
	}

	public static Map<Integer, WorkflowTaskSubscriberHandler> getRegistry() {
		return registry;
	}

	public static void setRegistry(Map<Integer, WorkflowTaskSubscriberHandler> registry) {
		WorkflowDataHandler.registry = registry;
	}
	
	
}
