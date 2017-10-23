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
    export interface IRoleService {
        getAllOrg(): ng.IPromise<any>;
        getOrgUserInfo(orgId: number): ng.IPromise<any>;
        userSearch(input: string): ng.IPromise<any>;
        createUser(userName:string, userEmail:string, userLogin:string): ng.IPromise<any>;
        getAllUsers(): ng.IPromise<any>;
        addUserToOrg(orgId: number, userLogin: string, role:string): ng.IPromise<any>;
        createOrg(orgName: string): ng.IPromise<any>;
        deleteUserFromOrg(userId: number, orgId:number): ng.IPromise<any>;
        addUser(userName: string, email: string, login:string): ng.IPromise<any>;
        updateUserRoleOrg(orgId: number, userId: number, role:string): ng.IPromise<any>;
    }

    export class RoleService implements IRoleService {
        static $inject = ['$q', '$resource', '$cookies', 'restCallHandlerService', 'restEndpointService'];
        constructor(private $q: ng.IQService, private $resource, private $cookies, private restCallHandlerService: IRestCallHandlerService,, private restEndpointService ) {
        }

        getAllOrg(): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.get("ORGS_GET");
        }

        getOrgUserInfo(orgId: number): ng.IPromise<any> {
     
            var restHandler = this.restCallHandlerService;
            return restHandler.post("ORG_USERS_GET",{"orgId":orgId},{'Content-Type': 'application/x-www-form-urlencoded'});
        }

        userSearch(input: string): ng.IPromise<any>{
            var restHandler = this.restCallHandlerService;
            return restHandler.get("USER_SEARCH",{'query':input});
       }

      createUser(userName:string, userEmail:string, userLogin:string): ng.IPromise<any> {
           var authToken = this.$cookies.get('Authorization');
           var orgUserData = this.$resource(this.restEndpointService.getServiceHost() + 'PlatformService/userMgmt/addUser',
               {},
               {
                   allOrgUserData: {
                       method: 'POST',
                       headers: {
                           'Authorization': authToken,
                           'Content-Type': 'application/x-www-form-urlencoded'
                       },
                       transformRequest: function(data) {
                         if (data && data.userName) {
                           return 'orgId='+data.orgId;
                         }
                         return;
                       }
                   }
               });
           return orgUserData.allOrgUserData({"orgId":1}).$promise;
       }

       getAllUsers(): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.get("ALL_USERS");
       }

       addUserToOrg(orgId: number, userLogin: string, role:string): ng.IPromise<any> {

           var restHandler = this.restCallHandlerService;
           return restHandler.post("USER_TO_ORG_ADD",{"orgId":orgId, "userLogin":userLogin, "role":role},{'Content-Type': 'application/x-www-form-urlencoded'});
       }

       createOrg(orgName: string): ng.IPromise<any> {
     
           var restHandler = this.restCallHandlerService;
           return restHandler.post("ORG_CREATE",{"orgName":orgName},{'Content-Type': 'application/x-www-form-urlencoded'});
       }

       deleteUserFromOrg(userId: number, orgId:number): ng.IPromise<any> {
 
           var restHandler = this.restCallHandlerService;
           return restHandler.post("USER_ORG_DELETE",{"userId":userId, "orgId":orgId},{'Content-Type': 'application/x-www-form-urlencoded'});
       }

       addUser(userName: string, email: string, login:string): ng.IPromise<any> {
       
           var restHandler = this.restCallHandlerService;
           return restHandler.post("USER_ADD",{"userName":userName, "email":email, "login":login},{'Content-Type': 'application/x-www-form-urlencoded'});
       }

       updateUserRoleOrg(orgId: number, userId: number, role:string): ng.IPromise<any> {
 
           var restHandler = this.restCallHandlerService;
           return restHandler.post("USER_ROLE_INORG_UPDATE",{"orgId":orgId, "userId":userId, "role":role},{'Content-Type': 'application/x-www-form-urlencoded'});
       }
    }
}
