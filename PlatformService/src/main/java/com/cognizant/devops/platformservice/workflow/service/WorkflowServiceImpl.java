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
package com.cognizant.devops.platformservice.workflow.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.EmailConfiguration;
import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.assessmentreport.InsightsAssessmentConfiguration;
import com.cognizant.devops.platformdal.assessmentreport.InsightsEmailTemplates;
import com.cognizant.devops.platformdal.assessmentreport.InsightsReportVisualizationContainer;
import com.cognizant.devops.platformdal.assessmentreport.ReportConfigDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTask;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTaskSequence;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowType;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service("workflowService")
public class WorkflowServiceImpl {
	private static final Logger log = LogManager.getLogger(WorkflowServiceImpl.class);
	WorkflowDAL workflowConfigDAL = new WorkflowDAL();
	ReportConfigDAL reportConfigDAL = new ReportConfigDAL();
	JsonParser parser = new JsonParser();
	String healthNotificationWorkflowId = WorkflowTaskEnum.WorkflowType.SYSTEM.getValue() + "_" + "HealthNotification";

	/**
	 * Adding tasks to the workflow task table *
	 * 
	 * @param workflowTaskJson
	 * @return
	 * @throws InsightsCustomException
	 */
	public int saveWorkflowTask(JsonObject workflowTaskJson) throws InsightsCustomException {
		int id = -1;
		try {
			String description = workflowTaskJson.get("description").getAsString();
			String mqChannel = workflowTaskJson.get("mqChannel").getAsString();
			String componentName = workflowTaskJson.get("componentName").getAsString();
			int dependency = workflowTaskJson.get("dependency").getAsInt();
			String workflowType = workflowTaskJson.get("workflowType").getAsString();

			InsightsWorkflowTask taskConfig = new InsightsWorkflowTask();

			// Entity Setters
			taskConfig.setDescription(description);
			taskConfig.setMqChannel(mqChannel);
			taskConfig.setCompnentName(componentName);
			taskConfig.setDependency(dependency);
			InsightsWorkflowType workflowTypeEntity = new InsightsWorkflowType();
			workflowTypeEntity.setWorkflowType(workflowType);
			taskConfig.setWorkflowType(workflowTypeEntity);
			id = workflowConfigDAL.saveInsightsWorkflowTaskConfig(taskConfig);
		} catch (NullPointerException e) {
			log.error(e);
			throw new InsightsCustomException(" assessment task does not have some mandatory field ");
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
		return id;
	}
	
	/**
	 * Adding tasks to the workflow task table *
	 * 
	 * @param workflowTaskJson
	 * @return
	 * @throws InsightsCustomException
	 */
	public int populateAndSaveWorkflowTask(String description,String mqChannel,String componentName,int dependency,String workflowType) throws InsightsCustomException {
		int id = -1;
		try {
			InsightsWorkflowTask taskConfig = new InsightsWorkflowTask();
			taskConfig.setDescription(description);
			taskConfig.setMqChannel(mqChannel);
			taskConfig.setCompnentName(componentName);
			taskConfig.setDependency(dependency);
			InsightsWorkflowType workflowTypeEntity = new InsightsWorkflowType();
			workflowTypeEntity.setWorkflowType(workflowType);
			taskConfig.setWorkflowType(workflowTypeEntity);
			id = workflowConfigDAL.saveInsightsWorkflowTaskConfig(taskConfig);
		} catch (NullPointerException e) {
			log.error(e);
			throw new InsightsCustomException(" assessment task does not have some mandatory field ");
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
		return id;
	}

	/**
	 * @param workflowId
	 * @param isActive
	 * @param reoccurence
	 * @param schedule
	 * @param reportStatus
	 * @param workflowType
	 * @param taskList
	 * @param startdate
	 * @param enddate
	 * @return InsightsWorkflowConfiguration
	 * @throws InsightsCustomException
	 */
	public InsightsWorkflowConfiguration saveWorkflowConfig(String workflowId, boolean isActive, boolean reoccurence,
			String schedule, String reportStatus, String workflowType, JsonArray taskList, long startdate,
			JsonObject emailDetails, boolean runImmediate) throws InsightsCustomException {
		InsightsWorkflowConfiguration workflowConfig = workflowConfigDAL.getWorkflowByWorkflowId(workflowId);
		if (workflowConfig != null) {
			throw new InsightsCustomException("Workflow already exists for with assessment report id "
					+ workflowConfig.getAssessmentConfig().getId());
		}

		workflowConfig = new InsightsWorkflowConfiguration();
		workflowConfig.setWorkflowId(workflowId);
		workflowConfig.setActive(isActive);
		if (schedule.equals(WorkflowTaskEnum.WorkflowSchedule.ONETIME.toString())) {
			workflowConfig.setNextRun(0L);
		} else if (schedule.equals(WorkflowTaskEnum.WorkflowSchedule.BI_WEEKLY_SPRINT.toString())
				|| schedule.equals(WorkflowTaskEnum.WorkflowSchedule.TRI_WEEKLY_SPRINT.toString())) {
			workflowConfig.setNextRun(InsightsUtils.getNextRunTime(startdate, schedule, true));
		} else {
			workflowConfig
					.setNextRun(InsightsUtils.getNextRunTime(InsightsUtils.getCurrentTimeInSeconds(), schedule, true));
		}
		workflowConfig.setLastRun(0L);
		workflowConfig.setReoccurence(reoccurence);
		workflowConfig.setScheduleType(schedule);
		workflowConfig.setStatus(reportStatus);
		workflowConfig.setWorkflowType(workflowType);
		workflowConfig.setRunImmediate(runImmediate);
		Set<InsightsWorkflowTaskSequence> sequneceEntitySet = setSequence(taskList, workflowConfig);
		// Attach TaskSequence to workflow
		workflowConfig.setTaskSequenceEntity(sequneceEntitySet);
		if (emailDetails != null) {
			InsightsEmailTemplates emailTemplateConfig = createEmailTemplateObject(emailDetails, workflowConfig);
			workflowConfig.setEmailConfig(emailTemplateConfig);
		}
		return workflowConfig;

	}

	/**
	 * Fetching get the task lists from the table
	 * 
	 * @param workflowType
	 * @return
	 * @throws InsightsCustomException
	 */
	public JsonArray getTaskList(String workflowType) throws InsightsCustomException {
		try {

			List<InsightsWorkflowTask> listofTasks = workflowConfigDAL.getTaskLists(workflowType);
			JsonArray jsonarray = new JsonArray();
			for (InsightsWorkflowTask taskDetail : listofTasks) {
				JsonObject jsonobject = new JsonObject();
				jsonobject.addProperty(AssessmentReportAndWorkflowConstants.TASK_ID, taskDetail.getTaskId());
				jsonobject.addProperty("description", taskDetail.getDescription());
				jsonobject.addProperty("dependency", taskDetail.getDependency());
				jsonobject.addProperty("componentName", taskDetail.getCompnentName());
				jsonarray.add(jsonobject);
			}
			return jsonarray;

		} catch (Exception e) {
			log.error("Error while deleting assesment report", e);
			throw new InsightsCustomException(e.toString());
		}
	}

	/**
	 * Add sequence to the tasks that have been assigned
	 * 
	 * @param taskList
	 * @param workflowConfig
	 * @return
	 * @throws InsightsCustomException
	 */
	public Set<InsightsWorkflowTaskSequence> setSequence(JsonArray taskList,
			InsightsWorkflowConfiguration workflowConfig) throws InsightsCustomException {
		Set<InsightsWorkflowTaskSequence> sequneceEntitySet = new HashSet<>();
		try {
			Set<InsightsWorkflowTaskSequence> taskSequenceSet = workflowConfig.getTaskSequenceEntity();
			if (!taskSequenceSet.isEmpty()) {
				workflowConfigDAL.deleteWorkflowTaskSequence(workflowConfig.getWorkflowId());
			}
			ArrayList<Integer> sortedTask = new ArrayList<>();
			taskList.forEach(taskObj -> sortedTask.add(taskObj.getAsJsonObject().get(AssessmentReportAndWorkflowConstants.TASK_ID).getAsInt()));
			@SuppressWarnings("unchecked")

			/*
			 * make a clone of list as sortedTask list will be iterated so same list can not
			 * used to get next element
			 */
			ArrayList<Integer> taskListClone = (ArrayList<Integer>) sortedTask.clone();

			int sequenceNo = 1;
			int nextTask = -1;

			ListIterator<Integer> listIterator = sortedTask.listIterator();
			while (listIterator.hasNext()) {

				int taskId = listIterator.next();
				int nextIndex = listIterator.nextIndex();
				if (nextIndex == taskListClone.size()) {
					nextTask = -1;
				} else {
					nextTask = taskListClone.get(nextIndex);
				}
				InsightsWorkflowTask taskEntity = workflowConfigDAL.getTaskByTaskId(taskId);
				InsightsWorkflowTaskSequence taskSequenceEntity = new InsightsWorkflowTaskSequence();
				// Attach each task to sequence
				taskSequenceEntity.setWorkflowTaskEntity(taskEntity);
				taskSequenceEntity.setWorkflowConfig(workflowConfig);
				taskSequenceEntity.setSequence(sequenceNo);
				taskSequenceEntity.setNextTask(nextTask);
				sequneceEntitySet.add(taskSequenceEntity);
				sequenceNo++;

			}

			return sequneceEntitySet;
		} catch (Exception e) {
			throw new InsightsCustomException("Something went wrong while attaching task to workflow");
		}
	}

	/**
	 * Method used to get Workflow Execution History records
	 * 
	 * @param configIdJson
	 * @return
	 * @throws InsightsCustomException
	 */
	public JsonObject getWorkFlowExecutionRecords(JsonObject configIdJson) throws InsightsCustomException {
		try {
			int assessmentConfigId = configIdJson.get("configid").getAsInt();
			List<Object[]> records = workflowConfigDAL
					.getWorkflowExecutionRecordsbyAssessmentConfigID(assessmentConfigId);
			JsonArray recordData = new JsonArray();
			records.stream().forEach(record -> {
				JsonObject rec = new JsonObject();
				rec.addProperty("executionid", (long) record[0]);
				rec.addProperty("startTime", (long) record[1]);
				if (((long) record[2]) == 0) {
					rec.addProperty(AssessmentReportAndWorkflowConstants.ENDTIME, 0);
				} else {
					rec.addProperty(AssessmentReportAndWorkflowConstants.ENDTIME, (long) record[2]);
				}
				rec.addProperty("retryCount", (int) record[3]);
				rec.addProperty("statusLog", String.valueOf(record[4]));
				rec.addProperty("taskStatus", String.valueOf(record[5]));
				rec.addProperty("currentTask", String.valueOf(record[6]));
				recordData.add(rec);
			});
			JsonObject responseJson = new JsonObject();
			responseJson.add("records", recordData);
			return responseJson;
		} catch (Exception e) {
			log.error("Error while fetching Workflow Execution History records.", e);
			throw new InsightsCustomException("Error while fetching Workflow Execution History records");
		}
	}

	/**
	 * Method used to get Workflow Execution History records by Workflow Id
	 * 
	 * @param configIdJson
	 * @return
	 * @throws InsightsCustomException
	 */
	public JsonObject getWorkFlowExecutionRecordsByWorkflowId(JsonObject workflowIdJson)
			throws InsightsCustomException {
		try {
			String workflowId = workflowIdJson.get(AssessmentReportAndWorkflowConstants.WORKFLOW_ID).getAsString();
			List<Object[]> records = workflowConfigDAL.getWorkflowExecutionRecordsByworkflowID(workflowId);
			JsonArray recordData = new JsonArray();
			records.stream().forEach(record -> {
				JsonObject rec = new JsonObject();
				rec.addProperty("executionid", (long) record[0]);
				rec.addProperty("startTime", (long) record[1]);
				if (((long) record[2]) == 0) {
					rec.addProperty(AssessmentReportAndWorkflowConstants.ENDTIME, 0);
				} else {
					rec.addProperty(AssessmentReportAndWorkflowConstants.ENDTIME, (long) record[2]);
				}
				rec.addProperty("retryCount", (int) record[3]);
				rec.addProperty("statusLog", String.valueOf(record[4]));
				rec.addProperty("taskStatus", String.valueOf(record[5]));
				rec.addProperty("currentTask", String.valueOf(record[6]));
				recordData.add(rec);
			});
			JsonObject responseJson = new JsonObject();
			responseJson.add("records", recordData);
			return responseJson;
		} catch (Exception e) {
			log.error("Error while fetching Workflow Execution History records.", e);
			throw new InsightsCustomException("Error while fetching Workflow Execution History records");
		}
	}

	/**
	 * Method to create Email Template Object
	 * 
	 * @param emailDetails
	 * @param workflowConfig
	 * @return InsightsEmailTemplates
	 */
	public InsightsEmailTemplates createEmailTemplateObject(JsonObject emailDetails,
			InsightsWorkflowConfiguration workflowConfig) {
		InsightsEmailTemplates emailTemplateConfig = workflowConfig.getEmailConfig();
		if (emailTemplateConfig == null) {
			emailTemplateConfig = new InsightsEmailTemplates();
		}
		String mailBody = emailDetails.get("mailBodyTemplate").getAsString();
		mailBody = mailBody.replace("#", "<").replace("~", ">");
		emailTemplateConfig.setMailFrom(emailDetails.get("senderEmailAddress").getAsString());
		if (!emailDetails.get(AssessmentReportAndWorkflowConstants.RECEIVEREMAILADDRESS).getAsString().isEmpty()) {
			emailTemplateConfig.setMailTo(emailDetails.get(AssessmentReportAndWorkflowConstants.RECEIVEREMAILADDRESS).getAsString());
		} else {
			emailTemplateConfig.setMailTo(null);
		}
		if (!emailDetails.get(AssessmentReportAndWorkflowConstants.RECEIVERCCEMAILADDRESS).getAsString().isEmpty()) {
			emailTemplateConfig.setMailCC(emailDetails.get(AssessmentReportAndWorkflowConstants.RECEIVERCCEMAILADDRESS).getAsString());
		} else {
			emailTemplateConfig.setMailCC(null);
		}
		if (!emailDetails.get(AssessmentReportAndWorkflowConstants.RECEIVERBCCEMAILADDRESS).getAsString().isEmpty()) {
			emailTemplateConfig.setMailBCC(emailDetails.get(AssessmentReportAndWorkflowConstants.RECEIVERBCCEMAILADDRESS).getAsString());
		} else {
			emailTemplateConfig.setMailBCC(null);
		}
		emailTemplateConfig.setSubject(emailDetails.get("mailSubject").getAsString());
		emailTemplateConfig.setMailBody(mailBody);
		emailTemplateConfig.setWorkflowConfig(workflowConfig);
		return emailTemplateConfig;
	}

	/**
	 * Method to get latest Execution Ids from WorkflowHistory and Report
	 * Visualization
	 * 
	 * @param configIdJson
	 * @return JsonObject
	 * @throws InsightsCustomException
	 */
	public JsonObject getMaximumExecutionIDs(JsonObject configIdJson) throws InsightsCustomException {
		try {
			long workflowExecutionId = -1;
			long reportVisualizerExecutionId = -1;
			boolean status = false;
			long executionId = -1;
			boolean alreadySet = false;
			int assessmentConfigId = configIdJson.get("configid").getAsInt();
			InsightsAssessmentConfiguration assessmentObj = reportConfigDAL.getAssessmentByConfigId(assessmentConfigId);
			String workflowId = assessmentObj.getWorkflowConfig().getWorkflowId();
			List<Object[]> result = workflowConfigDAL
					.getMaxExecutionIDsFromWorkflowExecutionAndReportVisualization(workflowId);
			for (Object[] record : result) {
				if (record[0] == null && record[1] == null) {
					executionId = -1;
					alreadySet = true;
				} else {
					reportVisualizerExecutionId = (long) record[0];
					workflowExecutionId = (long) record[1];
				}
			}
			if (!alreadySet) {
				if (workflowExecutionId == reportVisualizerExecutionId) {
					status = true;
					executionId = reportVisualizerExecutionId;
				} else {
					executionId = reportVisualizerExecutionId;
				}
			}
			JsonObject responseJson = new JsonObject();
			responseJson.addProperty("status", status);
			responseJson.addProperty("executionId", executionId);
			responseJson.addProperty(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			return responseJson;
		} catch (Exception e) {
			log.error("Error while fetching execution ids", e);
			throw new InsightsCustomException("Error while fetching execution ids");
		}
	}

	/**
	 * Method to get Report PDF
	 * 
	 * @param pdfDetailsJson
	 * @return byte[]
	 * @throws InsightsCustomException
	 */
	public byte[] getReportPDF(JsonObject pdfDetailsJson) throws InsightsCustomException {
		byte[] pdfContent = null;
		try {
			String workflowId = pdfDetailsJson.get(AssessmentReportAndWorkflowConstants.WORKFLOW_ID).getAsString();
			long executionId = pdfDetailsJson.get("executionId").getAsLong();
			InsightsReportVisualizationContainer reportVisObject = workflowConfigDAL
					.getReportVisualizationContainerByWorkflowAndExecutionId(workflowId, executionId);
			if (reportVisObject != null) {
				pdfContent = reportVisObject.getAttachmentData();
			} else {
				throw new InsightsCustomException("PDF not generated");
			}
		} catch (Exception e) {
			log.error("Error while updating report status.", e);
			throw new InsightsCustomException(e.toString());
		}
		return pdfContent;
	}

	/**
	 * Method to update Health Notification
	 * 
	 * @param statusJson
	 * @return String
	 * @throws InsightsCustomException
	 */
	public String updateHealthNotification(JsonObject statusJson) throws InsightsCustomException {
		try {
			int systemTask = 0;
			int emailTask = 0;
			String schedule = WorkflowTaskEnum.WorkflowSchedule.DAILY.toString();
			boolean reoccurence = true;
			boolean runImmediate = true;
			String reportStatus = WorkflowTaskEnum.WorkflowStatus.NOT_STARTED.toString();
			String workflowType = WorkflowTaskEnum.WorkflowType.SYSTEM.toString();
			Long epochStartDate = 0L;
			boolean isActive = statusJson.get("status").getAsBoolean();
			InsightsWorkflowConfiguration workflowConfig = workflowConfigDAL
					.getWorkflowConfigByWorkflowId(healthNotificationWorkflowId);
			if (workflowConfig == null) {
				InsightsWorkflowType workflowTypeObj = workflowConfigDAL
						.getWorkflowType(WorkflowTaskEnum.WorkflowType.SYSTEM.getValue());
				if (workflowTypeObj == null) {
					InsightsWorkflowType type = new InsightsWorkflowType();
					type.setWorkflowType(WorkflowTaskEnum.WorkflowType.SYSTEM.getValue());
					workflowConfigDAL.saveWorkflowType(type);
				}
				InsightsWorkflowTask systemNotificationTask = workflowConfigDAL
						.getTaskByChannel("WORKFLOW.SYSTEM_TASK.SYSTEMNOTIFICATION.EXECUTION");
				if (systemNotificationTask == null) {
					String description= "SystemNotification_Execute";
					String mqChannel="WORKFLOW.SYSTEM_TASK.SYSTEMNOTIFICATION.EXECUTION";
					String componentName="com.cognizant.devops.platformreports.assessment.core.SystemNotificationDetailSubscriber";
					int dependency=100;
					systemTask = populateAndSaveWorkflowTask(description,mqChannel,componentName,dependency,workflowType);
				} else {
					systemTask = systemNotificationTask.getTaskId();
				}
				InsightsWorkflowTask emailNotificationTask = workflowConfigDAL
						.getTaskByChannel("WORKFLOW.SYSTEM_TASK.EMAIL.EXCECUTION");
				if (emailNotificationTask == null) {
					String description= "Email_Execute";
					String mqChannel="WORKFLOW.SYSTEM_TASK.EMAIL.EXCECUTION";
					String componentName="com.cognizant.devops.platformreports.assessment.core.ReportEmailSubscriber";
					int dependency=101;
					emailTask = populateAndSaveWorkflowTask(description,mqChannel,componentName,dependency,workflowType);
				} else {
					emailTask = emailNotificationTask.getTaskId();
				}
				JsonArray taskList = new JsonArray();
				taskList.add(createTaskJson(systemTask, 0));
				taskList.add(createTaskJson(emailTask, 1));
				JsonObject emailDetails = getEmailDetails();
				InsightsWorkflowConfiguration saveWorkflowConfig = saveWorkflowConfig(healthNotificationWorkflowId,
						isActive, reoccurence, schedule, reportStatus, workflowType, taskList, epochStartDate,
						emailDetails, runImmediate);
				workflowConfigDAL.saveInsightsWorkflowConfig(saveWorkflowConfig);
			} else {
				workflowConfigDAL.updateWorkflowConfigActive(healthNotificationWorkflowId, isActive);
			}
		} catch (Exception e) {
			log.error("Error while setting System Notification.", e);
			throw new InsightsCustomException(e.toString());
		}
		return PlatformServiceConstants.SUCCESS;
	}

	/**
	 * Method to get Health Notification status
	 * 
	 * @return JsonObject
	 * @throws InsightsCustomException
	 */
	public JsonObject getHealthNotificationStatus() throws InsightsCustomException {
		JsonObject response = new JsonObject();
		try {
			InsightsWorkflowConfiguration workflowConfig = workflowConfigDAL
					.getWorkflowByWorkflowId(healthNotificationWorkflowId);
			if (workflowConfig != null) {
				response.addProperty(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowConfig.getWorkflowId());
				response.addProperty("isActive", workflowConfig.isActive());
			}
		} catch (Exception e) {
			log.error("Error while setting System Notification.", e);
			throw new InsightsCustomException(e.toString());
		}
		return response;
	}

	/**
	 * Method to convert String to Json
	 * 
	 * @param convertregisterkpi
	 * @return JsonObject
	 */
	public JsonObject convertStringIntoJson(String convertregisterkpi) {
		JsonObject objectJson = new JsonObject();
		objectJson = parser.parse(convertregisterkpi).getAsJsonObject();
		return objectJson;
	}

	/**
	 * Method to create Task Json object
	 * 
	 * @param taskId
	 * @param sequence
	 * @return JsonObject
	 */
	public JsonObject createTaskJson(int taskId, int sequence) {
		JsonObject taskJson = new JsonObject();
		taskJson.addProperty(AssessmentReportAndWorkflowConstants.TASK_ID, taskId);
		taskJson.addProperty("sequence", sequence);
		return taskJson;
	}

	/**
	 * Method to get Email Details from server-config
	 * 
	 * @return JsonObject
	 */
	public JsonObject getEmailDetails() {
		EmailConfiguration emailConfig = ApplicationConfigProvider.getInstance().getEmailConfiguration();
		JsonObject emailDetailsJson = new JsonObject();
		emailDetailsJson.addProperty("senderEmailAddress", emailConfig.getMailFrom());
		emailDetailsJson.addProperty("receiverEmailAddress", emailConfig.getSystemNotificationSubscriber());
		emailDetailsJson.addProperty("mailSubject", emailConfig.getSubject());
		emailDetailsJson.addProperty("mailBodyTemplate", "");
		emailDetailsJson.addProperty("receiverCCEmailAddress", "");
		emailDetailsJson.addProperty("receiverBCCEmailAddress", "");
		return emailDetailsJson;
	}

	public JsonObject getLatestExecutionId(String workflowId) throws InsightsCustomException{
		try {
			long workflowExecutionId = -1;
			long reportVisualizerExecutionId = -1;
			boolean status = false;
			long executionId = -1;
			boolean alreadySet = false;
			List<Object[]> result = workflowConfigDAL
					.getMaxExecutionIDsFromWorkflowExecutionAndReportVisualization(workflowId);
			for (Object[] record : result) {
				if (record[0] == null && record[1] == null) {
					executionId = -1;
					alreadySet = true;
				} else {
					reportVisualizerExecutionId = (long) record[0];
					workflowExecutionId = (long) record[1];
				}
			}
			if (!alreadySet) {
				if (workflowExecutionId == reportVisualizerExecutionId) {
					status = true;
					executionId = reportVisualizerExecutionId;
				} else {
					executionId = reportVisualizerExecutionId;
				}
			}
			JsonObject responseJson = new JsonObject();
			responseJson.addProperty("status", status);
			responseJson.addProperty("executionId", executionId);
			responseJson.addProperty(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			return responseJson;
		} catch (Exception e) {
			log.error("Error while fetching execution ids", e);
			throw new InsightsCustomException("Error while fetching execution ids");
		}
	}

}
