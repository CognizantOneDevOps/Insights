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
    export interface IDataDictionaryService {
        getToolsAndCategories(): ng.IPromise<any>;
        getToolProperties(toolName: string, categoryName: string): ng.IPromise<any>;
        getToolsRelationshipAndProperties(startToolName: string, startToolCategory: string, endToolName: string, endToolCatergory: string): ng.IPromise<any>;
    }

    export class DataDictionaryService implements IDataDictionaryService {
        static $inject = ['$resource', '$q', '$cookies', 'restEndpointService', 'restCallHandlerService'];
        constructor(private $resource, private $q, private $cookies, private restEndpointService: IRestEndpointService, private restCallHandlerService: IRestCallHandlerService) {
        }

        getToolsAndCategories(): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.get("DATA_DICTIONARY_TOOLS_AND_CATEGORY");
        }

        getToolProperties(toolName: string, categoryName: string): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.get("DATA_DICTIONARY_TOOL_PROPERTIES", { 'toolName': toolName, 'categoryName': categoryName });
        }

        getToolsRelationshipAndProperties(startToolName: string, startToolCategory: string, endToolName: string, endToolCatergory: string): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.get("DATA_DICTIONARY_TOOLS_RELATIONSHIPS", { 'startToolName': startToolName, 'startToolCategory': startToolCategory, 'endToolName': endToolName, 'endToolCatergory': endToolCatergory });
        }

    }
}
