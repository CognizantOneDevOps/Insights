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

public final class StringExpressionConstants {

	public static final String STR_EXP_TASKEXECUTION= "Type=TaskExecution executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}";
	public static final String STR_REGEX = "<[^>]*>";
	public static final String CYPHERQUERY_REGEX ="<\\/[^>]*>|<[^>]*\\/>";
	public static final String STR_EXP_TASKEXECUTION_1="Type=TaskExecution  executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}";
	public static final String STR_EXP_WORKFLOW="Type=WorkFlow ExecutionId={} WorkflowId={} WorkflowType={} TaskDescription={} TaskMQChannel={} status ={} LastRunTime ={} NextRunTime ={} Schedule={} TaskRetry={} isTaskRetry={} processingTime={} message={}";
	public static final String STR_EXP_WORKFLOW_1="Type=WorkFlow ExecutionId={} WorkflowId={} WorkflowType={} TaskDescription={} TaskMQChannel={} status ={} LastRunTime ={} NextRunTime ={} Schedule={} TaskRetryCount={} isTaskRetry={} processingTime={} message={}";
	public static final String STR_EXP_TASKMQ="TaskMQChannel={} WorkflowType={} processingTime={} status ={} message={}" ;
	public static final String STR_EXP_WORKFLOW_2="Type=WorkFlow executionId={} WorkflowId={} LastRunTime={} NextRunTime={} schedule={} isTaskRetry={} TaskRetryCount={} TaskDescription={} ";
	public static final String STR_EXP_EMAIL_EXECUTION="Type=EmailExecution executionId={} workflowId={} ReportName={} mailto={} mailFrom={} ProcessingTime={} message={}";
	public static final String STR_EXP_WORKFLOW_3="Type=WorkFlow ExecutionId={}  WorkflowId={}  WorkflowType={} TaskDescription={} TaskMQChannel={} status ={} LastRunTime ={} NextRunTime ={} Schedule={} TaskRetryCount={} processingTime={} message={}";
	public static final String STR_EXP_WORKFLOW_4="Type=WorkFlow executionId={} workflowId={} LastRunTime={} NextRunTime={} schedule={} isTaskRetry={} ";
	public static final String STR_EXP_TASKRETRYCOUNT="TaskRetryCount={} TaskDescription={} TaskMQChannel={} WorkflowType={} processingTime={} status ={} message={}";
	public static final String STR_EXP_WORKFLOWINITIALIZER="Type=WorkFlowInitializer TaskDescription={} TaskMQChannel={} componentName{} WorkflowType={} processingTime={} status ={} message={}";
	public static final String STR_EXP_TASK = "Type=TaskExecution  executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}";
	public static final String STR_EXP_ALERT_WORKFLOW="Type=AlertWorkFlow executionId={} workflowId={}  AlertName={} trend={} threshold={} count={} ProcessingTime={} message={} ";
	public static final String STR_EXP_ALERT_EMAIL_EXECUTION="Type=AlertEmailExecution executionId={} workflowId={}  AlertName={} mailto={} mailFrom={} ProcessingTime={} message={} ";
	
}
