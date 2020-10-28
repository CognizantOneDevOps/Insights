/*******************************************************************************
 * Copyright 2019 Cognizant Technology Solutions
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

import { Injectable } from '@angular/core';
import { InsightsInitService } from '@insights/common/insights-initservice';

export interface IRestAPIUrlService {
    getRestCallUrl(moduleUrlKey: String): String;
}

@Injectable()
export class RestAPIurlService implements IRestAPIUrlService {
    urlMapping = {};
    apiMap = new Map<String, String>();
    constructor() {
        this.initializeEndpoints();
    }
    public initializeEndpoints() {
        //this.addEndPoint("ABOUT_READ", '/PlatformService/about/read');

        //For Health Check Page
        this.addEndPoint("HEALTH_TOOL", '/PlatformService/admin/health/detailHealth');
        this.addEndPoint("INSIGHTS_COMP_STATUS", '/PlatformService/admin/health/globalHealth');
        this.addEndPoint("AGENT_COMP_STATUS", '/PlatformService/admin/health/globalAgentsHealth');
        this.addEndPoint("AGENTS_FAILURE_DETAILS", '/PlatformService/admin/health/getAgentFailureDetails');

        this.addEndPoint("LOGOUT", '/PlatformService/user/logout');
        this.addEndPoint("GRAPANA_CURRENT_ROLE_ORG", '/PlatformService/user/getCurrentOrgAndRole');
        //this.addEndPoint("ORGS_GET", '/PlatformService/admin/userMgmt/getOrgs');

        this.addEndPoint("USER_AUTHNTICATE", '/PlatformService/user/authenticate');

        this.addEndPoint("COOKIE_GRAFANA", '/PlatformService/user/cookiesForGrafana');


        //Inference & Report Configuration
        this.addEndPoint("SAVE_REPORT", '/PlatformService/insights/report/saveAssessmentReport');
        this.addEndPoint("GET_SCHEDULE", '/PlatformService/insights/report/getSchedule');
        this.addEndPoint("GET_REPORT_TEMPLATE", '/PlatformService/insights/report/getReportTemplate');
        this.addEndPoint("GET_ASSESSMENT_REPORT", '/PlatformService/insights/report/loadAssessmentReport');
        this.addEndPoint("DELETE_ASSESSMENT_REPORT", '/PlatformService/insights/report/deleteAssessmentReport');
        this.addEndPoint("STATE_CHANGE", '/PlatformService/insights/report/updateAssessmentReportState');
        this.addEndPoint("UPDATE_REPORT", '/PlatformService/insights/report/updateAssessmentReport');
        this.addEndPoint("GET_KPI_LIST", '/PlatformService/insights/report/getKPIlistOfReportTemplate');
        this.addEndPoint("GET_TASK_LIST", '/PlatformService/insights/workflow/getTaskList');
        this.addEndPoint("GET_WORKFLOW_EXECUTION_RECORDS", '/PlatformService/insights/workflow/workFlowExecutionRecords');
        this.addEndPoint("SET_REPORT_STATUS", '/PlatformService/insights/report/setReportStatus');
        this.addEndPoint("DOWNLOAD_REPORT_PDF", '/PlatformService/insights/workflow/downloadReportPDF');
        this.addEndPoint("GET_PDF_EXECUTIONID", '/PlatformService/insights/workflow/maxExecutionIDs');

        //Grafana Data Collection
        //this.addEndPoint("ACCESS_GROUP_MANAGEMENT_GET_ORGS", '/PlatformService/accessGrpMgmt/getOrgs');
        this.addEndPoint("ACCESS_GROUP_MANAGEMENT_GET_CURRENT_USER_ORGS", '/PlatformService/accessGrpMgmt/getCurrentUserOrgs');
        this.addEndPoint("ACCESS_GROUP_MANAGEMENT_SWITCH_ORGS", '/PlatformService/accessGrpMgmt/switchUserOrg');
        this.addEndPoint("ACCESS_GROUP_MANAGEMENT_GET_USERS", '/PlatformService/accessGrpMgmt/getUser');
        this.addEndPoint("ACCESS_GROUP_MANAGEMENT_GET_USERS_WITH_ORGS", '/PlatformService/accessGrpMgmt/getCurrentUserWithOrgs');
        this.addEndPoint("ACCESS_GROUP_MANAGEMENT_EDIT_ORGS_UESRS", '/PlatformService/admin/userMgmt/editOrganizationUser');
        this.addEndPoint("ACCESS_GROUP_MANAGEMENT_DELETE_ORGS_UESRS", '/PlatformService/admin/userMgmt/deleteOrganizationUser');
        this.addEndPoint("ACCESS_GROUP_MANAGEMENT_GET_USERS_ORGS", '/PlatformService/accessGrpMgmt/getCurrentOrgRole');
        this.addEndPoint("GET_GRAFANA_VERSION", "/PlatformService/accessGrpMgmt/getGrafanaVersion");
        this.addEndPoint("SEARCH_DASHBOARD", '/PlatformService/accessGrpMgmt/dashboards');
        this.addEndPoint("ORG_USERS_GET", '/PlatformService/admin/userMgmt/getOrgUsers');
        this.addEndPoint("ORG_CREATE", '/PlatformService/admin/userMgmt/createOrg');
        this.addEndPoint("USER_CREATE", '/PlatformService/accessGrpMgmt/addUserInOrg');
        this.addEndPoint("ASSIGN_USER", '/PlatformService/accessGrpMgmt/assignUser');
        this.addEndPoint("USER_ORG_SEARCH", '/PlatformService/accessGrpMgmt/searchUser');
        this.addEndPoint("CURRENT_USER_DETAIL", '/PlatformService/accessGrpMgmt/currentUserDetail');
        this.addEndPoint("SEARCH_DASHBOARD_FOLDERDETAIL", '/PlatformService/accessGrpMgmt/getDashboardsFoldersDetail');
        //Business Mapping module under Admin section
        this.addEndPoint("GET_ALL_HIERARCHY_DETAILS", '/PlatformService/admin/businessmapping/getAllHierarchyDetails');
        this.addEndPoint("GET_HIERARCHY_PROPERTIES", '/PlatformService/admin/businessmapping/getHierarchyProperties');
        this.addEndPoint("SAVE_TOOL_MAPPING", '/PlatformService/admin/businessmapping/saveToolsMapping');
        this.addEndPoint("GET_TOOL_MAPPING", '/PlatformService/admin/businessmapping/getToolsMapping');
        this.addEndPoint("EDIT_TOOL_MAPPING", '/PlatformService/admin/businessmapping/editToolsMapping');
        this.addEndPoint("DELETE_TOOL_MAPPING", '/PlatformService/admin/businessmapping/deleteToolsMapping');
        this.addEndPoint("GET_METADATA", '/PlatformService/admin/hierarchyDetails/getMetaData');

        this.addEndPoint("UPLOAD_IMAGE", '/PlatformService/admin/settings/uploadCustomLogo');
        this.addEndPoint("GET_LOGO_IMAGE", '/PlatformService/settings/getLogoImage');

        //this.addEndPoint("SAVE_DATAPURGING_SETTING", '/PlatformService/admin/settings/saveSettingsConfiguration');
        //this.addEndPoint("LIST_DATAPURGING_SETTING", '/PlatformService/admin/settings/loadSettingsConfiguration');

        //Agent Configuration
        this.addEndPoint("AGENT_REGISTER", '/PlatformService/admin/agentConfiguration/registerAgent');
        this.addEndPoint("AGENT_UPDATE", '/PlatformService/admin/agentConfiguration/updateAgent');
        this.addEndPoint("AGENT_START_STOP", '/PlatformService/admin/agentConfiguration/startStopAgent');
        this.addEndPoint("DOCROOT_AGENT_VERSION_TOOLS", '/PlatformService/admin/agentConfiguration/getSystemAvailableAgentList');
        this.addEndPoint("DOCROOT_AGENT_TOOL_CONFIG_DETAILS", '/PlatformService/admin/agentConfiguration/getToolRawConfigFile');
        this.addEndPoint("DB_AGENTS_LIST", '/PlatformService/admin/agentConfiguration/getRegisteredAgents');
        this.addEndPoint("DB_AGENT_CONFIG_DETAILS", '/PlatformService/admin/agentConfiguration/getRegisteredAgentDetail');
        this.addEndPoint("AGENT_UNINSTALL", '/PlatformService/admin/agentConfiguration/uninstallAgent');
        this.addEndPoint("AGENT_REGISTERV2", '/PlatformService/admin/agentConfiguration/2.0/registerAgent');
        this.addEndPoint("AGENT_UPDATEV2", '/PlatformService/admin/agentConfiguration/2.0/updateAgent');

        //Data Dictonary 
        this.addEndPoint("DATA_DICTIONARY_TOOLS_AND_CATEGORY", '/PlatformService/datadictionary/getToolsAndCategories');
        this.addEndPoint("DATA_DICTIONARY_TOOL_PROPERTIES", '/PlatformService/datadictionary/getToolProperties');
        this.addEndPoint("DATA_DICTIONARY_TOOLS_RELATIONSHIPS", '/PlatformService/datadictionary/getToolsRelationshipAndProperties');
        this.addEndPoint("INSIGHTS_LOG", '/PlatformService/insights/log');

        //Traceablity Dashboard
        this.addEndPoint("GET_DETAILS", '/PlatformService/traceabilitydashboard/getPipeline');
        this.addEndPoint("GET_TOOL_DETAILS", '/PlatformService/traceabilitydashboard/getToolSummary');
        this.addEndPoint("GET_TOOL_LIST", '/PlatformService/traceabilitydashboard/getAvailableTools');
        this.addEndPoint("GET_TOOL_KEYSET", '/PlatformService/traceabilitydashboard/getToolKeyset');

        //Audit Reporting
        this.addEndPoint("GET_ALL_ASSETS", '/PlatformService/traceability/getAllAssets');
        this.addEndPoint("GET_ASSET_INFO", '/PlatformService/traceability/getAssetInfo');
        this.addEndPoint("GET_ASSET_HISTORY", '/PlatformService/traceability/getAssetHistory');
        this.addEndPoint("EXPORT_TO_PDF", '/PlatformService/traceability/getAuditReport');
        this.addEndPoint("GET_PROCESS_JSON", '/PlatformService/traceability/getProcessFlow');

        this.addEndPoint("CREATE_UPDATE_CYPHER_QUERY", '/PlatformService/blockchain/queryBuilder/createQuery');
        this.addEndPoint("FETCH_CYPHER_QUERY", '/PlatformService/blockchain/queryBuilder/fetchQueries');
        this.addEndPoint("DELETE_CYPHER_QUERY", '/PlatformService/blockchain/queryBuilder/deleteQuery');
        this.addEndPoint("UPLOAD_QUERY_FILE", '/PlatformService/blockchain/queryBuilder/uploadFile');
        this.addEndPoint("DOWNLOAD_CYPHER_QUERY", '/PlatformService/blockchain/queryBuilder/getFileContents');
        this.addEndPoint("TEST_QUERY", '/PlatformService/traceability/testQuery');
        this.addEndPoint("CO_RELATIONSHIP_JSON", '/PlatformService/admin/correlationbuilder/getCorrelationJson');
        this.addEndPoint("SAVE_RELATIONSHIP_JSON", '/PlatformService/admin/correlationbuilder/saveConfig');
        this.addEndPoint("UPDATE_RELATIONSHIP", '/PlatformService/admin/correlationbuilder/updateCorrelation');
        this.addEndPoint("DELETE_RELATIONSHIP", '/PlatformService/admin/correlationbuilder/deleteCorrelation');

        //Bulk Upload
        this.addEndPoint("UPLOAD_FILE", '/PlatformService/admin/bulkupload/uploadToolData');
        this.addEndPoint("TOOLNAME_LABELNAME_JSON", '/PlatformService/admin/bulkupload/getToolJson');

        //webhook
        this.addEndPoint("SAVE_DATA_WEBHOOK_CONFIG", '/PlatformService/admin/webhook/saveWebhook');
        this.addEndPoint("LIST_WEBHOOK", '/PlatformService/admin/webhook/loadwebhookConfiguration');
        this.addEndPoint("DELETE_WEBHOOK", '/PlatformService/admin/webhook/uninstallWebHook');
        this.addEndPoint("UPDATE_WEBHOOK", '/PlatformService/admin/webhook/updateWebhook');
        this.addEndPoint("UPDATE_WEBHOOK_STATUS", '/PlatformService/admin/webhook/updateWebhookStatus');

        //SSO API
        this.addEndPoint("SSO_URL", InsightsInitService.singleSignOnConfig.loginURL);
        this.addEndPoint("SSO_DETAIL", '/PlatformService/user/insightsso/getUserDetail');
        this.addEndPoint("SSO_INSIGHTS_URL_LOGOUT", '/PlatformService/user/insightsso/logout');
        this.addEndPoint("SSO_URL_LOGOUT", InsightsInitService.singleSignOnConfig.logoutURL);//
        this.addEndPoint("KERBEROS_LOGIN_URL", "/PlatformService/user/insightsso/authenticateKerberos");
        this.addEndPoint("KERBEROS_USER_DETAIL", '/PlatformService/user/insightsso/getKerberosUserDetail');
        this.addEndPoint("JWT_USER_DETAIL", '/PlatformService/user/insightsso/getJWTUserDetail');

        //data archival
        this.addEndPoint("SAVE_ARCHIVE_DETAILS", '/PlatformService/admin/dataarchival/saveDataArchivalDetails');
        this.addEndPoint("ARCHIVED_DATA_LIST", '/PlatformService/admin/dataarchival/getAllArchivalRecord');
        this.addEndPoint("DELETE_ARCHIVED_DATA", '/PlatformService/admin/dataarchival/deleteArchivedRecord');
        this.addEndPoint("INACTIVATE_RECORD", '/PlatformService/admin/dataarchival/inactivateArchivalRecord');
        this.addEndPoint("ACTIVATE_RECORD", '/PlatformService/admin/dataarchival/activateArchivalRecord');
        this.addEndPoint("ACTIVE_ARCHIVED_DATA_LIST", '/PlatformService/admin/dataarchival/getActiveArchivalRecord');
        this.addEndPoint("UPDATE_DATASOURCE_URL", '/PlatformService/admin/dataarchival/updateArchivalSourceUrl');

         //kpi
         this.addEndPoint("KPI_CATEGORY",'/PlatformService/insights/report/getKpiCategory');
         this.addEndPoint("KPI_DATASOURCE",'/PlatformService/insights/report/getKpiDataSource');
         this.addEndPoint("SAVE_DATA_KPI", '/PlatformService/insights/report/saveKpiDefinition');
         this.addEndPoint("UPDATE_KPI", '/PlatformService/insights/report/updateKpiDefinition');
         this.addEndPoint("LIST_KPI", '/PlatformService/insights/report/getAllActiveKpiList'); 
         this.addEndPoint("DELETE_KPI",'/PlatformService/insights/report/deleteKpiDefinition')
         this.addEndPoint("UPLOAD_BULK_KPI",'/PlatformService/insights/report/saveBulkKpiDefinition')
 
         //content
         this.addEndPoint("LIST_CONTENT",'/PlatformService/insights/report/getAllActiveContentList');
         this.addEndPoint("SAVE_DATA_CONTENT", '/PlatformService/insights/report/saveContentDefinition');
         this.addEndPoint("DELETE_CONTENT",'/PlatformService/insights/report/deleteContentDefinition');
         this.addEndPoint("GET_ACTIONS",'/PlatformService/insights/report/getContentAction');
         this.addEndPoint("UPDATE_CONTENT",'/PlatformService/insights/report/updateContentDefinition');
         this.addEndPoint("UPLOAD_BULK_CONTENT",'/PlatformService/insights/report/saveBulkContentDefinition')

    }

    public addEndPoint(name: String, url: String) {
        if (!this.apiMap.has(name)) {
            this.apiMap.set(name, url);
        } else {
            throw new Error('Url with same name already exists');
        }
    }

    public getRestCallUrl(moduleUrlKey: String) {
        if (!this.apiMap.has(moduleUrlKey)) {
            console.error("Url Mapping doesnt exist "+moduleUrlKey);
            throw new Error("Url Mapping doesnt exist");
        }
        return InsightsInitService.serviceHost.toString().concat(this.apiMap.get(moduleUrlKey).toString());
    }
}