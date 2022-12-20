/*******************************************************************************
 * Copyright 2022 Cognizant Technology Solutions
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
package com.cognizant.devops.platformservice.emailconfiguration.service;

import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.EmailConfiguration;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.assessmentreport.InsightsAssessmentConfiguration;
import com.cognizant.devops.platformdal.assessmentreport.InsightsEmailTemplates;
import com.cognizant.devops.platformdal.assessmentreport.ReportConfigDAL;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaDashboardPdfConfig;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaDashboardPdfConfigDAL;
import com.cognizant.devops.platformdal.groupemail.GroupEmailConfigDAL;
import com.cognizant.devops.platformdal.groupemail.InsightsGroupEmailConfiguration;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTaskSequence;
import com.cognizant.devops.platformservice.workflow.service.WorkflowServiceImpl;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Service("emailConfigurationService")
public class EmailConfigurationServiceImpl implements EmailConfigurationService {

	private static final String MAIL_BODY_TEMPLATE = "mailBodyTemplate";
	private static final String MAIL_SUBJECT = "mailSubject";
	private static final String SENDER_EMAIL_ADDRESS = "senderEmailAddress";
	private static final String RECEIVER_EMAIL_ADDRESS = "receiverEmailAddress";
	private static final String RECEIVER_CC_EMAIL_ADDRESS = "receiverCCEmailAddress";
	private static final String RECEIVER_BCC_EMAIL_ADDRESS = "receiverBCCEmailAddress";
	private static final String EMAIL_DETAILS = "emailDetails";
	private static final String SOURCE = "source";
	private static final String SCHEDULE = "schedule";
	private static final String REPORTS = "reports";
	private static final String INVALID_SOURCE_MESSAGE = "Invalid Source";



	ReportConfigDAL reportConfigDAL = new ReportConfigDAL();
	GrafanaDashboardPdfConfigDAL grafanaDashboardConfigDAL = new GrafanaDashboardPdfConfigDAL();
	GroupEmailConfigDAL groupEmailConfigDAL = new GroupEmailConfigDAL();


	private static Logger log = LogManager.getLogger(EmailConfigurationServiceImpl.class);

	@Override
	public int saveEmailConfig(JsonObject emailConfigJson) throws InsightsCustomException {
		int id;
		try {
			id = populateGroupEmailConfiguration(emailConfigJson);
		} catch (InsightsCustomException e) {
			log.error("Error while saving the email config .. {}", e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
		return id;
	}

	@Override
	public JsonArray getAllGroupEmailConfigurations(String source) throws InsightsCustomException {
		JsonArray jsonarray = new JsonArray();
		try {
			List<InsightsGroupEmailConfiguration> groupEmailConfigs;
			if (source.equalsIgnoreCase(WorkflowTaskEnum.WorkflowType.GRAFANADASHBOARDPDFREPORT.getValue())
					|| source.equalsIgnoreCase(WorkflowTaskEnum.WorkflowType.REPORT.getValue())) {
				groupEmailConfigs = groupEmailConfigDAL.fetchAllGroupEmailConfigs(source);
				for (InsightsGroupEmailConfiguration config : groupEmailConfigs) {
					JsonObject configJson = new JsonObject();
					configJson.addProperty("groupTemplateId", config.getGroupEmailTemplateID());
					configJson.addProperty("batchName", config.getBatchName());
					configJson.addProperty(SCHEDULE, config.getSchedule());
					configJson.addProperty(REPORTS, config.getMapIdList());
					configJson.addProperty(SOURCE, config.getSource());
					configJson.addProperty("workflowId", config.getWorkflowConfig().getWorkflowId());
					configJson.addProperty("isActive", config.isActive());
					configJson.addProperty("lastRun", config.getWorkflowConfig().getLastRun());
					configJson.addProperty("nextRun", config.getWorkflowConfig().getNextRun());
					JsonObject emailDetails = new JsonObject();
					emailDetails.addProperty(SENDER_EMAIL_ADDRESS,
							config.getWorkflowConfig().getEmailConfig().getMailFrom());
					emailDetails.addProperty(RECEIVER_EMAIL_ADDRESS,
							config.getWorkflowConfig().getEmailConfig().getMailTo());
					emailDetails.addProperty(RECEIVER_CC_EMAIL_ADDRESS,
							config.getWorkflowConfig().getEmailConfig().getMailCC());
					emailDetails.addProperty(RECEIVER_BCC_EMAIL_ADDRESS,
							config.getWorkflowConfig().getEmailConfig().getMailBCC());
					emailDetails.addProperty(MAIL_SUBJECT, config.getWorkflowConfig().getEmailConfig().getSubject());
					emailDetails.addProperty(MAIL_BODY_TEMPLATE,
							config.getWorkflowConfig().getEmailConfig().getMailBody());
					configJson.add(EMAIL_DETAILS, emailDetails);
					jsonarray.add(configJson);
				}
			} else {
				throw new InsightsCustomException(INVALID_SOURCE_MESSAGE);
			}
		} catch (Exception e) {
			log.error("Error while fetching group email configs .. {}", e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
		return jsonarray;
	}

	@Override
	public JsonArray getAllReportTitles(String source, String userDetails) throws InsightsCustomException {
		JsonArray titleDetailsArray = new JsonArray();
		try {
			if (source.equals(WorkflowTaskEnum.WorkflowType.REPORT.getValue())) {
				List<InsightsAssessmentConfiguration> assessmentReportList = reportConfigDAL.getAllAssessmentConfig();
				for (InsightsAssessmentConfiguration assessmentReport : assessmentReportList) {
					if (assessmentReport.getWorkflowConfig().getEmailConfig() != null) {
						JsonObject titleDetails = prepareReportTitleDetails(assessmentReport.getId(), assessmentReport.getAsseementreportname());
						titleDetailsArray.add(titleDetails);
					}
				}

			} else if (source.equals(WorkflowTaskEnum.WorkflowType.GRAFANADASHBOARDPDFREPORT.getValue())) {
				List<GrafanaDashboardPdfConfig> result = grafanaDashboardConfigDAL.getAllGrafanaDashboardConfigs();
				for (GrafanaDashboardPdfConfig dashboardConfig : result) {
					if (dashboardConfig.getWorkflowConfig().getEmailConfig() != null) {
						JsonObject titleDetails = prepareReportTitleDetails(dashboardConfig.getId(), dashboardConfig.getTitle());
						titleDetailsArray.add(titleDetails);
					}
				}
			} else {
				throw new InsightsCustomException(INVALID_SOURCE_MESSAGE);
			}
		} catch (Exception e) {
			log.error("Error while getting report titles.");
			throw new InsightsCustomException(e.getMessage());
		}
		return titleDetailsArray;
	}

	@Override
	public String deleteGroupEmailConfiguration(int groupTemplateId) throws InsightsCustomException {
		try {
			InsightsGroupEmailConfiguration groupEmailConfig = groupEmailConfigDAL
					.getConfigByGroupEmailTemplateId(groupTemplateId);
			if (groupEmailConfig == null) {
				throw new InsightsCustomException("Group Email Configuration not found.");
			}
			groupEmailConfigDAL.deleteGroupEmailConfig(groupEmailConfig);
		} catch (Exception e) {
			throw new InsightsCustomException(e.getMessage());
		}
		return PlatformServiceConstants.SUCCESS;
	}

	@Override
	public String updateGroupEmailConfig(JsonObject emailConfigJson) throws InsightsCustomException {
		JsonObject emailDetails = null;
		boolean runImmediate = Boolean.TRUE;
		boolean reoccurence = Boolean.FALSE;
		boolean isActive = Boolean.TRUE;
		WorkflowServiceImpl workflowService = new WorkflowServiceImpl();
		try {
			String workflowStatus = WorkflowTaskEnum.WorkflowStatus.NOT_STARTED.name();
			InsightsGroupEmailConfiguration groupEmailConfig = groupEmailConfigDAL
					.getConfigByGroupEmailTemplateId(emailConfigJson.get("id").getAsInt());
			String schedule = groupEmailConfig.getSchedule();
			if (!schedule.equals(WorkflowTaskEnum.WorkflowSchedule.ONETIME.name())) {
				reoccurence = Boolean.TRUE;
			}
			InsightsWorkflowConfiguration workflowConfig = groupEmailConfig.getWorkflowConfig();
			JsonObject emailDetailsJson = null;
			emailDetails = emailConfigJson.get(EMAIL_DETAILS).getAsJsonObject();
			emailDetailsJson = getEmailDetails(emailDetails);
			JsonArray taskList = workflowService.getTaskList(workflowConfig.getWorkflowType());
			workflowConfig.setActive(isActive);
			if (schedule.equals(WorkflowTaskEnum.WorkflowSchedule.ONETIME.toString())) {
				workflowConfig.setNextRun(0L);
			} else if (schedule.equals(WorkflowTaskEnum.WorkflowSchedule.BI_WEEKLY_SPRINT.toString())
					|| schedule.equals(WorkflowTaskEnum.WorkflowSchedule.TRI_WEEKLY_SPRINT.toString())) {
				workflowConfig.setNextRun(InsightsUtils.getNextRunTime(0, schedule, true));
			} else {
				workflowConfig.setNextRun(
						InsightsUtils.getNextRunTime(InsightsUtils.getCurrentTimeInSeconds(), schedule, true));
			}
			Set<InsightsWorkflowTaskSequence> taskSequenceSet = workflowService.setSequence(taskList, workflowConfig);
			InsightsEmailTemplates emailTemplateConfig = workflowService.createEmailTemplateObject(emailDetailsJson,
					workflowConfig);
			workflowConfig.setEmailConfig(emailTemplateConfig);
			workflowConfig.setTaskSequenceEntity(taskSequenceSet);
			workflowConfig.setReoccurence(reoccurence);
			workflowConfig.setScheduleType(schedule);
			workflowConfig.setStatus(workflowStatus);
			workflowConfig.setRunImmediate(runImmediate);
			JsonArray reportsJsonArray = emailConfigJson.get(REPORTS).getAsJsonArray();
			groupEmailConfig.setActive(isActive);
			groupEmailConfig.setSchedule(schedule);
			groupEmailConfig.setMapIdList(reportsJsonArray.toString());
			groupEmailConfig.setWorkflowConfig(workflowConfig);
			groupEmailConfigDAL.updateGroupEmailConfig(groupEmailConfig);
		} catch (Exception e) {
			throw new InsightsCustomException("Error while Updating GroupEmailConfiguration");
		}
		return PlatformServiceConstants.SUCCESS;
	}

	@Override
	public String updateGroupEmailConfigState(JsonObject updateEmailConfigJson) throws InsightsCustomException {
		try {
			InsightsGroupEmailConfiguration groupEmailConfig = groupEmailConfigDAL
					.getConfigByGroupEmailTemplateId(updateEmailConfigJson.get("id").getAsInt());
			Boolean state = updateEmailConfigJson.get("isActive").getAsBoolean();
			InsightsWorkflowConfiguration workflowConfig = groupEmailConfig.getWorkflowConfig();
			workflowConfig.setActive(state);
			groupEmailConfig.setWorkflowConfig(workflowConfig);
			groupEmailConfig.setActive(state);
			groupEmailConfigDAL.updateGroupEmailConfig(groupEmailConfig);
		} catch (Exception e) {
			throw new InsightsCustomException("Error while Updating GroupEmailConfiguration");
		}
		return PlatformServiceConstants.SUCCESS;
	}


	/**
	 * Used to Populate the Group Email Configuration
	 * 
	 * @param emailConfigJson
	 * @return populatedConfigId
	 * @throws InsightsCustomException
	 */
	private int populateGroupEmailConfiguration(JsonObject emailConfigJson) throws InsightsCustomException {
		JsonObject emailDetails = null;
		boolean runImmediate = Boolean.TRUE;
		boolean reoccurence = Boolean.FALSE;
		boolean isActive = Boolean.TRUE;
		WorkflowServiceImpl workflowService = new WorkflowServiceImpl();
		int populatedConfigId;
		try {
			log.debug("Group Email config to be saved {} ", emailConfigJson);
			String batchName = emailConfigJson.get("batchName").getAsString();
			JsonArray reportsJsonArray = emailConfigJson.get(REPORTS).getAsJsonArray();
			String workflowType = WorkflowTaskEnum.WorkflowType.GROUP_EMAIL.getValue();
			InsightsGroupEmailConfiguration groupEmailConfig = groupEmailConfigDAL.getConfigByBatchName(batchName);
			if (groupEmailConfig != null) {
				throw new InsightsCustomException("GroupEmailConfiguration with the given Batch name already exists");
			}

			String source;
			if (emailConfigJson.get(SOURCE).getAsString()
					.equalsIgnoreCase(WorkflowTaskEnum.WorkflowType.REPORT.getValue())) {
				source = WorkflowTaskEnum.WorkflowType.REPORT.getValue();
			} else if (emailConfigJson.get(SOURCE).getAsString()
					.equalsIgnoreCase(WorkflowTaskEnum.WorkflowType.GRAFANADASHBOARDPDFREPORT.getValue())) {
				source = WorkflowTaskEnum.WorkflowType.GRAFANADASHBOARDPDFREPORT.getValue();
			} else {
				throw new InsightsCustomException(INVALID_SOURCE_MESSAGE);
			}
			String schedule = emailConfigJson.get(SCHEDULE).getAsString();
			if (!schedule.equals(WorkflowTaskEnum.WorkflowSchedule.ONETIME.name())) {
				reoccurence = Boolean.TRUE;
			}
			String status = WorkflowTaskEnum.WorkflowStatus.NOT_STARTED.toString();
			String workflowId = WorkflowTaskEnum.WorkflowType.GROUP_EMAIL.getValue() + "_"
					+ InsightsUtils.getCurrentTimeInSeconds();
			emailDetails = emailConfigJson.get(EMAIL_DETAILS).getAsJsonObject();
			JsonObject emailDetailsJson = getEmailDetails(emailDetails);

			JsonArray taskList = workflowService.getTaskList(workflowType);
			InsightsWorkflowConfiguration workflowConfig = workflowService.saveWorkflowConfig(workflowId, isActive,
					reoccurence, schedule, status, WorkflowTaskEnum.WorkflowType.GROUP_EMAIL.getValue(), taskList, 0,
					emailDetailsJson, runImmediate);
			groupEmailConfig = new InsightsGroupEmailConfiguration();
			groupEmailConfig.setBatchName(batchName);
			groupEmailConfig.setActive(isActive);
			groupEmailConfig.setMapIdList(reportsJsonArray.toString());
			groupEmailConfig.setSource(source);
			groupEmailConfig.setSchedule(schedule);
			groupEmailConfig.setWorkflowConfig(workflowConfig);
			populatedConfigId = groupEmailConfigDAL.saveInsightsGroupEmailConfiguration(groupEmailConfig);
		} catch (Exception e) {
			throw new InsightsCustomException(e.getMessage());
		}
		return populatedConfigId;
	}

	/**
	 * Used to fetch email details from server config
	 * 
	 * @param emailDetails
	 * @return emailDetailsJson
	 * @throws InsightsCustomException
	 */
	private JsonObject getEmailDetails(JsonObject emailDetails) {
		EmailConfiguration emailConfig = ApplicationConfigProvider.getInstance().getEmailConfiguration();
		JsonObject emailDetailsJson = new JsonObject();
		emailDetailsJson.addProperty(SENDER_EMAIL_ADDRESS, emailConfig.getMailFrom());
		emailDetailsJson.addProperty(RECEIVER_EMAIL_ADDRESS, emailDetails.get(RECEIVER_EMAIL_ADDRESS).getAsString());
		emailDetailsJson.addProperty(MAIL_SUBJECT, emailDetails.get(MAIL_SUBJECT).getAsString());
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
	
	/**
	 * Used to prepare report title details 
	 * 
	 * @param id
	 * @param reportName
	 * @return titleDetails
	 */
	private JsonObject prepareReportTitleDetails(int id, String reportName) {
		JsonObject titleDetails = new JsonObject();
		titleDetails.addProperty("id", id);
		titleDetails.addProperty("reportName", reportName);
		return titleDetails;

	}

}
