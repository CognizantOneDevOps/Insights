/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
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
package com.cognizant.devops.platformservice.grafanadashboard.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.EmailConfiguration;
import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.AES256Cryptor;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.dal.grafana.GrafanaHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.assessmentreport.InsightsEmailTemplates;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaDashboardPdfConfig;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaDashboardPdfConfigDAL;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaOrgToken;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTaskSequence;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.cognizant.devops.platformservice.workflow.service.WorkflowServiceImpl;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service
public class GrafanaPdfServiceImpl implements GrafanaPdfService{

	private static final Logger log = LogManager.getLogger(GrafanaPdfServiceImpl.class);
	
	private static final String SCHEDULE_TYPE = "scheduleType";
	private static final String EMAIL_DETAILS = "emailDetails";
	private static final String ORGANISATION = "organisation";
	private static final String MAIL_BODY_TEMPLATE = "mailBodyTemplate";
	private static final String RECEIVER_CC_EMAIL_ADDRESS= "receiverCCEmailAddress";
	private static final String RECEIVER_BCC_EMAIL_ADDRESS= "receiverBCCEmailAddress";


	GrafanaDashboardPdfConfigDAL grafanaDashboardConfigDAL = new GrafanaDashboardPdfConfigDAL();
	WorkflowDAL workflowConfigDAL = new WorkflowDAL();
	GrafanaHandler grafanaHandler = new GrafanaHandler();
	
	@Autowired
	private HttpServletRequest httpRequest;


	/**
	 * Used to store grafana dashboard configuration
	 * 
	 *@param dasboardDetails
	 *@return
	 *@throws InsightsCustomException
	 */
	@Override
	public void saveGrafanaDashboardConfig(JsonObject dashboardDetails) throws InsightsCustomException {
			int id = -1;
			WorkflowServiceImpl workflowService = new WorkflowServiceImpl();
			boolean runImmediate = Boolean.TRUE;
			boolean reoccurence = Boolean.FALSE;
			boolean isActive = Boolean.TRUE;
			try {
				String workflowType = WorkflowTaskEnum.WorkflowType.GRAFANADASHBOARDPDFREPORT.getValue();
				JsonElement scheduleType = dashboardDetails.get(SCHEDULE_TYPE);
				String schedule = scheduleType == null ? 
					WorkflowTaskEnum.WorkflowSchedule.ONETIME.name(): scheduleType.getAsString();
				if(!schedule.equals(WorkflowTaskEnum.WorkflowSchedule.ONETIME.name())) {
					reoccurence = Boolean.TRUE;
				}
				String workflowStatus = WorkflowTaskEnum.WorkflowStatus.NOT_STARTED.name();
				String workflowId =  WorkflowTaskEnum.WorkflowType.GRAFANADASHBOARDPDFREPORT.getValue() + "_"
															+ InsightsUtils.getCurrentTimeInSeconds();
				boolean emailEnabled = dashboardDetails.has(EMAIL_DETAILS);
				JsonObject emailDetails = null;
				JsonObject emailDetailsJson = null;
				if(emailEnabled) {
					emailDetails = dashboardDetails.get(EMAIL_DETAILS).getAsJsonObject();
					emailDetailsJson = getEmailDetails(emailDetails);
				}
			
				JsonArray taskList = new JsonArray();
				JsonArray workflowList = workflowService.getTaskList(workflowType);
				log.debug("Grafana task list from table==={}",workflowList);
				for(JsonElement task: workflowList) {
					JsonObject taskJson = task.getAsJsonObject();
					String componentName = task.getAsJsonObject().get("componentName").getAsString();
					if(!(!emailEnabled && componentName.contains("ReportEmailSubscriber"))) {
						taskList.add(workflowService.createTaskJson(taskJson.get(AssessmentReportAndWorkflowConstants.TASK_ID).getAsInt(), 
								taskJson.get("dependency").getAsInt()));
					}
				}
				log.debug("Grafana task list====={}",taskList);
				InsightsWorkflowConfiguration workflowConfig = workflowService.saveWorkflowConfig(workflowId, isActive,
						reoccurence, schedule, workflowStatus, WorkflowTaskEnum.WorkflowType.GRAFANADASHBOARDPDFREPORT.getValue(), 
						taskList, 0, emailDetailsJson, runImmediate);
			
				GrafanaDashboardPdfConfig grafanaDashboardConfig = new GrafanaDashboardPdfConfig();
				grafanaDashboardConfig.setDashboardJson(dashboardDetails.toString());
				grafanaDashboardConfig.setTitle(dashboardDetails.get("title").getAsString());
				grafanaDashboardConfig.setPdfType(dashboardDetails.get("pdfType").getAsString());
				grafanaDashboardConfig.setVariables(dashboardDetails.get("variables").getAsString());
				grafanaDashboardConfig.setWorkflowConfig(workflowConfig);
				grafanaDashboardConfig.setSource(dashboardDetails.get("source").getAsString());
				grafanaDashboardConfig.setScheduleType(dashboardDetails.get(SCHEDULE_TYPE)== null ? 
						WorkflowTaskEnum.WorkflowSchedule.ONETIME.name(): dashboardDetails.get(SCHEDULE_TYPE).getAsString());
				grafanaDashboardConfig.setCreatedDate(InsightsUtils.getCurrentTimeInEpochMilliSeconds());
				
				GrafanaOrgToken token = grafanaDashboardConfigDAL.getTokenByOrgId(dashboardDetails.get(ORGANISATION).getAsInt());
				if(token == null) {
					generateGrafanaToken(dashboardDetails);
				}
				
				id = grafanaDashboardConfigDAL.saveGrafanaDashboardConfig(grafanaDashboardConfig);
				log.debug(id);
			} catch(Exception e) {
				throw new InsightsCustomException(e.getMessage());
			}

	}

