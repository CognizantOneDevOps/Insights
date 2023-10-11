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

import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;

public class WorkflowSchedular {
	private static final Logger log = LogManager.getLogger(WorkflowSchedular.class);

	/**
	 * This method used to configure schedular based on server-config.json
	 * configuration
	 */
	public void executor() {
		try {
			Scheduler scheduler = new StdSchedulerFactory().getScheduler();
			JobDetail jobWorkflow = JobBuilder.newJob(WorkflowExecutor.class)
			.withIdentity(AssessmentReportAndWorkflowConstants.WORKFLOW_EXECUTOR,AssessmentReportAndWorkflowConstants. WORKFLOW_EXECUTOR).build();

			log.debug("Worlflow Detail ====  Workflow Executor corn created ==== {} ",
					ApplicationConfigProvider.getInstance().getWorkflowDetails().getWorkflowExecutorCron());

			CronTrigger triggerWorkflow = TriggerBuilder.newTrigger()
					.withIdentity("WorkflowExecutortrigger", AssessmentReportAndWorkflowConstants.WORKFLOW_EXECUTOR)
					.withSchedule(CronScheduleBuilder.cronSchedule(
							ApplicationConfigProvider.getInstance().getWorkflowDetails().getWorkflowExecutorCron()).inTimeZone(TimeZone.getTimeZone("UTC")))
					.build();

			JobDetail jobWorkflowRetry = JobBuilder.newJob(WorkflowRetryExecutor.class)
					.withIdentity(AssessmentReportAndWorkflowConstants.WORKFLOW_RETRY_EXECUTOR,AssessmentReportAndWorkflowConstants.WORKFLOW_RETRY_EXECUTOR)
					.build();

			log.debug("Worlflow Detail ====  Workflow Retry Executor corn created ==== {} ",
					ApplicationConfigProvider.getInstance().getWorkflowDetails().getWorkflowRetryExecutorCron());

			CronTrigger triggeWorkflowRetry = TriggerBuilder.newTrigger()
					.withIdentity("WorkflowRetryExecutortrigger", AssessmentReportAndWorkflowConstants.WORKFLOW_RETRY_EXECUTOR)
					.withSchedule(CronScheduleBuilder.cronSchedule(ApplicationConfigProvider.getInstance()
							.getWorkflowDetails().getWorkflowRetryExecutorCron()).inTimeZone(TimeZone.getTimeZone("UTC")))
					.build();

			JobDetail jobImmediateWorkflow = JobBuilder.newJob(WorkflowImmediateJobExecutor.class)
					.withIdentity(AssessmentReportAndWorkflowConstants.WORKFLOW_IMMEDIATE_JOB_EXECUTOR, AssessmentReportAndWorkflowConstants.WORKFLOW_IMMEDIATE_JOB_EXECUTOR).build();

			log.debug("Worlflow Detail ====  Workflow WorkflowImmediateJobExecutor Executor corn created ==== {} ",
					"0 */5 * ? * *");

			CronTrigger triggeImmediateWorkflow = TriggerBuilder.newTrigger()
					.withIdentity(AssessmentReportAndWorkflowConstants.WORKFLOW_IMMEDIATE_JOB_EXECUTOR, AssessmentReportAndWorkflowConstants.WORKFLOW_IMMEDIATE_JOB_EXECUTOR)
					.withSchedule(CronScheduleBuilder.cronSchedule("0 */5 * ? * *").inTimeZone(TimeZone.getTimeZone("UTC")))
					.build();
			
			JobDetail jobAutoWorkflowCorrection = JobBuilder.newJob(WorkflowAutoCorrectionExecutor.class)
					.withIdentity(AssessmentReportAndWorkflowConstants.WORKFLOW_AUTOCORRECTION_EXECUTOR , AssessmentReportAndWorkflowConstants.WORKFLOW_AUTOCORRECTION_EXECUTOR).build();

			log.debug("Worlflow Detail ====  Workflow WorkflowAutoCorrectionExecutor Executor corn created ==== {} ",
					ApplicationConfigProvider.getInstance().getWorkflowDetails().getWorkflowAutoCorrectionSchedular());

			CronTrigger triggeAutoWorkflowCorrection = TriggerBuilder.newTrigger()
					.withIdentity(AssessmentReportAndWorkflowConstants.WORKFLOW_AUTOCORRECTION_EXECUTOR, AssessmentReportAndWorkflowConstants.WORKFLOW_AUTOCORRECTION_EXECUTOR)
					.withSchedule(CronScheduleBuilder.cronSchedule(ApplicationConfigProvider.getInstance().getWorkflowDetails().getWorkflowAutoCorrectionSchedular()).inTimeZone(TimeZone.getTimeZone("UTC")))
					.build();

			JobDetail jobOfflineAlertWorkflow = JobBuilder.newJob(WorkflowOfflineAlertExecutor.class)
					.withIdentity(AssessmentReportAndWorkflowConstants.WORKFLOW_OFFLINE_ALERT_EXECUTOR, AssessmentReportAndWorkflowConstants.WORKFLOW_OFFLINE_ALERT_EXECUTOR).build();

			log.debug("Worlflow Detail ====  Workflow WorkflowOfflineAlertJobExecutor Executor corn created ==== {} ",
					"0 */10 * ? * *");

			CronTrigger triggeOfflineAlertWorkflow = TriggerBuilder.newTrigger()
					.withIdentity(AssessmentReportAndWorkflowConstants.WORKFLOW_OFFLINE_ALERT_EXECUTOR, AssessmentReportAndWorkflowConstants.WORKFLOW_OFFLINE_ALERT_EXECUTOR)
					.withSchedule(CronScheduleBuilder.cronSchedule("0 */10 * ? * *").inTimeZone(TimeZone.getTimeZone("UTC")))
					.build();
			
			scheduler.start();
			scheduler.scheduleJob(jobWorkflow, triggerWorkflow);
			scheduler.scheduleJob(jobWorkflowRetry, triggeWorkflowRetry);
			scheduler.scheduleJob(jobImmediateWorkflow, triggeImmediateWorkflow);
			scheduler.scheduleJob(jobAutoWorkflowCorrection, triggeAutoWorkflowCorrection);  
			scheduler.scheduleJob(jobOfflineAlertWorkflow, triggeOfflineAlertWorkflow);
		} catch (SchedulerException e) {
			log.error("Error creating scheduler {}",e.getMessage());
			InsightsStatusProvider.getInstance().createInsightStatusNode("Error creating scheduler "+e.getMessage(),
					PlatformServiceConstants.FAILURE);		
		}

	}

}
