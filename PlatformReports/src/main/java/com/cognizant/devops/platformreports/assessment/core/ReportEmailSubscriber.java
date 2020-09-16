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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformdal.assessmentreport.InsightsEmailTemplates;
import com.cognizant.devops.platformdal.assessmentreport.InsightsReportVisualizationContainer;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;
import com.cognizant.devops.platformworkflow.workflowtask.email.EmailProcesser;
import com.cognizant.devops.platformworkflow.workflowtask.email.MailReport;
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
		String message = new String(body, MQMessageConstants.MESSAGE_ENCODING);
		JsonObject statusObject = null;
		JsonObject incomingTaskMessage = new JsonParser().parse(message).getAsJsonObject();
		try {
			log.debug("Workflow Detail ==== ReportEmailSubscriber routing key message handleDelivery ===== {} ",
					message);
			InsightsEmailTemplates emailTemplate = workflowDAL
					.getEmailTemplateByWorkflowId(incomingTaskMessage.get("workflowId").getAsString());
			if (emailTemplate != null) {
				mailReportDTO = updateEmailHistoryWithEmailTemplateValues(incomingTaskMessage, emailTemplate);
				List<JsonObject> failedJobs = new ArrayList<>();
				List<JsonObject> successJobs = new ArrayList<>();
				List<Callable<JsonObject>> emailListToExecute = new ArrayList<>();
				EmailProcesser emailProcesser = new EmailProcesser(mailReportDTO);
				emailListToExecute.add(emailProcesser);
				/* segregate entire email execution list into defined chunks */
				List<List<Callable<JsonObject>>> emailChunkList = WorkflowThreadPool.getChunk(emailListToExecute, 1);
				/* submit each chunk to threadpool in a loop */
				executeEmailChunks(emailChunkList, failedJobs, successJobs);
				if (!successJobs.isEmpty()) {
					updateEmailHistoryWithStatus(incomingTaskMessage.get("executionId").getAsLong(),
							WorkflowTaskEnum.EmailStatus.COMPLETED.name());
				}
				if (!failedJobs.isEmpty()) {
					statusObject = updateFailedTaskStatusLog(failedJobs);
					throw new InsightsJobFailedException("Unable to send an email");
				}
			} else {
				throw new InsightsJobFailedException("Email template not found!");
			}
		} catch (InsightsJobFailedException e) {
			log.error("Workflow Detail ==== ReportEmailSubscriberEmail Send failed to execute Exception ===== ", e);
			updateEmailHistoryWithStatus(incomingTaskMessage.get("executionId").getAsLong(),
					WorkflowTaskEnum.EmailStatus.ERROR.name());
			if (statusObject != null) {
				setStatusLog(new Gson().toJson(statusObject));
			} else {
				setStatusLog(e.getMessage());
			}
			throw new InsightsJobFailedException("Failed to send email in ReportEmailSubscriber");
		} catch (Exception e) {
			log.error("Workflow Detail ==== ReportEmailSubscriberEmail Send failed to execute Exception ===== ", e);
		}

	}

	private void executeEmailChunks(List<List<Callable<JsonObject>>> chunkList, List<JsonObject> failedJobs,
			List<JsonObject> successJobs) throws InterruptedException, ExecutionException {

		for (List<Callable<JsonObject>> chunk : chunkList) {
			List<Future<JsonObject>> chunkResponse = WorkflowThreadPool.getInstance().invokeAll(chunk);
			for (Future<JsonObject> singleChunkResponse : chunkResponse) {
				if (!singleChunkResponse.isCancelled()
						&& !singleChunkResponse.get().get("status").getAsString().equalsIgnoreCase("success")) {
					failedJobs.add(singleChunkResponse.get());
				} else {
					successJobs.add(singleChunkResponse.get());
				}
			}
		}
	}

	public MailReport collectInfoFromDataBase(JsonObject incomingTaskMessage,
			InsightsReportVisualizationContainer emailHistory) {
		try {
			List<InternetAddress> recipientCCAddress = null;
			List<InternetAddress> recipientBCCAddress = null;
			List<InternetAddress> recipientAddress = null;
			InsightsWorkflowConfiguration workflowConfig;
			workflowId = incomingTaskMessage.get("workflowId").getAsString();
			executionId = incomingTaskMessage.get("executionId").getAsLong();
			mailReportDTO.setTimeOfReportGeneration(InsightsUtils.insightsTimeXFormat(executionId));
			workflowConfig = workflowDAL.getWorkflowConfigByWorkflowId(workflowId);
			mailReportDTO.setAsseementreportname(workflowConfig.getAssessmentConfig().getAsseementReportDisplayName());
			mailReportDTO.setReportFilePath(emailHistory.getAttachmentPath());
			if (emailHistory.getMailTo() != null) {
				String[] recipientList = emailHistory.getMailTo().split(",");
				recipientAddress = createRecipientAddress(recipientList);
			}
			if (emailHistory.getMailCC() != null) {
				String[] recipientCCList = emailHistory.getMailCC().split(",");
				recipientCCAddress = createRecipientAddress(recipientCCList);
			}
			if (emailHistory.getMailBCC() != null) {
				String[] recipientBCCList = emailHistory.getMailBCC().split(",");
				recipientBCCAddress = createRecipientAddress(recipientBCCList);
			}
			mailReportDTO.setMailTo(recipientAddress);
			mailReportDTO.setMailCC(recipientCCAddress);
			mailReportDTO.setMailBCC(recipientBCCAddress);
			mailReportDTO.setMailFrom(emailHistory.getMailFrom());
			mailReportDTO.setSubject(emailHistory.getSubject());
			mailReportDTO.setMailBody(emailHistory.getMailBody());
			return mailReportDTO;
		} catch (Exception e) {
			log.error("Workflow Detail ==== ReportEmailSubscriber Error while creating MailreportDTO ===== ", e);
			throw new InsightsJobFailedException("Error while creating MailreportDTO");
		}
	}

	/**
	 * Method to update Failed Task status
	 * 
	 * @param failedJobs
	 * @return JsonObject
	 */
	private JsonObject updateFailedTaskStatusLog(List<JsonObject> failedJobs) {
		JsonObject statusObject = new JsonObject();
		JsonArray logArray = new JsonArray();
		for (JsonObject failedJob : failedJobs) {
			logArray.add(failedJob);
		}
		statusObject.addProperty("executionId", executionId);
		statusObject.addProperty("workflowId", workflowId);
		statusObject.add("log", logArray);
		// statusLog set here which is class variable of WorkflowTaskSubscriberHandler
		log.error("Workflow Detail ==== unable to send an email statusLog {}  ", statusLog);
		return statusObject;

	}

	/**
	 * Method to update Email History record status
	 * 
	 * @param executionId
	 * @param status
	 */
	private void updateEmailHistoryWithStatus(long executionId, String status) {
		InsightsReportVisualizationContainer emailHistoryConfig = workflowDAL
				.getEmailExecutionHistoryByExecutionId(executionId);
		emailHistoryConfig.setStatus(status);
		emailHistoryConfig.setExecutionTime(InsightsUtils.getCurrentTimeInEpochMilliSeconds());
		workflowDAL.updateEmailExecutionHistory(emailHistoryConfig);
	}

	/**
	 * Method to update values in Email History table
	 * 
	 * @param incomingTaskMessage
	 * @param emailTemplate
	 * @return
	 */
	private MailReport updateEmailHistoryWithEmailTemplateValues(JsonObject incomingTaskMessage,
			InsightsEmailTemplates emailTemplate) {
		try {
			workflowId = incomingTaskMessage.get("workflowId").getAsString();
			executionId = incomingTaskMessage.get("executionId").getAsLong();
			InsightsWorkflowConfiguration workflowConfig = workflowDAL.getWorkflowConfigByWorkflowId(workflowId);
			Map<String, String> valuesMap = new HashMap<>();
			valuesMap.put("ReportDisplayName", workflowConfig.getAssessmentConfig().getAsseementReportDisplayName());
			valuesMap.put("TimeOfReportGeneration", InsightsUtils.specficTimeFormat(executionId, "yyyy-MM-dd"));
			valuesMap.put("Schedule", workflowConfig.getScheduleType());
			StringSubstitutor sub = new StringSubstitutor(valuesMap, "{", "}");
			InsightsReportVisualizationContainer emailHistoryConfig = workflowDAL
					.getEmailExecutionHistoryByExecutionId(incomingTaskMessage.get("executionId").getAsLong());
			emailHistoryConfig.setMailFrom(emailTemplate.getMailFrom());
			emailHistoryConfig.setMailTo(emailTemplate.getMailTo());
			emailHistoryConfig.setMailCC(emailTemplate.getMailCC());
			emailHistoryConfig.setMailBCC(emailTemplate.getMailBCC());
			emailHistoryConfig.setMailBody(sub.replace(emailTemplate.getMailBody()));
			emailHistoryConfig.setSubject(sub.replace(emailTemplate.getSubject()));
			emailHistoryConfig.setStatus(WorkflowTaskEnum.EmailStatus.IN_PROGRESS.toString());
			emailHistoryConfig.setMailId(emailTemplate.getId());
			workflowDAL.updateEmailExecutionHistory(emailHistoryConfig);
			return collectInfoFromDataBase(incomingTaskMessage, emailHistoryConfig);
		} catch (Exception e) {
			log.error("Workflow Detail ==== ReportEmailSubscriber Incorrect email format found ===== ", e);
			throw new InsightsJobFailedException("Error while updating values to Email History ");
		}

	}

	/**
	 * Method to create RecipientAddress
	 * 
	 * @param recipientList
	 * @return List<InternetAddress>
	 */
	private List<InternetAddress> createRecipientAddress(String[] recipientList) {
		List<InternetAddress> recipientAddress = new ArrayList<>();
		for (String recipient : recipientList) {
			try {
				recipientAddress.add(new InternetAddress(recipient.trim()));
			} catch (AddressException e) {
				log.error("Workflow Detail ==== ReportEmailSubscriber Incorrect email format found ===== ", e);
				throw new InsightsJobFailedException("Incorrect email format found!");
			}
		}
		return recipientAddress;
	}
}
