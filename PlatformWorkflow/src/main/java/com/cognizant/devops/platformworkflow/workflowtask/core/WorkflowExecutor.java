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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.cognizant.devops.platformcommons.config.ApplicationConfigInterface;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.constants.StringExpressionConstants;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTaskSequence;
import com.cognizant.devops.platformworkflow.workflowtask.exception.WorkflowTaskInitializationException;
import com.google.gson.JsonObject;

public class WorkflowExecutor implements Job , ApplicationConfigInterface{
	private static final Logger log = LogManager.getLogger(WorkflowExecutor.class);
	
	private static final long serialVersionUID = -282836461086726715L;
	final int maxWorkflowsRetries = ApplicationConfigProvider.getInstance().getAssessmentReport()
			.getMaxWorkflowRetries();
	private WorkflowDataHandler workflowProcessing = new WorkflowDataHandler();
	
	
	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		log.debug(" Worlflow Detail ====  Schedular Inside WorkflowExecutor ");
		try {
			ApplicationConfigInterface.loadConfiguration();
			initilizeWorkflowTasks();
			executeWorkflow();
		} catch (InsightsCustomException e) {
			log.error(" Worlflow Detail Error ====  {}",e.getMessage());			
		}
	}

	/**
	 * Get all ready to run workflow, fetch first workflow task,
	 * prepare request message and publish that in RabbitMq
	 */
	public void executeWorkflow() {
		log.debug(" Workflow Detail  ====  Schedular Inside executeWorkflow  ");
		long startTime = System.nanoTime();
		List<InsightsWorkflowConfiguration> readyToRunWorkflow = workflowProcessing.getReadyToRunWorkFlowConfigs();
		long processingTime =0;
		if (!readyToRunWorkflow.isEmpty()) {
			for (InsightsWorkflowConfiguration workflowConfig : readyToRunWorkflow) {
				long executionId = System.currentTimeMillis();
				log.debug(" WorKflow Detail ==== workflowId {} executionId {}  ", workflowConfig.getWorkflowId(),
						executionId);
				InsightsWorkflowTaskSequence firstworkflowTask = workflowProcessing
						.getWorkflowTaskSequenceByWorkflowId(workflowConfig.getWorkflowId());
				JsonObject mqRequestJson = new JsonObject();
				workflowProcessing.createTaskRequestJson(executionId, workflowConfig.getWorkflowId(),
						firstworkflowTask.getWorkflowTaskEntity().getTaskId(), firstworkflowTask.getNextTask(),
						firstworkflowTask.getSequence(), mqRequestJson);
				
				try {
					Thread.sleep(1);
					log.debug(" Workflow Detail ==== workflowId {} before publish message executeWorkflow {} ", workflowConfig.getWorkflowId(), mqRequestJson);
					workflowProcessing.publishMessageInMQ(firstworkflowTask.getWorkflowTaskEntity().getMqChannel(),
							mqRequestJson);
					processingTime= TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
					log.debug(StringExpressionConstants.STR_EXP_WORKFLOW_1,executionId,workflowConfig.getWorkflowId(),workflowConfig.getWorkflowType(),firstworkflowTask.getWorkflowTaskEntity().getDescription(),firstworkflowTask.getWorkflowTaskEntity().getMqChannel(),workflowConfig.getStatus(),workflowConfig.getLastRun()
							,workflowConfig.getNextRun(),workflowConfig.getScheduleType(),"-","-",processingTime,"-");			
				} catch (WorkflowTaskInitializationException e) {
					log.debug(" Worlflow Detail ====  workflowId {} failed to execute due to MQ exception {} ",
							workflowConfig.getWorkflowId());
					InsightsStatusProvider.getInstance().createInsightStatusNode("WorkflowExecutor failed due to exception: "+workflowConfig.getWorkflowId(),
							PlatformServiceConstants.FAILURE);
					workflowProcessing.updateWorkflowDetails(workflowConfig.getWorkflowId(),
							WorkflowTaskEnum.WorkflowStatus.TASK_INITIALIZE_ERROR.toString(), false);
					log.error(StringExpressionConstants.STR_EXP_WORKFLOW_1,executionId,workflowConfig.getWorkflowId(),workflowConfig.getWorkflowType(),firstworkflowTask.getWorkflowTaskEntity().getDescription(),firstworkflowTask.getWorkflowTaskEntity().getMqChannel(),workflowConfig.getStatus(),workflowConfig.getLastRun()
							,workflowConfig.getNextRun(),workflowConfig.getScheduleType(),"-","-",processingTime,e.getMessage());
					
				}catch (Exception e) {
					log.error(e);
					log.debug(" Worlflow Detail ====  workflowId{} failed to execute due to exception {} ",
							workflowConfig.getWorkflowId());
					InsightsStatusProvider.getInstance().createInsightStatusNode("WorkflowExecutor failed due to exception: "+workflowConfig.getWorkflowId(),
							PlatformServiceConstants.FAILURE);
					workflowProcessing.updateWorkflowDetails(workflowConfig.getWorkflowId(),
							WorkflowTaskEnum.WorkflowStatus.TASK_INITIALIZE_ERROR.toString(), false);
					log.error(StringExpressionConstants.STR_EXP_WORKFLOW_1,executionId,workflowConfig.getWorkflowId(),workflowConfig.getWorkflowType(),firstworkflowTask.getWorkflowTaskEntity().getDescription(),firstworkflowTask.getWorkflowTaskEntity().getMqChannel(),workflowConfig.getStatus(),workflowConfig.getLastRun()
							,workflowConfig.getNextRun(),workflowConfig.getScheduleType(),"-","-",processingTime,e.getMessage());
				}
			}
		} else {
			log.debug("Worlflow Detail ==== WorkflowExecutor No reports are currently due for run");
			InsightsStatusProvider.getInstance().createInsightStatusNode(
					" WorkflowExecutor: No reports are currently due for run ", PlatformServiceConstants.SUCCESS);
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
