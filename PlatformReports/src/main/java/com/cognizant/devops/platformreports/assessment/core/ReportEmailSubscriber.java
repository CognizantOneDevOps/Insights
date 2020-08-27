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
package com.cognizant.devops.platformreports.assessment.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformreports.assessment.datamodel.MailReport;
import com.cognizant.devops.platformreports.assessment.email.EmailProcesser;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineUtils;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;
import com.cognizant.devops.platformworkflow.workflowtask.message.factory.WorkflowTaskSubscriberHandler;
import com.cognizant.devops.platformworkflow.workflowtask.utils.MQMessageConstants;
import com.cognizant.devops.platformworkflow.workflowthread.core.WorkflowThreadPool;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ReportEmailSubscriber extends WorkflowTaskSubscriberHandler {
	private static Logger log = LogManager.getLogger(ReportEmailSubscriber.class.getName());
	private WorkflowDAL workflowDAL = new WorkflowDAL();

	MailReport mailReportDTO = new MailReport();

	private long executionId;
	private String workflowId;

	public ReportEmailSubscriber(String routingKey) throws Exception {
		super(routingKey);
	}

	@Override
	public void handleTaskExecution(byte[] body) throws IOException {
		try {
			String message = new String(body, MQMessageConstants.MESSAGE_ENCODING);
			log.debug("Workflow Detail ==== ReportEmailSubscriber routing key message handleDelivery ===== {} ",
					message);
			JsonObject incomingTaskMessage = new JsonParser().parse(message).getAsJsonObject();
			mailReportDTO = collectInfoFromDataBase(incomingTaskMessage);
			List<JsonObject> failedJobs = new ArrayList<>();
			List<Callable<JsonObject>> emailListToExecute = new ArrayList<>();
			EmailProcesser emailProcesser = new EmailProcesser(mailReportDTO);
			emailListToExecute.add(emailProcesser);
			/* segregate entire email execution list into defined chunks */
			List<List<Callable<JsonObject>>> emailChunkList = WorkflowThreadPool.getChunk(emailListToExecute, 1);
			/* submit each chunk to threadpool in a loop */
			executeEmailChunks(emailChunkList, failedJobs);
			if (!failedJobs.isEmpty()) {
				updateFailedTaskStatusLog(failedJobs);
			}
		} catch (Exception e) {
			log.error("Workflow Detail ==== ReportEmailSubscriberEmail Send failed to execute Exception ===== {} ", e);
		}

	}

	private void executeEmailChunks(List<List<Callable<JsonObject>>> chunkList, List<JsonObject> failedJobs)
			throws InterruptedException, ExecutionException {

		for (List<Callable<JsonObject>> chunk : chunkList) {
			List<Future<JsonObject>> chunkResponse = WorkflowThreadPool.getInstance().invokeAll(chunk);
			for (Future<JsonObject> singleChunkResponse : chunkResponse) {
				if (!singleChunkResponse.isCancelled()
						&& !singleChunkResponse.get().get("status").getAsString().equalsIgnoreCase("success")) {
					failedJobs.add(singleChunkResponse.get());
				}
			}
		}
	}

	public MailReport collectInfoFromDataBase(JsonObject incomingTaskMessage) {
		InsightsWorkflowConfiguration workflowConfig;
		workflowId = incomingTaskMessage.get("workflowId").getAsString();
		executionId = incomingTaskMessage.get("executionId").getAsLong();
		mailReportDTO.setTimeOfReportGeneration(InsightsUtils.insightsTimeXFormat(executionId));
		workflowConfig = workflowDAL.getWorkflowConfigByWorkflowId(workflowId);
		mailReportDTO.setAsseementreportname(workflowConfig.getAssessmentConfig().getAsseementreportname());
		String folderName = workflowConfig.getAssessmentConfig().getAsseementreportname() + "_"
				+ executionId;
		mailReportDTO.setReportFilePath(ReportEngineUtils.REPORT_PDF_EXECUTION_RESOLVED_PATH + folderName
				+ File.separator + mailReportDTO.getAsseementreportname() + "." + ReportEngineUtils.REPORT_TYPE);
		String[] recipientList = workflowConfig.getAssessmentConfig().getEmails().split(",");
		List<InternetAddress> recipientAddress = new ArrayList<>();
		for (String recipient : recipientList) {
			try {
				recipientAddress.add(new InternetAddress(recipient.trim()));
			} catch (AddressException e) {
				log.error("Workflow Detail ==== ReportEmailSubscriber Incorrect email format found ===== {} ", e);
				throw new InsightsJobFailedException("Incorrect email format found!");
			}
		}
		mailReportDTO.setMailTo(recipientAddress);
		return mailReportDTO;
	}

	private void updateFailedTaskStatusLog(List<JsonObject> failedJobs) {
		JsonObject statusObject = new JsonObject();
		JsonArray logArray = new JsonArray();
		for (JsonObject failedJob : failedJobs) {
			logArray.add(failedJob);
		}
		statusObject.addProperty("executionId", executionId);
		statusObject.addProperty("workflowId", workflowId);
		statusObject.add("log", logArray);
		// statusLog set here which is class variable of WorkflowTaskSubscriberHandler
		statusLog = new Gson().toJson(statusObject);
		log.error("Workflow Detail ==== unable to send an email statusLog {}  ", statusLog);
		throw new InsightsJobFailedException("unable to send an email");
	}
}
