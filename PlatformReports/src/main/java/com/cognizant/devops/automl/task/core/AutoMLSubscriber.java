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
package com.cognizant.devops.automl.task.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.cognizant.devops.automl.task.util.AutoMLExecutor;
import com.cognizant.devops.platformcommons.core.enums.AutMLEnum;
import com.cognizant.devops.platformdal.autoML.AutoMLConfig;
import com.cognizant.devops.platformdal.autoML.AutoMLConfigDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;
import com.cognizant.devops.platformworkflow.workflowtask.message.factory.WorkflowTaskSubscriberHandler;
import com.cognizant.devops.platformworkflow.workflowthread.core.WorkflowThreadPool;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AutoMLSubscriber extends WorkflowTaskSubscriberHandler {

	private static Logger log = LogManager.getLogger(AutoMLSubscriber.class);
	private InsightsWorkflowConfiguration workflowConfig = new InsightsWorkflowConfiguration();
	private WorkflowDAL workflowDAL = new WorkflowDAL();
	private AutoMLConfigDAL autoMlDAL = new AutoMLConfigDAL();
	private long executionId;

	public AutoMLSubscriber(String routingKey) throws Exception {
		super(routingKey);
	}

	@Override
	public void handleTaskExecution(byte[] body) throws IOException {
		try {
			String message = new String(body, StandardCharsets.UTF_8);
			JsonObject incomingTaskMessage = new JsonParser().parse(message).getAsJsonObject();
			String workflowId = incomingTaskMessage.get("workflowId").getAsString();
			executionId = incomingTaskMessage.get("executionId").getAsLong();
			workflowConfig = workflowDAL.getWorkflowConfigByWorkflowId(workflowId);
			AutoMLConfig autoMlConfig = autoMlDAL.fetchUseCasesByWorkflowId(workflowId);
			List<JsonObject> failedJobs = new ArrayList<>();
			if (autoMlConfig != null) {				
				autoMlConfig.setStatus(AutMLEnum.Status.IN_PROGRESS.name());
				autoMlDAL.updateMLConfig(autoMlConfig);				
				List<Callable<JsonObject>> mlTaskToExecute = new ArrayList<>();
				AutoMLExecutor autoMLExecutor = new AutoMLExecutor(autoMlConfig);
				mlTaskToExecute.add(autoMLExecutor);
				/* segregate entire automl execution list into defined chunks */
				List<List<Callable<JsonObject>>> kpiChunkList = WorkflowThreadPool.getChunk(mlTaskToExecute, 1);
				/* submit each chunk to threadpool in a loop */
				executeAutoMLChunks(kpiChunkList, failedJobs);
				if (!failedJobs.isEmpty()) {
					autoMlConfig.setStatus(AutMLEnum.Status.ERROR.name());
					autoMlDAL.updateMLConfig(autoMlConfig);
					updateFailedTaskStatusLog(failedJobs);
				}
			}
		} catch (InsightsJobFailedException e) {
			log.error("Worlflow Detail ==== InsightsJobFailedException ==== {} ", e);
			throw new InsightsJobFailedException("Auto ML task failed to execute " + e.getMessage());
		} catch (Exception e) {
			throw new InsightsJobFailedException("AutoML task failed to execute Exception " + e.getMessage());
		}

		log.debug("Worlflow Detail ==== AutoML task completed successfully.");
	}

	private void executeAutoMLChunks(List<List<Callable<JsonObject>>> chunkList, List<JsonObject> failedJobs)
			throws InterruptedException, ExecutionException {

		for (List<Callable<JsonObject>> chunk : chunkList) {
			List<Future<JsonObject>> chunkResponse = WorkflowThreadPool.getInstance().invokeAll(chunk);
			log.debug("Worlflow Detail ==== AutoMLSubscriber chunks allocated to threadpool ");
			for (Future<JsonObject> singleChunkResponse : chunkResponse) {
				if (!singleChunkResponse.isCancelled()
						&& !singleChunkResponse.get().get("Status").getAsString().equalsIgnoreCase("Success")) {
					log.debug("Worlflow Detail ==== AutoMLSubscriber chunk completed");
					failedJobs.add(singleChunkResponse.get());
				}

			}
		}

	}

	private void updateFailedTaskStatusLog(List<JsonObject> failedJobs) {
		JsonObject statusObject = new JsonObject();
		JsonArray errorLog = new JsonArray();
		failedJobs.forEach(failedJob -> errorLog.add(failedJob.get("errorLog").getAsString()));
		statusObject.addProperty("executionId", executionId);
		statusObject.addProperty("workflowId", workflowConfig.getWorkflowId());
		statusObject.add("errorLog", errorLog);
		// statusLog set here which is class variable of WorkflowTaskSubscriberHandler
		statusLog = new Gson().toJson(statusObject);
		throw new InsightsJobFailedException("AutoML task failed to execute");
	}

}
