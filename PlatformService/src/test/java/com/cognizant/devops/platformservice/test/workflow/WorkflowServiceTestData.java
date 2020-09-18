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
package com.cognizant.devops.platformservice.test.workflow;

import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowExecutionHistory;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WorkflowServiceTestData {

	WorkflowDAL workflowConfigDAL = new WorkflowDAL();

	JsonParser parser = new JsonParser();
	String workflowType = "Report";

	int historyid = 0;

	String registerkpiWorkflow = "{\"kpiId\":123456,\"name\":\"Total Successful Deployments_workflow\",\"group\":\"DEPLOYMENT\",\"toolName\":\"RUNDECK\",\"category\":\"STANDARD\",\"DBQuery\":\"MATCH (n:RUNDECK:DATA) WHERE n.SPKstartTime > {startTime} and n.SPKstartTime < {endTime} and  n.SPKvector = 'DEPLOYMENT' and n.SPKstatus='Success' RETURN count(n.SPKstatus) as totalDeploymentCount\",\"datasource\":\"NEO4J\",\"isActive\":true,\"resultField\":\"totalDeploymentCount\"}";
	public JsonObject registerkpiWorkflowJson = convertStringIntoJson(registerkpiWorkflow);

	String reportTemplateWorkflow = "{\"reportId\":\"123456\",\"reportName\":\"Fail Report_workflow\",\"description\":\"Testing\",\"isActive\":true,\"file\":\"File.json\",\"visualizationutil\":\"Fusion\",\"kpiConfigs\":[{\"kpiId\":123456,\"visualizationConfigs\":[{\"vId\":\"100\",\"vQuery\":\"Query\"}]}]}";
	public JsonObject reportTemplateWorkflowJson = convertStringIntoJson(reportTemplateWorkflow);

	String workflowTaskData = "{\"description\": \"KPI_Execute_Workflow_test\",\"mqChannel\": \"WORKFLOW.WORKFLOWSERVICE_TEST.TASK.KPI.EXCECUTION\",\"componentName\": \"com.cognizant.devops.platformreports.assessment.core.ReportKPISubscriber\",\"dependency\": \"100\",\"workflowType\": \"Report\"}";
	public JsonObject workflowTaskAsJson = convertStringIntoJson(workflowTaskData);

	String workflowEmailTaskData = "{\"description\": \"Email_Execute_Workflow_test\",\"mqChannel\": \"WORKFLOW.WORKFLOWSERVICE_TEST.TASK.EMAIL.EXCECUTION\",\"componentName\": \"com.cognizant.devops.platformreports.assessment.core.ReportEmailSubscriber\",\"dependency\": \"100\",\"workflowType\": \"Report\"}";
	public JsonObject workflowEmailTaskAsJson = convertStringIntoJson(workflowEmailTaskData);

	String incorrectWorkflowTask = "{\"description\": \"KPI_Execute_Workflow_test\",\"componentName\": \"com.cognizant.devops.platforminsights.workflowtask.message.tasksubscribers.ReportKPISubscriber\",\"dependency\": \"100\",\"workflowType\": \"Report\"}";

	public JsonObject incorrectWorkflowTaskJson = convertStringIntoJson(incorrectWorkflowTask);

	public JsonObject convertStringIntoJson(String convertregisterkpi) {
		JsonObject objectJson = new JsonObject();
		objectJson = parser.parse(convertregisterkpi).getAsJsonObject();
		return objectJson;
	}

	public int updateWorkflowExecutionHistory(String workflowId, int taskId) {
		InsightsWorkflowConfiguration workflowConfig = workflowConfigDAL.getWorkflowConfigByWorkflowId(workflowId);
		InsightsWorkflowExecutionHistory historyConfig = new InsightsWorkflowExecutionHistory();
		historyConfig.setCurrenttask(taskId);
		historyConfig.setExecutionId(System.currentTimeMillis());
		historyConfig.setWorkflowConfig(workflowConfig);
		historyConfig.setTaskStatus(WorkflowTaskEnum.WorkflowStatus.NOT_STARTED.toString());
		historyConfig.setStatusLog("Testing");
		int historyId = workflowConfigDAL.saveTaskworkflowExecutionHistory(historyConfig);
		return historyId;
	}
}
