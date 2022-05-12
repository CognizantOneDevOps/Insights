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

public interface AssessmentReportAndWorkflowConstants {

	String KPIID = "kpiId";
	String ISACTIVE = "isActive";
	String DATASOURCE = "datasource";
	String FUSION_DATASOURCE= "dataSource";
	String OUTPUTDATASOURCE = "outputDatasource";
	String REPORTNAME = "reportName";
	String STATUS = "status";
	String STARTDATE = "startdate";
	String RUNIMMEDIATE = "runimmediate";
	String RECEIVEREMAILADDRESS = "receiverEmailAddress";
	String RECEIVERCCEMAILADDRESS = "receiverCCEmailAddress";
	String RECEIVERBCCEMAILADDRESS = "receiverBCCEmailAddress";
	String CONTENTID = "contentId";
	String ISREOCCURING = "isReoccuring";
	String EMAILDETAILS = "emailDetails";
	String REPORTID = "reportId";
	String REPORT_CONFIG_DIR = "assessmentReportPdfTemplate";
	String REPORT_CONFIG_TEMPLATE_DIR = "reportTemplates";
	String REPORT_PDF_RESOLVED_PATH = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator
			+ REPORT_CONFIG_DIR + File.separator;
	String REPORT_PDF_EXECUTION_RESOLVED_PATH = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator
			+ REPORT_CONFIG_DIR + File.separator + "executionsDetail" + File.separator;
	String KPICONFIGS = "kpiConfigs";
	String CATEGORY = "category";
	String CONTENT_NAME = "contentName";
	String TASK_DESCRIPTION = "description";
	String WORKFLOW = "Workflow";
	String WORKFLOW_ID = "workflowId";
	// WorkflowDataHandler class constant
	String CURRENTTASKID = "currentTaskId";
	String EXECUTIONID = "executionId";
	String NEXT_TASK_ID = "nextTaskId";
	String CONTENTNAME = "contentName";

	String ACTUALDIRECTION = "actualdirection";
	String BELOW = "Below";
	String ABOVE = "Above";

	String AMBER = "amber";
	String GREEN = "green";
	String RESULT = "result";
	String KEYARRAY = "KeyArray";
	String CONTENT_ARRAY = "contentArray";

	String COLUMNS = "columns";
	String RESULTS = "results";

	String CAPTION = "caption";
	String HTMLEXTENSION = ".html";
	String VALUE = "value";

	String WORKFLOW_EXECUTOR = "WorkflowExecutor";
	String WORKFLOW_RETRY_EXECUTOR = "WorkflowRetryExecutor";
	String WORKFLOW_IMMEDIATE_JOB_EXECUTOR = "WorkflowImmediateJobExecutor";
	String WORKFLOW_AUTOCORRECTION_EXECUTOR = "WorkflowAutoCorrectionExecutor";
	
	String TASK_ID= "taskId";
	String ENDTIME = "endTime";
	String USECASE= "usecase";
	
	String GRAFANAPDF = "GRAFANAPDF";
	String USERNAME = "userName";
	String ORGNAME = "orgName";
	String DASHBOARD = "dashboard";
	String TITLE = "title";
	String VISUALIZATIONCONFIGS = "visualizationConfigs";
	String QUERYTEXT = "queryText";
	String TARGETS = "targets";
	String VTYPE = "vType";
	String VQUERY = "vQuery";
	String DASHBOARDTEMPLATEJSON = "dashboardTemplate.json";
	
	String GRAFANA_PDF_TOKEN_SIGNING_KEY = "insights_Grafana_PDF_Token_string";
	
	Set<String> validReportTemplateFileExtention = Collections
			.unmodifiableSet(new HashSet<String>(Arrays.asList("html", "json", "css", "webp")));
	String ISWOKFLOWTASKRETRY="isWorkflowTaskRetry";
	String WORKFLOW_IMMEDIATE_JOB_EXECUTOR_EXCEPTION="WorkflowEmmediateJobExecutorfailed to execute due to exception ";
	String WORK_FLOW_WITHOUT_HISTORY="WorkflowWithoutHistory";
}
