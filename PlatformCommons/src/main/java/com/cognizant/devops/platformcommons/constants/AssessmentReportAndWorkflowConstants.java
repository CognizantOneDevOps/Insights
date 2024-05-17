/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
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

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class AssessmentReportAndWorkflowConstants {

	public static final String KPIID = "kpiId";
	public static final String ISACTIVE = "isActive";
	public static final String DATASOURCE = "datasource";
	public static final String FUSION_DATASOURCE= "dataSource";
	public static final String OUTPUTDATASOURCE = "outputDatasource";
	public static final String REPORTNAME = "reportName";
	public static final String STATUS = "status";
	public static final String STARTDATE = "startdate";
	public static final String RUNIMMEDIATE = "runimmediate";
	public static final String RECEIVEREMAILADDRESS = "receiverEmailAddress";
	public static final String RECEIVERCCEMAILADDRESS = "receiverCCEmailAddress";
	public static final String RECEIVERBCCEMAILADDRESS = "receiverBCCEmailAddress";
	public static final String CONTENTID = "contentId";
	public static final String ISREOCCURING = "isReoccuring";
	public static final String EMAILDETAILS = "emailDetails";
	public static final String REPORTID = "reportId";
	public static final String REPORT_CONFIG_DIR = "assessmentReportPdfTemplate";
	public static final String REPORT_CONFIG_TEMPLATE_DIR = "reportTemplates";
	public static final String REPORT_PDF_RESOLVED_PATH = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator
			+ REPORT_CONFIG_DIR + File.separator;
	public static final String REPORT_PDF_EXECUTION_RESOLVED_PATH = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator
			+ REPORT_CONFIG_DIR + File.separator + "executionsDetail" + File.separator;
	public static final String KPICONFIGS = "kpiConfigs";
	public static final String CATEGORY = "category";
	public static final String CONTENT_NAME = "contentName";
	public static final String TASK_DESCRIPTION = "description";
	public static final String WORKFLOW = "Workflow";
	public static final String WORKFLOW_ID = "workflowId";
	public static final String AGENT_ID = "agentId";
	// WorkflowDataHandler class constant
	public static final String CURRENTTASKID = "currentTaskId";
	public static final String EXECUTIONID = "executionId";
	public static final String NEXT_TASK_ID = "nextTaskId";
	public static final String CONTENTNAME = "contentName";
	public static final String COMPONENTNAME = "componentName";

	public static final String ACTUALDIRECTION = "actualdirection";
	public static final String BELOW = "Below";
	public static final String ABOVE = "Above";

	public static final String AMBER = "amber";
	public static final String GREEN = "green";
	public static final String RESULT = "result";
	public static final String KEYARRAY = "KeyArray";
	public static final String CONTENT_ARRAY = "contentArray";

	public static final String COLUMNS = "columns";
	public static final String RESULTS = "results";

	public static final String CAPTION = "caption";
	public static final String HTMLEXTENSION = ".html";
	public static final String VALUE = "value";

	public static final String WORKFLOW_EXECUTOR = "WorkflowExecutor";
	public static final String WORKFLOW_RETRY_EXECUTOR = "WorkflowRetryExecutor";
	public static final String WORKFLOW_IMMEDIATE_JOB_EXECUTOR = "WorkflowImmediateJobExecutor";
	public static final String WORKFLOW_AUTOCORRECTION_EXECUTOR = "WorkflowAutoCorrectionExecutor";
	public static final String WORKFLOW_OFFLINE_ALERT_EXECUTOR = "WorkflowOfflineAlertExecutor";
	
	public static final String TASK_ID= "taskId";
	public static final String ENDTIME = "endTime";
	public static final String USECASE= "usecase";
	
	public static final String GRAFANAPDF = "GRAFANAPDF";
	public static final String USERNAME = "userName";
	public static final String ORGNAME = "orgName";
	public static final String DASHBOARD = "dashboard";
	public static final String TITLE = "title";
	public static final String VISUALIZATIONCONFIGS = "visualizationConfigs";
	public static final String QUERYTEXT = "queryText";
	public static final String TARGETS = "targets";
	public static final String VTYPE = "vType";
	public static final String VQUERY = "vQuery";
	public static final String DASHBOARDTEMPLATEJSON = "dashboardTemplate.json";
	
	
	public static final Set<String> validReportTemplateFileExtention = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("html", "json", "css", "webp")));
	public static final String ISWOKFLOWTASKRETRY="isWorkflowTaskRetry";
	public static final String WORKFLOW_IMMEDIATE_JOB_EXECUTOR_EXCEPTION="WorkflowEmmediateJobExecutorfailed to execute due to exception ";
	public static final String WORK_FLOW_WITHOUT_HISTORY="WorkflowWithoutHistory";
	public static final String SAFESTRING = "SafeString";

}
