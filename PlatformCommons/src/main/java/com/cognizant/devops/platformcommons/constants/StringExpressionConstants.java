/*******************************************************************************
* Copyright 2021 Cognizant Technology Solutions
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
package com.cognizant.devops.platformcommons.constants;
public interface StringExpressionConstants {

	String STR_EXP_TASKEXECUTION= "Type=TaskExecution executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}";
	String STR_REGEX = "<[^>]*>";
	String STR_EXP_TASKEXECUTION_1="Type=TaskExecution  executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}";
	String STR_EXP_WORKFLOW="Type=WorkFlow ExecutionId={} WorkflowId={} WorkflowType={} TaskDescription={} TaskMQChannel={} status ={} LastRunTime ={} NextRunTime ={} Schedule={} TaskRetry={} isTaskRetry={} processingTime={} message={}";
	String STR_EXP_WORKFLOW_1="Type=WorkFlow ExecutionId={} WorkflowId={} WorkflowType={} TaskDescription={} TaskMQChannel={} status ={} LastRunTime ={} NextRunTime ={} Schedule={} TaskRetryCount={} isTaskRetry={} processingTime={} message={}";
	String STR_EXP_TASKMQ="TaskMQChannel={} WorkflowType={} processingTime={} status ={} message={}" ;
	String STR_EXP_WORKFLOW_2="Type=WorkFlow executionId={} WorkflowId={} LastRunTime={} NextRunTime={} schedule={} isTaskRetry={} TaskRetryCount={} TaskDescription={} ";
	String STR_EXP_EMAIL_EXECUTION="Type=EmailExecution executionId={} workflowId={} ReportName={} mailto={} mailFrom={} ProcessingTime={} message={}";
	String STR_EXP_WORKFLOW_3="Type=WorkFlow ExecutionId={}  WorkflowId={}  WorkflowType={} TaskDescription={} TaskMQChannel={} status ={} LastRunTime ={} NextRunTime ={} Schedule={} TaskRetryCount={} processingTime={} message={}";
	String STR_EXP_WORKFLOW_4="Type=WorkFlow executionId={} workflowId={} LastRunTime={} NextRunTime={} schedule={} isTaskRetry={} ";
	String STR_EXP_TASKRETRYCOUNT="TaskRetryCount={} TaskDescription={} TaskMQChannel={} WorkflowType={} processingTime={} status ={} message={}";
	String STR_EXP_WORKFLOWINITIALIZER="Type=WorkFlowInitializer TaskDescription={} TaskMQChannel={} componentName{} WorkflowType={} processingTime={} status ={} message={}";
	String STR_EXP_TASK = "Type=TaskExecution  executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}";

}
