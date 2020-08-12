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
package com.cognizant.devops.platformworkflow.workflowtask.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformworkflow.workflowtask.core.InsightsStatusProvider;
import com.cognizant.devops.platformworkflow.workflowtask.core.WorkflowSchedular;
import com.cognizant.devops.platformworkflow.workflowtask.core.WorkflowTaskInitializer;
import com.cognizant.devops.platformworkflow.workflowthread.core.WorkflowThreadPool;

/**
 * Engine execution will start from Application. 1. Load the server-config json
 * from iSight config folder 2. Initilize task based on configuration 3.
 * Initialize Platform Workflow Module. 4. Log Platform Insight Health data in
 * DB
 */
public class PlatformWorkflowApplication {
	private static Logger log = LogManager.getLogger(PlatformWorkflowApplication.class);

	public static void main(String[] args) {
		workflowExecutor();
	}

	public static void workflowExecutor() {
		// Load isight config
		ApplicationConfigCache.loadConfigCache();
		// Create Default users
		ApplicationConfigProvider.performSystemCheck();

		try {
			PlatformWorkflowApplication applicationWorkflow = new PlatformWorkflowApplication();
			applicationWorkflow.initilizeWorkflowTasks();

			WorkflowSchedular schedular = new WorkflowSchedular();
			schedular.executor();

			InsightsStatusProvider.getInstance().createInsightStatusNode("Platform Workflow Application started ",
					PlatformServiceConstants.SUCCESS);
		} catch (Exception e) {
			log.error("Exception in  Workflow Application ", e);
			InsightsStatusProvider.getInstance().createInsightStatusNode("Platform Workflow Application not started ",
					PlatformServiceConstants.FAILURE);
		}
	}

	private void initilizeWorkflowTasks() {
		WorkflowTaskInitializer taskSubscriber = new WorkflowTaskInitializer();
		try {
			taskSubscriber.registerTaskSubscriber();

			WorkflowThreadPool.getInstance();

		} catch (Exception e) {
			log.error(e);
		}
	}
}
