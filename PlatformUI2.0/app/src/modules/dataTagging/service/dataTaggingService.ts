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
    export interface IDataTaggingService {
        addEntityDefination(rowId: number, levelName: string, entityName: string): ng.IPromise<any>;
        deleteEntityDefination(levelName: string, entityName: string): ng.IPromise<any>;
        getAllEntityDefination(): ng.IPromise<any>;
        getEntityDataByLevelName(levelName: string): ng.IPromise<any>;
        addEntityData(rowId: number, level1: string, level2: string, level3: string, level4: string, level5: string,
            level6: string, hierarchyName: string): ng.IPromise<any>;
        getAllEntityData(): ng.IPromise<any>;
        deleteEntityDataByHierarchy(hierarchyName: string): ng.IPromise<any>;
        addHierarchyMapping(rowId: number, hierarchyName: string, orgName: string, orgId: number): ng.IPromise<any>;
        getAllHierarchyMapping(): ng.IPromise<any>;
        deleteHierarchyMap(hierarchyName: string, orgName: string): ng.IPromise<any>;
        getHierarchyMappingByName(hierarchyName: string): ng.IPromise<any>;
        getHierarchyDetails(): ng.IPromise<any>
    }

    export class DataTaggingService implements IDataTaggingService {
        static $inject = ['$q', '$resource', '$cookies', 'restCallHandlerService'];
        constructor(private $q: ng.IQService, private $resource, private $cookies, private restCallHandlerService: IRestCallHandlerService) {
        }

        addEntityDefination(rowId: number, levelName: string, entityName: string): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.post("ENTITY_DEFINITION_ADD",{"rowId": rowId, "levelName": levelName, "entityName": entityName },{'Content-Type': 'application/x-www-form-urlencoded'});
        }

        deleteEntityDefination(levelName: string, entityName: string): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.post("ENTITY_DEFINITION_REMOVE",{ "levelName": levelName, "entityName": entityName },{'Content-Type': 'application/x-www-form-urlencoded'});
        }

        getAllEntityDefination(): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.get("ENTITY_DEFINITION_ALL");
        }

        getEntityDataByLevelName(levelName: string): ng.IPromise<any> {
            
            var restHandler = this.restCallHandlerService;
            return restHandler.post("ENTITY_BY_LEVEL",{ "levelName": levelName },{'Content-Type': 'application/x-www-form-urlencoded'});
        }

        addEntityData(rowId: number, level1: string, level2: string, level3: string, level4: string, level5: string,
            level6: string, hierarchyName: string): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.post("HIERARCHY_DETAILS_ADD",{
                "rowId": rowId, "level1": level1, "level2": level2, "level3": level3, "level4": level4, "level5": level5,
                "level6": level6, "hierarchyName": hierarchyName
            },{'Content-Type': 'application/x-www-form-urlencoded'});
        }
        
        getAllEntityData(): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.get("ENTITY_DATA_ALL");
        }
        
        deleteEntityDataByHierarchy(hierarchyName: string): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.post("HIERARCHY_DETAILS_REMOVE",{ "hierarchyName": hierarchyName },{'Content-Type': 'application/x-www-form-urlencoded'});
        }
        
        getHierarchyDetails(): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.get("HIERARCHY_DETAILS_GET");
        }
        
        addHierarchyMapping(rowId: number, hierarchyName: string, orgName: string, orgId: number): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.post("HIERARCHY_MAPPING_ADD",{"rowId": rowId, "hierarchyName": hierarchyName, "orgName": orgName, "orgId": orgId },{'Content-Type': 'application/x-www-form-urlencoded'});
        }
        
        getAllHierarchyMapping(): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.get("HIERARCHY_DATA_ALL");
        }
        
        deleteHierarchyMap(hierarchyName: string, orgName: string): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.post("HIERARCHY_MAPPING_REMOVE",{ "hierarchyName": hierarchyName, "orgName": orgName },{'Content-Type': 'application/x-www-form-urlencoded'});
        }
        
        getHierarchyMappingByName(hierarchyName: string): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.post("HIERARCHY_MAPPING",{ "hierarchyName": hierarchyName },{'Content-Type': 'application/x-www-form-urlencoded'});
        }

    }
}