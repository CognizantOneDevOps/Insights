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

    export interface IDashboardService {
        switchUserOrg(orgId: number): ng.IPromise<any>;
        loadOrganizations(): ng.IPromise<any>;
        loginUserAuthentication(): ng.IPromise<any>;
        getCurrentUserOrgs(): ng.IPromise<any>;
        getUsers(): ng.IPromise<any>;
    }

    export class DashboardService implements IDashboardService {
        static $inject = ['$resource', '$cookies', 'restCallHandlerService'];

        constructor(private $resource, private $cookies, private restCallHandlerService: IRestCallHandlerService) {
        }

        loadOrganizations(): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.get("ORGS_GET");
        }

        switchUserOrg(orgId: number): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.post("ACCESS_GROUP_MANAGEMENT_SWITCH_ORGS",{ "orgId": orgId },{'Content-Type': 'application/x-www-form-urlencoded'});
        }

        loginUserAuthentication(): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.post("USER_AUTHNTICATE");
        }

        getCurrentUserOrgs(): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.get("ACCESS_GROUP_MANAGEMENT_GET_CURRENT_USER_ORGS");
        }
        
        getUsers(): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
             return restHandler.get("ACCESS_GROUP_MANAGEMENT_GET_USERS");
        }
        
     }
}
