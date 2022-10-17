/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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
package com.cognizant.devops.platformservice.test.grafanadashboard;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTask;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowType;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformservice.workflow.service.WorkflowServiceImpl;
import com.google.gson.JsonObject;

public class GrafanaDashboardReportData {

	private static final Logger log = LogManager.getLogger(GrafanaDashboardReportData.class);
	ClassLoader classLoader = ClassLoader.getSystemClassLoader();
	WorkflowServiceImpl workflowService = new WorkflowServiceImpl();
	WorkflowDAL workflowConfigDAL = new WorkflowDAL();
	int taskID = 0;
	int relationTaskID = 0;
	String dashboardJson ="{\"from\":\"now-5m\",\"to\":\"now\",\"title\":\"5-sprint-score-card-updated\",\"source\":\"PLATFORM\",\"pdfType\":[\"Dashboard\"],\"variables\":\"SprintID=S75,from=now-5m,to=now\",\"dashUrl\":\"http://localhost:3000/dashboard/db/5-sprint-score-card-updated?var-SprintID=S75&from=now-5m&to=now\",\"panelUrls\":[\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=31&var-SprintID=S75&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=32&var-SprintID=S75&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=33&var-SprintID=S75&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=34&var-SprintID=S75&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=35&var-SprintID=S75&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=36&var-SprintID=S75&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=37&var-SprintID=S75&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=38&var-SprintID=S75&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=40&var-SprintID=S75&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=16&var-SprintID=S75&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=21&var-SprintID=S75&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=41&var-SprintID=S75&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=44&var-SprintID=S75&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=26&var-SprintID=S75&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=42&var-SprintID=S75&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=29&var-SprintID=S75&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=27&var-SprintID=S75&from=now-5m&to=now\"],\"metadata\":[{\"testDB\":\"false\"}],\"email\":\"demo123@gmail.com\",\"senderEmailAddress\":\"demo123@gmail.com\",\"mailSubject\":\"5-sprint-score-card\",\"mailBodyTemplate\":\"Test body\",\"receiverCCEmailAddress\":\"\",\"receiverBCCEmailAddress\":\"\",\"range\":\"relative\",\"emailBody\":\"\",\"scheduleType\":\"DAILY\",\"organisation\":\"1\",\"dashboard\":\"000000028\",\"rangeText\":\"Last 5 minutes\", \"emailDetails\":{\"senderEmailAddress\":\"demo123@gmail.com\",\"receiverEmailAddress\":\"demo123@gmail.com\",\"mailSubject\":\"demo-dashboard\",\"mailBodyTemplate\":\"demo123@gmail.com\",\"receiverCCEmailAddress\":\"\",\"receiverBCCEmailAddress\":\"\"}}";
    String updateJson = "{\"from\":\"now-5m\",\"to\":\"now\",\"rangeText\":\"Last 5 minutes\",\"id\":\"2975\",\"source\":\"PLATFORM\",\"workflowId\":\"GRAFANADASHBOARDPDFREPORT_1634020809\",\"title\":\"5-sprint-score-card-updated\",\"pdfType\":[\"Dashboard\"],\"variables\":\"SprintID=S76,from=now-5m,to=now\",\"dashUrl\":\"http://localhost:3000/dashboard/db/5-sprint-score-card?var-SprintID=S76&from=now-5m&to=now\",\"panelUrls\":[\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=31&var-SprintID=S76&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=32&var-SprintID=S76&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=33&var-SprintID=S76&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=34&var-SprintID=S76&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=35&var-SprintID=S76&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=36&var-SprintID=S76&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=37&var-SprintID=S76&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=38&var-SprintID=S76&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=40&var-SprintID=S76&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=16&var-SprintID=S76&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=21&var-SprintID=S76&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=41&var-SprintID=S76&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=44&var-SprintID=S76&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=26&var-SprintID=S76&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=42&var-SprintID=S76&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=29&var-SprintID=S76&from=now-5m&to=now\",\"http://localhost:3000/render/d-solo/000000028/5-sprint-score-card?panelId=27&var-SprintID=S76&from=now-5m&to=now\"],\"metadata\":[{\"testDB\":\"false\"}],\"email\":\"demo123@gmail.com\",\"senderEmailAddress\":\"demo123@gmail.com\",\"mailSubject\":\"5-sprint-score-card\",\"mailBodyTemplate\":\"Test body\",\"receiverCCEmailAddress\":\"\",\"receiverBCCEmailAddress\":\"\",\"range\":\"relative\",\"scheduleType\":\"DAILY\",\"organisation\":\"2\",\"dashboard\":\"000000028\",\"edit\":true, \"emailDetails\":{\"senderEmailAddress\":\"demo123@gmail.com\",\"receiverEmailAddress\":\"demo123@gmail.com\",\"mailSubject\":\"org-2-dash-demo\",\"mailBodyTemplate\":\"demo123@gmail.com\",\"receiverCCEmailAddress\":\"\",\"receiverBCCEmailAddress\":\"\"}}";
	void prepareDashboardData() {

		try {
			InsightsWorkflowType type = new InsightsWorkflowType();
			type.setWorkflowType(WorkflowTaskEnum.WorkflowType.GRAFANADASHBOARDPDFREPORT.getValue());
			workflowConfigDAL.saveWorkflowType(type);
		} catch (Exception e) {
			log.error("Error preparing data at WorkflowServiceTest workflowtype record ", e);
		}

		try {
			String workflowTaskTest = "    {\r\n" + 
					"    \"description\":\"GRAFANA_PDF_Execute\",\r\n" + 
					"    \"mqChannel\":\"WORKFLOW.TASK.GRAFANAPDF.EXCECUTION\",\r\n" + 
					"    \"componentName\":\"com.cognizant.devops.platformreports.assessment.core.GrafanaPDFExecutionSubscriber\",\r\n" + 
					"    \"dependency\":\"0\",\r\n" + 
					"    \"workflowType\":\"GRAFANADASHBOARDPDFREPORT\"\r\n" + 
					"    }";
			JsonObject workflowTaskJson = convertStringIntoJson(workflowTaskTest);
			int response = workflowService.saveWorkflowTask(workflowTaskJson);
			InsightsWorkflowTask tasks = workflowConfigDAL
					.getTaskbyTaskDescription(workflowTaskJson.get("description").getAsString());
			taskID = tasks.getTaskId();
		} catch (Exception e) {
			log.error("Error preparing UpshiftAssessmentServiceData task ", e);
		}

		try {
			String workflowTaskTest = "{\r\n" + 
					"   \"description\":\"GRAFANA_PDF_EMAIL_Execute\",\r\n" + 
					"   \"mqChannel\":\"WORKFLOW.TASK.GRAFANAPDFEMAIL.EXCECUTION\",\r\n" + 
					"   \"componentName\":\"com.cognizant.devops.platformreports.assessment.core.GrafanaPDFEmailExecutionSubscriber\",\r\n" + 
					"   \"dependency\":\"1\",\r\n" + 
					"   \"workflowType\":\"GRAFANADASHBOARDPDFREPORT\"\r\n" + 
					"}";
			JsonObject workflowTaskJson = convertStringIntoJson(workflowTaskTest);
			int response = workflowService.saveWorkflowTask(workflowTaskJson);
			InsightsWorkflowTask tasks = workflowConfigDAL
					.getTaskbyTaskDescription(workflowTaskJson.get("description").getAsString());
			relationTaskID = tasks.getTaskId();
		} catch (Exception e) {
			log.error("Error preparing UpshiftReportServiceData KPI task ", e);
		}

	}

	public JsonObject convertStringIntoJson(String convertregisterkpi) {
		JsonObject objectJson = new JsonObject();
		objectJson = JsonUtils.parseStringAsJsonObject(convertregisterkpi);
		return objectJson;
	}
}
