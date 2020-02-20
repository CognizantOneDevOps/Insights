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

        this.addEndPoint("SAVE_DATAPURGING_SETTING", '/PlatformService/admin/settings/saveSettingsConfiguration');
        this.addEndPoint("LIST_DATAPURGING_SETTING", '/PlatformService/admin/settings/loadSettingsConfiguration');

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
        this.addEndPoint("CO_RELATIONSHIP_JSON_NEO4J", '/PlatformService/admin/correlationbuilder/getNeo4jJson');
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

        //SSO API
        this.addEndPoint("SSO_URL", '/PlatformService/saml/login');
        this.addEndPoint("SSO_DETAIL", '/PlatformService/user/insightsso/getUserDetail');
        this.addEndPoint("SSO_INSIGHTS_URL_LOGOUT", '/PlatformService/user/insightsso/logout');
        this.addEndPoint("SSO_URL_LOGOUT", '/PlatformService/saml/logout');//
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
            throw new Error("Url Mapping doesnt exist");
        }
        return InsightsInitService.serviceHost.toString().concat(this.apiMap.get(moduleUrlKey).toString());
    }
}