	/**
	 * Used to fetch email details from server config
	 * 
	 *@param emailDetails
	 *@return emailDetailsJson
	 *@throws InsightsCustomException
	 */
	private JsonObject getEmailDetails(JsonObject emailDetails) {
		EmailConfiguration emailConfig = ApplicationConfigProvider.getInstance().getEmailConfiguration();
		JsonObject emailDetailsJson = new JsonObject();
		emailDetailsJson.addProperty("senderEmailAddress", emailConfig.getMailFrom());
		emailDetailsJson.addProperty("receiverEmailAddress", emailDetails.get("receiverEmailAddress").getAsString());
		emailDetailsJson.addProperty("mailSubject", emailDetails.get("mailSubject").getAsString());
		emailDetailsJson.addProperty(MAIL_BODY_TEMPLATE, emailDetails.get(MAIL_BODY_TEMPLATE) == null ? 
				"*** Please do not reply to this mail. ****. \n*** System triggered email for dashboards ***":emailDetails.get(MAIL_BODY_TEMPLATE).getAsString());
		emailDetailsJson.addProperty(RECEIVER_CC_EMAIL_ADDRESS, emailDetails.get(RECEIVER_CC_EMAIL_ADDRESS) == null ? "": emailDetails.get(RECEIVER_CC_EMAIL_ADDRESS).getAsString() );
		emailDetailsJson.addProperty(RECEIVER_BCC_EMAIL_ADDRESS, emailDetails.get(RECEIVER_BCC_EMAIL_ADDRESS) == null ? "": emailDetails.get(RECEIVER_BCC_EMAIL_ADDRESS).getAsString());
		return emailDetailsJson;
	}

	/**
	 * Used to fetch all grafana dashboard configurations
	 * 
	 *@param 
	 *@return list of GrafanaDashboardPdfConfig
	 *@throws InsightsCustomException
	 */
	@Override
	public List<GrafanaDashboardPdfConfig> getAllGrafanaDashboardConfigs() throws InsightsCustomException {
		return grafanaDashboardConfigDAL.getAllGrafanaDashboardConfigs();
	}
	
