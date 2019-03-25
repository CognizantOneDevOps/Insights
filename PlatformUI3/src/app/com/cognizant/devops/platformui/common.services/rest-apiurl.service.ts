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

        this.addEndPoint("LOGOUT", '/PlatformService/user/logout');
        this.addEndPoint("GRAPANA_CURRENT_ROLE_ORG", '/PlatformService/user/getCurrentOrgAndRole');
        this.addEndPoint("ORGS_GET", '/PlatformService/admin/userMgmt/getOrgs');

        this.addEndPoint("USER_AUTHNTICATE", '/PlatformService/user/authenticate');
        //this.addEndPoint("USER_GET", '/PlatformService/admin/userMgmt/getUser');
        //this.addEndPoint("CURRENT_USER_ORG", '/PlatformService/admin/userMgmt/getCurrentUserOrgs');
        //this.addEndPoint("SWITCH_USER_ORG", '/PlatformService/admin/userMgmt/switchUserOrg');
        //this.addEndPoint("USER_SEARCH", '/PlatformService/user/search');

        //this.addEndPoint("ENTITY_DEFINITION_ADD", '/PlatformService/admin/dataTagging/addEntityDefinition');
        //this.addEndPoint("ENTITY_DEFINITION_REMOVE", '/PlatformService/admin/dataTagging/removeEntityDefinition');
        //this.addEndPoint("ENTITY_DEFINITION_ALL", '/PlatformService/admin/dataTagging/fetchAllEntityDefinition');
        //this.addEndPoint("ENTITY_BY_LEVEL", '/ALL_latformService/admin/dataTagging/fetchEntityDataByLevel');
        //this.addEndPoint("ENTITY_DATA_ALL", '/PlatformService/admin/hierarchyDetails/fetchAllHierarchyDetails');

        //this.addEndPoint("HIERARCHY_DETAILS_ADD", '/PlatformService/admin/hierarchyDetails/addHierarchyDetails');
        //this.addEndPoint("HIERARCHY_DETAILS_REMOVE", '/PlatformService/admin/hierarchyDetails/removeHierarchyDetails');
        //this.addEndPoint("HIERARCHY_DETAILS_GET", '/PlatformService/admin/hierarchyDetails/getHierarchyDetails');
        //this.addEndPoint("HIERARCHY_DATA_ALL", '/PlatformService/admin/dataTagging/fetchAllHierarchyMapping');

        //this.addEndPoint("HIERARCHY_MAPPING_ADD", '/PlatformService/admin/dataTagging/addHierarchyMapping');
        //this.addEndPoint("HIERARCHY_MAPPING", '/PlatformService/admin/dataTagging/fetchHierarchyMapping');
        //this.addEndPoint("HIERARCHY_MAPPING_REMOVE", '/PlatformService/admin/dataTagging/removeHierarchyMapping');

        this.addEndPoint("SEARCH_DASHBOARD", '/PlatformService/search/dashboards');

        //this.addEndPoint("DB_DATA", '/PlatformService/db/data');

        //this.addEndPoint("PROJECT_MAPPING_REMOVE", '/PlatformService/admin/data/removeProjectMapping');
        //this.addEndPoint("PROJECT_MAPPING_ADD", '/PlatformService/admin/data/addProjectMapping');
        //this.addEndPoint("PROJECT_MAPPING_BY_HIERARCHY", '/PlatformService/admin/data/fetchProjectMappingByHierarchy');
        //this.addEndPoint("PROJECT_MAPPING_BY_ORGID", '/PlatformService/admin/data/fetchProjectMappingByOrgId');

        //this.addEndPoint("TOOL_MAPPING_DELETE", '/PlatformService/admin/data/deleteToolMapping');
        //this.addEndPoint("TOOL_NAME_GET", '/PlatformService/admin/mappingdata/tools');
        //this.addEndPoint("MAPPING_DATA", '/PlatformService/admin/mappingdata/toolsField');
        //this.addEndPoint("MAPPING_FIELD_VAL", '/PlatformService/admin/mappingdata/toolsFieldValue');
        //this.addEndPoint("TOOL_CATEGORY", '/PlatformService/admin/mappingdata/toolsCategory');
        //this.addEndPoint("PROJECT_MAPPING", '/PlatformService/admin/data/fetchAllProjectMapping');
        //this.addEndPoint("DISTINCT_HIERARCHY", '/PlatformService/admin/hierarchyDetails/fetchDistinctHierarchyName');

        //this.addEndPoint("TOOL_DATA_READ", '/PlatformService/admin/tools/read');
        //this.addEndPoint("TOOL_CONFIG_SAVE", '/PlatformService/admin/toolsConfig/update');
        //this.addEndPoint("TOOL_CONFIG_READ", '/PlatformService/admin/toolsConfig/read');
        //this.addEndPoint("AGENT_CONFIG_DOWNLOAD", '/PlatformService/admin/toolsConfig/download');
        //this.addEndPoint("TOOL_CONFIG_DELETE", '/PlatformService/admin/toolsConfig/delete');
        //this.addEndPoint("TOOL_LAYOUT_READ", '/PlatformService/admin/toollayout/read');
        //this.addEndPoint("ALL_TOOLS_CONFIGURATION_READ", '/PlatformService/admin/toolsConfig/readAll');

        this.addEndPoint("ORG_USERS_GET", '/PlatformService/admin/userMgmt/getOrgUsers');
        //this.addEndPoint("USER_ADD", '/PlatformService/admin/userMgmt/addUser');
        //this.addEndPoint("ALL_USERS", '/PlatformService/admin/userMgmt/getAllUsers');
        //this.addEndPoint("USER_TO_ORG_ADD", '/PlatformService/admin/userMgmt/addUserToOrg');
        this.addEndPoint("ORG_CREATE", '/PlatformService/admin/userMgmt/createOrg');
        //this.addEndPoint("USER_ORG_DELETE", '/PlatformService/admin/userMgmt/removeUserFromOrg');
        //this.addEndPoint("USER_ROLE_INORG_UPDATE", '/PlatformService/admin/userMgmt/updateUserRoleInOrg');

        this.addEndPoint("ACCESS_GROUP_MANAGEMENT_GET_ORGS", '/PlatformService/accessGrpMgmt/getOrgs');
        this.addEndPoint("ACCESS_GROUP_MANAGEMENT_GET_CURRENT_USER_ORGS", '/PlatformService/accessGrpMgmt/getCurrentUserOrgs');
        this.addEndPoint("ACCESS_GROUP_MANAGEMENT_SWITCH_ORGS", '/PlatformService/accessGrpMgmt/switchUserOrg');
        this.addEndPoint("ACCESS_GROUP_MANAGEMENT_GET_USERS", '/PlatformService/accessGrpMgmt/getUser');
        this.addEndPoint("ACCESS_GROUP_MANAGEMENT_EDIT_ORGS_UESRS", '/PlatformService/admin/userMgmt/editOrganizationUser');
        this.addEndPoint("ACCESS_GROUP_MANAGEMENT_DELETE_ORGS_UESRS", '/PlatformService/admin/userMgmt/deleteOrganizationUser');

        this.addEndPoint("GET_GRAFANA_VERSION", "/PlatformService/accessGrpMgmt/getGrafanaVersion");

        //Business Mapping module under Admin section
        this.addEndPoint("GET_ALL_HIERARCHY_DETAILS", '/PlatformService/admin/businessmapping/getAllHierarchyDetails');
        this.addEndPoint("GET_HIERARCHY_PROPERTIES", '/PlatformService/admin/businessmapping/getHierarchyProperties');
        this.addEndPoint("SAVE_TOOL_MAPPING", '/PlatformService/admin/businessmapping/saveToolsMapping');
        this.addEndPoint("GET_TOOL_MAPPING", '/PlatformService/admin/businessmapping/getToolsMapping');
        this.addEndPoint("EDIT_TOOL_MAPPING", '/PlatformService/admin/businessmapping/editToolsMapping');
        this.addEndPoint("DELETE_TOOL_MAPPING", '/PlatformService/admin/businessmapping/deleteToolsMapping');
        //this.addEndPoint("UPLOAD_HIERARCHY_DETAILS", '/PlatformService/admin/hierarchyDetails/uploadHierarchyDetails');
        this.addEndPoint("GET_METADATA", '/PlatformService/admin/hierarchyDetails/getMetaData');


        //this.addEndPoint("INSIGHTS_GET", '/PlatformService/insights/inferences');

        this.addEndPoint("UPLOAD_IMAGE", '/PlatformService/admin/settings/uploadCustomLogo');
        this.addEndPoint("GET_LOGO_IMAGE", '/PlatformService/settings/getLogoImage');

        this.addEndPoint("SAVE_DATAPURGING_SETTING", '/PlatformService/admin/settings/saveSettingsConfiguration');
        this.addEndPoint("LIST_DATAPURGING_SETTING", '/PlatformService/admin/settings/loadSettingsConfiguration');

        this.addEndPoint("AGENT_REGISTER", '/PlatformService/admin/agentConfiguration/registerAgent');
        this.addEndPoint("AGENT_UPDATE", '/PlatformService/admin/agentConfiguration/updateAgent');
        this.addEndPoint("AGENT_START_STOP", '/PlatformService/admin/agentConfiguration/startStopAgent');
        this.addEndPoint("DOCROOT_AGENT_VERSION_TOOLS", '/PlatformService/admin/agentConfiguration/getSystemAvailableAgentList');
        this.addEndPoint("DOCROOT_AGENT_TOOL_CONFIG_DETAILS", '/PlatformService/admin/agentConfiguration/getToolRawConfigFile');
        this.addEndPoint("DB_AGENTS_LIST", '/PlatformService/admin/agentConfiguration/getRegisteredAgents');
        this.addEndPoint("DB_AGENT_CONFIG_DETAILS", '/PlatformService/admin/agentConfiguration/getRegisteredAgentDetail');
        this.addEndPoint("AGENT_UNINSTALL", '/PlatformService/admin/agentConfiguration/uninstallAgent');

        this.addEndPoint("DATA_DICTIONARY_TOOLS_AND_CATEGORY", '/PlatformService/datadictionary/getToolsAndCategories');
        this.addEndPoint("DATA_DICTIONARY_TOOL_PROPERTIES", '/PlatformService/datadictionary/getToolProperties');
        this.addEndPoint("DATA_DICTIONARY_TOOLS_RELATIONSHIPS", '/PlatformService/datadictionary/getToolsRelationshipAndProperties');
        this.addEndPoint("INSIGHTS_LOG", '/PlatformService/insights/log');
        //Audit Reporting
        this.addEndPoint("GET_ALL_ASSETS", '/PlatformAuditService/traceability/getAllAssets');
        this.addEndPoint("GET_ASSET_INFO", '/PlatformAuditService/traceability/getAssetInfo');
        this.addEndPoint("GET_ASSET_HISTORY", '/PlatformAuditService/traceability/getAssetHistory');
        this.addEndPoint("EXPORT_TO_PDF", '/PlatformAuditService/traceability/getAuditReport');
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
        //console.log(InsightsInitService.serviceHost.toString().concat(this.apiMap.get(moduleUrlKey).toString()));
        return InsightsInitService.serviceHost.toString().concat(this.apiMap.get(moduleUrlKey).toString());
    }
}
