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
package com.cognizant.devops.platformworkflow.test;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformworkflow.workflowtask.message.factory.WorkflowTaskSubscriberHandler;
import com.cognizant.devops.platformworkflow.workflowtask.utils.MQMessageConstants;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WorkflowTestFailTaskSubscriber extends WorkflowTaskSubscriberHandler {
	
	private static Logger log = LogManager.getLogger(WorkflowTestFailTaskSubscriber.class.getName());
	private InsightsWorkflowConfiguration workflowConfig = new InsightsWorkflowConfiguration();
	private WorkflowDAL workflowDAL = new WorkflowDAL();
	private long executionId;
	
	public WorkflowTestFailTaskSubscriber(String routingKey) throws Exception {
		super(routingKey);
	}

	@Override
	public void handleTaskExecution(byte[] body) throws IOException {
		String message = new String(body, MQMessageConstants.MESSAGE_ENCODING);
		JsonObject incomingTaskMessage = new JsonParser().parse(message).getAsJsonObject();
		String workflowId = incomingTaskMessage.get("workflowId").getAsString();
		executionId = incomingTaskMessage.get("executionId").getAsLong();
		workflowConfig = workflowDAL.getWorkflowConfigByWorkflowId(workflowId);
		getStatusLog();
		getChannel();
		String messages = "{\"message\" : \"Invalid Format\"}";
		setStatusLog(messages);
		throw new IOException("Error");
	}
	

}
