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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTask;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformworkflow.workflowtask.message.factory.WorkflowTaskSubscriberHandler;
import com.cognizant.devops.platformworkflow.workflowtask.utils.WorkflowUtils;

public class WorkflowTaskInitializer {
	

	private static final Logger log = LogManager.getLogger(WorkflowTaskInitializer.class);

	/**
	 * Subscribe all workflow task using class loader,
	 * It will fetch all task from workflowjar folder which is located inside
	 * INSIGHTS_HOME
	 */
	public void registerTaskSubscriber() {
		URLClassLoader urlClassLoader = null;
		try {
			log.debug("Worlflow Detail ====  WORKFLOW_RESOLVED_PATH {}  ", WorkflowUtils.WORKFLOW_RESOLVED_PATH);
			// Getting the jar URL which contains target class
			URL[] classLoaderUrls = new URL[] { new URL(
					"file://" + WorkflowUtils.WORKFLOW_RESOLVED_PATH) };

			// Create a new URLClassLoader 
			urlClassLoader = new URLClassLoader(classLoaderUrls);

			WorkflowDAL workflowDAL = new WorkflowDAL();
			List<InsightsWorkflowTask> registerTaskList = workflowDAL.getAllWorkflowTask();
			for (InsightsWorkflowTask insightsWorkflowTaskEntity : registerTaskList) {
				if (!WorkflowDataHandler.registry.containsKey(insightsWorkflowTaskEntity.getTaskId())) {
					log.debug("Worlflow Detail ==== Task Detail {} ", insightsWorkflowTaskEntity);
					Class<?> classDetail = Class.forName(insightsWorkflowTaskEntity.getCompnentName(), true,
							urlClassLoader);
					Constructor<?> construct = classDetail.getConstructor(String.class);
					WorkflowTaskSubscriberHandler subscriberobject = (WorkflowTaskSubscriberHandler) construct
							.newInstance(insightsWorkflowTaskEntity.getMqChannel());
					WorkflowDataHandler.registry.put(insightsWorkflowTaskEntity.getTaskId(), subscriberobject);
					log.debug("Worlflow Detail ==== Task Detail {}  with path {} registered successfully ",
							insightsWorkflowTaskEntity.getMqChannel(), insightsWorkflowTaskEntity.getCompnentName());
				} else {
					log.debug("Worlflow Detail ==== Task is already subscribed  ==== Task Detail {} ",
							insightsWorkflowTaskEntity);
				}
			}
		} catch (Exception e) {
			log.error(" Worlflow Detail ==== Error while registering Task {} ", e);
		}finally {
			if(urlClassLoader!=null) {
				try {
					urlClassLoader.close();
				} catch (IOException e) {
					log.error(" Error while closing urlClassLoader {} ", e);
				}
			}
		}

	}

}
