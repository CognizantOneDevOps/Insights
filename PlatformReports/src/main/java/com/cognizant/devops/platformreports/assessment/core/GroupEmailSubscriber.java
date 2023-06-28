/*******************************************************************************
 * Copyright 2022 Cognizant Technology Solutions
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.jms.JMSException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.constants.StringExpressionConstants;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformcommons.exception.InsightsJobFailedException;
import com.cognizant.devops.platformdal.assessmentreport.InsightsEmailTemplates;
import com.cognizant.devops.platformdal.assessmentreport.InsightsReportVisualizationContainer;
import com.cognizant.devops.platformdal.groupemail.GroupEmailConfigDAL;
import com.cognizant.devops.platformdal.groupemail.InsightsGroupEmailConfiguration;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformworkflow.workflowtask.core.InsightsStatusProvider;
import com.cognizant.devops.platformworkflow.workflowtask.email.EmailProcesser;
import com.cognizant.devops.platformworkflow.workflowtask.email.MailReport;
import com.cognizant.devops.platformworkflow.workflowtask.message.factory.WorkflowTaskSubscriberHandler;
import com.cognizant.devops.platformworkflow.workflowthread.core.WorkflowThreadPool;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author 911215
 *
 */

public class GroupEmailSubscriber extends WorkflowTaskSubscriberHandler {

	private static Logger log = LogManager.getLogger(GroupEmailSubscriber.class.getName());
	private WorkflowDAL workflowDAL = new WorkflowDAL();
	private GroupEmailConfigDAL groupEmailConfigDAL = new GroupEmailConfigDAL();

	private long executionId;
	private String workflowId;
	MailReport mailReportDTO = new MailReport();

	public GroupEmailSubscriber(String routingKey) throws IOException, InsightsCustomException, TimeoutException, InterruptedException, JMSException {
		super(routingKey);
	}

	@Override
	public void handleTaskExecution(String message) throws IOException {
//		String message = new String(body, MQMessageConstants.MESSAGE_ENCODING);
		JsonObject statusObject = null;
		JsonObject incomingTaskMessage = JsonUtils.parseStringAsJsonObject(message);
		try {
			long startTime = System.nanoTime();
			workflowId = incomingTaskMessage.get(AssessmentReportAndWorkflowConstants.WORKFLOW_ID).getAsString();
			executionId = incomingTaskMessage.get(AssessmentReportAndWorkflowConstants.EXECUTIONID).getAsLong();
			InsightsGroupEmailConfiguration groupEmailConfig = groupEmailConfigDAL
					.getConfigByGroupEmailWorkflowId(workflowId);
			String source = groupEmailConfig.getSource();
			String reportsString = groupEmailConfig.getMapIdList();
			JsonArray reportsJsonArray = JsonUtils.parseStringAsJsonArray(reportsString);
			prepareMailReport(source, reportsJsonArray);
			List<JsonObject> failedJobs = new ArrayList<>();
			List<JsonObject> successJobs = new ArrayList<>();
			List<Callable<JsonObject>> emailListToExecute = new ArrayList<>();
			EmailProcesser emailProcesser = new EmailProcesser(mailReportDTO);
			emailListToExecute.add(emailProcesser);
			/* segregate entire email execution list into defined chunks */
			List<List<Callable<JsonObject>>> emailChunkList = WorkflowThreadPool.getChunk(emailListToExecute, 1);
			/* submit each chunk to Thread pool in a loop */
			executeEmailChunks(emailChunkList, failedJobs, successJobs);
			/* Updating the Email History when the email is sent successfully */
			if (!successJobs.isEmpty()) {
				updateEmailHistoryWithStatus(
						incomingTaskMessage.get(AssessmentReportAndWorkflowConstants.EXECUTIONID).getAsLong(),
						WorkflowTaskEnum.EmailStatus.COMPLETED.name());
				InsightsStatusProvider.getInstance().createInsightStatusNode("GroupEmailSubscriberEmail Completed ",
						PlatformServiceConstants.SUCCESS);
			}
			if (!failedJobs.isEmpty()) {
				statusObject = updateFailedTaskStatusLog(failedJobs);
				throw new InsightsJobFailedException("Unable to send Group email");
			}
			long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
			mailReportDTO.getMailAttachments()
					.forEach((attachmentName, file) -> log.debug(StringExpressionConstants.STR_EXP_EMAIL_EXECUTION,
							executionId, workflowId, "-", mailReportDTO.getMailTo(), mailReportDTO.getMailFrom(),
							processingTime, " AttachmentName: " + attachmentName));

		} catch (InsightsJobFailedException e) {
			log.error("Workflow Detail ==== GroupEmailSubscriber: Email Send failed to execute due to Exception ===== ", e);
			handleAttachmentErrors(e, executionId, statusObject);
			throw new InsightsJobFailedException("Failed to send email in GroupEmailSubscriberEmail");
		} catch (Exception e) {
			log.error("Workflow Detail ==== GroupEmailSubscriber: Email Send failed to execute due to Exception ===== ", e);
			handleAttachmentErrors(e, executionId, statusObject);
		}

	}

	/**
	 * Method to prepare the Mail Report
	 * 
	 * @param source
	 * @param reportsJsonArray
	 */