	/**
	 * Used to update grafana dashboard config details
	 * 
	 *@param dashboardDetails
	 *@return 
	 *@throws InsightsCustomException
	 */
	@Override
	public void updateGrafanaDashboardDetails(JsonObject dashboardDetails) throws InsightsCustomException {

		WorkflowServiceImpl workflowService = new WorkflowServiceImpl();
		boolean runImmediate = Boolean.TRUE;
		boolean reoccurence = Boolean.FALSE;
		boolean isActive = Boolean.TRUE;
		try {
			String workflowType = WorkflowTaskEnum.WorkflowType.GRAFANADASHBOARDPDFREPORT.getValue();
			String schedule = dashboardDetails.get(SCHEDULE_TYPE).getAsString();
			String workflowStatus = WorkflowTaskEnum.WorkflowStatus.NOT_STARTED.name();
			if(!schedule.equals(WorkflowTaskEnum.WorkflowSchedule.ONETIME.name())) {
				reoccurence = Boolean.TRUE;
			}
			GrafanaDashboardPdfConfig grafanaDashboardPdfConfig = grafanaDashboardConfigDAL.getWorkflowById(dashboardDetails.get("id").getAsInt());
			InsightsWorkflowConfiguration workflowConfig = grafanaDashboardPdfConfig.getWorkflowConfig();
	
			boolean emailEnabled = dashboardDetails.has(EMAIL_DETAILS);
			JsonObject emailDetails = null;
			JsonObject emailDetailsJson = null;
			if(emailEnabled) {
				emailDetails = dashboardDetails.get(EMAIL_DETAILS).getAsJsonObject();
				emailDetailsJson = getEmailDetails(emailDetails);
			}
			
			JsonArray taskList = new JsonArray();
			JsonArray workflowList = workflowService.getTaskList(workflowType);
			log.debug("Grafana task list from table===={}",workflowList);
			for(JsonElement task: workflowList) {
				JsonObject taskJson = task.getAsJsonObject();
				String componentName = task.getAsJsonObject().get("componentName").getAsString();
				if(!(!emailEnabled && componentName.contains("ReportEmailSubscriber"))) {
					taskList.add(workflowService.createTaskJson(taskJson.get(AssessmentReportAndWorkflowConstants.TASK_ID).getAsInt(), 
							taskJson.get("dependency").getAsInt()));
				}
			}
			log.debug("Grafana task list====={}",taskList);
			Set<InsightsWorkflowTaskSequence> taskSequenceSet = workflowService.setSequence(taskList, workflowConfig);
			workflowConfig.setTaskSequenceEntity(taskSequenceSet);
			if(emailEnabled) {
				InsightsEmailTemplates emailTemplateConfig = workflowService.createEmailTemplateObject(emailDetailsJson,
						workflowConfig);
				workflowConfig.setEmailConfig(emailTemplateConfig);
			}
			workflowConfig.setActive(isActive);
			if (schedule.equals(WorkflowTaskEnum.WorkflowSchedule.ONETIME.toString())) {
				workflowConfig.setNextRun(0L);
			} else if (schedule.equals(WorkflowTaskEnum.WorkflowSchedule.BI_WEEKLY_SPRINT.toString())
					|| schedule.equals(WorkflowTaskEnum.WorkflowSchedule.TRI_WEEKLY_SPRINT.toString())) {
				workflowConfig.setNextRun(InsightsUtils.getNextRunTime(0, schedule, true));
			} else {
				workflowConfig
						.setNextRun(InsightsUtils.getNextRunTime(InsightsUtils.getCurrentTimeInSeconds(), schedule, true));
			}
			workflowConfig.setLastRun(0L);
			workflowConfig.setReoccurence(reoccurence);
			workflowConfig.setScheduleType(schedule);
			workflowConfig.setStatus(workflowStatus);
			workflowConfig.setWorkflowType(workflowType);
			workflowConfig.setRunImmediate(runImmediate);
			
			grafanaDashboardPdfConfig.setDashboardJson(dashboardDetails.toString());
			grafanaDashboardPdfConfig.setTitle(dashboardDetails.get("title").getAsString());
			grafanaDashboardPdfConfig.setPdfType(dashboardDetails.get("pdfType").getAsString());
			grafanaDashboardPdfConfig.setVariables(dashboardDetails.get("variables").getAsString());
			grafanaDashboardPdfConfig.setWorkflowConfig(workflowConfig);
			grafanaDashboardPdfConfig.setSource(dashboardDetails.get("source").getAsString());
			grafanaDashboardPdfConfig.setScheduleType(schedule);
			grafanaDashboardPdfConfig.setCreatedDate(InsightsUtils.getCurrentTimeInEpochMilliSeconds());

			grafanaDashboardConfigDAL.updateGrafanaDashboardConfig(grafanaDashboardPdfConfig);
		} catch(Exception e) {
			throw new InsightsCustomException(e.getMessage());
		}
	}

