/*******************************************************************************
 * Copyright 2023 Cognizant Technology Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformservice.offlinealerting.service;

import java.util.ArrayList;
import java.util.List;
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
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.assessmentreport.InsightsEmailTemplates;
import com.cognizant.devops.platformdal.offlineAlerting.InsightsOfflineAlerting;
import com.cognizant.devops.platformdal.offlineAlerting.InsightsOfflineAlertingDAL;

import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTaskSequence;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformservice.workflow.service.WorkflowServiceImpl;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Service("offlineAlertingService")
public class OfflineAlertingServiceImpl implements OfflineAlertingService {
	InsightsOfflineAlertingDAL offlineAlertingDAL = new InsightsOfflineAlertingDAL();
	WorkflowDAL workflowConfigDAL = new WorkflowDAL();
	private static final Logger log = LogManager.getLogger(OfflineAlertingServiceImpl.class);
	private static final String SCHEDULE_TYPE = "scheduleType";
	private static final String SCHEDULE_DATETIME = "scheduleDateTime";
	private static final String ALERT_NAME = "alertName";
	private static final String CYPHER_QUERY = "cypherQuery";
	private static final String FILTERS = "filters";
	private static final String TREND = "trend";
	private static final String THRESHOLD = "threshold";
	private static final String FREQUENCY = "frequency";
	private static final String EMAIL_DETAILS = "emailDetails";
	private static final String TIME_RANGE = "timeRangeText";
	private static final String FROM = "from";
	private static final String MAIL_BODY_TEMPLATE = "mailBodyTemplate";
	private static final String RECEIVER_CC_EMAIL_ADDRESS = "receiverCCEmailAddress";
	private static final String RECEIVER_BCC_EMAIL_ADDRESS = "receiverBCCEmailAddress";
	boolean runImmediate = Boolean.TRUE;
	boolean reoccurence = Boolean.FALSE;
	boolean isActive = Boolean.TRUE;
	public static final int MAX_RETRY_COUNT = 2;

	@Override
	public List<InsightsOfflineAlerting> getAllOfflineAlertingConfig() throws InsightsCustomException {

		List<InsightsOfflineAlerting> alertConfigList = new ArrayList<>();
		try {
			alertConfigList = offlineAlertingDAL.getAllOfflineAlertingConfig();
			return alertConfigList;
		} catch (Exception e) {
			log.error("Error while getting OfflineAlertList...", e);
			throw new InsightsCustomException(e.toString());
		}
	}

	@Override
	public void saveAlertConfig(JsonObject alertConfigJson) throws InsightsCustomException {

		try {
			String scheduleType = alertConfigJson.get(SCHEDULE_TYPE).getAsString();

			InsightsWorkflowConfiguration workflowConfig = saveWorkflowConfigDetails(alertConfigJson, scheduleType);

			InsightsOfflineAlerting offlineAlerting = new InsightsOfflineAlerting();
			offlineAlerting.setAlertJson(alertConfigJson.toString());
			offlineAlerting.setAlertName(alertConfigJson.get(ALERT_NAME).getAsString());
			offlineAlerting.setTrend(alertConfigJson.get(TREND).getAsString());
			offlineAlerting.setFrequency(alertConfigJson.get(FREQUENCY).getAsInt());
			offlineAlerting.setThreshold(alertConfigJson.get(THRESHOLD).getAsInt());
			offlineAlerting.setBreachedCount(0);
			offlineAlerting.setScheduleType(alertConfigJson.get(SCHEDULE_TYPE).getAsString());
			offlineAlerting.setWorkflowConfig(workflowConfig);
			offlineAlerting.setStatus("NotStarted");
			offlineAlerting.setRetryCount(0);
			offlineAlertingDAL.saveOfflineAlertingConfig(offlineAlerting);

		} catch (Exception e) {
			throw new InsightsCustomException(e.getMessage());
		}

	}

	public InsightsWorkflowConfiguration saveWorkflowConfigDetails(JsonObject alertConfigJson, String scheduleType)
			throws InsightsCustomException {
		try {
			String workflowId = WorkflowTaskEnum.WorkflowType.OFFLINE_ALERT.getValue() + "_"
					+ InsightsUtils.getCurrentTimeInSeconds();
			InsightsWorkflowConfiguration workflowConfig = workflowConfigDAL.getWorkflowByWorkflowId(workflowId);
			if (workflowConfig != null) {
				throw new InsightsCustomException("Workflow already exists for with assessment report id "
						+ workflowConfig.getAssessmentConfig().getId());
			}

			boolean emailEnabled = alertConfigJson.has(EMAIL_DETAILS);
			JsonObject emailDetails = null;
			if (emailEnabled) {
				emailDetails = alertConfigJson.get(EMAIL_DETAILS).getAsJsonObject();
			}

			String workflowType = WorkflowTaskEnum.WorkflowType.OFFLINE_ALERT.getValue();
			String workflowStatus = WorkflowTaskEnum.WorkflowStatus.NOT_STARTED.name();

			JsonArray taskList = new JsonArray();
			WorkflowServiceImpl workflowService = new WorkflowServiceImpl();
			JsonArray workflowList = workflowService.getTaskList(workflowType);
			log.debug("Alert task list from table==={}", workflowList);
			for (JsonElement task : workflowList) {
				JsonObject taskJson = task.getAsJsonObject();

				taskList.add(workflowService.createTaskJson(
						taskJson.get(AssessmentReportAndWorkflowConstants.TASK_ID).getAsInt(),
						taskJson.get("dependency").getAsInt()));

			}
			workflowConfig = new InsightsWorkflowConfiguration();
			workflowConfig.setWorkflowId(workflowId);
			workflowConfig.setActive(isActive);
			workflowConfig.setNextRun(InsightsUtils.getUTCTime(alertConfigJson.get(SCHEDULE_DATETIME).getAsLong()));
			workflowConfig.setLastRun(0L);
			workflowConfig.setReoccurence(reoccurence);
			workflowConfig.setScheduleType(scheduleType);
			workflowConfig.setStatus(workflowStatus);
			workflowConfig.setWorkflowType(workflowType);
			workflowConfig.setRunImmediate(runImmediate);
			Set<InsightsWorkflowTaskSequence> taskSequenceSet = workflowService.setSequence(taskList, workflowConfig);
			workflowConfig.setTaskSequenceEntity(taskSequenceSet);
			if (emailDetails != null) {
				InsightsEmailTemplates emailTemplateConfig = workflowService.createEmailTemplateObject(emailDetails,
						workflowConfig);
				workflowConfig.setEmailConfig(emailTemplateConfig);
			}
			return workflowConfig;
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}

	}

	private JsonObject getEmailDetails(JsonObject emailDetails) {
		EmailConfiguration emailConfig = ApplicationConfigProvider.getInstance().getEmailConfiguration();
		JsonObject emailDetailsJson = new JsonObject();
		emailDetailsJson.addProperty("senderEmailAddress", emailConfig.getMailFrom());
		emailDetailsJson.addProperty("receiverEmailAddress", emailDetails.get("receiverEmailAddress").getAsString());
		emailDetailsJson.addProperty("mailSubject", emailDetails.get("mailSubject").getAsString());
		emailDetailsJson.addProperty(MAIL_BODY_TEMPLATE,
				emailDetails.get(MAIL_BODY_TEMPLATE) == null
						? "*** Please do not reply to this mail. ****. \n*** System triggered email for dashboards ***"
						: emailDetails.get(MAIL_BODY_TEMPLATE).getAsString());
		emailDetailsJson.addProperty(RECEIVER_CC_EMAIL_ADDRESS, emailDetails.get(RECEIVER_CC_EMAIL_ADDRESS) == null ? ""
				: emailDetails.get(RECEIVER_CC_EMAIL_ADDRESS).getAsString());
		emailDetailsJson.addProperty(RECEIVER_BCC_EMAIL_ADDRESS,
				emailDetails.get(RECEIVER_BCC_EMAIL_ADDRESS) == null ? ""
						: emailDetails.get(RECEIVER_BCC_EMAIL_ADDRESS).getAsString());
		return emailDetailsJson;
	}

	@Override
	public boolean deleteOfflineAlert(JsonObject offlineAlertJson) throws InsightsCustomException {
		String alertName = "-1";
		boolean isRecordDeleted = Boolean.FALSE;
		try {
			alertName = offlineAlertJson.get(ALERT_NAME).getAsString();
			InsightsOfflineAlerting offlineAlertConfig = offlineAlertingDAL.getAlertConfigByAlertName(alertName);
			if (offlineAlertConfig != null) {
				String workflowId = offlineAlertConfig.getWorkflowConfig().getWorkflowId();
				workflowConfigDAL.deleteEmailTemplateByWorkflowId(workflowId);
				workflowConfigDAL.deleteEmailExecutionHistoryByWorkflowId(workflowId);
				workflowConfigDAL.deleteWorkflowTaskSequence(workflowId);
				workflowConfigDAL.deleteWorkflowExecutionHistoryRecordsByWorkflowId(workflowId);
				offlineAlertingDAL.deleteOfflineAlerting(offlineAlertConfig);
				isRecordDeleted = Boolean.TRUE;
			} else {
				throw new InsightsCustomException("Offline Alert not exists");
			}
		} catch (Exception e) {
			log.error("Error while deleting offline alert.", e);
			throw new InsightsCustomException(e.getMessage());
		}
		return isRecordDeleted;
	}

	@Override
	public void updateAlertConfig(JsonObject alertConfigJson) throws InsightsCustomException {

		try {
			String workflowType = WorkflowTaskEnum.WorkflowType.OFFLINE_ALERT.getValue();
			String workflowStatus = WorkflowTaskEnum.WorkflowStatus.NOT_STARTED.name();
			String scheduleType = alertConfigJson.get(SCHEDULE_TYPE).getAsString();

			InsightsOfflineAlerting offlineAlertConfig = offlineAlertingDAL
					.getAlertConfigByAlertName(alertConfigJson.get(ALERT_NAME).getAsString());
			InsightsWorkflowConfiguration workflowConfig = offlineAlertConfig.getWorkflowConfig();

			boolean emailEnabled = alertConfigJson.has(EMAIL_DETAILS);
			JsonObject emailDetails = null;
			JsonObject emailDetailsJson = null;
			if (emailEnabled) {
				emailDetails = alertConfigJson.get(EMAIL_DETAILS).getAsJsonObject();
				emailDetailsJson = getEmailDetails(emailDetails);
			}

			JsonArray taskList = new JsonArray();
			WorkflowServiceImpl workflowService = new WorkflowServiceImpl();
			JsonArray workflowList = workflowService.getTaskList(workflowType);
			log.debug("Alert task list from table===={}", workflowList);
			for (JsonElement task : workflowList) {
				JsonObject taskJson = task.getAsJsonObject();

				taskList.add(workflowService.createTaskJson(
						taskJson.get(AssessmentReportAndWorkflowConstants.TASK_ID).getAsInt(),
						taskJson.get("dependency").getAsInt()));

			}
			log.debug("Offline Alert task list====={}", taskList);

			Set<InsightsWorkflowTaskSequence> taskSequenceSet = workflowService.setSequence(taskList, workflowConfig);
			workflowConfig.setTaskSequenceEntity(taskSequenceSet);
			if (emailEnabled) {
				InsightsEmailTemplates emailTemplateConfig = workflowService.createEmailTemplateObject(emailDetailsJson,
						workflowConfig);
				workflowConfig.setEmailConfig(emailTemplateConfig);
			}

			workflowConfig.setActive(isActive);
			workflowConfig.setNextRun(offlineAlertConfig.getWorkflowConfig().getNextRun());
			workflowConfig.setLastRun(0L);
			workflowConfig.setReoccurence(reoccurence);
			workflowConfig.setScheduleType(scheduleType);
			workflowConfig.setStatus(workflowStatus);
			workflowConfig.setWorkflowType(workflowType);
			workflowConfig.setRunImmediate(runImmediate);

			JsonObject existingAlertJson = JsonUtils.parseStringAsJsonObject(offlineAlertConfig.getAlertJson());
			existingAlertJson.remove(CYPHER_QUERY);
			existingAlertJson.remove(TREND);
			existingAlertJson.remove(FREQUENCY);
			existingAlertJson.remove(THRESHOLD);
			existingAlertJson.remove(FILTERS);
			existingAlertJson.remove(EMAIL_DETAILS);
			existingAlertJson.remove(SCHEDULE_TYPE);
            existingAlertJson.remove(TIME_RANGE);
			existingAlertJson.remove(FROM);
			existingAlertJson.addProperty(CYPHER_QUERY, alertConfigJson.get(CYPHER_QUERY).getAsString());
			existingAlertJson.addProperty(TREND, alertConfigJson.get(TREND).getAsString());
			existingAlertJson.addProperty(FREQUENCY, alertConfigJson.get(FREQUENCY).getAsInt());
			existingAlertJson.addProperty(THRESHOLD, alertConfigJson.get(THRESHOLD).getAsInt());
			existingAlertJson.addProperty(FILTERS, alertConfigJson.get(FILTERS).getAsString());
			existingAlertJson.add(EMAIL_DETAILS, alertConfigJson.get(EMAIL_DETAILS).getAsJsonObject());
			existingAlertJson.addProperty(SCHEDULE_TYPE, alertConfigJson.get(SCHEDULE_TYPE).getAsString());
			existingAlertJson.addProperty(TIME_RANGE, alertConfigJson.get(TIME_RANGE).getAsString());
			existingAlertJson.addProperty(FROM, alertConfigJson.get(FROM).getAsString());
			
			offlineAlertConfig.setAlertJson(existingAlertJson.toString());
			offlineAlertConfig.setTrend(alertConfigJson.get(TREND).getAsString());
			offlineAlertConfig.setFrequency(alertConfigJson.get(FREQUENCY).getAsInt());
			offlineAlertConfig.setThreshold(alertConfigJson.get(THRESHOLD).getAsInt());
			offlineAlertConfig.setBreachedCount(0);
			offlineAlertConfig.setScheduleType(alertConfigJson.get(SCHEDULE_TYPE).getAsString());
			offlineAlertConfig.setWorkflowConfig(workflowConfig);
			offlineAlertConfig.setStatus("InProgress");
			if(offlineAlertConfig.getRetryCount() > MAX_RETRY_COUNT) {
				offlineAlertConfig.setRetryCount(0);
			}
			offlineAlertingDAL.updateOfflineAlertingConfig(offlineAlertConfig);
		} catch (Exception e) {
			throw new InsightsCustomException(e.getMessage());
		}

	}

	@Override
	public String updateAlertConfigStatus(JsonObject alertStatusJson) throws InsightsCustomException {
		try {
			boolean status = alertStatusJson.get("isActive").getAsBoolean();
			String alertName = alertStatusJson.get(ALERT_NAME).getAsString();
			InsightsOfflineAlerting alertConfig = offlineAlertingDAL.getAlertConfigByAlertName(alertName);
			InsightsWorkflowConfiguration workflowConfig = alertConfig.getWorkflowConfig();
			workflowConfig.setActive(status);
			alertConfig.setWorkflowConfig(workflowConfig);
			offlineAlertingDAL.updateOfflineAlertingConfig(alertConfig);
			return PlatformServiceConstants.SUCCESS;
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(" Error while updating alert status " + e.getMessage());
		}
	}

	@Override
	public JsonObject getAlertExecutionRecordsByWorkflowId(JsonObject alertConfigIdJson)
			throws InsightsCustomException {
		try {
			InsightsOfflineAlerting offlineAlertConfig = offlineAlertingDAL
					.getAlertConfigByAlertName(alertConfigIdJson.get("alertName").getAsString());
			String workflowId = offlineAlertConfig.getWorkflowConfig().getWorkflowId();
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
				rec.addProperty("statusLog", String.valueOf(record[4]));
				rec.addProperty("taskStatus", String.valueOf(record[5]));
				rec.addProperty("currentTask", String.valueOf(record[6]));
				recordData.add(rec);
			});
			JsonObject responseJson = new JsonObject();
			responseJson.add("records", recordData);
			return responseJson;
		} catch (Exception e) {
			log.error("Error while fetching Alert Execution History records.", e);
			throw new InsightsCustomException("Error while fetching Alert Execution History records");
		}
	}

}