	private void prepareMailReport(String source, JsonArray reportsJsonArray) throws InsightsCustomException {
		List<String> reportIds = new ArrayList<>();
		List<InternetAddress> recipientCCAddress = null;
		List<InternetAddress> recipientBCCAddress = null;
		List<InternetAddress> recipientAddress = null;
		try {
			InsightsEmailTemplates emailTemplate = workflowDAL.getEmailTemplateByWorkflowId(workflowId);
			if (emailTemplate != null) {
				reportsJsonArray.forEach(item -> reportIds.add(item.getAsJsonObject().get("id").getAsString()));
				if (emailTemplate.getMailTo() != null) {
					String[] recipientList = emailTemplate.getMailTo().split(",");
					recipientAddress = createRecipientAddress(recipientList);
				}
				if (emailTemplate.getMailCC() != null) {
					String[] recipientCCList = emailTemplate.getMailCC().split(",");
					recipientCCAddress = createRecipientAddress(recipientCCList);
				}
				if (emailTemplate.getMailBCC() != null) {
					String[] recipientBCCList = emailTemplate.getMailBCC().split(",");
					recipientBCCAddress = createRecipientAddress(recipientBCCList);
				}
				mailReportDTO.setTimeOfReportGeneration(InsightsUtils.insightsTimeXFormat(this.executionId));
				mailReportDTO.setMailTo(recipientAddress);
				mailReportDTO.setMailCC(recipientCCAddress);
				mailReportDTO.setMailBCC(recipientBCCAddress);
				mailReportDTO.setMailFrom(emailTemplate.getMailFrom());
				mailReportDTO.setSubject(emailTemplate.getSubject());
				mailReportDTO.setMailBody(emailTemplate.getMailBody());
				mailReportDTO.setMailAttachments(getAttachmentsForMailReport(reportIds, source));

			} else {
				log.error("Workflow Detail ==== GroupEmailSubscriberEmail Email Template Not Found ===== ");
				throw new InsightsCustomException("Email Template Not Found");
			}
		} catch (Exception e) {
			log.error("Workflow Detail ==== GroupEmailSubscriberEmail Error While Preparing MailReport ===== ", e);
			throw new InsightsCustomException("Error While Preparing MailReport -> " + e.getMessage());
		}
	}

	/**
	 * Method to get the attachment PDFs
	 * 
	 * @param reportIds
	 * @param source
	 */
	public Map<String, byte[]> getAttachmentsForMailReport(List<String> reportIds, String source) {
		Map<String, byte[]> attachments = new HashMap<>();
		try {
			String reportsIdString = String.join(",", reportIds);
			/* Fetching the latest attachment for that report */
			List<Object[]> latestVisualizationConfigs = workflowDAL.getAttachmentDataForReportIds(reportsIdString,
					source);
			if (latestVisualizationConfigs.isEmpty()) {
				throw new InsightsCustomException("Error while fetching Latest Reports");
			}
			latestVisualizationConfigs.stream()
					.forEach(config -> attachments.put((String) config[0], (byte[]) config[1]));
			return attachments;
		} catch (Exception e) {
			log.error("Workflow Detail ==== GroupEmailSubscriberEmail Error while Fetching Attachments ===== ", e);
			throw new InsightsJobFailedException("Error while Fetching Attachments");
		}
	}

	/**
	 * Method to Execute Email Chunks
	 * 
	 * @param chunkList
	 * @param failedJobs
	 * @param successJobs
	 */
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
				log.error("Workflow Detail ==== GroupEmailSubscriberEmail Incorrect email format found ===== ", e);
				throw new InsightsJobFailedException("Incorrect email format found!");
			}
		}
		return recipientAddress;
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
		statusObject.addProperty(AssessmentReportAndWorkflowConstants.EXECUTIONID, executionId);
		statusObject.addProperty(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
		statusObject.add("log", logArray);
		// statusLog set here which is class variable of WorkflowTaskSubscriberHandler
		log.error("Workflow Detail ==== unable to send an email statusLog {}  ", statusLog);
		mailReportDTO.getMailAttachments()
				.forEach((attachmentName, file) -> log.error(StringExpressionConstants.STR_EXP_EMAIL_EXECUTION,
						executionId, workflowId, "-", mailReportDTO.getMailTo(), mailReportDTO.getMailFrom(), 0,
						"AttachmentName :" + attachmentName + " statuslog :" + statusLog + "Unable to send an email"));
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
		if (emailHistoryConfig != null) {
			emailHistoryConfig.setStatus(status);
			emailHistoryConfig.setExecutionTime(InsightsUtils.getCurrentTimeInEpochMilliSeconds());
			workflowDAL.updateEmailExecutionHistory(emailHistoryConfig);
		}
	}

	/**
	 * Method to handle attachment errors
	 * 
	 * @param exception
	 * @param additionalDetails
	 */
	private void handleAttachmentErrors(Exception e, long executionId, JsonObject statusObject) {
		updateEmailHistoryWithStatus(executionId, WorkflowTaskEnum.EmailStatus.ERROR.name());
		InsightsStatusProvider.getInstance().createInsightStatusNode(
				"GroupEmailSubscriberEmail Completed with error " + e.getMessage(), PlatformServiceConstants.FAILURE);
		/* Adding the workflow status log */
		if (statusObject != null) {
			setStatusLog(new Gson().toJson(statusObject));
		} else {
			setStatusLog(e.getMessage());
		}
		mailReportDTO.getMailAttachments()
				.forEach((attachmentName, file) -> log.error(StringExpressionConstants.STR_EXP_EMAIL_EXECUTION,
						executionId, workflowId, "-", mailReportDTO.getMailTo(), mailReportDTO.getMailFrom(), 0,
						" AttachmentName :" + attachmentName + " - "
								+ "Failed to send email in GroupEmailSubscriberEmail " + e.getMessage()));
	}

}