	/**
	 * Used to delete grafana dashboard configuration
	 * 
	 *@param id
	 *@return 
	 *@throws InsightsCustomException
	 */
	@Override
	public void deleteGrafanaDashboardDetails(int id) throws InsightsCustomException {
		try {
			GrafanaDashboardPdfConfig grafanaDashboardPdfConfig = grafanaDashboardConfigDAL.getWorkflowById(id);
			workflowConfigDAL.deleteEmailExecutionHistoryByWorkflowId(grafanaDashboardPdfConfig.getWorkflowConfig().getWorkflowId());
			grafanaDashboardConfigDAL.deleteGrafanaDashboardConfig(grafanaDashboardPdfConfig);
		} catch (Exception e) {
			log.error("Error while deleting dashboard report.", e);
			throw new InsightsCustomException(e.getMessage());
		}
	}
	
	/**
	 * Used to update grafana dashboard configuration status
	 * 
	 *@param dashboardJson
	 *@return String
	 *@throws InsightsCustomException
	 */
	@Override
	public String updateDashboardPdfConfigStatus(JsonObject dashboardJson)throws InsightsCustomException {
		try {							
			int id = dashboardJson.get("id").getAsInt();
			GrafanaDashboardPdfConfig grafanaDashboardPdfConfig = grafanaDashboardConfigDAL.getWorkflowById(id);
			InsightsWorkflowConfiguration workflowConfig = grafanaDashboardPdfConfig.getWorkflowConfig();
			if(dashboardJson.has(AssessmentReportAndWorkflowConstants.STATUS)) {
				String status = dashboardJson.get(AssessmentReportAndWorkflowConstants.STATUS).getAsString();
				workflowConfig.setStatus(status);
				grafanaDashboardPdfConfig.setWorkflowConfig(workflowConfig);
				grafanaDashboardConfigDAL.updateGrafanaDashboardConfig(grafanaDashboardPdfConfig);
			}
			return PlatformServiceConstants.SUCCESS;
		} catch (Exception e) {
			log.error("Error while updating Dashboard status.", e);
			throw new InsightsCustomException(e.getMessage());
		}
	}
	
	/**
	 * Used to set grafana dashboard active state
	 * 
	 *@param dashboardUpdateJson
	 *@return String
	 *@throws InsightsCustomException
	 */
	@Override
	public String setDashboardActiveState(JsonObject dashboardUpdateJson) throws InsightsCustomException {
		try {							
			int id = dashboardUpdateJson.get("id").getAsInt();
			GrafanaDashboardPdfConfig grafanaDashboardPdfConfig = grafanaDashboardConfigDAL.getWorkflowById(id);
			InsightsWorkflowConfiguration workflowConfig = grafanaDashboardPdfConfig.getWorkflowConfig();
			if(dashboardUpdateJson.has(AssessmentReportAndWorkflowConstants.ISACTIVE)) {
				Boolean isActive = dashboardUpdateJson.get(AssessmentReportAndWorkflowConstants.ISACTIVE).getAsBoolean();
				workflowConfig.setActive(isActive);
				grafanaDashboardPdfConfig.setWorkflowConfig(workflowConfig);
				grafanaDashboardConfigDAL.updateGrafanaDashboardConfig(grafanaDashboardPdfConfig);
			}
			return PlatformServiceConstants.SUCCESS;
		} catch (Exception e) {
			log.error("Error while updating Dashboard Active status.", e);
			throw new InsightsCustomException(e.getMessage());
		}
	}
	
	
	private void generateGrafanaToken(JsonObject dashboardDetails) {
		try {				
			GrafanaOrgToken grafanaOrgToken = new GrafanaOrgToken();
			Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
			headers.put("x-grafana-org-id", dashboardDetails.get(ORGANISATION).getAsString());
			headers.put("Content-Type", "application/json");
			JsonObject json = new JsonObject();
			json.addProperty("name", "pdftoken");
			json.addProperty("role", "Viewer");
			String response = grafanaHandler.grafanaPost("/api/auth/keys",json, headers);
			JsonObject apiObj = new JsonParser().parse(response).getAsJsonObject();
			grafanaOrgToken.setOrgId(dashboardDetails.get(ORGANISATION).getAsInt());
			grafanaOrgToken.setApiKey(AES256Cryptor.encrypt(apiObj.get("key").getAsString(),ApplicationConfigProvider.getInstance().getSingleSignOnConfig().getTokenSigningKey()));
			grafanaDashboardConfigDAL.saveGrafanaOrgToken(grafanaOrgToken);
		} catch (Exception e) {
			log.error("Unable to generate Grafana token  {}", e.getMessage());
		}
	}

}
