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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTaskSequence;
import com.cognizant.devops.platformworkflow.workflowtask.exception.WorkflowTaskInitializationException;
import com.google.gson.JsonObject;

public class WorkflowImmediateJobExecutor implements Job {
	/**
	 * 
	 */
	private static final Logger log = LogManager.getLogger(WorkflowImmediateJobExecutor.class);
	
	private static final long serialVersionUID = -282836461086782615L;
	final int maxWorkflowsRetries = ApplicationConfigProvider.getInstance().getAssessmentReport()
			.getMaxWorkflowRetries();
	private WorkflowDataHandler workflowProcessing = new WorkflowDataHandler();

	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		log.debug(" Worlflow Detail ====  Schedular Inside WorkflowImmediateJobExecutor ");
		executeImmediateWorkflow();
 
	}

	/**
	 * This class method use to fetch all immediate workflow configuration
	 * Get all ready to run workflow, fetch first workflow task,
	 * prepare request message and publish that in RabbitMq
	 */
	public void executeImmediateWorkflow() {
		log.debug(" Worlflow Detail ====  Schedular Inside executeImmediateWorkflow  ");
		List<InsightsWorkflowConfiguration> readyToRunWorkflow = workflowProcessing.getImmediateWorkFlowConfigs();

		if (!readyToRunWorkflow.isEmpty()) {
			for (InsightsWorkflowConfiguration workflowConfig : readyToRunWorkflow) {
				try {
					long executionId = System.currentTimeMillis();
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
				} catch (WorkflowTaskInitializationException e) {
					log.debug(" Worlflow Detail ====  workflow failed to execute due to MQ exception {}  ",
							workflowConfig.getWorkflowId());
					workflowProcessing.updateWorkflowDetails(workflowConfig.getWorkflowId(),
							WorkflowTaskEnum.WorkflowStatus.TASK_INITIALIZE_ERROR.toString(), false);
				}
			}
		} else {
			log.debug("Worlflow Detail ==== WorkflowImmediateJobExecutor No reports are currently on due to run ");
			
		}
	}
}
