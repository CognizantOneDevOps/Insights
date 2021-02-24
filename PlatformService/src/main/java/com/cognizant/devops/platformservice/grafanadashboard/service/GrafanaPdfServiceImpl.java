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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.EmailConfiguration;
import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaDashboardPdfConfig;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaDashboardPdfConfigDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformservice.workflow.service.WorkflowServiceImpl;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Service
public class GrafanaPdfServiceImpl implements GrafanaPdfService{

	private static final Logger log = LogManager.getLogger(GrafanaPdfServiceImpl.class);

	GrafanaDashboardPdfConfigDAL grafanaDashboardConfigDAL = new GrafanaDashboardPdfConfigDAL();

	@Override
	public void saveGrafanaDashboardConfig(JsonObject dashboardDetails) throws InsightsCustomException {
			int id = -1;
			WorkflowServiceImpl workflowService = new WorkflowServiceImpl();
			boolean runImmediate = true;
			boolean reoccurence = false;
			boolean isActive = true;
			String workflowType = WorkflowTaskEnum.WorkflowType.GRAFANADASHBOARDPDFREPORT.getValue();
			String schedule = WorkflowTaskEnum.WorkflowSchedule.ONETIME.name();
			String workflowStatus = WorkflowTaskEnum.WorkflowStatus.NOT_STARTED.name();
			String workflowId = WorkflowTaskEnum.WorkflowType.GRAFANADASHBOARDPDFREPORT.getValue() + "_"
					+ InsightsUtils.getCurrentTimeInSeconds();
			JsonArray taskList = new JsonArray();
			JsonArray workflowList = workflowService.getTaskList(workflowType);
			workflowList.forEach(task -> 
				taskList.add(workflowService.createTaskJson(task.getAsJsonObject().get(AssessmentReportAndWorkflowConstants.TASK_ID).getAsInt(), 
						task.getAsJsonObject().get("dependency").getAsInt()))
			);
			JsonObject emailDetailsJson = getEmailDetails(dashboardDetails.get("title").getAsString(), dashboardDetails.get("email").getAsString());
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
			grafanaDashboardConfig.setCreatedDate(InsightsUtils.getCurrentTimeInEpochMilliSeconds());
			id = grafanaDashboardConfigDAL.saveGrafanaDashboardConfig(grafanaDashboardConfig);
			log.debug(id);

	}

	private JsonObject getEmailDetails(String subject, String email) {
		EmailConfiguration emailConfig = ApplicationConfigProvider.getInstance().getEmailConfiguration();
		JsonObject emailDetailsJson = new JsonObject();
		emailDetailsJson.addProperty("senderEmailAddress", emailConfig.getMailFrom());
		emailDetailsJson.addProperty("receiverEmailAddress", email);
		emailDetailsJson.addProperty("mailSubject", subject);
		emailDetailsJson.addProperty("mailBodyTemplate", "");
		emailDetailsJson.addProperty("receiverCCEmailAddress", "");
		emailDetailsJson.addProperty("receiverBCCEmailAddress", "");
		return emailDetailsJson;
	}


}
