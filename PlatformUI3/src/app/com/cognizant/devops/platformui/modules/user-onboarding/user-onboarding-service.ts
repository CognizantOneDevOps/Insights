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
import { RestCallHandlerService } from '@insights/common/rest-call-handler.service';
import { Observable } from 'rxjs';

export interface IUserOnboardingService {
    getCurrentUserOrgs(): Promise<any>;
    switchUserOrg(orgId: number): Promise<any>;
    getGrafanaCurrentOrgAndRole(): Promise<any>;
    getGrafanaCurrentVersion(): Promise<any>;
}

@Injectable()
export class UserOnboardingService implements IUserOnboardingService {

    constructor(private restHandler: RestCallHandlerService) {
    }

    getCurrentUserOrgs(): Promise<any> {
        return this.restHandler.get("ACCESS_GROUP_MANAGEMENT_GET_CURRENT_USER_ORGS");
    }


    getUser(): Promise<any> {
        return this.restHandler.get("ACCESS_GROUP_MANAGEMENT_GET_USERS");
    }

    getCurrentOrgRole(): Promise<any> {
        return this.restHandler.get("ACCESS_GROUP_MANAGEMENT_GET_USERS_ORGS")
    }


    getOrganizationUsers(orgId: number): Promise<any> {
        return this.restHandler.postWithParameter("ORG_USERS_GET", { "orgId": orgId }, { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }

    switchUserOrg(orgId: number): Promise<any> {
        return this.restHandler.postWithParameter("ACCESS_GROUP_MANAGEMENT_SWITCH_ORGS", { "orgId": orgId }, { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }

    getGrafanaCurrentOrgAndRole(): Promise<any> {
        return this.restHandler.get("GRAPANA_CURRENT_ROLE_ORG");
    }

    getGrafanaCurrentVersion(): Promise<any> {
        return this.restHandler.get("GET_GRAFANA_VERSION");
    }

    editUserOrg(orgId: number, userId: number, role: String): Promise<any> {
        return this.restHandler.postWithParameter("ACCESS_GROUP_MANAGEMENT_EDIT_ORGS_UESRS", { "orgId": orgId, "userId": userId, "role": role }, { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }

    deleteUserOrg(orgId: number, userId: number, role: String): Promise<any> {
        return this.restHandler.postWithParameter("ACCESS_GROUP_MANAGEMENT_DELETE_ORGS_UESRS", { "orgId": orgId, "userId": userId, "role": role }, { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }

    createOrg(orgName: string): Promise<any> {
        return this.restHandler.postWithParameter("ORG_CREATE", { "orgName": orgName }, { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }

    /*  addUserInOrg(name: string, email: string, userName: string, role: string, orgId: number, orgName: string, pass: string) {
         return this.restHandler.postWithParameter("USER_CREATE", { "orgId": orgId, "name": name, "email": email, "role": role, "userName": userName, "orgName": orgName, "password": pass }, { 'Content-Type': 'application/x-www-form-urlencoded' })
     }
  */

    addUserInOrg(userPropertyList: string) {
        return this.restHandler.postWithData("USER_CREATE", userPropertyList, "", { 'Content-Type': 'application/x-www-form-urlencoded' })
    }
    assignUser(assignUserList: string) {
        return this.restHandler.postWithData("ASSIGN_USER", assignUserList, "", { 'Content-Type': 'application/json' })
    }
    getUsersOrganisation(userName: string) {
        return this.restHandler.postWithData("USER_ORG_SEARCH", userName, "", { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }


}

