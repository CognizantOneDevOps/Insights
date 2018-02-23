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
    export interface IUserOnboardingService {
        getCurrentUserOrgs(): ng.IPromise<any>;
        switchUserOrg(orgId: number): ng.IPromise<any>;
        getGrafanaCurrentOrgAndRole(): ng.IPromise<any>;
        getAllOrg(): ng.IPromise<any>;
        getOrgUserInfo(orgId: number): ng.IPromise<any>;
        userSearch(input: string): ng.IPromise<any>;
        createUser(userName: string, userEmail: string, userLogin: string): ng.IPromise<any>;
        getAllUsers(): ng.IPromise<any>;
        addUserToOrg(orgId: number, orgName: string, userLogin: string, role: string): ng.IPromise<any>;
        createOrg(orgName: string): ng.IPromise<any>;
        deleteUserFromOrg(userId: number, orgId: number): ng.IPromise<any>;
        addUser(name: string, email: string, login: string): ng.IPromise<any>;
        updateUserRoleOrg(orgId: number, userId: number, role: string): ng.IPromise<any>;
    }

    export class UserOnboardingService implements IUserOnboardingService {
        static $inject = ['$q', '$resource', '$cookies', 'restCallHandlerService'];
        constructor(private $q: ng.IQService, private $resource, private $cookies, private restCallHandlerService: IRestCallHandlerService) {
        }

        getCurrentUserOrgs(): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.get("ACCESS_GROUP_MANAGEMENT_GET_CURRENT_USER_ORGS");
        }

        switchUserOrg(orgId: number): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.post("ACCESS_GROUP_MANAGEMENT_SWITCH_ORGS", { "orgId": orgId }, { 'Content-Type': 'application/x-www-form-urlencoded' });
        }

        getGrafanaCurrentOrgAndRole(): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.get("GRAPANA_CURRENT_ROLE_ORG");
        }

        getAllOrg(): ng.IPromise<any> {

            var restHandler = this.restCallHandlerService;
            return restHandler.get("ORGS_GET");
        }

        getOrgUserInfo(orgId: number): ng.IPromise<any> {

            var restHandler = this.restCallHandlerService;
            return restHandler.post("ORG_USERS_GET", { "orgId": orgId }, { 'Content-Type': 'application/x-www-form-urlencoded' });
        }

        userSearch(input: string): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.get("USER_SEARCH", { 'query': input });
        }

        createUser(userName: string, userEmail: string, userLogin: string): ng.IPromise<any> {

            var restHandler = this.restCallHandlerService;
            return restHandler.post("USER_ADD", { "orgId": 1 }, { 'Content-Type': 'application/x-www-form-urlencoded' });
        }

        getAllUsers(): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.get("ALL_USERS");
        }

        addUserToOrg(orgId: number, orgName: string, userLogin: string, role: string): ng.IPromise<any> {

            var restHandler = this.restCallHandlerService;
            return restHandler.post("USER_TO_ORG_ADD", { "orgId": orgId, "orgName": orgName, "user": userLogin, "role": role }, { 'Content-Type': 'application/x-www-form-urlencoded' });
        }

        createOrg(orgName: string): ng.IPromise<any> {

            var restHandler = this.restCallHandlerService;
            return restHandler.post("ORG_CREATE", { "orgName": orgName }, { 'Content-Type': 'application/x-www-form-urlencoded' });
        }

        deleteUserFromOrg(userId: number, orgId: number): ng.IPromise<any> {

            var restHandler = this.restCallHandlerService;
            return restHandler.post("USER_ORG_DELETE", { "userId": userId, "orgId": orgId }, { 'Content-Type': 'application/x-www-form-urlencoded' });
        }

        addUser(name: string, email: string, login: string): ng.IPromise<any> {

            var restHandler = this.restCallHandlerService;
            return restHandler.post("USER_ADD", { "name": name, "email": email, "login": login }, { 'Content-Type': 'application/x-www-form-urlencoded' });
        }

        updateUserRoleOrg(orgId: number, userId: number, role: string): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.post("USER_ROLE_INORG_UPDATE", { "orgId": orgId, "userId": userId, "role": role }, { 'Content-Type': 'application/x-www-form-urlencoded' });
        }
    }
}