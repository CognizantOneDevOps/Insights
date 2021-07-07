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
package com.cognizant.devops.platformservice.traceabilitydashboard.constants;

public final class TraceabilityConstants {
	
	public static final Long PIPELINE_CACHE_EXPIRY_IN_SEC =1200l ;
	public static final Long MASTER_CACHE_EXPIRY_IN_SEC=1360l;
	public static final Long PIPELINE_CACHE_HEAP_SIZE_BYTES=1000000l;
	public static final Long MASTER_CACHE_HEAP_SIZE_BYTES=10000000l;
	public static final String DATAMODEL_FILE_NAME ="datamodel.json";
	public static final String DATAMODEL_FOLDER_NAME=".InSights";
	public static final String ENV_VAR_NAME="INSIGHTS_HOME";
	public static final String RESULTS = "results";
	public static final String ORDER = "order";
	public static final String MESSAGES = "messages";
	public static final String MESSAGE= "Message";
	public static final String DISPLAYPROPSTEXT = "displayText";
	public static final String EXCLUDE_LABEL_PROPERTY = "excludeLabels";
	public static final String CATEGORY = "category";
	public static final String TIME_LAG = "timelag";
	public static final String SUMMARY = "summary";
	public static final String PIPELINE="pipeline";
	public static final String METADATA = "metaData";
	public static final String DISPLAY_PROPS_TEXT = "displayTextProperty";
	public static final String TOOL_NAME = "toolName";
	public static final String RECORD_COUNT = "recordCount";
	public static final String COMBINED_SUMMARY ="combinedSummary";
	public static final String COUNT = "count";
	public static final String OTHER_PIPELINE = "Other";
	public static final String ISSUE_TYPE = "issueType";
	public static final String PROPERTY = "property";
}
