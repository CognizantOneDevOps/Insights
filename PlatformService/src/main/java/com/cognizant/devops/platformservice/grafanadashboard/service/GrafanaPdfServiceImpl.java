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

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.EmailConfiguration;
import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
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

	GrafanaDashboardPdfConfigDAL grafanaDashboardConfigDAL = new GrafanaDashboardPdfConfigDAL();
	WorkflowDAL workflowConfigDAL = new WorkflowDAL();
	GrafanaHandler grafanaHandler = new GrafanaHandler();
	
	@Autowired
	private HttpServletRequest httpRequest;

	@Override
	public void saveGrafanaDashboardConfig(JsonObject dashboardDetails) throws InsightsCustomException {
			int id = -1;
			WorkflowServiceImpl workflowService = new WorkflowServiceImpl();
			boolean runImmediate = Boolean.TRUE;
			boolean reoccurence = Boolean.FALSE;
			boolean isActive = Boolean.TRUE;
			String workflowType = WorkflowTaskEnum.WorkflowType.GRAFANADASHBOARDPDFREPORT.getValue();
			JsonElement scheduleType = dashboardDetails.get("scheduleType");
			String schedule = scheduleType == null ? 
					WorkflowTaskEnum.WorkflowSchedule.ONETIME.name(): scheduleType.getAsString();
			if(!schedule.equals(WorkflowTaskEnum.WorkflowSchedule.ONETIME.name())) {
				reoccurence = Boolean.TRUE;
			}
			String workflowStatus = WorkflowTaskEnum.WorkflowStatus.NOT_STARTED.name();
			String workflowId =  WorkflowTaskEnum.WorkflowType.GRAFANADASHBOARDPDFREPORT.getValue() + "_"
															+ InsightsUtils.getCurrentTimeInSeconds();
			JsonArray taskList = new JsonArray();
			JsonArray workflowList = workflowService.getTaskList(workflowType);
			workflowList.forEach(task -> 
				taskList.add(workflowService.createTaskJson(task.getAsJsonObject().get(AssessmentReportAndWorkflowConstants.TASK_ID).getAsInt(), 
						task.getAsJsonObject().get("dependency").getAsInt()))
			);
			JsonObject emailDetailsJson = getEmailDetails(dashboardDetails);
			InsightsWorkflowConfiguration workflowConfig = workflowService.saveWorkflowConfig(workflowId, isActive,
					reoccurence, schedule, workflowStatus, WorkflowTaskEnum.WorkflowType.GRAFANADASHBOARDPDFREPORT.getValue(), 
					taskList, 0, emailDetailsJson, runImmediate);
			
			GrafanaDashboardPdfConfig grafanaDashboardConfig = new GrafanaDashboardPdfConfig();
			grafanaDashboardConfig.setDashboardJson(dashboardDetails.toString());
			grafanaDashboardConfig.setTitle(dashboardDetails.get("title").getAsString());
			grafanaDashboardConfig.setPdfType(dashboardDetails.get("pdfType").getAsString());
			grafanaDashboardConfig.setStatus(workflowStatus);
			grafanaDashboardConfig.setVariables(dashboardDetails.get("variables").getAsString());
			grafanaDashboardConfig.setWorkflowConfig(workflowConfig);
			grafanaDashboardConfig.setEmail(dashboardDetails.get("email").getAsString());
			grafanaDashboardConfig.setSource(dashboardDetails.get("source").getAsString());
			grafanaDashboardConfig.setScheduleType(dashboardDetails.get("scheduleType")== null ? 
					WorkflowTaskEnum.WorkflowSchedule.ONETIME.name(): dashboardDetails.get("scheduleType").getAsString());
			grafanaDashboardConfig.setCreatedDate(InsightsUtils.getCurrentTimeInEpochMilliSeconds());
			grafanaDashboardConfig.setEmailbody(dashboardDetails.get("mailBodyTemplate") == null ? 
				"*** Please do not reply to this mail. ****. \n*** System triggered email for reports ***":dashboardDetails.get("mailBodyTemplate").getAsString());
			
			
			
			GrafanaOrgToken token = grafanaDashboardConfigDAL.getTokenByOrgId(dashboardDetails.get("organisation").getAsInt());
			if(token == null) {
				GrafanaOrgToken grafanaOrgToken = new GrafanaOrgToken();
				Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
				headers.put("x-grafana-org-id", dashboardDetails.get("organisation").getAsString());
				headers.put("Content-Type", "application/json");
				JsonObject json = new JsonObject();
				json.addProperty("name", "pdftoken");
				json.addProperty("role", "Viewer");
				String response = grafanaHandler.grafanaPost("/api/auth/keys",json, headers);
				JsonObject apiObj = new JsonParser().parse(response).getAsJsonObject();
				grafanaOrgToken.setOrgId(dashboardDetails.get("organisation").getAsInt());
				grafanaOrgToken.setApiKey(AES256Cryptor.encrypt(apiObj.get("key").getAsString(),ApplicationConfigProvider.getInstance().getSingleSignOnConfig().getTokenSigningKey()));
				grafanaDashboardConfigDAL.saveGrafanaOrgToken(grafanaOrgToken);
			}
			
			id = grafanaDashboardConfigDAL.saveGrafanaDashboardConfig(grafanaDashboardConfig);
			log.debug(id);

	}

	private JsonObject getEmailDetails(JsonObject dashboardDetails) {
		EmailConfiguration emailConfig = ApplicationConfigProvider.getInstance().getEmailConfiguration();
		JsonObject emailDetailsJson = new JsonObject();
		emailDetailsJson.addProperty("senderEmailAddress", emailConfig.getMailFrom());
		emailDetailsJson.addProperty("receiverEmailAddress", dashboardDetails.get("email").getAsString());
		emailDetailsJson.addProperty("mailSubject", dashboardDetails.get("title").getAsString());
		emailDetailsJson.addProperty("mailBodyTemplate", dashboardDetails.get("mailBodyTemplate") == null ? 
				"*** Please do not reply to this mail. ****. \n*** System triggered email for reports ***":dashboardDetails.get("mailBodyTemplate").getAsString());
		emailDetailsJson.addProperty("receiverCCEmailAddress", dashboardDetails.get("receiverCCEmailAddress") == null ? "": dashboardDetails.get("receiverCCEmailAddress").getAsString() );
		emailDetailsJson.addProperty("receiverBCCEmailAddress", dashboardDetails.get("receiverBCCEmailAddress") == null ? "": dashboardDetails.get("receiverBCCEmailAddress").getAsString());
		return emailDetailsJson;
	}

	@Override
	public List<GrafanaDashboardPdfConfig> getAllGrafanaDashboardConfigs() throws InsightsCustomException {
		return grafanaDashboardConfigDAL.getAllGrafanaDashboardConfigs();
	}

	@Override
	public void updateGrafanaDashboardDetails(JsonObject dashboardDetails) throws InsightsCustomException {

		WorkflowServiceImpl workflowService = new WorkflowServiceImpl();
		boolean runImmediate = Boolean.TRUE;
		boolean reoccurence = Boolean.FALSE;
		boolean isActive = Boolean.TRUE;
		String workflowType = WorkflowTaskEnum.WorkflowType.GRAFANADASHBOARDPDFREPORT.getValue();
		String schedule = dashboardDetails.get("scheduleType").getAsString();
		String workflowStatus = WorkflowTaskEnum.WorkflowStatus.NOT_STARTED.name();
		if(!schedule.equals(WorkflowTaskEnum.WorkflowSchedule.ONETIME.name())) {
			reoccurence = Boolean.TRUE;
		}
		GrafanaDashboardPdfConfig grafanaDashboardPdfConfig = grafanaDashboardConfigDAL.getWorkflowById(dashboardDetails.get("id").getAsInt());
		InsightsWorkflowConfiguration workflowConfig = grafanaDashboardPdfConfig.getWorkflowConfig();
		JsonObject emailDetailsJson = getEmailDetails(dashboardDetails);
		
		InsightsEmailTemplates emailTemplateConfig = workflowService.createEmailTemplateObject(emailDetailsJson,
				workflowConfig);
		workflowConfig.setEmailConfig(emailTemplateConfig);
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
		grafanaDashboardPdfConfig.setStatus(workflowStatus);
		grafanaDashboardPdfConfig.setVariables(dashboardDetails.get("variables").getAsString());
		grafanaDashboardPdfConfig.setWorkflowConfig(workflowConfig);
		grafanaDashboardPdfConfig.setEmail(dashboardDetails.get("email").getAsString());
		grafanaDashboardPdfConfig.setSource(dashboardDetails.get("source").getAsString());
		grafanaDashboardPdfConfig.setScheduleType(schedule);
		grafanaDashboardPdfConfig.setCreatedDate(InsightsUtils.getCurrentTimeInEpochMilliSeconds());
		grafanaDashboardPdfConfig.setEmailbody(dashboardDetails.get("mailBodyTemplate").getAsString());
		grafanaDashboardConfigDAL.updateGrafanaDashboardConfig(grafanaDashboardPdfConfig);
	}

	@Override
	public void deleteGrafanaDashboardDetails(int id) throws InsightsCustomException {
		GrafanaDashboardPdfConfig grafanaDashboardPdfConfig = grafanaDashboardConfigDAL.getWorkflowById(id);
		grafanaDashboardConfigDAL.deleteGrafanaDashboardConfig(grafanaDashboardPdfConfig);
	}

}
