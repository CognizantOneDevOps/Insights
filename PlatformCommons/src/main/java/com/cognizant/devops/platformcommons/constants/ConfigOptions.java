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
package com.cognizant.devops.platformcommons.constants;

import java.io.File;

public final class ConfigOptions {
	public static final String  CONFIG_DIR = ".InSights";
	public static final String  CONFIG_FILE = "server-config.json";
	public static final String  TOOLS_CONFIG_FILE = "toolsConfig.json";
	public static final String  CORRELATION_TEMPLATE = "correlation.json";
	public static final String  TOOLDETAIL_TEMPLATE = "toolDetail.json";
	public static final String  CORRELATION = "correlation" + System.currentTimeMillis() + ".json";
	public static final String  DATA_ENRICHMENT_TEMPLATE = "data-enrichment.json";
	public static final String  BLOCKCHAIN_CONFIG_FILE = "connections-tls.json";
	public static final String  ENDPOINT_DATA = "endpointData";
	public static final String  USER_DATA = "userData";
	public static final String  PROPERTY_USER_HOME = "user.home";
	public static final String  INSIGHTS_HOME = "INSIGHTS_HOME";
	public static final String  TRANSFORMATION = PlatformServiceConstants.TRANSFORMATION_DECODED;
	public static final String  SP = PlatformServiceConstants.SP_DECODED;
	public static final String  RSA = PlatformServiceConstants.RSA_DECODED;
	public static final String  FILE_SEPERATOR = File.separator;
	public static final String  CONFIG_FILE_RESOLVED_PATH = System.getenv().get(INSIGHTS_HOME) + FILE_SEPERATOR + ConfigOptions.CONFIG_DIR
			+ FILE_SEPERATOR + ConfigOptions.CONFIG_FILE;
	public static final String  BLOCKCHAIN_CONFIG_FILE_RESOLVED_PATH = System.getenv().get(INSIGHTS_HOME) + FILE_SEPERATOR
			+ ConfigOptions.CONFIG_DIR + FILE_SEPERATOR + ConfigOptions.BLOCKCHAIN_CONFIG_FILE;
	public static final String  CORRELATION_FILE_RESOLVED_PATH = System.getenv().get(INSIGHTS_HOME) + FILE_SEPERATOR
			+ ConfigOptions.CONFIG_DIR + FILE_SEPERATOR + ConfigOptions.CORRELATION_TEMPLATE;
	public static final String  TOOLS_CONFIG_FILE_RESOLVED_PATH = System.getProperty(ConfigOptions.PROPERTY_USER_HOME) + FILE_SEPERATOR
			+ ConfigOptions.CONFIG_DIR + FILE_SEPERATOR + ConfigOptions.TOOLS_CONFIG_FILE;
	public static final String  DATA_ENRICHMENT_FILE_RESOLVED_PATH = System.getenv().get(INSIGHTS_HOME) + FILE_SEPERATOR
			+ ConfigOptions.CONFIG_DIR + FILE_SEPERATOR + ConfigOptions.DATA_ENRICHMENT_TEMPLATE;
	public static final String  FAILURE_RESPONSE = "FAILURE";
	public static final String  SUCCESS_RESPONSE = "SUCCESS";
	public static final String  CONFIG_FILE_DIR = System.getenv().get(INSIGHTS_HOME) + FILE_SEPERATOR + ConfigOptions.CONFIG_DIR;
	public static final String  TRANSFORMATION_ENCODED = "UlNBL05PTkUvTm9QYWRkaW5n";
	public static final String  SP_ENCODED = "QkM=";
	public static final String  RSA_ENCODED = "UlNB";
	public static final String  CLIENT_NAME = "clientName";
	public static final String  CLIENT_ADDRESS = "address";
	public static final String  MAC_ADDRESS = "macAddress";
	public static final String  VALID_TILL = "validTill";
	public static final String  DATE_FORMAT = "MM/dd/yyyy";
	public static final String  DATAPURGING_SETTINGS_TYPE = "DATAPURGING";
	public static final String  ROW_LIMIT = "rowLimit";
	public static final String  BACKUP_FILE_LOCATION = "backupFileLocation";
	public static final String  BACKUP_FILE_NAME = "backupFileName";
	public static final String  BACKUP_DURATION_IN_DAYS = "backupRetentionInDays";
	public static final String  DATA_ARCHIVAL_FREQUENCY = "dataArchivalFrequency";
	public static final String  LAST_RUN_TIME = "lastRunTime";
	public static final String  NEXT_RUN_TIME = "nextRunTime";
	public static final String  BACKUP_FILE_FORMAT = "backupFileFormat";
	public static final String  CSV_FORMAT = "CSV";
	public static final String  JSON_FORMAT = "JSON";
	public static final String  OFFLINE_DATA_PROCESSING_FOLDER = "data-enrichment";
	public static final String  QUERY_DATA_PROCESSING_FOLDER = "Audit-report";
	public static final String  OFFLINE_DATA_PROCESSING_RESOLVED_PATH = System.getenv().get(INSIGHTS_HOME) + FILE_SEPERATOR
			+ ConfigOptions.CONFIG_DIR + FILE_SEPERATOR + ConfigOptions.OFFLINE_DATA_PROCESSING_FOLDER;
	public static final String  QUERY_DATA_PROCESSING_RESOLVED_PATH = System.getenv().get(INSIGHTS_HOME) + FILE_SEPERATOR
			+ ConfigOptions.CONFIG_DIR + FILE_SEPERATOR + ConfigOptions.QUERY_DATA_PROCESSING_FOLDER;

	public static final String  ML_DIRECTORY = "MLData";
	public static final String  ML_DATA_STORAGE_RESOLVED_PATH = System.getenv().get(INSIGHTS_HOME) + FILE_SEPERATOR + ML_DIRECTORY;
	public static final String  ONLINE_REGISTRATION_MODE_DOCROOT = "docroot";
	public static final String  ONLINE_REGISTRATION_MODE_NEXUS = "nexus";
	public static final String  GRAPH = "graph";
	public static final String  AUTHORIZATION = "Authorization";
	public static final String  RESULTDATACONTENTS = "resultDataContents";
	public static final String  STATEMENT = "statement";
	public static final String  STATEMENTS = "statements";
	public static final String  RESULTS= "results";
	public static final String  NODES="nodes";
	public static final String  RELATIONSHIPS= "relationships";
	public static final String  CRLF_PATTERN = "(\r\n|\r|\n|\n\r)";
	public static final String  NBSP = "&nbsp;";
	public static final String  AMP = "&amp;";	
	public static final String  CONTENT_NAME = "ContentName :";
	public static final String  CONTENT_RESULT = "ContentResult :";
	public static final String  ACTION = "action :";
	public static final String  CONTENT_ID = "ContentId :";

}
