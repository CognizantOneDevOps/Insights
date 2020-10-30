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

public interface AssessmentReportAndWorkflowConstants {

	static final String KPIID = "kpiId";
	static final String ISACTIVE = "isActive";
	static final String DATASOURCE = "datasource";
	static final String REPORTNAME = "reportName";
	static final String STATUS = "status";
	static final String STARTDATE = "startdate";
	static final String RUNIMMEDIATE = "runimmediate";
	static final String RECEIVEREMAILADDRESS = "receiverEmailAddress";
	static final String RECEIVERCCEMAILADDRESS = "receiverCCEmailAddress";
	static final String RECEIVERBCCEMAILADDRESS = "receiverBCCEmailAddress";
	static final String CONTENTID = "contentId";
	static final String ISREOCCURING = "isReoccuring";
	static final String EMAILDETAILS = "emailDetails";
	static final String REPORTID = "reportId";
	static final String REPORT_CONFIG_DIR = "assessmentReportPdfTemplate";
	static final String REPORT_CONFIG_TEMPLATE_DIR = "reportTemplates";
	static final String REPORT_PDF_RESOLVED_PATH = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator
	+ REPORT_CONFIG_DIR + File.separator;
	static final String REPORT_PDF_EXECUTION_RESOLVED_PATH = System.getenv().get(ConfigOptions.INSIGHTS_HOME)
			+ File.separator
	+ REPORT_CONFIG_DIR + File.separator + "executionsDetail" + File.separator;
	static final String KPICONFIGS = "kpiConfigs";
	static final String CATEGORY = "category";
	static final String CONTENT_NAME = "contentName";
	static final String TASK_DESCRIPTION = "description";
}
