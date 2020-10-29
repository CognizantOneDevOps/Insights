/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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
package com.cognizant.devops.automl.task.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformdal.autoML.AutoMLConfig;
import com.cognizant.devops.platformdal.autoML.AutoMLConfigDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformworkflow.workflowtask.message.factory.WorkflowTaskSubscriberHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AutoMLMojoSubscriber extends WorkflowTaskSubscriberHandler {

	private static Logger log = LogManager.getLogger(AutoMLMojoSubscriber.class);
	private InsightsWorkflowConfiguration workflowConfig = new InsightsWorkflowConfiguration();
	private WorkflowDAL workflowDAL = new WorkflowDAL();
	private AutoMLConfigDAL autoMlDAL = new AutoMLConfigDAL();
	private long executionId;
	public AutoMLMojoSubscriber(String routingKey) throws Exception {
		super(routingKey);
	}

	@Override
	public void handleTaskExecution(byte[] body) throws IOException {
		
		try
		{
			String message = new String(body, StandardCharsets.UTF_8);
			JsonObject incomingTaskMessage = new JsonParser().parse(message).getAsJsonObject();
			String workflowId = incomingTaskMessage.get("workflowId").getAsString();
			executionId = incomingTaskMessage.get("executionId").getAsLong();
			workflowConfig = workflowDAL.getWorkflowConfigByWorkflowId(workflowId);
			workflowConfig.getAssessmentConfig();
			//AutoMLConfig autoMlConfig = autoMlDAL.fetchUseCasesByWorkflowId(workflowId);
			
		}
		catch(Exception e) {}
		
	}

}
