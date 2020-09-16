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
package com.cognizant.devops.platformworkflow.workflowtask.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformworkflow.workflowtask.core.WorkflowExecutor;
import com.cognizant.devops.platformworkflow.workflowtask.core.WorkflowImmediateJobExecutor;
import com.cognizant.devops.platformworkflow.workflowtask.core.WorkflowRetryExecutor;
import com.cognizant.devops.platformworkflow.workflowtask.core.WorkflowTaskInitializer;
import com.cognizant.devops.platformworkflow.workflowthread.core.WorkflowThreadPool;

/**
 * Engine execution will start from Application. 1. Load the iSight config 2.
 * Initialize Inference Module. 3. Log Platform Insight Health data in DB
 */

public class PlatformWorkflowApplicationTest {
	private static Logger log = LogManager.getLogger(PlatformWorkflowApplicationTest.class);

	public PlatformWorkflowApplicationTest() {

	}

	public static void main(String[] args) {

		testWorkflowExecutor();
	}

	public static void testWorkflowExecutor() {
		// Load isight config
		ApplicationConfigCache.loadConfigCache();
		// Create Default users
		ApplicationConfigProvider.performSystemCheck();

		try {

			log.debug(" Worlflow Detail ==== Inside PlatformWorkflowApplicationTest   ");

			PlatformWorkflowApplicationTest testclass = new PlatformWorkflowApplicationTest();
			testclass.initilizeWorkflowTasks();

			Scheduler scheduler = new StdSchedulerFactory().getScheduler();
			scheduler.start();
			JobDetail jobWorkflow = JobBuilder.newJob(WorkflowExecutor.class)
					.withIdentity("WorkflowExecutor", "Workflow").build();

			Trigger triggerWorkflow = TriggerBuilder.newTrigger().withIdentity("WorkflowExecutortrigger", "Workflow")
					.startNow().build();

			JobDetail jobWorkflowRetry = JobBuilder.newJob(WorkflowRetryExecutor.class)
					.withIdentity("WorkflowRetryExecutor", "Workflow").build();

			Trigger triggeWorkflowRetry = TriggerBuilder.newTrigger()
					.withIdentity("WorkflowRetryExecutortrigger", "Workflow").startNow().build();

			JobDetail jobImmediateWorkflow = JobBuilder.newJob(WorkflowImmediateJobExecutor.class)
					.withIdentity("WorkflowImmediateJobExecutorTest", "Workflow").build();

			Trigger triggeImmediateWorkflow = TriggerBuilder.newTrigger()
					.withIdentity("WorkflowImmediateJobExecutortriggerTEst", "Workflow").startNow()
					.build();

			scheduler.start();
			scheduler.scheduleJob(jobWorkflow, triggerWorkflow);
			scheduler.scheduleJob(jobWorkflowRetry, triggeWorkflowRetry);
			scheduler.scheduleJob(jobImmediateWorkflow, triggeImmediateWorkflow);

		} catch (Exception e) {
			log.error("Exception in TriggerBuilder ", e);

		}
	}

	private void initilizeWorkflowTasks() {
		WorkflowTaskInitializer taskSubscriber = new WorkflowTaskInitializer();
		try {
			log.debug(" Worlflow Detail ==== Inside initilizeWorkflowTasks   ");
			taskSubscriber.registerTaskSubscriber();

			WorkflowThreadPool.getInstance();
		} catch (Exception e) {
			log.error(e);
		}
	}
}
