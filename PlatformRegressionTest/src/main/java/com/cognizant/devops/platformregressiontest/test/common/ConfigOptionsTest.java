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
package com.cognizant.devops.platformregressiontest.test.common;
public interface ConfigOptionsTest {
	String PROP_FILE = "regression_test.properties";
	String TRACEABILITY_JSON_FILE = "traceability.json";
	String AGENT_JSON_FILE = "agentTestData.json";
	String AGENT_ONDEMAND_JSON_FILE = "agentOnDemandTestData.json";
	String AGENT_RELEASE_JSON_FILE = "agentReleaseData.json";
	String AGENT_DUMMY_DATA_JSON_FILE = "dummyDataAgent.json";
	String LOGIN_JSON_FILE = "login.json";
	String AGENT_OFFLINE_JSON_FILE = "agentOfflineTestData.json";
	String REPORT_JSON_FILE = "reportTestData.json";
	String REPORT_CONFIGURATION_FILE="ReportConfiguration.xlsx";
	String BULKUPLOAD_JSON_FILE = "bulkUpload_test_data.json";
	String CORRELATION_JSON_FILE = "correlationTestData.json";
	String EVENTCONFIG_JSON_FILE = "eventConfigWebhook.json";
	String HEALTHCHECK_JSON_FILE = "healthCheckTestData.json";
	String SERVERCONFIGURATION_JSON_FILE = "serverConfiguration.json";
	String DATADICTIONARY_JSON_FILE = "dataDictionary.json";
	String GROUP_JSON_FILE = "groupTestData.json";
	String ADD_CONFIG_FILES = "addConfigFiles.json";
	String ARCHIVAL_JSON_FILE = "dataArchival.json";
	String LOGO_JSON_FILE = "logoSetting.json";
	String WEBHOOK_JSON_FILE = "webhook.json";
	String CONFIGURATION_JSON_FILE = "configurationFileManagementTestData.json";
	String DYNAMICRESPONSE_JSON_FILE = "dynamicTemplateWebhook.json";
	String TESTDATA_FILE = "Test_Data.xlsx";
	String INSIGHTS_HOME = "INSIGHTS_HOME";
	String CONFIG_DIR = ".InSights";
	String AUTO_DIR = "Automation";
	String ENGINE_AUTO_DIR ="engineAutomation";
	String AGENT_DIR = "agent_management";
	String LOGIN_DIR = "login";
	String TRACEABILITY_DASHBOARD_DIR = "trceability_dashboard";
	String HEALTH_CHECK_DIR = "health_check";
	String SERVERCONFIGURATION_DIR = "server_configuration";
	String DATADICTIONARY_DIR = "data_dictionary";
	String LOGOSETTING_DIR = "logo_setting";
	String BULKUPLOAD_DIR = "bulk_upload";
	String WEBHOOK_CONFIG_DIR = "webhook_configuration";
	String CONFIGURATION_FILE_DIR = "configuration_file_management";
	String GROUPS_AND_USERS_DIR = "groups_and_user";
	String CORRELATION_BUILDER_DIR = "correlation_builder";
	String REPORT_MANAGEMENT_DIR = "report_management";
	String CHROME_DIR = "chromedriver_win32";
	String DRIVER_FILE = "chromedriver.exe";
	String TESTNG_FILE = "testng.xml";
	String AUTH_HEADER_KEY = "authorization";
	String CSRF_NAME_KEY = "XSRF-TOKEN";
	String SESSION_ID_KEY = "JSESSIONID";
	String GRAFANA_COOKIES_ORG = "grafanaOrg";
	String GRAFANA_COOKIES_ROLE = "grafanaRole";
	String CONTENT_HEADER_KEY = "Content-Type";
	String CONTENT_TYPE_VALUE = "application/json";
	String REPORT_CONFIG_FOLDER="reportConfig";
}