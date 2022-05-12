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
import java.util.concurrent.TimeUnit;

import javax.persistence.PersistenceException;

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
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTaskSequence;
import com.cognizant.devops.platformworkflow.workflowtask.exception.WorkflowTaskInitializationException;
import com.google.gson.JsonObject;

public class WorkflowImmediateJobExecutor implements Job, ApplicationConfigInterface {
	/**
	 * 
	 */
	private static final Logger log = LogManager.getLogger(WorkflowImmediateJobExecutor.class);

	private static final long serialVersionUID = -282836461086782615L;
	final int maxWorkflowsRetries = ApplicationConfigProvider.getInstance().getAssessmentReport()
			.getMaxWorkflowRetries();
	private WorkflowDataHandler workflowProcessing = new WorkflowDataHandler();
	
	//private static final String strExpression="Type=WorkFlow ExecutionId={}  WorkflowId={}  WorkflowType={} TaskDescription={} TaskMQChannel={} status ={} LastRunTime ={} NextRunTime ={} Schedule={} TaskRetryCount={} processingTime={} message={}";
	//private static final String workflowExceptionMessage="WorkflowEmmediateJobExecutorfailed to execute due to exception ";
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.debug(" Worlflow Detail ====  Schedular Inside WorkflowImmediateJobExecutor ");
		try {
			ApplicationConfigInterface.loadConfiguration();
			initilizeWorkflowTasks();
			executeImmediateWorkflow();
		} catch (Exception e) {
			log.error("Worlflow Detail Error === {}",e.getMessage());
		}

	}

	/**
	 * This class method use to fetch all immediate workflow configuration Get all
	 * ready to run workflow, fetch first workflow task, prepare request message and
	 * publish that in RabbitMq
	 */
	public void executeImmediateWorkflow() {
		long startTime = System.nanoTime();
		log.debug(" Worlflow Detail ====  Schedular Inside executeImmediateWorkflow  ");
		List<InsightsWorkflowConfiguration> readyToRunWorkflow = workflowProcessing.getImmediateWorkFlowConfigs();
       //same label with diff statis in case of error		
		String workflowId ="-";
		String workflowType= "-";
		String status= "-";
		Long lastRuntime = null;
		Long nextRuntime = null;
		String schedule= "-";
		long executionId = System.currentTimeMillis();		
		long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
		if (!readyToRunWorkflow.isEmpty()) {
			for (InsightsWorkflowConfiguration workflowConfig : readyToRunWorkflow) {
				try {
					executionId = System.currentTimeMillis();
					log.debug(" Worlflow Detail ==== WorkflowImmediateJobExecutor workflowId {} executionId {}  ",
							workflowConfig.getWorkflowId(), executionId);
					InsightsWorkflowTaskSequence firstworkflowTask = workflowProcessing
							.getWorkflowTaskSequenceByWorkflowId(workflowConfig.getWorkflowId());
					JsonObject mqRequestJson = new JsonObject();
					workflowProcessing.createTaskRequestJson(executionId, workflowConfig.getWorkflowId(),
							firstworkflowTask.getWorkflowTaskEntity().getTaskId(), firstworkflowTask.getNextTask(),
							firstworkflowTask.getSequence(), mqRequestJson);
					workflowProcessing.publishMessageInMQ(firstworkflowTask.getWorkflowTaskEntity().getMqChannel(),
							mqRequestJson);		
							
					Thread.sleep(1);
					log.debug(StringExpressionConstants.STR_EXP_WORKFLOW_3,executionId,workflowConfig.getWorkflowId(),workflowConfig.getWorkflowType(),firstworkflowTask.getWorkflowTaskEntity().getDescription(),firstworkflowTask.getWorkflowTaskEntity().getMqChannel(),workflowConfig.getStatus(),"-"
							,"-",workflowConfig.getScheduleType(),"-",processingTime,"-");

				} catch (WorkflowTaskInitializationException  | PersistenceException e) {
					   //same label with diff statis in case of error
					log.debug(" Worlflow Detail ====  workflow failed to execute due to MQ exception {}  ",
							workflowConfig.getWorkflowId());
					InsightsStatusProvider.getInstance()
							.createInsightStatusNode(AssessmentReportAndWorkflowConstants.WORKFLOW_IMMEDIATE_JOB_EXECUTOR_EXCEPTION+ workflowConfig.getWorkflowId(), PlatformServiceConstants.FAILURE);
					workflowProcessing.updateWorkflowDetails(workflowConfig.getWorkflowId(),
							WorkflowTaskEnum.WorkflowStatus.TASK_INITIALIZE_ERROR.toString(), false);
					log.debug(StringExpressionConstants.STR_EXP_WORKFLOW_3,executionId,workflowConfig.getWorkflowId(),workflowConfig.getWorkflowType(),"-","-",workflowConfig.getStatus(),"-"
							,"-",workflowConfig.getScheduleType(),"-",processingTime,e.getMessage());				
				}catch(InterruptedException e) {
					   //same label with diff statis in case of error
					log.debug(" Worlflow Detail ====  workflow failed to execute due to InterruptedException {}  ",
							workflowConfig.getWorkflowId());
					InsightsStatusProvider.getInstance()
							.createInsightStatusNode(AssessmentReportAndWorkflowConstants.WORKFLOW_IMMEDIATE_JOB_EXECUTOR_EXCEPTION+ workflowConfig.getWorkflowId(), PlatformServiceConstants.FAILURE);
					workflowProcessing.updateWorkflowDetails(workflowConfig.getWorkflowId(),
							WorkflowTaskEnum.WorkflowStatus.TASK_INITIALIZE_ERROR.toString(), false);
					Thread.currentThread().interrupt();
					log.debug(StringExpressionConstants.STR_EXP_WORKFLOW_3,executionId,workflowConfig.getWorkflowId(),workflowConfig.getWorkflowType(),"-","-",workflowConfig.getStatus(),"-"
							,"-",workflowConfig.getScheduleType(),"-",processingTime,e.getMessage());					
				}catch (Exception e) {
					   //same label with diff statis in case of error
					log.error(e);
					log.debug(" Worlflow Detail ====  workflow failed to execute due exception {}  ",
							workflowConfig.getWorkflowId());
					InsightsStatusProvider.getInstance()
							.createInsightStatusNode(AssessmentReportAndWorkflowConstants.WORKFLOW_IMMEDIATE_JOB_EXECUTOR_EXCEPTION+e.getMessage()
									+ workflowConfig.getWorkflowId(), PlatformServiceConstants.FAILURE);
					workflowProcessing.updateWorkflowDetails(workflowConfig.getWorkflowId(),
							WorkflowTaskEnum.WorkflowStatus.TASK_INITIALIZE_ERROR.toString(), false);
					log.debug(StringExpressionConstants.STR_EXP_WORKFLOW_3,executionId,workflowConfig.getWorkflowId(),workflowConfig.getWorkflowType(),"-","-",workflowConfig.getStatus(),"-"
							,"-",workflowConfig.getScheduleType(),"-",processingTime,e.getMessage());					
				}
			}
		} else {
			log.debug("Worlflow Detail ==== WorkflowImmediateJobExecutor No reports are currently on due to run ");
			log.debug("Type=WorkFlow ExecutionId={}  WorkflowId={}  WorkflowType={} status ={} LastRunTime ={} NextRunTime ={} Schedule={} TaskRetryCount={} processingTime={} message={}"
					,executionId,workflowId ,workflowType,status ,"-" ,"-" ,schedule,"-",processingTime,"-");
			
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
