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
    export interface IDataTaggingDetailsService {
        
        getHierarchyMapping(): ng.IPromise<any>;
        getHierarchyProperties(level1:string,level2:string,level3:string,level4:string): ng.IPromise<any>;
    }

    export class DataTaggingDetailsService implements IDataTaggingDetailsService {
        static $inject = ['$q', '$resource', '$cookies', 'restCallHandlerService'];
        constructor(private $q: ng.IQService, private $resource, private $cookies, private restCallHandlerService: IRestCallHandlerService) {
        }

               
        getHierarchyMapping(): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.get("HIERARCHY_ALL_DETAILS_GET",{'Content-Type': 'application/x-www-form-urlencoded'});
        }

        getHierarchyProperties(level1:string,level2:string,level3:string,level4:string): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.get("GET_HIERARCHY_PROPERTIES",{"level1":level1,"level2":level2,"level3":level3,"level4":level4},{'Content-Type': 'application/x-www-form-urlencoded'});
        }


    }
}