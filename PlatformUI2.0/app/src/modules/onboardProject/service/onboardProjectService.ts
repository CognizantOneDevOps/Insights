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

/// <reference path="../../../_all.ts" />

module ISightApp {
    export interface IOnboardProjectService {
        getAllOrg(): ng.IPromise<any>;
        addProjectMapping(orgId: number, rowId: number, category: string, toolName: string, fieldName: string, fieldValue: string,
            projectName: string, projectId: string, businessUnit: string, hierarchyName: string): ng.IPromise<any>;
        removeProjectMapping(orgId: number): ng.IPromise<any>;
        fetchProjectMappingByOrgId(orgId: number): ng.IPromise<any>;
        getToolName(): ng.IPromise<any>;
        getPrjtMappingFields(toolName: string): ng.IPromise<any>;
        getPrjtMappingFieldVal(toolName: string, fieldName: string): ng.IPromise<any>;
        getToolcat(toolName: string): ng.IPromise<any>;
        fetchAllProjectMapping(): ng.IPromise<any>;
        fetchProjectMappingByHierarchyName(hierarchyName: string): ng.IPromise<any>;
        getAllHierarchyName(): ng.IPromise<any>;
    }

    export class OnboardProjectService implements IOnboardProjectService {
        static $inject = ['$q', '$resource', '$cookies', 'restEndpointService', 'restCallHandlerService'];
        constructor(private $q: ng.IQService, private $resource, private $cookies, private restEndpointService: IRestEndpointService, private restCallHandlerService: IRestCallHandlerService) {
        }

        getAllOrg(): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.get("ORGS_GET");
        }

        addProjectMapping(orgId: number, rowId: number, category: string, toolName: string, fieldName: string, fieldValue: string,
            projectName: string, projectId: string, businessUnit: string, hierarchyName: string): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.post("PROJECT_MAPPING_ADD",{"orgId": orgId, "rowId": rowId, "category": category, "toolName": toolName, "fieldName": fieldName, "fieldValue": fieldValue,"projectName": projectName, "projectId": projectId, "businessUnit": businessUnit, "hierarchyName": hierarchyName
            },{'Content-Type': 'application/x-www-form-urlencoded'});
        }

        removeProjectMapping(orgId: number): ng.IPromise<any> {
           
            var restHandler = this.restCallHandlerService;
            return restHandler.post("PROJECT_MAPPING_REMOVE",{ "orgId": orgId },{'Content-Type': 'application/x-www-form-urlencoded'});
                        
        }

        fetchProjectMappingByHierarchyName(hierarchyName: string): ng.IPromise<any> {
           
            var restHandler = this.restCallHandlerService;
            return restHandler.get("PROJECT_MAPPING_BY_HIERARCHY",{'hierarchyName':hierarchyName});
        }
        
        fetchProjectMappingByOrgId(orgId: number): ng.IPromise<any> {

            var restHandler = this.restCallHandlerService;
            return restHandler.get("PROJECT_MAPPING_BY_ORGID",{'orgId':orgId});
        }

        deleteToolMapping(orgId: number, category: string, toolName: string, toolId: string): ng.IPromise<any> {
  
            var restHandler = this.restCallHandlerService;
            return restHandler.post("TOOL_MAPPING_DELETE",{ "orgId": orgId, "category": category, "toolName": toolName, "rowId": toolId },{'Content-Type': 'application/x-www-form-urlencoded'});
        }

        getToolName(): ng.IPromise<any> {
  
            var restHandler = this.restCallHandlerService;
            return restHandler.get("TOOL_NAME_GET");
        }

        getPrjtMappingFields(toolName: string): ng.IPromise<any> {
    
            var restHandler = this.restCallHandlerService;
            return restHandler.get("MAPPING_DATA",{'toolName':toolName});
        }

        getPrjtMappingFieldVal(toolName: string, fieldName: string): ng.IPromise<any> {
  
            var restHandler = this.restCallHandlerService;
            return restHandler.get("MAPPING_FIELD_VAL",{'toolName':toolName,'fieldName':fieldName});
        }

        getToolcat(toolName: string): ng.IPromise<any> {

            var restHandler = this.restCallHandlerService;
            return restHandler.get("TOOL_CATEGORY",{'toolName':toolName});
        }

        fetchAllProjectMapping(): ng.IPromise<any> {

            var restHandler = this.restCallHandlerService;
            return restHandler.get("PROJECT_MAPPING");
        }
        
        getAllHierarchyName(): ng.IPromise<any> {

            var restHandler = this.restCallHandlerService;
            return restHandler.get("DISTINCT_HIERARCHY");
        }
    }
}
