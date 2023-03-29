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

import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowExecutionHistory;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.google.gson.JsonObject;

public class WorkflowServiceTestData extends AbstractTestNGSpringContextTests{

	WorkflowDAL workflowConfigDAL = new WorkflowDAL();
	int taskID = 0;
	int emailTaskID = 0;
	int assessmentId = 0;
	int systemHealthTaskId = 0;
	int systemEmailTaskId = 0;
	int workflowTypeId = 0;
	String workflowId = null;
	String system_workflowId = "SYSTEM_HealthNotification";
	String workflowType = "Report";
	String incorrectWorkflowType = "Reportt";
	String updateWorkflowTaskData = "";
	String updateWorkflowTaskDataValidation="";
	String workflowTaskDataUpdateException = "";
	public JsonObject dailyAssessmentReportWorkflowJson=null;
	int historyid = 0;
	int reportId = 0;

	public void getWorkflowTaskData(int TaskId)
	{
		this.updateWorkflowTaskData = "{\"taskId\":" + TaskId+ ",\"description\": \"demo\",\"mqChannel\": \"WORKFLOW.WORKFLOWSERVICE_TEST.TASK.KPI.EXCECUTION\",\"componentName\": \"com.cognizant.devops.platformreports.assessment.core.ReportKPISubscriber\",\"dependency\": \"100\",\"workflowType\": \"Report\"}";
		this.updateWorkflowTaskDataValidation = "&amp;{<\"taskId\":" + TaskId+ ",\"description\": \"demo\",\"mqChannel\": \"WORKFLOW.WORKFLOWSERVICE_TEST.TASK.KPI.EXCECUTION\",\"componentName\": \"com.cognizant.devops.platformreports.assessment.core.ReportKPISubscriber\",\"dependency\": \"100\",\"workflowType\": \"Report\"}";
		this.workflowTaskDataUpdateException = "{\"taskId\":" + (TaskId+123)+ ",\"description\": \"demo\",\"mqChannel\": \"WORKFLOW.WORKFLOWSERVICE_TEST.TASK.KPI.EXCECUTION\",\"componentName\": \"com.cognizant.devops.platformreports.assessment.core.ReportKPISubscriber\",\"dependency\": \"100\",\"workflowType\": \"Report\"}";
		
	}
	
	public void setReportId(int reportId) {
		this.reportId=reportId;
	}
	
	public JsonObject convertStringIntoJson(String convertregisterkpi) {
		JsonObject objectJson = new JsonObject();
		objectJson = JsonUtils.parseStringAsJsonObject(convertregisterkpi);
		return objectJson;
	}

	public void SetInfo(int task, int emailTaskID)
	{
		this.taskID = task;
		this.emailTaskID=emailTaskID;
		String dailyAssessmentReportWorkflow = "{\"reportName\":\"Daily_Deployment_Workflow\",\"reportTemplate\":"+this.reportId+",\"emailList\":\"fdfsfsdfs\",\"schedule\":\"DAILY\",\"startdate\":null,\"isReoccuring\":true,\"datasource\":\"\",\"tasklist\":[{\"taskId\":"+taskID+",\"sequence\":0}],\"asseementreportdisplayname\":\"Report_test\",\"emailDetails\":null,\"orgName\":\"Test Org\",\"userName\":\"Test_User\"}";
		dailyAssessmentReportWorkflowJson = convertStringIntoJson(dailyAssessmentReportWorkflow);

	}
	
	public int updateWorkflowExecutionHistory(String workflowId, int taskId, int assessmentId) {
		this.workflowId=workflowId;
		this.assessmentId=assessmentId;
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
